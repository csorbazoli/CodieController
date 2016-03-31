/**
 *
 */
package hu.herba.util.codie.commands.mcu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hu.herba.util.codie.CodieCommandException;
import hu.herba.util.codie.model.ArgumentType;
import hu.herba.util.codie.model.CodieCommandType;
import hu.herba.util.codie.model.DataPackage;

/**
 * ID: 0x1064<br/>
 * ARG: duration[u16](ms)<br/>
 * ReARG: nSuccessful[u8]<br/>
 *
 * Play a beep on the speaker. The frequency is fixed, you can only specify the duration in milliseconds.
 *
 * Replies: nSuccessful: 0 means command has been successfully executed, any other value means error.
 *
 * Busy call behavior: execution ends immediately, the command is nacked, and then the new command starts executing.
 *
 * @author csorbazoli
 */
public class SpeakBeepCommand extends MCUCommand {
	private static final Logger LOGGER = LogManager.getLogger(SpeakBeepCommand.class);
	private static final int MAX_DURATION = 10000; // 10 seconds

	@Override
	public int processRequest(final String[] commandParts) throws CodieCommandException {
		LOGGER.info("Processing " + getClass().getSimpleName() + "...");
		int duration = (int) (getDoubleParam(commandParts, 1, 1) * 1000); // sec -> millis
		int ret = request.prepareRequest(this, 2);
		request.addArgument(Math.min(duration, MAX_DURATION), ArgumentType.U16);
		sendCommand();
		return ret;
	}

	@Override
	public void processResponse(final DataPackage response) throws CodieCommandException {
		int nSuccessful = response.readArgument(0, ArgumentType.U8);
		if (nSuccessful != 0) {
			getSensorValueStore().setLastResult(false);
		}
	}

	@Override
	public CodieCommandType getCommandType() {
		return CodieCommandType.SpeakBeep;
	}

	@Override
	public boolean isWait() {
		return true;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
