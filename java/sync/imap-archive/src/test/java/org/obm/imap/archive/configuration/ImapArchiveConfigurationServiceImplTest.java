/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.CYRUS_PARTITION_SUFFIX;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.DEFAULT_CYRUS_PARTITION_SUFFIX;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.DEFAULT_PROCESSING_BATCH_SIZE;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.PROCESSING_BATCH_SIZE;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.QUOTA_MAX_SIZE;
import static org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl.TRANSACTION_TIMEOUT_IN_SECONDS;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.utils.IniFile;


public class ImapArchiveConfigurationServiceImplTest {

	private IMocksControl control;
	private TransactionConfiguration transactionConfiguration;
	private IniFile iniFile;
	private ImapArchiveConfigurationServiceImpl testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		transactionConfiguration = control.createMock(TransactionConfiguration.class);
		iniFile = control.createMock(IniFile.class);
		
		testee = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
	}
	
	@Test
	public void factoryCreateShouldReturnServiceWhenFieldsAreValid() {
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn("archivetest");
		
		control.replay();
		ImapArchiveConfigurationServiceImpl.Factory factory = new ImapArchiveConfigurationServiceImpl.Factory(transactionConfiguration, iniFile);
		ImapArchiveConfigurationServiceImpl service = factory.create();
		control.verify();
		
		assertThat(service).isNotNull();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void factoryCreateShouldTriggerExceptionWhenPartitionSuffixContainsUpperCase() {
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn("archiveTest");
		
		control.replay();
		try {
			new ImapArchiveConfigurationServiceImpl.Factory(transactionConfiguration, iniFile).create();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void factoryCreateShouldTriggerExceptionWhenEmptyCyrusPartitionSuffix() {
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn("");
		
		control.replay();
		try {
			new ImapArchiveConfigurationServiceImpl.Factory(transactionConfiguration, iniFile).create();
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void cyrusPartitionSuffixShouldTriggerExceptionWhenContainsUpperCase() {
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn("archiveTest");
		
		control.replay();
		try {
			new ImapArchiveConfigurationServiceImpl.Factory(transactionConfiguration, iniFile).create();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void cyrusPartitionSuffixShouldReturnInFileValue() {
		String expectedCyrusPartitionSuffix = "mypartitionsuffix";
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn(expectedCyrusPartitionSuffix);
		
		control.replay();
		String cyrusPartitionSuffix = testee.getCyrusPartitionSuffix();
		control.verify();
		
		assertThat(cyrusPartitionSuffix).isEqualTo(expectedCyrusPartitionSuffix);
	}
	
	@Test
	public void cyrusPartitionSuffixShouldBeDefaultWhenNone() {
		expect(iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn(DEFAULT_CYRUS_PARTITION_SUFFIX);
		
		control.replay();
		String cyrusPartitionSuffix = testee.getCyrusPartitionSuffix();
		control.verify();
		
		assertThat(cyrusPartitionSuffix).isEqualTo(DEFAULT_CYRUS_PARTITION_SUFFIX);
	}
	
	@Test
	public void processingBatchSizeShouldBeDefaultValueWhenNotInFile() {
		expect(iniFile.getIntValue(PROCESSING_BATCH_SIZE, DEFAULT_PROCESSING_BATCH_SIZE))
			.andReturn(DEFAULT_PROCESSING_BATCH_SIZE);
		
		control.replay();
		int processingBatchSize = testee.getProcessingBatchSize();
		control.verify();
		
		assertThat(processingBatchSize).isEqualTo(DEFAULT_PROCESSING_BATCH_SIZE);
	}
	
	@Test
	public void processingBatchSizeShouldReturnInFileValue() {
		int expectedProcessingBatchSize = 123;
		expect(iniFile.getIntValue(PROCESSING_BATCH_SIZE, DEFAULT_PROCESSING_BATCH_SIZE))
			.andReturn(expectedProcessingBatchSize);
		
		control.replay();
		int processingBatchSize = testee.getProcessingBatchSize();
		control.verify();
		
		assertThat(processingBatchSize).isEqualTo(expectedProcessingBatchSize);
	}
	
	@Test
	public void quotaMaxSizeShouldBeDefaultValueWhenNotInFile() {
		expect(iniFile.getIntValue(QUOTA_MAX_SIZE, DEFAULT_QUOTA_MAX_SIZE))
			.andReturn(DEFAULT_QUOTA_MAX_SIZE);
		
		control.replay();
		int quotaMaxSize = testee.getQuotaMaxSize();
		control.verify();
		
		assertThat(quotaMaxSize).isEqualTo(DEFAULT_QUOTA_MAX_SIZE);
	}
	
	@Test
	public void quotaMaxSizeShouldReturnInFileValue() {
		int expectedQuotaMaxSize = 1234;
		expect(iniFile.getIntValue(QUOTA_MAX_SIZE, DEFAULT_QUOTA_MAX_SIZE))
			.andReturn(expectedQuotaMaxSize);
		
		control.replay();
		int quotaMaxSize = testee.getQuotaMaxSize();
		control.verify();
		
		assertThat(quotaMaxSize).isEqualTo(expectedQuotaMaxSize);
	}
	
	@Test
	public void timeOutInSecondShouldBeDefaultValueWhenNotInFile() {
		expect(iniFile.getIntValue(TRANSACTION_TIMEOUT_IN_SECONDS, DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS))
			.andReturn(DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS);
		
		control.replay();
		int timeOutInSecond = testee.getTimeOutInSecond();
		control.verify();
		
		assertThat(timeOutInSecond).isEqualTo(DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS);
	}
	
	@Test
	public void timeOutInSecondShouldReturnInFileValue() {
		int expectedTimeOutInSecond = 12345;
		expect(iniFile.getIntValue(TRANSACTION_TIMEOUT_IN_SECONDS, DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS))
			.andReturn(expectedTimeOutInSecond);
		
		control.replay();
		int timeOutInSecond = testee.getTimeOutInSecond();
		control.verify();
		
		assertThat(timeOutInSecond).isEqualTo(expectedTimeOutInSecond);
	}
}
