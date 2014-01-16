/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Convert text/plain mail body to HTML. The HTML version should be used for
 * display & rich formatting.
 */
public class HTMLBodyFormatter{

	private static String PATTERN_EMAIL = "[a-zA-Z_0-9\\-.]+@[a-zA-Z_0-9\\-.]+\\.[a-z]+";

	private static String PATTERN_URL = "(((http://)|(https://)|(w{3}.))"
			+ "+[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)"
			+ "+(/[#&;\\n\\-=?:|,()@\\+\\%/\\.\\w]+)?+(:[0-9]*+)?)";

	public HTMLBodyFormatter() {
		super();
	}

	public String convert(String plain) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		String escaped = StringEscapeUtils.escapeHtml(plain);
		escaped = escaped.replaceAll("\r\n", "<br>");
		sb.append(escaped);
		sb.append("</body></html>");
		
		Pattern p = Pattern.compile(PATTERN_URL + "|" + PATTERN_EMAIL);
		Matcher m = p.matcher(sb);
		StringBuffer res = parseUrl(m);
		if (res.toString().length() > 0) {
			return res.toString();
		} else {
			return sb.toString();
		}

	}

	public StringBuffer parseUrl(Matcher m) {
		StringBuffer result = new StringBuffer();

		Pattern pEmail = Pattern.compile(PATTERN_EMAIL);
		Pattern pUrl = Pattern.compile(PATTERN_URL);

		while (m.find()) {
			String href = m.group();
			Matcher mEmail = pEmail.matcher(href);
			Matcher mUrl = pUrl.matcher(href);

			if (mUrl.find()) {
				if (href.startsWith("http")) {
					m.appendReplacement(result, "<a href=\"" + href
							+ "\" target=\"_blank\">" + href + "</a>");
				} else {
					m.appendReplacement(result, "<a href=\"http://" + href
							+ "\" target=\"_blank\">" + href + "</a>");
				}
			} else if (mEmail.find()) {
				m.appendReplacement(result, "<a href=mailto:" + href + ">"
						+ href + "</a>");
			}
		}
		m.appendTail(result);
		return result;
	}

}
