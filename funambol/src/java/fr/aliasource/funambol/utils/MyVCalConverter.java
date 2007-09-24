package fr.aliasource.funambol.utils;

import java.util.ArrayList;
import java.util.TimeZone;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.calendar.CalendarContent;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.VCalendarContentConverter;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.model.VCalendarContent;

public class MyVCalConverter extends VCalendarConverter {

	public MyVCalConverter(TimeZone timezone, String charset) {
		super(timezone, charset);
	}

	public Calendar vcalendar2calendar(VCalendar vcal)
			throws ConverterException {
		Calendar cal = new Calendar();
		setCommonProperties(cal, vcal);
		MyCalContentConverter vccf = new MyCalContentConverter(timezone,
				charset);
		boolean xv = false;
		if (vcal.getProperty("VERSION") != null
				&& vcal.getProperty("VERSION").getValue() != null
				&& vcal.getProperty("VERSION").getValue().equals("1.0")) {
			xv = true;
		}
		CalendarContent cc = vccf.vcc2cc(vcal.getVCalendarContent(), xv);
		cal.setCalendarContent(cc);
		return cal;
	}

	public VCalendar calendar2vcalendar(Calendar cal, boolean xv)
			throws ConverterException {
		VCalendar vcal = new VCalendar();
		setCommonProperties(vcal, cal);
		String version = (xv ? "1.0" : "2.0");
		vcal.addProperty(new com.funambol.common.pim.model.Property("VERSION",
				false, new ArrayList(), version));
		MyCalContentConverter vccf = new MyCalContentConverter(
				timezone, charset);
		VCalendarContent vcc = vccf.cc2vcc(cal.getCalendarContent(), xv);
		vcal.addComponent(vcc);

		return vcal;
	}

	private void setCommonProperties(VCalendar vcal, Calendar cal)
			throws ConverterException {

		if (cal.getProdId() != null) {
			com.funambol.common.pim.model.Property prodId = composeField(
					"PRODID", cal.getProdId());
			if (prodId != null) {
				vcal.addProperty(prodId);
			}
		}

		if (cal.getVersion() != null) {
			com.funambol.common.pim.model.Property version = composeField(
					"VERSION", cal.getVersion());
			if (version != null) {
				vcal.addProperty(version);
			}
		}

		if (cal.getCalScale() != null) {
			com.funambol.common.pim.model.Property calscale = composeField(
					"CALSCALE", cal.getCalScale());
			if (calscale != null) {
				vcal.addProperty(calscale);
			}
		}

		if (cal.getMethod() != null) {
			com.funambol.common.pim.model.Property method = composeField(
					"METHOD", cal.getMethod());
			if (method != null) {
				vcal.addProperty(method);
			}
		}
	}

	private void setCommonProperties(Calendar cal, VCalendar vcal)
			throws ConverterException {

		cal.setProdId(decodeField(vcal.getProperty("PRODID")));
		cal.setVersion(decodeField(vcal.getProperty("VERSION")));
		cal.setCalScale(decodeField(vcal.getProperty("CALSCALE")));
		cal.setMethod(decodeField(vcal.getProperty("METHOD")));
	}

}
