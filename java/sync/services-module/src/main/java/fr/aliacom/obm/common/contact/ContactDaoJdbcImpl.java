/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
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
import java.util.Collection;
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
import org.obm.annotations.database.AutoTruncate;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.configuration.ContactConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.utils.jdbc.IntegerIndexedSQLCollectionHelper;
import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Folder;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.dao.EntityId;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.solr.SolrHelper;
import org.obm.sync.solr.SolrHelper.Factory;
import org.obm.utils.LinkedEntity;
import org.obm.utils.ObmHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.calendar.CalendarDao;
import fr.aliacom.obm.common.domain.ObmDomain;

/**
 * SQL queries for contact for sync
 */
@Singleton
@AutoTruncate
public class ContactDaoJdbcImpl implements ContactDao {

	private static final Logger logger = LoggerFactory
			.getLogger(ContactDaoJdbcImpl.class);

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
	private final EventExtId.Factory eventExtIdFactory;

	private final Function<EntityId, Integer> entityIdToIntegerFunction = new Function<EntityId, Integer>() {

		@Override
		public Integer apply(EntityId entityId) {
			return entityId.getId();
		}

	};

	@VisibleForTesting
	@Inject
	ContactDaoJdbcImpl(ContactConfiguration contactConfiguration, CalendarDao calendarDao,
			SolrHelper.Factory solrHelperFactory, ObmHelper obmHelper, EventExtId.Factory eventExtIdFactory) {
		this.contactConfiguration = contactConfiguration;
		this.calendarDao = calendarDao;
		this.solrHelperFactory = solrHelperFactory;
		this.obmHelper = obmHelper;
		this.eventExtIdFactory = eventExtIdFactory;
	}

	private String getSelectForFindUpdatedContacts() {
		String sql = "SELECT "
				+ CONTACT_SELECT_FIELDS
				+ ", contact_archive, now() as last_sync FROM Contact"
				+ " INNER JOIN SyncedAddressbook s ON (contact_addressbook_id=s.addressbook_id AND s.user_id=?) "
				+ "INNER JOIN ContactEntity ON contactentity_contact_id=contact_id "
				+ "INNER JOIN UserEntity ON userentity_user_id=? "
				+ "INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id=s.addressbook_id "
				+ "INNER JOIN AddressBook ON id=s.addressbook_id "
				+ "LEFT JOIN EntityRight urights ON urights.entityright_entity_id=addressbookentity_entity_id AND urights.entityright_consumer_id=userentity_entity_id "
				+ "LEFT JOIN EntityRight grights ON grights.entityright_entity_id=addressbookentity_entity_id AND grights.entityright_consumer_id IN (" + MY_GROUPS_QUERY + ") "
				+ "LEFT JOIN EntityRight prights ON prights.entityright_entity_id=addressbookentity_entity_id AND prights.entityright_consumer_id IS NULL "
				+ "WHERE "
				+ "(owner=? OR urights.entityright_read=1 OR grights.entityright_read=1 OR prights.entityright_read=1)";
		sql += " AND (contact_timecreate >= ? OR contact_timeupdate >= ? OR s.timestamp >= ?)";
		return sql;
	}
	
	private String getSelectForFindUpdatedContacts(Integer addressBookId) {
		String sql = getSelectForFindUpdatedContacts();
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
	
	@Override
	public ContactUpdates findUpdatedContacts(Date timestamp, AccessToken at) throws SQLException {
		String sql = getSelectForFindUpdatedContacts();
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
			ps.setInt(idx++, at.getObmId());
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			ps.setTimestamp(idx++, new Timestamp(timestamp.getTime()));
			rs = ps.executeQuery();

			Map<EntityId, Contact> entityContact = new HashMap<EntityId, Contact>();
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

	private void loadBirthday(Connection con, Map<EntityId, Contact> entityContact) {
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

	private void loadAnniversary(Connection con, Map<EntityId, Contact> entityContact) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Set<EventObmId> bdayIds = new HashSet<EventObmId>();
		HashMap<EventObmId, Contact> eventIdMap = new HashMap<EventObmId, Contact>();
		for (Contact c : entityContact.values()) {
			if (c.getAnniversaryId() != null) {
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
		c.setEntityId(EntityId.valueOf(rs.getInt(4)));
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

	@AutoTruncate
	protected Contact createContactInAddressBook(Connection con, AccessToken at, @DatabaseEntity Contact c, int addressBookId) 
			throws ServerFault, SQLException {
		
		try {
			EventObmId anniversaryId = createOrUpdateDate(at, con, c, c.getAnniversary(), ANNIVERSARY_FIELD);
			c.setAnniversaryId(anniversaryId);

			EventObmId birthdayId = createOrUpdateDate(at, con, c, c.getBirthday(), BIRTHDAY_FIELD);
			c.setBirthdayId(birthdayId);

			int contactId = insertIntoContact(con, at, c, addressBookId);
			LinkedEntity le = obmHelper.linkEntity(con, "ContactEntity", "contact_id", contactId);
			c.setEntityId(le.getEntityId());

			createOrUpdatePhones(con, c.getEntityId(), c.getPhones());
			createOrUpdateAddresses(con, c.getEntityId(), c.getAddresses());
			createOrUpdateEmails(con, c.getEntityId(), c.getEmails());
			createOrUpdateWebsites(con, c);
			createOrUpdateIMIdentifiers(con, c.getEntityId(), c.getImIdentifiers());
			c.setUid(contactId);
		} catch (FindException e) {
			throw new ServerFault(e.getMessage());
		} catch (EventNotFoundException e) {
			throw new ServerFault(e.getMessage());
		}
		indexContact(at, c);
		return c;
	}

	private void indexContact(AccessToken at, Contact c) throws ServerFault {
		try {
			// no need to pass the sql connection as indexing will be done in a
			// separate thread
			solrHelperFactory.createClient(at).createOrUpdate(c);
		} catch (Exception e) {
			throw new ServerFault("Indexing server is unavailable", e);
		}
	}

	@Override
	public Contact createContact(AccessToken at, Connection con, Contact c) throws SQLException, ServerFault {
		int addressbookId = chooseAddressBookFromContact(con, at, c);
		return createContactInAddressBook(con, at, c, addressbookId);
	}

	@Override
	public Contact createContactInAddressBook(AccessToken at, Contact c, int addressbookId) throws SQLException, ServerFault {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			c = createContactInAddressBook(con, at, c, addressbookId);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return c;
	}
	
	@Override
	public Contact createCollectedContact(String name, String email, ObmDomain domain, Integer ownerId) throws ServerFault, SQLException {
		Contact c = new Contact();
		
		c.setLastname(name);
		c.setFirstname("");
		c.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain(email));
		c.setCollected(true);
		
		logger.info("Attendee {} not found in OBM, will create a contact.", email);
		
		return createContact(c, domain, ownerId);
	}
	
	private Contact createContact(Contact c, ObmDomain domain, Integer ownerId) throws SQLException, ServerFault {
		Connection con = null;
		
		try {
			con = obmHelper.getConnection();
			
			int addressbookId = chooseAddressBookFromContact(con, ownerId, c);
			AccessToken token = new AccessToken(ownerId, "automatically-collected");
			
			token.setDomain(domain);
			
			return createContactInAddressBook(con, token, c, addressbookId);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}

	private Event getEvent(AccessToken token, String displayName, Date startDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		cal.setTime(startDate);

		Event e = new Event();
		
		e.setExtId(eventExtIdFactory.generate());
		e.setTitle(displayName);
		e.setStartDate(cal.getTime());
		e.setDuration(3600);
		e.setAllday(true);
		EventRecurrence rec = new EventRecurrence();
		rec.setDays(new RecurrenceDays());
		rec.setFrequence(1);
		rec.setKind(RecurrenceKind.yearly);
		rec.setEnd(null);
		e.setRecurrence(rec);
		e.setPrivacy(EventPrivacy.PRIVATE);
		e.setPriority(1);
		
		Attendee at = UserAttendee
				.builder()
				.email(token.getUserEmail())
				.participationRole(ParticipationRole.CHAIR)
				.participation(Participation.accepted())
				.build();
		
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

	private EventObmId createOrUpdateDate(AccessToken at, Connection con, Contact c, Date date, String idField)
			throws SQLException, FindException, EventNotFoundException, ServerFault {
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


	private EventObmId retrieveAndModifyEventForContactDate(AccessToken at, Connection con, Date date, EventObmId dateId)
			throws EventNotFoundException, ServerFault, SQLException, FindException {
		logger.info("eventId != null");
		Event e = calendarDao.findEventById(at, dateId);
		e.setStartDate(date);
		calendarDao.modifyEvent(con, at, at.getUserWithDomain(), e, false, true);
		return e.getObmId();
	}


	private EventObmId createEventForContactDate(AccessToken at, Connection con, Contact c, Date date)
			throws SQLException,
			FindException, ServerFault {
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

	private void createOrUpdateIMIdentifiers(Connection con, EntityId entityId, Map<String, InstantMessagingId> imIdentifiers)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper imIds = new StringSQLCollectionHelper(imIdentifiers.keySet());
			ps = con.prepareStatement(
				"DELETE FROM IM WHERE im_entity_id=? AND im_label IN (" + imIds.asPlaceHolders() + ")");
			ps.setInt(1, entityId.getId());
			imIds.insertValues(ps, 2);
			ps.executeUpdate();
			ps.close();

			ps = insertImIdenfifiers(con, entityId, imIdentifiers);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void removeAndCreateIMIdentifiers(Connection con, EntityId entityId, Map<String, InstantMessagingId> imIdentifiers)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
				"DELETE FROM IM " +
				"WHERE im_entity_id=?");
			ps.setInt(1, entityId.getId());
			ps.executeUpdate();
			ps.close();
			
			ps = insertImIdenfifiers(con, entityId, imIdentifiers);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private PreparedStatement insertImIdenfifiers(Connection con, EntityId entityId, Map<String, InstantMessagingId> imIdentifiers)
			throws SQLException {
		PreparedStatement ps;
		ps = con.prepareStatement(
			"INSERT INTO IM (im_entity_id, im_label, im_protocol, im_address) " +
			"VALUES (?, ?, ?, ?)");
		for (Entry<String, InstantMessagingId> entry: imIdentifiers.entrySet()) {
			ps.setInt(1, entityId.getId());
			ps.setString(2, entry.getKey());
			ps.setString(3, entry.getValue().getProtocol());
			ps.setString(4, entry.getValue().getId());
			ps.addBatch();
		}
		ps.executeBatch();
		return ps;
	}

	private void createOrUpdateWebsites(final Connection con, final Contact contact) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(contact.listWebSitesLabel());
			ps = con.prepareStatement(
				"DELETE FROM Website " +
				"WHERE website_entity_id=? AND website_label IN (" + labels.asPlaceHolders() + ")");
			ps.setInt(1, contact.getEntityId().getId());
			labels.insertValues(ps, 2);
			ps.executeUpdate();

			insertWebSites(con, contact);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void removeAndCreateWebsites(final Connection con, final Contact contact) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
				"DELETE FROM Website " +
				"WHERE website_entity_id=?");
			ps.setInt(1, contact.getEntityId().getId());
			ps.executeUpdate();

			insertWebSites(con, contact);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void insertWebSites(final Connection con, final Contact contact)
			throws SQLException {
		final String label = "CALURI;X-OBM-Ref1";
		insertWebSite(con, contact.getEntityId(), label, contact.getCalUri());
		for (final Website website: contact.getWebsites()) {
			if (!website.isCalendarUrl() || !website.getLabel().equalsIgnoreCase(label)) {
				insertWebSite(con, contact.getEntityId(), website.getLabel(),  website.getUrl());
			}
		}
	}

	private void insertWebSite(Connection con, EntityId entityId, String label, String url) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO Website (website_entity_id, website_label, website_url) VALUES (?, ?, ?)");
			if (!StringUtils.isEmpty(url)) {
				ps.setInt(1, entityId.getId());
				ps.setString(2, label);
				ps.setString(3, url);
				ps.executeUpdate();
			}
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void createOrUpdateAddresses(Connection con, EntityId entityId, Map<String, Address> addresses)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(addresses.keySet());
			ps = con.prepareStatement(
				"DELETE FROM Address " +
				"WHERE address_entity_id=? and address_label IN (" + labels.asPlaceHolders() + ")");
			ps.setInt(1, entityId.getId());
			labels.insertValues(ps, 2);
			ps.executeUpdate();
			ps.close();

			ps = insertAddresses(con, entityId, addresses);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void removeAndCreateAddresses(Connection con, EntityId entityId, Map<String, Address> addresses)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con
			.prepareStatement(
				"DELETE FROM Address " +
				"WHERE address_entity_id=?");
			ps.setInt(1, entityId.getId());
			ps.executeUpdate();
			ps.close();

			ps = insertAddresses(con, entityId, addresses);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private PreparedStatement insertAddresses(Connection con, EntityId entityId, Map<String, Address> addresses)
			throws SQLException {
		PreparedStatement ps;
		ps = con.prepareStatement(
				"INSERT INTO Address (address_entity_id, address_label, "
				+ "address_street, address_zipcode, address_town,"
				+ "address_expresspostal, address_country, address_state) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		for (Entry<String, Address> entry: addresses.entrySet()) {
			Address ad = entry.getValue();
			ps.setInt(1, entityId.getId());
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
		return ps;
	}

	private void createOrUpdateEmails(Connection con, EntityId entityId, Map<String, EmailAddress> emails)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper emailStrings = new StringSQLCollectionHelper(emails.keySet());
			ps = con.prepareStatement(
				"DELETE FROM Email " +
				"WHERE email_entity_id=? AND email_label IN (" + emailStrings.asPlaceHolders() + ")");
			ps.setInt(1, entityId.getId());
			emailStrings.insertValues(ps, 2);
			ps.executeUpdate();
			ps.close();

			ps = insertEmails(con, entityId, emails);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private void removeAndCreateEmails(Connection con, EntityId entityId, Map<String, EmailAddress> emails)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
				"DELETE FROM Email " +
				"WHERE email_entity_id=?");
			ps.setInt(1, entityId.getId());
			ps.executeUpdate();
			ps.close();

			ps = insertEmails(con, entityId, emails);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private PreparedStatement insertEmails(Connection con, EntityId entityId, Map<String, EmailAddress> emails)
			throws SQLException {
		PreparedStatement ps;
		ps = con.prepareStatement(
			"INSERT INTO Email (email_entity_id, email_label, email_address) " +
			"VALUES (?, ?, ?)");
		for (Entry<String, EmailAddress> entry: emails.entrySet()) {
			ps.setInt(1, entityId.getId());
			ps.setString(2, entry.getKey());
			ps.setString(3, entry.getValue().get());
			ps.addBatch();
		}
		ps.executeBatch();
		return ps;
	}

	private void createOrUpdatePhones(Connection con, EntityId entityId, Map<String, Phone> phones)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			StringSQLCollectionHelper labels = new StringSQLCollectionHelper(phones.keySet());
			ps = con.prepareStatement(
				"DELETE FROM Phone " +
				"WHERE phone_entity_id=? and phone_label IN (" + labels.asPlaceHolders() + ")");
			ps.setInt(1, entityId.getId());
			labels.insertValues(ps, 2);
			ps.executeUpdate();
			ps.close();

			ps = insertPhones(con, entityId, phones);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private void removeAndCreatePhones(Connection con, EntityId entityId, Map<String, Phone> phones)
			throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
				"DELETE FROM Phone " +
				"WHERE phone_entity_id=?");
			ps.setInt(1, entityId.getId());
			ps.executeUpdate();
			ps.close();

			ps = insertPhones(con, entityId, phones);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}

	}

	private PreparedStatement insertPhones(Connection con, EntityId entityId, Map<String, Phone> phones)
			throws SQLException {
		PreparedStatement ps;
		ps = con.prepareStatement(
			"INSERT INTO Phone (phone_entity_id, phone_label, phone_number) " +
			"VALUES (?, ?, ?)");
		for (Entry<String, Phone> entry: phones.entrySet()) {
			ps.setInt(1, entityId.getId());
			ps.setString(2, entry.getKey());
			ps.setString(3, entry.getValue().getNumber());
			ps.addBatch();
		}
		ps.executeBatch();
		return ps;
	}

	
	private int chooseAddressBookFromContact(Connection con, AccessToken at, Contact c) throws SQLException {
		return chooseAddressBookFromContact(con, at.getObmId(), c);
	}
	
	private int chooseAddressBookFromContact(Connection con, Integer ownerId, Contact c) throws SQLException {
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
			ps.setInt(2, ownerId);

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
			ps.setInt(idx++, at.getDomain().getId());
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

	@Override
	@AutoTruncate
	public Contact modifyContact(AccessToken token, @DatabaseEntity Contact c) throws SQLException, FindException, EventNotFoundException, ServerFault {

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

	@Override
	@AutoTruncate
	public Contact updateContact(AccessToken token, @DatabaseEntity Contact contact) throws SQLException, FindException, EventNotFoundException, ServerFault {

		String q = "UPDATE Contact SET "
			+ "contact_commonname=?, contact_firstname=?, "
			+ "contact_lastname=?, contact_origin=?, contact_userupdate=?, "
			+ "contact_aka=?, contact_title=?, contact_service=?, contact_company=?, contact_comment=?, "
			+ "contact_suffix=?, contact_manager=?, contact_middlename=?, contact_assistant=?, contact_spouse=?, contact_anniversary_id=?, contact_birthday_id=? "
			+ "WHERE contact_id=? ";
		logger.info("update contact with id=" + contact.getUid() + " entityId=" + contact.getEntityId());

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			EventObmId anniversaryId = createOrUpdateDate(token, con, contact, contact.getAnniversary(), ANNIVERSARY_FIELD);
			contact.setAnniversaryId(anniversaryId);

			EventObmId birthdayId = createOrUpdateDate(token, con, contact, contact.getBirthday(), BIRTHDAY_FIELD);
			contact.setBirthdayId(birthdayId);

			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setString(idx++, contact.getCommonname());
			ps.setString(idx++, contact.getFirstname());
			ps.setString(idx++, contact.getLastname());
			ps.setString(idx++, token.getOrigin());
			ps.setInt(idx++, token.getObmId());

			ps.setString(idx++, contact.getAka());
			ps.setString(idx++, contact.getTitle());
			ps.setString(idx++, contact.getService());
			ps.setString(idx++, contact.getCompany());
			ps.setString(idx++, contact.getComment());

			ps.setString(idx++, contact.getSuffix());
			ps.setString(idx++, contact.getManager());
			ps.setString(idx++, contact.getMiddlename());
			ps.setString(idx++, contact.getAssistant());
			ps.setString(idx++, contact.getSpouse());
			if (contact.getAnniversaryId() == null) {
				ps.setNull(idx++, Types.INTEGER);
			} else {
				ps.setInt(idx++, contact.getAnniversaryId().getObmId());
			}
			if (contact.getBirthdayId() == null) {
				ps.setNull(idx++, Types.INTEGER);
			} else {
				ps.setInt(idx++, contact.getBirthdayId().getObmId());
			}

			ps.setInt(idx++, contact.getUid());
			ps.executeUpdate();

			removeAndCreateAddresses(con, contact.getEntityId(), contact.getAddresses());
			removeAndCreateEmails(con, contact.getEntityId(), contact.getEmails());
			removeAndCreatePhones(con, contact.getEntityId(), contact.getPhones());
			removeAndCreateWebsites(con, contact);
			removeAndCreateIMIdentifiers(con, contact.getEntityId(), contact.getImIdentifiers());
		} finally {
			obmHelper.cleanup(con, ps, null);
		}

		indexContact(token, contact);

		return contact;
	}
	
	@Override
	public boolean hasRightsOnAddressBook(AccessToken token, int addressBookId) {
		String q = "SELECT owner = ? or ur.entityright_write = 1 or gr.entityright_write = 1 or pr.entityright_write = 1 " +
				"FROM AddressBook " +
				"INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id = id " +
				"LEFT JOIN EntityRight ur ON ur.entityright_entity_id = addressbookentity_entity_id AND ur.entityright_consumer_id = (SELECT userentity_entity_id FROM UserEntity WHERE userentity_user_id = ?) " +
				"LEFT JOIN EntityRight gr ON gr.entityright_entity_id = addressbookentity_entity_id AND gr.entityright_consumer_id IN (" + MY_GROUPS_QUERY + ") " +
				"LEFT JOIN EntityRight pr ON pr.entityright_entity_id = addressbookentity_entity_id AND pr.entityright_consumer_id IS NULL " +
				"WHERE id = ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			ps.setInt(1, token.getObmId());
			ps.setInt(2, token.getObmId());
			ps.setInt(3, token.getObmId());
			ps.setInt(4, addressBookId);

			rs = ps.executeQuery();

			return rs.next() && rs.getBoolean(1);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return false;
	}

	/**
	 * @return the contact with the given id if it is not archived
	 * @throws ContactNotFoundException 
	 * @throws SQLException 
	 */
	@Override
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
				Map<EntityId, Contact> entityContact = new HashMap<EntityId, Contact>();
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
	
	@Override
	public Contact findAttendeeContactFromEmailForUser(String email, Integer userId) throws SQLException {
		String q = "SELECT contact_id, contactentity_entity_id, contact_firstname, contact_lastname, contact_commonname, email_label "
				+ "FROM Contact "
				+ "INNER JOIN ContactEntity ON contactentity_contact_id = contact_id "
				+ "INNER JOIN Email ON email_entity_id = contactentity_entity_id "
				+ "INNER JOIN AddressBook ON id = contact_addressbook_id "
				+ "INNER JOIN AddressbookEntity ON addressbookentity_addressbook_id = id "
				+ "INNER JOIN UserEntity ON userentity_user_id = ? "
				+ "LEFT JOIN EntityRight urights ON (urights.entityright_entity_id = addressbookentity_entity_id AND urights.entityright_consumer_id = userentity_entity_id) "
				+ "LEFT JOIN EntityRight grights ON (grights.entityright_entity_id = addressbookentity_entity_id AND grights.entityright_consumer_id IN (" + MY_GROUPS_QUERY + ")) "
				+ "LEFT JOIN EntityRight prights ON (prights.entityright_entity_id = addressbookentity_entity_id AND prights.entityright_consumer_id IS NULL) "
				+ "WHERE email_address = ? AND contact_archive != 1 AND (contact_usercreate = ? OR owner = ? OR urights.entityright_access = 1 or grights.entityright_access = 1 or prights.entityright_access = 1)";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			ps.setInt(1, userId);
			ps.setInt(2, userId);
			ps.setString(3, email);
			ps.setInt(4, userId);
			ps.setInt(5, userId);
			rs = ps.executeQuery();

			if (rs.next()) {
				Contact contact = new Contact();
				
				contact.setUid(rs.getInt("contact_id"));
				contact.setEntityId(EntityId.valueOf(rs.getInt("contactentity_entity_id")));
				contact.addEmail(rs.getString("email_label"), EmailAddress.loginAtDomain(email));
				contact.setCommonname(rs.getString("contact_commonname"));
				contact.setFirstname(rs.getString("contact_firstname"));
				contact.setLastname(rs.getString("contact_lastname"));
				
				return contact;
			}
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		
		return null;
	}

	/**
	 * bulk loads all emails of the given entities (contacts)
	 */
	private void loadEmails(Connection con, Map<EntityId, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(Collections2.transform(entityContact.keySet(), entityIdToIntegerFunction));
		String q = "select email_entity_id, email_label, email_address FROM Email where email_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				loadEmailInContact(entityContact, rs);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	@VisibleForTesting void loadEmailInContact(Map<EntityId, Contact> entityContact, ResultSet rs) throws SQLException {
		Contact contact = entityContact.get(EntityId.valueOf(rs.getInt(1)));
		String contactEmailLabel = rs.getString(2);
		String contactEmail = rs.getString(3);
		if (!Strings.isNullOrEmpty(contactEmailLabel) && EmailAddress.isEmailAddress(contactEmail)) {
			contact.addEmail(contactEmailLabel, EmailAddress.loginAtDomain(contactEmail));
		} else {
			logger.warn("An email field has been ignored for contact:{}, the field is not valid:{} -> {}",
					new Object[]{contact.getEntityId(), contactEmailLabel, contactEmail});
		}
	}

	private void loadAddresses(AccessToken token, Connection con,
			Map<EntityId, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(Collections2.transform(entityContact.keySet(), entityIdToIntegerFunction));
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
				Contact c = entityContact.get(EntityId.valueOf(rs.getInt(1)));
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

	private void loadWebsites(Connection con, Map<EntityId, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(Collections2.transform(entityContact.keySet(), entityIdToIntegerFunction));
		String q = "select website_entity_id, website_label, website_url FROM Website where website_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(EntityId.valueOf(rs.getInt(1)));
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
			Map<EntityId, Contact> entityContact) {
		IntegerSQLCollectionHelper contactIds = new IntegerSQLCollectionHelper(Collections2.transform(entityContact.keySet(), entityIdToIntegerFunction));
		String q = "select im_entity_id, im_label, im_address, im_protocol FROM IM where im_entity_id IN ("
			+ contactIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			contactIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(EntityId.valueOf(rs.getInt(1)));
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
	
	private void loadPhones(Connection con, Map<EntityId, Contact> entityContact) {
		IntegerSQLCollectionHelper phoneIds = new IntegerSQLCollectionHelper(Collections2.transform(entityContact.keySet(), entityIdToIntegerFunction));
		String q = "select phone_entity_id, phone_label, phone_number FROM Phone where phone_entity_id IN ("
			+ phoneIds.asPlaceHolders() + ")";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			phoneIds.insertValues(st, 1);
			rs = st.executeQuery();
			while (rs.next()) {
				Contact c = entityContact.get(EntityId.valueOf(rs.getInt(1)));
				Phone p = new Phone(rs.getString(3));
				c.addPhone(rs.getString(2), p);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
	}

	@Override
	public Contact removeContact(AccessToken at, Contact c) throws ServerFault, SQLException {
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

	private void removeContactFromSolr(AccessToken at, Contact c) throws ServerFault {
		try {
			solrHelperFactory.createClient(at).delete(c);
		} catch (Exception e) {
			throw new ServerFault("Indexing server is unavailable", e);
		}
	}

	@Override
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

	@Override
	public List<AddressBook> findAddressBooks(AccessToken at) throws SQLException {
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

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			int idx = 1;
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getDomain().getId());
			ps.setInt(idx++, at.getObmId());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(AddressBook
						.builder()
						.uid(AddressBook.Id.valueOf(rs.getInt(1)))
						.name(rs.getString(2))
						.readOnly(false)
						.build());
			}
			return ret;
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

	private List<Contact> searchContact(AccessToken at, Collection<AddressBook> addrBooks, Connection con, String query, int limit, Integer offset) 
			throws MalformedURLException, SQLException, LocatorClientException {
		
		Set<Integer> contactIds = new HashSet<Integer>();

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
			if (query != null && !"".equals(query)) {
				sb.append(" +(displayname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR firstname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR lastname:(");
				sb.append(query.toLowerCase());
				sb.append("*) OR email:(");
				sb.append(query.toLowerCase());
				sb.append("*))");
			}
			SolrQuery params = new SolrQuery();
			params.setQuery(sb.toString());
			params.setIncludeScore(true);
			params.setRows(limit);
			params.setStart(offset);

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
					contactIds.add((Integer) payload.get("id"));
				}
			} catch (SolrServerException e) {
				logger.error("Error querying server for '" + sb.toString()
						+ " url: "
						+ ClientUtils.toQueryString(params, false), e);
			}
		}

		ContactResults contactResults = loadContactsFromDB(contactIds, con, limit);

		if (!contactResults.contactMap.isEmpty()) {
			loadPhones(con, contactResults.contactMap);
			loadIMIdentifiers(con, contactResults.contactMap);
			loadWebsites(con, contactResults.contactMap);
			loadAddresses(at, con, contactResults.contactMap);
			loadEmails(con, contactResults.contactMap);
			loadBirthday(con, contactResults.contactMap);
			loadAnniversary(con, contactResults.contactMap);
		}

		return contactResults.contactList;
	}

	@VisibleForTesting
	ContactResults loadContactsFromDB(Set<Integer> contactIds, Connection con, int limit) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			IntegerSQLCollectionHelper contactIdsHelper = new IntegerSQLCollectionHelper(contactIds);
			String q = "SELECT "
				+ CONTACT_SELECT_FIELDS
				+ ", now() as last_sync FROM Contact, ContactEntity WHERE "
				+ "contactentity_contact_id=contact_id AND contact_archive != 1 AND contact_id IN ("
				+ contactIdsHelper.asPlaceHolders() + ") ORDER BY contact_lastname";

			ps = con.prepareStatement(q);
			contactIdsHelper.insertValues(ps, 1);
			rs = ps.executeQuery();
			Map<EntityId, Contact> entityIdToContact = Maps.newHashMap();
			List<Contact> contacts = Lists.newLinkedList();

			int i = 0;
			while (rs.next() && i < limit) {
				int entity = rs.getInt("contactentity_entity_id");
				if (!entityIdToContact.containsKey(EntityId.valueOf(entity))) {
					Contact ct = contactFromCursor(rs);
					entityIdToContact.put(ct.getEntityId(), ct);
					contacts.add(ct);
					i++;
				}
			}
			rs.close();
			return new ContactResults(contacts, entityIdToContact);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	@Override
	public List<Contact> searchContactsInAddressBooksList(AccessToken at, Collection<AddressBook> addrBooks, String query, int limit, Integer offset) 
			throws MalformedURLException, LocatorClientException, SQLException {
		
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return searchContact(at, addrBooks, con, query, limit, offset);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}

	/**
	 * Search contacts. Query will match against lastname, firstname & email
	 * prefixes.
	 */
	@Override
	public List<Contact> searchContact(AccessToken at, AddressBook book, String query, int limit, Integer offset) {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return searchContact(at, Arrays.asList(book), con, query, limit, offset);
		} catch (Throwable e1) {
			logger.error(e1.getMessage(), e1);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return new ArrayList<Contact>();
	}

	@Override
	public Set<Folder> findUpdatedFolders(Date timestamp, AccessToken at) throws SQLException {
		String q = "SELECT a.id, a.name, userobm_id, userobm_lastname, userobm_firstname, userobm_login, domain.domain_name"
			+ " FROM AddressBook a "
			+ " INNER JOIN SyncedAddressbook as s ON (addressbook_id=id AND user_id=?) "
			+ " INNER JOIN UserObm ON (owner=userobm_id) "
			+ " INNER JOIN Domain as domain ON (userobm_domain_id=domain.domain_id) "
			+ "WHERE (a.syncable OR a.name=?) AND "
			+ "(a.timeupdate >= ? OR a.timecreate >= ? OR s.timestamp >= ?)";

		int idx = 1;

		Set<Folder> folders = new HashSet<Folder>();
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
				Folder f = buildFolder(at, rs);
				folders.add(f);
			}

		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		logger.info("returning " + folders.size() + " folder(s) updated");

		return folders;
	}

	@Override
	public Set<Folder> findRemovedFolders(Date date, AccessToken at) throws SQLException {
		Set<Folder> folders = new HashSet<Folder>();
		folders.addAll(listDeletedAddressbook(date, at));
		folders.addAll(listDeletedSyncedAddressbook(date, at));
		return folders;
	}

	private Set<Folder> listDeletedAddressbook(Date date, AccessToken at) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		
		String sql = "SELECT id, name, userobm_id, userobm_lastname, userobm_firstname, userobm_login, domain.domain_name" +
				   " FROM AddressBook" +
				   " INNER JOIN DeletedAddressbook ON (addressbook_id = id)" +
				   " INNER JOIN UserObm ON (owner = userobm_id)" +
				   " INNER JOIN Domain as domain ON (userobm_domain_id=domain.domain_id) " +
				   " WHERE user_id = ? AND timestamp >= ?";
		
		Set<Folder> folders = new HashSet<Folder>();
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);
			ps.setInt(1, at.getObmId());
			ps.setTimestamp(2, new Timestamp(date.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				Folder f = buildFolder(at, rs);
				folders.add(f);
			}
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return folders;
	}


	private Set<Folder> listDeletedSyncedAddressbook(Date date, AccessToken at) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		
		String sql = "SELECT id, name, userobm_id, userobm_lastname, userobm_firstname, userobm_login, domain.domain_name" +
				   " FROM AddressBook" +
				   " INNER JOIN DeletedSyncedAddressbook ON (addressbook_id = id)" +
				   " INNER JOIN UserObm ON (owner = userobm_id)" +
				   " INNER JOIN Domain as domain ON (userobm_domain_id=domain.domain_id) " +
				   " WHERE user_id = ? AND timestamp >= ?";
		
		Set<Folder> folders = new HashSet<Folder>();
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);
			ps.setInt(1, at.getObmId());
			ps.setTimestamp(2, new Timestamp(date.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				Folder f = buildFolder(at, rs);
				folders.add(f);
			}
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return folders;
	}
	
	private Folder buildFolder(AccessToken at, ResultSet rs) throws SQLException {
		Folder.Builder folderBuilder = Folder.builder()
				.uid(rs.getInt(1))
				.name(rs.getString(2))
				.ownerLoginAtDomain(rs.getString(6) + "@" + rs.getString(7));
		if (rs.getInt(3) != at.getObmId()) {
			String ownerFirstName = rs.getString(4);
			String ownerLastName = rs.getString(5);
			folderBuilder.ownerDisplayName(ownerFirstName + " " + ownerLastName);
		}
		return folderBuilder.build();
	}

	@Override
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

	@Override
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

	@Override
	public ContactUpdates findUpdatedContacts(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException {
		String sql = getSelectForFindUpdatedContacts(addressBookId);
		return findUpdatedContacts(sql, lastSync, token);
	}

	@Override
	public Set<Integer> findRemovalCandidates(Date lastSync, Integer addressBookId, AccessToken token) throws SQLException {
		String sql = getSelectForFindRemovalCandidates(addressBookId, token);
		return findRemovalCandidates(sql, lastSync);
	}

	@Override
	public Collection<AddressBook> listSynchronizedAddressBooks(AccessToken token) throws SQLException {
		String q = "SELECT a.id, a.name, userobm_id, userobm_lastname, userobm_firstname"
				+ " FROM AddressBook a"
				+ " INNER JOIN SyncedAddressbook as s ON (addressbook_id=id AND user_id=?)"
				+ " INNER JOIN UserObm ON (owner=userobm_id)"
				+ " WHERE (a.syncable OR a.name=?)";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			int idx = 1;
			ps.setInt(idx++, token.getObmId());
			ps.setString(idx++, contactConfiguration.getDefaultAddressBookName());
			rs = ps.executeQuery();

			Set<AddressBook> listAddressBooks = new HashSet<AddressBook>();
			while (rs.next()) {
				listAddressBooks.add(AddressBook
						.builder()
						.uid(AddressBook.Id.valueOf(rs.getInt(1)))
						.name(rs.getString(2))
						.readOnly(false)
						.build());
			}
			return listAddressBooks;
			
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}
	
	@Override
	public int countContactsInGroup(int gid) throws SQLException {
		String query = "SELECT COUNT(*) "
					+  "FROM Contact "
					+  "WHERE contact_addressbook_id = ? "
					+  "AND contact_archive != 1 ";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, gid);
			rs = ps.executeQuery();
			return rs.next() ? rs.getInt(1) : 0;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

	@VisibleForTesting
	static class ContactResults {
		public List<Contact> contactList;
		public Map<EntityId, Contact> contactMap;
		
		public ContactResults(List<Contact> contactList, Map<EntityId, Contact> contactMap) {
			this.contactList = contactList;
			this.contactMap = contactMap;
		}
	}
}
