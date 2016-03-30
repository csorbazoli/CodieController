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
	private static final int ARG_POS = 9; // INFO(1)+SEQ(2)+CMD(2)+ARGLEN(2)+REQSEQ(2)
	private final byte[] dataPackage = new byte[20];
	private int packageLength = 0;
	private static int seq = 0;

	private static int getNextSequenceNumber() {
		int ret = ++seq;
		if (ret > 0xFFFF) {
			// restart sequence
			ret = seq = 1;
		}
		return ret;
	}

	static void resetSequence() {
		seq = 0;
	}

	private byte getInfoByte(final CodieRole from, final CodieRole to, final boolean highPrio) {
		return (byte) ((from.ordinal() << 4) | (to.ordinal() << 6) | (highPrio ? CodieCommandBase.HIGH : CodieCommandBase.NORMAL));
	}

	public int prepareRequest(final CodieCommandBase cmd, final int argLen) {
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

		return seq;
	}

	public void prepareResponse(final byte[] requestPack, final int argLen) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits), swap sender/destination
		dataPackage[packageLength++] = (byte) ((requestPack[0] & 0x0F) | ((requestPack[0] & 0x00C0) >> 2) | ((requestPack[0] & 0x0030) << 2));
		// SEQ (16 bits)
		int seq = getNextSequenceNumber();
		dataPackage[packageLength++] = (byte) (seq & 0x00FF);
		dataPackage[packageLength++] = (byte) ((seq & 0x0FF00) >> 8);
		// CMD (16 bits)
		dataPackage[packageLength++] = requestPack[3];
		dataPackage[packageLength++] = (byte) (requestPack[4] | 0x0080); // set MSB = 1
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) ((argLen & 0x0FF00) >> 8);
		// ARGDAT - first 2 bytes = request sequence
		dataPackage[packageLength++] = requestPack[1];
		dataPackage[packageLength++] = requestPack[2];
		// additional arguments - see addArgument
	}

	public void prepareResponse(final CodieCommandBase cmd, final int reqSeq, final int argLen) {
		packageLength = 0;
		// INFO: ROUTE+PRIO (8 bits)
		byte info = getInfoByte(cmd.getDestination(), cmd.getSender(), false);
		dataPackage[packageLength++] = info;
		// SEQ (16 bits)
		int seq = getNextSequenceNumber();
		dataPackage[packageLength++] = (byte) (seq & 0x00FF);
		dataPackage[packageLength++] = (byte) ((seq & 0x0FF00) >> 8);
		// CMD (16 bits)
		int cmdId = cmd.getCommandType().getCommandId();
		dataPackage[packageLength++] = (byte) (cmdId & 0x00FF);
		dataPackage[packageLength++] = (byte) (((cmdId & 0x0FF00) >> 8) | 0x0080); // set MSB = 1
		// ARGLEN (16 bits)
		dataPackage[packageLength++] = (byte) (argLen & 0x00FF);
		dataPackage[packageLength++] = (byte) ((argLen & 0x0FF00) >> 8);
		// ARGDAT - first 2 bytes = request sequence
		dataPackage[packageLength++] = (byte) (reqSeq & 0x00FF);
		dataPackage[packageLength++] = (byte) ((reqSeq & 0x0FF00) >> 8);
		// additional arguments - see addArgument
	}

	public void addArgument(final int value, final ArgumentType argType) {
		switch (argType) {
		case I8:
			// it handles negative values also
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

	/**
	 * @param bytePos
	 *            Argument's position specified in number of bytes. E.g. first argument is 0,<br/>
	 *            second argument is 2 if the first was 2-bytes long.
	 * @param argType
	 *            Argument type
	 * @return argument value
	 */
	public int readArgument(final int bytePos, final ArgumentType argType) {
		int ret = 0;
		int pos = ARG_POS + bytePos;
		switch (argType) {
		case I8:
			// it handles negative values also
			ret = dataPackage[pos++];
			break;
		case U8:
			ret = dataPackage[pos++] & 0x00FF;
			break;
		case U16:
			ret = dataPackage[pos++] & 0x00FF;
			ret = ret + ((dataPackage[pos++] & 0x00FF) << 8);
			break;
		default:
			LOGGER.error("Unhandled argument type: " + argType);
		}
		return ret;
	}

}
