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
	public void processResponse(final DataPackage response) throws CodieCommandException {
		int stateOfCharge = response.readArgument(0, ArgumentType.U8);
		getSensorValueStore().updateSensorValue(SensorType.batterySensor, stateOfCharge);
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.BatteryGetSoc;
	}

	@Override
	public SensorType getSensorType() {
		return SensorType.batterySensor;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
