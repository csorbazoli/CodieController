package hu.herba.util.codie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.bluetooth.CodieBluetoothConnectionFactory;
import hu.herba.util.bluetooth.CodieConnectionException;

/**
 * Servlet implementation class CodieControllerDispatcher
 */
@WebServlet(description = "Codie command dispatcher", urlPatterns = { "/" })
public class CodieControllerDispatcher extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerDispatcher.class);
	private static final long RETRY_TIMEOUT = 1000; // retry timeout in milliseconds for a failed connection

	private Object conn;
	private long lastConnectionFailure;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CodieControllerDispatcher() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		LOGGER.info("CodieController initialized");
		initConnection();
	}

	private void initConnection() {
		// initialize connection to Codie
		if (conn == null && retryTimeout()) {
			try {
				conn = CodieBluetoothConnectionFactory.connect();
				CodieSensorPollService.getInstance().resetTimers(conn);
			} catch (CodieConnectionException e) {
				LOGGER.warn("Failed to connect to Codie: " + e.getMessage());
			} finally {
				if (conn == null) {
					lastConnectionFailure = System.currentTimeMillis();
				}
			}
		}
	}

	private boolean retryTimeout() {
		return System.currentTimeMillis() - lastConnectionFailure > RETRY_TIMEOUT;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		// LOGGER.info(request.getContextPath() + ", request = " + servletPath);
		PrintWriter out = response.getWriter();
		handleRequest(request.getServletPath(), out);
	}

	public void handleRequest(final String servletPath, final Writer out) throws IOException {
		String uri = servletPath.startsWith("/") ? servletPath.substring(1) : servletPath;
		LOGGER.debug("URI:" + uri);
		switch (uri) {
		case "crossdomain.xml":
			sendCrossDomainXml(out);
			break;
		case "reset_all":
			resetDevice();
			break;
		case "poll":
			initConnection();
			pollRequest(out);
			break;
		default:
			if (uri.indexOf('.') != -1) {
				sendFile(out, uri);
			} else {
				CodieCommandProcessor.getInstance().handleCommand(out, uri);
			}
		}
	}

	private void resetDevice() {
		LOGGER.info("TODO need to reset connection to Codie");
		CodieSensorPollService.getInstance().cancelTimers();
	}

	@Override
	protected void finalize() throws Throwable {
		LOGGER.info("Shutdown codie controller...");
		CodieSensorPollService.getInstance().cancelTimers();
		super.finalize();
	}

	private void pollRequest(final Writer out) throws IOException {
		out.append("CodieController 1.0\n");
		if (conn == null) {
			out.append("_problem NOT CONNECTED");
		} else {
			CodieCommandProcessor.getInstance().providePollInfo(out);
		}
	}

	private void sendCrossDomainXml(final Writer out) throws IOException {
		sendFile(out, "crossdomain.xml");
		out.append((char) 0x00);
	}

	private void sendFile(final Writer out, final String fileName) throws IOException {
		BufferedReader reader = null;
		try {
			LOGGER.debug("Open file: " + fileName);
			InputStream is = this.getClass().getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName);
			if (is == null) {
				out.append("File not found: " + fileName + "!");
				LOGGER.error("File not found: " + fileName + "!");
			} else {
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					out.append(line).append('\n');
				}
			}
		} catch (UnsupportedEncodingException e) {
			out.append("Failure: " + e.getMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
