/**
 *
 */
package hu.herba.util.codie.model;

/**
 * @author csorbazoli
 *
 */
public abstract class AbstractCodieCommand extends AbstractCodieCommandBase implements CodieCommand {

	/**
	 * @param commandParts
	 * @param i
	 * @return
	 */
	protected int getIntParam(final String[] commandParts, final int idx, final int defVal) {
		int ret = defVal;
		if (commandParts.length > idx) {
			String param = commandParts[idx];
			try {
				ret = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				getLogger().warn("Invalid number parameter: " + param);
			}
		}
		return ret;
	}

	protected String getStringParam(final String[] commandParts, final int idx, final String defVal) {
		String ret = defVal;
		if (commandParts.length > idx) {
			ret = commandParts[idx];
		}
		return ret;
	}

	@Override
	public boolean isWait() {
		// default is non-waiting
		return false;
	}

}
