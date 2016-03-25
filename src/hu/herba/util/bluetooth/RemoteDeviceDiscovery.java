/**
 * http://snapshot.bluecove.org/bluecove/apidocs/overview-summary.html#DeviceDiscovery
 */
package hu.herba.util.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Minimal Device Discovery example.
 */
public class RemoteDeviceDiscovery {
	private static final Logger LOGGER = LogManager.getLogger(RemoteDeviceDiscovery.class);
	public static final List<RemoteDevice> devicesDiscovered = new ArrayList<>();

	public static void main(final String[] args) {
		System.out.println("OS: " + getOS() + " (" + System.getProperty("os.name") + ")");
		Collection<RemoteDevice> list;
		try {
			list = RemoteDeviceDiscovery.discover();
			for (RemoteDevice device : list) {
				System.out.println("Device: " + device + " - " + device.getFriendlyName(false));
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static List<RemoteDevice> discover() throws IOException, InterruptedException {

		final Object inquiryCompletedEvent = new Object();

		devicesDiscovered.clear();

		DiscoveryListener listener = new DiscoveryListener() {

			@Override
			public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
				LOGGER.info("Device " + btDevice.getBluetoothAddress() + " found");
				devicesDiscovered.add(btDevice);
				try {
					LOGGER.info("     name " + btDevice.getFriendlyName(false));
				} catch (IOException cantGetDeviceName) {
				}
			}

			@Override
			public void inquiryCompleted(final int discType) {
				LOGGER.info("Device Inquiry completed!");
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void serviceSearchCompleted(final int transID, final int respCode) {
				LOGGER.info("Service search completed");
			}

			@Override
			public void servicesDiscovered(final int transID, final ServiceRecord[] servRecord) {
				LOGGER.info("Services discovered");
			}
		};

		synchronized (inquiryCompletedEvent) {
			// LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
			boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.LIAC,
					listener);
			if (started) {
				LOGGER.info("wait for device inquiry to complete...");
				inquiryCompletedEvent.wait();
				LOGGER.info(devicesDiscovered.size() + " device(s) found");
			}
			for (RemoteDevice d : LocalDevice.getLocalDevice().getDiscoveryAgent()
					.retrieveDevices(DiscoveryAgent.PREKNOWN)) {
				devicesDiscovered.add(d);
			}
		}
		return devicesDiscovered;
	}

	private static String os;

	static String getOS() {
		if (os != null) {
			return os;
		}
		String sysName = System.getProperty("os.name");
		if (sysName == null) {
			LOGGER.fatal("Native Library not available on unknown platform");
			os = "OS_UNSUPPORTED";
		} else {
			sysName = sysName.toLowerCase();
			if (sysName.indexOf("windows") != -1) {
				if (sysName.indexOf("ce") != -1) {
					os = "OS_WINDOWS_CE";
				} else {
					os = "OS_WINDOWS";
				}
			} else if (sysName.indexOf("mac os x") != -1) {
				os = "OS_MAC_OS_X";
			} else if (sysName.indexOf("linux") != -1) {
				String javaRuntimeName = System.getProperty("java.runtime.name");
				if (javaRuntimeName != null && javaRuntimeName.toLowerCase().indexOf("android runtime") != -1) {
					try {
						int androidApiLevel = Class.forName("android.os.Build$VERSION").getField("SDK_INT")
								.getInt(null);
						// android 2.0 has code 5
						if (androidApiLevel >= 5) {
							// let's consider probability that Android 2.x bluetooth APIs
							// are available but for some reason, we want to use the native
							// bluez stack directly.
							// In this case, user just has not to include
							// bluecove-android2.jar in classpath.
							Class.forName("com.intel.bluetooth.BluetoothStackAndroid");
							os = "OS_ANDROID_2_X";
						} else {
							os = "OS_ANDROID_1_X";
						}
					} catch (Exception ex) {
						// if field android.os.Build.VERSION.SDK_INT doesn't exist,
						// we are on android 1.5 or earlier as this field was introduced
						// in android 1.6 (API Level 4).

						// also if com.intel.bluetooth.BluetoothStackAndroid class
						// doesn't exist in classpath, we will use native
						// bluez implementation
						os = "OS_ANDROID_1_X";
					}
				} else {
					os = "OS_LINUX";
				}
			} else {
				LOGGER.fatal("Native Library not available on platform " + sysName);
				os = "OS_UNSUPPORTED";
			}
		}
		return os;
	}

}