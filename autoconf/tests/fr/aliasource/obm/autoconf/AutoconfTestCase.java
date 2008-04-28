package fr.aliasource.obm.autoconf;

import junit.framework.TestCase;
import fr.aliasource.obm.utils.ConstantService;

public abstract class AutoconfTestCase extends TestCase {

	protected DirectoryConfig dc;
	protected DBConfig dbc;

	protected void setUp() throws Exception {
		super.setUp();
		ConstantService cs = ConstantService.getInstance();
		dc = new DirectoryConfig("thomas@zz.com", cs);
		dbc = new DBConfig(cs,"thomas","zz.com");
	}

	protected void tearDown() throws Exception {
		dc = null;
		dbc = null;
		super.tearDown();
	}

}
