/**
 *
 */
package hu.herba.util.codie.model;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.CodieCommandProcessor;

/**
 * @author csorbazoli
 */
public interface CodieCommand extends CodieCommandBase {
	void process(CodieCommandProcessor codieCommandProcessor, String[] commandParts) throws CodieCommandException;

	/**
	 * @return true if Command is 'wait' type, which means it waits until the command is processed fully by Codie robot.
	 *         E.g. "drive 20 cm" has to wait until Codie really takes the 20 cm distance
	 */
	boolean isWait();

}
