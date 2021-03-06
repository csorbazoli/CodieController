/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;
import hu.herba.util.codie.model.SensorType;

/**
 * ID: 0x106b<br/>
 * ARG: none<br/>
 * ReARG: valueLeft[u16], valueRight[u16]<br/>
 *
 * Get a reading from the line sensor. The line sensors on the bottom of Codie are simple infrared reflective optical sensors.
 *
 * Replies: valueLeft, valueRight: Raw 12bit values from the ADC.
 *
 * @author csorbazoli
 */
public class LineGetRawSensor extends MCUSensor {
	private static final Logger LOGGER = LogManager.getLogger(LineGetRawSensor.class);

	private static LineGetRawSensor instance;

	private LineGetRawSensor() {
		// private constructor
	}

	public static LineGetRawSensor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new LineGetRawSensor();
		}
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		int leftValue = response.readArgument(0, ArgumentType.U16);
		int rightValue = response.readArgument(2, ArgumentType.U16);
		getSensorValueStore().updateSensorValue(SensorType.lineLeft, leftValue);
		getSensorValueStore().updateSensorValue(SensorType.lineRight, rightValue);
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.LineGetRaw;
	}

	@Override
	public SensorType getSensorType() {
		return SensorType.lineSensor;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
