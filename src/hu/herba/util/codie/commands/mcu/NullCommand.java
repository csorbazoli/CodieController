/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;

/**
 * The Null command has the 0x0 command ID, which is never used in normal communication.
 *
 * @author csorbazoli
 */
public class NullCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(NullCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + ": nothing to do");
	}

	@Override
	public int getCommandId() {
		return 0x0;
	}

	@Override
	public String getName() {
		return "Null";
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
