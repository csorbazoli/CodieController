/**
 *
 */
package hu.herba.util.bluetooth;

import java.io.IOException;
import java.util.Collection;

import javax.bluetooth.RemoteDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author csorbazoli
 *
 */
public class CodieBluetoothConnectionFactory {

	private static final Logger LOGGER = LogManager.getLogger(CodieBluetoothConnectionFactory.class);

	private static final long RETRY_TIMEOUT = 1000; // retry timeout in milliseconds for a failed connection

	// Codie has a custom BLE service with the following UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
	private static final String CODIE_BLE_SERVICE_UUID = "52af0001-978a-628d-c845-0a104ca2b8dd";
	// In this service there are two characteristics:
	private static final String CODIE_RX_CHARACTERISTICS = "{52af0002-978a-628d-c845-0a104ca2b8dd}";
	// Codie receives commands through this characteristic. It is readable and writeable (write without response).
	// Be aware, that only 20 bytes can be written at a time (due to the BLE packet payload limit).
	private static final String CODIE_TX_CHARACTERISTICS = "{52af0003-978a-628d-c845-0a104ca2b8dd}";

	// Codie sends commands through this characteristic.
	// Readable and can be subscribed for notification (indication not supported).
	private static Object currentConnection;
	private static long lastConnectionFailure = 0l;
	private static String lastError;
	private static int errorCounter = 0;

	/**
	 * Connect to Codie with bluetooth driver.
	 *
	 * @return Connection
	 *
	 */
	public static synchronized Object connect() {
		if (currentConnection == null && retryTimeout()) {
			if (lastError == null) {
				LOGGER.debug("Open connection to codie...");
			}
			try {
				openConnection();
			} catch (CodieConnectionException e) {
				if (lastError == null || !lastError.equals(e.getMessage())) {
					lastError = e.getMessage();
					LOGGER.warn("Failed to connect to Codie: " + e.getMessage());
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

	private static void openConnection() throws CodieConnectionException {
		// Code MAC address: FF:0b:C9:5A:08:C7
		// UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
		// RX: {52af0002-978a-628d-c845-0a104ca2b8dd}
		// TX: {52af0003-978a-628d-c845-0a104ca2b8dd} - not used yet
		try {
			Collection<RemoteDevice> devices = RemoteDeviceDiscovery.discover();
			for (RemoteDevice device : devices) {
				LOGGER.info("Device: " + device.getBluetoothAddress() + " - " + device.getFriendlyName(false));
				currentConnection = device;
			}
		} catch (IOException | InterruptedException e) {
			throw new CodieConnectionException(e);
		}
	}

	private static boolean retryTimeout() {
		return System.currentTimeMillis() - lastConnectionFailure > RETRY_TIMEOUT;
	}

}
