package org.obm.push.calendar;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.exception.ConversionException;


public class MSEventToObmEventConverterImplTest {

    private MSEventToObmEventConverterImpl msToObmConverter;

    @Before
    public void setUp()
    {
        msToObmConverter = new MSEventToObmEventConverterImpl();
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestWhenNoStartTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setEndTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestWhenNoEndTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setStartTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestWhenNoStartNorEndTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test
    public void assertEventTimesValidityTestWhenStartAndDateTimeSet() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setStartTime(new Date());
        msevt.setEndTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestAllDayWhenNoStartTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setAllDayEvent(true);
        msevt.setEndTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestAllDayWhenNoEndTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setAllDayEvent(true);
        msevt.setStartTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test(expected=ConversionException.class)
    public void assertEventTimesValidityTestAllDayWhenNoStartNorEndTime() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setAllDayEvent(true);
        msToObmConverter.assertEventTimesValidity(msevt);
    }

    @Test
    public void assertEventTimesValidityTestAllDayWhenStartAndDateTimeSet() throws ConversionException {
        MSEventCommon msevt = new MSEvent();

        msevt.setAllDayEvent(true);
        msevt.setStartTime(new Date());
        msevt.setEndTime(new Date());
        msToObmConverter.assertEventTimesValidity(msevt);
    }
}
