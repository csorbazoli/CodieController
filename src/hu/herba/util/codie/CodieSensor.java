/**
 *
 */
package hu.herba.util.codie;

/**
 * @author csorbazoli
 *
 */
public interface CodieSensor extends CodieCommandBase {
	/**
	 * @param sensorValueStore
	 */
	void poll(SensorValueStore sensorValueStore);
}
