/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import hu.herba.util.codie.SensorValueStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ID: 0x1069<br/>
 * ARG: none<br/>
 * ReARG: stateOfCharge[u8](%)<br/>
 *
 * Get the state of charge of the battery in percentage 0-100%.
 *
 * Replies: stateOfCharge: 0-100% the state of charge of the battery
 *
 * @author csorbazoli
 */
public class BatteryGetSocSensor extends MCUSensor {
	private static final Logger LOGGER = LogManager.getLogger(BatteryGetSocSensor.class);

	private static BatteryGetSocSensor instance;

	private BatteryGetSocSensor() {
		// private constructor
	}

	public static BatteryGetSocSensor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new BatteryGetSocSensor();
		}
	}

	@Override
	public void poll(final SensorValueStore sensorValueStore) {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
	}

	@Override
	public int getCommandId() {
		return 0x1069;
	}

	@Override
	public String getName() {
		return "batteryStateOfCharge";
	}

}
