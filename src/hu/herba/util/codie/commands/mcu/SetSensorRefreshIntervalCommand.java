/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.SensorType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Not a standard Codie command.
 *
 * It sets the refresh interval for a given sensor or for all which is applied for retrieving values.
 *
 * @author csorbazoli
 */
public class SetSensorRefreshIntervalCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(SetSensorRefreshIntervalCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		CodieSensorPollService.getInstance().setTimerInterval(SensorType.valueOf(commandParts[2]), Integer.parseInt(commandParts[1]));
	}

	@Override
	public int getCommandId() {
		return 0x0;
	}

	@Override
	public String getName() {
		return "SetSensorRefreshInterval";
	}

	@Override
	public boolean isWait() {
		return true;
	}
}
