/**
 *
 */
package hu.herba.util.bluetooth.mock;

import java.io.IOException;

import javax.obex.HeaderSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author csorbazoli
 *
 */
public class MockHeaderSet implements HeaderSet {
	private static final Logger LOGGER = LogManager.getLogger(MockHeaderSet.class);
	private static final String LOG_PREFIX = "MockedHeaderSet: ";
	private static int idCnt = 0;

	private final int id;

	public MockHeaderSet() {
		id = getNextId();
	}

	private static synchronized int getNextId() {
		return ++idCnt;
	}

	@Override
	public void createAuthenticationChallenge(final String s, final boolean flag, final boolean flag1) {
		LOGGER.trace(LOG_PREFIX + " createAuthenticationChallenge");
	}

	@Override
	public Object getHeader(final int idx) throws IOException {
		LOGGER.trace(LOG_PREFIX + " getHeader#" + idx);
		return null;
	}

	@Override
	public int[] getHeaderList() throws IOException {
		LOGGER.trace(LOG_PREFIX + " getHeaderList");
		return null;
	}

	@Override
	public int getResponseCode() throws IOException {
		LOGGER.trace(LOG_PREFIX + " getResponseCode");
		return 0;
	}

	@Override
	public void setHeader(final int idx, final Object obj) {
		LOGGER.trace(LOG_PREFIX + " setHeader " + idx + " = " + obj);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		return id == ((MockHeaderSet) obj).id;
	}

}
