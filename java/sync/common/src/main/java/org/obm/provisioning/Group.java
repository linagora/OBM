package org.obm.provisioning;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.user.ObmUser;

public class Group {
	
	
	protected GroupExtId extId;	
	protected String name;
	protected String description;
	protected Set<ObmUser> users;
	protected Set<Group> subgroups;
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		
		protected GroupExtId extId;
		protected String name;
		protected String description;
		protected Set<ObmUser> users;
		protected Set<Group> subgroups;

		protected Builder() {
			super();
		}
		
		public Builder extId(GroupExtId extId) {
			this.extId = extId;
			return this;
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder user(ObmUser user) {
			this.users.add(user);
			return this;
		}
		
		public Builder subgroup(Group group) {
			this.subgroups.add(group);
			return this;
		}
		
		public Group build() {
			Preconditions.checkNotNull(this.extId);
			Group group = new Group();
			group.extId = extId;
			group.name = name;
			group.description = description;
			group.users = users;
			group.subgroups = subgroups;
			return group;
		}
	}
	
	protected Group() {
		super();
	}
	
	public GroupExtId getExtId() {
		return extId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Set<ObmUser> getUsers() {
		return users;
	}
	
	public Set<Group> getSubgroups() {
		return subgroups;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(extId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Group) {
			Group other = (Group) obj;
			return Objects.equal(extId, other.extId);
		}		
		return false;
	}
}
