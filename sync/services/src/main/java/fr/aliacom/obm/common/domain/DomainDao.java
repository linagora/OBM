package fr.aliacom.obm.common.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class DomainDao {

	private static final Logger logger = LoggerFactory
			.getLogger(DomainDao.class);
	private final ObmHelper obmHelper;

	@Inject
	private DomainDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public ObmDomain findDomainByName(String domainName) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String uq = "SELECT domain_id FROM Domain WHERE domain_name=?";
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(uq);
			ps.setString(1, domainName);
			rs = ps.executeQuery();
			if (rs.next()) {
				ObmDomain ret = new ObmDomain();
				ret.setId(rs.getInt(1));
				ret.setName(domainName);
				return ret;
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return null;
	}
	
}
