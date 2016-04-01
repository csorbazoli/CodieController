/**
 *
 */
package hu.herba.util.bluetooth.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.obex.HeaderSet;
import javax.obex.Operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.CodieCommandBase;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;

/**
 * @author zcsorba
 *
 */
public class CodieMockOperation implements Operation {
	private static final Logger LOGGER = LogManager.getLogger(CodieMockOperation.class);

	private final String type;
	private InputStream inputStream;
	private final byte[] buf = new byte[20];
	private DataInputStream dataInputStream;
	private ByteArrayOutputStream outputStream;
	private DataOutputStream dataOutputStream;
	private HeaderSet headerSetReceived;
	private boolean aborted;
	private final DataPackage pack = new DataPackage();
	private static final Random rand = new Random(System.currentTimeMillis());
	private static ResponseThread responseThread;

	private static final int MIN_DIST = 20;
	private static final int MAX_DIST = 100;
	private static final int MAX_LIGHT = 4000;
	private static final int MIN_LIGHT = 500;
	private static final long IMMEDIATE = 100; // 0.1 sec

	/**
	 * @param headerSet
	 */
	public CodieMockOperation(final HeaderSet headerSet) {
		String typeParam;
		try {
			typeParam = String.valueOf(headerSet.getHeader(HeaderSet.TYPE));
		} catch (IOException e) {
			typeParam = "binary";
		}
		type = typeParam;
		headerSetReceived = headerSet;
	}

	@Override
	public String getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.microedition.io.ContentConnection#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return "UTF-8";
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = new ByteArrayInputStream(buf);
		}
		return inputStream;
	}

	@Override
	public DataInputStream openDataInputStream() throws IOException {
		if (dataInputStream == null) {
			dataInputStream = new DataInputStream(openInputStream());
		}
		return dataInputStream;
	}

	@Override
	public void close() throws IOException {
		if (inputStream != null) {
			inputStream.close();
		}
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = new ByteArrayOutputStream();
		}
		return outputStream;
	}

	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		if (dataOutputStream == null) {
			dataOutputStream = new DataOutputStream(openOutputStream());
		}
		return dataOutputStream;
	}

	@Override
	public void abort() throws IOException {
		aborted = true;
	}

	public boolean isAborted() {
		return aborted;
	}

	@Override
	public HeaderSet getReceivedHeaders() throws IOException {
		return headerSetReceived;
	}

	@Override
	public void sendHeaders(final HeaderSet headerset) throws IOException {
		headerSetReceived = headerset;
	}

	@Override
	public int getResponseCode() throws IOException {
		return processOperation();
	}

	/**
	 * This is the actual method that is doing the requested operation.
	 */
	private int processOperation() throws IOException {
		int ret = 0;
		CodieCommandType operation;
		byte[] dataPackage = outputStream.toByteArray();
		try {
			operation = checkHeaderContent(dataPackage);
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
		switch (operation) {
		case Null:
		case Echo:
			// nothing to do
			break;
		case BatteryGetSoc:
			ret = handleBatteryGetSoc(dataPackage);
			break;
		case DriveDistance:
			ret = handleDriveDistance(dataPackage);
			break;
		case DriveSpeed:
			ret = handleDriveSpeed(dataPackage);
			break;
		case DriveTurn:
			ret = handleDriveTurn(dataPackage);
			break;
		case LedSetColor:
			ret = handleLedSetColor(dataPackage);
			break;
		case LightSenseGetRaw:
			ret = handleLightSenseGetRaw(dataPackage);
			break;
		case LineGetRaw:
			ret = handleLineGetRaw(dataPackage);
			break;
		case MicGetRaw:
			ret = handleMicGetRaw(dataPackage);
			break;
		case SonarGetRange:
			ret = handleSonarGetRange(dataPackage);
			break;
		case SpeakBeep:
			ret = handleSpeakBeep(dataPackage);
			break;
		default:
			LOGGER.warn("Operation type " + operation + " is not implemented in " + this.getClass().getSimpleName());
		}
		return ret;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleSpeakBeep(final byte[] dataPackage) {
		// nothing to do, result is always 0
		LOGGER.trace("DING-DONG");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(0, ArgumentType.U8);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleSonarGetRange(final byte[] dataPackage) {
		LOGGER.trace("SONAR...");
		pack.prepareResponse(dataPackage, 2);
		// TODO handle virtual map where mock codie can move and we can measure the distance from the virtual walls

		int randomDistance = getRandomValue(MIN_DIST, MAX_DIST);
		pack.addArgument(randomDistance, ArgumentType.U16);
		setResponseTimeout(300);
		return 0;
	}

	private int getRandomValue(final int min, final int max) {
		int maxDiff = max - min;
		return (int) (min + Math.abs((2 * ((System.currentTimeMillis() / 1000) % maxDiff)) - maxDiff));
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleMicGetRaw(final byte[] dataPackage) {
		LOGGER.trace("MIC");
		pack.prepareResponse(dataPackage, 2);
		pack.addArgument(getRandom(1000, 1500), ArgumentType.U16);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleLineGetRaw(final byte[] dataPackage) {
		LOGGER.trace("ReadLines...");
		pack.prepareResponse(dataPackage, 4);
		pack.addArgument(getRandom(0, 4000), ArgumentType.U16);
		pack.addArgument(getRandom(0, 4000), ArgumentType.U16);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleLightSenseGetRaw(final byte[] dataPackage) {
		LOGGER.trace("Light");
		pack.prepareResponse(dataPackage, 2);
		// 0-brightest, 4095-darkest
		pack.addArgument(getRandomValue(MIN_LIGHT, MAX_LIGHT), ArgumentType.U16);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleLedSetColor(final byte[] dataPackage) {
		// TODO ledMask[u16], hue[u8], saturation[u8], value[u8]
		// LedMask can be used to set leds one-by-one!!! :)
		LOGGER.trace("SET LED COLOR...");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(0, ArgumentType.U8);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleDriveTurn(final byte[] dataPackage) {
		// TODO get degree u16 and speed i8
		// TODO handle virtual map where mock codie can move
		LOGGER.trace("TURN....");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(0, ArgumentType.U8);
		setResponseTimeout(3000);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleDriveSpeed(final byte[] dataPackage) {
		// TODO get speedLeft[i8](%), speedRight[i8](%)
		// TODO handle virtual map where mock codie can move
		LOGGER.trace("SPEED....");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(0, ArgumentType.U8);
		setResponseTimeout(2000);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleDriveDistance(final byte[] dataPackage) {
		// TODO get distance[u16](mm), speedLeft[i8](%), speedRight[i8](%)
		// TODO handle virtual map where mock Codie can move
		LOGGER.trace("DISTANCE....");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(0, ArgumentType.U8);
		setResponseTimeout(4000);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private int handleBatteryGetSoc(final byte[] dataPackage) {
		LOGGER.trace("BATTERY");
		pack.prepareResponse(dataPackage, 1);
		pack.addArgument(getRandomValue(0, 100), ArgumentType.U8);
		setResponseTimeout(IMMEDIATE);
		return 0;
	}

	/**
	 * @param dataPackage
	 * @return OperationType specified in the header data
	 * @throws IllegalArgumentException
	 *             if operation type is unknown, or header content is invalid (e.g. destination/receiver is invalid)
	 */
	private CodieCommandType checkHeaderContent(final byte[] dataPackage) throws IllegalArgumentException {
		CodieCommandType ret = CodieCommandType.DriveSpeed;
		// check info APP -> MCU/BLE/broadcast
		int destination = checkInfo(dataPackage);
		// determine operation type
		switch (destination) {
		case CodieCommandBase.TO_MCU:
			ret = getMCUCommand(dataPackage);
			break;
		case CodieCommandBase.TO_BLE:
			ret = getBLECommand(dataPackage);
			break;
		}
		return ret;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private CodieCommandType getBLECommand(final byte[] dataPackage) {
		if (dataPackage.length < 5) {
			throw new IllegalArgumentException("Received data package does not contain the CMD bytes!");
		}
		CodieCommandType ret = null;
		// only Null and Echo commands are handled!
		int cmdByte = (dataPackage[3] << 8) & dataPackage[4];
		switch (cmdByte) {
		case 0x00: // Null command
			ret = CodieCommandType.Null;
			break;
		case 0x01: // Echo command
			ret = CodieCommandType.Echo;
			break;
		default:
			throw new IllegalArgumentException("Invalid BLE command type 0x" + Integer.toHexString(cmdByte) + "!");
		}
		return ret;
	}

	/**
	 * @param dataPackage
	 * @return
	 */
	private CodieCommandType getMCUCommand(final byte[] dataPackage) {
		if (dataPackage.length < 5) {
			throw new IllegalArgumentException("Received data package does not contain the CMD bytes!");
		}
		CodieCommandType ret = null;
		// only Null and Echo commands are handled!
		int cmdByte = (dataPackage[4] << 8) + dataPackage[3];
		switch (cmdByte) {
		case 0x00: // Null command
			ret = CodieCommandType.Null;
			break;
		case 0x01: // Echo command
			ret = CodieCommandType.Echo;
			break;
		case 0x1060:
			ret = CodieCommandType.DriveSpeed;
			break;
		case 0x1061:
			ret = CodieCommandType.DriveDistance;
			break;
		case 0x1062:
			ret = CodieCommandType.DriveTurn;
			break;
		case 0x1063:
			ret = CodieCommandType.SonarGetRange;
			break;
		case 0x1064:
			ret = CodieCommandType.SpeakBeep;
			break;
		case 0x1065:
			ret = CodieCommandType.LedSetColor;
			break;
		case 0x1069:
			ret = CodieCommandType.BatteryGetSoc;
			break;
		case 0x106a:
			ret = CodieCommandType.LightSenseGetRaw;
			break;
		case 0x106b:
			ret = CodieCommandType.LineGetRaw;
			break;
		case 0x106c:
			ret = CodieCommandType.MicGetRaw;
			break;
		default:
			throw new IllegalArgumentException("Invalid MCU command type 0x" + Integer.toHexString(cmdByte) + "!");
		}
		return ret;
	}

	private int checkInfo(final byte[] dataPackage) throws IllegalArgumentException {
		if (dataPackage.length < 3) {
			throw new IllegalArgumentException("Received data package does not contain the INFO byte!");
		}
		// destination could be CodieCommandBase.TO_MCU/TO_BLE
		byte infoByte = dataPackage[0];
		int dest = (infoByte & 0x0c0) >> 6;
		switch (dest) {
		case 0: // APP
			throw new IllegalArgumentException("Invalid INFO byte (0x" + Integer.toHexString(infoByte) + ")! Destination should not be APP!");
		case 1: // MCU
			dest = CodieCommandBase.TO_MCU;
			LOGGER.trace("Route: APP -> MCU");
			break;
		case 2: // BLE
			dest = CodieCommandBase.TO_BLE;
			LOGGER.debug("Route: APP -> BLE");
			break;
		default:
			throw new IllegalArgumentException("Invalid INFO byte (0x" + Integer.toHexString(infoByte) + ") - unknown!");
		}
		// source should be APP
		int source = infoByte & 0x30;
		if (source != 0) {
			throw new IllegalArgumentException("Invalid INFO byte (0x" + Integer.toHexString(infoByte) + ")! Source should be APP!");
		}
		// Priority could be high/normal
		int prio = infoByte & 0x0f;
		switch (prio) {
		case 0: // normal
			LOGGER.trace("Prio: NORMAL");
			break;
		case 8: // high
			LOGGER.trace("Prio: HIGH");
			break;
		default:
			LOGGER.warn("Invalid INFO byte (0x" + Integer.toHexString(infoByte) + ")! Priority should use only the P3 bit!");
		}
		int seq = (dataPackage[2] << 8) + dataPackage[1];
		LOGGER.trace("Sequence: " + seq);
		return dest;
	}

	protected void sendResponse() {
		LOGGER.trace("Send response(" + hashCode() + "): " + pack.readResponseSequence());
		// send back dataPackage constructed by command handler methods
		CodieCommandProcessor.getInstance().processResponse(pack.getPackage());
	}

	private static class ResponseThread implements Runnable {
		private static final long RESPONSE_THREAD_TIMEOUT = 100;
		private final SortedMap<Long, DataPackage> responseMap = Collections.synchronizedSortedMap(new TreeMap<Long, DataPackage>());
		private boolean stop = false;

		@Override
		public void run() {
			while (!stop) {
				sendResponses();
				waiting(RESPONSE_THREAD_TIMEOUT);
			}
		}

		private void sendResponses() {
			DataPackage nextPackage = responseMap.isEmpty() ? null : responseMap.get(responseMap.firstKey());
			while ((nextPackage != null) && (nextPackage.getTimestamp() <= System.currentTimeMillis())) {
				responseMap.remove(responseMap.firstKey());
				LOGGER.debug("Send response: " + nextPackage.readResponseSequence());
				// send back dataPackage constructed by command handler methods
				CodieCommandProcessor.getInstance().processResponse(nextPackage.getPackage());
				nextPackage = responseMap.isEmpty() ? null : responseMap.get(responseMap.firstKey());
			}
		}

		public void stop() {
			stop = true;
		}

		private synchronized void waiting(final long timeout) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				LOGGER.trace("Waiting (" + timeout + ") interrupted: " + e.getMessage(), e);
			}

		}

		/**
		 * @param l
		 * @param package1
		 */
		public void addResponse(final long timestamp, final DataPackage responsePackage) {
			responsePackage.setTimestamp(timestamp);
			// System.out.println("AddResponse: " + System.nanoTime() + " - " + responsePackage.readResponseSequence());
			responseMap.put(System.nanoTime(), responsePackage);
		}
	}

	/**
	 * @param immediate2
	 */
	private void setResponseTimeout(final long timeout) {
		if (responseThread == null) {
			startResponseThread();
		}
		responseThread.addResponse(System.currentTimeMillis() + timeout, pack);
	}

	public static synchronized void stopResponseThread() {
		if (responseThread != null) {
			responseThread.stop();
			responseThread = null;
		}
	}

	private static synchronized void startResponseThread() {
		if (responseThread == null) {
			responseThread = new ResponseThread();
			new Thread(responseThread).start();
		}
	}

	private int getRandom(final int from, final int to) {
		return from + rand.nextInt(to - from);
	}

}
