package fr.aliacom.jndi;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.obm.aliapool.pool.DataSource;

public class DataSourceFactory implements ObjectFactory {

	private static Log logger = LogFactory.getLog(DataSourceFactory.class);
	private static HashMap<String, fr.aliasource.obm.aliapool.pool.DataSource> sources;

	static {
		sources = new HashMap<String, DataSource>();
	}

	public DataSourceFactory() {
		logger.info("DataSourceFactory created");
	}

	@Override
	public synchronized Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception {

		if (sources.containsKey(name.toString())) {
			logger.info("Stopping previous instance of '" + name.toString()
					+ "' datasource");
			fr.aliasource.obm.aliapool.pool.DataSource ds = sources.get(name.toString());
			ds.stop();
		}

		HashMap<String, String> env = new HashMap<String, String>();
		Reference ref = (Reference) obj;
		Enumeration<RefAddr> addrs = ref.getAll();
		while (addrs.hasMoreElements()) {
			RefAddr addr = (RefAddr) addrs.nextElement();
			env.put(addr.getType(), (String) addr.getContent());
		}
		fr.aliasource.obm.aliapool.pool.DataSource ret = getDs(env);
		sources.put(name.toString(), ret);
		return ret;
	}

	private fr.aliasource.obm.aliapool.pool.DataSource getDs(HashMap<String, String> env)
		throws Exception {
		return create(env);
	}

	private fr.aliasource.obm.aliapool.pool.DataSource create(HashMap<String, String> env)
			throws SQLException, ClassNotFoundException {
		logger.info("Initializing datasource...");
		String className = env.get("className");
		logger.info("   o className: " + className);

		String login = env.get("login");
		logger.info("   o login: " + login);

		String password = env.get("password");
		logger.info("   o password: " + password);

		String url = env.get("url");
		logger.info("   o url: " + url);

		String pingQuery = env.get("pingQuery");
		if (pingQuery != null) {
			logger.info("   o pingQuery: " + pingQuery);
		} else {
			pingQuery = "SELECT 1";
		}

		int max = Integer.parseInt(env.get("max"));
		logger.info("   o max: " + max);

		return new fr.aliasource.obm.aliapool.pool.DataSource(className, url,
				login, password, null, max, pingQuery);
	}

}
