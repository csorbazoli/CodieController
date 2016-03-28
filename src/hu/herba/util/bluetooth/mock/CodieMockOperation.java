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

import javax.obex.HeaderSet;
import javax.obex.Operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.model.CodieCommandType;

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
	private OutputStream outputStream;
	private DataOutputStream dataOutputStream;
	private HeaderSet headerSetReceived;
	private final int responseCode = 0;
	private boolean aborted;

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
		processOperation();
	}

	@Override
	public int getResponseCode() throws IOException {
		return responseCode;
	}

	/**
	 * This is the actual method that is doing the requested operation.
	 */
	private void processOperation() throws IOException {
		CodieCommandType operation;
		try {
			operation = checkHeaderContent();
		} catch (IllegalArgumentException e) {
			throw new IOException(e);
		}
		switch (operation) {
		case DriveSpeed: // nothing to do
			break;
		default:
			LOGGER.warn("Operation type " + operation + " is not implemented in " + this.getClass().getSimpleName());
		}
	}

	/**
	 * @return OperationType specified in the header data
	 * @throws IllegalArgumentException
	 *             if operation type is unknown, or header content is invalid (e.g. destination/receiver is invalid)
	 */
	private CodieCommandType checkHeaderContent() throws IllegalArgumentException {
		CodieCommandType ret = CodieCommandType.DriveSpeed;
		// TODO check header content
		// determine operation type
		return ret;
	}

}
