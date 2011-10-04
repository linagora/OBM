package org.obm.push.tnefconverter.helper;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

public class ICSHelper {
	
	public static Calendar initCalendar(){
		Calendar calendar = new Calendar();
		calendar.getProperties().add(
				new ProdId("-//OBM Tnef Converter 1.0 //FR"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		return calendar;
	}
}
