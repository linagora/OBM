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
package org.obm.push.tnefconverter.ScheduleMeeting;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.obm.push.utils.FileUtils;

import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIValue;
import net.freeutils.tnef.Message;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFUtils;

public class TNEFExtractorUtils {
	
	public static Boolean isScheduleMeetingRequest(Message message){
		PidTagMessageClass req = PidTagMessageClass.getPidTagMessageClass(TNEFExtractorUtils
				.getAttrString(message, Attr.attMessageClass));
//		return PidTagMessageClass.ScheduleMeetingRequest.equals(req) || PidTagMessageClass.ScheduleMeetingCanceled.equals(req);
		return req != null;
	}
	
	public static String getAttrString(Message message, int id) {
		try {
			Attr attr = message.getAttribute(id);
			if (attr != null) {
				return clear(toString(attr.getValue()), true);
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	public static Date getAttrDate(Message message, int id) {
		try {
			Attr attr = message.getAttribute(id);
			if (attr != null && attr.getValue() instanceof Date) {
					return (Date) attr.getValue();
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	public static int getMAPIPropInt(Message tnefMsg, int id) {
		try {
			MAPIProp prop = tnefMsg.getMAPIProps().getProp(id);
			if (prop != null) {
				MAPIValue[] vals = prop.getValues();
				if (vals != null) {
					for (int i = 0; i < vals.length; ) {
						String ret = toString(vals[i].getValue());
						if(ret != null){
							return Integer.parseInt(ret);
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return 0;
	}

	public static RawInputStream getMAPIPropInputStream(Message tnefMsg,
			int id) {
		try {
			MAPIProp prop = tnefMsg.getMAPIProps().getProp(id);
			if (prop != null) {
				MAPIValue[] vals = prop.getValues();
				if (vals != null) {
					for (int i = 0; i < vals.length; i++) {
						if (vals[i] != null){
								return vals[i].getRawData();
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return null;
	}

	public static Date getMAPIPropDate(Message tnefMsg, int id) {
		try {
			MAPIProp prop = tnefMsg.getMAPIProps().getProp(id);
			if (prop != null) {
				MAPIValue[] vals = prop.getValues();
				if (vals != null) {
					for (int i = 0; i < vals.length; i++) {
						if (vals[i] != null
								&& vals[i].getValue() instanceof Date) {
							return (Date) vals[i].getValue();
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return null;

	}

	public static Boolean getMAPIPropBoolean(Message tnefMsg, int id) {
		try {
			MAPIProp prop = tnefMsg.getMAPIProps().getProp(id);
			if (prop != null) {
				MAPIValue[] vals = prop.getValues();
				if (vals != null) {
					for (int i = 0; i < vals.length; i++) {
						if (vals[i] != null
								&& vals[i].getValue() instanceof Boolean) {
							return (Boolean) vals[i].getValue();
						}
					}
				}
			}
		} catch (IOException e) {
		}
		return false;
	}

	private static String clear(String v, boolean escape) {
		// escape the value
		if (escape)
			v = escape(v);

		if (v != null) {
			// escape line breaks within value
			v = TNEFUtils.replace(v, "\r\n", "\\n");
			v = TNEFUtils.replace(v, "\n", "\\n");
		}
		return v;
	}

	/**
	 * Returns a string representation of the given value.
	 * 
	 * @param val
	 *            the value to represent as a string
	 * @return a string representation of the given value
	 * @throws IOException
	 *             if an error occurs while processing value
	 */
	private static String toString(Object val) throws IOException {
		String v = null;
		if (val != null) {
			if (val instanceof Date) {
				// format the date according to given format string
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyyMMdd'T'HHmmss'Z'");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				return df.format((Date) val);
			} else if (val instanceof RawInputStream) {
				RawInputStream ris = (RawInputStream) val;
				byte[] b = FileUtils.streamBytes(ris, false);
				return new String(b,Charset.forName("US-ASCII"));
			} else {
				v = val.toString();
			}
		}
		return v;
	}

	private static String escape(String v) {
		if (v == null)
			return null;
		v = TNEFUtils.replace(v, "\r\n", "\\n");
		v = TNEFUtils.replace(v, "\n", "\\n");
		v = TNEFUtils.replace(v, ",", "\\,");
		v = TNEFUtils.replace(v, ";", "\\;");
		return v;
	}
}
