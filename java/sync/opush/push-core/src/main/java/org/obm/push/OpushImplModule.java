/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.ConfigurationModule;
import org.obm.push.backend.BackendWindowingService;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.PolicyConfigurationService;
import org.obm.push.backend.PolicyConfigurationServiceFileImpl;
import org.obm.push.handler.BackendWindowingServiceImpl;
import org.obm.push.impl.ClientIdServiceImpl;
import org.obm.push.impl.ContinuationServiceImpl;
import org.obm.push.impl.ContinuationTransactionMapImpl;
import org.obm.push.impl.DateServiceImpl;
import org.obm.push.impl.DeviceServiceImpl;
import org.obm.push.impl.MappingServiceImpl;
import org.obm.push.impl.PushPublishAndSubscribeImpl;
import org.obm.push.protocol.data.ASTimeZoneConverter;
import org.obm.push.protocol.data.ASTimeZoneConverterImpl;
import org.obm.push.protocol.data.Base64ASTimeZoneDecoder;
import org.obm.push.protocol.data.Base64ASTimeZoneDecoderImpl;
import org.obm.push.protocol.data.TimeZoneConverter;
import org.obm.push.protocol.data.TimeZoneConverterImpl;
import org.obm.push.protocol.data.TimeZoneEncoder;
import org.obm.push.protocol.data.TimeZoneEncoderImpl;
import org.obm.push.qos.OpushQoSKeyProvider;
import org.obm.push.search.ISearchSource;
import org.obm.push.search.ldap.BookSource;
import org.obm.push.service.ClientIdService;
import org.obm.push.service.DateService;
import org.obm.push.service.DeviceService;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.service.impl.MappingService;
import org.obm.push.state.IStateMachine;
import org.obm.push.state.StateMachine;
import org.obm.push.store.DaoModule;
import org.obm.servlet.filter.qos.QoSContinuationSupport;
import org.obm.servlet.filter.qos.QoSFilterModule;
import org.obm.servlet.filter.qos.QoSRequestHandler;
import org.obm.servlet.filter.qos.handlers.BusinessKeyProvider;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestHandler;
import org.obm.servlet.filter.qos.handlers.NPerClientQoSRequestSuspendHandler;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.services.AttendeeService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;


public class OpushImplModule extends AbstractModule {

	/**
	 * Opush must accepts only one requests per client to prevent lock timeout on same resources
	 */
	private static final int MAX_REQUESTS_PER_CLIENT = 1;
	private static final String APPLICATION_NAME = "opush";
	private static final String APPLICATION_ORIGIN = "o-push";
	

	@Override
	protected void configure() {
		install(new TransactionalModule());
		install(new TechnicalLoggingModule());
		install(new DaoModule());
		install(qosModule());
		install(new OpushServletModule());
		install(new TechnicalLogServletModule());
		install(new ConfigurationModule());
		install(new org.obm.push.configuration.ConfigurationModule());
		bind(BackendWindowingService.class).to(BackendWindowingServiceImpl.class);
		bind(IHierarchyExporter.class).to(HierarchyExporter.class);
		bind(IContentsExporter.class).to(ContentsExporter.class);
		bind(IStateMachine.class).to(StateMachine.class);
		bind(IContentsImporter.class).to(ContentsImporter.class);
		bind(DeviceService.class).to(DeviceServiceImpl.class);
		bind(PushPublishAndSubscribe.Factory.class).to(PushPublishAndSubscribeImpl.Factory.class);
		bind(MappingService.class).to(MappingServiceImpl.class);
		bind(String.class).annotatedWith(Names.named("origin")).toInstance(APPLICATION_ORIGIN);
		bind(String.class).annotatedWith(Names.named("application-name")).toInstance(APPLICATION_NAME);
		bind(TimeZoneEncoder.class).to(TimeZoneEncoderImpl.class);
		bind(TimeZoneConverter.class).to(TimeZoneConverterImpl.class);
		bind(Base64ASTimeZoneDecoder.class).to(Base64ASTimeZoneDecoderImpl.class);
		bind(ASTimeZoneConverter.class).to(ASTimeZoneConverterImpl.class);
		bind(ContinuationTransactionMap.class).to(ContinuationTransactionMapImpl.class);
		bind(ContinuationService.class).to(ContinuationServiceImpl.class);
		bind(DateService.class).to(DateServiceImpl.class);
		bind(ClientIdService.class).to(ClientIdServiceImpl.class);
		bind(QoSContinuationSupport.class).to(OpushContinuationSupport.class);
		bind(AttendeeService.class).to(SimpleAttendeeService.class);
		bind(String.class).annotatedWith(Names.named("opushPolicyConfigurationFile")).toInstance("/etc/opush/policy.ini");
		bind(PolicyConfigurationService.class).to(PolicyConfigurationServiceFileImpl.class);
		Multibinder<ISearchSource> searchSources = Multibinder.newSetBinder(binder(), ISearchSource.class);
		searchSources.addBinding().to(BookSource.class);
	}
	
	private Module qosModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				bind(new TypeLiteral<BusinessKeyProvider<Integer>>(){}).to(OpushQoSKeyProvider.class);
				bind(Integer.class).annotatedWith(Names.named(NPerClientQoSRequestHandler.MAX_REQUESTS_PER_CLIENT_PARAM)).toInstance(MAX_REQUESTS_PER_CLIENT);
				bind(QoSRequestHandler.class).to(new TypeLiteral<NPerClientQoSRequestSuspendHandler<Integer>>(){});
				install(new QoSFilterModule());
			}
		};
	}
}
