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
package org.obm.imap.archive.resources;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ChunkedOutput;
import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingDates;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.obm.imap.archive.service.SchedulingDatesService;
import org.obm.imap.archive.services.ArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TreatmentsResource {
	
	public static final Logger logger = LoggerFactory.getLogger(TreatmentsResource.class);
	
	@Inject
	private SchedulingDatesService schedulingDateService;
	@Inject
	private ArchiveService archiveService;
	@Inject
	private ArchiveSchedulingService archiveSchedulingService;
	@Inject
	private ObmDomain domain;
	@Inject
	private DateTimeProvider dateTimeProvider;

	@POST
	@Path("next")
	public Response calculateNextScheduledDate(DomainConfigurationDto domainConfigurationDto) {
		DomainConfiguration domainConfiguration = DomainConfiguration.from(domainConfigurationDto);
		if (!domainConfiguration.isEnabled()) {
			return Response.noContent().build();
		}
		
		DateTime nextTreatmentDate = schedulingDateService.nextTreatmentDate(domainConfiguration.getSchedulingConfiguration());
		return Response.ok(SchedulingDates.builder()
				.nextTreatmentDate(nextTreatmentDate)
				.build()).build();
	}
	
	@POST
	public Response startArchiving() {
		ArchiveTreatmentRunId runId = archiveSchedulingService.schedule(domain, dateTimeProvider.now());
		return Response.ok(runId).build();
	}
	
	@GET
	@Path("logs")
	@Produces(MediaType.TEXT_PLAIN)
	public ChunkedOutput<String> runningTreatment(@QueryParam("run_id") UUID runId) {
		return archiveService.runningProcessLogs(ArchiveTreatmentRunId.from(runId));
	}
}
