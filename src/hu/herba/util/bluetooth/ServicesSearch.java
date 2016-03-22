package hu.herba.util.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * Minimal Services Search example.
 */
public class ServicesSearch {

	private static final Logger LOGGER = LogManager.getLogger(ServicesSearch.class);
	static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);

	static final UUID OBEX_FILE_TRANSFER = new UUID(0x1106);

	public static final List<String> serviceFound = new ArrayList<>();

	public static void main(final String[] args) throws IOException, InterruptedException {
		LOGGER.info("Search for bluetooth serivces... ");
		String uuid = null;
		if (args != null && args.length > 0) {
			uuid = args[0];
		}
		for (String url : ServicesSearch.search(uuid)) {
			LOGGER.info("URL: " + url);
		}
	}

	public static Collection<String> search(final String uuid) throws IOException, InterruptedException {

		// First run RemoteDeviceDiscovery and use discovered device
		Collection<RemoteDevice> devices = RemoteDeviceDiscovery.discover();

		serviceFound.clear();

		UUID serviceUUID = OBEX_OBJECT_PUSH;
		if (uuid != null) {
			serviceUUID = new UUID(uuid, false);
		}

		final Object serviceSearchCompletedEvent = new Object();

		DiscoveryListener listener = new DiscoveryListener() {

			@Override
			public void deviceDiscovered(final RemoteDevice btDevice, final DeviceClass cod) {
				LOGGER.info("DeviceDiscovered: " + btDevice + ", " + cod);
			}

			@Override
			public void inquiryCompleted(final int discType) {
				LOGGER.info("Inquiry completed: " + discType);
			}

			@Override
			public void servicesDiscovered(final int transID, final ServiceRecord[] servRecord) {
				for (int i = 0; i < servRecord.length; i++) {
					String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
					if (url == null) {
						continue;
					}
					serviceFound.add(url);
					DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
					if (serviceName != null) {
						System.out.println("service " + serviceName.getValue() + " found " + url);
					} else {
						System.out.println("service found " + url);
					}
				}
			}

			@Override
			public void serviceSearchCompleted(final int transID, final int respCode) {
				System.out.println("service search completed!");
				synchronized (serviceSearchCompletedEvent) {
					serviceSearchCompletedEvent.notifyAll();
				}
			}

		};

		UUID[] searchUuidSet = new UUID[] { serviceUUID };
		int[] attrIDs = new int[] { 0x0100 // Service name
		};

		for (RemoteDevice btDevice : devices) {

			synchronized (serviceSearchCompletedEvent) {
				System.out.println("****  search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
				LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
				serviceSearchCompletedEvent.wait();
			}
		}

		return serviceFound;

	}

}