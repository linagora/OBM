package fr.aliasource.funambol.utils;

import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.ConverterException;

public class MyCal2Sif extends CalendarToSIFE {

	private TimeZone savedTz;
	private Log logger = LogFactory.getLog(getClass());
	
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
			propValue = handleConversionToAllDayDate((String) propValue,
					savedTz, savedTz);
			logger.info("got '"+propValue+"' from handleConvToAllDayDate");
		} catch (ConverterException ce) {
			return new StringBuffer(); // empty
		}

		return createTagFromStringAndProperty(tag, propValue, prop);
	}

}
