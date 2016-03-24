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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.commands.ble.BLEEchoCommand;
import hu.herba.util.codie.commands.mcu.MCUEchoCommand;
import hu.herba.util.codie.commands.mcu.NullCommand;
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
		// BLE
		registerCommand(new BLEEchoCommand());

	}

	/**
	 * @param command
	 */
	private void registerCommand(final CodieCommand command) {
		CodieCommandBase curCommand = commands.get(command.getName());
		if (curCommand == null) {
			commands.put(command.getName(), command);
		} else if (curCommand.equals(command)) {
			LOGGER.warn("Command already registered: " + command);
		} else {
			LOGGER.error("Command name collision: " + command + " = " + curCommand + "!");
		}

	}

	public void handleCommand(final Writer out, final String commandDetails) throws IOException {
		out.append("Request = " + commandDetails + "\n");
		String[] parts = commandDetails.split("/");
		if (parts.length > 0 && !getCommandName(parts).isEmpty()) {
			String commandName = getCommandName(parts);
			if ("reset_all".equals(commandName)) {
				doResetAll();
			} else {
				for (CodieCommand command : commands.values()) {
					if (commandName.equals(command.getName())) {
						processCommand(command, parts);
						return;
					}
				}
				LOGGER.warn("Unhandled command type: " + commandName);
			}
		}
	}

	private void processCommand(final CodieCommand command, final String[] parts) {
		Integer uniqueCommandId = null;
		if (command.isWait()) {

		}
		commandStarted(command, uniqueCommandId);
		try {
			command.process(this, parts);
			setLastResult(true);
		} catch (CodieCommandException e) {
			LOGGER.warn("Failed to process Codie command: " + e.getMessage(), e);
			setLastResult(false);
		} finally {
			commandFinished(command, uniqueCommandId);
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
			out.append(sensorValue.getKey().name() + " " + sensorValue.getValue() + "\n");
		}

		out.append("lastResult " + lastResult + "\n");
	}

	public void commandStarted(final CodieCommandBase command, final Integer uniqueCommandId) {
		LOGGER.debug("Command " + command.getName() + " started " + (uniqueCommandId != null ? uniqueCommandId : ""));
		if (uniqueCommandId != null) {
			busyCommands.add(uniqueCommandId);
		}
	}

	public void commandFinished(final CodieCommandBase command, final Integer uniqueCommandId) {
		LOGGER.debug("Command " + command.getName() + " finished " + (uniqueCommandId != null ? uniqueCommandId : ""));
		if (uniqueCommandId != null) {
			busyCommands.remove(uniqueCommandId);
		}
	}
}
