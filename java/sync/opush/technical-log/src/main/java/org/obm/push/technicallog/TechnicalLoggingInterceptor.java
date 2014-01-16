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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.DateTime;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.obm.push.technicallog.logger.ITechnicalLoggingBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class TechnicalLoggingInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(TechnicalLoggingInterceptor.class);

	@Inject
	private ITechnicalLoggingBinder technicalLoggingBinder;
	
	public TechnicalLoggingInterceptor() {
		super();
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		TechnicalLogging technicalLogging = readTechnicalLoggintMetadata(methodInvocation);
		if (technicalLogging == null) {
			logger.debug("no TechnicalLogging annotation found on {}", methodInvocation.getMethod().getName());
			return methodInvocation.proceed();
		}
		
		if (technicalLogging.onStartOfMethod()) {
			log(technicalLogging, methodInvocation, DateTime.now(), null);
		}
		
		try {
			return methodInvocation.proceed();
		} finally {
			if (technicalLogging.onEndOfMethod()) {
				log(technicalLogging, methodInvocation, null, DateTime.now());
			}
		}
	}

	private TechnicalLogging readTechnicalLoggintMetadata(MethodInvocation methodInvocation) {
		Method method = methodInvocation.getMethod();

		if (method.isAnnotationPresent(TechnicalLogging.class)) {
			return method.getAnnotation(TechnicalLogging.class);
		}
		return null;
	}

	private void log(TechnicalLogging technicalLogging, MethodInvocation methodInvocation, DateTime startTime, DateTime endTime) {
		switch(technicalLogging.kindToBeLogged()){
			case TRANSACTION:
				technicalLoggingBinder.logTransaction(startTime, endTime);
				break;
			case REQUEST:
				technicalLoggingBinder.logRequest(methodInvocation, startTime, endTime);
				break;
			case RESOURCE:
				technicalLoggingBinder.logResource(technicalLogging.resourceType(), startTime, endTime);
				break;
		}
	}

}
