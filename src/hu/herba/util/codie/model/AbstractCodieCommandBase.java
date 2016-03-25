/**
 *
 */
package hu.herba.util.codie.model;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.SensorValueStore;

/**
 * @author Zolt�n
 *
 */
public abstract class AbstractCodieCommandBase implements CodieCommandBase, Comparable<CodieCommandBase> {

	public abstract byte getInfoByte(boolean highPrio);

	// basic methods will be implemented here
	private int seq = 0;
	private final byte[] dataPackage = new byte[20];
	private int packageLength = 0;

	protected int getNextSequenceNumber() {
		int ret = ++seq;
		if (ret > 0xFFFF) {
			// restart sequence
			ret = seq = 1;
		}
		return ret;
	}

	/**
	 * @param from
	 * @param to
	 * @param highPrio
	 * @return
	 */
	protected byte getInfoByte(final int from, final int to, final boolean highPrio) {
		return (byte) (from | to | (highPrio ? HIGH : NORMAL));
	}

	public void prepareDataPackage(final int argLen) {
		prepareDataPackage(argLen, false);
	}

	protected void prepareDataPackage(final int argLen, final boolean highPrio) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits)
		byte info = getInfoByte(highPrio);
		dataPackage[packageLength++] = info;
		// SEQ (16 bits)
		int seq = getNextSequenceNumber();
		dataPackage[packageLength++] = (byte) (seq & 0x00FF);
		dataPackage[packageLength++] = (byte) (seq & 0x0FF00);
		// CMD (16 bits)
		int cmdId = getCommandId();
		dataPackage[packageLength++] = (byte) (cmdId & 0x00FF);
		dataPackage[packageLength++] = (byte) (cmdId & 0x0FF00);
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) (argLen & 0x0FF00);
		// ARGDAT - see addArgument
	}

	protected void addArgument(final int value, final ArgumentType argType) {
		switch (argType) {
		case I8:
			dataPackage[packageLength++] = (byte) (value & 0x00FF);
			break;
		case U16:
			dataPackage[packageLength++] = (byte) (value & 0x00FF);
			dataPackage[packageLength++] = (byte) (value & 0x0FF00);
			break;
		default:
			getLogger().error("Unhandled argument type: " + argType);
		}
	}

	protected byte[] getDataPackage() {
		// return only the relevant part
		return Arrays.copyOf(dataPackage, packageLength);
	}

	protected int sendCommand() {
		int ret = 0;
		byte[] data = getDataPackage();
		// TODO push data on channel
		// TODO wait for result and return it
		getSensorValueStore().updateSensorValue(SensorType.lastResult, ret);
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
			ret = getName().compareTo(o.getName());
			if (ret == 0) {
				ret = Integer.compare(getCommandId(), o.getCommandId());
			}
		}
		return ret;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (obj == null || !this.getClass().equals(obj.getClass())) {
			ret = false;
		} else {
			ret = getCommandId() == ((CodieCommandBase) obj).getCommandId();
		}
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 37;
		int hash = getCommandId();
		hash = hash * prime;
		return hash;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + getCommandId() + "]-" + getName();
	}

	/**
	 * @return
	 */
	protected abstract Logger getLogger();

}
