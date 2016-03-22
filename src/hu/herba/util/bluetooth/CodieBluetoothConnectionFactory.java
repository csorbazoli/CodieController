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

	// Codie has a custom BLE service with the following UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
	private static final String CODIE_BLE_SERVICE_UUID = "52af0001-978a-628d-c845-0a104ca2b8dd";
	// In this service there are two characteristics:
	private static final String CODIE_RX_CHARACTERISTICS = "{52af0002-978a-628d-c845-0a104ca2b8dd}";
	// Codie receives commands through this characteristic. It is readable and writeable (write without response).
	// Be aware, that only 20 bytes can be written at a time (due to the BLE packet payload limit).
	private static final String CODIE_TX_CHARACTERISTICS = "{52af0003-978a-628d-c845-0a104ca2b8dd}";

	// Codie sends commands through this characteristic.
	// Readable and can be subscribed for notification (indication not supported).

	/**
	 * Connect to Codie with bluetooth driver.
	 *
	 * @return Connection
	 *
	 */
	public static Object connect() throws CodieConnectionException {
		Object ret = null;
		LOGGER.debug("Open connection to codie...");
		// Code MAC address: FF:0b:C9:5A:08:C7
		// UUID: 52af0001-978a-628d-c845-0a104ca2b8dd
		// RX: {52af0002-978a-628d-c845-0a104ca2b8dd}
		// TX: {52af0003-978a-628d-c845-0a104ca2b8dd} - not used yet
		try {
			Collection<RemoteDevice> devices = RemoteDeviceDiscovery.discover();
			for (RemoteDevice device : devices) {
				LOGGER.info("Device: " + device.getBluetoothAddress() + " - " + device.getFriendlyName(false));
				ret = device;
			}
		} catch (IOException | InterruptedException e) {
			throw new CodieConnectionException(e);
		}
		return ret;
	}

}
