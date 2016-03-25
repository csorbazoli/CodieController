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

	protected static final Object serviceSearchCompletedEvent = new Object();

	private static final DiscoveryListener listener = new DiscoveryListener() {

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
					LOGGER.info("service " + serviceName.getValue() + " found " + url);
				} else {
					LOGGER.info("service found " + url);
				}
			}
		}

		@Override
		public void serviceSearchCompleted(final int transID, final int respCode) {
			LOGGER.info("service search completed!");
			synchronized (serviceSearchCompletedEvent) {
				serviceSearchCompletedEvent.notifyAll();
			}
		}

	};

	public static List<String> search(final RemoteDevice device, final String uuid)
			throws IOException, InterruptedException {
		serviceFound.clear();

		LOGGER.info("Local device: " + LocalDevice.getLocalDevice().getBluetoothAddress() + " - "
				+ LocalDevice.getLocalDevice().getFriendlyName());
		List<UUID> uuidList = new ArrayList<>();
		// uuidList.add(OBEX_OBJECT_PUSH);
		if (uuid != null) {
			uuidList.add(new UUID(uuid, false));
			// uuidList.add(new UUID("52af0002978a628dc8450a104ca2b8dd", false));
			// uuidList.add(new UUID("52af0003978a628dc8450a104ca2b8dd", false));
		}

		UUID[] searchUuidSet = uuidList.toArray(new UUID[0]);
		int[] attrIDs = new int[] { 0x0100, // Service name
				0x2a00, // device name
				0x0101, 0x0A0, 0x0A1, 0x0A2, 0x0A3, 0x0A4 };

		synchronized (serviceSearchCompletedEvent) {
			int securityMode = ServiceRecord.NOAUTHENTICATE_NOENCRYPT;
			String service = null;
			for (UUID id : searchUuidSet) {
				LOGGER.info("Direct search (" + securityMode + "): " + id);
				service = LocalDevice.getLocalDevice().getDiscoveryAgent().selectService(id, securityMode, false);
				if (service != null) {
					break;
				}
			}
			if (service == null) {
				LOGGER.info("****  search services (" + uuid + ") on " + device.getBluetoothAddress() + " "
						+ device.getFriendlyName(false));
				LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, device,
						listener);
				serviceSearchCompletedEvent.wait();
			} else {
				LOGGER.info("Service found: " + service);
				serviceFound.add(service);
			}
		}
		return serviceFound;
	}

	public static Collection<String> search(final String uuid) throws IOException, InterruptedException {

		// First run RemoteDeviceDiscovery and use discovered device
		Collection<RemoteDevice> devices = RemoteDeviceDiscovery.discover();

		for (RemoteDevice btDevice : devices) {
			search(btDevice, uuid);
		}
		return serviceFound;
	}

}