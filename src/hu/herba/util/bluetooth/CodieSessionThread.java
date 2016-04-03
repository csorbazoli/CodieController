package hu.herba.util.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

import javax.bluetooth.DataElement;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodieSessionThread extends Thread implements CodieClientSession {
	private static final Logger LOGGER = LogManager.getLogger(CodieSessionThread.class);

	/**
	 * Specifies via the configuration the connection string parameter receiveMTU and transmitMTU for the Bluetooth service that the implementation supports.
	 * The value assigned here must equal the value assignment to the parameter "bluetooth.agent_mtu" configured on the client side in the global.xml file.
	 */
	static final String BLUETOOTH_AGENT_MTU = "bluetooth.agent_mtu";

	static final String TIMEOUT = "timeout";

	/**
	 * This variable refers to the value the parameters ReceiveMTU and TransmitMTU have in the Bluetooth service that this thread starts up. The default value
	 * is 512 if it is not overridden by the user via "bluetooth.agent_mtu" when this thread is started.
	 */
	private final int agentMtu = 512;

	private String rxUuid;
	private String txUuid;

	/**
	 * Default timeout value if not set by the user
	 */
	private int configTimeout = TCKAgentUtil.SHORT;

	public static String message;

	private String data = "data", command = "Command";

	// private L2CAPConnectionNotifier server = null;
	private StreamConnectionNotifier server = null;

	private ServiceRecord serviceRecord;

	private StreamConnection channel = null, helperchannel = null;

	int buffersize = 680, counter = 1, bytes_read = 0, timeout = 0;

	byte[] buffer;

	private boolean can_run = true;

	private String connString;

	private LocalDevice localdevice;

	public CodieSessionThread(final String str, final String serviceId, final String rxId, final String txId, final String customTimeout) {
		super(str);
		setCharacteristics(rxId, txId);
		setConfigTimeout(customTimeout);
		try {
			LOGGER.info("CodieSessionThread: Starting Stream Service...");
			// connString = "btspp://localhost:" + serviceId; // + ";ReceiveMTU=" + txId + ";TransmitMTU=" + rxId;
			connString = "btspp://" + serviceId + ":" + CodieBluetoothConnectionFactory.CODIE_BLE_SERVICE_UUID; // + ";ReceiveMTU=" + txId +
																												// ";TransmitMTU=" + rxId;
			server = (StreamConnectionNotifier) Connector.open(connString, Connector.READ_WRITE, true);
			// LOGGER.info("CodieSessionThread: Starting Bluetooth Service using characteristics(R/T): " + rxId + "/" + txId + " and timeout: " +
			// configTimeout);
			// connString = "btl2cap://localhost:" + serviceId + ";ReceiveMTU=" + agentMtu + ";TransmitMTU=" + agentMtu;
			// "ReceiveMTU=" + rxId + ";TransmitMTU=" + txId;
			// server = (L2CAPConnectionNotifier) Connector.open(connString);
			LOGGER.info("Connection: " + server);

			// Adding an attribute value to the service record with a DataElement
			// of type DataElement.URL
			try {
				localdevice = LocalDevice.getLocalDevice();
				LOGGER.info("Local Device bluetooth address is = " + localdevice.getBluetoothAddress());
				serviceRecord = localdevice.getRecord(server);
				// DataElement docURL = new DataElement(DataElement.URL, "http://www.motorola.com/");
				// srv_record.setAttributeValue(0x000A, docURL);
				//
				// localdevice.updateRecord(srv_record);
				// DataElement tmp = srv_record.getAttributeValue(0x000A);
				// LOGGER.info("The URL attribute added is = " + (String) tmp.getValue());
				LOGGER.info("ServiceRecord: " + serviceRecord);

			} catch (Exception e) {
				LOGGER.error("Error updating the service record in the local device running the agent. "
						+ "WARNING: Tests trying to retreive service record with an attribute value of dataelement type DataElement.URL will fail.", e);
			} // attribute value added to the service record

		} catch (Exception e) {
			LOGGER.error("CodieSessionThread: Error starting Bluetooth service. Aborting service.", e);
			can_run = false;
		}
	}

	/**
	 * @param rxId
	 * @param txId
	 */
	private void setCharacteristics(final String rxId, final String txId) {
		rxUuid = rxId;
		txUuid = txId;
	}

	@Override
	public void run() {
		while (can_run) {
			try {
				LOGGER.info("CodieSessionThread: Waiting for Client to Connect");
				channel = server.acceptAndOpen();
				LOGGER.info("CodieSessionThread: CONNECTED");
			} catch (InterruptedIOException e) {
				LOGGER.error("CodieSessionThread:TCK Interrupted", e);
				return;
			} catch (Exception e) {
				LOGGER.info("CodieSessionThread: Error connecting to client. Aborting connection.");
				can_run = false;
				if ("Stack closed".equals(e.getMessage())) {
					return;
				}
			} finally {
				if (!can_run) {
					command = "CLOSE";
				} else {
					LOGGER.info("CodieSessionThread: Client made a connection");
				}
			}

			can_run = true;
			while (!command.equals("CLOSE")) {
				buffer = new byte[buffersize];
				LOGGER.info("CodieSessionThread: Reading BluetoothConnection Stream");
				try {
					/*
					 * Keep reading until data comes in
					 */
					timeout = 0;
					while (!isReady() && (timeout < 10)) {
						TCKAgentUtil.pause(1000);
						timeout++;
					}

					if (timeout < 10) {
						bytes_read = receive();
						LOGGER.info("CodieSessionThread.run(): Bytes Read: " + bytes_read);
					}
				} catch (Exception e) {
					LOGGER.error("CodieSessionThread: Failure while reading BluetoothConnection.", e);
					timeout = 10;
				}

				if (timeout == 10) {
					LOGGER.info("CodieSessionThread: Client Connection Timed Out. Closing connection");
					message = "CLOSE connection";
					buffer = message.getBytes();
				}

				// allow sending the empty message. Therefore, do not trim the
				// message as doing so will not be able to parse out the
				// command
				message = (new String(buffer));
				LOGGER.info("CodieSessionThread.run(): Message \"" + message + "\" and message size: " + message.length());
				int space = message.indexOf(" ");
				if (space != -1) {
					command = message.substring(0, space);
					data = message.substring(space + 1);
					LOGGER.info("data: " + data + " data len: " + data.length());
					// since the buffer was allocated with size more than the
					// client message sent
					// trim it to get rid of access before sending to client
					data = data.trim();
					LOGGER.info(" data size after trim: " + data.length() + " data: " + data);
				}

				if (command.equals("ECHO")) {
					LOGGER.info("CodieSessionThread: ECHO Command Called");
					TCKAgentUtil.pause(TCKAgentUtil.SHORT);

					try {
						send(data.getBytes());
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Error writing to client. Closing connection", e);
						command = "CLOSE";
					}
				} // ECHO Command

				else if (command.equals("READ")) {
					LOGGER.info("CodieSessionThread: READ Command Called");
				} // READ Command

				else if (command.equals("LOG")) {
					LOGGER.info("CodieSessionThread LOG: " + data);
				} // LOG Command

				else if (command.equals("WAIT")) {
					data = data.trim();
					try {
						LOGGER.info("CodieSessionThread: WAIT Command Called");
						int timetowait = Integer.parseInt(data);
						Thread.sleep(timetowait);
					} catch (Exception e) {
					}
				} // WAIT Command

				else if (command.equals("CLIENT")) {
					LOGGER.info("CodieSessionThread: CLIENT Command Called");

					command = "CLOSE";
					try {
						TCKAgentUtil.pause(TCKAgentUtil.SHORT);
						channel.close();
						TCKAgentUtil.pause(TCKAgentUtil.MEDIUM);
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Error closing existing connection.", e);
					}

					data = "btl2cap://" + data + ";authenticate=false;encrypt=false;master=false;ReceiveMTU=" + rxUuid + ";TransmitMTU=" + txUuid;
					try {
						helperchannel = (StreamConnection) Connector.open(data);
						LOGGER.info("CodieSessionThread: Connected successfully to client");
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Unable to connect to the client the following connection string: " + data + " with error: " + e, e);
					}

					try {
						TCKAgentUtil.pause(TCKAgentUtil.SHORT);
						helperchannel.close();
					} catch (Exception e) {
					}
				} // CLIENT Command

				else if (command.equals("GETSDCLASS")) {
					LOGGER.info("CodieSessionThread: GETSDCLASS Command Called");

					int sdClass = -1;
					String msg = null, btAddress;

					btAddress = data.substring(0, 12);
					command = "CLOSE";

					try {
						channel.close();
						// https://opensource.motorola.com/sf/discussion/do/listPosts/projects.jsr82/discussion.google_jsr_82_support.topc1845
						// TCKAgentUtil.pause(TCKAgentUtil.SHORT);
						TCKAgentUtil.pause(TCKAgentUtil.MEDIUM);
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Error closing existing connection.", e);
					}

					sdClass = TCKAgentUtil.getServiceClass(btAddress, configTimeout);
					LOGGER.info("CodieSessionThread: Retrieved the service classes for " + btAddress + " : " + Integer.toString(sdClass, 16));

					if (sdClass == -1) {
						LOGGER.error("CodieSessionThread: Unable to retrieve ServiceDeviceClass of the client device: " + btAddress);
					}

					msg = Integer.toString(sdClass);

					data = "btl2cap://" + data + ";authenticate=false;encrypt=false;master=false;ReceiveMTU=" + rxUuid + ";TransmitMTU=" + txUuid;
					try {
						helperchannel = (StreamConnection) Connector.open(data);
						sendHelper(msg.getBytes());
						TCKAgentUtil.pause(TCKAgentUtil.SHORT);
						helperchannel.close();
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Unable to send data with the connection string: " + data, e);
					}
				} // GETSDCLASS Command
				else if (command.equals("GETSRHANDLE")) {
					LOGGER.info("CodieSessionThread: GETSRHANDLE Command Called WITH DATA " + data);
					long recordHandle = -1;
					String msg = null, btAddress;

					space = data.indexOf(' ');
					if (space == -1) {
						LOGGER.error("CodieSessionThread: Missing UUID");
					}

					String url = data.substring(0, space);
					LOGGER.info("CodieSessionThread.run(): URL \"" + url + "\"");

					String uuid = data.substring(space + 1);
					LOGGER.info("CodieSessionThread.run(): UUID \"" + uuid + "\"");

					btAddress = url.substring(0, 12);
					LOGGER.info("CodieSessionThread.run(): BTADDRESS: \"" + btAddress + "\"");
					command = "CLOSE";

					try {
						channel.close();
						TCKAgentUtil.pause(TCKAgentUtil.SHORT);
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Error closing existing connection.", e);
					}

					UUID uuids[] = { new UUID(uuid, false) };
					LOGGER.info("CodieSessionThread: Searching for services with uuid \"" + uuid + "\" on device " + btAddress);
					ServiceRecord records[] = TCKAgentUtil.getServiceRecords(btAddress, uuids);

					if ((records == null) || (records.length == 0)) {
						LOGGER.error("CodieSessionThread: Unable to retreive Service records for the service with uuid \"" + uuid + "\" ,running on device \""
								+ btAddress + "\" .");
					} else {
						LOGGER.info("CodieSessionThread: Retreived a service record");
						ServiceRecord record = records[0];
						DataElement elem = record.getAttributeValue(0x0000);
						if (elem == null) {
							LOGGER.error("CodieSessionThread: Missing Record handle for service record.");
						} else {
							recordHandle = elem.getLong();
						}
					}

					msg = Long.toString(recordHandle);
					LOGGER.info("CodieSessionThread.run(): Retreived handle " + recordHandle);

					url = "btl2cap://" + url + ";authenticate=false;encrypt=false;master=false;ReceiveMTU=" + rxUuid + ";TransmitMTU=" + txUuid;
					try {
						helperchannel = (StreamConnection) Connector.open(url);
						LOGGER.info("CodieSessionThread.run(): Sending msg \"" + msg + "\"");
						sendHelper(msg.getBytes());
						TCKAgentUtil.pause(TCKAgentUtil.SHORT);
						helperchannel.close();
					} catch (Exception e) {
						LOGGER.error("CodieSessionThread: Unable to send data with the connection string: " + url + ". Exception: " + e, e);
					}
				} // GETSRHANDLE command
				else { // If no command is executed, then CLOSE connection
					if (!command.equals("CLOSE")) {
						LOGGER.error("CodieSessionThread: Unrecognized Command");
					}
				}
			} // While Channel Connection Not Closed

			LOGGER.info("Closing Channel");

			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e) {
				}
			}

			command = "Command";
			LOGGER.info("CodieSessionThread: Connection Closed By Client");
		} // While true
	} // method run()

	private int send(final byte[] dataPackage) {
		int ret = 0;
		if (isReady()) {
			// channel.send(data.getBytes());
			try {
				dataOutputStream = channel.openDataOutputStream();
			} catch (IOException e) {
				LOGGER.error("Failed to open output stream:" + e.getMessage(), e);
				ret = -1;
			}
			if (dataOutputStream != null) {
				try {
					dataOutputStream.write(dataPackage);
				} catch (IOException e) {
					LOGGER.error("Failed to write output stream:" + e.getMessage(), e);
					ret = -2;
				}
			}
		} else {
			ret = 1;
		}
		return ret;
	}

	private void sendHelper(final byte[] dataPackage) {
		// channel.send(data.getBytes());
		try {
			dataOutputStreamHelper = helperchannel.openDataOutputStream();
		} catch (IOException e) {
			LOGGER.error("Failed to open helper output stream:" + e.getMessage(), e);
		}
		if (dataOutputStreamHelper != null) {
			try {
				dataOutputStreamHelper.write(dataPackage);
			} catch (IOException e) {
				LOGGER.error("Failed to write helper output stream:" + e.getMessage(), e);
			}
		}
	}

	private DataInputStream dataInputStream;

	private DataOutputStream dataOutputStream;
	private DataOutputStream dataOutputStreamHelper;

	private int receive() {
		int ret = 0;
		if (dataInputStream == null) {
			try {
				dataInputStream = channel.openDataInputStream();
			} catch (IOException e) {
				LOGGER.error("Failed to open input stream: " + e.getMessage(), e);
			}
		}
		if (dataInputStream != null) {
			try {
				ret = dataInputStream.read(buffer);
			} catch (IOException e) {
				LOGGER.error("Failed to read input stream: " + e.getMessage(), e);
			}
		}
		return ret;
	}

	@Override
	public boolean isReady() {
		return channel != null; // channel.ready();
	}

	/**
	 * @return the agentMtu
	 */
	public int getAgentMtu() {
		return agentMtu;
	}

	/**
	 * @return the configTimeout
	 */
	public int getConfigTimeout() {
		return configTimeout;
	}

	/**
	 * @param configTimeout
	 *            the configTimeout to set
	 */
	private void setConfigTimeout(final String customizedTimeout) {
		if (customizedTimeout != null) {
			configTimeout = Integer.parseInt(customizedTimeout.trim());
			LOGGER.info("Use customized timeout sets to: " + configTimeout);
		} else {
			LOGGER.info("Use default timeout: " + configTimeout);
		}

	}

	/**
	 * @return the server
	 */
	public StreamConnectionNotifier getServer() {
		return server;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hu.herba.util.bluetooth.CodieClientSession#sendCommand(java.lang.String, byte[])
	 */
	@Override
	public int sendCommand(final String commandName, final byte[] data) {
		LOGGER.trace("Send command: " + commandName + "...");
		return send(data);
	}

} // class CodieSessionThread
