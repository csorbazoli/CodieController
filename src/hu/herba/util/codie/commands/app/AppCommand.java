/**
 *
 */
package hu.herba.util.codie.commands.app;

import hu.herba.util.codie.model.AbstractCodieCommand;

/**
 * App commands can be executed by the client application.
 *
 * @author csorbazoli
 */
public abstract class AppCommand extends AbstractCodieCommand {
	@Override
	public byte getInfoByte(final boolean highPrio) {
		// let's assume it comes from MCU
		return getInfoByte(FROM_MCU, TO_APP, highPrio);
	}

}
