package org.obm.provisioning;

import java.util.Date;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.user.ObmUser;

public class Group {

	public static class Id {

		public static Id valueOf(String idAsString) {
			return builder().id(Integer.parseInt(idAsString)).build();
		}

		public static Id valueOf(int uid) {
			return builder().id(uid).build();
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

        private Id uid;
        private Integer gid;
        private String email;
        private Date timecreate;
        private Date timeupdate;
        private Boolean privateGroup;
        private Boolean archive;
        private GroupExtId extId;
        private String name;
        private String description;
        private ImmutableSet.Builder<ObmUser> users;
        private ImmutableSet.Builder<Group> subgroups;

        private Builder() {
            this.users = ImmutableSet.builder();
            this.subgroups = ImmutableSet.builder();
        }

        public Builder from(Group group) {
        	return uid(group.uid)
        			.gid(group.gid)
        			.email(group.email)
        			.timecreate(group.timecreate)
        			.timeupdate(group.timeupdate)
        			.privateGroup(group.privateGroup)
        			.archive(group.archive)
        			.extId(group.extId)
        			.name(group.name)
        			.description(group.description)
        			.users(group.users)
        			.subgroups(group.subgroups);
        }
        
        public Builder uid(Id uid) {
            this.uid = uid;
            return this;
        }

        public Builder gid(Integer gid) {
            this.gid = gid;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder privateGroup(boolean privateGroup) {
            this.privateGroup = privateGroup;
            return this;
        }

        public Builder archive(boolean archive) {
            this.archive = archive;
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
        
		public Builder users(Iterable<ObmUser> users) {
			this.users.addAll(users);
			return this;
		}

        public Builder subgroup(Group group) {
            this.subgroups.add(group);
            return this;
        }
        
		public Builder subgroups(Iterable<Group> groups) {
			this.subgroups.addAll(groups);
			return this;
		}

        public Group build() {
            Preconditions.checkState(uid != null || extId != null);

            privateGroup = Objects.firstNonNull(privateGroup, false);
            archive = Objects.firstNonNull(archive, false);

            return new Group(uid, gid, extId, name, email, description, users.build(), subgroups.build(),
            		privateGroup, archive, timecreate, timeupdate);
        }
    }

    private final Id uid;
    private final Integer gid;
    private final String email;
    private final Date timecreate;
    private final Date timeupdate;
    private final boolean privateGroup;
    private final boolean archive;
    private final GroupExtId extId;
    private final String name;
    private final String description;
    private final Set<ObmUser> users;
    private final Set<Group> subgroups;

    private Group(Id uid, Integer gid, GroupExtId extId, String name, String email, String description, Set<ObmUser> users, Set<Group> subgroups,
    		boolean privateGroup, boolean archive, Date timecreate, Date timeupdate) {
		this.uid = uid;
		this.extId = extId;
		this.name = name;
		this.description = description;
		this.users = users;
		this.subgroups = subgroups;
		this.gid = gid;
		this.email = email;
		this.timecreate = timecreate;
		this.timeupdate = timeupdate;
		this.privateGroup = privateGroup;
		this.archive = archive;
    }

    public Id getUid() {
		return uid;
	}

	public boolean isPrivateGroup() {
		return privateGroup;
	}

	public boolean isArchive() {
		return archive;
	}

	public Integer getGid() {
		return gid;
	}

	public String getEmail() {
		return email;
	}

	public Date getTimecreate() {
		return timecreate;
	}

	public Date getTimeupdate() {
		return timeupdate;
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
        return Objects.hashCode(uid, extId, name, description, users, subgroups, gid, email, privateGroup, archive);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            Group other = (Group) obj;

			return Objects.equal(uid, other.uid)
					&& Objects.equal(extId, other.extId)
					&& Objects.equal(name, other.name)
					&& Objects.equal(description, other.description)
					&& Objects.equal(users, other.users)
					&& Objects.equal(subgroups, other.subgroups)
					&& Objects.equal(gid, other.gid)
					&& Objects.equal(email, other.email)
					&& Objects.equal(privateGroup, other.privateGroup)
					&& Objects.equal(archive, other.archive);
        }

        return false;
    }

    @Override
    public String toString() {
		return Objects
				.toStringHelper(this)
				.add("uid", uid)
				.add("gid", gid)
				.add("extId", extId)
				.add("name", name)
				.add("email", email)
				.add("privateGroup", privateGroup)
				.add("archive", archive)
				.add("description", description)
				.add("timecreate", timecreate)
				.add("timeupdate", timeupdate)
				.add("users", users)
				.add("subgroups", subgroups)
				.toString();
    }

}
