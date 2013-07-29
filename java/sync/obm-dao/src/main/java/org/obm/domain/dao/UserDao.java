/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.domain.dao;

import static com.google.common.base.Strings.emptyToNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.base.DomainName;
import org.obm.sync.base.EmailLogin;
import org.obm.sync.book.AddressBook;
import org.obm.sync.host.ObmHost;
import org.obm.utils.ObmHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

@Singleton
public class UserDao {

	public static final String DB_INNER_FIELD_SEPARATOR = "\r\n";
	public static final int DEFAULT_GID = 1000;
	public static final int FIRST_UID = 1001;
	
	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
	private static final String USER_FIELDS = 
			"userobm_id, " +
			"userobm_email, " +
			"userobm_firstname, " +
			"userobm_lastname, " +
			"userobm_commonname, " +
			"userobm_login, " +
			"userobm_ext_id, " +
			"userobm_password, " +
			"userobm_perms, " + // Profile
			"userobm_kind, " +
			"userobm_title, " +
			"userobm_description, " +
			"userobm_company, " +
			"userobm_service, " +
			"userobm_direction, " +
			"userobm_address1, " +
			"userobm_address2, " +
			"userobm_address3, " +
			"userobm_town, " +
			"userobm_zipcode, " +
			"userobm_expresspostal, " + // BusinessZipCode
			"userobm_country_iso3166, " +
			"userobm_phone, " +
			"userobm_phone2, " +
			"userobm_mobile, " +
			"userobm_fax, " +
			"userobm_fax2, " +
			"userobm_mail_quota, " +
			"userobm_mail_server_id, " +
			"userobm_timecreate, " +
			"userobm_timeupdate, " +
			"userobm_usercreate, " +
			"userobm_userupdate, " +
			"userobm_uid, " +
			"userobm_gid, " +
			"defpref.userobmpref_value, " +
			"userpref.userobmpref_value, " +
			"userentity_entity_id, " +
			"host_name, " +
			"host_fqdn, " +
			"host_ip";
	private static final AddressBook CONTACTS_BOOK = AddressBook
			.builder()
			.name("contacts")
			.defaultBook(true)
			.syncable(true)
			.origin("provisioning")
			.build();
	private static final AddressBook COLLECTED_CONTACTS_BOOK = AddressBook
			.builder()
			.name("collected_contacts")
			.defaultBook(false)
			.syncable(true)
			.origin("provisioning")
			.build();

	private final ObmHelper obmHelper;
	private final ObmInfoDao obmInfoDao;
	private final AddressBookDao addressBookDao;
	private final UserPatternDao userPatternDao;
	
	@Inject
	@VisibleForTesting
	UserDao(ObmHelper obmHelper, ObmInfoDao obmInfoDao, AddressBookDao addressBookDao, UserPatternDao userPatternDao) {
		this.obmHelper = obmHelper;
		this.obmInfoDao = obmInfoDao;
		this.addressBookDao = addressBookDao;
		this.userPatternDao = userPatternDao;
	}
	
	public Map<String, String> loadUserProperties(int userObmId) {
		String q = "SELECT serviceproperty_service, serviceproperty_property, serviceproperty_value "
				+ "FROM ServiceProperty "
				+ "INNER JOIN UserEntity ON serviceproperty_entity_id=userentity_entity_id AND userentity_user_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, userObmId);
			rs = ps.executeQuery();
			Map<String, String> map = Maps.newHashMap();
			while (rs.next()) {
				String k = rs.getString(1) + "/" + rs.getString(2);
				String v = rs.getString(3);
				map.put(k, v);
				return map;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ImmutableMap.of();
	}
	
	@VisibleForTesting Integer userIdFromEmailQuery(Connection con, EmailLogin login, DomainName domain) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = con.createStatement();

			String request = "SELECT userobm_id, userobm_email, domain_name, domain_alias " +
					"FROM UserObm " +
					"INNER JOIN Domain ON domain_id = userobm_domain_id " +
					"WHERE UPPER(userobm_email) like UPPER('%" + login.get() + "%') AND userobm_archive != 1";
			
			rs = st.executeQuery(request);
			
			while (rs.next()) {
				
				int id = rs.getInt(1);
				String emailsToCompare = rs.getString(2); 
				String domainNameToCompare = rs.getString(3);
				String domainsAliasToCompare = rs.getString(4);
				
				if (compareEmailLogin(login, emailsToCompare)) {
					
					if (strictCompareDomain(domain, domainNameToCompare, domainsAliasToCompare)) {
						return id;	
					}	
				}
			}

		} finally {
			obmHelper.cleanup(null, st, rs);
		}
		
		return null;
	}

	private boolean strictCompareDomain(DomainName domain, String domainNameToCompare, String domainsAliasToCompare) {
		if (domain != null && domainNameToCompare != null) {
			if (domain.equals(new DomainName(domainNameToCompare))) {
				return true;
			}
			
			if (domainsAliasToCompare != null) {
				Iterable<String> domains = Splitter.on(DB_INNER_FIELD_SEPARATOR).split(domainsAliasToCompare);					
				for (String domainToCompare: domains) {
					if (domain.equals(new DomainName(domainToCompare))) {
						return true;
					}
				}	
			}
		}
		return false;
	}

	private boolean compareEmailLogin(EmailLogin login, String emailsToCompare) {
		if (login != null && emailsToCompare != null) {
			Iterable<String> emails = Splitter.on(DB_INNER_FIELD_SEPARATOR).split(emailsToCompare);			
			for (String email: emails) {
				if (login.equals(getEmailLogin(email))) {
					return true;
				}
			}	
		}
		return false;
	}

	private EmailLogin getEmailLogin(String email) {
		if (email != null && email.contains("@")) {
			return new EmailLogin(email.split("@")[0]);
		}
		return new EmailLogin(email);
	}
	
	public ObmUser findUser(String emailOrLogin, ObmDomain domain) {
		Connection con = null;
		Integer id = null;
		try {
			con = obmHelper.getConnection();
			id = userIdFromEmail(con, emailOrLogin, domain.getId());
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		if (id != null && id > 0) {
			return findUserById(id, domain);
		}
		return null;
	}

	/**
	 * does not return archived users
	 */
	public ObmUser findUserByLogin(String login, ObmDomain domain) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ObmUser obmUser = null;
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(uq);
			ps.setInt(1, domain.getId());
			ps.setString(2, login);
			rs = ps.executeQuery();
			if (rs.next()) {
				obmUser = createUserFromResultSet(domain, rs);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return obmUser;
	}

	@VisibleForTesting ObmUser createUserFromResultSet(ObmDomain domain, ResultSet rs) throws SQLException {
		String extId = rs.getString("userobm_ext_id");
		
		return ObmUser.builder()
				.uid(rs.getInt(1))
				.login(rs.getString("userobm_login"))
				.emailAndAliases(rs.getString(2))
				.domain(domain)
				.firstName(rs.getString("userobm_firstname"))
				.lastName(rs.getString("userobm_lastname"))
				.publicFreeBusy(computePublicFreeBusy(5, rs))
				.commonName(rs.getString("userobm_commonname"))
				.extId(extId != null ? UserExtId.builder().extId(extId).build() : null)
				.entityId(rs.getInt("userentity_entity_id"))
				.password(Strings.emptyToNull(rs.getString("userobm_password")))
				.profileName(ProfileName.builder().name(rs.getString("userobm_perms")).build())
				.kind(rs.getString("userobm_kind"))
				.title(emptyToNull(rs.getString("userobm_title")))
				.description(rs.getString("userobm_description"))
				.company(rs.getString("userobm_company"))
				.service(rs.getString("userobm_service"))
				.direction(rs.getString("userobm_direction"))
				.address1(rs.getString("userobm_address1"))
				.address2(rs.getString("userobm_address2"))
				.address3(rs.getString("userobm_address3"))
				.town(rs.getString("userobm_town"))
				.zipCode(rs.getString("userobm_zipcode"))
				.expresspostal(rs.getString("userobm_expresspostal"))
				.countryCode(rs.getString("userobm_country_iso3166"))
				.phone(emptyToNull(rs.getString("userobm_phone")))
				.phone2(emptyToNull(rs.getString("userobm_phone2")))
				.mobile(emptyToNull(rs.getString("userobm_mobile")))
				.fax(emptyToNull(rs.getString("userobm_fax")))
				.fax2(emptyToNull(rs.getString("userobm_fax2")))
				.mailQuota(rs.getInt("userobm_mail_quota"))
				.mailHost(hostFromCursor(rs, domain))
				.timeCreate(JDBCUtils.getDate(rs, "userobm_timecreate"))
				.timeUpdate(JDBCUtils.getDate(rs, "userobm_timeupdate"))
				.createdBy(findUserById(rs.getInt("userobm_usercreate"), domain))
				.updatedBy(findUserById(rs.getInt("userobm_userupdate"), domain))
				.uidNumber(JDBCUtils.getInteger(rs, "userobm_uid"))
				.gidNumber(JDBCUtils.getInteger(rs, "userobm_gid"))
				.build();
	}

	private ObmHost hostFromCursor(ResultSet rs, ObmDomain domain) throws SQLException {
		int id = rs.getInt("userobm_mail_server_id");

		if (rs.wasNull()) {
			return null;
		}

		return ObmHost
				.builder()
				.id(id)
				.name(rs.getString("host_name"))
				.fqdn(rs.getString("host_fqdn"))
				.ip(rs.getString("host_ip"))
				.domainId(domain.getId())
				.build();
	}
	
	public ObmUser findUserById(int id, ObmDomain domain) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ObmUser obmUser = null;
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=? "
				+ "WHERE userobm_id=? ";
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(uq);
			ps.setInt(1, id);
			ps.setInt(2, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				obmUser = createUserFromResultSet(domain, rs);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return obmUser;
	}

	private boolean computePublicFreeBusy(int idx, ResultSet rs)
	throws SQLException {
		boolean user = true;
		boolean def = !"no".equalsIgnoreCase(rs.getString(idx));
		String userPref = rs.getString(idx + 1);
		if (rs.wasNull()) {
			user = def;
		} else {
			user = "yes".equals(userPref);
		}
		return user;
	}

	
	@VisibleForTesting Integer userIdFromLogin(Connection con, EmailLogin login, Integer domainId) {
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		Integer ret = null;
		String uq = "SELECT userobm_id "
				+ "FROM UserObm "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try {
			ps = con.prepareStatement(uq);
			ps.setInt(1, domainId);
			ps.setString(2, login.get());
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return ret;
	}
	
	public Integer userIdFromEmail(Connection con, String email, Integer domainId) throws SQLException{
		String[] parts = email.split("@");
		EmailLogin login = new EmailLogin(parts[0]);
		DomainName domain = new DomainName("-");
		Integer ownerId = null;
		
		// OBMFULL-4353
		// We only fetch the user by login if a login was provided
		// If a full email is given, we must favor the fetch by email
		if (parts.length > 1) {
			domain = new DomainName(parts[1]);
		} else {
			ownerId = userIdFromLogin(con, login, domainId);
		}
		
		if(ownerId == null){
			ownerId = userIdFromEmailQuery(con, login, domain);	
		}
		
		return ownerId;
	}
	
	public ObmUser getByExtId(UserExtId userExtId, ObmDomain domain) throws SQLException, UserNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id "
				+ "WHERE userobm_domain_id=? AND userobm_ext_id=? AND userobm_archive != '1'";
		try {
			conn = obmHelper.getConnection();
			ps = conn.prepareStatement(uq);
			ps.setInt(1, domain.getId());
			ps.setString(2, userExtId.getExtId());
			rs = ps.executeQuery();
			if (rs.next()) {
				return createUserFromResultSet(domain, rs);
			}
			else {
				throw new UserNotFoundException(userExtId);
			}
		} finally {
			obmHelper.cleanup(conn, ps, rs);
		}
	}

	public List<ObmUser> list(ObmDomain domain) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ObmUser> users = Lists.newArrayList();

		String query = "SELECT " + USER_FIELDS + " FROM UserObm " + 
				"INNER JOIN UserEntity ON userentity_user_id = userobm_id " + 
				"LEFT JOIN Host ON host_id = userobm_mail_server_id " +
				"LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL " + 
				"LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id " + 
				"WHERE userobm_domain_id = ? AND userobm_archive != '1'";

		try {
			conn = obmHelper.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, domain.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				users.add(createUserFromResultSet(domain, rs));
			}
		} finally {
			obmHelper.cleanup(conn, ps, rs);
		}

		return users;
	}

	public ObmUser create(ObmUser user) throws SQLException, DaoException {
		Connection conn = null;
		PreparedStatement ps = null;

		String q = "INSERT INTO UserObm (" +
				"userobm_domain_id, " +
				"userobm_usercreate, " +
				"userobm_ext_id, " +
				"userobm_login, " +
				"userobm_password, " +
				"userobm_perms, " +
				"userobm_kind, " +
				"userobm_commonname, " +
				"userobm_lastname, " +
				"userobm_firstname, " +
				"userobm_title, " +
				"userobm_company, " +
				"userobm_direction, " +
				"userobm_service, " +
				"userobm_address1, " +
				"userobm_address2, " +
				"userobm_address3, " +
				"userobm_zipcode, " +
				"userobm_town, " +
				"userobm_expresspostal, " +
				"userobm_country_iso3166, " +
				"userobm_phone, " +
				"userobm_phone2, " +
				"userobm_mobile, " +
				"userobm_fax, " +
				"userobm_fax2, " +
				"userobm_description, " +
				"userobm_email, " +
				"userobm_mail_server_id, " +
				"userobm_mail_quota," +
				"userobm_uid," +
				"userobm_gid" +
				") VALUES (" +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
				"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				")";

		try {
			int idx = 1;
			conn = obmHelper.getConnection();
			ps = conn.prepareStatement(q);

			ps.setInt(idx++, user.getDomain().getId());

			if (user.getCreatedBy() != null) {
				ps.setInt(idx++, user.getCreatedBy().getUid());
			} else {
				ps.setNull(idx++, Types.INTEGER);
			}

			if (user.getExtId() != null) {
				ps.setString(idx++, user.getExtId().getExtId());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getLogin());
			ps.setString(idx++, Strings.nullToEmpty(user.getPassword()));

			if (user.getProfileName() != null) {
				ps.setString(idx++, user.getProfileName().getName());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getKind());
			ps.setString(idx++, user.getCommonName());
			ps.setString(idx++, user.getLastName());
			ps.setString(idx++, user.getFirstName());
			ps.setString(idx++, user.getTitle());
			ps.setString(idx++, user.getCompany());
			ps.setString(idx++, user.getDirection());
			ps.setString(idx++, user.getService());
			ps.setString(idx++, user.getAddress1());
			ps.setString(idx++, user.getAddress2());
			ps.setString(idx++, user.getAddress3());
			ps.setString(idx++, user.getZipCode());
			ps.setString(idx++, user.getTown());
			ps.setString(idx++, user.getExpresspostal());
			ps.setString(idx++, user.getCountryCode());
			ps.setString(idx++, user.getPhone());
			ps.setString(idx++, user.getPhone2());
			ps.setString(idx++, user.getMobile());
			ps.setString(idx++, user.getFax());
			ps.setString(idx++, user.getFax2());
			ps.setString(idx++, user.getDescription());

			if (user.getEmail() != null && user.getMailHost() != null) {
				ps.setString(idx++, Joiner
						.on(DB_INNER_FIELD_SEPARATOR)
						.skipNulls()
						.join(Iterables
								.concat(Collections.singleton(user.getEmail()),
										user.getEmailAlias())));
				ps.setInt(idx++, user.getMailHost().getId());
				ps.setInt(idx++, user.getMailQuotaAsInt());
			} else {
				ps.setString(idx++, "");
				ps.setNull(idx++, Types.INTEGER);
				ps.setInt(idx++, 0);
			}

			if (user.getUidNumber() != null) {
				ps.setInt(idx++, user.getUidNumber());
			} else {
				ps.setInt(idx++, getAndIncrementUidMaxUsed());
			}
			if (user.getGidNumber() != null) {
				ps.setInt(idx++, user.getGidNumber());
			} else {
				ps.setInt(idx++, DEFAULT_GID);
			}

			ps.executeUpdate();

			int userId = obmHelper.lastInsertId(conn);

			obmHelper.linkEntity(conn, "UserEntity", "user_id", userId);
			obmHelper.linkEntity(conn, "CalendarEntity", "calendar_id", userId);
			obmHelper.linkEntity(conn, "MailboxEntity", "mailbox_id", userId);

			ObmUser createdUser = findUserById(userId, user.getDomain());

			AddressBook contactsBook = addressBookDao.create(CONTACTS_BOOK, createdUser);
			AddressBook collectedContactsBook = addressBookDao.create(COLLECTED_CONTACTS_BOOK, createdUser);

			addressBookDao.enableAddressBookSynchronization(contactsBook.getUid(), createdUser);
			addressBookDao.enableAddressBookSynchronization(collectedContactsBook.getUid(), createdUser);

			userPatternDao.updateUserIndex(createdUser);

			return createdUser;
		} finally {
			obmHelper.cleanup(conn, ps, null);
		}
	}

	private int getAndIncrementUidMaxUsed() throws DaoException {
		Integer uid = obmInfoDao.getUidMaxUsed();

		if (uid == null) {
			return obmInfoDao.insertUidMaxUsed(FIRST_UID);
		} else {
			return obmInfoDao.updateUidMaxUsed(uid + 1);
		}
	}

	public ObmUser update(ObmUser user) throws SQLException, UserNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;

		String query = "UPDATE UserObm SET " +
                    "userobm_timeupdate = ?, " +
                    "userobm_userupdate = ?, " +
                    "userobm_ext_id = ?, " +
                    "userobm_login = ?, " +
                    "userobm_password = ?, " +
                    "userobm_perms = ?, " +
                    "userobm_kind = ?, " +
                    "userobm_commonname = ?, " +
                    "userobm_lastname = ?, " +
                    "userobm_firstname = ?, " +
                    "userobm_title = ?, " +
                    "userobm_company = ?, " +
                    "userobm_direction = ?, " +
                    "userobm_service = ?, " +
                    "userobm_address1 = ?, " +
                    "userobm_address2 = ?, " +
                    "userobm_address3 = ?, " +
                    "userobm_zipcode = ?, " +
                    "userobm_town = ?, " +
                    "userobm_expresspostal = ?, " +
                    "userobm_country_iso3166 = ?, " +
                    "userobm_phone = ?, " +
                    "userobm_phone2 = ?, " +
                    "userobm_mobile = ?, " +
                    "userobm_fax = ?, " +
                    "userobm_fax2 = ?, " +
                    "userobm_description = ?, " +
                    "userobm_email = ?, " +
                    "userobm_mail_server_id = ?, " +
                    "userobm_mail_quota = ? " +
                    "WHERE userobm_id = ?";

		try {
			int idx = 1;
			conn = obmHelper.getConnection();
			ps = conn.prepareStatement(query);

			ps.setTimestamp(idx++, new Timestamp(obmHelper.selectNow(conn).getTime()));

			if (user.getUpdatedBy() != null) {
				ps.setInt(idx++, user.getUpdatedBy().getUid());
			} else {
				ps.setNull(idx++, Types.INTEGER);
			}

			if (user.getExtId() != null) {
				ps.setString(idx++, user.getExtId().getExtId());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getLogin());
			ps.setString(idx++, Strings.nullToEmpty(user.getPassword()));

			if (user.getProfileName() != null) {
				ps.setString(idx++, user.getProfileName().getName());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getKind());
			ps.setString(idx++, user.getCommonName());
			ps.setString(idx++, user.getLastName());
			ps.setString(idx++, user.getFirstName());
			ps.setString(idx++, user.getTitle());
			ps.setString(idx++, user.getCompany());
			ps.setString(idx++, user.getDirection());
			ps.setString(idx++, user.getService());
			ps.setString(idx++, user.getAddress1());
			ps.setString(idx++, user.getAddress2());
			ps.setString(idx++, user.getAddress3());
			ps.setString(idx++, user.getZipCode());
			ps.setString(idx++, user.getTown());
			ps.setString(idx++, user.getExpresspostal());
			ps.setString(idx++, user.getCountryCode());
			ps.setString(idx++, user.getPhone());
			ps.setString(idx++, user.getPhone2());
			ps.setString(idx++, user.getMobile());
			ps.setString(idx++, user.getFax());
			ps.setString(idx++, user.getFax2());
			ps.setString(idx++, user.getDescription());

			if (user.getEmail() != null && user.getMailHost() != null) {
				ps.setString(idx++, Joiner
						.on(DB_INNER_FIELD_SEPARATOR)
						.skipNulls()
						.join(Iterables
								.concat(Collections.singleton(user.getEmail()),
										user.getEmailAlias())));
				ps.setInt(idx++, user.getMailHost().getId());
				ps.setInt(idx++, user.getMailQuotaAsInt());
			} else {
				ps.setString(idx++, "");
				ps.setNull(idx++, Types.INTEGER);
				ps.setInt(idx++, 0);
			}

			ps.setInt(idx++, user.getUid());

			int updateCount = ps.executeUpdate();

			if (updateCount != 1) {
				throw new UserNotFoundException(String.format("No user found with id %d and login %s", user.getUid(), user.getLogin()));
			}

			return findUserById(user.getUid(), user.getDomain());
		} finally {
			obmHelper.cleanup(conn, ps, null);
		}
	}

	public void delete(ObmUser user) throws SQLException, UserNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = obmHelper.getConnection();
			ps = connection.prepareStatement("DELETE FROM UserObm WHERE userobm_ext_id = ? AND userobm_domain_id = ?");

			final String extId = user.getExtId().getExtId();
			ps.setString(1, extId);
			ps.setInt(2, user.getDomain().getId());

			int updateCount = ps.executeUpdate();

			if (updateCount != 1) {
				throw new UserNotFoundException(String.format("No user found with extid %s.", extId));
			}
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

}
