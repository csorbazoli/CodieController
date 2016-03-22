/**
 *
 */
package hu.herba.util.codie.commands.ble;

import hu.herba.util.codie.AbstractCodieCommand;

/**
 * BLE commands can be executed only on the BLE module.
 *
 * @author csorbazoli
 */
public abstract class BLECommand extends AbstractCodieCommand {
	@Override
	public byte getInfoByte(final boolean highPrio) {
		return getInfoByte(FROM_APP, TO_BLE, highPrio);
	}

}
