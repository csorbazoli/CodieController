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
		if (sensors.containsKey(sensor)) {
			LOGGER.warn("Sensor already registered: " + sensor);
		}
		sensors.put(sensor, DEF_REFRESH_INTERVAL);

	}

	@Override
	public Set<Map.Entry<SensorType, String>> getSensorValues() {
		return sensorValues.entrySet();
	}

	@Override
	public void updateSensorValue(final SensorType sensor, final Object value) {
		sensorValues.put(sensor, String.valueOf(value));
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
			timer = new SensorRefreshTimer(this, item.getKey().getName(), IS_DAEMON);
			timer.schedule(item.getKey(), item.getValue());
			timers.put(item.getKey(), timer);
		}
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
		if (interval < 100) {
			LOGGER.warn("Refresh interval for sensors should not be less the 100ms! (requested value was: " + interval
					+ ")");
		}
		switch (sensorType) {
		case all:
			setTimer(BatteryGetSocSensor.getInstance(), interval);
			setTimer(LightSenseGetRawSensor.getInstance(), interval);
			setTimer(LineGetRawSensor.getInstance(), interval);
			setTimer(MicGetRawSensor.getInstance(), interval);
			setTimer(SonarGetRangeSensor.getInstance(), interval);
			break;
		case batteryStateOfCharge:
			setTimer(BatteryGetSocSensor.getInstance(), interval);
			break;
		case sonarRange:
			setTimer(SonarGetRangeSensor.getInstance(), interval);
			break;
		case lightValue:
			setTimer(LightSenseGetRawSensor.getInstance(), interval);
			break;
		case lineLeft:
		case lineRight:
			setTimer(LineGetRawSensor.getInstance(), interval);
			break;
		case micLevel:
			setTimer(MicGetRawSensor.getInstance(), interval);
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
			LOGGER.info("Stop timer of " + sensor.getName());
			sensors.put(sensor, null);
		} else {
			LOGGER.info("Set timer of " + sensor.getName() + " to " + interval + " ms");
			sensors.put(sensor, interval);
		}
		SensorRefreshTimer timer = timers.get(sensor);
		if (timer == null && interval > 0) {
			timer = new SensorRefreshTimer(this, sensor.getName(), IS_DAEMON);
			timers.put(sensor, timer);
		}
		if (timer != null) {
			timer.schedule(sensor, interval);
		}
	}

}
