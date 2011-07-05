package org.obm.push.tnefconverter.test;

import java.io.InputStream;

import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.tnefconverter.TNEFUtils;

import junit.framework.TestCase;

/**
 * 
 * @author adrienp
 * 
 */
public class TNEFUtilsTest  extends TestCase{

	public void testIsScheduleMeetingRequest() throws TNEFConverterException{
		InputStream in = loadDataFile("recurmontly.eml");
		Boolean ret = TNEFUtils.isScheduleMeetingRequest(in);
		assertTrue(ret);
	}

	public void testContainsTNEFAttchment() throws TNEFConverterException {
		InputStream in = loadDataFile("recurmontly.eml");
		Boolean ret = TNEFUtils.containsTNEFAttchment(in);
		assertTrue(ret);
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/eml/" + name);
	}
}
