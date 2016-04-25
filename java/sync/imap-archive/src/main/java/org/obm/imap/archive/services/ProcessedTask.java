/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.services;

import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.HigherBoundary;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ProcessedTask {

	public static ProcessedTask.Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ArchiveConfiguration archiveConfiguration;
		private HigherBoundary higherBoundary;
		private Optional<ArchiveTreatment> previousArchiveTreatment;
		private boolean continuePrevious;
		
		private Builder() {}

		public ProcessedTask.Builder archiveConfiguration(ArchiveConfiguration archiveConfiguration) {
			this.archiveConfiguration = archiveConfiguration;
			return this;
		}
		
		public ProcessedTask.Builder higherBoundary(HigherBoundary higherBoundary) {
			this.higherBoundary = higherBoundary;
			return this;
		}
		
		public ProcessedTask.Builder previousArchiveTreatment(Optional<ArchiveTreatment> previousArchiveTreatment) {
			this.previousArchiveTreatment = previousArchiveTreatment;
			return this;
		}
		
		public ProcessedTask.Builder continuePrevious(boolean continuePrevious) {
			this.continuePrevious = continuePrevious;
			return this;
		}
		
		public ProcessedTask build() {
			Preconditions.checkState(archiveConfiguration != null);
			Preconditions.checkState(higherBoundary != null);
			Preconditions.checkState(previousArchiveTreatment != null);
			return new ProcessedTask(archiveConfiguration.getLogger(), archiveConfiguration.getRunId(),
				archiveConfiguration.getDomain(), higherBoundary, 
				archiveConfiguration.getConfiguration(), previousArchiveTreatment, continuePrevious);
		}
	}
	
	private final Logger logger;
	private final ArchiveTreatmentRunId runId;
	private final ObmDomain domain;
	private final HigherBoundary higherBoundary;
	final DomainConfiguration domainConfiguration;
	private final Optional<ArchiveTreatment> previousArchiveTreatment;
	private final boolean continuePrevious;
	
	private ProcessedTask(Logger logger, ArchiveTreatmentRunId runId, ObmDomain domain, 
			HigherBoundary higherBoundary, DomainConfiguration domainConfiguration, 
			Optional<ArchiveTreatment> previousArchiveTreatment, boolean continuePrevious) {
		
		this.logger = logger;
		this.runId = runId;
		this.domain = domain;
		this.higherBoundary = higherBoundary;
		this.domainConfiguration = domainConfiguration;
		this.previousArchiveTreatment = previousArchiveTreatment;
		this.continuePrevious = continuePrevious;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}
	
	public ObmDomain getDomain() {
		return domain;
	}

	public HigherBoundary getHigherBoundary() {
		return higherBoundary;
	}

	public DomainConfiguration getDomainConfiguration() {
		return domainConfiguration;
	}

	public Optional<ArchiveTreatment> getPreviousArchiveTreatment() {
		return previousArchiveTreatment;
	}
	
	public boolean continuePrevious() {
		return continuePrevious;
	}
}