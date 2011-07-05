/*
 * Created on Oct 29, 2003
 *
 */
package fr.aliasource.obm.aliapool.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.aliapool.tm.TransactionManager;
import fr.aliasource.obm.aliapool.tm.Tx;

/**
 * @author tom
 * 
 */
public class ConnectionPool {

	private static Log logger = LogFactory.getLog(ConnectionPool.class);

	private Vector<ConnectionProxy> freeList;

	private String url;
	private String login;
	private String password;
	private int max;

	private Timer cron;
	private HashMap<Transaction, ConnectionProxy> txConMapping;
	private TransactionManager tm;

	private Semaphore pingMutex;
	private Semaphore conListSem;

	private Properties jdbcProperties;

	public ConnectionPool(String url, String login, String password,
			Properties jdbcProperties, int max, String pingQuery)
			throws SQLException {
		this.txConMapping = new HashMap<Transaction, ConnectionProxy>();
		this.url = url;
		this.login = login;
		this.password = password;
		this.max = max;
		this.jdbcProperties = jdbcProperties;
		pingMutex = new Semaphore(1);
		conListSem = new Semaphore(max);

		freeList = new Vector<ConnectionProxy>(max + 1);

		for (int i = 0; i < max; i++) {
			freeList.add(createPhysicalConnection());
		}

		logger.info("Pool State after start is " + (max - freeList.size())
				+ " used out of " + max + " connections");
		PingThread pt = new PingThread(freeList, pingQuery);
		pt.setPingMutex(pingMutex);
		cron = new Timer();
		cron.scheduleAtFixedRate(pt, 0, 1000 * 120); // 2 minutes
		this.tm = TransactionManager.getInstance();
	}

	private ConnectionProxy createPhysicalConnection() throws SQLException {
		logger.info("Creating Physical connection...");
		try {
			Connection con = getNewPhysicalConnection();
			ConnectionProxy ret = new ConnectionProxy(con, this);
			logger.info("Physical connection established.");
			return ret;
		} catch (SQLException sqle) {
			logger.error("Could not create DB connection to " + url
					+ " with l / p: " + login + " / " + password);
			throw sqle;
		}
	}

	Connection getNewPhysicalConnection() throws SQLException {
		if (jdbcProperties != null) {
			return DriverManager.getConnection(url, jdbcProperties);
		} else {
			return DriverManager.getConnection(url, login, password);
		}
	}

	public Connection getConnection() throws SQLException, InterruptedException {
		try {
			Transaction tx = tm.getTransaction();
			// no transaction in progress
			if (tx == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Not in tx, giving a plain connection");
				}
				return getPhysicalConnection();
				// transaction with enlisted jdbc connection
			} else if (tx.getStatus() == Status.STATUS_ACTIVE
					&& txConMapping.containsKey(tx)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Connection already enlisted.");
				}
				return txConMapping.get(tx);
				// transaction without enlisted connection (first query)
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Enlisting connection.");
				}
				ConnectionProxy ret = getPhysicalConnection();
				tx.enlistResource(ret);
				txConMapping.put(tx, ret);
				return ret;
			}
		} catch (SystemException e) {
			logger.error("Cannot get current transaction", e);
		} catch (IllegalStateException e) {
			logger.error(e, e);
		} catch (RollbackException e) {
			logger.error(e.getMessage(), e);
		}
		logger.error("returning null JDBC connection");
		return null;
	}

	private ConnectionProxy getPhysicalConnection() throws InterruptedException {
		ConnectionProxy ret = null;
		conListSem.acquire();
		boolean success = pingMutex.tryAcquire(5000, TimeUnit.MILLISECONDS);
		if (!success) {
			logger.error("try acquire failed.");
			return null;
		}
		ret = freeList.get(0);
		freeList.remove(0);
		pingMutex.release();

		if (logger.isDebugEnabled()) {
			logger.debug("Pool State is " + (max - freeList.size())
					+ " used out of " + max + " connections");
		}
		return ret;
	}

	void releaseConnection(ConnectionProxy proxy) throws SQLException,
			InterruptedException {

		if (freeList.contains(proxy)) {
			return;
		}

		Transaction tx;
		try {
			tx = (Tx) TransactionManager.getInstance().getTransaction();
			if (txConMapping.containsKey(tx)) {
				txConMapping.remove(tx);
				tx.delistResource(proxy, 0);
			}
		} catch (SystemException e) {
			logger.error(e, e);
		}
		pingMutex.acquire();
		freeList.add(proxy);
		pingMutex.release();
		conListSem.release();
		if (logger.isDebugEnabled()) {
			logger.debug("Pool State is " + (max - freeList.size())
					+ " used out of " + max + " connections");
		}
	}

	void stop() throws SQLException, InterruptedException {
		logger.info("  Cleaning pool.");
		cron.cancel();
		for (int i = 0; i < freeList.size(); i++) {
			ConnectionProxy c = freeList.get(i);
			c.getPhysicalConnection().close();
		}
		freeList.clear();
		logger.info("  Pool cleaned.");
	}

}
