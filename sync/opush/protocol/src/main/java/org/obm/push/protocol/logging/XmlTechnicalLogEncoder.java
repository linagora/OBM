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
