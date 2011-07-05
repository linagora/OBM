/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.pool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author tom
 * 
 */
public class PingThread extends TimerTask {

	private String query;

	private Vector<ConnectionProxy> freeList;
	private Log logger;
	private Semaphore pingMutex;

	public PingThread(Vector<ConnectionProxy> freeList, String query) {
		this.query = query;
		logger = LogFactory.getLog(getClass());
		this.freeList = freeList;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			ping();
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}

	private void ping() throws InterruptedException {
		int count = 0;
		Statement st = null;
		ResultSet rs = null;
		pingMutex.acquire();
		int size = freeList.size();
		for (int i = 0; i < size; i++) {
			ConnectionProxy con = null;
			try {
				con = freeList.get(i);
				st = con.createStatement();
				rs = st.executeQuery(query);
				count++;
			} catch (Throwable t) {
				if (con != null) {
					con.recycleConnection();
				}
			} finally {
				try {
					if (st != null)
						st.close();
					if (rs != null)
						rs.close();
				} catch (SQLException e) {
				}
			}

		}

		pingMutex.release();
		if (count > 0) {
			logger.info(count + " unused connections pinged.");
		} else {
			logger.warn(count
					+ " unused connections pinged. SQL connection leak ?");
		}
	}

	/**
	 * @param mutex
	 */
	public void setPingMutex(Semaphore mutex) {
		pingMutex = mutex;
	}

}
