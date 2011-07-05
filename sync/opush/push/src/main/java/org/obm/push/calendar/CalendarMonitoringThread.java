package org.obm.push.calendar;

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
import org.obm.push.impl.ChangedCollections;
import org.obm.push.impl.MonitoringThread;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.SyncCollection;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.calendar.EventType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CalendarMonitoringThread extends MonitoringThread {

	@Singleton
	public static class Factory {
		private final ObmSyncBackend backend;
		private final DBCP dbcp;

		@Inject
		private Factory(CalendarBackend backend, DBCP dbcp) {
			this.backend = backend;
			this.dbcp = dbcp;
		}

		public CalendarMonitoringThread createClient(long freqMs,
				Set<ICollectionChangeListener> ccls) {
			
			return new CalendarMonitoringThread(freqMs, ccls, this.backend,
					this.dbcp);
		}
	}

	private static final String POLL_QUERY = "select "
			+ "userobm_login, domain_name, now(), e.event_type as type "
			+ "from EventLink "
			+ "inner join UserEntity ON userentity_entity_id=eventlink_entity_id "
			+ "inner join UserObm on userobm_id=userentity_user_id "
			+ "inner join Domain on userobm_domain_id=domain_id "
			+ "inner join Event e on eventlink_event_id=e.event_id "
			+ "where eventlink_timeupdate >= ? OR eventlink_timecreate >= ? OR event_timeupdate >= ? OR event_timecreate >= ? "
			+ "UNION " + "select "
			+ "userobm_login, domain_name, now(), deletedevent_type as type "
			+ "from DeletedEvent "
			+ "inner join UserObm on deletedevent_user_id=userobm_id "
			+ "inner join Domain on userobm_domain_id=domain_id "
			+ "where deletedevent_timestamp >= ? ";

	private CalendarMonitoringThread(long freqMs,
			Set<ICollectionChangeListener> ccls, ObmSyncBackend backend,
			DBCP dbcp) {

		super(freqMs, ccls, backend, dbcp);
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
			ps = con.prepareStatement(POLL_QUERY);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			dbDate = fillChangedCollections(rs, changed, lastSync);
		} catch (Throwable t) {
			logger.error("Error running calendar poll query", t);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		if (logger.isInfoEnabled() && changed.size() > 0) {
			logger.info("changed collections: " + changed.size() + " dbDate: "
					+ dbDate);
		}

		return new ChangedCollections(dbDate, changed);
	}

	private Date fillChangedCollections(ResultSet rs,
			Set<SyncCollection> changed, Date lastSync) throws SQLException {
		Date ret = lastSync;
		int i = 0;
		while (rs.next()) {
			String login = rs.getString(1);
			String domain = rs.getString(2);
			ret = new Date(rs.getTimestamp(3).getTime());
			EventType type = EventType.valueOf(rs.getString(4));
			StringBuffer colName = new StringBuffer(255);
			colName.append("obm:\\\\");
			colName.append(login);
			colName.append('@');
			colName.append(domain);
			if (EventType.VTODO.equals(type)) {
				colName.append("\\tasks\\");
			} else {
				colName.append("\\calendar\\");
			}
			colName.append(login);
			colName.append('@');
			colName.append(domain);

			SyncCollection sc = new SyncCollection();
			String s = colName.toString();
			sc.setCollectionPath(s);
			changed.add(sc);
			i++;
			if (logger.isInfoEnabled()) {
				logger.info("Detected cal change for " + s);
			}
		}
		return ret;
	}

}
