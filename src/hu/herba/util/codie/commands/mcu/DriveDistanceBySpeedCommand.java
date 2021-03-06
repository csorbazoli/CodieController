/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;
import hu.herba.util.codie.model.SensorType;

/**
 * Set motor speeds to the given value in percents (0-100%).
 *
 * Move a specified amount of distance (in mm) with given speeds (in percentage 0-100%).<br/>
 * Moving backwards can be done with negative speeds.<br/>
 * Currently both of Codie's tracks will travel the given distance, so covering arcs by specifying different speeds will not yield results as intended
 *
 * Later we probably will improve this behavior so that distance will mean the length of trajectory of the center of the robot while following an arc.
 *
 * Replies: nSuccessful, 0 means command has been successfully executed, any other value means error.Busy call behavior: execution ends immediately, the command
 * is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveDistanceBySpeedCommand extends DriveDistanceCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveDistanceBySpeedCommand.class);

	@Override
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		int distance = getIntParam(commandParts, 1, 5) * 10;
		int speed = getIntParam(commandParts, 2, 10);
		// then revert speed values as Codie stops
		int ret = request.prepareRequest(this, 4);
		request.addArgument(distance, ArgumentType.U16);
		request.addArgument(speed, ArgumentType.I8); // leftSpeed
		request.addArgument(speed, ArgumentType.I8); // rightSpeed
		// update leftSpeed/rightSpeed values
		getSensorValueStore().updateSensorValue(SensorType.leftSpeed, speed);
		getSensorValueStore().updateSensorValue(SensorType.rightSpeed, speed);
		sendCommand();
		return ret;
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		int nSuccessful = response.readArgument(0, ArgumentType.U8);
		if (nSuccessful != 0) {
			getSensorValueStore().setLastResult(false);
			getSensorValueStore().updateSensorValue(SensorType.leftSpeed, 0);
			getSensorValueStore().updateSensorValue(SensorType.rightSpeed, 0);
		}
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.DriveDistanceBySpeed;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
