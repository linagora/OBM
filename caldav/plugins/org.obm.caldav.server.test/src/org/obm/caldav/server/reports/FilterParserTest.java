package org.obm.caldav.server.reports;

import java.io.InputStream;


import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.server.share.filter.Filter;
import org.obm.caldav.server.share.filter.ParamFilter;
import org.obm.caldav.server.share.filter.PropFilter;
import org.obm.caldav.server.share.filter.TimeRange;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class FilterParserTest extends TestCase {

	FilterParser filterParser;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		filterParser = new FilterParser();

	}

	public void testParse() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"data/filter1.xml");
		try {
			Document doc = DOMUtils.parse(is);
			Filter filter = filterParser.parse(doc);
			assertNotNull(filter);
			assertNotNull(filter.getCompFilter());
			CompFilter cfPere = filter.getCompFilter();
			assertNotNull(cfPere.getName());
			assertEquals(cfPere.getName(), "VCALENDAR");
			assertEquals(cfPere.getCompFilters().size(), 1);
			CompFilter cfVEVENT = cfPere.getCompFilters().get(0);
			assertNotNull(cfVEVENT);
			assertNotNull(cfVEVENT.getName());
			assertEquals(cfVEVENT.getName(), "VEVENT");
			TimeRange tr = cfVEVENT.getTimeRange();
			assertNotNull(tr);
			assertNotNull(tr.getStart());
			assertNotNull(tr.getEnd());
			
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
	}
	public void testParse1() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"data/filter2.xml");
		try {
			Document doc = DOMUtils.parse(is);
			Filter filter = filterParser.parse(doc);
			assertNotNull(filter);
			assertNotNull(filter.getCompFilter());
			CompFilter cfPere = filter.getCompFilter();
			assertNotNull(cfPere.getName());
			assertEquals(cfPere.getName(), "VCALENDAR");
			
			assertEquals(2,cfPere.getCompFilters().size());
			CompFilter cfVTODO = cfPere.getCompFilters().get(0);
			assertNotNull(cfVTODO);
			assertNotNull(cfVTODO.getName());
			assertEquals(cfVTODO.getName(), "VTODO");
			
			assertEquals(cfVTODO.getPropFilters().size(), 3);
			PropFilter pf1 = cfVTODO.getPropFilters().get(0);
			assertEquals(pf1.getName(), "COMPLETED");
			assertTrue(pf1.isNotDefined());
			
			PropFilter pf2 = cfVTODO.getPropFilters().get(1);
			assertEquals(pf2.getName(), "STATUS");
			assertNotNull(pf2.getTextMatch());
			assertTrue(pf2.getTextMatch().isNegateCondition());
			assertEquals(pf2.getTextMatch().getText(), "CANCELLED");
			
			PropFilter pf3 = cfVTODO.getPropFilters().get(2);
			assertNotNull(pf3);
			assertEquals(pf3.getName(), "START");
			assertNotNull(pf3.getTimeRange());
			assertNotNull(pf3.getTimeRange().getStart());
			assertNotNull(pf3.getTimeRange().getEnd());
			
			
			CompFilter cfVEVENT = cfPere.getCompFilters().get(1);
			assertNotNull(cfVEVENT);
			assertNotNull(cfVEVENT.getName());
			assertEquals(cfVEVENT.getName(), "VEVENT");
			
			assertEquals(cfVEVENT.getPropFilters().size(), 2);
			PropFilter pf4 = cfVEVENT.getPropFilters().get(0);
			assertEquals(pf4.getName(), "COMPLETED");
			assertTrue(pf4.isNotDefined());
			assertNotNull(pf4.getTimeRange());
			assertNotNull(pf4.getTimeRange().getStart());
			assertNotNull(pf4.getTimeRange().getEnd());
			assertNotNull(pf4.getTextMatch());
			assertEquals(pf4.getTextMatch().getText(), "NEEDS-ACTION");
			assertEquals(pf4.getTextMatch().getCollation(), "i;ascii-casemap");
			
			PropFilter pf5 = cfVEVENT.getPropFilters().get(1);
			assertEquals(pf5.getName(), "STATUS");
			assertEquals(pf5.getParamFilters().size(), 1);
			ParamFilter paramf = pf5.getParamFilters().get(0);
			assertTrue(paramf.isNotDefined());
			assertFalse(paramf.getTextMatch().isNegateCondition());
			assertEquals(paramf.getTextMatch().getText(), "NEEDS-ACTION");
			assertEquals(paramf.getTextMatch().getCollation(), "i;ascii-casemap");
			
			
			
			TimeRange tr = cfVEVENT.getTimeRange();
			assertNotNull(tr);
			assertNotNull(tr.getStart());
			assertNotNull(tr.getEnd());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
