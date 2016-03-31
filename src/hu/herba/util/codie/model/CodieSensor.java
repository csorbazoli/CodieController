/**
 *
 */
package hu.herba.util.codie.model;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.SensorValueStore;

/**
 * @author csorbazoli
 *
 */
public interface CodieSensor extends CodieCommandBase {
	/**
	 * @param sensorValueStore
	 * @throws CodieCommandException
	 */
	void poll(SensorValueStore sensorValueStore) throws CodieCommandException;

	/**
	 * @return type of sensor
	 */
	SensorType getSensorType();
}
