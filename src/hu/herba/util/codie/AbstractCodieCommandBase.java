/**
 *
 */
package hu.herba.util.codie;

import java.util.Arrays;

/**
 * @author Zoltán
 *
 */
public abstract class AbstractCodieCommandBase implements CodieCommandBase, Comparable<CodieCommandBase> {

	public abstract byte getInfoByte(boolean highPrio);

	// basic methods will be implemented here
	private int seq = 0;

	protected int getNextSequenceNumber() {
		int ret = ++seq;
		if (ret > 0xFFFF) {
			// restart sequence
			ret = seq = 1;
		}
		return ret;
	}

	/**
	 * @param from
	 * @param to
	 * @param highPrio
	 * @return
	 */
	protected byte getInfoByte(final int from, final int to, final boolean highPrio) {
		return (byte) (from | to | (highPrio ? HIGH : NORMAL));
	}

	public byte[] createDataPackage(final boolean highPrio, final byte[] data) {
		byte[] ret = new byte[20];
		int len = 0;
		// INFO: ROUTE+PRIO (8 bits)
		byte info = getInfoByte(highPrio);
		ret[len++] = info;
		// SEQ (16 bits)
		int seq = getNextSequenceNumber();
		ret[len++] = (byte) (seq & 0x00FF);
		ret[len++] = (byte) (seq & 0x0FF00);
		// CMD (16 bits)
		int cmdId = getCommandId();
		ret[len++] = (byte) (cmdId & 0x00FF);
		ret[len++] = (byte) (cmdId & 0x0FF00);
		// ARGLEN (16 bits)
		int arglen = data == null ? 0 : data.length;
		ret[len++] = (byte) (arglen & 0x00FF);
		ret[len++] = (byte) (arglen & 0x0FF00);
		// ARGDAT
		for (int i = 0; i < arglen; i++) {
			ret[len++] = data[i];
		}

		// return only the relevant part
		return Arrays.copyOf(ret, len);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final CodieCommandBase o) {
		int ret;
		if (o == null) {
			ret = 1;
		} else {
			ret = getName().compareTo(o.getName());
			if (ret == 0) {
				ret = Integer.compare(getCommandId(), o.getCommandId());
			}
		}
		return ret;
	}

	@Override
	public boolean equals(final Object obj) {
		boolean ret;
		if (obj == null || !this.getClass().equals(obj.getClass())) {
			ret = false;
		} else {
			ret = getCommandId() == ((CodieCommandBase) obj).getCommandId();
		}
		return ret;
	}

	@Override
	public int hashCode() {
		int prime = 37;
		int hash = getCommandId();
		hash = hash * prime;
		return hash;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + getCommandId() + "]-" + getName();
	}

}
