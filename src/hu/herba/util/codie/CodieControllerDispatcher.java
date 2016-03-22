package hu.herba.util.codie;

import hu.herba.util.bluetooth.CodieBluetoothConnectionFactory;
import hu.herba.util.bluetooth.CodieConnectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String servletPath = request.getServletPath().substring(1);
		// LOGGER.info(request.getContextPath() + ", request = " + servletPath);
		PrintWriter out = response.getWriter();
		switch (servletPath) {
		case "crossdomain.xml":
			sendCrossDomainXml(response, out);
			break;
		case "reset_all":
			resetDevice();
			break;
		case "poll":
			initConnection();
			pollRequest(out);
			break;
		default:
			if (servletPath.indexOf('.') != -1) {
				sendFile(response, servletPath);
			} else {
				CodieCommandProcessor.getInstance().handleCommand(out, servletPath);
			}
		}
	}

	private void resetDevice() {
		LOGGER.info("TODO need to reset connection to Codie");
	}

	private void pollRequest(final PrintWriter out) {
		out.append("CodieController 1.0\n");
		// out.append("_problem NOT CONNECTED");
	}

	private void sendCrossDomainXml(final HttpServletResponse response, final PrintWriter out) throws IOException {
		sendFile(response, "crossdomain.xml");
		out.append((char) 0x00);
	}

	private void sendFile(final HttpServletResponse response, final String fileName) throws IOException {
		PrintWriter out = response.getWriter();
		BufferedReader reader = null;
		try {
			LOGGER.info("Open file: " + fileName);
			InputStream is = this.getClass().getResourceAsStream(fileName);
			if (is == null) {
				out.append("File not found: " + fileName + "!");
			} else {
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					out.println(line);
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

	private void append(final HttpServletResponse response, final String line) throws IOException {
		response.getWriter().append(line);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
