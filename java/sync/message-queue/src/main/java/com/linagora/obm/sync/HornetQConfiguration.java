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
package com.linagora.obm.sync;

import java.util.List;
import java.util.Map;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.settings.impl.AddressSettings;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.hornetq.spi.core.remoting.AcceptorFactory;
import org.hornetq.spi.core.remoting.ConnectorFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HornetQConfiguration {

	public enum Connector {
		HornetQInVMCore(
			connectorBuilder()
				.factory(InVMConnectorFactory.class)
				.name("in-vm")
				.build()),
		HornetQSocketCore(
			connectorBuilder()
				.factory(NettyConnectorFactory.class)
				.name("netty")
				.build());
		
		private TransportConfiguration configuration;

		private Connector(TransportConfiguration configuration) {
			this.configuration = configuration;
		}

		public TransportConfiguration getConfiguration() {
			return configuration;
		}
	}
	
	public enum Acceptor {
		HornetQInVMCore(
				HornetQConfiguration.acceptorBuilder()
				.factory(InVMAcceptorFactory.class)
				.name("in-vm")
				.build()),
		HornetQSocketCore(
			acceptorBuilder()
				.factory(NettyAcceptorFactory.class)
				.name("netty")
				.build()),
		Stomp(
			acceptorBuilder()
				.factory(NettyAcceptorFactory.class)
				.name("stomp-acceptor")
				.param("protocol", "stomp")
				.param("port", 61613)
				.build());
		
		private TransportConfiguration configuration;

		private Acceptor(TransportConfiguration configuration) {
			this.configuration = configuration;
		}
		
		public TransportConfiguration getConfiguration() {
			return configuration;
		}
	}
	
	public static ConfigurationBuilder configuration() {
		return new ConfigurationBuilder();
	}
	
	public static class ConfigurationBuilder {
		
		private static final long DEFAULT_MAX_SIZE_BEFORE_PAGING = 10485760L;
		private static final long DEFAULT_PAGING_SIZE = 1048576L;
		private Boolean persistenceEnabled;
		private Boolean securityEnabled;
		private List<TransportConfiguration> connectors;
		private List<TransportConfiguration> acceptors;
		private String largeMessagesDirectory;
		private String bindingsDirectory;
		private String journalDirectory;
		private String pagingDirectory;
		
		private ConfigurationBuilder() {
			connectors = Lists.newArrayList();
			acceptors = Lists.newArrayList(); 
		}
		
		public ConfigurationBuilder enablePersistence(boolean enable) {
			persistenceEnabled = enable;
			return this;
		}
		
		public ConfigurationBuilder enableSecurity(boolean enable) {
			securityEnabled = enable;
			return this;
		}
		
		public ConfigurationBuilder largeMessagesDirectory(String largeMessagesDirectory) {
			this.largeMessagesDirectory = largeMessagesDirectory;
			return this;
		}
		
		public ConfigurationBuilder bindingsDirectory(String bindingsDirectory) {
			this.bindingsDirectory = bindingsDirectory;
			return this;
		}
		
		public ConfigurationBuilder journalDirectory(String journalDirectory) {
			this.journalDirectory = journalDirectory;
			return this;
		}
		
		public ConfigurationBuilder pagingDirectory(String pagingDirectory) {
			this.pagingDirectory = pagingDirectory;
			return this;
		}

		public ConfigurationBuilder connector(Connector connector) {
			return connector(connector.getConfiguration());
		}

		
		public ConfigurationBuilder connector(TransportConfiguration connector) {
			connectors.add(connector);
			return this;
		}
		
		public ConfigurationBuilder acceptor(Acceptor acceptor) {
			return acceptor(acceptor.getConfiguration());
		}
		
		public ConfigurationBuilder acceptor(TransportConfiguration acceptor) {
			acceptors.add(acceptor);
			return this;
		}
		
		public Configuration build() {
			ConfigurationImpl configurationImpl = new ConfigurationImpl();
			if (persistenceEnabled != null) {
				configurationImpl.setPersistenceEnabled(persistenceEnabled);
			}
			if (securityEnabled != null) {
				configurationImpl.setSecurityEnabled(securityEnabled);
			}
			if (largeMessagesDirectory != null) {
				configurationImpl.setLargeMessagesDirectory(largeMessagesDirectory);
			}
			if (bindingsDirectory != null) {
				configurationImpl.setBindingsDirectory(bindingsDirectory);
			}
			if (journalDirectory != null) {
				configurationImpl.setJournalDirectory(journalDirectory);
			}
			if (pagingDirectory != null) {
				configurationImpl.setPagingDirectory(pagingDirectory);
				AddressSettings addressSettings = new AddressSettings();
				addressSettings.setMaxSizeBytes(DEFAULT_MAX_SIZE_BEFORE_PAGING);
				addressSettings.setPageSizeBytes(DEFAULT_PAGING_SIZE);
				configurationImpl.setAddressesSettings(ImmutableMap.of("#", addressSettings));
			}
			configurationImpl.setConnectorConfigurations(listAsMap(connectors));
			configurationImpl.setAcceptorConfigurations(ImmutableSet.copyOf(acceptors));
			configurationImpl.setJournalType(JournalType.NIO);
			return configurationImpl;
		}

		private ImmutableMap<String, TransportConfiguration> listAsMap(List<TransportConfiguration> entries) {
			return Maps.uniqueIndex(entries, new Function<TransportConfiguration, String>() {

				@Override
				public String apply(TransportConfiguration input) {
					return input.getName();
				}
			});
		}
		
	}
	
	public static AcceptorConfigurationBuilder acceptorBuilder() {
		return new AcceptorConfigurationBuilder();
	}

	public static class AcceptorConfigurationBuilder {
		
		private Class<? extends AcceptorFactory> factory;
		private Map<String, Object> params;
		private String name;
		
		private AcceptorConfigurationBuilder() {
			params = Maps.newHashMap();
		}
		
		public AcceptorConfigurationBuilder factory(Class<? extends AcceptorFactory> factory) {
			this.factory = factory;
			return this;
		}
		
		public AcceptorConfigurationBuilder param(String key, Object value) {
			params.put(key, value);
			return this;
		}
		
		public AcceptorConfigurationBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public TransportConfiguration build() {
			return new TransportConfiguration(factory.getCanonicalName(), params, name);
		}
		
	}


	public static ConnectorConfigurationBuilder connectorBuilder() {
		return new ConnectorConfigurationBuilder();
	}
	
	public static class ConnectorConfigurationBuilder {
		
		private Class<? extends ConnectorFactory> factory;
		private Map<String, Object> params;
		private String name;
		
		private ConnectorConfigurationBuilder() {
			params = Maps.newHashMap();
		}
		
		public ConnectorConfigurationBuilder factory(Class<? extends ConnectorFactory> factory) {
			this.factory = factory;
			return this;
		}
		
		public ConnectorConfigurationBuilder param(String key, Object value) {
			params.put(key, value);
			return this;
		}
		
		public ConnectorConfigurationBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public TransportConfiguration build() {
			return new TransportConfiguration(factory.getCanonicalName(), params, name);
		}
		
	}
	
	public static JMSConfigurationBuilder jmsConfiguration() {
		return new JMSConfigurationBuilder();
	}
	
	public static class JMSConfigurationBuilder {
		
		private final List<ConnectionFactoryConfiguration> connectionFactoryConfigurations;
        private final List<JMSQueueConfiguration> queueConfigurations;
        private final List<TopicConfiguration> topicConfigurations;
        private String domain;

		private JMSConfigurationBuilder() {
			connectionFactoryConfigurations = Lists.newArrayList();
			queueConfigurations = Lists.newArrayList();
			topicConfigurations = Lists.newArrayList();
		}

		public JMSConfigurationBuilder connectionFactory(ConnectionFactoryConfiguration factory) {
			connectionFactoryConfigurations.add(factory);
			return this;
		}
		
		public JMSConfigurationBuilder connectionFactory(String name, String connector, String... bindings) {
			return connectionFactory(
					new ConnectionFactoryConfigurationImpl(name, false, ImmutableList.of(connector), bindings));
		}
		
		public JMSConfigurationBuilder queue(JMSQueueConfiguration queueConfiguration) {
			queueConfigurations.add(queueConfiguration);
			return this;
		}
		
		public JMSConfigurationBuilder topic(TopicConfiguration topic) {
			topicConfigurations.add(topic);
			return this;
		}
		
		public JMSConfigurationBuilder topic(String name, String... bindings) {
			return topic(new TopicConfigurationImpl(name, bindings));
		}
		
		public JMSConfigurationBuilder domain(String domain) {
			this.domain = domain;
			return this;
		}
		
		public JMSConfiguration build() {
			return new JMSConfigurationImpl(
					connectionFactoryConfigurations, 
					queueConfigurations, 
					topicConfigurations, 
					domain);
		}
		
	}
	
	public static ConnectionFactoryConfigurationBuilder connectionFactoryConfigurationBuilder() {
		return new ConnectionFactoryConfigurationBuilder();
	}
	
	public static class ConnectionFactoryConfigurationBuilder {
		
		private final List<String> bindings;
        private final List<String> connectors;
        private String name;
        private Boolean ha;
		private JMSFactoryType factoryType;
		
		private ConnectionFactoryConfigurationBuilder() {
			bindings = Lists.newArrayList();
			connectors = Lists.newArrayList();
		}
		
		public ConnectionFactoryConfigurationBuilder ha(boolean ha) {
			this.ha = ha;
			return this;
		}
		
		public ConnectionFactoryConfigurationBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public ConnectionFactoryConfigurationBuilder binding(String binding) {
			bindings.add(binding);
			return this;
		}
		
		public ConnectionFactoryConfigurationBuilder connector(TransportConfiguration connector) {
			connectors.add(connector.getName());
			return this;
		}
		
		public ConnectionFactoryConfigurationBuilder connector(Connector connector) {
			return connector(connector.getConfiguration());
		}

		public ConnectionFactoryConfigurationBuilder factoryType(JMSFactoryType factoryType) {
			this.factoryType = factoryType;
			return this;
		}
		
		public ConnectionFactoryConfiguration build() {
			ConnectionFactoryConfigurationImpl configuration 
				= new ConnectionFactoryConfigurationImpl(name, 
						Objects.firstNonNull(ha, false), 
						connectors, 
						bindings.toArray(new String[0]));
			if (factoryType != null) {
				configuration.setFactoryType(factoryType);
			}
			return configuration;
		}

	}
	
}
