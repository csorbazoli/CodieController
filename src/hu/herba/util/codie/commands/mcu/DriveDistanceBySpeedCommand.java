/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.CodieSensorPollService;
import hu.herba.util.codie.SensorValueStore;
import hu.herba.util.codie.model.SensorType;

/**
 * Set motor speeds to the given value in percents (0-100%).
 *
 * Move a specified amount of distance (in mm) with given speeds (in percentage 0-100%).<br/>
 * Moving backwards can be done with negative speeds.<br/>
 * Currently both of Codie�s tracks will travel the given distance, so covering arcs by specifying different speeds
 * will not yield results as intended� Later we probably will improve this behavior so that distance will mean the
 * length of trajectory of the center of the robot while following an arc.
 *
 * Replies: nSuccessful, 0 means command has been successfully executed, any other value means error.Busy call behavior:
 * execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveDistanceBySpeedCommand extends DriveDistanceCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveDistanceBySpeedCommand.class);

	@Override
	public void process(final CodieCommandProcessor codieCommandProcessor, final String[] commandParts) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		// TODO update leftSpeed/rightSpeed values
		getSensorValueStore().updateSensorValue(SensorType.leftSpeed, 10);
	}

	/**
	 * @return
	 *
	 */
	private SensorValueStore getSensorValueStore() {
		return CodieSensorPollService.getInstance();
	}

	@Override
	public int getCommandId() {
		return 0x1061;
	}

	@Override
	public String getName() {
		return "DriveDistanceBySpeed";
	}

}
