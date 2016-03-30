/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import hu.herba.util.codie.model.AbstractCodieCommand;
import hu.herba.util.codie.model.CodieRole;

/**
 * MCU commands can be executed only on the MCU.
 *
 * @author csorbazoli
 */
public abstract class MCUCommand extends AbstractCodieCommand {
	@Override
	public CodieRole getSender() {
		return CodieRole.APP;
	}

	@Override
	public CodieRole getDestination() {
		return CodieRole.MCU;
	}

}
