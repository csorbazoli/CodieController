/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;

/**
 * Set motor speeds to the given value in percents (0-100%).
 *
 * ID: 0x1062<br/>
 * ARG: degree[u16](�), speed[i8](%)<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * This command makes the robot turn given degrees in one place, by starting the tracks in different directions with
 * given speed. Pozitive speeds turns left, negative speed turns right.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * Busy call behavior: execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveTurnCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveTurnCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
	}

	@Override
	public int getCommandId() {
		return 0x1061;
	}

	@Override
	public String getName() {
		return "DriveTurn";
	}

	@Override
	public boolean isWait() {
		return true;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
