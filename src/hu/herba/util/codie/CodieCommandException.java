/**
 *
 */
package hu.herba.util.codie;

/**
 * @author csorbazoli
 */
public class CodieCommandException extends Exception {

	private static final long serialVersionUID = 6225292376801523327L;

	/**
	 * @param message
	 */
	public CodieCommandException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CodieCommandException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CodieCommandException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CodieCommandException(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
