/**
 *
 */
package hu.herba.util.codie;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.commands.mcu.DriveDistanceBySpeedCommand;
import hu.herba.util.codie.commands.mcu.DriveDistanceCommand;
import hu.herba.util.codie.commands.mcu.DriveSpeedCommand;
import hu.herba.util.codie.commands.mcu.DriveTurnCommand;
import hu.herba.util.codie.commands.mcu.LedSetColorCommand;
import hu.herba.util.codie.commands.mcu.MCUEchoCommand;
import hu.herba.util.codie.commands.mcu.NullCommand;
import hu.herba.util.codie.commands.mcu.SetSensorRefreshIntervalCommand;
import hu.herba.util.codie.commands.mcu.SpeakBeepCommand;
import hu.herba.util.codie.model.CodieCommand;
import hu.herba.util.codie.model.CodieCommandBase;
import hu.herba.util.codie.model.SensorType;

/**
 * Process handler class for Codie commands triggered by Scratch.
 *
 * @author csorbazoli
 */
public class CodieCommandProcessor {

	private static final Logger LOGGER = LogManager.getLogger(CodieCommandProcessor.class);
	private static CodieCommandProcessor instance;

	private final Map<String, CodieCommand> commands = new TreeMap<>();
	private final Set<Integer> busyCommands = Collections.synchronizedSet(new TreeSet<Integer>());
	private boolean lastResult;

	private CodieCommandProcessor() {
		initCommands();
	}

	/**
	 * @return
	 */
	public static CodieCommandProcessor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new CodieCommandProcessor();
		}
	}

	private void initCommands() {
		// MCU
		registerCommand(new NullCommand());
		registerCommand(new MCUEchoCommand());
		registerCommand(new DriveDistanceBySpeedCommand());
		registerCommand(new DriveDistanceCommand());
		registerCommand(new DriveSpeedCommand());
		registerCommand(new DriveTurnCommand());
		registerCommand(new LedSetColorCommand());
		registerCommand(new SetSensorRefreshIntervalCommand());
		registerCommand(new SpeakBeepCommand());
		// BLE
		// registerCommand(new BLEEchoCommand());

	}

	/**
	 * @param command
	 */
	private void registerCommand(final CodieCommand command) {
		String[] names = command.getCommandType().getCommandName().split(Pattern.quote(CodieCommandBase.SEPARATOR));
		for (String name : names) {
			CodieCommandBase curCommand = commands.get(name);
			if (curCommand == null) {
				commands.put(name, command);
			} else if (curCommand.equals(command)) {
				LOGGER.warn("Command already registered: " + command);
			} else {
				LOGGER.error("Command name collision: " + command + " = " + curCommand + "!");
			}
		}
	}

	public void handleCommand(final Writer out, final String commandDetails) throws IOException {
		out.append("Request = " + commandDetails + "\n");
		LOGGER.info("Command: " + commandDetails);
		String[] parts = commandDetails.split("/");
		if ((parts.length > 0) && !getCommandName(parts).isEmpty()) {
			String commandName = getCommandName(parts);
			if ("reset_all".equals(commandName)) {
				doResetAll();
			} else {
				CodieCommand command = commands.get(commandName);
				if (command == null) {
					LOGGER.warn("Unhandled command type: " + commandName);
				} else {
					processCommand(command, parts);
					return;
				}
			}
		}
	}

	private void processCommand(final CodieCommand command, final String[] parts) {
		Integer uniqueCommandId = null;
		if (command.isWait()) {
			uniqueCommandId = Integer.parseInt(parts[1]);
		}
		commandStarted(command, uniqueCommandId);
		try {
			command.processRequest(parts);
			setLastResult(true);
		} catch (CodieCommandException e) {
			LOGGER.warn("Failed to process Codie command: " + e.getMessage(), e);
			setLastResult(false);
		} finally {
			if (uniqueCommandId == null) {
				commandFinished(command, uniqueCommandId);
			}
		}
	}

	/**
	 * @param b
	 */
	private void setLastResult(final boolean b) {
		lastResult = b;
	}

	private String getCommandName(final String[] parts) {
		String ret = parts[0];
		if (ret.startsWith("/")) {
			ret = ret.substring(1);
		}
		return ret;
	}

	private void doResetAll() {
		LOGGER.info("Reset ALL...");
		// TODO stop Codie
	}

	/**
	 * method that returns poll info
	 *
	 * @param out
	 *            writes _busy and sensor info to this print writer
	 * @throws IOException
	 */
	public void providePollInfo(final Writer out) throws IOException {
		// list of busy commands (commands that wait until a given action completes, i.e. type "w" commands)
		// _busy <msgid> ...
		// e.g. _busy 1427 1511 1600
		if (!busyCommands.isEmpty()) {
			StringBuilder busyInfo = new StringBuilder("_busy");
			for (Integer msgId : busyCommands) {
				busyInfo.append(' ').append(msgId);
			}
			if (busyInfo.length() > 5) {
				out.append(busyInfo.toString()).append('\n');
			}
		}

		// provide info about the sensors
		// <sensorname> <value>
		// e.g. leftspeed 10
		// which means, that the left wheel is working on 10%
		// sensor values could be updated by AppCommands where Codie sends info about the state
		// or normal commands which affects the state of Codie (e.g. moves codie which means the speed is changing)
		for (Map.Entry<SensorType, String> sensorValue : CodieSensorPollService.getInstance().getSensorValues()) {
			out.append(sensorValue.getKey().getSensorName() + " " + sensorValue.getValue() + "\n");
		}

		out.append("lastResult " + lastResult + "\n");
	}

	public void commandStarted(final CodieCommandBase command, final Integer uniqueCommandId) {
		LOGGER.trace("Command " + command.getCommandType() + " started " + (uniqueCommandId != null ? uniqueCommandId : ""));
		if (uniqueCommandId != null) {
			busyCommands.add(uniqueCommandId);
		}
	}

	public void commandFinished(final CodieCommandBase command, final Integer uniqueCommandId) {
		LOGGER.trace("Command " + command.getCommandType() + " finished " + (uniqueCommandId != null ? uniqueCommandId : ""));
		if (uniqueCommandId != null) {
			busyCommands.remove(uniqueCommandId);
		}
	}
}
