package fr.aliasource.funambol;

import java.io.ByteArrayInputStream;

import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.icalendar.ParseException;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;

import junit.framework.TestCase;

public class ICalParsingTest extends TestCase {

	private String vcal = "BEGIN:VCALENDAR\n" + "VERSION:1.0\n"
			+ "BEGIN:VEVENT\n" + "SUMMARY;CHARSET=UTF-8:Déjeuner \n"
			+ "CLASS:PUBLIC\n" + "DESCRIPTION:\n"
			+ "DTSTART:20071009T100000Z\n" + "DTEND:20071009T123000Z\n"
			+ "TRANSP:0\n"
			+ "AALARM;CHARSET=UTF-8:20071009T094500Z;;;Déjeuner \n"
			+ "DALARM;CHARSET=UTF-8:20071009T094500Z;;;Déjeuner \n"
			+ "LOCATION:\n" + "END:VEVENT\n" + "END:VCALENDAR\n";

	private ByteArrayInputStream bin;

	public void testAsIcal() {
		ICalendarParser parser = new ICalendarParser(bin);
		try {
			parser.ICalendar();
			fail("should not parse as icalendar");
		} catch (ParseException e) {
		}

	}

	public void testAsVcal() {
		XVCalendarParser parser = new XVCalendarParser(bin);
		try {
			VCalendar cal = parser.XVCalendar();
			assertNotNull(cal);
		} catch (com.funambol.common.pim.xvcalendar.ParseException e) {
			e.printStackTrace();
			fail("not parseable as icalendar");
		}
	}

	protected void setUp() throws Exception {
		bin = new ByteArrayInputStream(vcal.getBytes());
		super.setUp();
	}

	protected void tearDown() throws Exception {
		if (bin != null) {
			bin.close();
			bin = null;
		}
		super.tearDown();
	}

}
