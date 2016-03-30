/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;
import hu.herba.util.codie.model.SensorType;

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
	public int processRequest(final String[] commandParts) {
		LOGGER.trace("Processing " + getClass().getSimpleName() + "...");
		CodieSensorPollService.getInstance().setTimerInterval(SensorType.valueOf(getStringParam(commandParts, 2, "allSensors")),
				getIntParam(commandParts, 1, 1000));
		return 0;
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		// nothing to do
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.SetSensorRefreshInterval;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
