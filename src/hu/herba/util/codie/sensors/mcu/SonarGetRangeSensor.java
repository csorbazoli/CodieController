/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.SensorType;
import hu.herba.util.codie.SensorValueStore;

/**
 * ID: 0x1063<br/>
 * ARG: none<br/>
 * ReARG: range[u16](mm)<br/>
 *
 * Request a range measurement with the sonar sensor. The measurement takes time (depends on how far there is a surface
 * in front of Codie), from ~10ms up to 60ms.
 *
 * Replies: range: the measured distance by the sonar in mm. 0 means error.
 *
 * Busy call behavior: requests are queued, however you should NOT request a measurement until the reply arrives for the
 * previous request, because the command queue might get jammed.
 *
 * @author csorbazoli
 */
public class SonarGetRangeSensor extends MCUSensor {
	private static final Logger LOGGER = LogManager.getLogger(SonarGetRangeSensor.class);

	private static SonarGetRangeSensor instance;

	private SonarGetRangeSensor() {
		// private constructor
	}

	public static SonarGetRangeSensor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new SonarGetRangeSensor();
		}
	}

	@Override
	public void poll(final SensorValueStore sensorValueStore) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		sensorValueStore.updateSensorValue(SensorType.sonarRange, 15);
	}

	@Override
	public int getCommandId() {
		return 0x1063;
	}

	@Override
	public String getName() {
		return "sonarRange";
	}

}
