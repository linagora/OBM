package fr.aliasource.obm.autoconf.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import fr.aliasource.obm.utils.ObmHelper;


public class DataSourceEnv {
	private static boolean poolBinded = false;

	protected DataSourceEnv() {
	}

	public void bindPool() throws Exception {
		if (poolBinded) {
			return;
		}
		Properties props = new Properties();
		try {
			InputStream in = new FileInputStream("test-data/db.properties");

			if (in != null) {
				props.load(in);
			} else {
				System.out
						.println("test-data/db.properties not found. Null stream");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("no test-data/db.properties found");
			return;
		}

		props.list(System.out);
	

		UserTransaction ut = (UserTransaction) fr.aliacom.tm.TransactionManager
				.getInstance();
		TransactionManager tm = fr.aliacom.tm.TransactionManager.getInstance();

		Class.forName(props.getProperty("driver"));

		DataSource ds = new fr.aliacom.pool.DataSource(
				props.getProperty("url"), props.getProperty("login"), props
						.getProperty("password"), 5, "SELECT 1");

		System.setProperty("java.naming.factory.initial",
				"fr.aliasource.obm.autoconf.impl.MemoryContextFactory");
		InitialContext ic = new InitialContext();

		Context ctx = ic.createSubcontext("comp");
		ctx.bind("UserTransaction", ut);
		ctx.bind("TransactionManager", tm);
		ctx = ctx.createSubcontext("env").createSubcontext("jdbc");

		ctx.bind("AutoConfDS", ds);
		poolBinded = true;
		System.out.println("Test case fully initialised.");
	}

	public void shutdown() throws NamingException, SQLException, InterruptedException {
		fr.aliacom.pool.DataSource pool = (fr.aliacom.pool.DataSource) new InitialContext().lookup(ObmHelper.DATA_SOURCE);
		pool.stop();
	}
}
