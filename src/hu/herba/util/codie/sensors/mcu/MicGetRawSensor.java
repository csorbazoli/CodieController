/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.SensorValueStore;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.SensorType;

/**
 * ID: 0x106c<br/>
 * ARG: none<br/>
 * ReARG: value[u16]<br/>
 *
 * Get a reading from the microphone.
 *
 * Replies: value: Raw value ranging from 0 to about 2048. Several measurements are averaged into one value, the window of averaging is about 50ms.
 *
 * @author csorbazoli
 */
public class MicGetRawSensor extends MCUSensor {
	private static final Logger LOGGER = LogManager.getLogger(MicGetRawSensor.class);

	private static MicGetRawSensor instance;

	private MicGetRawSensor() {
		// private constructor
	}

	public static MicGetRawSensor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new MicGetRawSensor();
		}
	}

	@Override
	public void poll(final SensorValueStore sensorValueStore) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		sensorValueStore.updateSensorValue(SensorType.micSensor, 46);
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.MicGetRaw;
	}

	@Override
	public SensorType getSensorType() {
		return SensorType.micSensor;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
