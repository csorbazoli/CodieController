/**
 *
 */
package hu.herba.util.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.obex.HeaderSet;
import javax.obex.Operation;

/**
 * @author zcsorba
 *
 */
public class CodieMockOperation implements Operation {

	/**
	 * @param headerSet
	 */
	public CodieMockOperation(HeaderSet headerSet) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getType()
	 */
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getEncoding()
	 */
	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.ContentConnection#getLength()
	 */
	@Override
	public long getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.InputConnection#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.InputConnection#openDataInputStream()
	 */
	@Override
	public DataInputStream openDataInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.Connection#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.OutputConnection#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.io.OutputConnection#openDataOutputStream()
	 */
	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#abort()
	 */
	@Override
	public void abort() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#getReceivedHeaders()
	 */
	@Override
	public HeaderSet getReceivedHeaders() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#sendHeaders(javax.obex.HeaderSet)
	 */
	@Override
	public void sendHeaders(HeaderSet headerset) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.obex.Operation#getResponseCode()
	 */
	@Override
	public int getResponseCode() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
