package fr.aliasource.obm.autoconf;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;

public class DBQueryToolTests extends AutoconfTestCase {

	public void testQuery() {
		DBQueryTool dqt = new DBQueryTool(dbc);
		String mailHost = dqt.getDBInformation();
		assertNotNull(mailHost);
		System.out.println("mailHost :"+mailHost);
	}
}
