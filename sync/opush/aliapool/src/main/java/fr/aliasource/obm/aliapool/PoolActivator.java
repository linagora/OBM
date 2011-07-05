package fr.aliasource.obm.aliapool;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.aliapool.pool.DataSource;
import fr.aliasource.obm.aliapool.tm.TransactionManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class PoolActivator {

	private TransactionManager tm;
	private Log logger;

	private static PoolActivator instance;
	private PoolActivator() {
		logger = LogFactory.getLog(getClass());
		tm = TransactionManager.getInstance();
	
		logger.info("Pool bundle started.");
	}

	public static PoolActivator getInstance() {
		if (instance == null) {
			instance = new PoolActivator();
		}
		return instance;
	}

	public TransactionManager getTransactionManager() {
		return tm;
	}

	public DataSource createDataSource(String driverClass, String url,
			String login, String password, int max, String pingQuery)
			throws SQLException {
		return new DataSource(driverClass, url, login, password,
				null, max, pingQuery);
	}

	/**
	 * Creates a new datasource with the given jdbc properties, driver class & url.
	 */
	public DataSource createDataSource(String driverClass, String url,
			Properties jdbcProps, int max, String pingQuery)
			throws SQLException {
		return new DataSource(driverClass, url, null, null, jdbcProps,
				max, pingQuery);
	}
	
}
