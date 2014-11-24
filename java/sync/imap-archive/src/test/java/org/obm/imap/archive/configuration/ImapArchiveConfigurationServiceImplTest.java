/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.utils.IniFile;


public class ImapArchiveConfigurationServiceImplTest {

	private IMocksControl control;
	
	private TransactionConfiguration transactionConfiguration; 
	
	@Before
	public void setup() {
		control = createControl();
		
		transactionConfiguration = control.createMock(TransactionConfiguration.class);
	}
	
	@Test
	public void cyrusPartitionSuffixShouldBeDefaultValueWhenNotInFile() {
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getStringValue(ImapArchiveConfigurationServiceImpl.CYRUS_PARTITION_SUFFIX, ImapArchiveConfigurationServiceImpl.DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_CYRUS_PARTITION_SUFFIX);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		String cyrusPartitionSuffix = imapArchiveConfigurationServiceImpl.getCyrusPartitionSuffix();
		control.verify();
		
		assertThat(cyrusPartitionSuffix).isEqualTo(ImapArchiveConfigurationServiceImpl.DEFAULT_CYRUS_PARTITION_SUFFIX);
	}
	
	@Test
	public void cyrusPartitionSuffixShouldReturnInFileValue() {
		String expectedCyrusPartitionSuffix = "mypartitionsuffix";
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getStringValue(ImapArchiveConfigurationServiceImpl.CYRUS_PARTITION_SUFFIX, ImapArchiveConfigurationServiceImpl.DEFAULT_CYRUS_PARTITION_SUFFIX))
			.andReturn(expectedCyrusPartitionSuffix);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		String cyrusPartitionSuffix = imapArchiveConfigurationServiceImpl.getCyrusPartitionSuffix();
		control.verify();
		
		assertThat(cyrusPartitionSuffix).isEqualTo(expectedCyrusPartitionSuffix);
	}
	
	@Test
	public void archiveMainFolderShouldBeDefaultValueWhenNotInFile() {
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getStringValue(ImapArchiveConfigurationServiceImpl.ARCHIVE_MAIN_FOLDER, ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER))
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		String archiveMainFolder = imapArchiveConfigurationServiceImpl.getArchiveMainFolder();
		control.verify();
		
		assertThat(archiveMainFolder).isEqualTo(ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER);
	}
	
	@Test
	public void archiveMainFolderShouldReturnInFileValue() {
		String expectedArchiveMainFolder = "myfolder";
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getStringValue(ImapArchiveConfigurationServiceImpl.ARCHIVE_MAIN_FOLDER, ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER))
			.andReturn(expectedArchiveMainFolder);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		String archiveMainFolder = imapArchiveConfigurationServiceImpl.getArchiveMainFolder();
		control.verify();
		
		assertThat(archiveMainFolder).isEqualTo(expectedArchiveMainFolder);
	}
	
	@Test
	public void processingBatchSizeShouldBeDefaultValueWhenNotInFile() {
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.PROCESSING_BATCH_SIZE, ImapArchiveConfigurationServiceImpl.DEFAULT_PROCESSING_BATCH_SIZE))
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_PROCESSING_BATCH_SIZE);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int processingBatchSize = imapArchiveConfigurationServiceImpl.getProcessingBatchSize();
		control.verify();
		
		assertThat(processingBatchSize).isEqualTo(ImapArchiveConfigurationServiceImpl.DEFAULT_PROCESSING_BATCH_SIZE);
	}
	
	@Test
	public void processingBatchSizeShouldReturnInFileValue() {
		int expectedProcessingBatchSize = 123;
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.PROCESSING_BATCH_SIZE, ImapArchiveConfigurationServiceImpl.DEFAULT_PROCESSING_BATCH_SIZE))
			.andReturn(expectedProcessingBatchSize);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int processingBatchSize = imapArchiveConfigurationServiceImpl.getProcessingBatchSize();
		control.verify();
		
		assertThat(processingBatchSize).isEqualTo(expectedProcessingBatchSize);
	}
	
	@Test
	public void quotaMaxSizeShouldBeDefaultValueWhenNotInFile() {
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.QUOTA_MAX_SIZE, ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE))
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int quotaMaxSize = imapArchiveConfigurationServiceImpl.getQuotaMaxSize();
		control.verify();
		
		assertThat(quotaMaxSize).isEqualTo(ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE);
	}
	
	@Test
	public void quotaMaxSizeShouldReturnInFileValue() {
		int expectedQuotaMaxSize = 1234;
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.QUOTA_MAX_SIZE, ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE))
			.andReturn(expectedQuotaMaxSize);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int quotaMaxSize = imapArchiveConfigurationServiceImpl.getQuotaMaxSize();
		control.verify();
		
		assertThat(quotaMaxSize).isEqualTo(expectedQuotaMaxSize);
	}
	
	@Test
	public void timeOutInSecondShouldBeDefaultValueWhenNotInFile() {
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.TRANSACTION_TIMEOUT_IN_SECONDS, ImapArchiveConfigurationServiceImpl.DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS))
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int timeOutInSecond = imapArchiveConfigurationServiceImpl.getTimeOutInSecond();
		control.verify();
		
		assertThat(timeOutInSecond).isEqualTo(ImapArchiveConfigurationServiceImpl.DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS);
	}
	
	@Test
	public void timeOutInSecondShouldReturnInFileValue() {
		int expectedTimeOutInSecond = 12345;
		IniFile iniFile = control.createMock(IniFile.class);
		expect(iniFile.getIntValue(ImapArchiveConfigurationServiceImpl.TRANSACTION_TIMEOUT_IN_SECONDS, ImapArchiveConfigurationServiceImpl.DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS))
			.andReturn(expectedTimeOutInSecond);
		
		control.replay();
		ImapArchiveConfigurationServiceImpl imapArchiveConfigurationServiceImpl = new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration);
		int timeOutInSecond = imapArchiveConfigurationServiceImpl.getTimeOutInSecond();
		control.verify();
		
		assertThat(timeOutInSecond).isEqualTo(expectedTimeOutInSecond);
	}
}
