/**
 *
 */
package hu.herba.util.codie;

import java.util.Map;
import java.util.Set;

import hu.herba.util.codie.model.SensorType;

/**
 * @author csorbazoli
 *
 */
public interface SensorValueStore {

	Set<Map.Entry<SensorType, String>> getSensorValues();

	void updateSensorValue(final SensorType sensor, final Object value);

	int getSensorValueInt(final SensorType sensor);

	Object getConnection();

}
