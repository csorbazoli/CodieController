/**
 *
 */
package hu.herba.util.codie.model;

import hu.herba.util.codie.SensorValueStore;

/**
 * @author csorbazoli
 *
 */
public interface CodieSensor extends CodieCommandBase {
	/**
	 * @param sensorValueStore
	 */
	void poll(SensorValueStore sensorValueStore);

	/**
	 * @return type of sensor
	 */
	SensorType getSensorType();
}
