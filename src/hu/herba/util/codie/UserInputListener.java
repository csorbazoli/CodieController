/**
 *
 */
package hu.herba.util.codie;

/**
 * @author Zolt�n
 *
 */
public interface UserInputListener {

	/**
	 * @param line
	 *            String typed by the user
	 * @return true if line was processed by the listener
	 */
	boolean handle(String line);

}
