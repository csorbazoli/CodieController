/**
 *
 */
package hu.herba.util.codie;

import java.util.TimerTask;

/**
 * Single timed task for updating a sensor value
 *
 * @author csorbazoli
 */
public class SensorRefreshTimerTask extends TimerTask {

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
		sensor.poll(sensorValueStore);
	}

}
