/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.message;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Formater to render a Date-object to a RFC2822 compatible date
 * 
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class MessageDate {
	
	private static final String[] dayOfWeek = {
		"Sun",
		"Mon",
		"Tue",
		"Wed",
		"Thu",
		"Fri",
		"Sat"
	};
	
	private static final String[] month = {
		"Jan",
		"Feb",
		"Mar",
		"Apr",
		"May",
		"Jun",
		"Jul",
		"Aug",
		"Sep",
		"Oct",
		"Nov",
		"Dec"
	};
	
	/**
	 * Render the Date to an RFC compliant String representation.
	 * 
	 * @param date
	 * @return the RFC compliant Date String.
	 */
	public static final String toString( Date date) {
		return toString( date, TimeZone.getDefault() );
	}
	
	/**
	 * Render the Date to an RFC compliant String representation.
	 * 
	 * @param date
	 * @param tz
	 * @return the RFC compliant Date String.
	 */
	public static final String toString( Date date, TimeZone tz ) {		
		Calendar calendar = Calendar.getInstance(tz);
		calendar.setTime( date );
		
		StringBuffer result = new StringBuffer(31);
		
		// day of week
		result.append( dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK)-1]);
		result.append(", ");
		
		// day of month
		result.append( calendar.get(Calendar.DAY_OF_MONTH));
		result.append(' ');
		
		// month
		result.append( month[calendar.get(Calendar.MONTH)]);
		result.append(' ');
		
		// year
		result.append( calendar.get(Calendar.YEAR));
		result.append(' ');
		
		// hour
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) result.append('0');
		result.append( hour);
		result.append(':');
		
		// minute
		int min = calendar.get(Calendar.MINUTE);
		if( min < 10 ) result.append( '0' );
		result.append( min );
		result.append(':');
		
		// second
		int sec = calendar.get(Calendar.SECOND);
		if( sec < 10 ) result.append( '0' );
		result.append(sec);
		result.append(' ');
		
		// timezone
		int rawOffset = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET));
		if( rawOffset < 0 ) {		
			int hours = (-rawOffset) / 3600000; 
			int minutes = ((-rawOffset) % 3600000) / 60000;
			
			result.append( "-");
			if( hours < 10 ) {
				result.append('0');			
			}			
			result.append( hours);
			
			if( minutes < 10 ) {
				result.append('0'); 
			}
			result.append(minutes);
		} else {
			int hours = rawOffset / 3600000; 
			int minutes = (rawOffset % 3600000) / 60000;
			
			result.append( "+" );
			if( hours < 10 ) {
				result.append('0');			
			}			
			result.append( hours);
			
			if( minutes < 10 ) {
				result.append('0'); 
			}
			result.append(minutes);
		}
		
		return result.toString();
	}

}
