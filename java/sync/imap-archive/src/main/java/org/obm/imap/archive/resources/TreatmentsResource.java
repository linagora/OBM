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

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingDates;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.imap.archive.exception.DomainConfigurationDisableException;
import org.obm.imap.archive.exception.DomainConfigurationNotFoundException;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.obm.imap.archive.services.SchedulingDatesService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TreatmentsResource {
	
	public static final Logger logger = LoggerFactory.getLogger(TreatmentsResource.class);
	
	@Inject
	private SchedulingDatesService schedulingDateService;
	@Inject
	private ArchiveSchedulingService archiveSchedulingService;
	@Inject
	private ObmDomain domain;
	@Inject
	private DateTimeProvider dateTimeProvider;
	@Inject
	private UriInfo uriInfo;

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
	public Response startArchiving(@QueryParam("archive_treatment_kind") ArchiveTreatmentKind archiveTreatmentKind) {
		try {
			ArchiveTreatmentKind actualTreatmentKind = Objects.firstNonNull(archiveTreatmentKind, ArchiveTreatmentKind.REAL_RUN);
			ArchiveTreatmentRunId runId = archiveSchedulingService.schedule(domain.getUuid(), dateTimeProvider.now(), actualTreatmentKind);
			return Response.seeOther(buildUri(runId)).build();
		} catch (DaoException e) {
			logger.error("Cannot schedule an archiving", e);
			return Response.serverError().build();
		} catch (DomainConfigurationNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (DomainConfigurationDisableException e) {
			return Response.status(Status.CONFLICT).build();
		}
	}
	
	private URI buildUri(ArchiveTreatmentRunId runId) {
		return UriBuilder.fromUri(uriInfo.getAbsolutePath())
			.path(runId.serialize())
			.build();
	}
	
	@Path(TreatmentFactory.PATH)
	public Class<TreatmentResource> treatment() {
		return TreatmentResource.class;
	}
}
