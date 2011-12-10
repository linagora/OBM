package fr.aliasource.obm.autoconf;

import java.util.HashMap;

import javax.transaction.TransactionManager;

import org.obm.dbcp.DBCP;
import org.obm.dbcp.IDBCP;

import bitronix.tm.TransactionManagerServices;
import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;
import fr.aliasource.obm.utils.ObmHelper;

public class DBQueryToolTests extends AutoconfTestCase {

	public void testQuery() {
		TransactionManager transactionManager = TransactionManagerServices.getTransactionManager();
		IDBCP dbcp = new DBCP(transactionManager);

		ObmHelper helper = new ObmHelper(dbcp);
		DBQueryTool dqt = new DBQueryTool(helper);
		HashMap<String, String> mailHost = dqt.getDBInformation("thomas", "zz.com");
		assertNotNull(mailHost);
		System.out.println("mailHost: "+mailHost);

		mailHost = dqt.getDBInformation("doesnotexist", "notdomain.fr");
		assertNotNull(mailHost);
		System.out.println("mailHost: "+mailHost);
	}
}
