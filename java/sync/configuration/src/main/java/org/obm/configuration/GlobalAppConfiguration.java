/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.configuration;

public class GlobalAppConfiguration<MainConfigurationType extends ConfigurationService> {

	public static <T extends ConfigurationService> Builder<T> builder() {
		return new Builder<T>();
	}
	
	public static class Builder<MainConfigurationType extends ConfigurationService> {
		
		private MainConfigurationType configurationService;
		private LocatorConfiguration locatorConfiguration;
		private DatabaseConfiguration databaseConfiguration;
		private TransactionConfiguration transactionConfiguration;

		private Builder() {}
		
		public Builder<MainConfigurationType> mainConfiguration(MainConfigurationType configurationService) {
			this.configurationService = configurationService;
			return this;
		}
		
		public Builder<MainConfigurationType> locatorConfiguration(LocatorConfiguration locatorConfiguration) {
			this.locatorConfiguration = locatorConfiguration;
			return this;
		}
		
		public Builder<MainConfigurationType> databaseConfiguration(DatabaseConfiguration databaseConfiguration) {
			this.databaseConfiguration = databaseConfiguration;
			return this;
		}
		
		public Builder<MainConfigurationType> transactionConfiguration(TransactionConfiguration transactionConfiguration) {
			this.transactionConfiguration = transactionConfiguration;
			return this;
		}
		
		public GlobalAppConfiguration<MainConfigurationType> build() {
			return new GlobalAppConfiguration<MainConfigurationType>(configurationService, 
					locatorConfiguration, databaseConfiguration, transactionConfiguration);
		}
		
	}
	
	private final MainConfigurationType configurationService;
	private final LocatorConfiguration locatorConfiguration;
	private final DatabaseConfiguration databaseConfiguration;
	private final TransactionConfiguration transactionConfiguration;

	private GlobalAppConfiguration(MainConfigurationType configurationService, 
			LocatorConfiguration locatorConfiguration, 
			DatabaseConfiguration databaseConfiguration, 
			TransactionConfiguration transactionConfiguration) {
				this.configurationService = configurationService;
				this.locatorConfiguration = locatorConfiguration;
				this.databaseConfiguration = databaseConfiguration;
				this.transactionConfiguration = transactionConfiguration;
	}

	public MainConfigurationType getConfigurationService() {
		return configurationService;
	}
	
	public LocatorConfiguration getLocatorConfiguration() {
		return locatorConfiguration;
	}
	
	public DatabaseConfiguration getDatabaseConfiguration() {
		return databaseConfiguration;
	}
	
	public TransactionConfiguration getTransactionConfiguration() {
		return transactionConfiguration;
	}
	
}
