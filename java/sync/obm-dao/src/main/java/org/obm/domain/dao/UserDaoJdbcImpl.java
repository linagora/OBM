/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.domain.dao;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;

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
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.obm.provisioning.Group;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.base.DomainName;
import org.obm.sync.base.EmailLogin;
import org.obm.sync.book.AddressBook;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;
import org.obm.utils.ObmHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserAddress;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserNomad;
import fr.aliacom.obm.common.user.UserPassword;
import fr.aliacom.obm.common.user.UserPhones;
import fr.aliacom.obm.common.user.UserWork;

@Singleton
public class UserDaoJdbcImpl implements UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
	private static final int HIDDEN_TRUE = 1;
	private static final int NOMAD_ENABLED = 1;
	private static final int NOMAD_ALLOWED = 1;
	private static final int NOMAD_LOCAL_COPY = 1;

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
			"userobm_archive, " +
			"userobm_hidden, " +
			"userobm_timecreate, " +
			"userobm_timeupdate, " +
			"userobm_usercreate, " +
			"userobm_userupdate, " +
			"userobm_uid, " +
			"userobm_gid, " +
			"userobm_samba_perms, " +
			"userobm_samba_home_drive, " +
			"userobm_samba_home, " +
			"userobm_samba_logon_script, " +
			"userobm_nomade_enable, " +
			"userobm_email_nomade, " +
			"userobm_nomade_perms, " +
			"userobm_nomade_local_copy, " +
			"defpref.userobmpref_value AS defpref_userobmpref_value, " +
			"userpref.userobmpref_value AS userpref_userobmpref_value, " +
			"userentity_entity_id, " +
			"host_name, " +
			"host_fqdn, " +
			"host_ip, " +
			"host_domain_id," +
			"userobm_account_dateexp," +
			"userobm_delegation, " +
			"userobm_delegation_target";
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
			.defaultBook(true)
			.syncable(true)
			.origin("provisioning")
			.build();

	private final ObmHelper obmHelper;
	private final ObmInfoDao obmInfoDao;
	private final GroupDao groupDao;
	private final AddressBookDao addressBookDao;
	private final UserPatternDao userPatternDao;
	private final ProfileDao profileDao;
	
	@Inject
	@VisibleForTesting
	UserDaoJdbcImpl(ObmHelper obmHelper, ObmInfoDao obmInfoDao, AddressBookDao addressBookDao, UserPatternDao userPatternDao, GroupDao groupDao, ProfileDao profileDao) {
		this.obmHelper = obmHelper;
		this.obmInfoDao = obmInfoDao;
		this.addressBookDao = addressBookDao;
		this.userPatternDao = userPatternDao;
		this.groupDao = groupDao;
		this.profileDao = profileDao;
	}
	
	public Map<String, String> loadUserProperties(int userObmId) {
		String q = "SELECT serviceproperty_service, serviceproperty_property, serviceproperty_value "
				+ "FROM ServiceProperty "
				+ "INNER JOIN UserEntity ON serviceproperty_entity_id=userentity_entity_id AND userentity_user_id=?";
		
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(q)) {
			ps.setInt(1, userObmId);
			ResultSet rs = ps.executeQuery();
			Map<String, String> map = Maps.newHashMap();
			while (rs.next()) {
				String k = rs.getString(1) + "/" + rs.getString(2);
				String v = rs.getString(3);
				map.put(k, v);
				return map;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ImmutableMap.of();
	}
	
	@VisibleForTesting Integer userIdFromEmailQuery(Connection con, EmailLogin login, DomainName domain) throws SQLException {
		try (Statement st = con.createStatement()) {
			// Don't use a PreparedStatement here, as they can't be load-balanced and this is a very frequent query
			String request = String.format("SELECT userobm_id, userobm_email, domain_name, domain_alias " +
					"FROM UserObm " +
					"INNER JOIN Domain ON domain_id = userobm_domain_id " +
					"WHERE UPPER(userobm_email) like UPPER('%s') AND userobm_archive != 1", StringEscapeUtils.escapeSql(
							"%" + login.get().toString() + "%"));
			
			ResultSet rs = st.executeQuery(request);
			
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
		}
		
		return null;
	}

	private boolean strictCompareDomain(DomainName domain, String domainNameToCompare, String domainsAliasToCompare) {
		if (domain != null && domainNameToCompare != null) {
			if (domain.equals(new DomainName(domainNameToCompare))) {
				return true;
			}
			
			if (domainsAliasToCompare != null) {
				Iterable<String> domains = Splitter.on(DB_INNER_FIELD_SEPARATOR).omitEmptyStrings().split(domainsAliasToCompare);					
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
		if (login != null) {
			for (String email: deserializeEmails(emailsToCompare)) {
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
	
	@Override
	public ObmUser findUser(String email, ObmDomain domain) {
		Integer id = null;
		try (Connection con = obmHelper.getConnection()) {
			id = userIdFromEmail(con, email, domain.getId());
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		}
		if (id != null && id > 0) {
			return findUserById(id, domain);
		}
		return null;
	}

	/**
	 * does not return archived users
	 */
	@Override
	public ObmUser findUserByLogin(String login, ObmDomain domain) {
		ObmUser obmUser = null;
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(uq)) {
			ps.setInt(1, domain.getId());
			ps.setString(2, login);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				obmUser = createUserFromResultSetAndFetchCreators(domain, rs);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return obmUser;
	}

	@VisibleForTesting
	ObmUser createUserFromResultSetAndFetchCreators(ObmDomain domain, ResultSet rs) throws SQLException {
		ObmUser creator = findUserById(rs.getInt("userobm_usercreate"), domain, false);
		ObmUser updator = findUserById(rs.getInt("userobm_userupdate"), domain, false);

		return createUserFromResultSet(domain, rs, creator, updator, null);
	}

	@VisibleForTesting
	ObmUser createUserFromResultSetAndFetchCreatorsAndGroups(ObmDomain domain, ResultSet rs) throws SQLException {
		ObmUser creator = findUserById(rs.getInt("userobm_usercreate"), domain, false);
		ObmUser updator = findUserById(rs.getInt("userobm_userupdate"), domain, false);

		Set<Group> groups = groupDao.getAllPublicGroupsForUserExtId(domain, UserExtId.builder().extId(rs.getString("userobm_ext_id")).build());

		return createUserFromResultSet(domain, rs, creator, updator, groups);
	}
	
	private ObmUser createUserFromResultSet(ObmDomain domain, ResultSet rs, ObmUser creator, ObmUser updator, Set<Group> groups) throws SQLException {

		try {
			String extId = rs.getString("userobm_ext_id");
			int quota = rs.getInt("userobm_mail_quota");
			
			
			return ObmUser.builder()
					.uid(rs.getInt("userobm_id"))
					.login(UserLogin.valueOf(rs.getString("userobm_login")))
					.admin(profileDao.isAdminProfile(rs.getString("userobm_perms")))
					.domain(domain)
					.identity(UserIdentity.builder()
						.kind(rs.getString("userobm_kind"))
						.firstName(emptyToNull(rs.getString("userobm_firstname")))
						.lastName(emptyToNull(rs.getString("userobm_lastname")))
						.commonName(emptyToNull(rs.getString("userobm_commonname")))
						.build())
					.publicFreeBusy(computePublicFreeBusy(rs))
					.extId(extId != null ? UserExtId.builder().extId(extId).build() : null)
					.entityId(EntityId.valueOf(rs.getInt("userentity_entity_id")))
					.password(UserPassword.valueOf(Strings.emptyToNull(rs.getString("userobm_password"))))
					.profileName(ProfileName.builder().name(rs.getString("userobm_perms")).build())
					.work(UserWork.builder()
						.title(emptyToNull(rs.getString("userobm_title")))
						.company(rs.getString("userobm_company"))
						.service(rs.getString("userobm_service"))
						.direction(rs.getString("userobm_direction"))
						.build())
					.description(rs.getString("userobm_description"))
					.address(UserAddress.builder()
								.addressPart(rs.getString("userobm_address1"))
								.addressPart(rs.getString("userobm_address2"))
								.addressPart(rs.getString("userobm_address3"))
								.town(rs.getString("userobm_town"))
								.zipCode(rs.getString("userobm_zipcode"))
								.expressPostal(rs.getString("userobm_expresspostal"))
								.countryCode(rs.getString("userobm_country_iso3166"))
								.build())
					.phones(UserPhones.builder()
								.addPhone(emptyToNull(rs.getString("userobm_phone")))
								.addPhone(emptyToNull(rs.getString("userobm_phone2")))
								.mobile(emptyToNull(rs.getString("userobm_mobile")))
								.addFax(emptyToNull(rs.getString("userobm_fax")))
								.addFax(emptyToNull(rs.getString("userobm_fax2")))
								.build())
					.nomad(UserNomad.builder()
							.enabled(rs.getInt("userobm_nomade_enable") == NOMAD_ENABLED)
							.email(rs.getString("userobm_email_nomade"))
							.allowed(rs.getInt("userobm_nomade_perms") == NOMAD_ALLOWED)
							.localCopy(rs.getInt("userobm_nomade_local_copy") == NOMAD_LOCAL_COPY)
							.build())
					.emails(UserEmails.builder()
						.quota(quotaToNullable(quota))
						.server(hostFromCursor(rs))
						.addresses(deserializeEmails(rs.getString("userobm_email")))
						.domain(domain)
						.build())
					.archived(rs.getBoolean("userobm_archive"))
					.hidden(rs.getInt("userobm_hidden") == HIDDEN_TRUE)
					.timeCreate(JDBCUtils.getDate(rs, "userobm_timecreate"))
					.timeUpdate(JDBCUtils.getDate(rs, "userobm_timeupdate"))
					.uidNumber(JDBCUtils.getInteger(rs, "userobm_uid"))
					.gidNumber(JDBCUtils.getInteger(rs, "userobm_gid"))
					.createdBy(creator)
					.updatedBy(updator)
					.groups(Objects.firstNonNull(groups, Collections.EMPTY_SET))
					.expirationDate(JDBCUtils.getDate(rs, "userobm_account_dateexp"))
					.delegation(emptyToNull(rs.getString("userobm_delegation")))
					.delegationTarget(emptyToNull(rs.getString("userobm_delegation_target")))
					.sambaAllowed(rs.getBoolean("userobm_samba_perms"))
					.sambaHomeDrive(emptyToNull(rs.getString("userobm_samba_home_drive")))
					.sambaHomeFolder(emptyToNull(rs.getString("userobm_samba_home")))
					.sambaLogonScript(emptyToNull(rs.getString("userobm_samba_logon_script")))
					.build();
		} catch (DaoException e) {
			throw new SQLException(e);
		}

	}

	private Iterable<String> deserializeEmails(String emails) {
		return Splitter
				.on(DB_INNER_FIELD_SEPARATOR)
				.omitEmptyStrings()
				.split(nullToEmpty(emails));
	}
	
	private String serializeEmails(ObmUser user) {
		return Joiner
				.on(DB_INNER_FIELD_SEPARATOR)
				.skipNulls()
				.join(user.getEmails());
	}
	
	private ObmHost hostFromCursor(ResultSet rs) throws SQLException {
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
				.domainId(rs.getInt("host_domain_id"))
				.build();
	}
	
	@Override
	public ObmUser findUserById(int id, ObmDomain domain) {
		return findUserById(id, domain, true);
	}

	@VisibleForTesting ObmUser findUserById(int id, ObmDomain domain, boolean fetchCreators) {
		ObmUser obmUser = null;
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=? "
				+ "WHERE userobm_id=? ";
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(uq)) {
			ps.setInt(1, id);
			ps.setInt(2, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				if (fetchCreators) {
					obmUser = createUserFromResultSetAndFetchCreators(domain, rs);
				} else {
					obmUser = createUserFromResultSet(domain, rs, null, null, null);
				}
			}
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
		return obmUser;
	}

	private boolean computePublicFreeBusy(ResultSet rs)
	throws SQLException {
		boolean user = true;
		boolean def = !"no".equalsIgnoreCase(rs.getString("defpref_userobmpref_value"));
		String userPref = rs.getString("userpref_userobmpref_value");
		if (rs.wasNull()) {
			user = def;
		} else {
			user = "yes".equals(userPref);
		}
		return user;
	}

	
	@VisibleForTesting Integer userIdFromLogin(Connection con, EmailLogin login, Integer domainId) {
		Integer ret = null;
		String uq = "SELECT userobm_id "
				+ "FROM UserObm "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try (PreparedStatement ps = con.prepareStatement(uq)) {
			ps.setInt(1, domainId);
			ps.setString(2, login.get());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return ret;
	}
	
	public Integer userIdFromEmail(Connection con, String email, Integer domainId) throws SQLException {

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

	@Override
	public ObmUser getByExtId(UserExtId userExtId, ObmDomain domain) throws SQLException, UserNotFoundException {
		return getByExtIdWithOptionalGroups(userExtId, domain, false);
	}

	@Override
	public ObmUser getByExtIdWithGroups(UserExtId userExtId, ObmDomain domain) throws SQLException, UserNotFoundException {
		return getByExtIdWithOptionalGroups(userExtId, domain, true);
	}

	private ObmUser getByExtIdWithOptionalGroups(UserExtId userExtId, ObmDomain domain, boolean fetchGroups) throws SQLException, UserNotFoundException {
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
				+ "INNER JOIN Domain ON domain_id = userobm_domain_id "
				+ "LEFT JOIN Host ON host_id = userobm_mail_server_id "
				+ "LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id "
				+ "WHERE domain_uuid=? AND userobm_ext_id=?";
		try (Connection conn = obmHelper.getConnection();
				PreparedStatement ps = conn.prepareStatement(uq)) {
			ps.setString(1, domain.getUuid().get());
			ps.setString(2, userExtId.getExtId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				if (!fetchGroups) {
					return createUserFromResultSetAndFetchCreators(domain, rs);
				} else {
					return createUserFromResultSetAndFetchCreatorsAndGroups(domain, rs);
				}
			}
			else {
				throw new UserNotFoundException(userExtId);
			}
		}
	}

	@Override
	public List<ObmUser> list(ObmDomain domain) throws SQLException {
		List<ObmUser> users = Lists.newArrayList();

		String query = "SELECT " + USER_FIELDS + " FROM UserObm " + 
				"INNER JOIN UserEntity ON userentity_user_id = userobm_id " + 
				"LEFT JOIN Host ON host_id = userobm_mail_server_id " +
				"LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL " + 
				"LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id " + 
				"WHERE userobm_domain_id = ?";

		try (Connection conn = obmHelper.getConnection();
				PreparedStatement ps =  conn.prepareStatement(query)) {
			ps.setInt(1, domain.getId());
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				users.add(createUserFromResultSetAndFetchCreators(domain, rs));
			}
		}

		return users;
	}

	@Override
	public ObmUser create(ObmUser user) throws SQLException, DaoException {
		ObmDomain domain = user.getDomain();

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
				"userobm_mail_perms, " +
				"userobm_hidden, " +
				"userobm_archive, " +
				"userobm_uid," +
				"userobm_gid," +
				"userobm_account_dateexp," +
				"userobm_nomade_enable, " +
				"userobm_email_nomade, " +
				"userobm_nomade_perms, " +
				"userobm_nomade_local_copy, " +
				"userobm_delegation, " +
				"userobm_delegation_target, " +
				"userobm_samba_perms, " +
				"userobm_samba_home_drive," +
				"userobm_samba_home," +
				"userobm_samba_logon_script" +
				") VALUES (" +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"?, ?, ?, ?, ?, ?" +
				")";

		try (Connection conn = obmHelper.getConnection();
				PreparedStatement ps = conn.prepareStatement(q)) {

			int idx = 1;
			ps.setInt(idx++, domain.getId());

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
			ps.setString(idx++, userPasswordNullToEmpty(user));

			if (user.getProfileName() != null) {
				ps.setString(idx++, user.getProfileName().getName());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getKind());
			ps.setString(idx++, nullToEmpty(user.getCommonName()));
			ps.setString(idx++, nullToEmpty(user.getLastName()));
			ps.setString(idx++, nullToEmpty(user.getFirstName()));
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
				ps.setInt(idx++, getQuotaAsInt0(user));
				ps.setInt(idx++, 1);
			} else {
				ps.setString(idx++, "");
				ps.setNull(idx++, Types.INTEGER);
				ps.setInt(idx++, 0);
				ps.setInt(idx++, 0);
			}
			
			ps.setInt(idx++, user.isHidden() ? 1 : 0);
			ps.setInt(idx++, user.isArchived() ? 1 : 0);
			
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

			JDBCUtils.setOptionalDate(ps, user.getExpirationDate(), idx++);

			ps.setInt(idx++, user.getNomad().isEnabled() ? 1 : 0);
			ps.setString(idx++, user.getNomad().getEmail());
			ps.setInt(idx++, user.getNomad().isAllowed() ? 1 : 0);
			ps.setInt(idx++, user.getNomad().hasLocalCopy() ? 1 : 0);

			ps.setString(idx++, user.getDelegation());
			ps.setString(idx++, user.getDelegationTarget());

			ps.setInt(idx++, user.isSambaAllowed() ? 1 : 0);
			ps.setString(idx++, nullToEmpty(user.getSambaHomeDrive()));
			ps.setString(idx++, nullToEmpty(user.getSambaHomeFolder()));
			ps.setString(idx++, nullToEmpty(user.getSambaLogonScript()));

			ps.executeUpdate();

			int userId = obmHelper.lastInsertId(conn);

			obmHelper.linkEntity(conn, "UserEntity", "user_id", userId);
			obmHelper.linkEntity(conn, "CalendarEntity", "calendar_id", userId);
			obmHelper.linkEntity(conn, "MailboxEntity", "mailbox_id", userId);

			ObmUser createdUser = findUserById(userId, domain);

			AddressBook contactsBook = addressBookDao.create(CONTACTS_BOOK, createdUser);
			AddressBook collectedContactsBook = addressBookDao.create(COLLECTED_CONTACTS_BOOK, createdUser);

			addressBookDao.enableAddressBookSynchronization(contactsBook.getUid(), createdUser);
			addressBookDao.enableAddressBookSynchronization(collectedContactsBook.getUid(), createdUser);

			userPatternDao.updateUserIndex(createdUser);

			return createdUser;
		}
	}

	private String userPasswordNullToEmpty(ObmUser user) {
		UserPassword password = user.getPassword();
		if (password == null) {
			return "";
		}
		return Strings.nullToEmpty(password.getStringValue());
	}

	private int getAndIncrementUidMaxUsed() throws DaoException {
		Integer uid = obmInfoDao.getUidMaxUsed();

		if (uid == null) {
			return obmInfoDao.insertUidMaxUsed(FIRST_UID);
		} else {
			return obmInfoDao.updateUidMaxUsed(uid + 1);
		}
	}

	@Override
	public ObmUser update(ObmUser user) throws SQLException, UserNotFoundException {
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
                    "userobm_mail_quota = ?, " +
                    "userobm_mail_perms = ?, " +
                    "userobm_hidden = ?, " +
                    "userobm_archive = ?, " +
                    "userobm_account_dateexp = ?, " +
                    "userobm_nomade_enable = ?," +
                    "userobm_email_nomade = ?, " +
                    "userobm_nomade_perms = ?, " +
                    "userobm_nomade_local_copy = ?, " +
                    "userobm_delegation = ?, " +
                    "userobm_delegation_target = ?, " +
                    "userobm_samba_perms = ?, " +
                    "userobm_samba_home_drive = ?, " +
                    "userobm_samba_home = ?, " +
                    "userobm_samba_logon_script = ? " +
                    "WHERE userobm_id = ?";

		try (Connection conn = obmHelper.getConnection();
				PreparedStatement ps =  conn.prepareStatement(query)) {

			int idx = 1;
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
			ps.setString(idx++, userPasswordNullToEmpty(user));

			if (user.getProfileName() != null) {
				ps.setString(idx++, user.getProfileName().getName());
			} else {
				ps.setNull(idx++, Types.VARCHAR);
			}

			ps.setString(idx++, user.getKind());
			ps.setString(idx++, nullToEmpty(user.getCommonName()));
			ps.setString(idx++, nullToEmpty(user.getLastName()));
			ps.setString(idx++, nullToEmpty(user.getFirstName()));
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
				ps.setString(idx++, serializeEmails(user));
				ps.setInt(idx++, user.getMailHost().getId());
				ps.setInt(idx++, getQuotaAsInt0(user));
				ps.setInt(idx++, 1);
			} else {
				ps.setString(idx++, "");
				ps.setNull(idx++, Types.INTEGER);
				ps.setInt(idx++, 0);
				ps.setInt(idx++, 0);
			}

			ps.setInt(idx++, user.isHidden() ? 1 : 0);
			ps.setInt(idx++, user.isArchived() ? 1 : 0);
			JDBCUtils.setOptionalDate(ps, user.getExpirationDate(), idx++);

			ps.setInt(idx++, user.getNomad().isEnabled() ? 1 : 0);
			ps.setString(idx++, user.getNomad().getEmail());
			ps.setInt(idx++, user.getNomad().isAllowed() ? 1 : 0);
			ps.setInt(idx++, user.getNomad().hasLocalCopy() ? 1 : 0);

			ps.setString(idx++, user.getDelegation());
			ps.setString(idx++, user.getDelegationTarget());
			ps.setInt(idx++, user.isSambaAllowed() ? 1 : 0);
			ps.setString(idx++, nullToEmpty(user.getSambaHomeDrive()));
			ps.setString(idx++, nullToEmpty(user.getSambaHomeFolder()));
			ps.setString(idx++, nullToEmpty(user.getSambaLogonScript()));
			
			ps.setInt(idx++, user.getUid());

			int updateCount = ps.executeUpdate();

			if (updateCount != 1) {
				throw new UserNotFoundException(String.format("No user found with id %d and login %s", user.getUid(), user.getLogin()));
			}

			return findUserById(user.getUid(), user.getDomain());
		}
	}

	@VisibleForTesting int getQuotaAsInt0(ObmUser user) {
		return Objects.firstNonNull(user.getMailQuota(), 0);
	}

	@VisibleForTesting Integer quotaToNullable(int quota) {
		return quota != 0 ? quota : null;
	}

	
	@Override
	public void delete(ObmUser user) throws SQLException, UserNotFoundException {
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM UserObm WHERE userobm_ext_id = ? AND userobm_domain_id = ?")) {

			String extId = user.getExtId().getExtId();

			ps.setString(1, extId);
			ps.setInt(2, user.getDomain().getId());

			int updateCount = ps.executeUpdate();

			if (updateCount != 1) {
				throw new UserNotFoundException(String.format("No user found with extid %s.", extId));
			}
		}
	}

	@Override
	public void archive(ObmUser user) throws SQLException, UserNotFoundException {
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement("UPDATE UserObm SET userobm_archive = 1 WHERE userobm_ext_id = ? AND userobm_domain_id = ?")) {

			String extId = user.getExtId().getExtId();
			ps.setString(1, extId);
			ps.setInt(2, user.getDomain().getId());

			int updateCount = ps.executeUpdate();

			if (updateCount != 1) {
				throw new UserNotFoundException(String.format("No user found with extid %s.", extId));
			}
		}
	}

	@Override
	public ImmutableSet<String> getAllEmailsFrom(ObmDomain domain, UserExtId toIgnore) throws SQLException {
		Preconditions.checkArgument(toIgnore != null);
		
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(
				"SELECT userobm_email as mail FROM UserObm " +
				"INNER JOIN Domain ON Domain.domain_id = userobm_domain_id " +
				"WHERE domain_id = ? " +
				"AND userobm_ext_id != ? " +
				"UNION " +
				"SELECT mailshare_email as mail FROM MailShare " +
				"INNER JOIN Domain ON Domain.domain_id = mailshare_domain_id " +
				"WHERE domain_id = ? " +
				"UNION " +
				"SELECT group_email as mail FROM UGroup " + 
				"INNER JOIN Domain ON Domain.domain_id = group_domain_id " +
				"WHERE domain_id = ?")) {

			ps.setInt(1, domain.getId());
			ps.setString(2, toIgnore.getExtId());
			ps.setInt(3, domain.getId());
			ps.setInt(4, domain.getId());
			
			ResultSet rs = ps.executeQuery();
			ImmutableSet.Builder<String> builder = ImmutableSet.builder();
			
			while (rs.next()) {
				builder.addAll(deserializeEmails(rs.getString("mail")));
			}
			
			return builder.build();
		}
	}

	@Override
	public String getUniqueObmDomain(String userLogin) throws DomainNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String domain = null;

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(
					"SELECT domain_name FROM Domain " +
					"INNER JOIN UserObm ON userobm_domain_id = domain_id " +
					"WHERE userobm_login = ?");

			ps.setString(1, userLogin);
			rs = ps.executeQuery();

			if (rs.next()) {
				domain = rs.getString("domain_name");
			} else {
				throw new DomainNotFoundException(String.format("No domain found for login %s", userLogin));
			}

			if (!rs.next()) {
				return domain;
			} else {
				throw new DomainNotFoundException(String.format("The login %s is in several domains (at least %s and  %s).", userLogin, domain, rs.getString("domain_name")));
			}
		} catch (SQLException e){
			throw Throwables.propagate(e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

}
