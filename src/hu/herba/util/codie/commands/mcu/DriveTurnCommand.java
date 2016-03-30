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
 * ID: 0x1062<br/>
 * ARG: degree[u16](�), speed[i8](%)<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * This command makes the robot turn given degrees in one place, by starting the tracks in different directions with given speed. Pozitive speeds turns left,
 * negative speed turns right.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * Busy call behavior: execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class DriveTurnCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(DriveTurnCommand.class);

	@Override
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		int degree = getIntParam(commandParts, 1, 5);
		int speed = getIntParam(commandParts, 2, 5);
		int ret = pack.prepareRequest(this, 3);
		pack.addArgument(degree, ArgumentType.U16);
		pack.addArgument(speed, ArgumentType.I8);
		// update leftSpeed/rightSpeed values
		getSensorValueStore().updateSensorValue(SensorType.leftSpeed, speed);
		getSensorValueStore().updateSensorValue(SensorType.rightSpeed, -speed);
		if (sendCommand() == 0) {
			getSensorValueStore().updateSensorValue(SensorType.leftSpeed, 0);
			getSensorValueStore().updateSensorValue(SensorType.rightSpeed, 0);
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
		return CodieCommandType.DriveTurn;
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
