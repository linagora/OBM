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

import java.io.File;

import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.utils.IniFile;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImapArchiveConfigurationServiceImpl implements ImapArchiveConfigurationService {

	@VisibleForTesting static final String CYRUS_PARTITION_SUFFIX = "partition_suffix";
	public static final String DEFAULT_CYRUS_PARTITION_SUFFIX = "archive";
	
	@VisibleForTesting static final String ARCHIVE_MAIN_FOLDER = "main_folder";
	public static final String DEFAULT_ARCHIVE_MAIN_FOLDER = "ARCHIVE";
	
	@VisibleForTesting static final String PROCESSING_BATCH_SIZE = "batch_size";
	public static final int DEFAULT_PROCESSING_BATCH_SIZE = 20;
	
	@VisibleForTesting static final String QUOTA_MAX_SIZE = "quota_max_size";
	public static final int DEFAULT_QUOTA_MAX_SIZE = Integer.MAX_VALUE;
	
	@VisibleForTesting static final String TRANSACTION_TIMEOUT_IN_SECONDS = "transaction_timeout_in_seconds";
	public static final int DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS = 3600;
	
	public static class Factory {
		
		protected IniFile iniFile;
		private final TransactionConfiguration transactionConfiguration;

		@Inject
		public Factory(TransactionConfiguration transactionConfiguration) {
			this(transactionConfiguration, new IniFile.Factory().build(ImapArchiveConfigurationModule.IMAP_ARCHIVE_CONFIG_FILE_PATH));
		}
		
		@VisibleForTesting Factory(TransactionConfiguration transactionConfiguration, IniFile iniFile) {
			this.transactionConfiguration = transactionConfiguration;
			this.iniFile = iniFile;
		}
		
		public ImapArchiveConfigurationServiceImpl create() {
			return checkCorrectnessForEarlyFail(
					new ImapArchiveConfigurationServiceImpl(iniFile, transactionConfiguration));
		}

		private ImapArchiveConfigurationServiceImpl checkCorrectnessForEarlyFail(ImapArchiveConfigurationServiceImpl conf) {
			checkCyrusPartitionSuffix(conf);
			return conf;
		}

		private void checkCyrusPartitionSuffix(ImapArchiveConfigurationServiceImpl conf) {
			String value = conf.getCyrusPartitionSuffix();
			Preconditions.checkArgument(!Strings.isNullOrEmpty(value), CYRUS_PARTITION_SUFFIX + " cannot be null or empty");
			
			String onlyLowerCaseChars = value.toLowerCase();
			Preconditions.checkArgument(onlyLowerCaseChars.equals(value), CYRUS_PARTITION_SUFFIX + " must only use lowercase");
		}
	}
	
	private final IniFile iniFile;
	private final TransactionConfiguration transactionConfiguration;
	
	@VisibleForTesting ImapArchiveConfigurationServiceImpl(IniFile iniFile, TransactionConfiguration transactionConfiguration) {
		this.iniFile = iniFile;
		this.transactionConfiguration = transactionConfiguration;
	}

	@Override
	public String getCyrusPartitionSuffix() {
		return iniFile.getStringValue(CYRUS_PARTITION_SUFFIX, DEFAULT_CYRUS_PARTITION_SUFFIX);
		
	}

	@Override
	public String getArchiveMainFolder() {
		return iniFile.getStringValue(ARCHIVE_MAIN_FOLDER, DEFAULT_ARCHIVE_MAIN_FOLDER);
	}

	@Override
	public int getProcessingBatchSize() {
		return iniFile.getIntValue(PROCESSING_BATCH_SIZE, DEFAULT_PROCESSING_BATCH_SIZE);
	}

	@Override
	public int getQuotaMaxSize() {
		return iniFile.getIntValue(QUOTA_MAX_SIZE, DEFAULT_QUOTA_MAX_SIZE);
	}

	@Override
	public int getTimeOutInSecond() {
		return iniFile.getIntValue(TRANSACTION_TIMEOUT_IN_SECONDS, DEFAULT_TRANSACTION_TIMEOUT_IN_SECONDS);
	}

	@Override
	public File getJournalPart1Path() {
		return transactionConfiguration.getJournalPart1Path();
	}

	@Override
	public File getJournalPart2Path() {
		return transactionConfiguration.getJournalPart2Path();
	}

	@Override
	public boolean enableJournal() {
		return transactionConfiguration.enableJournal();
	}

}
