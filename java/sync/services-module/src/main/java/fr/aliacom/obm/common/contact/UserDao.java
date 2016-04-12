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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obm.configuration.ContactConfiguration;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.ContactLabel;
import org.obm.sync.book.DeletedContact;
import org.obm.sync.book.Phone;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.utils.ObmHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

	private static final String USEROBM_ALL_JOBS = "userobm_company, userobm_service, userobm_direction, userobm_title, userobm_description";
	private static final String USEROBM_ALL_PHONES = "userobm_phone, userobm_phone2, userobm_mobile, userobm_fax, userobm_fax2";
	private static final String USEROBM_ALL_ADDRESSES = "userobm_address1, userobm_address2, userobm_address3, userobm_zipcode, "
			+ "userobm_town, userobm_expresspostal";
	
	private static final Map<ContactLabel, String> contactLabelForUserObmField = 
			ImmutableMap.<ContactLabel, String>builder()
				.put(ContactLabel.PHONE, "userobm_phone")
				.put(ContactLabel.PHONE2, "userobm_phone2")
				.put(ContactLabel.MOBILE, "userobm_mobile")
				.put(ContactLabel.FAX, "userobm_fax")
				.put(ContactLabel.FAX2, "userobm_fax2")
				.build();
	
	
	private final ObmHelper obmHelper;
	private final ContactConfiguration contactConfiguration;
	
	@Inject
	@VisibleForTesting UserDao(ContactConfiguration contactConfiguration, ObmHelper obmHelper) {
		this.contactConfiguration = contactConfiguration;
		this.obmHelper = obmHelper;
	}

	/**
	 * Find updated user since timestamp
	 * @throws SQLException 
	 */
	public ContactUpdates findUpdatedUsers(Date timestamp, AccessToken at) throws SQLException {
		ContactUpdates cu = new ContactUpdates();

		String q = "SELECT userobm_id, userobm_login, userobm_firstname, userobm_lastname, "
				+ "userobm_email, userobm_commonname, "
				+ Joiner.on(", ").join(USEROBM_ALL_JOBS, USEROBM_ALL_PHONES, USEROBM_ALL_ADDRESSES) + " "
				+ "FROM UserObm "
				+ "WHERE userobm_archive != 1 and userobm_domain_id=? and userobm_hidden != 1 ";
		if (timestamp != null) {
			q += " and (userobm_timecreate >= ? or userobm_timeupdate >= ? )";
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			List<Contact> contacts = new ArrayList<Contact>();
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			
			ps.setInt(1, at.getDomain().getId());
			if (timestamp != null) {
				ps.setTimestamp(2, new Timestamp(timestamp.getTime()));
				ps.setTimestamp(3, new Timestamp(timestamp.getTime()));
			}
			
			rs = ps.executeQuery();
			while (rs.next()) {
				contacts.add(userAsContact(rs, at));
			}
			rs.close();
			rs = null;
			
			cu.setContacts(contacts);
			
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return cu;
	}

	@VisibleForTesting Contact userAsContact(ResultSet rs, AccessToken at) throws SQLException {
		Contact c = new Contact();
		c.setFolderId(contactConfiguration.getAddressBookUserId());
		
		c.setUid(rs.getInt("userobm_id"));
		c.setFirstname(rs.getString("userobm_firstname"));
		c.setLastname(rs.getString("userobm_lastname"));
		c.setCommonname(rs.getString("userobm_commonname"));
		c.setCompany(rs.getString("userobm_company"));
		c.setService(rs.getString("userobm_service"));
		c.setManager(rs.getString("userobm_direction"));
		c.setTitle(rs.getString("userobm_title"));
		c.setComment(rs.getString("userobm_description"));
			
		String email = getEmail(rs.getString("userobm_email"), at.getDomain().getName());
		if (!Strings.isNullOrEmpty(email)) {
			c.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain(email));
		}
		
		addPhonesToContact(rs, c);
		addAddressToContact(rs, c);
		return c;
	}

	private void addAddressToContact(ResultSet rs, Contact c)
			throws SQLException {
		
		String street = buildFullStreetAddressFromResultSet(rs);
		String zipCode = rs.getString("userobm_zipcode");
		String cedex = rs.getString("userobm_expresspostal");
		String town = rs.getString("userobm_town");
		String country = null;
		String state = null;
		Address address = new Address(street , zipCode, cedex, town, country, state);
		c.addAddress(ContactLabel.ADDRESS.getContactLabel(), address);
	}

	private String buildFullStreetAddressFromResultSet(ResultSet rs) throws SQLException {
		return Joiner.on(" ").skipNulls().join(
			Strings.emptyToNull(rs.getString("userobm_address1")),
			Strings.emptyToNull(rs.getString("userobm_address2")),
			Strings.emptyToNull(rs.getString("userobm_address3")));
	}

	private void addPhonesToContact(ResultSet rs, Contact c)
			throws SQLException {
		
		for (Entry<ContactLabel, String> labelToField : contactLabelForUserObmField.entrySet()) {
			addPhoneToContact(c, rs, labelToField.getKey(), labelToField.getValue());
		}
	}

	private void addPhoneToContact(Contact contact, ResultSet rs,
			ContactLabel contactLabel, String phoneFieldName) throws SQLException {
		
		String phoneFieldValue = rs.getString(phoneFieldName);
		contact.addPhone(contactLabel.getContactLabel(), phone(phoneFieldValue));
	}

	private Phone phone(String phoneNumber) {
		return new Phone(phoneNumber);
	}

	private String getEmail(String string, String domain) {
		String[] mails = string.split("\r\n");
		String m = mails[0];
		if (m.length() == 0) {
			return null;
		}
		if (!m.contains("@")) {
			return m + "@" + domain;
		}
		return m;
	}

	public Set<DeletedContact> findRemovalCandidates(Date d, AccessToken at) throws SQLException {
		Set<DeletedContact> ret = new HashSet<DeletedContact>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;

		// FIXME add domain_id to deleted users
		String q = "SELECT deleteduser_user_id FROM DeletedUser ";
		if (d != null) {
			q += " WHERE deleteduser_timestamp >= ?";
		}
		q += " UNION ";
		q += "SELECT userobm_id FROM UserObm where (userobm_archive=1 OR userobm_hidden=1) AND userobm_domain_id="
				+ at.getDomain().getId();
		if (d != null) {
			q += " AND userobm_timeupdate >= ? ";
		}
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			if (d != null) {
				ps.setTimestamp(1, new Timestamp(d.getTime()));
				ps.setTimestamp(2, new Timestamp(d.getTime()));
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(DeletedContact
						.builder()
						.id(rs.getInt(1))
						.addressbookId(contactConfiguration.getAddressBookUserId())
						.build());
			}

		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

	public String getUserDomain(String login) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String userDomain = null;
		try {
			con = obmHelper.getConnection();

			String query;
			query = "SELECT domain_name" + " FROM UserObm, Domain WHERE"
					+ " userobm_domain_id = domain_id AND userobm_login = ?";

			ps = con.prepareStatement(query);

			ps.setString(1, login);
			rs = ps.executeQuery();

			if (rs.next()) {
				do {
					userDomain = rs.getString(1);
				} while (rs.next());
			}

		} catch (SQLException e) {
			logger.error("Could not find user domain", e);
		} finally {
			try {
				obmHelper.cleanup(con, ps, rs);
			} catch (Exception e) {
				logger.error("Could not clean up jdbc stuff");
			}
		}
		return userDomain;
	}

	public Contact findUserObmContact(AccessToken token, Integer userId) throws SQLException, ContactNotFoundException {
		
		String sql = "SELECT userobm_id, userobm_login, userobm_firstname, userobm_lastname, userobm_email, userobm_commonname, "
				+ Joiner.on(", ").join(USEROBM_ALL_JOBS, USEROBM_ALL_PHONES, USEROBM_ALL_ADDRESSES) + " "
				+ "FROM UserObm "
				+ "WHERE userobm_archive != 1 and userobm_domain_id=? and userobm_hidden != 1 and userobm_id = ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(sql);
			
			ps.setInt(1, token.getDomain().getId());
			ps.setInt(2, userId);
			
			rs = ps.executeQuery();
		
			if (rs.next()) {
				return userAsContact(rs, token);
			}
			throw new ContactNotFoundException("Contact user obm not found.", userId);
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

}
