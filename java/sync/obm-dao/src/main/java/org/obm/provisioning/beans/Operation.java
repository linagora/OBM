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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Operation {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Integer id;
		private BatchStatus status;
		private BatchEntityType entityType;
		private Request request;
		private String error;
		private Date timecreate;
		private Date timecommit;

		private Builder() {
		}

		public Builder id(Integer id) {
			this.id = id;
			return this;
		}

		public Builder status(BatchStatus status) {
			this.status = status;
			return this;
		}

		public Builder entityType(BatchEntityType entityType) {
			this.entityType = entityType;
			return this;
		}

		public Builder request(Request request) {
			this.request = request;
			return this;
		}

		public Builder error(String error) {
			this.error = error;
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

		public Operation build() {
			Preconditions.checkState(status != null, "'status' should be set");
			Preconditions.checkState(request != null, "'request' should be set");
			Preconditions.checkState(entityType != null, "'entityType' should be set");

			return new Operation(id, status, entityType, request, error, timecreate, timecommit);
		}
	}

	private Integer id;
	private BatchStatus status;
	private BatchEntityType entityType;
	private Request request;
	private String error;
	private Date timecreate;
	private Date timecommit;

	private Operation(Integer id, BatchStatus status, BatchEntityType entityType, Request request, String error, Date timecreate, Date timecommit) {
		this.id = id;
		this.status = status;
		this.entityType = entityType;
		this.request = request;
		this.error = error;
		this.timecreate = timecreate;
		this.timecommit = timecommit;
	}

	public Integer getId() {
		return id;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public BatchEntityType getEntityType() {
		return entityType;
	}

	public Request getRequest() {
		return request;
	}

	public String getError() {
		return error;
	}

	public Date getTimecreate() {
		return timecreate;
	}

	public Date getTimecommit() {
		return timecommit;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, status, entityType, request, error, timecreate, timecommit);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Operation) {
			Operation other = (Operation) obj;

			return Objects.equal(id, other.id)
					&& Objects.equal(status, other.status)
					&& Objects.equal(entityType, other.entityType)
					&& Objects.equal(request, other.request)
					&& Objects.equal(error, other.error)
					&& Objects.equal(timecreate, other.timecreate)
					&& Objects.equal(timecommit, other.timecommit);
		}

		return false;
	}

}
