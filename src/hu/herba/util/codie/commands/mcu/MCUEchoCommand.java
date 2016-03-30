/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;

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
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		LOGGER.info("Processing " + getClass().getSimpleName() + ": send echo request");
		int ret = pack.prepareRequest(this, 2);
		sendCommand();
		return ret;
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		getSensorValueStore().setLastResult(true);
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
