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
package org.obm.sync.server.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import org.obm.sync.server.mailer.AbstractMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class TemplateLoaderFreeMarkerImpl implements ITemplateLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(CalendarBindingImpl.class);

	private ObmSyncConfigurationService constantService;

	@Inject
	public TemplateLoaderFreeMarkerImpl(ObmSyncConfigurationService constantService) {
		this.constantService = constantService;
	}

	private Configuration getDefaultCfg() {
		Configuration externalCfg = new Configuration();
		externalCfg
				.setClassForTemplateLoading(AbstractMailer.class, "template");
		return externalCfg;
	}

	private Configuration getOverrideCfg() throws IOException {
		Configuration externalCfg = new Configuration();
		externalCfg.setDirectoryForTemplateLoading(new File(constantService
				.getOverrideTemplateFolder()));
		return externalCfg;
	}

	public Template getTemplate(String templateName, Locale locale,
			TimeZone timezone) throws IOException {
		Template ret = null;
		try {
			ret = getOverrideCfg().getTemplate(templateName, locale);
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Error while loading Template[ " + templateName
								+ "] in "
								+ constantService.getOverrideTemplateFolder(),
						e);
			}
		}
		if (ret == null) {
			ret = getDefaultCfg().getTemplate(templateName, locale);
		}
		if (ret == null) {
			throw new FileNotFoundException("Error while loading Template[ "
					+ templateName + "] in "
					+ constantService.getDefaultTemplateFolder());
		}
		ret.setTimeZone(timezone);
		return ret;
	}
}
