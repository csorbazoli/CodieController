/**
 *
 */
package hu.herba.util.codie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * @author csorbazoli
 *
 */
public class CodieControllerServer {
	private static final int LISTEN_PORT = 8080;
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerServer.class);
	private CodieControllerDispatcher dispatcher;
	private int port = LISTEN_PORT;

	private class CodieCommandHandler implements HttpHandler {

		@Override
		public void handle(final HttpExchange t) throws IOException {
			// LOGGER.info("Serve request: " + httpExchange);
			readInput(t);
			switch (t.getRequestMethod()) {
			case "GET":
				String servletPath = t.getRequestURI().toString();
				LOGGER.trace("GET " + servletPath);
				Headers h = t.getResponseHeaders();
				// setting the content type is not required!
				// h.add("Content-Type", "text/plain");
				h.add("Server", "Apache-Coyote/1.1");
				StringWriter sw = new StringWriter();
				dispatcher.handleRequest(servletPath, sw);
				LOGGER.trace("DONE " + servletPath);
				writeOutput(t, sw.getBuffer().toString());
				break;
			default:
				LOGGER.warn("Unhandled request method: " + t.getRequestMethod() + " using " + t.getProtocol() + " on "
						+ t.getRequestURI());
				t.getResponseBody().close();
			}
		}

		/**
		 * @param bytes
		 * @throws IOException
		 */
		private void writeOutput(final HttpExchange t, final String response) throws IOException {
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			try {
				os.write(response.getBytes());
			} catch (IOException e) {
				LOGGER.error("Failed to write output: " + response, e);
			} finally {
				try {
					os.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close output: " + e.getMessage(), e);
				}
			}
		}

		/**
		 * @param httpExchange
		 */
		private void readInput(final HttpExchange t) {
			BufferedReader is = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			try {
				if (is.ready()) {
					LOGGER.info("Reading input stream from " + t.getRequestMethod() + " - " + t.getRequestURI());
					String line;
					while ((line = is.readLine()) != null) {
						LOGGER.info(line);
					}
				}
			} catch (IOException e) {
				LOGGER.warn("Failed to read inputstream from " + t.getRequestMethod() + " - " + t.getRequestURI());
			}
		}

	}

	public static void main(final String[] args) {
		CodieControllerServer codieControllerServer = new CodieControllerServer();
		codieControllerServer.start(8080);
		Scanner scan = new Scanner(System.in);
		while (scan.hasNextLine()) {
			String readLine = scan.nextLine();
			if ("q".equalsIgnoreCase(readLine) || "quit".equalsIgnoreCase(readLine)
					|| "exit".equalsIgnoreCase(readLine)) {
				scan.close();
				codieControllerServer.stop();
			} else {
				printUsage();
			}
		}
	}

	private static void printUsage() {
		System.out.println();
		System.out.println("Type quit to stop the server");
	}

	/**
	 *
	 */
	private void stop() {
		LOGGER.info("CodieController server is stopping...");
		System.exit(0);
	}

	private void start(final int port) {
		HttpServer server;
		try {
			LOGGER.info("CodieController server starting...");
			initDispatcher();
			server = HttpServer.create(new InetSocketAddress(getPort()), 0);
			server.createContext("/", new CodieCommandHandler());
			server.setExecutor(null);
			// VirtualHost virtualHost = new VirtualHost("CodieController");
			// virtualHost.addContext("/", new CodieCommandHandler(), "GET");
			// server.addVirtualHost(virtualHost);
			server.start();
			LOGGER.info("CodieController server is started and listening on port " + getPort());
		} catch (IOException e) {
			LOGGER.error("Failed to start HTTP server on port " + getPort(), e);
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	private void initDispatcher() {
		LOGGER.info("Init dispatcher...");
		dispatcher = new CodieControllerDispatcher();
		LOGGER.info("Dispatcher initialized");
	}
}
