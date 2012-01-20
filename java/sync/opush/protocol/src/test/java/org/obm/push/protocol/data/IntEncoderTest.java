package org.obm.push.protocol.data;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class IntEncoderTest {

	private IntEncoder intEncoder;

	@Before
	public void setUp() {
		intEncoder = new IntEncoder();
	}

	private void testInt(int value, byte[] expected) {
		byte[] byteArray = intEncoder.toByteArray(value);
		Assertions.assertThat(byteArray).isEqualTo(expected);
	}

	@Test
	public void testZero() {
		testInt(0, new byte[] {0, 0, 0, 0});
	}

	@Test
	public void testOne() {
		testInt(1, new byte[] {1, 0, 0, 0});
	}
	
	@Test
	public void test255() {
		testInt(255, new byte[] {-1, 0, 0, 0});
	}
	
	@Test
	public void test256() {
		testInt(256, new byte[] {0, 1, 0, 0});
	}
	
	@Test
	public void testIntMax() {
		testInt(Integer.MAX_VALUE, new byte[] {-1, -1, -1, 127});
	}
	
	@Test
	public void testIntMin() {
		testInt(Integer.MIN_VALUE, new byte[] {0, 0, 0, -128});
	}

}
