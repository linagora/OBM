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
package org.obm.push.technicallog.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.DateTime;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.technicallog.TechnicalLoggerService;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.bean.jaxb.Resource;
import org.obm.push.technicallog.bean.jaxb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class TechnicalLoggingBinder implements ITechnicalLoggingBinder {
	
	private final static Logger logger = LoggerFactory.getLogger(TechnicalLoggingBinder.class);
	
	private final TechnicalLoggerService technicalLoggerService;
	
	@Inject
	@VisibleForTesting public TechnicalLoggingBinder(TechnicalLoggerService technicalLoggerService) {
		this.technicalLoggerService = technicalLoggerService;
	}
	
	@Override
	public void logTransaction(DateTime startTime, DateTime endTime) {
		technicalLoggerService.trace(Transaction.builder()
				.id(Thread.currentThread().getId())
				.transactionStartTime(startTime)
				.transactionEndTime(endTime)
				.build());
	}

	@Override
	public void logRequest(MethodInvocation methodInvocation, DateTime startTime, DateTime endTime) {
		UserDataRequest userDataRequest = 
				(UserDataRequest) getCorrespondingObjectFromMethodParameters(UserDataRequest.class, methodInvocation.getArguments());
		if (userDataRequest == null) {
			logger.debug("Parameter userDataRequest not found in method {}", methodInvocation.getMethod().getName());
			return;
		}
		
		IContinuation continuation = 
				(IContinuation) getCorrespondingObjectFromMethodParameters(IContinuation.class, methodInvocation.getArguments());
		if (continuation == null) {
			logger.debug("Parameter continuation not found in method {}", methodInvocation.getMethod().getName());
			return;
		}
		
		logStartRequest(startTime, userDataRequest, continuation); 
		
		logEndRequest(endTime, userDataRequest, continuation); 
	}

	@VisibleForTesting Object getCorrespondingObjectFromMethodParameters(Class<?> clazz, Object[] arguments) {
		for (Object argument : arguments) {
			if (argument.getClass().equals(clazz) || 
					checkInterface(argument.getClass(), clazz)) {
				return argument;
			}
		}
		return null;
	}

	@VisibleForTesting boolean checkInterface(Class<?> argument, Class<?> clazz) {
		if (!clazz.isInterface()) {
			return false;
		}
		
		for (Class<?> interfazz : argument.getInterfaces()) {
			if (interfazz.equals(clazz))
				return true;
		}
		return false;
	}

	private void logStartRequest(DateTime startTime, UserDataRequest userDataRequest, IContinuation continuation) {
		if (startTime != null) {
			technicalLoggerService.traceStartedRequest(Request.builder()
					.deviceId(userDataRequest.getDevId().getDeviceId())
					.deviceType(userDataRequest.getDevType())
					.command(userDataRequest.getCommand())
					.requestId(continuation.getReqId())
					.transactionId(Thread.currentThread().getId())
					.requestStartTime(startTime)
					.build());
		}
	}

	private void logEndRequest(DateTime endTime, UserDataRequest userDataRequest, IContinuation continuation) {
		if (endTime != null) {
			technicalLoggerService.traceEndedRequest(Request.builder()
					.deviceId(userDataRequest.getDevId().getDeviceId())
					.deviceType(userDataRequest.getDevType())
					.command(userDataRequest.getCommand())
					.requestId(continuation.getReqId())
					.transactionId(Thread.currentThread().getId())
					.requestEndTime(endTime)
					.build());
		}
	}

	@Override
	public void logResource(ResourceType resourceType, DateTime startTime, DateTime endTime) {
		technicalLoggerService.traceResource(Resource.builder()
				.resourceType(resourceType)
				.transactionId(Thread.currentThread().getId())
				.resourceStartTime(startTime)
				.resourceEndTime(endTime)
				.build());
	}
}
