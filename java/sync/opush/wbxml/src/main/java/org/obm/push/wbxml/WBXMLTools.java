package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.TransformerException;

import org.obm.push.utils.DOMUtils;
import org.obm.push.wbxml.parsers.WbxmlEncoder;
import org.obm.push.wbxml.parsers.WbxmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WBXMLTools {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public WBXMLTools() {
		super();
	}
	
	/**
	 * Transforms a wbxml byte array into the corresponding DOM representation
	 */
	public Document toXml(byte[] wbxml) throws IOException {

		WbxmlParser parser = new WbxmlParser();
		parser.setTagTable(0, TagsTables.CP_0); // AirSync
		parser.setTagTable(1, TagsTables.CP_1); // Contacts
		parser.setTagTable(2, TagsTables.CP_2); // Email
		parser.setTagTable(3, TagsTables.CP_3); // AirNotify
		parser.setTagTable(4, TagsTables.CP_4); // Calendar
		parser.setTagTable(5, TagsTables.CP_5); // Move
		parser.setTagTable(6, TagsTables.CP_6); // ItemEstimate
		parser.setTagTable(7, TagsTables.CP_7); // FolderHierarchy
		parser.setTagTable(8, TagsTables.CP_8); // MeetingResponse
		parser.setTagTable(9, TagsTables.CP_9); // Tasks
		parser.setTagTable(10, TagsTables.CP_10); // ResolveRecipients
		parser.setTagTable(11, TagsTables.CP_11); // ValidateCert
		parser.setTagTable(12, TagsTables.CP_12); // Contacts2
		parser.setTagTable(13, TagsTables.CP_13); // Ping
		parser.setTagTable(14, TagsTables.CP_14); // Provision
		parser.setTagTable(15, TagsTables.CP_15); // Search
		parser.setTagTable(16, TagsTables.CP_16); // GAL
		parser.setTagTable(17, TagsTables.CP_17); // AirSyncBase
		parser.setTagTable(18, TagsTables.CP_18); // Settings
		parser.setTagTable(19, TagsTables.CP_19); // DocumentLibrary
		parser.setTagTable(20, TagsTables.CP_20); // ItemOperations
		parser.switchPage(0);
		PushDocumentHandler pdh = new PushDocumentHandler();
		parser.setDocumentHandler(pdh);
		try {
			parser.parse(new ByteArrayInputStream(wbxml));
			return pdh.getDocument();
		} catch (SAXException e) {
			storeWbxml(wbxml);
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}

	}

	private void storeWbxml(byte[] wbxml) {
		FileOutputStream fout = null;
		try {
			File tmp = File.createTempFile("debug_", ".wbxml");
			fout = new FileOutputStream(tmp);
			fout.write(wbxml);
			logger.error("unparsable wbxml saved in " + tmp.getAbsolutePath());
		} catch (Throwable t) {
			logger.error("error storing debug file", t);
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					logger.error("error storing debug file", e);
				}
			}
		}
	}

	public byte[] toWbxml(String defaultNamespace, Document doc)
			throws WBXmlException, IOException {
		WbxmlEncoder encoder = new WbxmlEncoder(defaultNamespace);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			DOMUtils.serialise(doc, out);
			StringReader stringReader = new StringReader(new String(out.toByteArray(), "UTF-8"));
			InputSource is = new InputSource(stringReader);
			out = new ByteArrayOutputStream();
			encoder.convert(is, out);
			byte[] ret = out.toByteArray();

			return ret;
		} catch (SAXException e) {
			throw new WBXmlException("error during wbxml encoding", e);
		} catch (TransformerException e) {
			throw new WBXmlException("error during wbxml encoding", e);
		}
	}

}
