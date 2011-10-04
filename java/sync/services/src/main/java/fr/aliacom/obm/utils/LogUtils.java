package fr.aliacom.obm.utils;

import org.obm.sync.auth.AccessToken;

public class LogUtils {

	public static String prefix(AccessToken token) {
		return "[CUID " + token.getConversationUid() + "] ";
	}
	
}
