/**
 *
 */
package hu.herba.util.codie.model;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.CodieCommandProcessor;
import hu.herba.util.codie.SensorValueStore;

/**
 * @author csorbazoli
 *
 */
public abstract class AbstractCodieSensor extends AbstractCodieCommandBase implements CodieSensor {
	@Override
	public void poll(final SensorValueStore sensorValueStore) throws CodieCommandException {
		getLogger().trace("Processing " + getClass().getSimpleName() + "...");
		sendCommand(request.prepareRequest(this, 0));
	}

	/**
	 * @param commandSeq
	 * @throws CodieCommandException
	 */
	protected void sendCommand(final int commandSeq) throws CodieCommandException {
		super.sendCommand();
		CodieCommandProcessor.getInstance().commandStarted(this, null, commandSeq);
	}

}
