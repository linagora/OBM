package org.obm.caldav.server.impl;

import javax.servlet.http.HttpServletRequest;

public class AuthHandler {

	public Token doAuth(HttpServletRequest req) {
		// TODO Auto-generated method stub
		Token t = new Token("thomas@zz.com", "aliacom");
		return t;
	}

}
