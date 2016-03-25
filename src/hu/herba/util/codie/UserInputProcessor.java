/**
 *
 */
package hu.herba.util.codie;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Zoltán
 *
 */
public class UserInputProcessor {
	private static final Logger LOGGER = LogManager.getLogger(UserInputProcessor.class);

	private static UserInputProcessor instance;

	private final List<UserInputListener> listeners = new ArrayList<>();

	private UserInputProcessor() {
		// private constructor
	}

	public static UserInputProcessor getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			instance = new UserInputProcessor();
		}
	}

	public void scan() {
		Scanner scan = new Scanner(System.in);
		while (scan.hasNextLine()) {
			String readLine = scan.nextLine();
			if ("q".equalsIgnoreCase(readLine) || "quit".equalsIgnoreCase(readLine)
					|| "exit".equalsIgnoreCase(readLine)) {
				scan.close();
				stop();
			} else if (!notifyListeners(readLine)) {
				printUsage();
			}
		}
	}

	public String readLine(final String regex) {
		final StringBuilder sb = new StringBuilder();
		addListener(new UserInputListener() {
			@Override
			public boolean handle(final String line) {
				if (line.matches(regex)) {
					sb.append(line);
					removeListener(this);
					return true;
				}
				return false;
			}
		});
		while (sb.length() == 0) {
			waiting(100);
		}
		return sb.toString();
	}

	private synchronized void waiting(final int timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException e) {
			LOGGER.warn("Waiting interrupted: " + e.getMessage());
		}
	}

	public synchronized void addListener(final UserInputListener listener) {
		listeners.add(0, listener);
	}

	public synchronized void removeListener(final UserInputListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return
	 */
	private boolean notifyListeners(final String line) {
		boolean ret = false;
		for (UserInputListener listener : listeners) {
			if (listener.handle(line)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	private static void printUsage() {
		System.out.println();
		System.out.println("Type quit or exit to stop the server");
	}

	private void stop() {
		LOGGER.info("CodieController server is stopping...");
		System.exit(0);
	}

}
