/**
 *
 */
package hu.herba.util.codie.model;

// list of operations
public enum CodieCommandType {
	Null(0x0), //
	Echo(0x0001), //
	DriveSpeed(0x1060, "DriveSpeed" + CodieCommandBase.SEPARATOR + "DriveSpeedLeft" + CodieCommandBase.SEPARATOR + "DriveSpeedRight"), //
	DriveDistanceBySpeed(0x1060), // actually, it is the same command from Codie point of view as DriveDistance
	DriveDistance(0x1061), //
	DriveTurn(0x1062), //
	SpeakBeep(0x1064), //
	LedSetColor(0x1065), // set led color for all 12 leds
	LedSetColorSingle(0x1065), // same as LedSetColor but it specifies also a single led by its index
	// extra commands
	SetSensorRefreshInterval(0xff), // not handled by Codie, provided by CodieCommandProcessor
	// sensors
	BatteryGetSoc(0x1069), //
	LightSenseGetRaw(0x106a), //
	LineGetRaw(0x106b), //
	SonarGetRange(0x1063), //
	MicGetRaw(0x106c), //
	;

	private final int commandId;
	private final String commandName;

	private CodieCommandType(final int cid) {
		commandId = cid;
		commandName = name();
	}

	private CodieCommandType(final int cid, final String name) {
		commandId = cid;
		commandName = name;
	}

	/**
	 * Two bytes that specifies the command id (e.g. 0x1061 is 'move forward')
	 *
	 * @return Numeric (unique) id of this command.
	 */
	public int getCommandId() {
		return commandId;
	}

	public String getCommandName() {
		return commandName;
	}

}