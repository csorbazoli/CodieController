/**
 *
 */
package hu.herba.util.bluetooth;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;

/**
 * @author Zoltán
 *
 */
public class CodieRemoteDevice extends RemoteDevice {

	protected CodieRemoteDevice() {
		super(CodieBluetoothConnectionFactory.CODIE_MAC_ADDRESS);
	}

	@Override
	public String getFriendlyName(final boolean alwaysAsk) throws IOException {
		return "Codie";
	}

	@Override
	public boolean isTrustedDevice() {
		return true;
	}

}
