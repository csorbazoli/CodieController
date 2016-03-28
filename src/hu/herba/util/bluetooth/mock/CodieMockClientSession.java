/**
 *
 */
package hu.herba.util.bluetooth.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.obex.Authenticator;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author zcsorba
 *
 */
public class CodieMockClientSession implements ClientSession {
	private static final Logger LOGGER = LogManager.getLogger(CodieMockClientSession.class);
	private static final String CODIE_MOCK_CONNECTION = "Codie mock connection: ";
	private static long connectionIdx = 0l;
	private Long connectionId;
	private Map<HeaderSet, CodieMockOperation> operations = Collections.synchronizedMap(new HashMap<HeaderSet, CodieMockOperation>());

	/* (non-Javadoc)
	 * @see javax.microedition.io.Connection#close()
	 */
	@Override
	public void close() throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "close...");
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#connect(javax.obex.HeaderSet)
	 */
	@Override
	public HeaderSet connect(HeaderSet arg0) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "connect...");
		return createHeaderSet();
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#createHeaderSet()
	 */
	@Override
	public HeaderSet createHeaderSet() {
		LOGGER.info(CODIE_MOCK_CONNECTION + "create header set...");
		HeaderSet ret = new HeaderSet() {
			@Override
			public void createAuthenticationChallenge(String s, boolean flag, boolean flag1) {
				LOGGER.info(CODIE_MOCK_CONNECTION + "-headerSet: createAuthenticationChallenge");
			}

			@Override
			public Object getHeader(int idx) throws IOException {
				LOGGER.info(CODIE_MOCK_CONNECTION + "-headerSet: getHeader#" + idx);
				return null;
			}

			@Override
			public int[] getHeaderList() throws IOException {
				LOGGER.info(CODIE_MOCK_CONNECTION + "-headerSet: getHeaderList");
				return null;
			}

			@Override
			public int getResponseCode() throws IOException {
				LOGGER.info(CODIE_MOCK_CONNECTION + "-headerSet: getResponseCode");
				return 0;
			}

			@Override
			public void setHeader(int idx, Object obj) {
				LOGGER.info(CODIE_MOCK_CONNECTION + "-headerSet: setHeader " + idx + " = " + obj);
			}

		};
		return ret;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#delete(javax.obex.HeaderSet)
	 */
	@Override
	public HeaderSet delete(HeaderSet headerSet) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "delete header set: " + headerSet);
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#disconnect(javax.obex.HeaderSet)
	 */
	@Override
	public HeaderSet disconnect(HeaderSet headerSet) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "disconnect...");
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.obex.ClientSession#get(javax.obex.HeaderSet)
	 */
	@Override
	public Operation get(HeaderSet headerSet) throws IOException {
		return operations.get(headerSet);
	}

	@Override
	public long getConnectionID() {
		if (connectionId == null) {
			connectionId = getNextConnectionId();
		}
		return connectionId;
	}

	private static synchronized Long getNextConnectionId() {
		LOGGER.info(CODIE_MOCK_CONNECTION + "generate new connection id = " + (connectionIdx + 1));
		return ++connectionIdx;
	}

	@Override
	public Operation put(HeaderSet headerSet) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "put = " + headerSet);
		CodieMockOperation operation = new CodieMockOperation(headerSet);
		operations.put(headerSet, operation);
		return operation;
	}

	@Override
	public void setAuthenticator(Authenticator authenticator) {
		LOGGER.info(CODIE_MOCK_CONNECTION + "set authenticator = " + authenticator);
	}

	@Override
	public void setConnectionID(long connId) {
		LOGGER.info(CODIE_MOCK_CONNECTION + "set connection id = " + connId);
		connectionId = connId;
	}

	@Override
	public HeaderSet setPath(HeaderSet headerSet, boolean arg1, boolean arg2) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "set path = " + headerSet + " [" + arg1 + ", " + arg2 + "]");
		return null;
	}

}
