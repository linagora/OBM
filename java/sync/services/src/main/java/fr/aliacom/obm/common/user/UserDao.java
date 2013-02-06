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
package fr.aliacom.obm.common.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class UserDao {

	private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
	private static final String USER_FIELDS = " userobm_id, userobm_email, userobm_firstname, userobm_lastname, defpref.userobmpref_value, userpref.userobmpref_value, userobm_commonname, userobm_login, userentity_entity_id";
	private final ObmHelper obmHelper;
	
	@Inject
	@VisibleForTesting
	UserDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public Map<String, String> loadUserProperties(AccessToken token) {
		String q = "SELECT serviceproperty_service, serviceproperty_property, serviceproperty_value "
				+ "FROM ServiceProperty "
				+ "INNER JOIN UserEntity ON serviceproperty_entity_id=userentity_entity_id AND userentity_user_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, token.getObmId());
			rs = ps.executeQuery();
			Map<String, String> map = Maps.newHashMap();
			while (rs.next()) {
				String k = rs.getString(1) + "/" + rs.getString(2);
				String v = rs.getString(3);
				map.put(k, v);
				logger.info("found property for " + token.getUserLogin() + "@"
						+ token.getDomain().getName() + ": " + k + " => " + v);
				return map;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ImmutableMap.of();
	}
	
	@VisibleForTesting Integer userIdFromEmailQuery(Connection con, String mail, String domain) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		
		try {
			st = con.createStatement();

			String request = "SELECT userobm_id, userobm_email, domain_name, domain_alias " +
					"FROM UserObm " +
					"INNER JOIN Domain ON domain_id = userobm_domain_id " +
					"WHERE UPPER(userobm_email) like UPPER('%" + mail + "%') AND userobm_archive != 1";
			
			rs = st.executeQuery(request);
			
			while (rs.next()) {
				
				int id = rs.getInt(1);
				String emailsToCompare = rs.getString(2); 
				String domainNameToCompare = rs.getString(3);
				String domainsAliasToCompare = rs.getString(4);
				
				if (compareMail(mail, emailsToCompare)) {
					
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

	private boolean strictCompareDomain(String domain, String domainNameToCompare, String domainsAliasToCompare) {
		if (domain != null && domainNameToCompare != null) {
			if (domain.equals(domainNameToCompare)) {
				return true;
			}
			
			if (domainsAliasToCompare != null) {
				Iterable<String> domains = Splitter.on("\r\n").split(domainsAliasToCompare);					
				for (String domainToCompare: domains) {
					if (domain.equals(domainToCompare)) {
						return true;
					}
				}	
			}
		}
		return false;
	}

	private boolean compareMail(String mail, String emailsToCompare) {
		if (mail != null && emailsToCompare != null) {
			Iterable<String> emails = Splitter.on("\r\n").split(emailsToCompare);			
			for (String email: emails) {
				email = getEmailWithoutDomain(email);
				if (mail.equalsIgnoreCase(email)) {
					return true;
				}
			}	
		}
		return false;
	}

	private String getEmailWithoutDomain(String email) {
		if (email != null && email.contains("@")) {
			String[] s = email.split("@");
			return s[0];
		}
		return email;
	}
	
	public ObmUser findUser(String email, ObmDomain domain) {
		Connection con = null;
		Integer id = null;
		try {
			con = obmHelper.getConnection();
			id = userIdFromEmail(con, email, domain.getId());
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

	private ObmUser createUserFromResultSet(ObmDomain domain, ResultSet rs) throws SQLException {
		ObmUser obmUser;
		obmUser = new ObmUser();
		obmUser.setUid(rs.getInt(1));
		obmUser.setLogin(rs.getString("userobm_login"));
		obmUser.setDomain(domain);
		setEmailAndAlias(obmUser, rs.getString(2));
		obmUser.setFirstName(rs.getString(3));
		obmUser.setLastName(rs.getString(4));
		obmUser.setPublicFreeBusy(computePublicFreeBusy(5, rs));
		obmUser.setFirstName(rs.getString("userobm_firstname"));
		obmUser.setLastName(rs.getString("userobm_lastname"));
		obmUser.setCommonName(rs.getString("userobm_commonname"));
		obmUser.setEntityId(rs.getInt("userentity_entity_id"));
		
		return obmUser;
	}

	private void setEmailAndAlias(ObmUser obmUser, String emails) {
		Iterable<String> emailAndAlias = Splitter.on("\r\n").split(emails);
		for (String emailOrAlias: emailAndAlias) {
			if (obmUser.getEmail() == null) {
				obmUser.setEmail(emailOrAlias);	
			} else {
				obmUser.addAlias(emailOrAlias);
			}			
		}
	}
	
	public ObmUser findUserById(int id, ObmDomain domain) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ObmUser obmUser = null;
		String uq = "SELECT " + USER_FIELDS
				+ " FROM UserObm "
				+ "INNER JOIN UserEntity ON userentity_user_id = userobm_id "
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

	
	@VisibleForTesting Integer userIdFromLogin(Connection con, String login, Integer domainId) {
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		Integer ret = null;
		String uq = "SELECT userobm_id "
				+ "FROM UserObm "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try {
			ps = con.prepareStatement(uq);
			ps.setInt(1, domainId);
			ps.setString(2, login);
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
		String login = parts[0];
		String domain = "-";
		Integer ownerId = null;
		
		// OBMFULL-4353
		// We only fetch the user by login if a login was provided
		// If a full email is given, we must favor the fetch by email
		if (parts.length > 1) {
			domain = parts[1];
		} else {
			ownerId = userIdFromLogin(con, login, domainId);
		}
		
		if(ownerId == null){
			ownerId = userIdFromEmailQuery(con, login, domain);	
		}
		
		return ownerId;
	}
}
