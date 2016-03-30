/**
 *
 */
package hu.herba.util.codie.model;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author csorbazoli
 *
 */
public class DataPackage {
	private static final Logger LOGGER = LogManager.getLogger(DataPackage.class);
	private final byte[] dataPackage = new byte[20];
	private int packageLength = 0;
	private int seq = 0;

	private int getNextSequenceNumber() {
		int ret = ++seq;
		if (ret > 0xFFFF) {
			// restart sequence
			ret = seq = 1;
		}
		return ret;
	}

	private byte getInfoByte(final CodieRole from, final CodieRole to, final boolean highPrio) {
		return (byte) (from.ordinal() << 4 | to.ordinal() << 6 | (highPrio ? CodieCommandBase.HIGH : CodieCommandBase.NORMAL));
	}

	public void prepareRequest(final CodieCommandBase cmd, final int argLen) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits)
		byte info = getInfoByte(cmd.getSender(), cmd.getDestination(), false);
		dataPackage[packageLength++] = info;
		// SEQ (16 bits)
		int seq = getNextSequenceNumber();
		dataPackage[packageLength++] = (byte) (seq & 0x00FF);
		dataPackage[packageLength++] = (byte) ((seq & 0x0FF00) >> 8);
		// CMD (16 bits)
		int cmdId = cmd.getCommandType().getCommandId();
		dataPackage[packageLength++] = (byte) (cmdId & 0x00FF);
		dataPackage[packageLength++] = (byte) ((cmdId & 0x0FF00) >> 8);
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) ((argLen & 0x0FF00) >> 8);
		// ARGDAT - see addArgument
	}

	public void prepareResponse(final byte[] requestPack, final int argLen) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits), swap sender/destination
		dataPackage[packageLength++] = (byte) (requestPack[0] | 0x0F | requestPack[0] & 0x00C0 >> 2 | requestPack[0] & 0x0030 << 2);
		// SEQ (16 bits)
		dataPackage[packageLength++] = requestPack[1];
		dataPackage[packageLength++] = requestPack[2];
		// CMD (16 bits)
		dataPackage[packageLength++] = requestPack[3];
		dataPackage[packageLength++] = (byte) (requestPack[4] | 0x0080); // set MSB = 1
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) ((argLen & 0x0FF00) >> 8);
		// ARGDAT - see addArgument
	}

	public void prepareResponse(final CodieCommandBase cmd, final int seq, final int argLen) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits)
		byte info = getInfoByte(cmd.getSender(), cmd.getDestination(), false);
		dataPackage[packageLength++] = info;
		// SEQ (16 bits)
		dataPackage[packageLength++] = (byte) (seq & 0x00FF);
		dataPackage[packageLength++] = (byte) ((seq & 0x0FF00) >> 8);
		// CMD (16 bits)
		int cmdId = cmd.getCommandType().getCommandId();
		dataPackage[packageLength++] = (byte) (cmdId & 0x00FF);
		dataPackage[packageLength++] = (byte) ((cmdId & 0x0FF00) >> 8 | 0x0080); // set MSB = 1
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) ((argLen & 0x0FF00) >> 8);
		// ARGDAT - see addArgument
	}

	public void addArgument(final int value, final ArgumentType argType) {
		switch (argType) {
		case I8:
			dataPackage[packageLength++] = (byte) (value & 0x00FF);
			break;
		case U8:
			dataPackage[packageLength++] = (byte) (value & 0x00FF);
			break;
		case U16:
			dataPackage[packageLength++] = (byte) (value & 0x00FF);
			dataPackage[packageLength++] = (byte) ((value & 0x0FF00) >> 8);
			break;
		default:
			LOGGER.error("Unhandled argument type: " + argType);
		}
	}

	/**
	 * @return
	 */
	public byte[] getPackage() {
		return Arrays.copyOf(dataPackage, packageLength);
	}

}
