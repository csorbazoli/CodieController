/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import hu.herba.util.codie.CodieCommandProcessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Set motor speeds to the given value in percents (0-100%).
 *
 * Speed values are signed, negative value means backwards.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.Busy call behavior:
 * execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveSpeedCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveSpeedCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
	}

	@Override
	public int getCommandId() {
		return 0x1060;
	}

	@Override
	public String getName() {
		return "DriveSpeed";
	}

}
