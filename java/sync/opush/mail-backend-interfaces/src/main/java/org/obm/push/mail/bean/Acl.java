package org.obm.push.mail.bean;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class Acl {

	public enum Rights {
		Lookup("l"), Read("r"), PersistSeenStatus("s"), Write("w"), Insert("i"), Post("p"), Create("k"), DeleteMailbox("x"), DeleteMessage("t"), PerformExpunge("e"), Administer("a"),
		CreateRFC2086("c"), DeleteRFC2086("d");
		
		private String value;
		
		private Rights(String right) {
			this.value = right;
		}
		
		public String asSpecificationValue() {
			return value;
		}
		
		public static Rights fromSpecificationValue(String value) {
			for (Rights right: values()) {
				if (right.asSpecificationValue().equals(value)) {
					return right;
				}
			}
			throw new IllegalArgumentException();
		}
	}
	
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String user;
		private String rights;
		
		public Builder user(String user) {
			this.user = user;
			return this;
		}
		
		public Builder rights(String rights) {
			this.rights = rights;
			return this;
		}
		
		public Acl build() {
			Preconditions.checkState(user != null);
			Preconditions.checkState(rights != null);
			Set<Rights> rightsToBuild = Sets.newHashSet();
			char[] chars = rights.toCharArray();
			
			for (char charToCast: chars) {
				rightsToBuild.add(Rights.fromSpecificationValue(String.valueOf(charToCast)));
			}
			
			return new Acl(user, rightsToBuild);
		}
		
	}
	
	private String user;
	private Set<Rights> rights;
	
	private Acl(String user, Set<Rights> rights) {
		this.user = user;
		this.rights = rights;
	}
	
	public String getUser() {
		return user;
	}
	
	public Set<Rights> getRights() {
		return rights;
	}
	
	public String format() {
		StringBuilder builder = new StringBuilder();
		for(Rights right: rights) {
			builder.append(right.asSpecificationValue());
		}
		return builder.toString();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(user, rights);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Acl) {
			Acl that = (Acl) object;
			return Objects.equal(this.user, that.user)
				&& Objects.equal(this.rights, that.rights);
		}
		return false;
	}
}
