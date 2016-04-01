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
	public static final String CODIE_MOCK_CONNECTION = "Codie mock connection: ";
	private static long connectionIdx = 0l;
	private Long connectionId;
	private final Map<HeaderSet, CodieMockOperation> operations = Collections.synchronizedMap(new HashMap<HeaderSet, CodieMockOperation>());

	@Override
	public void close() throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "close...");
		CodieMockOperation.stopResponseThread();
	}

	@Override
	public HeaderSet connect(final HeaderSet arg0) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "connect...");
		return createHeaderSet();
	}

	@Override
	public HeaderSet createHeaderSet() {
		LOGGER.trace(CODIE_MOCK_CONNECTION + "create header set...");
		return new MockHeaderSet();
	}

	@Override
	public HeaderSet delete(final HeaderSet headerSet) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "delete header set: " + headerSet);
		return null;
	}

	@Override
	public HeaderSet disconnect(final HeaderSet headerSet) throws IOException {
		LOGGER.info(CODIE_MOCK_CONNECTION + "disconnect...");
		CodieMockOperation.stopResponseThread();
		return null;
	}

	@Override
	public Operation get(final HeaderSet headerSet) throws IOException {
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
		LOGGER.debug(CODIE_MOCK_CONNECTION + "generate new connection id = " + (connectionIdx + 1));
		return ++connectionIdx;
	}

	@Override
	public Operation put(final HeaderSet headerSet) throws IOException {
		LOGGER.trace(CODIE_MOCK_CONNECTION + "put = " + headerSet);
		CodieMockOperation operation = new CodieMockOperation(headerSet);
		operations.put(headerSet, operation);
		return operation;
	}

	@Override
	public void setAuthenticator(final Authenticator authenticator) {
		LOGGER.trace(CODIE_MOCK_CONNECTION + "set authenticator = " + authenticator);
	}

	@Override
	public void setConnectionID(final long connId) {
		LOGGER.trace(CODIE_MOCK_CONNECTION + "set connection id = " + connId);
		connectionId = connId;
	}

	@Override
	public HeaderSet setPath(final HeaderSet headerSet, final boolean arg1, final boolean arg2) throws IOException {
		LOGGER.trace(CODIE_MOCK_CONNECTION + "set path = " + headerSet + " [" + arg1 + ", " + arg2 + "]");
		return null;
	}

}
