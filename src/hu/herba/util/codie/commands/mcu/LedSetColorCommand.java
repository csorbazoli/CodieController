/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import hu.herba.util.codie.CodieCommandProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ID: 0x1065<br/>
 * ARG: ledMask[u16], hue[u8], saturation[u8], value[u8]<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * Set the color of the LEDs. The first (least significant) 12 bits of LedMask is a binary mask of which of the 12
 * pieces of LEDs to change. Where the bit is set, then the color of the corresponding LED whill change to the given HSV
 * value. Hue, saturation and value values are given on a full 8bit range of 0-255.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * @author csorbazoli
 */
public class LedSetColorCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(LedSetColorCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
	}

	@Override
	public int getCommandId() {
		return 0x1065;
	}

	@Override
	public String getName() {
		return "LedSetColor";
	}

}
