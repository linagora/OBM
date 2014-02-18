/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.breakdownduration;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expectLastCall;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.breakdownduration.BreakdownDurationFilter;
import org.obm.breakdownduration.BreakdownDurationLoggerService;
import org.obm.breakdownduration.bean.Group;


public class BreakdownDurationFilterTest {

	private IMocksControl mocks;
	private ServletRequest request;
	private ServletResponse response;
	private FilterChain chain;
	private BreakdownDurationLoggerService loggerService;

	private BreakdownDurationFilter testee;

	@Before
	public void setUp() {
		mocks = createControl();
		
		loggerService = mocks.createMock(BreakdownDurationLoggerService.class);
		request = mocks.createMock(ServletRequest.class);
		response = mocks.createMock(ServletResponse.class);
		chain = mocks.createMock(FilterChain.class);
		
		testee = new BreakdownDurationFilter(loggerService);
	}
	
	@Test(expected=CustomRuntimeException.class)
	public void cleanSessionWhenExceptionDuringEnable() throws Exception {
		loggerService.enableRecording();
		expectLastCall().andThrow(new CustomRuntimeException());
		loggerService.endRecordingNode(anyInt());
		expectLastCall();
		loggerService.disableRecording();
		expectLastCall();
		loggerService.log();
		expectLastCall();
		loggerService.cleanSession();
		expectLastCall();
		
		mocks.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test(expected=CustomRuntimeException.class)
	public void cleanSessionWhenExceptionDuringStartNode() throws Exception {
		loggerService.enableRecording();
		expectLastCall();
		loggerService.startRecordingNode(Group.REQUEST);
		expectLastCall().andThrow(new CustomRuntimeException());
		loggerService.endRecordingNode(anyInt());
		expectLastCall();
		loggerService.disableRecording();
		expectLastCall();
		loggerService.log();
		expectLastCall();
		loggerService.cleanSession();
		expectLastCall();
		
		mocks.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test(expected=CustomRuntimeException.class)
	public void cleanSessionWhenExceptionDuringEndNode() throws Exception {
		loggerService.enableRecording();
		expectLastCall();
		loggerService.startRecordingNode(Group.REQUEST);
		expectLastCall();
		loggerService.endRecordingNode(anyInt());
		expectLastCall().andThrow(new CustomRuntimeException());
		loggerService.cleanSession();
		expectLastCall();
		
		mocks.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test(expected=CustomRuntimeException.class)
	public void cleanSessionWhenExceptionDuringDisable() throws Exception {
		loggerService.enableRecording();
		expectLastCall();
		loggerService.startRecordingNode(Group.REQUEST);
		expectLastCall();
		loggerService.endRecordingNode(anyInt());
		expectLastCall();
		loggerService.disableRecording();
		expectLastCall().andThrow(new CustomRuntimeException());
		loggerService.cleanSession();
		expectLastCall();
		
		mocks.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			mocks.verify();
			throw e;
		}
	}
	
	@Test(expected=CustomRuntimeException.class)
	public void cleanSessionWhenExceptionDuringLog() throws Exception {
		loggerService.enableRecording();
		expectLastCall();
		loggerService.startRecordingNode(Group.REQUEST);
		expectLastCall();
		loggerService.endRecordingNode(anyInt());
		expectLastCall();
		loggerService.disableRecording();
		expectLastCall();
		loggerService.log();
		expectLastCall().andThrow(new CustomRuntimeException());
		loggerService.cleanSession();
		expectLastCall();
		
		mocks.replay();
		try {
			testee.doFilter(request, response, chain);
		} catch (Exception e) {
			mocks.verify();
			throw e;
		}
	}
	
	private static class CustomRuntimeException extends RuntimeException {}
}
