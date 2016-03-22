/**
 *
 */
package hu.herba.util.codie;

import java.io.IOException;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author csorbazoli
 *
 */
public class CodieControllerServer {
	private static final Logger LOGGER = LogManager.getLogger(CodieControllerServer.class);

	private class CodieCommandHandler implements HTTPServer.ContextHandler {

		@Override
		public int serve(final Request req, final Response resp) throws IOException {
			LOGGER.info("Serve request: " + req);
			return 0;
		}

	}

	public static void main(final String[] args) {
		new CodieControllerServer().start(8080);
	}

	private void start(final int port) {
		HTTPServer server = new HTTPServer(8080);
		try {
			VirtualHost virtualHost = new VirtualHost("CodieController");
			virtualHost.addContext("/", new CodieCommandHandler(), "GET");
			server.addVirtualHost(virtualHost);
			server.start();
		} catch (IOException e) {
			LOGGER.error("Failed to start Http Server: " + e.getMessage(), e);
		}
	}
}
