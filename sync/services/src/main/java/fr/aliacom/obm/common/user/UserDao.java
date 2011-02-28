package fr.aliacom.obm.common.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class UserDao {

	private static final Log logger = LogFactory.getLog(UserDao.class);
	private final ObmHelper obmHelper;
	
	@Inject
	private UserDao(ObmHelper obmHelper) {
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
				logger.info("found property for " + token.getUser() + "@"
						+ token.getDomain() + ": " + k + " => " + v);
				return map;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ImmutableMap.of();
	}
	
	private Integer userIdFromEmailQuery(Connection con, String mail, String domain)
			throws SQLException {
		Integer ret = null;

		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			// by Mehdi
			rs = st.executeQuery("SELECT userobm_id FROM UserObm "
					+ "INNER JOIN Domain ON userobm_domain_id = domain_id "
					+ "WHERE " + "( " + "(userobm_email = '"
					+ mail
					+ "' OR userobm_email like '"
					+ mail
					+ "\r\n%' OR userobm_email like '%\r\n"
					+ mail
					+ "\r\n%' OR userobm_email like '%\r\n"
					+ mail
					+ "') "
					+ "AND "
					+ "(domain_name = '"
					+ domain
					+ "' OR domain_alias = '"
					+ domain
					+ "' OR domain_alias like '"
					+ domain
					+ "\r\n%' OR domain_alias like '%\r\n"
					+ domain
					+ "\r\n%' OR domain_alias like '%\r\n"
					+ domain
					+ "') "
					+ ") OR (userobm_email = '"
					+ mail
					+ "' OR userobm_email like '"
					+ mail
					+ "\r\n%' OR userobm_email like '%\r\n"
					+ mail
					+ "\r\n%' OR userobm_email like '%\r\n" + mail + "') ");
			if (rs.next()) {
				ret = rs.getInt(1);
			}

		} finally {
			obmHelper.cleanup(null, st, rs);
		}
		return ret;
	}

	public Integer contactEntityFromEmailQuery(Connection con, String mail) {
		Integer ret = null;
		String q = "SELECT email_entity_id FROM Email "
				// prevents linking to an orphan email row
				+ "INNER JOIN ContactEntity ON email_entity_id=contactentity_entity_id "
				+ "WHERE email_address=?";
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = con.prepareStatement(q);
			st.setString(1, mail);
			rs = st.executeQuery();
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, st, rs);
		}
		return ret;
	}

	public Integer userEntityFromEmailQuery(Connection con, String mail)
			throws SQLException {
		String[] parts = mail.split("@");
		String left = parts[0];
		String right = "-";
		if (parts.length > 1) {
			right = parts[1];
		}

		Integer ret = null;

		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			// by Mehdi
			rs = st
					.executeQuery("SELECT userentity_entity_id FROM UserObm INNER JOIN UserEntity ON userobm_id=userentity_user_id "
							+ "INNER JOIN Domain ON userobm_domain_id = domain_id "
							+ "WHERE " + "( " + "(userobm_email = '"
							+ left
							+ "' OR userobm_email like '"
							+ left
							+ "\r\n%' OR userobm_email like '%\r\n"
							+ left
							+ "\r\n%' OR userobm_email like '%\r\n"
							+ left
							+ "') "
							+ "AND "
							+ "(domain_name = '"
							+ right
							+ "' OR domain_alias = '"
							+ right
							+ "' OR domain_alias like '"
							+ right
							+ "\r\n%' OR domain_alias like '%\r\n"
							+ right
							+ "\r\n%' OR domain_alias like '%\r\n"
							+ right
							+ "') "
							+ ") OR (userobm_email = '"
							+ mail
							+ "' OR userobm_email like '"
							+ mail
							+ "\r\n%' OR userobm_email like '%\r\n"
							+ mail
							+ "\r\n%' OR userobm_email like '%\r\n"
							+ mail
							+ "') ");
			if (rs.next()) {
				ret = rs.getInt(1);
			}

		} finally {
			obmHelper.cleanup(null, st, rs);
		}
		return ret;
	}

	public ObmUser findUser(String email) {
		Connection con = null;
		Integer id = null;
		try {
			String[] parts = email.split("@");
			String login = parts[0];
			String domain = "-";
			if (parts.length > 1) {
				domain = parts[1];
			}
			con = obmHelper.getConnection();
			id = userIdFromEmailQuery(con, login, domain);
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		if (id != null && id > 0) {
			return findUserById(id);
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

		ObmUser ret = null;
		String uq = "SELECT userobm_id, userobm_email, userobm_firstname, userobm_lastname, defpref.userobmpref_value, userpref.userobmpref_value "
				+ "FROM UserObm LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=userobm_id "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(uq);
			ps.setInt(1, domain.getId());
			ps.setString(2, login);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = new ObmUser();
				ret.setUid(rs.getInt(1));
				ret.setLogin(login);
				ret.setDomain(domain);
				ret.setEmail(rs.getString(2));
				ret.setFirstName(rs.getString(3));
				ret.setLastName(rs.getString(4));
				ret.setPublicFreeBusy(computePublicFreeBusy(5, rs));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
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

	public ObmUser findUserById(int id) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		ObmUser ret = null;
		String uq = "SELECT userobm_id, userobm_email, userobm_login, defpref.userobmpref_value, userpref.userobmpref_value "
				+ "FROM UserObm LEFT JOIN UserObmPref defpref ON defpref.userobmpref_option='set_public_fb' AND defpref.userobmpref_user_id IS NULL "
				+ "LEFT JOIN UserObmPref userpref ON userpref.userobmpref_option='set_public_fb' AND userpref.userobmpref_user_id=? "
				+ "WHERE userobm_id=? ";
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(uq);
			ps.setInt(1, id);
			ps.setInt(2, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = new ObmUser();
				ret.setUid(rs.getInt(1));
				ret.setEmail(rs.getString(2));
				ret.setLogin(rs.getString(3));
				ret.setPublicFreeBusy(computePublicFreeBusy(4, rs));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	private Integer userIdFromLogin(Connection con, String login, Integer domainId) {
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		Integer ret = null;
		String uq = "SELECT userobm_id "
				+ "FROM UserObm "
				+ "WHERE userobm_domain_id=? AND userobm_login=? AND userobm_archive != '1'";
		try {
			con = obmHelper.getConnection();
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
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}
	
	public Integer userIdFromEmail(Connection con, String calendar, Integer domainId) throws SQLException{
		String[] parts = calendar.split("@");
		String login = parts[0];
		String domain = "-";
		if (parts.length > 1) {
			domain = parts[1];
		}
		Integer ownerId = userIdFromLogin(con, login, domainId);
		if(ownerId == null){
			ownerId = userIdFromEmailQuery(con, calendar, domain);	
		}
		
		return ownerId;
	}
}
