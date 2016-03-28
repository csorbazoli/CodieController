/**
 *
 */
package hu.herba.util.bluetooth;

/**
 * @author csorbazoli
 *
 */
public class CodieConnectionException extends Exception {

	private static final long serialVersionUID = 8898735880528223699L;

	/**
	 * @param message
	 */
	public CodieConnectionException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CodieConnectionException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CodieConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CodieConnectionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
