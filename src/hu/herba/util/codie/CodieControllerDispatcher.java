package hu.herba.util.codie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.bluetooth.CodieBluetoothConnectionFactory;
import hu.herba.util.bluetooth.CodieClientSession;

/**
 * Servlet implementation class CodieControllerDispatcher
 */
public class CodieControllerDispatcher {
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerDispatcher.class);

	private CodieClientSession conn;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CodieControllerDispatcher() {
		super();
	}

	private void initConnection() {
		// initialize connection to Codie
		if (conn == null) {
			conn = CodieBluetoothConnectionFactory.connect();
			if (conn != null) {
				CodieSensorPollService.getInstance().resetTimers(conn);
			}
		}
	}

	public void handleRequest(final String servletPath, final Writer out) throws IOException {
		String uri = servletPath.startsWith("/") ? servletPath.substring(1) : servletPath;
		LOGGER.trace("URI:" + uri);
		switch (uri) {
		case "crossdomain.xml":
			sendCrossDomainXml(out);
			break;
		case "reset_all":
			resetDevice();
			break;
		case "poll":
			initConnection();
			pollRequest(out);
			break;
		default:
			if (uri.indexOf('.') != -1) {
				sendFile(out, uri);
			} else {
				CodieCommandProcessor.getInstance().handleCommand(out, uri);
			}
		}
	}

	private void resetDevice() {
		LOGGER.info("Reset connection to Codie...");
		CodieSensorPollService.getInstance().doReset();
		CodieCommandProcessor.getInstance().doReset();
	}

	@Override
	protected void finalize() throws Throwable {
		LOGGER.info("Shutdown codie controller...");
		CodieSensorPollService.getInstance().cancelTimers();
		super.finalize();
	}

	private void pollRequest(final Writer out) throws IOException {
		// out.append("CodieController 1.0\n");
		if (conn == null) {
			out.append("_problem NOT CONNECTED");
		} else {
			CodieCommandProcessor.getInstance().providePollInfo(out);
		}
	}

	private void sendCrossDomainXml(final Writer out) throws IOException {
		sendFile(out, "crossdomain.xml");
		out.append((char) 0x00);
	}

	private void sendFile(final Writer out, final String fileName) throws IOException {
		BufferedReader reader = null;
		try {
			LOGGER.debug("Open file: " + fileName);
			InputStream is = this.getClass().getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName);
			if (is == null) {
				out.append("File not found: " + fileName + "!");
				LOGGER.error("File not found: " + fileName + "!");
			} else {
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					out.append(line).append('\n');
				}
			}
		} catch (UnsupportedEncodingException e) {
			out.append("Failure: " + e.getMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

	}

}
