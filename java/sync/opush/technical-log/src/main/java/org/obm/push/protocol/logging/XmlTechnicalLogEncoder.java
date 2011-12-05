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
package org.obm.push.protocol.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.EncoderBase;

public class XmlTechnicalLogEncoder<E> extends EncoderBase<E> {

	protected Layout<E> layout;
	private String buffer;

	public Layout<E> getLayout() {
		return layout;
	}

	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}

	public void init(OutputStream os) throws IOException {
		buffer = "";
		super.init(os);
		writeHeader();
	}

	public void writeHeader() {
		if (layout != null && outputStream != null) {
			buffer += layout.getFileHeader();
	    }
	}

	public void writeFooter() throws IOException{
		if (layout != null && outputStream != null) {
			buffer += layout.getFileFooter();

		    flushBuffer();
		}
	}

	private void flushBuffer() throws IOException{
		String formattedBuffer = formatXmlInput(buffer);
	    outputStream.write(formattedBuffer.getBytes());
	    outputStream.flush();
		buffer = "";
	}

	@Override
	public void doEncode(E event) throws IOException {
		buffer += layout.doLayout(event);
	}

	@Override
	public void close() throws IOException {
		writeFooter();
	}

	private String formatXmlInput(String xmlInput) {
		try {

			Source sourceXmlInput = new StreamSource(new StringReader(xmlInput));
			StreamResult xmlOutput = new StreamResult(new StringWriter());

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(sourceXmlInput, xmlOutput);

			return xmlOutput.getWriter().toString();

		} catch (Exception e) {
			return xmlInput;
		}
	}
}
