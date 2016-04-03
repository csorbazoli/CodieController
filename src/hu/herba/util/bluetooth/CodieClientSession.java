/**
 *
 */
package hu.herba.util.bluetooth;

/**
 * @author Zoltán
 *
 */
public interface CodieClientSession {

	/**
	 * @return true if connection is ready for data transmission
	 */
	boolean isReady();

	/**
	 * @param commandName
	 * @param data
	 * @return
	 */
	int sendCommand(String commandName, byte[] data);

}
