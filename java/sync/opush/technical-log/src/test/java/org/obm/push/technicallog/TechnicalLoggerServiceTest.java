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
package org.obm.push.technicallog;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import net.sf.ehcache.Element;

import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.push.technicallog.TechnicalLoggerService;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.bean.jaxb.Resource;
import org.obm.push.technicallog.bean.jaxb.Transaction;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestNotFoundException;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestStore;
import org.slf4j.Logger;


public class TechnicalLoggerServiceTest {

	@Test
	public void testTrace() {
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).once();
		logger.trace(anyObject(String.class));
		expectLastCall().once();

		replay(logger);
		
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, null);
		technicalLoggerService.trace(Transaction.builder()
				.id(Thread.currentThread().getId())
				.transactionStartTime(DateTime.now())
				.build());
		
		verify(logger);
	}
	
	@Test
	public void testTraceStartedRequest() {
		long transactionId = Thread.currentThread().getId();
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.requestId(1)
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		RequestStore requestStore = createStrictMock(RequestStore.class);
		expect(requestStore.put(transactionId, request))
			.andReturn(null).once();
		
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).times(2);
		logger.trace(anyObject(String.class));
		expectLastCall().once();
		
		replay(requestStore, logger);
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, requestStore);
		technicalLoggerService.traceStartedRequest(request);
		
		verify(requestStore, logger);
	}
	
	@Test
	public void testTraceStartedRequestWithPrevious() {
		long transactionId = Thread.currentThread().getId();
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.requestId(1)
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		Element previous = new Element(transactionId, request);
		RequestStore requestStore = createStrictMock(RequestStore.class);
		expect(requestStore.put(transactionId, request))
			.andReturn(previous).once();
		
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).times(2);
		logger.trace(anyObject(String.class));
		expectLastCall().once();
		
		replay(requestStore, logger);
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, requestStore);
		technicalLoggerService.traceStartedRequest(request);
		
		verify(requestStore, logger);
	}
	
	@Test
	public void testTraceEndedRequest() {
		long transactionId = Thread.currentThread().getId();
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.requestId(1)
				.transactionId(transactionId)
				.requestEndTime(DateTime.now())
				.build();
		
		RequestStore requestStore = createStrictMock(RequestStore.class);
		requestStore.delete(transactionId);
		expectLastCall();
		
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).times(2);
		logger.trace(anyObject(String.class));
		expectLastCall().once();
		
		replay(requestStore, logger);
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, requestStore);
		technicalLoggerService.traceEndedRequest(request);
		
		verify(requestStore, logger);
	}
	
	@Test
	public void testTraceResource() throws Exception {
		Resource resource = Resource.builder()
				.resourceId(Long.valueOf(1))
				.resourceType(ResourceType.HTTP_CLIENT)
				.resourceStartTime(DateTime.now())
				.build();
		
		long transactionId = Thread.currentThread().getId();
		Request request = Request.builder()
				.deviceId("devId")
				.deviceType("devType")
				.command("Sync")
				.requestId(1)
				.transactionId(transactionId)
				.requestStartTime(DateTime.now())
				.build();
		
		RequestStore requestStore = createStrictMock(RequestStore.class);
		expect(requestStore.getRequest(transactionId))
			.andReturn(request).once();
		
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).times(2);
		logger.trace(anyObject(String.class));
		expectLastCall().once();
		
		replay(requestStore, logger);
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, requestStore);
		technicalLoggerService.traceResource(resource);
		
		verify(requestStore, logger);
		assertThat(request.getResources()).containsOnly(resource);
	}
	
	@Test
	public void testTraceResourceWithoutRequest() throws Exception {
		Resource resource = Resource.builder()
				.resourceId(Long.valueOf(1))
				.resourceType(ResourceType.HTTP_CLIENT)
				.resourceStartTime(DateTime.now())
				.build();
		
		RequestStore requestStore = createStrictMock(RequestStore.class);
		expect(requestStore.getRequest(Thread.currentThread().getId()))
			.andThrow(new RequestNotFoundException()).once();
		
		Logger logger = createStrictMock(Logger.class);
		expect(logger.isTraceEnabled())
			.andReturn(true).times(2);
		logger.trace(anyObject(String.class));
		expectLastCall().once();
		
		replay(requestStore, logger);
		
		TechnicalLoggerService technicalLoggerService = new TechnicalLoggerService(logger, requestStore);
		technicalLoggerService.traceResource(resource);
		
		verify(requestStore, logger);
	}
}
