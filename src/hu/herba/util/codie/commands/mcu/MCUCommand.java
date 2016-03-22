/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import hu.herba.util.codie.AbstractCodieCommand;

/**
 * MCU commands can be executed only on the MCU.
 *
 * @author csorbazoli
 */
public abstract class MCUCommand extends AbstractCodieCommand {
	@Override
	public byte getInfoByte(final boolean highPrio) {
		return getInfoByte(FROM_APP, TO_MCU, highPrio);
	}

}
