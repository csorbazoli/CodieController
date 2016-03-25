/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;

/**
 * ID: 0x1064<br/>
 * ARG: duration[u16](ms)<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * Play a beep on the speaker. The frequency is fixed, you can only specify the duration in milliseconds.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * Busy call behavior: execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class SpeakBeepCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(SpeakBeepCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
	}

	@Override
	public int getCommandId() {
		return 0x1064;
	}

	@Override
	public String getName() {
		return "SpeakBeep";
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
