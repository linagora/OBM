package org.obm.push.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class DOMUtilsTest {
	
	@Test
	public void testEncodingXmlWithAccents() throws TransformerException, UnsupportedEncodingException{
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		String expectedString = "éàâ";
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		root.setTextContent(expectedString);
		DOMUtils.serialise(reply, out);
		
		Assert.assertThat(new String(out.toByteArray(), "UTF-8"), 
				StringContains.containsString(expectedString));
	}
}

