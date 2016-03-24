/**
 *
 */
package hu.herba.util.codie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;

/**
 * @author csorbazoli
 *
 */
public class CodieControllerThreadPooledServer implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerThreadPooledServer.class);
	private static final int LISTEN_PORT = 9123;
	private static final int POOL_SIZE = 10;

	protected ServerSocket serverSocket = null;
	protected boolean isStopped = false;
	protected Thread runningThread = null;
	protected final ExecutorService threadPool;

	private CodieControllerDispatcher dispatcher;
	private final int port;

	/**
	 * @param listenPort
	 * @param poolSize TODO
	 */
	public CodieControllerThreadPooledServer(int listenPort, int poolSize) {
		port = listenPort;
		threadPool = Executors.newFixedThreadPool(poolSize);
	}

	private class CodieCommandHandler implements Runnable {

		protected Socket clientSocket = null;

		public CodieCommandHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			String servletPath = "poll";
			try {
				InputStream input = clientSocket.getInputStream();
				OutputStream output = clientSocket.getOutputStream();
				long time = System.currentTimeMillis();
				StringWriter sw = new StringWriter();
				dispatcher.handleRequest(servletPath, sw);
				LOGGER.trace("DONE " + servletPath);
				writeOutput(output, sw.getBuffer().toString());
				input.close();
				System.out.println("Request processed: " + time);
			} catch (IOException e) {
				LOGGER.error("Failed to process request: " + e.getMessage(), e);
			}
			LOGGER.debug(servletPath + ": " + (System.currentTimeMillis() - start) + "ms");
		}

		/**
		 * @param bytes
		 * @throws IOException
		 */
		private void writeOutput(OutputStream output, final String response) throws IOException {
			output.write("HTTP/1.1 200 OK\n\n".getBytes());
			try {
				output.write(response.getBytes());
				output.flush();
			} catch (IOException e) {
				LOGGER.error("Failed to write output: " + response, e);
			} finally {
				try {
					output.close();
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
		CodieControllerThreadPooledServer codieControllerServer = new CodieControllerThreadPooledServer(LISTEN_PORT, POOL_SIZE);
		new Thread(codieControllerServer).start();
		Scanner scan = new Scanner(System.in);
		while (scan.hasNextLine()) {
			String readLine = scan.nextLine();
			if ("q".equalsIgnoreCase(readLine) || "quit".equalsIgnoreCase(readLine) || "exit".equalsIgnoreCase(readLine)) {
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

	@Override
	public void run() {
		synchronized (this) {
			runningThread = Thread.currentThread();
		}
		LOGGER.info("CodieController server starting...");
		initDispatcher();
		openServerSocket();
		LOGGER.info("CodieController server is started and listening on port " + getPort());
		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					System.out.println("Server Stopped.");
					break;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			threadPool.execute(new CodieCommandHandler(clientSocket));
		}
		threadPool.shutdown();
		System.out.println("Server Stopped.");

		// server.createContext("/", new CodieCommandHandler());
	}

	private synchronized boolean isStopped() {
		return isStopped;
	}

	private void openServerSocket() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port " + port, e);
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
