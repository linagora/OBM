package org.obm.push.impl;

import org.obm.push.backend.BackendSession;

public class HintsLoader {

	public void addHints(ActiveSyncRequest r, BackendSession bs) {
		String ua = r.getHeader("User-Agent");
		
		// NokiaE71
		if (ua != null && ua.contains("Nokia") && ua.contains("MailforExchange")) {
			bs.setHint("hint.multipleCalendars", false);
			bs.setHint("hint.loadAttendees", false);
			bs.setDevType(ua);
		}
	}

}
