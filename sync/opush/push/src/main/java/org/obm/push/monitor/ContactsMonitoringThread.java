package org.obm.push.monitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.obm.dbcp.DBCP;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.impl.ChangedCollections;
import org.obm.push.store.SyncCollection;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class ContactsMonitoringThread extends MonitoringThread {

	@Singleton
	public static class Factory {
		private final DBCP dbcp;
		private final IContentsExporter contentsExporter;

		@Inject
		private Factory(DBCP dbcp, IContentsExporter contentsExporter) {
			this.dbcp = dbcp;
			this.contentsExporter = contentsExporter;
		}

		public ContactsMonitoringThread createClient(long freqMs,
				Set<ICollectionChangeListener> ccls) {
			
			return new ContactsMonitoringThread(freqMs, ccls,
					this.dbcp, this.contentsExporter);
		}
	}
	
	private static final String CHANGED_UIDS = "		select "
			+ "		distinct sa.user_id, now() "
			+ "		from SyncedAddressbook sa "
			+ "		inner join AddressBook ab on ab.id=sa.addressbook_id "
			+ "		where ab.timeupdate >= ? or ab.timecreate >= ? or sa.timestamp >= ? ";

	private static final String POLL_QUERY = "		select "
			+ "		uo.userobm_login, d.domain_name "
			+ "		FROM UserObm uo "
			+ "		inner join Domain d on d.domain_id=uo.userobm_domain_id WHERE uo.userobm_id IN ";
	
	private ContactsMonitoringThread(long freqMs,
			Set<ICollectionChangeListener> ccls,
			DBCP dbcp, IContentsExporter contentsExporter) {

		super(freqMs, ccls, dbcp, contentsExporter);
	}

	@Override
	protected ChangedCollections getChangedCollections(Date lastSync) {

		Date dbDate = lastSync;
		Set<SyncCollection> changed = new HashSet<SyncCollection>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(lastSync.getTime());
		Timestamp ts = new Timestamp(cal.getTimeInMillis());
		if (logger.isDebugEnabled()) {
			logger.debug("poll date is " + cal.getTime());
		}
		int idx = 1;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(CHANGED_UIDS);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			dbDate = fillChangedCollections(con, rs, changed, lastSync);
		} catch (Throwable t) {
			logger.error("Error running changed uids query", t);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		if (logger.isInfoEnabled() && changed.size() > 0) {
			logger.info("changed collections: " + changed.size() + " dbDate: "
					+ dbDate);
		}

		return new ChangedCollections(dbDate, changed);
	}

	private Date fillChangedCollections(Connection con, ResultSet rs,
			Set<SyncCollection> changed, Date lastSync) throws SQLException {
		Date ret = lastSync;

		int i = 0;
		StringBuilder ids = new StringBuilder("(0");
		while (rs.next()) {
			int id = rs.getInt(1);
			ids.append(", " + id);
			if (i == 0) {
				ret = new Date(rs.getTimestamp(2).getTime());
			}
			i++;
		}
		ids.append(")");

		if (i > 0) {

			PreparedStatement ps = null;
			ResultSet res = null;
			try {
				ps = con.prepareStatement(POLL_QUERY + ids.toString());
				res = ps.executeQuery();
				while (res.next()) {
					StringBuilder colName = new StringBuilder(255);
					colName.append("obm:\\\\");
					colName.append(res.getString(1));
					colName.append('@');
					colName.append(res.getString(2));
					colName.append("\\contacts");

					SyncCollection sc = new SyncCollection();
					String s = colName.toString();
					sc.setCollectionPath(s);
					changed.add(sc);
					if (logger.isInfoEnabled()) {
						logger.info("Detected contacts change for " + s);
					}

				}

			} catch (Throwable t) {
				logger.error("Error running calendar poll query", t);
			} finally {
				JDBCUtils.cleanup(null, ps, res);
			}

		}

		return ret;
	}

}
