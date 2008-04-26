package fr.aliasource.obm.autoconf;

import junit.framework.TestCase;
import fr.aliasource.obm.utils.ConstantService;

public abstract class AutoconfTestCase extends TestCase {

	protected DirectoryConfig dc;

	protected void setUp() throws Exception {
		super.setUp();
		ConstantService cs = ConstantService.getInstance();
		dc = new DirectoryConfig("thomas@zz.com", cs);
	}

	protected void tearDown() throws Exception {
		dc = null;
		super.tearDown();
	}

}
