/**
 *
 */
package hu.herba.util.codie.model;

/**
 * @author csorbazoli
 *
 */
public abstract class AbstractCodieCommand extends AbstractCodieCommandBase implements CodieCommand {

	@Override
	public boolean isWait() {
		// default is non-waiting
		return false;
	}

}
