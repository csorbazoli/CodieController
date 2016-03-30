/**
 *
 */
package hu.herba.util.codie.commands.app;

import hu.herba.util.codie.model.AbstractCodieCommand;
import hu.herba.util.codie.model.CodieRole;

/**
 * App commands can be executed by the client application.
 *
 * @author csorbazoli
 */
public abstract class AppCommand extends AbstractCodieCommand {

	@Override
	public CodieRole getSender() {
		return CodieRole.MCU;
	}

	@Override
	public CodieRole getDestination() {
		return CodieRole.APP;
	}

}
