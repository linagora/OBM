package org.obm.provisioning;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.user.ObmUser;

public class Group {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private GroupExtId extId;
        private String name;
        private String description;
        private ImmutableSet.Builder<ObmUser> users;
        private ImmutableSet.Builder<Group> subgroups;

        private Builder() {
            this.users = ImmutableSet.builder();
            this.subgroups = ImmutableSet.builder();
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

            return new Group(extId, name, description, users.build(), subgroups.build());
        }
    }

    private final GroupExtId extId;
    private final String name;
    private final String description;
    private final Set<ObmUser> users;
    private final Set<Group> subgroups;

    private Group(GroupExtId extId, String name, String description, Set<ObmUser> users, Set<Group> subgroups) {
		this.extId = extId;
		this.name = name;
		this.description = description;
		this.users = users;
		this.subgroups = subgroups;
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
        return Objects.hashCode(extId, name, description, users, subgroups);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            Group other = (Group) obj;
            return Objects.equal(extId, other.extId) &&
                   Objects.equal(name, other.name) &&
                   Objects.equal(description, other.description) &&
                   Objects.equal(users, other.users) &&
                   Objects.equal(subgroups, other.subgroups);
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("extId", extId)
                      .add("name", name)
                      .add("description", description)
                      .add("users", users)
                      .add("subgroups", subgroups)
                      .toString();
    }

}
