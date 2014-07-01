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
package org.obm.sync.locators;

import org.obm.breakdownduration.bean.Watch;
import org.obm.configuration.DomainConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.utils.jvm.VMArgumentsUtils;
import org.obm.sync.BreakdownGroups;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Watch(BreakdownGroups.CLIENT_LOCATOR)
public class Locator {

	protected static final String OBM_SYNC_SERVICE = "sync/obm_sync";
	protected static final String OBM_SYNC_HOST = "obmSyncHost";
	
	private final LocatorService locatorService;
	private final DomainConfiguration domainConfiguration;

	@Inject
	protected Locator(DomainConfiguration domainConfiguration, LocatorService locatorService) {
		this.domainConfiguration = domainConfiguration;
		this.locatorService = locatorService;
	}
	
	public String backendUrl(String loginAtDomain) throws LocatorClientException {
		String obmSyncHost = getObmSyncHost(loginAtDomain);
		return domainConfiguration.getObmSyncServicesUrl(obmSyncHost);
	}
	
	public String backendBaseUrl(String loginAtDomain) throws LocatorClientException {
		String obmSyncHost = getObmSyncHost(loginAtDomain);
		return domainConfiguration.getObmSyncBaseUrl(obmSyncHost);
	}	
	
	@VisibleForTesting String getObmSyncHost(String loginAtDomain) throws LocatorClientException {
		String obmSyncHost = VMArgumentsUtils.stringArgumentValue(OBM_SYNC_HOST);
		if (Strings.isNullOrEmpty(obmSyncHost)) {
			return locatorService.getServiceLocation(OBM_SYNC_SERVICE, loginAtDomain);
		}
		return obmSyncHost;
	}
	
}
