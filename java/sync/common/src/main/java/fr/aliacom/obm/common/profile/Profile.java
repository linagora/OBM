/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package fr.aliacom.obm.common.profile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;

public class Profile {

	public static enum AdminRealm {
		DOMAIN, DELEGATION, USER
	}

	public static enum AccessRestriction {
		ALLOW_ALL, DENY_ALL
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private ProfileId id;
		private ObmDomain domain;
		private Date timecreate;
		private Date timeupdate;
		private ProfileName name;
		private Integer level;
		private Boolean managePeers;
		private AccessRestriction accessRestriction;
		private String accessExceptions;
		private List<AdminRealm> adminRealms;
		private Integer maxMailQuota;
		private Integer defaultMailQuota;
		private ImmutableMap.Builder<Module, ModuleCheckBoxStates> defaultCheckBoxStates;

		private Builder() {
			defaultCheckBoxStates = ImmutableMap.builder();
			adminRealms = Lists.newArrayList();
		}

		public Builder id(ProfileId id) {
			this.id = id;
			return this;
		}

		public Builder name(ProfileName name) {
			this.name = name;
			return this;
		}

		public Builder timecreate(Date timecreate) {
			this.timecreate = timecreate;
			return this;
		}

		public Builder timeupdate(Date timeupdate) {
			this.timeupdate = timeupdate;
			return this;
		}

		public Builder domain(ObmDomain domain) {
			this.domain = domain;
			return this;
		}

		public Builder level(Integer level) {
			this.level = level;
			return this;
		}

		public Builder managePeers(boolean managePeers) {
			this.managePeers = managePeers;
			return this;
		}

		public Builder accessRestriction(AccessRestriction accessRestriction) {
			this.accessRestriction = accessRestriction;
			return this;
		}

		public Builder accessExceptions(String accessExceptions) {
			this.accessExceptions = accessExceptions;
			return this;
		}

		public Builder adminRealms(AdminRealm... adminRealms) {
			for (AdminRealm adminRealm : adminRealms) {
				this.adminRealms.add(adminRealm);
			}
			return this;
		}

		public Builder defaultMailQuota(Integer defaultMailQuota) {
			this.defaultMailQuota = defaultMailQuota;
			return this;
		}

		public Builder maxMailQuota(Integer maxMailQuota) {
			this.maxMailQuota = maxMailQuota;
			return this;
		}

		public Builder defaultCheckBoxState(Module module, ModuleCheckBoxStates rights) {
			this.defaultCheckBoxStates.put(module, rights);
			return this;
		}

		public Profile build() {
			Preconditions.checkState(id != null);
			Preconditions.checkState(name != null);
			Preconditions.checkState(domain != null);
			Preconditions.checkState(level != null);

			managePeers = Objects.firstNonNull(managePeers, false);
			accessRestriction = Objects.firstNonNull(accessRestriction, AccessRestriction.ALLOW_ALL);
			maxMailQuota = Objects.firstNonNull(maxMailQuota, 0);
			defaultMailQuota = Objects.firstNonNull(defaultMailQuota, 0);
			AdminRealm adminRealmsArr[] = {};

			return new Profile(id, name, domain, timecreate, timeupdate, level, managePeers, accessRestriction, accessExceptions,
					adminRealms.toArray(adminRealmsArr), maxMailQuota, defaultMailQuota, defaultCheckBoxStates.build());
		}
	}

	private final ProfileId id;
	private final ObmDomain domain;
	private final Date timecreate;
	private final Date timeupdate;
	private final ProfileName name;
	private final int level;
	private final boolean managePeers;
	private final AccessRestriction accessRestriction;
	private final String accessExceptions;
	private final AdminRealm adminRealms[];
	private final int maxMailQuota;
	private final int defaultMailQuota;
	private final Map<Module, ModuleCheckBoxStates> defaultCheckBoxStates;

	private Profile(ProfileId id, ProfileName name, ObmDomain domain, Date timecreate, Date timeupdate, int level, boolean managePeers, AccessRestriction accessRestriction, String accessExceptions,
			AdminRealm adminRealms[], int maxMailQuota, int defaultMailQuota, Map<Module, ModuleCheckBoxStates> defaultCheckBoxStates) {
		this.id = id;
		this.domain = domain;
		this.timecreate = timecreate;
		this.timeupdate = timeupdate;
		this.name = name;
		this.level = level;
		this.managePeers = managePeers;
		this.accessRestriction = accessRestriction;
		this.accessExceptions = accessExceptions;
		this.adminRealms = adminRealms;
		this.maxMailQuota = maxMailQuota;
		this.defaultMailQuota = defaultMailQuota;
		this.defaultCheckBoxStates = defaultCheckBoxStates;
	}

	public ProfileId getId() {
		return id;
	}

	public ObmDomain getDomain() {
		return domain;
	}

	public Date getTimecreate() {
		return timecreate;
	}

	public Date getTimeupdate() {
		return timeupdate;
	}

	public ProfileName getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

	public boolean isManagePeers() {
		return managePeers;
	}

	public AccessRestriction getAccessRestriction() {
		return accessRestriction;
	}

	public String getAccessExceptions() {
		return accessExceptions;
	}

	public AdminRealm[] getAdminRealms() {
		return adminRealms;
	}

	public int getMaxMailQuota() {
		return maxMailQuota;
	}

	public int getDefaultMailQuota() {
		return defaultMailQuota;
	}

	public Map<Module, ModuleCheckBoxStates> getDefaultCheckBoxStates() {
		return defaultCheckBoxStates;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, domain, level, managePeers, accessRestriction, accessExceptions,
				Arrays.hashCode(adminRealms), maxMailQuota, defaultMailQuota, defaultCheckBoxStates);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Profile) {
			Profile other = (Profile) obj;

			return Objects.equal(id, other.id)
					&& Objects.equal(name, other.name)
					&& Objects.equal(domain, other.domain)
					&& Objects.equal(level, other.level)
					&& Objects.equal(managePeers, other.managePeers)
					&& Objects.equal(accessRestriction, other.accessRestriction)
					&& Objects.equal(accessExceptions, other.accessExceptions)
					&& Arrays.equals(adminRealms, other.adminRealms)
					&& Objects.equal(maxMailQuota, other.maxMailQuota)
					&& Objects.equal(defaultMailQuota, other.defaultMailQuota)
					&& Objects.equal(defaultCheckBoxStates, other.defaultCheckBoxStates);
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("domain", domain)
				.add("level", level)
				.add("managePeers", managePeers)
				.add("accessRestriction", accessRestriction)
				.add("accessExceptions", accessExceptions)
				.add("adminRealms", adminRealms)
				.add("defaultMailQuota", defaultMailQuota)
				.add("maxMailQuota", maxMailQuota)
				.add("timecreate", timecreate)
				.add("timeupdate", timeupdate)
				.toString();
	}

}
