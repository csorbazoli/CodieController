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

	/**
	 * @param arg0
	 */
	protected CodieRemoteDevice() {
		super(CodieBluetoothConnectionFactory.CODIE_MAC_ADDRESS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.bluetooth.RemoteDevice#getFriendlyName(boolean)
	 */
	@Override
	public String getFriendlyName(final boolean alwaysAsk) throws IOException {
		return "Codie";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.bluetooth.RemoteDevice#isTrustedDevice()
	 */
	@Override
	public boolean isTrustedDevice() {
		return true;
	}

}
