/**
 *
 */
package hu.herba.util.bluetooth;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author csorbazoli
 *
 */
public class CodieRemoteDevice extends RemoteDevice {
	private static final Logger LOGGER = LogManager.getLogger(CodieRemoteDevice.class);

	protected CodieRemoteDevice() {
		super(CodieBluetoothConnectionFactory.CODIE_MAC_ADDRESS);
		LOGGER.info("Codie remote device: " + getBluetoothAddress());
	}

	@Override
	public String getFriendlyName(final boolean alwaysAsk) throws IOException {
		return "Codie";
	}

	@Override
	public boolean isTrustedDevice() {
		return true;
	}

	@Override
	public boolean authenticate() throws IOException {
		return true;
	}

}
