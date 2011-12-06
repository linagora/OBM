package org.obm.push.bean;

import java.io.Serializable;
import com.google.common.base.Objects;

public class Credentials implements Serializable {

	private final User user;
	private final String password;

	public Credentials(User user, String password) {
		super();
		this.user = user;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public User getUser() {
		return user;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(user, password);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Credentials) {
			Credentials that = (Credentials) object;
			return Objects.equal(this.user, that.user)
				&& Objects.equal(this.password, that.password);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("user", user)
			.add("password", password)
			.toString();
	}
	
}