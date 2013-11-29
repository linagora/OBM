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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import net.freeutils.tnef.CompressedRTFInputStream;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class RTFUtils {

	private static final Logger logger = LoggerFactory
			.getLogger(RTFUtils.class);

	public static String getFolderId(String devId, String dataClass) {
		return devId + "\\" + dataClass;
	}

	public static String extractB64CompressedRTF(String b64) {
		String ret = "";
		try {
			byte[] bin = Base64.decodeBase64(b64);
			if (bin.length > 0) {
				ByteArrayInputStream in = new ByteArrayInputStream(bin);
				CompressedRTFInputStream cin = new CompressedRTFInputStream(in);

				String rtfDecompressed = FileUtils.streamString(cin, true);
				ret = extractRtfText(new ByteArrayInputStream(rtfDecompressed
						.getBytes(Charsets.UTF_8)));
			}
		} catch (Exception e) {
			logger.error("error extracting compressed rtf", e);
		}
		return ret;
	}

	public static String extractCompressedRTF(InputStream in) {
		String ret = "";
		try {
			CompressedRTFInputStream cin = new CompressedRTFInputStream(in);
			String rtfDecompressed = FileUtils.streamString(cin, true);
			ret = extractRtfText(new ByteArrayInputStream(rtfDecompressed
					.getBytes(Charsets.UTF_8)));
		} catch (Exception e) {
			logger.error("error extracting compressed rtf", e);
		}
		return ret;
	}

	private static String extractRtfText(InputStream stream)
			throws IOException, BadLocationException {
		RTFEditorKit kit = new RTFEditorKit();
		Document doc = kit.createDefaultDocument();
		kit.read(stream, doc, 0);

		return doc.getText(0, doc.getLength());
	}

}
