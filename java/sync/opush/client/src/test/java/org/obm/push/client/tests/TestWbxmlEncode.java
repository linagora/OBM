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
package org.obm.push.client.tests;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.w3c.dom.Document;

public class TestWbxmlEncode extends AbstractPushTest {

	@Test
	public void testEncode() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		byte[] data = wbxmlTools.toWbxml("FolderHierarchy", doc);
		assertNotNull(data);
	}

	@Test
	public void testDecode() throws Exception {
		InputStream in = loadDataFile("foldersync_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}
	
	@Test
	public void testDecodeOmniaProB7330() throws Exception {
		InputStream in = loadDataFile("OmniaProB7330.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}
	
	@Test
	public void testDecodeSettingsOmniaPro() throws Exception {
		InputStream in = loadDataFile("settings_omnia_pro.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testSettingsDecode() throws Exception {
		InputStream in = loadDataFile("settings.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testDecodeSync() throws Exception {
		InputStream in = loadDataFile("contact_sync_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testDecodeIPhoneSync1() throws Exception {
		InputStream in = loadDataFile("iphone_sync1.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testDecodeIPhoneSync2() throws Exception {
		InputStream in = loadDataFile("iphone_sync2.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testDecodeIPhoneSync3() throws Exception {
		InputStream in = loadDataFile("iphone_sync3.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testDecodeEmailSync() throws Exception {
		InputStream in = loadDataFile("sync_request_wm61.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}

	@Test
	public void testExchangeFolderSync() throws Exception {
		InputStream in = loadDataFile("exchange_foldersync.wbxml");
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}
}
