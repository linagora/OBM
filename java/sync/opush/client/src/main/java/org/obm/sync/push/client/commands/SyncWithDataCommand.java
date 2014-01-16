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
package org.obm.sync.push.client.commands;

import java.io.IOException;

import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.protocol.data.SyncRequestFields;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.beans.AccountInfos;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class SyncWithDataCommand extends SyncWithCommand {

	@Singleton
	public static class Factory {

		private final EncoderFactory encoderFactory;
		private final SyncDecoder decoder;

		@Inject 
		private Factory(EncoderFactory encoderFactory, SyncDecoder decoder) {
			this.encoderFactory = encoderFactory;
			this.decoder = decoder;
		}
		
		public SyncWithDataCommand create(SyncKey syncKey, String collectionId, SyncCommand command,
				String serverId, String clientId, IApplicationData data, Device device) throws SAXException, IOException {
			return new SyncWithDataCommand(decoder, syncKey, collectionId, command, serverId, clientId, data, encoderFactory, device);
		}
	}
	
	private SyncWithDataCommand(SyncDecoder decoder, SyncKey syncKey, String collectionId, SyncCommand command,
			String serverId, String clientId, IApplicationData data, EncoderFactory encoders, Device device)
					throws SAXException, IOException {
		super(decoder, new SyncWithCommandDataTemplate(syncKey, collectionId, command, serverId, clientId, data, encoders, device));
	}
	
	public static class SyncWithCommandDataTemplate extends SyncWithCommandTemplate {

		private final IApplicationData data;
		private final EncoderFactory encoders;
		private final Device device;

		protected SyncWithCommandDataTemplate(SyncKey syncKey, String collectionId, SyncCommand command,
				String serverId, String clientId, IApplicationData data, EncoderFactory encoders, Device device)
				throws SAXException, IOException {
			super(syncKey, collectionId, command, serverId, clientId);
			this.data = data;
			this.encoders = encoders;
			this.device = device;
		}

		@Override
		protected void customize(Document document, AccountInfos accountInfos) {
			try {
				super.customize(document, accountInfos);
				Element commandsEl = DOMUtils.getUniqueElement(document.getDocumentElement(), SyncRequestFields.COMMANDS.getName());
				Element commandEl = DOMUtils.getUniqueElement(commandsEl, command.asSpecificationValue());
				Element applicationDataEl = DOMUtils.createElement(commandEl, SyncRequestFields.APPLICATION_DATA.getName());
				encoders.encode(device, applicationDataEl, data, false);
			} catch (IOException e) {
				Throwables.propagate(e);
			}
		}
	}
}