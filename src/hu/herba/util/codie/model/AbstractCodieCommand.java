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
		int idx2 = isWait() ? idx + 1 : idx;
		if (commandParts.length > idx2) {
			String param = commandParts[idx2];
			try {
				ret = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				getLogger().warn("Invalid number parameter: " + param);
			}
		}
		return ret;
	}

	/**
	 * @param commandParts
	 * @param i
	 * @return
	 */
	protected double getDoubleParam(final String[] commandParts, final int idx, final double defVal) {
		double ret = defVal;
		int idx2 = isWait() ? idx + 1 : idx;
		if (commandParts.length > idx2) {
			String param = commandParts[idx2];
			try {
				ret = Double.parseDouble(param);
			} catch (NumberFormatException e) {
				getLogger().warn("Invalid number parameter: " + param);
			}
		}
		return ret;
	}

	protected String getStringParam(final String[] commandParts, final int idx, final String defVal) {
		String ret = defVal;
		int idx2 = isWait() ? idx + 1 : idx;
		if (commandParts.length > idx2) {
			ret = commandParts[idx2];
		}
		return ret;
	}

	@Override
	public boolean isWait() {
		// default is non-waiting
		return false;
	}

}
