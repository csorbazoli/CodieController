/**
 *
 */
package hu.herba.util.codie.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.Logger;

import hu.herba.util.bluetooth.CodieBluetoothConnectionFactory;
import hu.herba.util.bluetooth.CodieClientSession;
import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.SensorValueStore;

/**
 * @author csorbazoli
 *
 */
public abstract class AbstractCodieCommandBase implements CodieCommandBase, Comparable<CodieCommandBase> {

	// basic methods will be implemented here
	protected final DataPackage request = new DataPackage();

	protected byte[] getRequestDataPackage() {
		// return only the relevant part
		return request.getPackage();
	}

	protected int sendCommand() throws CodieCommandException {
		int ret = 0;
		try {
			getLogger().trace("SendCommand: " + toString() + "#" + hashCode());
			// push data on channel
			ret = sendByteArray(CodieBluetoothConnectionFactory.connect(), getCommandType().getCommandName(), getRequestDataPackage());
		} catch (IOException e) {
			throw new CodieCommandException(e.getMessage(), e);
		}
		if (this instanceof CodieCommand) {
			getSensorValueStore().setLastResult(ret == 0);
		} // don't need to set lastResult for sensor requests
		return ret;
	}

	public int sendByteArray(final CodieClientSession clientSession, final String name, final byte[] data) throws IOException, UnsupportedEncodingException {
		int ret = 0;
		getLogger().trace("Send message: '" + name + "', with content '" + data + "'");
		ret = clientSession.sendCommand(name, data);
		if (ret != 0) {
			getLogger().warn("Response code failure for " + name + ": " + ret);
		}
		return ret;
	}

	protected SensorValueStore getSensorValueStore() {
		return CodieSensorPollService.getInstance();
	}

	@Override
	public int compareTo(final CodieCommandBase o) {
		int ret;
		if (o == null) {
			ret = 1;
		} else {
			ret = getCommandType().compareTo(o.getCommandType());
		}
		return ret;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if ((obj == null) || !this.getClass().equals(obj.getClass())) {
			ret = false;
		} else {
			ret = getCommandType().equals(((CodieCommandBase) obj).getCommandType());
		}
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 37;
		int hash = getCommandType().getCommandId();
		hash = hash * prime;
		return hash;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + getCommandType().getCommandId() + "]-" + getCommandType().getCommandName();
	}

	/**
	 * @return
	 */
	protected abstract Logger getLogger();

}
