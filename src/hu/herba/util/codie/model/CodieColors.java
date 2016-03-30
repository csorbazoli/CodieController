/**
 *
 */
package hu.herba.util.codie.model;

import java.awt.Color;

/**
 * @author Zoltán
 *
 */
public enum CodieColors {
	white(Color.WHITE), //
	green(Color.GREEN), //
	red(Color.RED), //
	blue(Color.BLUE), //
	cyan(Color.CYAN), //
	yellow(Color.YELLOW), //
	orange(Color.ORANGE), //
	;

	private Color color;
	private float[] hsv;

	private CodieColors(final Color color) {
		this.color = color;
		hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
	}

	public Color getColor() {
		return color;
	}

	public int getHue() {
		return (int) (hsv[0] * 100);
	}

	public int getSaturation() {
		return (int) (hsv[1] * 100);
	}

	public int getValue() {
		return (int) (hsv[2] * 100);
	}
}
