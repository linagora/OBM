package org.obm.push.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.google.common.io.ByteStreams;

public class SizeLimitingInputStreamTest {

	@Test
	public void testNotTooBig() throws IOException {
		
		byte[] testArray = new byte[1024];
		byte[] result = ByteStreams.toByteArray(
				new SizeLimitingInputStream(new ByteArrayInputStream(testArray), 2048));
		Assertions.assertThat(result).isEqualTo(testArray);
	}

	@Test(expected=SizeLimitExceededException.class)
	public void testTooBig() throws IOException {
		
		byte[] testArray = new byte[4096];
		ByteStreams.toByteArray(
				new SizeLimitingInputStream(new ByteArrayInputStream(testArray), 2048));
		
	}

	@Test(expected=IOException.class)
	public void testMarkNotSupported() throws IOException {
		
		byte[] testArray = new byte[1024];
		SizeLimitingInputStream sizeLimitingInputStream = 
				new SizeLimitingInputStream(new ByteArrayInputStream(testArray), 2048);
		sizeLimitingInputStream.mark(100);
		sizeLimitingInputStream.read(new byte[50]);
		sizeLimitingInputStream.reset();
	}
	
}
