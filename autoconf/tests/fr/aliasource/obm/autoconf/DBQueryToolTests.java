package fr.aliasource.obm.autoconf;

import java.util.HashMap;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;

public class DBQueryToolTests extends AutoconfTestCase {

	public void testQuery() {
		DBQueryTool dqt = new DBQueryTool();
		HashMap<String, String> mailHost = dqt.getDBInformation("thomas", "zz.com");
		assertNotNull(mailHost);
		System.out.println("mailHost: "+mailHost);

		mailHost = dqt.getDBInformation("doesnotexist", "notdomain.fr");
		assertNotNull(mailHost);
		System.out.println("mailHost: "+mailHost);
	}
}
