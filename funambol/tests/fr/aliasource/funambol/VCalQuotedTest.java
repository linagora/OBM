package fr.aliasource.funambol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.xvcalendar.ParseException;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;

import fr.aliasource.funambol.utils.FunisHelper;

public class VCalQuotedTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private String processVCal(String vcal) {
		String ret = FunisHelper.removeQuotedPrintableFromVCalString(vcal);
		System.out.println("Fixed vcal:\n"+ret);
		return ret;
	}
	
	private String readTestFile(String name) throws IOException {
		RandomAccessFile raf = new RandomAccessFile("test-data/"+name, "r");
		byte[] content = new byte[(int) raf.length()];
		raf.readFully(content);
		raf.close();
		return new String(content);
	}
	
	public void testParsing() throws IOException, ParseException {
		String vcal = readTestFile("quoted.vcal");
		vcal = processVCal(vcal);
		XVCalendarParser parser = new XVCalendarParser(new ByteArrayInputStream(vcal.getBytes()));
		VCalendar xv = parser.XVCalendar();
		assertNotNull(xv);
	}
	
	
}
