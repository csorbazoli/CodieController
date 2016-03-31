/**
 *
 */
package hu.herba.util.codie;

import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.model.CodieSensor;

/**
 * Single timed task for updating a sensor value
 *
 * @author csorbazoli
 */
public class SensorRefreshTimerTask extends TimerTask {

	private static final Logger LOGGER = LogManager.getLogger(SensorRefreshTimerTask.class);
	private final CodieSensor sensor;
	private final SensorValueStore sensorValueStore;

	/**
	 * @param sensorValueStore
	 * @param sensor
	 */
	public SensorRefreshTimerTask(final SensorValueStore sensorValueStore, final CodieSensor sensor) {
		this.sensorValueStore = sensorValueStore;
		this.sensor = sensor;
	}

	@Override
	public void run() {
		try {
			sensor.poll(sensorValueStore);
		} catch (CodieCommandException e) {
			LOGGER.error("Failed to poll sensor value:" + e.getMessage(), e);
		}
	}

}
