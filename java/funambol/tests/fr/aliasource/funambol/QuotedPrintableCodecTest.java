package fr.aliasource.funambol;

import junit.framework.TestCase;

import com.funambol.framework.tools.codec.CodecException;
import com.funambol.framework.tools.codec.QuotedPrintableCodec;

public class QuotedPrintableCodecTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDecode() {
		QuotedPrintableCodec codec = new QuotedPrintableCodec("iso-8159-1");
		String decoded = null;
		try {
			decoded = new String(codec.decode("Bb=0D=0A=0D=0AEndbb\r\n".getBytes()));
		} catch (CodecException e) {
			e.printStackTrace();
			fail("valid quoted printable refused");
		}
		System.out.println("decoded='"+decoded+"'");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
