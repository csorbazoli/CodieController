/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.SensorType;

/**
 * Set motor speeds to the given value in percents (0-100%).
 *
 * Move a specified amount of distance (in mm) with given speeds (in percentage 0-100%).<br/>
 * Moving backwards can be done with negative speeds.<br/>
 * Currently both of Codie's tracks will travel the given distance, so covering arcs by specifying different speeds will
 * not yield results as intended
 *
 * Later we probably will improve this behavior so that distance will mean the length of trajectory of the center of the
 * robot while following an arc.
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
		int distance = getIntParam(commandParts, 1, 5) * 10;
		int speed = getIntParam(commandParts, 2, 10);
		// then revert speed values as Codie stops
		prepareDataPackage(4);
		addArgument(distance, ArgumentType.U16);
		addArgument(speed, ArgumentType.I8); // leftSpeed
		addArgument(speed, ArgumentType.I8); // rightSpeed
		// update leftSpeed/rightSpeed values
		getSensorValueStore().updateSensorValue(SensorType.leftSpeed, speed);
		getSensorValueStore().updateSensorValue(SensorType.rightSpeed, speed);
		if (sendCommand() == 0) {
			getSensorValueStore().updateSensorValue(SensorType.leftSpeed, 0);
			getSensorValueStore().updateSensorValue(SensorType.rightSpeed, 0);
		}
	}

	@Override
	public int getCommandId() {
		return 0x1061;
	}

	@Override
	public String getName() {
		return "DriveDistanceBySpeed";
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
