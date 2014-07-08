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
package org.obm.imap.archive.startup;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.scheduling.OnlyOnePerDomainScheduler;
import org.obm.server.LifeCycleHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class RestoreTasksOnStartupHandler implements LifeCycleHandler {

	public static final DateTime WHEN = DateTime.parse("2024-11-1T05:04");
	public static final ArchiveTreatmentRunId RUN_ID = ArchiveTreatmentRunId.from("ee855151-f0a8-4182-a3e5-7469141526b4");
	public static final ObmDomain DOMAIN = ObmDomain.builder()
			.id(6)
			.uuid(ObmDomainUuid.of("67ecfad0-a684-47ed-aec5-f2c303f90467"))
			.name("test domain")
			.build();
	
	private final OnlyOnePerDomainScheduler scheduler;

	@Inject
	private RestoreTasksOnStartupHandler(OnlyOnePerDomainScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	public void starting() {
		restoreScheduledTasks();
	}
	
	private void restoreScheduledTasks() {
		scheduler.scheduleDomainArchiving(DOMAIN, WHEN, RUN_ID);
	}
}
