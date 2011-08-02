/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.services.constant;

import org.obm.configuration.ObmConfigurationService;
import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Configuration service
 */
@Singleton
public class ConstantService extends ObmConfigurationService {

	private static final String DEFAULT_TEMPLATE_FOLDER = "/usr/share/obm-sync/resources";
	private static final String OVERRIDE_TEMPLATE_FOLDER = "/etc/obm-sync/resources/template/";
	private static final String OBM_SYNC_MAILER = "x-obm-sync";

	@Inject
	private ConstantService() {
		super();
	}

	public String getDefaultTemplateFolder() {
		return DEFAULT_TEMPLATE_FOLDER;
	}

	public String getOverrideTemplateFolder() {
		return OVERRIDE_TEMPLATE_FOLDER;
	}

	public String getObmSyncMailer(AccessToken at) {
		return OBM_SYNC_MAILER + "@" + at.getDomain();
	}
}
