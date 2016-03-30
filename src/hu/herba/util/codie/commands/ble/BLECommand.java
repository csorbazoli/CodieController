/**
 *
 */
package hu.herba.util.codie.commands.ble;

import hu.herba.util.codie.model.AbstractCodieCommand;
import hu.herba.util.codie.model.CodieRole;

/**
 * BLE commands can be executed only on the BLE module.
 *
 * @author csorbazoli
 */
public abstract class BLECommand extends AbstractCodieCommand {
	@Override
	public CodieRole getSender() {
		return CodieRole.APP;
	}

	@Override
	public CodieRole getDestination() {
		return CodieRole.BLE;
	}
}
