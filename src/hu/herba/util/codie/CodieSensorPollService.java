/**
 *
 */
package hu.herba.util.codie;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.model.CodieSensor;
import hu.herba.util.codie.model.SensorType;
import hu.herba.util.codie.sensors.mcu.BatteryGetSocSensor;
import hu.herba.util.codie.sensors.mcu.LightSenseGetRawSensor;
import hu.herba.util.codie.sensors.mcu.LineGetRawSensor;
import hu.herba.util.codie.sensors.mcu.MicGetRawSensor;
import hu.herba.util.codie.sensors.mcu.SonarGetRangeSensor;

/**
 * This service polls for sensor values in a specified (and modifiable) refresh interval.
 *
 * @author csorbazoli
 */
public class CodieSensorPollService implements SensorValueStore {
	/**
	 *
	 */
	private static final boolean IS_DAEMON = false;

	private static final Logger LOGGER = LogManager.getLogger(CodieSensorPollService.class);

	private static final long DEF_REFRESH_INTERVAL = 5000; // sensor refresh interval (ms)
	private static CodieSensorPollService instance;
	private final Map<CodieSensor, Long> sensors = new TreeMap<>();
	private final Map<CodieSensor, SensorRefreshTimer> timers = new TreeMap<>();
	private final Map<SensorType, String> sensorValues = Collections.synchronizedMap(new TreeMap<SensorType, String>());

	private Object connection;

	private CodieSensorPollService() {
		// private constructor
		initSensors();
	}

	@Override
	protected void finalize() throws Throwable {
		LOGGER.info("Shutdown timers...");
		cancelTimers();
		super.finalize();
	}

	@Override
	public Object getConnection() {
		return connection;
	}

	public static CodieSensorPollService getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new CodieSensorPollService();
		}
	}

	private void initSensors() {
		// MCU
		registerSensor(BatteryGetSocSensor.getInstance());
		registerSensor(LightSenseGetRawSensor.getInstance());
		registerSensor(LineGetRawSensor.getInstance());
		registerSensor(MicGetRawSensor.getInstance());
		registerSensor(SonarGetRangeSensor.getInstance());
		// BLE - none
	}

	/**
	 * @param sensor
	 */
	private void registerSensor(final CodieSensor sensor) {
		sensors.put(sensor, DEF_REFRESH_INTERVAL);
	}

	@Override
	public Set<Map.Entry<SensorType, String>> getSensorValues() {
		return sensorValues.entrySet();
	}

	@Override
	public int getSensorValueInt(final SensorType sensor) {
		int ret = 0;
		String strVal = sensorValues.get(sensor);
		if (strVal != null) {
			try {
				ret = Integer.parseInt(strVal);
			} catch (NumberFormatException e) {
				LOGGER.warn(sensor + " value should be an integer: '" + strVal + "'", e);
			}
		}
		return ret;
	}

	@Override
	public void updateSensorValue(final SensorType sensor, final Object value) {
		sensorValues.put(sensor, String.valueOf(value));
	}

	@Override
	public void setLastResult(final boolean success) {
		sensorValues.put(SensorType.lastResult, Boolean.toString(success));
	}

	/**
	 * @param conn
	 *
	 */
	public void resetTimers(final Object conn) {
		LOGGER.info("Reset sensor timers...");
		connection = conn;
		for (Map.Entry<CodieSensor, Long> item : sensors.entrySet()) {
			SensorRefreshTimer timer = timers.get(item.getKey());
			if (timer != null) {
				timer.cancel();
			}
			timer = new SensorRefreshTimer(this, item.getKey().getCommandType().name(), IS_DAEMON);
			timer.schedule(item.getKey(), item.getValue());
			timers.put(item.getKey(), timer);
		}
	}

	public void doReset() {
		cancelTimers();
		initSensors();
	}

	public void cancelTimers() {
		LOGGER.info("Cancel sensor timers...");
		for (Map.Entry<CodieSensor, SensorRefreshTimer> item : timers.entrySet()) {
			if (item.getValue() != null) {
				item.getValue().cancel();
			}
			item.setValue(null);
		}
	}

	/**
	 * @param valueOf
	 * @param parseInt
	 */
	public void setTimerInterval(final SensorType sensorType, final int interval) {
		int refreshInterval = interval;
		if (interval < 100) {
			LOGGER.warn("Refresh interval for sensors should not be less the 100ms! (requested value was: " + interval + ")");
			refreshInterval = 100;
		}
		updateSensorValue(SensorType.refreshInterval, refreshInterval);
		switch (sensorType) {
		case allSensors:
			setTimer(BatteryGetSocSensor.getInstance(), refreshInterval);
			setTimer(LightSenseGetRawSensor.getInstance(), refreshInterval);
			setTimer(LineGetRawSensor.getInstance(), refreshInterval);
			setTimer(MicGetRawSensor.getInstance(), refreshInterval);
			setTimer(SonarGetRangeSensor.getInstance(), refreshInterval);
			break;
		case batterySensor:
			setTimer(BatteryGetSocSensor.getInstance(), refreshInterval);
			break;
		case distanceSensor:
			setTimer(SonarGetRangeSensor.getInstance(), refreshInterval);
			break;
		case lightSensor:
			setTimer(LightSenseGetRawSensor.getInstance(), refreshInterval);
			break;
		case lineLeft:
		case lineRight:
			setTimer(LineGetRawSensor.getInstance(), refreshInterval);
			break;
		case micSensor:
			setTimer(MicGetRawSensor.getInstance(), refreshInterval);
			break;
		default:
			LOGGER.warn("Unhandled sensor type: " + sensorType);
		}

	}

	/**
	 * @param instance2
	 * @param interval
	 */
	private void setTimer(final CodieSensor sensor, final long interval) {
		if (interval <= 0) { // clear timer
			LOGGER.info("Stop timer of " + sensor.getCommandType());
			sensors.put(sensor, null);
		} else {
			LOGGER.info("Set timer of " + sensor.getCommandType() + " to " + interval + " ms");
			sensors.put(sensor, interval);
		}
		SensorRefreshTimer timer = timers.get(sensor);
		if ((timer == null) && (interval > 0)) {
			timer = new SensorRefreshTimer(this, sensor.getCommandType().name(), IS_DAEMON);
			timers.put(sensor, timer);
		}
		if (timer != null) {
			timer.schedule(sensor, interval);
		}
	}

}
