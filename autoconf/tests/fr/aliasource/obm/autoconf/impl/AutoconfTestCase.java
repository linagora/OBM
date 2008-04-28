package fr.aliasource.obm.autoconf.impl;

import junit.framework.TestCase;
import fr.aliasource.obm.autoconf.DBConfig;
import fr.aliasource.obm.autoconf.DirectoryConfig;
import fr.aliasource.obm.utils.ConstantService;

public abstract class AutoconfTestCase extends TestCase {

	protected DirectoryConfig dc;
	protected DBConfig dbc;
	private DataSourceEnv dse;

	protected void setUp() throws Exception {
		super.setUp();
		dse = new DataSourceEnv();
		dse.bindPool();
		ConstantService cs = ConstantService.getInstance();
		dc = new DirectoryConfig("thomas@zz.com", cs);
		dbc = new DBConfig(cs,"thomas","zz.com");
	}

	protected void tearDown() throws Exception {
		dse.shutdown();
		dse = null;
		dc = null;
		dbc = null;
		super.tearDown();
	}

}
