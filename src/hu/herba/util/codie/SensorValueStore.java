/**
 *
 */
package hu.herba.util.codie;

import java.util.Map;
import java.util.Set;

/**
 * @author Zolt�n
 *
 */
public interface SensorValueStore {

	Set<Map.Entry<SensorType, String>> getSensorValues();

	void updateSensorValue(final SensorType sensor, final Object value);

	Object getConnection();

}
