package hu.herba.util.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OBEXPutClient {
	private static final Logger LOGGER = LogManager.getLogger(OBEXPutClient.class);

	public static void main(final String[] args) throws IOException, InterruptedException {

		String serverURL;
		if (args != null && args.length > 0) {
			serverURL = args[0];
		} else {
			String searchArgs = null;
			// Connect to OBEXPutServer from examples
			// searchArgs = "11111111111111111111111111111123";
			ServicesSearch.search(searchArgs);
			if (ServicesSearch.serviceFound.size() == 0) {
				LOGGER.error("OBEX service not found");
				return;
			}
			// Select the first service found
			serverURL = ServicesSearch.serviceFound.get(0);
		}

		ClientSession clientSession = openSession(serverURL);
		if (clientSession == null) {
			LOGGER.error("Failed to connect");
			return;
		}

		sendTextMessage(clientSession, "Hello.txt", "Hello world!");

		closeSession(clientSession);
	}

	public static void closeSession(final ClientSession clientSession) throws IOException {
		clientSession.disconnect(null);

		clientSession.close();
	}

	public static void sendTextMessage(final ClientSession clientSession, final String name, final String content)
			throws IOException, UnsupportedEncodingException {
		LOGGER.info("Send text message: '" + name + "', with content '" + content + "'");
		sendByteArray(clientSession, name, content.getBytes("iso-8859-1"), "text");
	}

	public static void sendByteArray(final ClientSession clientSession, final String name, final byte[] data,
			final String type) throws IOException, UnsupportedEncodingException {
		LOGGER.info("Send message: '" + name + "', with content '" + data + "' of type = " + type);
		HeaderSet hsOperation = clientSession.createHeaderSet();
		hsOperation.setHeader(HeaderSet.NAME, name);
		hsOperation.setHeader(HeaderSet.TYPE, type);
		hsOperation.setHeader(HeaderSet.LENGTH, Long.valueOf(data.length));

		// Create PUT Operation
		Operation putOperation = clientSession.put(hsOperation);
		OutputStream os = putOperation.openOutputStream();
		os.write(data);
		os.close();

		int responseCode = putOperation.getResponseCode();
		Field[] fields = ResponseCodes.class.getFields();
		Field found = null;
		for (Field field : fields) {
			try {
				if (int.class.equals(field.getType()) && (field.getModifiers() & Modifier.STATIC) > 0
						&& responseCode == field.getInt(null)) {
					found = field;
					break;
				}
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Field is not a static int: " + field);
			} catch (IllegalAccessException e) {
				LOGGER.warn("Field is not a static int: " + field);
			}
		}
		if (found == null) {
			LOGGER.warn("Unknown responseCode: " + responseCode);
		} else {
			LOGGER.info("ResponseCode: " + found.getName() + " = " + responseCode);
		}
		putOperation.close();
	}

	/**
	 * @param serverURL
	 * @return
	 * @throws IOException
	 */
	public static ClientSession openSession(final String serverURL) throws IOException {
		LOGGER.info("Connecting to " + serverURL);

		ClientSession clientSession = (ClientSession) Connector.open(serverURL);
		HeaderSet hsConnectReply = clientSession.connect(null);
		if (hsConnectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
			LOGGER.error("Failed to connect: error code = " + hsConnectReply.getResponseCode());
			return null;
		}
		return clientSession;
	}
}