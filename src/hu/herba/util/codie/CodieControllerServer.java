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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class CodieControllerServer implements Runnable, Executor {
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerServer.class);

	private static final int LISTEN_PORT = 9123;
	private static final int POOL_SIZE = 10;

	private CodieControllerDispatcher dispatcher;
	protected final ExecutorService threadPool;

	private final int port;
	private final int poolSize;

	/**
	 * @param listenPort
	 * @param maxThreads
	 */
	public CodieControllerServer(final int listenPort, final int maxThreads) {
		port = listenPort;
		poolSize = maxThreads;
		threadPool = Executors.newFixedThreadPool(poolSize);
	}

	@Override
	public void execute(final Runnable command) {
		threadPool.execute(command);
	}

	private class CodieCommandHandler implements HttpHandler {

		@Override
		public void handle(final HttpExchange t) throws IOException {
			String servletPath = t.getRequestURI().toString();
			long start = System.currentTimeMillis();
			readInput(t);
			switch (t.getRequestMethod()) {
			case "GET":
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
			LOGGER.trace(servletPath + ": " + (System.currentTimeMillis() - start) + "ms");
		}

		/**
		 * @param bytes
		 * @throws IOException
		 */
		private void writeOutput(final HttpExchange t, final String response) throws IOException {
			t.sendResponseHeaders(200, 0); // response.length());
			OutputStream os = t.getResponseBody();
			try {
				os.write(response.getBytes());
				os.flush();
			} catch (IOException e) {
				LOGGER.error("Failed to write output: " + response, e);
			} finally {
				try {
					os.close();
					t.getRequestBody().close();
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
		CodieControllerServer codieControllerServer = new CodieControllerServer(LISTEN_PORT, POOL_SIZE);
		new Thread(codieControllerServer).start();
		UserInputProcessor.getInstance().scan();
	}

	@Override
	public void run() {
		HttpServer server;
		try {
			LOGGER.info("CodieController server starting...");
			initDispatcher();
			server = HttpServer.create(new InetSocketAddress(getPort()), 10);
			server.createContext("/", new CodieCommandHandler());
			server.setExecutor(this);
			server.start();
			LOGGER.info("CodieController server is started and listening on port " + getPort());
		} catch (IOException e) {
			LOGGER.error("Failed to start HTTP server on port " + getPort(), e);
		}
	}

	public int getPort() {
		return port;
	}

	private void initDispatcher() {
		LOGGER.info("Init dispatcher...");
		dispatcher = new CodieControllerDispatcher();
		LOGGER.info("Dispatcher initialized");
	}

}
