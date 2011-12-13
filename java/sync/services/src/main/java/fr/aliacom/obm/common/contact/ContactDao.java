/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.contact;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.obm.locator.LocatorClientException;
import org.obm.configuration.ContactConfiguration;
import org.obm.push.utils.jdbc.IntegerIndexedSQLCollectionHelper;
import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ContactNotFoundException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Address;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.Folder;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.solr.SolrHelper;
import org.obm.sync.solr.SolrHelper.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.utils.LinkedEntity;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * SQL queries for contact for sync
 */
@Singleton
public class ContactDao {

	private static final Logger logger = LoggerFactory
			.getLogger(ContactDao.class);

	private static final String ANNIVERSARY_FIELD = "contact_anniversary_id";
	private static final String BIRTHDAY_FIELD = "contact_birthday_id";

	private static final String CONTACT_SELECT_FIELDS = "contact_id, contact_firstname, contact_lastname, contactentity_entity_id, "
		+ "contact_aka, contact_company, contact_title, contact_service, contact_birthday_id, "
		+ "contact_anniversary_id, contact_middlename, contact_suffix, contact_manager, contact_assistant, contact_spouse, "
		+ "contact_addressbook_id, contact_comment, contact_commonname";

	private static final String MY_GROUPS_QUERY = "SELECT groupentity_entity_id FROM of_usergroup "
		+ "INNER JOIN GroupEntity ON of_usergroup_group_id=groupentity_group_id WHERE of_usergroup_user_id=?";

	private final CalendarDao calendarDao;
	private final Factory solrHelperFactory;
	private final ObmHelper obmHelper;
	private final ContactConfiguration contactConfiguration;

	@Inject
	private ContactDao(ContactConfiguration contactConfiguration, CalendarDao calendarDao, SolrHelper.Factory solrHelperFactory, ObmHelper obmHelper) {
		this.contactConfiguration = contactConfiguration;
		this.calendarDao = calendarDao;
		this.solrHelperFactory = solrHelperFactory;
		this.obmHelper = obmHelper;
	}

	private String getSelectForFindUpdatedContacts(AccessToken at) {
		String sql = "SELECT "
				+ CONTACT_SELECT_FIELDS
				+ ", contact_archive, now() as last_sync FROM Contact"
				+ " INNER JOIN SyncedAddressbook s ON (contact_addressbook_id=s.addressbook_id AND s.user_id="
				+ at.getObmId()
				+ ") "
				+ "INNER JOIN ContactEntity ON contactentity_contact_id=contact_id "
				+ "INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id=s.addressbook_id "
				+ "INNER JOIN AddressBook ON id=s.addressbook_id "
				+ "LEFT JOIN EntityRight urights ON "
				+ "(urights.entityright_entity_id=addressbookentity_entity_id AND "
				+ "urights.entityright_consumer_id=(select userentity_entity_id FROM UserEntity WHERE userentity_user_id=?)) "
				+ "LEFT JOIN EntityRight grights ON grights.entityright_entity_id=addressbookentity_entity_id "
				+ "AND grights.entityright_consumer_id IN ("
				+ MY_GROUPS_QUERY
				+ ") "
				+ "LEFT JOIN EntityRight prights ON prights.entityright_entity_id=addressbookentity_entity_id AND prights.entityright_consumer_id IS NULL "
				+ "WHERE "
				+ "(owner=? OR urights.entityright_read=1 OR grights.entityright_read=1 OR prights.entityright_read=1)";
		sql += " AND (contact_timecreate >= ? OR contact_timeupdate >= ? OR s.timestamp >= ?)";
		return sql;
	}
	
	private String getSelectForFindUpdatedContacts(Integer addressBookId, AccessToken at) {
		String sql = getSelectForFindUpdatedContacts(at);
		sql += " AND contact_addressbook_id = " + addressBookId;
		return sql;
	}

	private String getSelectForFindRemovalCandidates(AccessToken at) {
		String q = "SELECT "
			+ "deletedcontact_contact_id "
			+ "FROM DeletedContact "
			+ "INNER JOIN SyncedAddressbook s ON ( s.addressbook_id=deletedcontact_addressbook_id AND s.user_id= "
			+ at.getObmId() + ")";
		q += " WHERE deletedcontact_timestamp >= ? ";
		return q;
	}
	
	private String getSelectForFindRemovalCandidates(Integer addressBookId, AccessToken at) {
		String sql = getSelectForFindRemovalCandidates(at);
		sql += " AND deletedcontact_addressbook_id = " + addressBookId;
		return sql;
	}
	
	public ContactUpdates findUpdatedContacts(Date timestamp, AccessToken at) throws SQLException {
		String sql = getSelectForFindUpdatedContacts(at);
		return findUpdatedContacts(sql, timestamp, at);
	}
	
	private ContactUpdates findUpdatedContacts(String sql, Date timestamp, AccessToken at) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ContactUpdates upd = new ContactUpdates();
		try {

			List<Contact> contacts = new ArrayList<Contact>();
			Set<Integer> archivedContactIds = new TreeSet<Integer>();

			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);

			int idx = 1;
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getObmId());
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			rs = ps.executeQuery();

			Map<Integer, Contact> entityContact = new HashMap<Integer, Contact>();
			while (rs.next()) {
				boolean archived = rs.getBoolean("contact_archive");
				Contact c = contactFromCursor(rs);
				if (!archived) {
					entityContact.put(c.getEntityId(), c);
					contacts.add(c);
				} else {
					archivedContactIds.add(c.getUid());
				}
			}
			rs.close();
			rs = null;

			if (!entityContact.isEmpty()) {
				loadPhones(con, entityContact);
				loadIMIdentifiers(con, entityContact);
				loadWebsites(con, entityContact);
				loadAddresses(at, con, entityContact);
				loadEmails(con, entityContact);
				loadBirthday(con, entityContact);
				loadAnniversary(con, entityContact);
			}
			
			upd.setArchived(archivedContactIds);
			upd.setContacts(contacts);
			
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return upd;
	}

	private void loadBirthday(Connection con,
			Map<Integer, Contact> entityContact) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<EventObmId> bdayIds = new HashSet<EventObmId>();
		HashMap<EventObmId, Contact> eventIdMap = new HashMap<EventObmId, Contact>();
		for (Contact c : entityContact.values()) {
			if (c.getBirthdayId() != null) {
				bdayIds.add(c.getBirthdayId());
				eventIdMap.put(c.getBirthdayId(), c);
			}
		}
		if (bdayIds.isEmpty()) {
			return;
		}
		
		IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(bdayIds);
		String q = "select event_id, event_date from Event where event_id IN ("
			+ eventIds.asPlaceHolders() + ")";
		try {
			ps = con.prepareStatement(q);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				EventObmId evId = new EventObmId(rs.getInt(1));
				Contact c = eventIdMap.get(evId);
				c.setBirthday(rs.getTimestamp(2));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private void loadAnniversary(Connection con,
			Map<Integer, Contact> entityContact) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<EventObmId> bdayIds = new HashSet<EventObmId>();
		HashMap<EventObmId, Contact> eventIdMap = new HashMap<EventObmId, Contact>();
		for (Contact c : entityContact.values()) {
			if (c.getBirthdayId() != null) {
				bdayIds.add(c.getAnniversaryId());
				eventIdMap.put(c.getAnniversaryId(), c);
			}
		}
		if (bdayIds.isEmpty()) {
			return;
		}
		
		IntegerIndexedSQLCollectionHelper eventIds = new IntegerIndexedSQLCollectionHelper(bdayIds);
		String q = "select event_id, event_date from Event where event_id IN ("
			+ eventIds.asPlaceHolders() + ")";
		try {
			ps = con.prepareStatement(q);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				EventObmId evId = new EventObmId(rs.getInt(1));
				Contact c = eventIdMap.get(evId);
				c.setAnniversary(rs.getTimestamp(2));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private Contact contactFromCursor(ResultSet rs) throws SQLException {
		Contact c = new Contact();

		c.setUid(rs.getInt(1));
		c.setFirstname(rs.getString(2));
		c.setLastname(rs.getString(3));
		c.setEntityId(rs.getInt(4));
		c.setAka(rs.getString(5));
		c.setCompany(rs.getString(6));
		c.setTitle(rs.getString(7));
		c.setService(rs.getString(8));
		int birthdayId = rs.getInt(9);
		if (!rs.wasNull()) {
			c.setBirthdayId(new EventObmId(birthdayId));
		}

		// "contact_anniversary_id, contact_middlename, contact_suffix, contact_manager, contact_assistant, contact_spouse ";
		// post freeze fields
		int anniversaryId = rs.getInt(10);
		if (!rs.wasNull()) {
			c.setAnniversaryId(new EventObmId(anniversaryId));
		}
		c.setMiddlename(rs.getString(11));
		c.setSuffix(rs.getString(12));
		c.setManager(rs.getString(13));
		c.setAssistant(rs.getString(14));
		c.setSpouse(rs.getString(15));

		c.setFolderId(rs.getInt(16));
		c.setComment(rs.getString(17));
		c.setCommonname(rs.getString(18));

		return c;
	}

	private Contact createContactInAddressBook(Connection con, AccessToken at, Contact c, int addressBookId) {
		try {
			EventObmId anniversaryId = createOrUpdateDate(at, con, c, c
					.getAnniversary(), ANNIVERSARY_FIELD);
			c.setAnniversaryId(anniversaryId);

			EventObmId birthdayId = createOrUpdateDate(at, con, c,
					c.getBirthday(), BIRTHDAY_FIELD);
			c.setBirthdayId(birthdayId);

			int contactId = insertIntoContact(con, at, c, addressBookId);
			LinkedEntity le = obmHelper.linkEntity(con, "ContactEntity",
					"contact_id", contactId);
			c.setEntityId(le.getEntityId());

			createOrUpdatePhones(con, c.getEntityId(), c.getPhones());
			createOrUpdateAddresses(con, c.getEntityId(), c.getAddresses());
			createOrUpdateEmails(con, c.getEntityId(), c.getEmails());
			createOrUpdateWebsites(con, c);
			createOrUpdateIMIdentifiers(con, c.getEntityId(), c.getImIdentifiers());
			c.setUid(contactId);
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		}

		indexContact(at, c);

		return c;
	}

	private void indexContact(AccessToken at, Contact c) {
		try {
			// no need to pass the sql connection as indexing will be done in a
			// separate thread
			solrHelperFactory.createClient(at).createOrUpdate(c);
		} catch (Exception e) {
			logger.error("Error indexing contact", e);
		}
	}

	public Contact createContact(AccessToken at, Contact c) throws SQLException {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			createContact(at, con, c);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return c;
	}


	public Contact createContact(AccessToken at, Connection con, Contact c)
	throws SQLException {
		int addressbookId = chooseAddressBookFromContact(con, at, c);
		return createContactInAddressBook(con, at, c, addressbookId);
	}

	public Contact createContactInAddressBook(AccessToken at, Contact c, int addressbookId) throws SQLException {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			c = createContactInAddressBook(con, at, c, addressbookId);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return c;
	}

	private Event getEvent(AccessToken token, String displayName, Date startDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTime(startDate);

		Event e = new Event();
		e.setTitle(displayName);
		e.setDate(cal.getTime());
		e.setDuration(3600);
		e.setAllday(true);
		EventRecurrence rec = new EventRecurrence();
		rec.setDays("0000000");
		rec.setFrequence(1);
		rec.setKind(RecurrenceKind.yearly);
		rec.setEnd(null);
		e.setRecurrence(rec);
		e.setPrivacy(1);
		e.setPriority(1);
		Attendee at = new Attendee();
		at.setEmail(token.getEmail());
		at.setRequired(ParticipationRole.CHAIR);
		at.setState(ParticipationState.ACCEPTED);
		e.addAttendee(at);

		logger.info("inserting birthday with date " + cal.getTime());
		return e;
	}

	private String displayName(Contact c) {
		StringBuilder b = new StringBuilder(255);
		if (c.getFirstname() != null) {
			b.append(c.getFirstname());
			b.append(" ");
		}
		if (c.getLastname() != null) {
			b.append(c.getLastname());
		}
		return b.toString();
	}

	private EventObmId createOrUpdateDate(AccessToken at, Connection con,
			Contact c, Date date, String idField) throws SQLException, FindException, EventNotFoundException, ServerFault {
		EventObmId dateId = null;
		if (c.getUid() != null && c.getUid().intValue() != 0) {
			dateId = getDateIdForContact(con, c, idField);
		}

		if (date != null) {
			logger.info("date != null");
			if (dateId == null) {
				return createEventForContactDate(at, con, c, date);
			} else {
				return retrieveAndModifyEventForContactDate(at, con, date, dateId);
			}
		} else {
			logger.info("date == null");
			if (dateId != null) {
				//sequence is set to zero as no email notification will be send 
				calendarDao.removeEventById(con, at, dateId, EventType.VEVENT, 0);
				return null;
			} else {
				return dateId;
			}
		}
	}


	private EventObmId retrieveAndModifyEventForContactDate(AccessToken at,
			Connection con, Date date, EventObmId dateId)
			throws EventNotFoundException, ServerFault, SQLException,
			FindException {
		logger.info("eventId != null");
		Event e = calendarDao.findEventById(at, dateId);
		e.setDate(date);
		calendarDao.modifyEvent(con, at, at.getUserWithDomain(), e, false, true);
		return e.getObmId();
	}


	private EventObmId createEventForContactDate(AccessToken at,
			Connection con, Contact c, Date date) throws SQLException,
			FindException {
		logger.info("eventId == null");
		Event e = calendarDao.createEvent(con, at, at.getUserWithDomain(),
				getEvent(at, displayName(c), date), true);
		return e.getObmId();
	}


	private EventObmId getDateIdForContact(Connection con, Contact c, String idField) {
		
		logger.info("c.getUid != null");
		PreparedStatement ps = null;
		ResultSet rs = null;
		String q = "select " + idField
		+ " from Contact where contact_id=?";
		try {
			ps = con.prepareStatement(q);
			ps.setInt(1, c.getUid());
			rs = ps.executeQuery();
			if (rs.next()) {
				int eventObmId = rs.getInt(1);
				if (!rs.wasNull()) {
					return new EventObmId(eventObmId);
				}
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return null;
	}

	private void createOrUpdateIMIdentifiers(Connection con, int entityId,
			Map<String, InstantMessagingId> imIdentifiers) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper imIds = new StringSQLCollectionHelper(imIdentifiers.keySet());
			ps = con
			.prepareStatement("DELETE FROM IM WHERE im_entity_id=? AND im_label IN ("
					+ imIds.asPlaceHolders() + ")");
			ps.setInt(1, entityId);
			imIds.insertValues(ps, 2);
			ps.executeUpdate();

			ps.close();
			ps = con
			.prepareStatement("INSERT INTO IM (im_entity_id, im_label, im_protocol, im_address) "
					+ "VALUES (?, ?, ?, ?)");
			for (Entry<String, InstantMessagingId> entry: imIdentifiers.entrySet()) {
				ps.setInt(1, entityId);
				ps.setString(2, entry.getKey());
				ps.setString(3, entry.getValue().getProtocol());
				ps.setString(4, entry.getValue().getId());
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void createOrUpdateWebsites(final Connection con, final Contact c) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(c.listWebSitesLabel());
			ps = con.prepareStatement("DELETE FROM Website WHERE website_entity_id=? AND website_label IN (" + 
					labels.asPlaceHolders() + ")");
			ps.setInt(1, c.getEntityId());
			labels.insertValues(ps, 2);
			ps.executeUpdate();

			ps.close();
			ps = con.prepareStatement("INSERT INTO Website (website_entity_id, website_label, website_url) VALUES (?, ?, ?)");

			final String label = "CALURI;X-OBM-Ref1";
			insertWebSite(con, c.getEntityId(), label, c.getCalUri());
			for (final Website website: c.getWebsites()) {
				if (!website.isCalendarUrl() || !website.getLabel().equalsIgnoreCase(label)) {
					insertWebSite(con, c.getEntityId(), website.getLabel(),  website.getUrl());
				}
			}
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void insertWebSite(Connection con, int entityId, String label, String url) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO Website (website_entity_id, website_label, website_url) VALUES (?, ?, ?)");
			if (!StringUtils.isEmpty(url)) {
				ps.setInt(1, entityId);
				ps.setString(2, label);
				ps.setString(3, url);
				ps.executeUpdate();
			}
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void createOrUpdateAddresses(Connection con, int entityId,
			Map<String, Address> addresses) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(addresses.keySet());
			ps = con
			.prepareStatement("DELETE FROM Address WHERE address_entity_id=? and address_label IN ("
					+ labels.asPlaceHolders() + ")");
			ps.setInt(1, entityId);
			labels.insertValues(ps, 2);
			ps.executeUpdate();

			ps.close();
			ps = con
			.prepareStatement("INSERT INTO Address (address_entity_id, address_label, "
					+ "address_street, address_zipcode, address_town, address_expresspostal, address_country, address_state) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			for (Entry<String, Address> entry: addresses.entrySet()) {
				Address ad = entry.getValue();
				ps.setInt(1, entityId);
				ps.setString(2, entry.getKey());
				ps.setString(3, ad.getStreet());
				ps.setString(4, ad.getZipCode());
				ps.setString(5, ad.getTown());
				ps.setString(6, ad.getExpressPostal());
				ps.setString(7, getCountryIso3166(con, ad.getCountry()));
				ps.setString(8, ad.getState());
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}


	private void createOrUpdateEmails(Connection con, int entityId,
			Map<String, Email> emails) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper emailStrings = new StringSQLCollectionHelper(emails.keySet());
			ps = con
			.prepareStatement("DELETE FROM Email WHERE email_entity_id=? AND email_label IN ("
					+ emailStrings.asPlaceHolders() + ")");
			ps.setInt(1, entityId);
			emailStrings.insertValues(ps, 2);
			ps.executeUpdate();

			ps.close();
			ps = con
			.prepareStatement("INSERT INTO Email (email_entity_id, email_label, email_address) "
					+ "VALUES (?, ?, ?)");
			for (Entry<String, Email> entry: emails.entrySet()) {
				ps.setInt(1, entityId);
				ps.setString(2, entry.getKey());
				ps.setString(3, entry.getValue().getEmail());
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void createOrUpdatePhones(Connection con, int entityId,
			Map<String, Phone> phones) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(phones.keySet());
			ps = con
			.prepareStatement("DELETE FROM Phone WHERE phone_entity_id=? and phone_label IN ("
					+ labels.asPlaceHolders() + ")");
			ps.setInt(1, entityId);
			labels.insertValues(ps, 2);
			ps.executeUpdate();

			ps.close();
			ps = con
			.prepareStatement("INSERT INTO Phone (phone_entity_id, phone_label, phone_number) "
					+ "VALUES (?, ?, ?)");
			for (Entry<String, Phone> entry: phones.entrySet()) {
				ps.setInt(1, entityId);
				ps.setString(2, entry.getKey());
				ps.setString(3, entry.getValue().getNumber());
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private int chooseAddressBookFromContact(Connection con, AccessToken at, Contact c) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(
			"SELECT id from AddressBook WHERE name=? AND owner=? AND is_default");
			if (c.isCollected()) {
				ps.setString(1, contactConfiguration.getCollectedAddressBookName());
			} else {
				ps.setString(1, contactConfiguration.getDefaultAddressBookName());
			}
			ps.setInt(2, at.getObmId());

			rs = ps.executeQuery();
			rs.next();

			return rs.getInt(1);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private int insertIntoContact(Connection con, AccessToken at, Contact c, int addressBookId)
	throws SQLException {
		PreparedStatement ps = null;
		try {

			ps = con
			.prepareStatement("INSERT INTO Contact "
					+ " (contact_commonname, contact_firstname, contact_lastname, contact_origin, contact_domain_id, contact_usercreate, "
					+ "contact_company, contact_aka, contact_service, contact_title, contact_birthday_id, contact_anniversary_id, "
					+ "contact_timecreate, "
					+ "contact_suffix, contact_middlename, contact_manager, contact_spouse, contact_assistant, "
					+ "contact_collected, contact_addressbook_id) "
					+ " VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?) ");
			int idx = 1;
			ps.setString(idx++, c.getCommonname());
			ps.setString(idx++, c.getFirstname());
			ps.setString(idx++, c.getLastname());
			ps.setString(idx++, at.getOrigin());
			ps.setInt(idx++, at.getDomainId());
			ps.setInt(idx++, at.getObmId());

			ps.setString(idx++, c.getCompany());
			ps.setString(idx++, c.getAka());
			ps.setString(idx++, c.getService());
			ps.setString(idx++, c.getTitle());
			if (c.getBirthdayId() != null) {
				ps.setInt(idx++, c.getBirthdayId().getObmId());
			} else {
				ps.setNull(idx++, Types.BIGINT);
			}
			if (c.getAnniversaryId() != null) {
				ps.setInt(idx++, c.getAnniversaryId().getObmId());
			} else {
				ps.setNull(idx++, Types.BIGINT);
			}

			ps.setString(idx++, c.getSuffix());
			ps.setString(idx++, c.getMiddlename());
			ps.setString(idx++, c.getManager());
			ps.setString(idx++, c.getSpouse());
			ps.setString(idx++, c.getAssistant());

			ps.setBoolean(idx++, c.isCollected());
			ps.setInt(idx++, addressBookId);

			ps.executeUpdate();

			int contactId = obmHelper.lastInsertId(con);

			return contactId;

		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	public Contact modifyContact(AccessToken token, Contact c) throws SQLException, FindException, EventNotFoundException, ServerFault {

		String q = "update Contact SET "
			+ "contact_commonname=?, contact_firstname=?, "
			+ "contact_lastname=?, contact_origin=?, contact_userupdate=?, "
			+ "contact_aka=?, contact_title=?, contact_service=?, contact_company=?, contact_comment=?, "
			+ "contact_suffix=?, contact_manager=?, contact_middlename=?, contact_assistant=?, contact_spouse=?, contact_anniversary_id=?, contact_birthday_id=? "
			+ "WHERE contact_id=? ";
		logger.info("modify contact with id=" + c.getUid() + " entityId="
				+ c.getEntityId());

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			EventObmId anniversaryId = createOrUpdateDate(token, con, c, c
					.getAnniversary(), ANNIVERSARY_FIELD);
			c.setAnniversaryId(anniversaryId);

			EventObmId birthdayId = createOrUpdateDate(token, con, c, c
					.getBirthday(), BIRTHDAY_FIELD);
			c.setBirthdayId(birthdayId);

			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setString(idx++, c.getCommonname());
			ps.setString(idx++, c.getFirstname());
			ps.setString(idx++, c.getLastname());
			ps.setString(idx++, token.getOrigin());
			ps.setInt(idx++, token.getObmId());

			ps.setString(idx++, c.getAka());
			ps.setString(idx++, c.getTitle());
			ps.setString(idx++, c.getService());
			ps.setString(idx++, c.getCompany());
			ps.setString(idx++, c.getComment());

			ps.setString(idx++, c.getSuffix());
			ps.setString(idx++, c.getManager());
			ps.setString(idx++, c.getMiddlename());
			ps.setString(idx++, c.getAssistant());
			ps.setString(idx++, c.getSpouse());
			if (c.getAnniversaryId() == null) {
				ps.setNull(idx++, Types.INTEGER);
			} else {
				ps.setInt(idx++, c.getAnniversaryId().getObmId());
			}
			if (c.getBirthdayId() == null) {
				ps.setNull(idx++, Types.INTEGER);
			} else {
				ps.setInt(idx++, c.getBirthdayId().getObmId());
			}

			ps.setInt(idx++, c.getUid());
			ps.executeUpdate();

			createOrUpdateAddresses(con, c.getEntityId(), c.getAddresses());
			createOrUpdateEmails(con, c.getEntityId(), c.getEmails());
			createOrUpdatePhones(con, c.getEntityId(), c.getPhones());
			createOrUpdateWebsites(con, c);
			createOrUpdateIMIdentifiers(con, c.getEntityId(), c.getImIdentifiers());
		} finally {
			obmHelper.cleanup(con, ps, null);
		}

		indexContact(token, c);

		return c;
	}

	public boolean hasRightsOn(AccessToken token, int contactUid) {
		
		String q = "select contact_usercreate=? or a.owner=? "
			+ "or urights.entityright_write=1 or grights.entityright_write=1 or prights.entityright_write=1 "
			+ "FROM Contact "
			+ "INNER JOIN AddressBook a ON a.id=contact_addressbook_id "
			+ "INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id=a.id "
			+ "LEFT JOIN EntityRight urights ON "
			+ "(urights.entityright_entity_id=addressbookentity_entity_id AND "
			+ "urights.entityright_consumer_id=(select userentity_entity_id FROM UserEntity WHERE userentity_user_id=?)) "
			+ "LEFT JOIN EntityRight grights ON grights.entityright_entity_id=addressbookentity_entity_id "
			+ "AND grights.entityright_consumer_id IN ("
			+ MY_GROUPS_QUERY
			+ ") "
			+ "LEFT JOIN EntityRight prights ON prights.entityright_entity_id=addressbookentity_entity_id AND prights.entityright_consumer_id IS NULL "
			+ "WHERE contact_id=?";

		boolean ret = false;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			int idx = 1;

			ps.setInt(idx++, token.getObmId());
			ps.setInt(idx++, token.getObmId());
			ps.setInt(idx++, token.getObmId());
			ps.setInt(idx++, token.getObmId());
			ps.setInt(idx++, contactUid);

			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getBoolean(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	/**
	 * @return the contact with the given id if it is not archived
	 * @throws ContactNotFoundException 
	 * @throws SQLException 
	 */
	public Contact findContact(AccessToken token, int contactId) throws ContactNotFoundException, SQLException {
		String q = "SELECT "
			+ CONTACT_SELECT_FIELDS
			+ ", now() as last_sync FROM Contact, ContactEntity WHERE "
			+ "contact_id = ? AND contactentity_contact_id = contact_id AND contact_archive != 1";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setInt(idx++, contactId);
			rs = ps.executeQuery();

			Contact ret = null;
			if (rs.next()) {
				ret = contactFromCursor(rs);
				Map<Integer, Contact> entityContact = new HashMap<Integer, Contact>();
				entityContact.put(ret.getEntityId(), ret);
				loadPhones(con, entityContact);
				loadIMIdentifiers(con, entityContact);
				loadWebsites(con, entityContact);
				loadAddresses(token, con, entityContact);
				loadEmails(con, entityContact);
				loadBirthday(con, entityContact);
				loadAnniversary(con, entityContact);
			}
			
			if (ret == null) {
				throw new ContactNotFoundException("Contact " + contactId + " not found");
			}
			return ret;
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

	/**
	 * bulk loads all emails of the given entities (contacts)
	 */
	private void loadEmails(Connection con, Map<Integer, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(entityContact.keySet());
		String q = "select email_entity_id, email_label, email_address FROM Email where email_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(rs.getInt(1));
				Email p = new Email(rs.getString(3));
				c.addEmail(rs.getString(2), p);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	private void loadAddresses(AccessToken token, Connection con,
			Map<Integer, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(entityContact.keySet());
		String q = "select address_entity_id, address_label, "
			+ "address_street, address_zipcode, address_expresspostal, address_town, address_country, address_state "
			+ "FROM Address where address_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(rs.getInt(1));
				Address p = new Address(rs.getString(3), rs.getString(4), rs
						.getString(5), rs.getString(6), getCountryName(token,
								con, rs.getString(7)), rs.getString(8));
				c.addAddress(rs.getString(2), p);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	private void loadWebsites(Connection con, Map<Integer, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(entityContact.keySet());
		String q = "select website_entity_id, website_label, website_url FROM Website where website_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(rs.getInt(1));
				String label = rs.getString(2);
				String url = rs.getString(3);
				if (c.getCalUri() ==  null && label.toLowerCase().startsWith("caluri")) {
					c.setCalUri(url);
				}				
				c.addWebsite(new Website(label, url));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	private void loadIMIdentifiers(Connection con,
			Map<Integer, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(entityContact.keySet());
		String q = "select im_entity_id, im_label, im_address, im_protocol FROM IM where im_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(rs.getInt(1));
				InstantMessagingId p = new InstantMessagingId(rs.getString(4),
						rs.getString(3));
				c.addIMIdentifier(rs.getString(2), p);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}
	
	private void loadPhones(Connection con, Map<Integer, Contact> entityContact) {
		IntegerSQLCollectionHelper phoneIds = new IntegerSQLCollectionHelper(entityContact.keySet());
		String q = "select phone_entity_id, phone_label, phone_number FROM Phone where phone_entity_id IN ("
			+ phoneIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			phoneIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(rs.getInt(1));
				Phone p = new Phone(rs.getString(3));
				c.addPhone(rs.getString(2), p);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	private Contact removeContact(AccessToken at, Contact c) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();
			ps = con
			.prepareStatement("UPDATE Contact set contact_archive=1, contact_origin=? WHERE contact_id=?");
			ps.setString(1, at.getOrigin());
			ps.setInt(2, c.getUid());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(con, ps, null);
		}

		removeContactFromSolr(at, c);

		return c;
	}

	private void removeContactFromSolr(AccessToken at, Contact c) {
		try {
			solrHelperFactory.createClient(at).delete(c);
		} catch (Exception e) {
			logger.error("Error indexing contact", e);
		}
	}

	public Contact removeContact(AccessToken at, int uid) throws SQLException, ContactNotFoundException {
		Contact c = findContact(at, uid);
		if (!hasRightsOn(at, uid)) {
			logger.info("contact " + uid + " removal not permitted for "
					+ at.getEmail());
			return c;
		}
		return removeContact(at, c);
	}

	public Set<Integer> findRemovalCandidates(Date d, AccessToken at) throws SQLException {
		String sql = getSelectForFindRemovalCandidates(at);
		return findRemovalCandidates(sql, d);
	}
	
	private Set<Integer> findRemovalCandidates(String sql, Date d) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		Set<Integer> l = new HashSet<Integer>();
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);

			int idx = 1;
			ps.setTimestamp(idx++, new Timestamp(d.getTime()));
			rs = ps.executeQuery();
			
			while (rs.next()) {
				l.add(rs.getInt(1));
			}
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return l;
	}

	/**
	 * Return id of contacts that look similar (used by funambol)
	 */
	public List<String> findContactTwinKeys(AccessToken at, Contact contact) {
		List<String> ret = new LinkedList<String>();

		List<Contact> contacts = searchSimilar(at, contact);
		for (Contact c : contacts) {
			ret.add(c.getUid().toString());
		}
		return ret;
	}

	/**
	 * Return country iso3166 searched by country name
	 */
	private String getCountryIso3166(Connection con, String countryName) {
		if (countryName == null) {
			return null;
		}

		String q = "select country_iso3166 from Country where lower(trim(country_name))=? OR lower(country_iso3166)=?";

		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = null;
		try {
			ps = con.prepareStatement(q);
			ps.setString(1, countryName.toLowerCase());
			ps.setString(2, countryName.toLowerCase());
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return result;
	}

	/**
	 * Return country name searched by country iso3166
	 */
	private String getCountryName(AccessToken token, Connection con,
			String isoCode) {
		if (isoCode == null) {
			return null;
		}
		Map<String, String> cache = token.getIsoCodeToNameCache();
		if (cache.containsKey(isoCode)) {
			return cache.get(isoCode);
		}

		String q = "select country_name from Country where lower(country_lang)="
			+ userLangSelect(token.getObmId())
			+ " AND (lower(trim(country_name))=? OR lower(country_iso3166)=?)";

		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = null;
		try {
			ps = con.prepareStatement(q);
			ps.setString(1, isoCode.toLowerCase());
			ps.setString(2, isoCode.toLowerCase());
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		cache.put(isoCode, result);
		return result;
	}

	private String userLangSelect(int obmId) {
		String q = "(SELECT lower(userobmpref_value) "
			+ "FROM UserObmPref "
			+ "WHERE  userobmpref_option='set_lang' AND (userobmpref_user_id = "
			+ obmId
			+ " OR ( "
			+ "userobmpref_user_id IS NULL AND "
			+ "userobmpref_option  NOT IN (SELECT userobmpref_option FROM UserObmPref WHERE userobmpref_user_id ="
			+ obmId + "))))";
		return q;
	}

	/**
	 * Search contacts that look "similar" to the given contact. Used by Funis
	 * to find duplicates
	 */
	public List<Contact> searchSimilar(AccessToken at, Contact c) {
		String q = "SELECT "
			+ CONTACT_SELECT_FIELDS
			+ ", now() as last_sync FROM Contact "
			+ "INNER JOIN AddressBook a ON a.id=contact_addressbook_id "
			+ "INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id=a.id "
			+ "INNER JOIN ContactEntity ON contactentity_contact_id=contact_id ";

		if (c.getEmails().size() > 0) {
			q += "LEFT JOIN Email ON email_entity_id=contactentity_entity_id ";
		}

		if (c.getPhones().containsKey("CELL;VOICE;X-OBM-Ref1")) {
			q += "LEFT JOIN Phone ON (phone_entity_id=contactentity_entity_id AND phone_label='CELL;VOICE;X-OBM-Ref1') ";
		}

		q += "LEFT JOIN EntityRight urights ON "
			+ "(urights.entityright_entity_id=addressbookentity_entity_id AND "
			+ "urights.entityright_consumer_id=(select userentity_entity_id FROM UserEntity WHERE userentity_user_id=?)) "
			+ "LEFT JOIN EntityRight grights ON grights.entityright_entity_id=addressbookentity_entity_id "
			+ "AND grights.entityright_consumer_id IN ("
			+ MY_GROUPS_QUERY
			+ ") "
			+ "LEFT JOIN EntityRight prights ON prights.entityright_entity_id=addressbookentity_entity_id AND prights.entityright_consumer_id IS NULL "
			+ "WHERE "
			+ "((contact_archive != 1 AND contact_usercreate=?) OR "
			+ "(contact_archive != 1) OR "
			+ "(contact_archive != 1 AND (urights.entityright_read=1 OR grights.entityright_read=1 OR prights.entityright_read=1))) ";

		if (c.getFirstname() != null && c.getFirstname().length() > 0) {
			q += " AND lower(contact_firstname) = ? ";
		}
		if (c.getLastname() != null && c.getLastname().length() > 0) {
			q += " AND lower(contact_lastname) = ? ";
		}

		if (c.getEmails().size() > 0) {
			q += "AND (email_address IS NULL ";
			for (int i = 0; i < c.getEmails().size(); i++) {
				q += " OR lower(email_address) = ? ";
			}
			q += ") ";
		}
		if (c.getPhones().containsKey("CELL;VOICE;X-OBM-Ref1")) {
			q += "AND (phone_number IS NULL OR phone_number=?) ";
		}

		int idx = 1;
		List<Contact> found = new LinkedList<Contact>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			// userentity_user_id
			ps.setInt(idx++, at.getObmId());
			// my groups
			ps.setInt(idx++, at.getObmId());

			ps.setInt(idx++, at.getObmId());

			// values
			if (c.getFirstname() != null && c.getFirstname().length() > 0) {
				ps.setString(idx++, c.getFirstname().toLowerCase());
			}

			if (c.getLastname() != null && c.getLastname().length() > 0) {
				ps.setString(idx++, c.getLastname().toLowerCase());
			}

			for (String s : c.getEmails().keySet()) {
				ps.setString(idx++, c.getEmails().get(s).getEmail()
						.toLowerCase());
			}

			if (c.getPhones().containsKey("CELL;VOICE;X-OBM-Ref1")) {
				ps.setString(idx++, c.getPhones().get("CELL;VOICE;X-OBM-Ref1")
						.getNumber());
			}

			rs = ps.executeQuery();
			Map<Integer, Contact> entityContact = new HashMap<Integer, Contact>();

			while (rs.next()) {
				int entity = rs.getInt("contactentity_entity_id");
				if (!entityContact.containsKey(entity)) {
					Contact ct = contactFromCursor(rs);
					entityContact.put(ct.getEntityId(), ct);
					found.add(ct);
				}
			}
			rs.close();
			rs = null;

			if (!entityContact.isEmpty()) {
				loadPhones(con, entityContact);
				loadIMIdentifiers(con, entityContact);
				loadWebsites(con, entityContact);
				loadAddresses(at, con, entityContact);
				loadEmails(con, entityContact);
				loadBirthday(con, entityContact);
				loadAnniversary(con, entityContact);
			}

		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		logger.info("[" + at.getUser() + "] searchSimilar for '"
				+ c.getLastname() + "' returned " + found.size()
				+ " contact(s)");

		return found;
	}

	public List<AddressBook> findAddressBooks(Connection con, AccessToken at) {
		List<AddressBook> ret = new LinkedList<AddressBook>();
		String q = "SELECT AddressBook.id as uid, "
			+ " AddressBook.name as name"
			+ " FROM AddressBook "
			+ "WHERE AddressBook.owner = ? "
			+ "UNION "
			+ "SELECT AddressBook.id as uid, "
			+ " AddressBook.name as name "
			+ "FROM AddressBook "
			+ "INNER JOIN ( "
			+ "SELECT addressbookentity_addressbook_id FROM  UserEntity "
			+ " INNER JOIN EntityRight ON userentity_entity_id = entityright_consumer_id "
			+ " INNER JOIN AddressbookEntity ON addressbookentity_entity_id = entityright_entity_id "
			+ " WHERE userentity_user_id = ? AND entityright_read = 1 "
			+ " UNION ALL "
			+ " SELECT addressbookentity_addressbook_id FROM EntityRight "
			+ "  INNER JOIN AddressbookEntity ON addressbookentity_entity_id = entityright_entity_id "
			+ " INNER JOIN AddressBook ON addressbookentity_addressbook_id = AddressBook.id "
			+ "  WHERE entityright_consumer_id IS NULL AND entityright_read = 1 AND AddressBook.domain_id = ? "
			+ "  UNION ALL "
			+ "  SELECT addressbookentity_addressbook_id FROM of_usergroup "
			+ "  INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id "
			+ "  INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id "
			+ "  INNER JOIN AddressbookEntity ON addressbookentity_entity_id = entityright_entity_id "
			+ "  WHERE of_usergroup_user_id = ? AND entityright_read = 1 "
			+ ") AS Rights ON AddressBook.id = Rights.addressbookentity_addressbook_id";

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(q);
			int idx = 1;
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getDomainId());
			ps.setInt(idx++, at.getObmId());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(new AddressBook(rs.getString(2), rs.getInt(1), false));
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return ret;
	}

	private List<Contact> searchContact(AccessToken at, List<AddressBook> addrBooks, Connection con, String querys, int limit) 
			throws MalformedURLException, SQLException, LocatorClientException {
		
		List<Contact> ret = new LinkedList<Contact>();
		Set<Integer> evtIds = new HashSet<Integer>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if (addrBooks.size() > 0) {
				SolrHelper solrHelper = solrHelperFactory.createClient(at);
				CommonsHttpSolrServer solrServer = solrHelper.getSolrContact();
				StringBuilder sb = new StringBuilder();
				sb.append("-is:archive ");
				sb.append("+addressbookId:(");
				int idx = 0;
				for (AddressBook book : addrBooks) {
					if (idx > 0) {
						sb.append(" OR ");
					}
					sb.append(book.getUid());
					idx++;
				}
				sb.append(")");
				if (querys != null && !"".equals(querys)) {
					sb.append(" +(displayname:(");
					sb.append(querys.toLowerCase());
					sb.append("*) OR firstname:(");
					sb.append(querys.toLowerCase());
					sb.append("*) OR lastname:(");
					sb.append(querys.toLowerCase());
					sb.append("*) OR email:(");
					sb.append(querys.toLowerCase());
					sb.append("*))");
				}
				SolrQuery params = new SolrQuery();
				params.setQuery(sb.toString());
				params.setIncludeScore(true);
				params.setRows(limit);

				try {
					QueryResponse resp = solrServer.query(params);

					SolrDocumentList results = resp.getResults();
					if (logger.isDebugEnabled()) {
						logger.debug("SOLR query time for " + results.size()
								+ " results: " + resp.getElapsedTime() + "ms.");
					}

					for (int i = 0; i < limit && i < results.size(); i++) {
						SolrDocument doc = results.get(i);
						Map<String, Object> payload = doc.getFieldValueMap();
						evtIds.add((Integer) payload.get("id"));
					}
				} catch (SolrServerException e) {
					logger.error("Error querying server for '" + sb.toString()
							+ " url: "
							+ ClientUtils.toQueryString(params, false), e);
				}
			}

			IntegerSQLCollectionHelper eventIds = new IntegerSQLCollectionHelper(evtIds);
			String q = "SELECT "
				+ CONTACT_SELECT_FIELDS
				+ ", now() as last_sync FROM Contact, ContactEntity WHERE "
				+ "contactentity_contact_id=contact_id AND contact_archive != 1 AND contact_id IN ("
				+ eventIds.asPlaceHolders() + ")";

			ps = con.prepareStatement(q);
			eventIds.insertValues(ps, 1);
			rs = ps.executeQuery();
			Map<Integer, Contact> entityContact = new HashMap<Integer, Contact>();

			int i = 0;
			while (rs.next() && i < limit) {
				int entity = rs.getInt("contactentity_entity_id");
				if (!entityContact.containsKey(entity)) {
					Contact ct = contactFromCursor(rs);
					entityContact.put(ct.getEntityId(), ct);
					ret.add(ct);
					i++;
				}
			}
			rs.close();
			rs = null;

			if (!entityContact.isEmpty()) {
				loadPhones(con, entityContact);
				loadIMIdentifiers(con, entityContact);
				loadWebsites(con, entityContact);
				loadAddresses(at, con, entityContact);
				loadEmails(con, entityContact);
				loadBirthday(con, entityContact);
				loadAnniversary(con, entityContact);
			}

		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return ret;
	}

	/**
	 * Search contacts. Query will match against lastname, firstname & email
	 * prefixes.
	 */
	public List<Contact> searchContact(AccessToken at, String querys, int limit) {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			List<AddressBook> addrBooks = findAddressBooks(con, at);
			return searchContact(at, addrBooks, con, querys, limit);
		} catch (Throwable e1) {
			logger.error(e1.getMessage(), e1);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return new ArrayList<Contact>();
	}

	/**
	 * Search contacts. Query will match against lastname, firstname & email
	 * prefixes.
	 */
	public List<Contact> searchContact(AccessToken at, AddressBook book, String querys, int limit) {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return searchContact(at, Arrays.asList(book), con, querys, limit);
		} catch (Throwable e1) {
			logger.error(e1.getMessage(), e1);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return new ArrayList<Contact>();
	}

	public List<Folder> findUpdatedFolders(Date timestamp, AccessToken at) throws ServerFault {
		String q = "SELECT a.id, a.name, userobm_id, userobm_lastname, userobm_firstname"
			+ " FROM AddressBook a "
			+ " INNER JOIN SyncedAddressbook as s ON (addressbook_id=id AND user_id=?) "
			+ " INNER JOIN UserObm ON (owner=userobm_id) "
			+ "WHERE (a.syncable OR a.name=?) AND "
			+ "(a.timeupdate >= ? OR a.timecreate >= ? OR s.timestamp >= ?)";

		int idx = 1;

		List<Folder> folders = new ArrayList<Folder>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			int userId = at.getObmId();
			ps.setInt(idx++, userId);
			ps.setString(idx++, contactConfiguration.getDefaultAddressBookName());
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				Folder f = new Folder();
				f.setUid(rs.getInt(1));
				f.setName(rs.getString(2));
				if (rs.getInt(3) != userId) {
					String ownerFirstName = rs.getString(4);
					String ownerLastName = rs.getString(5);
					f.setOwnerDisplayName(ownerFirstName + " " + ownerLastName);
				}
				folders.add(f);
			}

		} catch (SQLException se) {
			throw new ServerFault(se.getMessage());
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		logger.info("returning " + folders.size() + " folder(s) updated");

		return folders;
	}

	public Set<Integer> findRemovedFolders(Date d, AccessToken at) throws ServerFault {
		Set<Integer> l = new HashSet<Integer>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		String q = 
			"SELECT addressbook_id FROM DeletedAddressbook WHERE user_id=?  AND timestamp >= ? "
			+ " UNION "
			+ "SELECT addressbook_id FROM DeletedSyncedAddressbook WHERE user_id=? AND timestamp >= ?";

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setInt(idx++, at.getObmId());
			ps.setTimestamp(idx++, new Timestamp(d.getTime()));
			ps.setInt(idx++, at.getObmId());
			ps.setTimestamp(idx++, new Timestamp(d.getTime()));

			rs = ps.executeQuery();
			while (rs.next()) {
				l.add(rs.getInt(1));
			}

			logger.info("Returning " + l.size() + " folder(s) deleted");
		} catch (SQLException e) {
			throw new ServerFault(e.getMessage());
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return l;
	}

	public int markUpdated(int databaseId) throws SQLException {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = obmHelper.getConnection();
			st = con.prepareStatement("update Contact SET contact_timeupdate=? WHERE contact_id=?");
			st.setTimestamp(1, new Timestamp(obmHelper.selectNow(con).getTime()));
			st.setInt(2, databaseId);
			st.execute();
		} finally {
			obmHelper.cleanup(con, st, null);
		}
		return databaseId;
	}

	public boolean unsubscribeBook(AccessToken at, Integer addressBookId) throws SQLException {
		boolean success = unsubscribeBookQuery(at, addressBookId);
		if (success) {
			keepTrackOfDeletedBookSubscription(at, addressBookId);
		}
		return false;
	}

	private boolean unsubscribeBookQuery(AccessToken at, int addressBookId) throws SQLException {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = obmHelper.getConnection();
			st = con.prepareStatement("delete from SyncedAddressbook WHERE addressbook_id=? AND user_id=?");
			st.setInt(1, addressBookId);
			st.setInt(2, at.getObmId());
			return st.executeUpdate() > 0;
		} finally {
			obmHelper.cleanup(con, st, null);
		}
	}

	private boolean keepTrackOfDeletedBookSubscription(AccessToken at, int addressBookId) throws SQLException {
		Connection con = null;
		PreparedStatement st = null;
		try {
			con = obmHelper.getConnection();
			st = con.prepareStatement("INSERT INTO DeletedSyncedAddressbook (addressbook_id, user_id) VALUES (?, ?)");
			st.setInt(1, addressBookId);
			st.setInt(2, at.getObmId());
			return st.executeUpdate() > 0;
		} finally {
			obmHelper.cleanup(con, st, null);
		}
	}

	public ContactUpdates findUpdatedContacts(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException {
		String sql = getSelectForFindUpdatedContacts(addressBookId, token);
		return findUpdatedContacts(sql, lastSync, token);
	}

	public Set<Integer> findRemovalCandidates(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException {
		String sql = getSelectForFindRemovalCandidates(addressBookId, token);
		return findRemovalCandidates(sql, lastSync);
	}
	
}