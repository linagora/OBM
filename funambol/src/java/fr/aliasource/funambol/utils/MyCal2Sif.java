package fr.aliasource.funambol.utils;

import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.CalendarToSIFE;

public class MyCal2Sif extends CalendarToSIFE {

	private TimeZone savedTz;
	private Log logger = LogFactory.getLog(getClass());
	private String storedStart;

	public MyCal2Sif(TimeZone timezone, String charset) {
		super(timezone, charset);
		this.savedTz = timezone;
	}

	@Override
	protected StringBuffer createTagFromAllDayProperty(String tag, Property prop) {

		String propValue = null;
		if (prop != null) {
			propValue = (String) prop.getPropertyValue();
		}

		try {
			if ("Start".equals(tag)) {
				storedStart = propValue;
			}

			String oldVal = propValue;
			// propValue = handleConversionToAllDayDate((String) propValue,
			// savedTz, savedTz);
			if ("End".equals(tag)) {
				propValue = CalendarHelper.formatWithTiret(storedStart);
			} else {
				propValue = CalendarHelper.formatWithTiret(propValue);
			}

			logger.info("[tag: " + tag + "] got '" + propValue
					+ "' from handleConvToAllDayDate(" + oldVal + ", tz, tz)");
		} catch (Throwable ce) {
			return new StringBuffer(); // empty
		}

		return createTagFromStringAndProperty(tag, propValue, prop);
	}

}
