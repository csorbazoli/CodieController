/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.SensorType;
import hu.herba.util.codie.SensorValueStore;

/**
 * ID: 0x106a<br/>
 * ARG: none<br/>
 * ReARG: lightValue[u16]<br/>
 *
 * Get a reading from the light sensor.
 *
 * Replies: lightValue: A raw reading from the light sensor. The value is measured by ADC on 12 bits, so this can be
 * 0-4096, 0 meaining the brightest and 4095 the darkest light.
 *
 * @author csorbazoli
 */
public class LightSenseGetRawSensor extends MCUSensor {
	private static final Logger LOGGER = LogManager.getLogger(LightSenseGetRawSensor.class);

	private static LightSenseGetRawSensor instance;

	private LightSenseGetRawSensor() {
		// private constructor
	}

	public static LightSenseGetRawSensor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new LightSenseGetRawSensor();
		}
	}

	@Override
	public void poll(final SensorValueStore sensorValueStore) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		sensorValueStore.updateSensorValue(SensorType.lightValue, 2048);
	}

	@Override
	public int getCommandId() {
		return 0x106a;
	}

	@Override
	public String getName() {
		return "lightValue";
	}

}
