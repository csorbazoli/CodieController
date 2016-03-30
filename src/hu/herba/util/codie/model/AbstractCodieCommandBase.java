/**
 *
 */
package hu.herba.util.codie.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;

import org.apache.logging.log4j.Logger;

import hu.herba.util.bluetooth.CodieBluetoothConnectionFactory;
import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.SensorValueStore;

/**
 * @author csorbazoli
 *
 */
public abstract class AbstractCodieCommandBase implements CodieCommandBase, Comparable<CodieCommandBase> {

	// basic methods will be implemented here
	protected final DataPackage pack = new DataPackage();

	protected byte[] getDataPackage() {
		// return only the relevant part
		return pack.getPackage();

	}

	protected int sendCommand() throws CodieCommandException {
		int ret = 0;
		try {
			// push data on channel
			ret = sendByteArray(CodieBluetoothConnectionFactory.connect(), getCommandType().getCommandName(), getDataPackage(), "binary");
			// TODO wait for result and return it
		} catch (IOException e) {
			throw new CodieCommandException(e.getMessage(), e);
		}
		getSensorValueStore().setLastResult(ret == 0);
		return ret;
	}

	public int sendByteArray(final ClientSession clientSession, final String name, final byte[] data, final String type)
			throws IOException, UnsupportedEncodingException {
		int ret = 0;
		getLogger().info("Send message: '" + name + "', with content '" + data + "' of type = " + type);
		HeaderSet hsOperation = clientSession.createHeaderSet();
		hsOperation.setHeader(HeaderSet.NAME, name);
		hsOperation.setHeader(HeaderSet.TYPE, type);
		hsOperation.setHeader(HeaderSet.LENGTH, Long.valueOf(data.length));

		// Create PUT Operation
		Operation putOperation = clientSession.put(hsOperation);
		OutputStream os = putOperation.openOutputStream();
		os.write(data);
		os.close();

		ret = putOperation.getResponseCode();
		if (ret == 0) {

		} else {
			getLogger().warn("Response code failure: " + ret);
		}
		putOperation.close();
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
		if (obj == null || !this.getClass().equals(obj.getClass())) {
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
