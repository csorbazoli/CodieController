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
	 */
	void poll(SensorValueStore sensorValueStore);

	/**
	 * Process response from Codie robot.
	 *
	 * @param response
	 * @throws CodieCommandException
	 */
	void processResponse(DataPackage response) throws CodieCommandException;

	/**
	 * @return type of sensor
	 */
	SensorType getSensorType();
}
