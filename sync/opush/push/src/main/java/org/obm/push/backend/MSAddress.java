package org.obm.push.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class MSAddress {
	
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
}
