/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.protocol.bean;

import org.apache.commons.lang.NotImplementedException;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;

public class SyncRequestCollection {

	public static class Builder {

		public SyncRequestCollection build() {
			return new SyncRequestCollection();
		}

	}
	
	protected SyncRequestCollection() {}
	
	public int getId() {
		throw new NotImplementedException("Will be implemented in next commits");
	}

	public String getDataClass() {
		throw new NotImplementedException("Will be implemented in next commits");
	}

	public SyncKey getSyncKey() {
		throw new NotImplementedException("Will be implemented in next commits");
	}

	public Integer getWindowSize() {
		throw new NotImplementedException("Will be implemented in next commits");
	}
	
	public boolean hasWindowSize() {
		throw new NotImplementedException("Will be implemented in next commits");
	}

	public SyncCollectionOptions getOptions() {
		throw new NotImplementedException("Will be implemented in next commits");
	}
	
	public SyncRequestCollectionCommands getCommands() {
		throw new NotImplementedException("Will be implemented in next commits");
	}
}
