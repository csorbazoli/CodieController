/**
 *
 */
package hu.herba.util.codie.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

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
		int cmdId = getCommandType().getCommandId();
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

	protected int sendCommand() throws CodieCommandException {
		int ret = 0;
		try {
			sendByteArray(CodieBluetoothConnectionFactory.connect(), getCommandType().getCommandName(), getDataPackage(), "binary");
		} catch (IOException e) {
			throw new CodieCommandException(e.getMessage(), e);
		}
		// TODO push data on channel
		// TODO wait for result and return it
		getSensorValueStore().updateSensorValue(SensorType.lastResult, ret);
		return ret;
	}

	public void sendByteArray(final ClientSession clientSession, final String name, final byte[] data, final String type)
			throws IOException, UnsupportedEncodingException {
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

		int responseCode = putOperation.getResponseCode();
		Field[] fields = ResponseCodes.class.getFields();
		Field found = null;
		for (Field field : fields) {
			try {
				if (int.class.equals(field.getType()) && (field.getModifiers() & Modifier.STATIC) > 0 && responseCode == field.getInt(null)) {
					found = field;
					break;
				}
			} catch (IllegalArgumentException e) {
				getLogger().warn("Field is not a static int: " + field);
			} catch (IllegalAccessException e) {
				getLogger().warn("Field is not a static int: " + field);
			}
		}
		if (found == null) {
			getLogger().warn("Unknown responseCode: " + responseCode);
		} else {
			getLogger().info("ResponseCode: " + found.getName() + " = " + responseCode);
		}
		putOperation.close();
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
