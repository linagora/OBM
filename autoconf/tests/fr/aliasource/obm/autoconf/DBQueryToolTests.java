package fr.aliasource.obm.autoconf;

import java.util.HashMap;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;

public class DBQueryToolTests extends AutoconfTestCase {

	public void testQuery() {
		DBQueryTool dqt = new DBQueryTool(dbc);
		HashMap<String, String> mailHost = dqt.getDBInformation();
		assertNotNull(mailHost);
		System.out.println("mailHost: "+mailHost);
	}
}
