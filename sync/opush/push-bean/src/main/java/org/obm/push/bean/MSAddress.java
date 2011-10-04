package org.obm.push.bean;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class MSAddress implements Serializable {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MSAddress.class);
	
	private String mail;
	private String displayName;

	public MSAddress(String mail) {
		this(null, mail);
	}

	public MSAddress(String displayName, String mail) {
		if (displayName != null) {
			this.displayName = displayName.replace("\"", "").replace("<", "")
					.replace(">", "");
		}
		if (mail != null && mail.contains("@")) {
			this.mail = mail.replace("\"", "").replace("<", "")
					.replace(">", "");
		} else {
			// FIXME ...
			if (logger.isDebugEnabled()) {
				logger
						.debug("mail: "
								+ mail
								+ " is not a valid email, building a john.doe@minig.org");
			}
			this.displayName = Strings.nullToEmpty(mail).replace("\"", "").replace("<", "").replace(
					">", "");
			this.mail = "john.doe@minig.org";
		}
	}

	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(mail, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAddress) {
			MSAddress that = (MSAddress) object;
			return Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("logger", logger)
			.add("mail", mail)
			.add("displayName", displayName)
			.toString();
	}
	
}
