/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliasource.obm.autoconf.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import bitronix.tm.TransactionManagerServices;


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
			props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("test-data/db.properties not found. Null stream");
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("no test-data/db.properties found");
			return;
		}

		props.list(System.out);
	

		TransactionManager tm = TransactionManagerServices.getTransactionManager();

		Transaction ut = tm.getTransaction();
		Class.forName(props.getProperty("driver"));
/*
		DataSource ds = new javax.sql.DataSource(
				props.getProperty("url"), props.getProperty("login"), props
						.getProperty("password"), 5, "SELECT 1");*/

		System.setProperty("java.naming.factory.initial",
				"fr.aliasource.obm.autoconf.impl.MemoryContextFactory");
		InitialContext ic = new InitialContext();

		Context ctx = ic.createSubcontext("comp");
		ctx.bind("UserTransaction", ut);
		ctx.bind("TransactionManager", tm);
		ctx = ctx.createSubcontext("env").createSubcontext("jdbc");

		ctx.bind("AutoConfDS", null);
		poolBinded = true;
		System.out.println("Test case fully initialised.");
	}

	public void shutdown() {
		/*fr.aliacom.pool.DataSource pool = (fr.aliacom.pool.DataSource) new InitialContext().lookup(ObmHelper.DATA_SOURCE);
		pool.stop();*/
	}
}
