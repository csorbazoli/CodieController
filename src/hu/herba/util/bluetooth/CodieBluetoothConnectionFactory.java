/**
 *
 */
package hu.herba.util.bluetooth;

import java.io.IOException;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.RemoteDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.bluetooth.mock.CodieMockClientSession;
import hu.herba.util.codie.UserInputProcessor;

/**
 * @author csorbazoli
 *
 */
public class CodieBluetoothConnectionFactory {

	private static final Logger LOGGER = LogManager.getLogger(CodieBluetoothConnectionFactory.class);

	private static final long RETRY_TIMEOUT = 1000; // retry timeout in milliseconds for a failed connection

	private static final String CODIE_MOCK_SERVICE = "service://CodieMockService";

	public static final String CODIE_MAC_ADDRESS = "FF0BC95A08C7";
	// Codie has a custom BLE service with the following UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
	public static final String CODIE_BLE_SERVICE_UUID = "52af0001978a628dc8450a104ca2b8dd";
	// In this service there are two characteristics:
	public static final String CODIE_RX_CHARACTERISTICS = "52af0002978a628dc8450a104ca2b8dd";
	// Codie receives commands through this characteristic. It is readable and writeable (write without response).
	// Be aware, that only 20 bytes can be written at a time (due to the BLE packet payload limit).
	public static final String CODIE_TX_CHARACTERISTICS = "52af0003978a628dc8450a104ca2b8dd";
	// Codie sends commands through this characteristic.
	// Readable and can be subscribed for notification (indication not supported).

	private static CodieClientSession currentConnection;
	private static long lastConnectionFailure = 0l;
	private static String lastError;
	private static int errorCounter = 0;

	/**
	 * Connect to Codie with bluetooth driver.
	 *
	 * @return Connection
	 *
	 */
	public static synchronized CodieClientSession connect() {
		if ((currentConnection == null) && retryTimeout()) {
			if (lastError == null) {
				LOGGER.debug("Open connection to codie...");
			}
			try {
				currentConnection = openConnection();
			} catch (CodieConnectionException e) {
				if ((lastError == null) || !lastError.equals(e.getMessage())) {
					lastError = e.getMessage();
					LOGGER.warn("Failed to connect to Codie: " + e.getMessage(), e);
				}
				if (++errorCounter == 10) {
					errorCounter = 0;
					lastError = null;
				}
			} finally {
				if (currentConnection == null) {
					lastConnectionFailure = System.currentTimeMillis();
				}
			}
		}
		return currentConnection;
	}

	private static CodieClientSession openConnection() throws CodieConnectionException {
		CodieClientSession ret = null;
		if (Boolean.parseBoolean(System.getenv("MOCK_CODIE"))) {
			ret = getMockSession();
		} else {
			// Code MAC address: FF:0b:C9:5A:08:C7
			// UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
			// RX: {52af0002-978a-628d-c845-0a104ca2b8dd}
			// TX: {52af0003-978a-628d-c845-0a104ca2b8dd} - not used yet
			ret = getStreamSession();
		}
		return ret;
	}

	/**
	 * @return
	 */
	private static CodieClientSession getStreamSession() {
		CodieClientSession ret = null;
		try {
			RemoteDevice codieDevice = selectDevice(); // TCKAgentUtil.getRemoteDevice(CODIE_MAC_ADDRESS);
			LOGGER.info("CodieDevice: " + codieDevice);
			// UUID[] uArr = new UUID[] { new UUID(CODIE_BLE_SERVICE_UUID, false) };
			// ServiceRecord[] serviceRecords = TCKAgentUtil.getServiceRecords(codieDevice.getBluetoothAddress(), uArr);
			// LOGGER.info("ServiceRecords found: " + serviceRecords);
			// CODIE_BLE_SERVICE_UUID
			CodieSessionThread thread = new CodieSessionThread("Codie", codieDevice.getBluetoothAddress(), CODIE_RX_CHARACTERISTICS, CODIE_TX_CHARACTERISTICS,
					"5000");
			thread.start();
			ret = thread;
		} catch (Exception e) {
			LOGGER.warn("Failed to connect to Codie: " + e.getMessage(), e);
		}
		return ret;
	}

	private static CodieClientSession getMockSession() throws CodieConnectionException {
		CodieMockClientSession codieMockClientSession = new CodieMockClientSession();
		try {
			codieMockClientSession.connect(null);
		} catch (IOException e) {
			throw new CodieConnectionException(e.getMessage(), e);
		}
		return codieMockClientSession;
	}

	private static boolean isMissingBluetoothStackException(final Throwable e) {
		if ((e instanceof BluetoothStateException) && "BluetoothStack not detected".equals(e.getMessage())) {
			return true;
		} else if (e.getCause() != null) {
			return isMissingBluetoothStackException(e.getCause());
		}
		return false;
	}

	/**
	 * @param selectedDevice
	 * @param serviceUUID
	 *            TODO
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private static String selectService(final RemoteDevice selectedDevice, final String serviceUUID) throws IOException, InterruptedException {
		String ret = null;
		List<String> services;
		int selected = 0;
		do {
			LOGGER.info("Searching for services on " + selectedDevice.getFriendlyName(false));
			services = ServicesSearch.search(selectedDevice, serviceUUID);
			if (services.isEmpty()) {
				break;
			} else {
				int idx = 0;
				for (String service : services) {
					idx++;
					LOGGER.info("#" + idx + ". Service: " + service);
				}
				if (idx > 1) {
					LOGGER.info("Select service (1-" + services.size() + "):");
					String input = UserInputProcessor.getInstance().readLine("(\\d+)|(r)|(refresh)");
					if (input != null) {
						if ("r".equalsIgnoreCase(input) || "refresh".equalsIgnoreCase(input)) {
							continue;
						} else {
							selected = Integer.parseInt(input);
							if (selected > services.size()) {
								LOGGER.warn("You must choose from the available devices (1-" + services.size() + ")!");
								selected = 0;
							}
						}
					}
				} else {
					selected = 1;
				}
			}
		} while (selected <= 0);
		if ((selected > 0) && (selected <= services.size())) {
			ret = services.get(selected - 1);
		}
		return ret;
	}

	private static RemoteDevice selectDevice() throws IOException, InterruptedException {
		return new CodieRemoteDevice();
		// RemoteDevice ret = null;
		// List<RemoteDevice> devices;
		// int selected = 0;
		// do {
		// devices = RemoteDeviceDiscovery.discover();
		// if (devices.isEmpty()) {
		// break;
		// } else {
		// int idx = 0;
		// for (RemoteDevice device : devices) {
		// idx++;
		// LOGGER.info("#" + idx + ". Device: " + device.getBluetoothAddress() + " - "
		// + device.getFriendlyName(false));
		// }
		// if (idx > 1) {
		// LOGGER.info("Select device (1-" + devices.size() + "):");
		// String input = UserInputProcessor.getInstance().readLine("(\\d+)|(r)|(refresh)");
		// if (input != null) {
		// if ("r".equalsIgnoreCase(input) || "refresh".equalsIgnoreCase(input)) {
		// continue;
		// } else {
		// selected = Integer.parseInt(input);
		// if (selected > devices.size()) {
		// LOGGER.warn("You must choose from the available devices (1-" + devices.size() + ")!");
		// selected = -1;
		// }
		// }
		// }
		// } else {
		// selected = 1;
		// }
		// }
		// } while (selected <= 0);
		// if (selected > 0 && selected <= devices.size()) {
		// ret = devices.get(selected - 1);
		// }
		// return ret;
	}

	private static boolean retryTimeout() {
		return (System.currentTimeMillis() - lastConnectionFailure) > RETRY_TIMEOUT;
	}

}
