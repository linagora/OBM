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
package org.obm.push;

import org.obm.push.mail.MailboxService;
import org.obm.push.mail.MessageInputStreamProvider;
import org.obm.push.mail.MessageInputStreamProviderImpl;
import org.obm.push.mail.imap.LinagoraMailboxService;
import org.obm.push.mail.imap.MinigStoreClient;
import org.obm.push.mail.imap.MinigStoreClientImpl;
import org.obm.push.mail.imap.idle.IdleClient;
import org.obm.push.minig.imap.IdleClientImpl;
import org.obm.push.resource.LinagoraMailResourcesService;
import org.obm.push.resource.ResourcesService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class LinagoraImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapClientModule());
		bind(MinigStoreClient.Factory.class).to(MinigStoreClientImpl.Factory.class);
		bind(MailboxService.class).to(LinagoraMailboxService.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
		bind(IdleClient.Factory.class).to(IdleClientImpl.Factory.class);
		
		Multibinder<ResourcesService> resources = Multibinder.newSetBinder(binder(), ResourcesService.class);
		resources.addBinding().to(LinagoraMailResourcesService.class);
	}
}
