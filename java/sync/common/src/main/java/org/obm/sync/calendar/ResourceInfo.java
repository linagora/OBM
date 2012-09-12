package org.obm.sync.calendar;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class ResourceInfo {
	private int id;
	private String name;
	private String mail;
	private String description;
	private boolean read;
	private boolean write;

	public static class Builder {
		private int id;
		private String name;
		private String mail;
		private String description;
		private Boolean read;
		private Boolean write;

		private Builder() {
			id = -1;
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder mail(String mail) {
			this.mail = mail;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder read(boolean read) {
			this.read = read;
			return this;
		}

		public Builder write(boolean write) {
			this.write = write;
			return this;
		}

		public ResourceInfo build() {
			Preconditions.checkState(id > -1);
			Preconditions.checkState(!Strings.isNullOrEmpty(name));
			Preconditions.checkState(!Strings.isNullOrEmpty(mail));
			Preconditions.checkState(read != null);
			Preconditions.checkState(write != null);
			return new ResourceInfo(id, name, mail, description, read, write);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private ResourceInfo(int id, String name, String mail, String description, boolean read, boolean write) {
		this.id = id;
		this.name = name;
		this.mail = mail;
		this.description = description;
		this.read = read;
		this.write = write;
	}

	public int getId() {
		return id;
	}

	public String getMail() {
		return mail;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isWrite() {
		return write;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ResourceInfo)) {
			return false;
		}
		return id == ((ResourceInfo) o).id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}