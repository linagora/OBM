package org.obm.push.tnefconverter.test;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.tnefconverter.EmailConverter;
import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.utils.FileUtils;

public class EmailConverterTest {

	@Ignore("upgrade MIME4J from 0.5 TO 0.6.1")
	@Test
	public void testConvert() throws TNEFConverterException, IOException {
		InputStream eml = loadDataFile("fgggh.eml");
		assertNotNull(eml);
		String s = FileUtils.streamString(eml, true);
		InputStream in = new EmailConverter().convert(new ByteArrayInputStream(
				s.getBytes()));
		assertNotNull(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(in, out, true);
	}

	@Test
	public void testConvert1() throws TNEFConverterException, IOException {
		InputStream eml = loadDataFile("cancelOrrurEvent.eml");
		assertNotNull(eml);
		String s = FileUtils.streamString(eml, true);
		InputStream in = new EmailConverter().convert(new ByteArrayInputStream(
				s.getBytes()));
		assertNotNull(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(in, out, true);
	}
	
	@Test
	public void testConvert2() throws TNEFConverterException, IOException {
		InputStream eml = loadDataFile("accptInv.eml");
		assertNotNull(eml);
		String s = FileUtils.streamString(eml, true);
		InputStream in = new EmailConverter().convert(new ByteArrayInputStream(
				s.getBytes()));
		assertNotNull(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(in, out, true);
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/eml/" + name);
	}
	
}
