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
package org.obm.push.tnefconverter;

public class MAPIExtendedProp {

	/**
	 * MAPI property ID constant.
	 */
	public static final int 
		PR_PID_LID_OWNER_CRITICAL_CHANGE				= 0x8200,// ID 0x1a
		PR_PID_LID_WHERE								= 0x8202,// ID 0x2;
		PR_PID_LID_GLOBAL_OBJECT_ID						= 0x8203,// ID 0x3;
		PR_PID_LID_APPOINTMENT_SUB_TYPE					= 0x8227,// ID 0x8215;
		PR_PID_LID_OLD_RECURRENCE_TYPE					= 0x8219,// ID 0x18;
		PR_PID_LID_CLIENT_INTENT						= 0x8224,// ID 0x15;
		PR_PID_LID_IS_EXCEPTION 						= 0x8211,// ID 0xa
		PR_PID_LID_START_RECURRENCE_DATE				= 0x8214,// ID 0xd
		PR_PID_LID_START_RECURRENCE_TIME				= 0x8215,// ID 0xe
		PR_PID_LID_TIMEZONE								= 0x8213,// ID 0xc
		PR_PID_LID_IS_RECURRING							= 0x8210,// ID 0x5
		PR_PID_LID_END_RECURRENCE_TIME					= 0x8217,// ID 0x10
		PR_PID_LID_WEEK_INTERVAL						= 0x8221,// ID 0x12
		PR_PID_LID_MONTH_INTERVAL						= 0x8222,// ID 0x13
		PR_PID_LID_YEAR_INTERVAL						= 0x8223,// ID 0x14
		PR_PID_LID_MONTH_OF_YEAR_MASK					= 0x8226,// ID 0x17
		PR_PID_LID_RENDERING_POSITION					= 0x8225,// ID 0x16
		PR_PID_LID_SINGLE_INVITE						= 0x8212;// ID 0x0b

}
