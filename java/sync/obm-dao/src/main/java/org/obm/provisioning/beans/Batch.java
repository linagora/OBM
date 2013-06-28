/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning.beans;

import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomain;

public class Batch {

	public static class Id {

		public static Id valueOf(String idAsString) {
			return builder().id(Integer.parseInt(idAsString)).build();
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private Integer id;

			private Builder() {
			}

			public Builder id(Integer id) {
				this.id = id;
				return this;
			}

			public Id build() {
				return new Id(id);
			}
		}

		private final Integer id;

		public Integer getId() {
			return id;
		}

		private Id(Integer id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Id) {
				Id other = (Id) obj;

				return Objects.equal(id, other.id);
			}

			return false;
		}

		@Override
		public String toString() {
			return String.valueOf(id);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Id id;
		private BatchStatus status;
		private Date timecreate;
		private Date timecommit;
		private ObmDomain domain;
		private ImmutableList.Builder<Operation> operations;

		private Builder() {
			operations = ImmutableList.builder();
		}

		public Builder id(Id id) {
			this.id = id;
			return this;
		}

		public Builder status(BatchStatus status) {
			this.status = status;
			return this;
		}

		public Builder timecreate(Date timecreate) {
			this.timecreate = timecreate;
			return this;
		}

		public Builder timecommit(Date timecommit) {
			this.timecommit = timecommit;
			return this;
		}

		public Builder domain(ObmDomain domain) {
			this.domain = domain;
			return this;
		}

		public Builder operations(List<Operation> operations) {
			this.operations.addAll(operations);
			return this;
		}
		
		public Builder operation(Operation operation) {
			this.operations.add(operation);
			return this;
		}

		public Batch build() {
			Preconditions.checkState(status != null, "'status' should be set");
			Preconditions.checkState(domain != null, "'domain' should be set");

			return new Batch(id, status, timecreate, timecommit, domain, operations.build());
		}

	}

	private Id id;
	private BatchStatus status;
	private Date timecreate;
	private Date timecommit;
	private ObmDomain domain;
	private List<Operation> operations;

	private Batch(Id id, BatchStatus status, Date timecreate, Date timecommit, ObmDomain domain, List<Operation> operations) {
		this.id = id;
		this.status = status;
		this.timecreate = timecreate;
		this.timecommit = timecommit;
		this.domain = domain;
		this.operations = operations;
	}

	public Id getId() {
		return id;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public Date getTimecreate() {
		return timecreate;
	}

	public Date getTimecommit() {
		return timecommit;
	}

	public ObmDomain getDomain() {
		return domain;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, status, timecreate, timecommit, domain, operations);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Batch) {
			Batch other = (Batch) obj;

			return Objects.equal(id, other.id)
					&& Objects.equal(status, other.status)
					&& Objects.equal(timecreate, other.timecreate)
					&& Objects.equal(timecommit, other.timecommit)
					&& Objects.equal(domain, other.domain)
					&& Objects.equal(operations, other.operations);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("status", status)
				.add("timecreate", timecreate)
				.add("timecommit", timecommit)
				.add("domain", domain)
				.add("operations", operations)
				.toString();
	}

}
