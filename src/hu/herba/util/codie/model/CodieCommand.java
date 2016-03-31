/**
 *
 */
package hu.herba.util.codie.model;

import hu.herba.util.codie.CodieCommandException;

/**
 * @author csorbazoli
 */
public interface CodieCommand extends CodieCommandBase {
	/**
	 * Process request received from Scratch interface.
	 * 
	 * @param commandParts
	 * @return TODO
	 *
	 * @throws CodieCommandException
	 */
	int processRequest(String[] commandParts) throws CodieCommandException;

	/**
	 * @return true if Command is 'wait' type, which means it waits until the command is processed fully by Codie robot. E.g. "drive 20 cm" has to wait until
	 *         Codie really takes the 20 cm distance
	 */
	boolean isWait();

}
