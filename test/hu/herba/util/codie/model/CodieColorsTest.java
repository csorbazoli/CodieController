/**
 *
 */
package hu.herba.util.codie.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Zoltán
 *
 */
public class CodieColorsTest {

	/**
	 * Test method for {@link hu.herba.util.codie.model.CodieColors#getHue()}.
	 */
	@Test
	public void testGetHueRed() {
		// given
		// when
		int actual = CodieColors.red.getHue();
		// then
		Assert.assertEquals(0, actual);
	}

	@Test
	public void testGetSaturationRed() {
		// given
		// when
		int actual = CodieColors.red.getSaturation();
		// then
		Assert.assertEquals(100, actual);
	}

	@Test
	public void testGetValueRed() {
		// given
		// when
		int actual = CodieColors.red.getValue();
		// then
		Assert.assertEquals(100, actual);
	}

	@Test
	public void testGetHueWhite() {
		// given
		// when
		int actual = CodieColors.white.getHue();
		// then
		Assert.assertEquals(0, actual);
	}

	@Test
	public void testGetSaturationWhite() {
		// given
		// when
		int actual = CodieColors.white.getSaturation();
		// then
		Assert.assertEquals(0, actual);
	}

	@Test
	public void testGetValueWhite() {
		// given
		// when
		int actual = CodieColors.white.getValue();
		// then
		Assert.assertEquals(100, actual);
	}

	@Test
	public void testGetHueYellow() {
		// given
		// when
		int actual = CodieColors.yellow.getHue();
		// then
		Assert.assertEquals(16, actual);
	}

	@Test
	public void testGetSaturationYellow() {
		// given
		// when
		int actual = CodieColors.yellow.getSaturation();
		// then
		Assert.assertEquals(100, actual);
	}

	@Test
	public void testGetValueYellow() {
		// given
		// when
		int actual = CodieColors.yellow.getValue();
		// then
		Assert.assertEquals(100, actual);
	}

}
