/**
 *
 */
package hu.herba.util.codie.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import hu.herba.util.codie.commands.mcu.DriveSpeedCommand;

/**
 * @author Zoltán
 *
 */
public class DataPackageTest {

	private DataPackage underTest;
	private CodieCommandBase codieCommand;

	@Before
	public void setUp() {
		underTest = new DataPackage();
		codieCommand = new DriveSpeedCommand();
	}

	/**
	 * Test method for {@link hu.herba.util.codie.model.DataPackage#prepareRequest(hu.herba.util.codie.model.CodieCommandBase, int)}.
	 */
	@Test
	public void testPrepareRequest() {
		// given
		DataPackage.resetSequence();
		// when
		underTest.prepareRequest(codieCommand, 2);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals(0x40, actual[0]); // APP -> MCU
		Assert.assertEquals(0x01, actual[1]); // seq/1
		Assert.assertEquals(0x0, actual[2]); // seq/2
		Assert.assertEquals(0x60, actual[3]); // cmd/1
		Assert.assertEquals(0x10, actual[4]); // cmd/2
		Assert.assertEquals(0x02, actual[5]); // arglen/1
		Assert.assertEquals(0x0, actual[6]); // arglen/2
	}

	/**
	 * Test method for {@link hu.herba.util.codie.model.DataPackage#prepareResponse(byte[], int)}.
	 */
	@Test
	public void testPrepareResponseByteArrayInt() {
		// given
		byte[] request = new byte[] { 0x40, // APP -> MCU
				0x12, 0x34, // seq = 1
				0x60, 0x10, // cmdId = 1060
				0x02, 0x00, // arglen = 2
				0x01, // first arg
				0x02 // second arg
		};
		DataPackage.resetSequence();
		// when
		underTest.prepareResponse(request, 1);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals(0x10, actual[0]); // MCU -> APP
		Assert.assertEquals(0x01, actual[1]); // seq/1
		Assert.assertEquals(0x00, actual[2]); // seq/2
		Assert.assertEquals(0x60, actual[3]); // cmd/1
		Assert.assertEquals((byte) 0x90, actual[4]); // cmd/2
		Assert.assertEquals(0x03, actual[5]); // arglen/1 = 1+2
		Assert.assertEquals(0x0, actual[6]); // arglen/2
		Assert.assertEquals(0x12, actual[7]); // reqseq/1
		Assert.assertEquals(0x34, actual[8]); // reqseq/2
	}

	/**
	 * Test method for {@link hu.herba.util.codie.model.DataPackage#prepareResponse(hu.herba.util.codie.model.CodieCommandBase, int, int)}.
	 */
	@Test
	public void testPrepareResponseCodieCommandBaseIntInt() {
		// given
		DataPackage.resetSequence();
		// when
		underTest.prepareResponse(codieCommand, 0x1234, 3);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals(0x10, actual[0]); // MCU -> APP
		Assert.assertEquals(0x01, actual[1]); // seq/1
		Assert.assertEquals(0x00, actual[2]); // seq/2
		Assert.assertEquals(0x60, actual[3]); // cmd/1
		Assert.assertEquals((byte) 0x90, actual[4]); // cmd/2
		Assert.assertEquals(0x03, actual[5]); // arglen/1
		Assert.assertEquals(0x0, actual[6]); // arglen/2
		Assert.assertEquals(0x34, actual[7]); // reqseq/1
		Assert.assertEquals(0x12, actual[8]); // reqseq/2
	}

	@Test
	public void testReadResponseSequence() {
		// given
		DataPackage.resetSequence();
		underTest.prepareResponse(codieCommand, 0x1234, 3);
		// when
		int actual = underTest.readResponseSequence();
		// then
		Assert.assertEquals(0x1234, actual);
	}

	/**
	 * Test method for {@link hu.herba.util.codie.model.DataPackage#addArgument(int, hu.herba.util.codie.model.ArgumentType)}.
	 */
	@Test
	public void testAddRequestArgumentU8() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(12, ArgumentType.U8);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals(0x0c, actual[7]); // arg
	}

	@Test
	public void testAddRequestArgumentU8Big() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(162, ArgumentType.U8);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals((byte) 0xa2, actual[7]); // arg
	}

	@Test
	public void testAddRequestArgumentU16() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(162, ArgumentType.U16); // 0xc2
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals((byte) 0xa2, actual[7]); // arg/1st byte
		Assert.assertEquals((byte) 0x00, actual[8]); // arg/2nd byte
	}

	@Test
	public void testAddRequestArgumentU16Big() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(1620, ArgumentType.U16); // 0x654
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals((byte) 0x54, actual[7]); // arg/1st byte
		Assert.assertEquals((byte) 0x06, actual[8]); // arg/2nd byte
	}

	@Test
	public void testAddRequestArgumentI8() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(12, ArgumentType.I8);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals(0x0c, actual[7]); // arg
	}

	@Test
	public void testAddRequestArgumentI8Neg() {
		// given
		underTest.prepareRequest(codieCommand, 3);
		// when
		underTest.addArgument(-12, ArgumentType.I8);
		// then
		byte[] actual = underTest.getPackage();
		Assert.assertEquals((byte) 0xf4, actual[7]); // arg
	}

	/**
	 * Test method for {@link hu.herba.util.codie.model.DataPackage#readArgument(int, hu.herba.util.codie.model.ArgumentType)}.
	 */
	@Test
	public void testReadArgumentU8() {
		// given
		underTest.prepareResponse(codieCommand, 0x1234, 3);
		underTest.addArgument(134, ArgumentType.U8); // 0x86
		// when
		int actual = underTest.readArgument(0, ArgumentType.U8);
		// then
		Assert.assertEquals(134, actual);
	}

	@Test
	public void testReadArgumentU16() {
		// given
		underTest.prepareResponse(codieCommand, 0x1234, 3);
		underTest.addArgument(1748, ArgumentType.U16); // 0x6D4
		// when
		int actual = underTest.readArgument(0, ArgumentType.U16);
		// then
		Assert.assertEquals(1748, actual);
	}

	@Test
	public void testReadArgumentI8() {
		// given
		underTest.prepareResponse(codieCommand, 0x1234, 3);
		underTest.addArgument(-17, ArgumentType.I8); // 0xef
		// when
		int actual = underTest.readArgument(0, ArgumentType.I8);
		// then
		Assert.assertEquals(-17, actual);
	}

}