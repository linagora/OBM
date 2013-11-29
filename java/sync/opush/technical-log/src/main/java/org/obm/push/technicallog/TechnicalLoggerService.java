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

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;

import net.sf.ehcache.Element;

import org.obm.push.technicallog.bean.jaxb.JAXBBean;
import org.obm.push.technicallog.bean.jaxb.Request;
import org.obm.push.technicallog.bean.jaxb.Resource;
import org.obm.push.technicallog.jaxb.JAXBParser;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestNotFoundException;
import org.obm.push.technicallog.jaxb.store.ehcache.RequestStore;
import org.obm.push.utils.stream.UTF8Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TechnicalLoggerService {
	
	private static final Logger logger = LoggerFactory.getLogger(TechnicalLoggerService.class);

	private final Logger technicalLogger;
	private final RequestStore requestStore;
	
	@Inject
	@VisibleForTesting TechnicalLoggerService(@Named(TechnicalLoggingModule.TECHNICAL_LOG)Logger technicalLogger, RequestStore requestStore) {
		this.technicalLogger = technicalLogger;
		this.requestStore = requestStore;
	}
	
	public void trace(JAXBBean jaxbBean) {
		if (technicalLogger.isTraceEnabled()) {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				JAXBParser.marshal(jaxbBean, byteArrayOutputStream);
				technicalLogger.trace(UTF8Utils.asString(byteArrayOutputStream));
			} catch (JAXBException e) {
				logger.error("JAXB serialization failed", e);
			}
		}
	}
	
	public void traceStartedRequest(Request request) {
		if (technicalLogger.isTraceEnabled()) {
			trace(request);
			Element previous = requestStore.put(Thread.currentThread().getId(), request);
			if (previous != null) {
				logger.error("Request {} already mapped", request.getRequestId());
			}
		}
	}
	
	public void traceEndedRequest(Request request) {
		if (technicalLogger.isTraceEnabled()) {
			trace(request);
			requestStore.delete(Thread.currentThread().getId());
		}
	}
	
	public void traceResource(Resource resource) {
		if (technicalLogger.isTraceEnabled()) {
			try {
				Request request = requestStore.getRequest(Thread.currentThread().getId());
				request.add(resource);
				trace(request);
			} catch (RequestNotFoundException e) {
				trace(resource);
			}
		}
	}
}
