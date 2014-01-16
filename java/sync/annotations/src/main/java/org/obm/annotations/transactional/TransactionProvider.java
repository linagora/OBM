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
package org.obm.annotations.transactional;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.slf4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class TransactionProvider implements Provider<TransactionManager> {
	
	private BitronixTransactionManager transactionManager;

	@Inject
	public TransactionProvider(TransactionConfiguration configuration,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) throws SystemException {
		int transactionTimeOutInSecond = configuration.getTimeOutInSecond();
		configurationLogger.info("Transaction timeout in seconds : {}", transactionTimeOutInSecond);
		configureBitronix(configuration);
		transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.setTransactionTimeout(transactionTimeOutInSecond);
	}

	private void configureBitronix(TransactionConfiguration configuration) {
		Configuration btConfiguration = TransactionManagerServices.getConfiguration();
		if (configuration.enableJournal()) {
			btConfiguration.setLogPart1Filename(configuration.getJournalPart1Path().getAbsolutePath());
			btConfiguration.setLogPart2Filename(configuration.getJournalPart2Path().getAbsolutePath());	
		} else {
			btConfiguration.setJournal("null");
		}
		btConfiguration.setDefaultTransactionTimeout(configuration.getTimeOutInSecond());
	}
	
	@Override
	public BitronixTransactionManager get() {
		return transactionManager;
	}
}