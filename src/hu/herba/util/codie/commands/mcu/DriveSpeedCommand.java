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
 * Speed values are signed, negative value means backwards.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.Busy call behavior: execution ends immediately, the command
 * is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveSpeedCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveSpeedCommand.class);

	@Override
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		int speed = getIntParam(commandParts, 1, 10);
		Integer leftSpeed = null;
		Integer rightSpeed = null;
		switch (commandParts[0]) {
		case "DriveSpeed":
			leftSpeed = rightSpeed = speed;
			break;
		case "DriveSpeedLeft":
			leftSpeed = speed;
			rightSpeed = getSensorValueStore().getSensorValueInt(SensorType.rightSpeed);
			break;
		case "DriveSpeedRight":
			rightSpeed = speed;
			rightSpeed = getSensorValueStore().getSensorValueInt(SensorType.leftSpeed);
			break;
		default:
			LOGGER.error("Unhandled command type: " + commandParts[0]);
		}
		// then revert speed values as Codie stops
		int ret = request.prepareRequest(this, 2);
		request.addArgument(leftSpeed, ArgumentType.I8); // leftSpeed
		request.addArgument(rightSpeed, ArgumentType.I8); // rightSpeed
		if (sendCommand() == 0) {
			// update leftSpeed/rightSpeed values
			getSensorValueStore().updateSensorValue(SensorType.leftSpeed, leftSpeed);
			getSensorValueStore().updateSensorValue(SensorType.rightSpeed, rightSpeed);
		}
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
		return CodieCommandType.DriveSpeed;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
