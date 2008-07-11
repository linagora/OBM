package fr.aliasource.funambol.engine.source;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.funambol.common.pim.calendar.Calendar;
import com.funambol.common.pim.common.Property;
import com.funambol.common.pim.converter.CalendarToSIFE;
import com.funambol.common.pim.converter.ConverterException;
import com.funambol.common.pim.converter.VCalendarConverter;
import com.funambol.common.pim.converter.VComponentWriter;
import com.funambol.common.pim.icalendar.ICalendarParser;
import com.funambol.common.pim.model.VCalendar;
import com.funambol.common.pim.sif.SIFCalendarParser;
import com.funambol.common.pim.xvcalendar.XVCalendarParser;
import com.funambol.foundation.exception.EntityException;
import com.funambol.framework.engine.SyncItem;
import com.funambol.framework.engine.SyncItemImpl;
import com.funambol.framework.engine.SyncItemKey;
import com.funambol.framework.engine.SyncItemState;
import com.funambol.framework.engine.source.SyncContext;
import com.funambol.framework.engine.source.SyncSource;
import com.funambol.framework.engine.source.SyncSourceException;
import com.funambol.framework.tools.Base64;

import fr.aliasource.funambol.OBMException;
import fr.aliasource.funambol.utils.FunisHelper;
import fr.aliasource.funambol.utils.Helper;
import fr.aliasource.funambol.utils.MyCal2Sif;
import fr.aliasource.funambol.utils.MyVCalConverter;
import fr.aliasource.obm.items.manager.CalendarManager;

public class CalendarSyncSource extends ObmSyncSource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8820543271150832304L;

	private CalendarManager manager;
	private Log logger = LogFactory.getLog(getClass());

	public void beginSync(SyncContext context) throws SyncSourceException {

		logger.info("Begin an OBM-Funambol Calendar sync");

		manager = new CalendarManager(getObmAddress());

		try {
			manager.logIn(context.getPrincipal().getUser().getUsername(),
					context.getPrincipal().getUser().getPassword());
			manager.setCalendar(manager.getToken().getUser());
			manager.initUserEmail();
			manager.initRestriction(getRestrictions());

			super.beginSync(context);
		} catch (Throwable e) {
			logger.error("pb in begin sync", e);
			throw new SyncSourceException(e);
		}
		logger.info("beginSync end.");
	}

	/**
	 * @see SyncSource
	 */
	public SyncItem addSyncItem(SyncItem syncItem) throws SyncSourceException {

		logger.info("addSyncItem(" + principal + " , "
				+ syncItem.getKey().getKeyAsString() + ")");
		com.funambol.common.pim.calendar.Calendar created = null;
		try {
			com.funambol.common.pim.calendar.Calendar calendar = getFoundationFromSyncItem(syncItem);

			created = manager.addItem(calendar, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}

		if (created == null) {
			logger.warn("Sending faked syncitem to PDA, we skipped this event");
			syncItem.setState(SyncItemState.SYNCHRONIZED);
			return syncItem;
		} else {
			logger.info(" created with id : "
					+ created.getCalendarContent().getUid()
							.getPropertyValueAsString());
			return getSyncItemFromFoundation(created,
					SyncItemState.SYNCHRONIZED);
		}
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getAllSyncItemKeys() throws SyncSourceException {

		logger.info("getAllSyncItemKeys(" + principal + ")");

		String[] keys = null;
		try {
			keys = manager.getAllItemKeys();
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);

		logger.info(" returning " + ret.length + " key(s)");
		return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getDeletedSyncItemKeys(Timestamp since, Timestamp until)
			throws SyncSourceException {

		logger.info("getDeletedSyncItemKeys(" + principal + " , " + since
				+ " , " + until + ")");
		String[] keys = null;

		try {
			keys = manager.getDeletedItemKeys(since);

		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);

		logger.info(" returning " + ret.length + " key(s)");

		return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getNewSyncItemKeys(Timestamp since, Timestamp until)
			throws SyncSourceException {

		logger.info("getNewSyncItemKeys(" + principal + " , " + since + " , "
				+ until + ") => null");

		return new SyncItemKey[0];
	}

	/**
	 * @see SyncSource
	 */
	public SyncItemKey[] getSyncItemKeysFromTwin(SyncItem syncItem)
			throws SyncSourceException {

		logger.info("getSyncItemKeysFromTwin(" + principal + ")");
		String[] keys = null;
		try {
			syncItem.getKey().setKeyValue("");
			Calendar event = getFoundationFromSyncItem(syncItem);

			keys = manager.getEventTwinKeys(event, getSourceType());
		} catch (OBMException e) {
			logger.error(e.getMessage(), e);
			throw new SyncSourceException(e);
		}
		SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);

		logger.info(" returning " + ret.length + " key(s)");
		return ret;
	}

	/*
	 * @see SyncSource
	 */
	public SyncItemKey[] getUpdatedSyncItemKeys(Timestamp since, Timestamp until)
			throws SyncSourceException {

		logger.info("getUpdatedSyncItemKeys(" + principal + " , " + since
				+ " , " + until + ")");
		String[] keys = null;

		try {
			keys = manager.getUpdatedItemKeys(since);

		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItemKey[] ret = getSyncItemKeysFromKeys(keys);

		logger.info(" returning " + ret.length + " key(s)");

		return ret;
	}

	public void removeSyncItem(SyncItemKey syncItemKey, Timestamp time,
			boolean softDelete) throws SyncSourceException {

		logger.info("removeSyncItem(" + principal + " , " + syncItemKey + " , "
				+ time + ")");

		try {
			manager.removeItem(syncItemKey.getKeyAsString());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
	}

	/*
	 * @see SyncSource
	 */
	public SyncItem updateSyncItem(SyncItem syncItem)
			throws SyncSourceException {

		logger.info("updateSyncItem(" + principal + " , "
				+ syncItem.getKey().getKeyAsString() + ")");
		Calendar event = null;
		try {
			Calendar calendar = getFoundationFromSyncItem(syncItem);

			event = manager.updateItem(syncItem.getKey().getKeyAsString(),
					calendar, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		
		if (event == null) {
			logger.warn("Sending faked syncitem to PDA, we skipped this event");
			syncItem.setState(SyncItemState.SYNCHRONIZED);
			return syncItem;
		}
		return getSyncItemFromFoundation(event, SyncItemState.SYNCHRONIZED);
	}

	/*
	 * @see SyncSource
	 */
	public SyncItem getSyncItemFromId(SyncItemKey syncItemKey)
			throws SyncSourceException {

		logger
				.info("getSyncItemFromId(" + principal + ", " + syncItemKey
						+ ")");

		String key = syncItemKey.getKeyAsString();

		Calendar calendar = null;
		try {
			calendar = manager.getItemFromId(key, getSourceType());
		} catch (OBMException e) {
			throw new SyncSourceException(e);
		}
		SyncItem ret = getSyncItemFromFoundation(calendar,
				SyncItemState.UNKNOWN);

		return ret;
	}

	// -------------------- Private methods ----------------------

	/**
	 * Get Data from com.funambol.foundation.pdi.event.Calendar converting the
	 * Calendar object into a v-card item
	 * 
	 * @param calendar
	 * @return
	 * @throws SyncSourceException
	 */
	private String getICalFromFoundationCalendar(Calendar calendar)
			throws SyncSourceException {

		String ical = null;

		// dateAsUTC(calendar);

		try {
			VCalendarConverter c2vcal = new MyVCalConverter(deviceTimezone,
					deviceCharset);
			VCalendar cal = c2vcal.calendar2vcalendar(calendar, true);
			VComponentWriter writer = new VComponentWriter(
					VComponentWriter.NO_FOLDING);
			ical = writer.toString(cal);
		} catch (ConverterException ex) {
			throw new SyncSourceException("Error converting calendar in iCal",
					ex);
		}
		return ical;
	}

	/**
	 * Get Data from ICal message converting the ical item into a Calendar
	 * object
	 * 
	 * the calendar object is a com.funambol.foundation.pim.calendar.Calendar
	 * 
	 * @param content
	 *            String
	 * @return Calendar
	 * @throws OBMException
	 */
	private Calendar getFoundationCalendarFromICal(String content)
			throws OBMException {
		logger.info("pda sent:\n"+content);
		
		
		String toParse = content;
		toParse = toParse.replace("encoding", "ENCODING");
		toParse = toParse.replace("PRINTABLE:", "PRINTABLE;CHARSET=UTF-8:");
		toParse = FunisHelper.removeQuotedPrintableFromVCalString(toParse);
		ByteArrayInputStream buffer = new ByteArrayInputStream(toParse
				.getBytes());

		try {
			VCalendar vcal = null;
			if (toParse.contains("VERSION:1.0")) {
				logger.info("Parsing version 1.0 as xvcalendar");
				XVCalendarParser parser = new XVCalendarParser(buffer,
						deviceCharset);
				vcal = (VCalendar) parser.XVCalendar();
			} else {
				logger.info("Parsing version 2.0 as icalendar");
				ICalendarParser parser = new ICalendarParser(buffer,
						deviceCharset);
				vcal = parser.ICalendar();
			}
			VCalendarConverter vconvert = new MyVCalConverter(deviceTimezone,
					deviceCharset);

			Calendar ret = vconvert.vcalendar2calendar(vcal);
			return ret;
		} catch (Exception e) {
			throw new OBMException("Error converting from ical ", e);
		}

	}

	/**
	 * 
	 * Get Data from sync4j.foundation.pdi.event.Calendar converting the
	 * Calendar object into an xml item
	 * 
	 * @param calendar
	 *            Calendar
	 * @return String
	 * @throws OBMException
	 * @throws SyncSourceException
	 * @throws EntityException
	 */
	private String getXMLFromFoundationCalendar(Calendar calendar)
			throws SyncSourceException {

		String xml = null;
		try {
			CalendarToSIFE c2xml = new MyCal2Sif(deviceTimezone, deviceCharset);
			xml = c2xml.convert(calendar);
		} catch (ConverterException ex) {
			throw new SyncSourceException("Error converting calendar in xml",
					ex);
		}

		return xml;
	}

	/**
	 * Get Data from XML message converting the xml item into a Calendar object
	 * 
	 * the calendar object is a sync4j.foundation.pdi.event.Calendar
	 * 
	 * @param content
	 *            String
	 * @return Calendar
	 * @throws OBMException
	 */
	private Calendar getFoundationCalendarFromXML(String content)
			throws OBMException {

		ByteArrayInputStream buffer = null;
		Calendar calendar = null;
		try {
			calendar = new Calendar();
			buffer = new ByteArrayInputStream(content.getBytes());
			if ((content.getBytes()).length > 0) {
				SIFCalendarParser parser = new SIFCalendarParser(buffer);
				calendar = parser.parse();
			}
		} catch (Exception e) {
			throw new OBMException("Error converting ", e);
		}
		return calendar;
	}

	private Calendar getFoundationFromSyncItem(SyncItem item)
			throws OBMException, SyncSourceException {
		Calendar foundationCalendar = null;

		String content = Helper.getContentOfSyncItem(item, this.isEncode());
		logger.info("foundFromSync:\n" + content);
		logger.info(" ===> syncItemKey: " + item.getKey());

		if (MSG_TYPE_ICAL.equals(getSourceType())) {
			foundationCalendar = getFoundationCalendarFromICal(content);
		} else {
			foundationCalendar = getFoundationCalendarFromXML(content);
		}

		logger.info("calContent.uid: "
				+ foundationCalendar.getCalendarContent().getUid());

		foundationCalendar.getCalendarContent().setUid(
				new Property(item.getKey().getKeyAsString()));

		return foundationCalendar;
	}

	private SyncItem getSyncItemFromFoundation(Calendar calendar, char status)
			throws SyncSourceException {
		SyncItem syncItem = null;

		String content = null;

		if (MSG_TYPE_ICAL.equals(getSourceType())) {
			content = getICalFromFoundationCalendar(calendar);
		} else {
			content = getXMLFromFoundationCalendar(calendar);
		}

		syncItem = new SyncItemImpl(this, calendar.getCalendarContent()
				.getUid().getPropertyValueAsString(), status);

		logger.info("sending syncitem to pda:\n" + content);
		if (isEncode()) {
			syncItem.setContent(Base64.encode(content.getBytes()));
			syncItem.setType(getSourceType());
			syncItem.setFormat("b64");
		} else {
			syncItem.setContent(content.getBytes());
			syncItem.setType(getSourceType());
		}

		return syncItem;
	}
}