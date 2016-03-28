/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.model.CodieCommandType;

/**
 * The echo command simply requests an echo. Each node should reply with a packet with:
 * <ul>
 * <li>swapped ROUTE</li>
 * <li>same PRIO</li>
 * <li>MSB set in command ID</li>
 * <li>ARGLEN=0</li>
 * </ul>
 *
 * @author csorbazoli
 */
public class MCUEchoCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(MCUEchoCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + ": send echo request");
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.Echo;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
