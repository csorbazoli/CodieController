/**
 *
 */
package hu.herba.util.codie;

import java.util.Timer;

import hu.herba.util.codie.model.CodieSensor;

/**
 * Timer for refreshing sensor value
 *
 * @author csorbazoli
 */
public class SensorRefreshTimer extends Timer {

	private static final int TASK_DELAY = 1000;
	private SensorRefreshTimerTask currentTask;
	private final SensorValueStore sensorValueStore;

	/**
	 * @param valueStore
	 * @param name
	 * @param isDaemon
	 */
	public SensorRefreshTimer(final SensorValueStore valueStore, final String name, final boolean isDaemon) {
		super(name, isDaemon);
		sensorValueStore = valueStore;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void schedule(final CodieSensor sensor, final Long period) {
		if (currentTask != null) {
			currentTask.cancel();
			currentTask = null;
		}
		if (period > 0) {
			currentTask = new SensorRefreshTimerTask(sensorValueStore, sensor);
			this.schedule(currentTask, TASK_DELAY, period);
		}
	}

}
