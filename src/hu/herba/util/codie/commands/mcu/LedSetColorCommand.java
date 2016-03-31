/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.CodieColors;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;

/**
 * ID: 0x1065<br/>
 * ARG: ledMask[u16], hue[u8], saturation[u8], value[u8]<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * Set the color of the LEDs. The first (least significant) 12 bits of LedMask is a binary mask of which of the 12 pieces of LEDs to change. Where the bit is
 * set, then the color of the corresponding LED whill change to the given HSV value. Hue, saturation and value values are given on a full 8bit range of 0-255.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * @author csorbazoli
 */
public class LedSetColorCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(LedSetColorCommand.class);

	@Override
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		String colorName = getStringParam(commandParts, 1, "green");
		CodieColors color = CodieColors.valueOf(colorName);
		int ret = request.prepareRequest(this, 5);
		request.addArgument(0x08ff, ArgumentType.U16); // all of the first 12 bits are 1
		request.addArgument(color.getHue(), ArgumentType.U8); // hue
		request.addArgument(color.getSaturation(), ArgumentType.U8); // saturation
		request.addArgument(color.getValue(), ArgumentType.U8); // value
		sendCommand();
		return ret;
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		int nSuccessful = response.readArgument(0, ArgumentType.U8);
		if (nSuccessful != 0) {
			getSensorValueStore().setLastResult(false);
		}
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.LedSetColor;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
