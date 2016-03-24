/**
 *
 */
package hu.herba.util.codie;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Headers;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

//import net.freeutils.httpserver.HTTPServer;
//import net.freeutils.httpserver.HTTPServer.Request;
//import net.freeutils.httpserver.HTTPServer.Response;
//import net.freeutils.httpserver.HTTPServer.VirtualHost;

/**
 * @author csorbazoli
 *
 */
public class CodieControllerJLHTTPServer {
	private static final int LISTEN_PORT = 8080;
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerJLHTTPServer.class);
	private CodieControllerDispatcher dispatcher;

	private class CodieCommandHandler implements ContextHandler {

		@Override
		public int serve(final Request req, final Response resp) throws IOException {
			// LOGGER.info("Serve request: " + req);
			switch (req.getMethod()) {
			case "GET":
				String servletPath = req.getPath();
				LOGGER.info("GET " + servletPath);
				Headers h = req.getHeaders();
				// setting the content type is not required!
				// h.add("Content-Type", "text/plain");
				// h.add("Server", "Apache-Coyote/1.1");
				// req.sendResponseHeaders(200, 138);
				PrintWriter out = new PrintWriter(resp.getOutputStream());
				try {
					dispatcher.handleRequest(servletPath, out);
				} finally {
					out.flush();
					out.close();
					LOGGER.info("DONE " + servletPath);
				}
				break;
			default:
				LOGGER.warn("Unhandled request method: " + req.getMethod() + " on " + req.getPath());
				resp.close();
			}
			return 0;
		}

	}

	public static void main(final String[] args) {
		CodieControllerJLHTTPServer codieControllerServer = new CodieControllerJLHTTPServer();
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
		HTTPServer server;
		try {
			LOGGER.info("CodieController server starting...");
			initDispatcher();
			server = new HTTPServer(LISTEN_PORT);
			VirtualHost virtualHost = new VirtualHost("CodieController");
			virtualHost.addContext("/", new CodieCommandHandler());
			server.addVirtualHost(virtualHost);
			server.start();
			LOGGER.info("CodieController server is started and listening on port " + LISTEN_PORT);
		} catch (IOException e) {
			LOGGER.error("Failed to start HTTP server on port " + LISTEN_PORT, e);
		}
	}

	private void initDispatcher() {
		LOGGER.info("Init dispatcher...");
		dispatcher = new CodieControllerDispatcher();
		LOGGER.info("Dispatcher initialized");
	}
}
