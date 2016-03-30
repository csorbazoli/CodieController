/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import hu.herba.util.codie.model.AbstractCodieSensor;
import hu.herba.util.codie.model.CodieRole;

/**
 * MCU sensor commands that retrieves a value from Codie and sets corresponding sensor value that will be reported for Scratch.
 *
 * @author csorbazoli
 */
public abstract class MCUSensor extends AbstractCodieSensor {

	@Override
	public CodieRole getDestination() {
		return CodieRole.MCU;
	}

	@Override
	public CodieRole getSender() {
		return CodieRole.APP;
	}

}
