/**
 *
 */
package hu.herba.util.codie.model;

/**
 * @author csorbazoli
 *
 */
public enum SensorType {
	allSensors, //
	// sensors
	micSensor("micLevel"), //
	lineSensor("lineLeft" + CodieCommandBase.SEPARATOR + "lineRight"), //
	lightSensor("lightValue"), //
	batterySensor("batteryStateOfCharge"), //
	distanceSensor("sonarRange"), //
	// reporters
	lineLeft, //
	lineRight, //
	leftSpeed, //
	rightSpeed, //
	lastResult, //
	refreshInterval, //
	;

	private final String sensorName;

	private SensorType() {
		sensorName = name();
	}

	private SensorType(final String sensorName) {
		this.sensorName = sensorName;
	}

	public String getSensorName() {
		return sensorName;
	}
}
