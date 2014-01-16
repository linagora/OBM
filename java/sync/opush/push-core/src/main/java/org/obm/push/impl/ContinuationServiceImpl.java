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
package org.obm.push.impl;

import org.obm.push.ContinuationService;
import org.obm.push.ContinuationTransactionMap;
import org.obm.push.ElementNotFoundException;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.Device;
import org.obm.push.bean.UserDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class ContinuationServiceImpl implements ContinuationService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final ContinuationTransactionMap<IContinuation> continuationTransactionMap;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject
	@VisibleForTesting ContinuationServiceImpl(ContinuationTransactionMap continuationTransactionMap) {
		this.continuationTransactionMap = continuationTransactionMap;
	}

	@Override
	public void suspend(UserDataRequest userDataRequest, IContinuation continuation, long secondsTimeout, String cancellingStatus) {
		logger.debug("suspend {} {} {}", userDataRequest.getDevice(), secondsTimeout, cancellingStatus);
		continuation.error(cancellingStatus);
		
		boolean hasPreviousElement = continuationTransactionMap.putContinuationForDevice(userDataRequest.getDevice(), continuation);
		if (hasPreviousElement) {
			logger.error("Continuation was already cached for device {}", userDataRequest.getDevice());
		}
		continuation.suspend(userDataRequest, secondsTimeout);
	}

	@Override
	public void resume(Device device) {
		try {
			logger.debug("resume {}", device);
			
			IContinuation continuation = continuationTransactionMap.getContinuationForDevice(device);
			continuationTransactionMap.delete(device);
			continuation.resume();
		} catch (ElementNotFoundException e) {
			logger.debug("resume device {} not found", device);
		}
		
	}

	@Override
	public void cancel(Device device) {
		try {
			logger.debug("cancel {} {}", device);
			
			IContinuation continuation = continuationTransactionMap.getContinuationForDevice(device);
			continuationTransactionMap.delete(device);
			continuation.resume();
		} catch (ElementNotFoundException e) {
			logger.debug("cancel device {} not found", device);
		}
	}
}
