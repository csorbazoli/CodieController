/**
 *
 */
package hu.herba.util.codie.sensors.mcu;

import hu.herba.util.codie.model.AbstractCodieSensor;

/**
 * MCU sensor commands that retrieves a value from Codie and sets corresponding sensor value that will be reported for
 * Scratch.
 *
 * @author csorbazoli
 */
public abstract class MCUSensor extends AbstractCodieSensor {
	@Override
	public byte getInfoByte(final boolean highPrio) {
		return getInfoByte(FROM_APP, TO_MCU, highPrio);
	}

}
