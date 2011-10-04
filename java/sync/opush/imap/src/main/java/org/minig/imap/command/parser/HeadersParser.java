package org.minig.imap.command.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.obm.push.utils.DOMUtils;

public class HeadersParser {

	private Map<String, String> parsedHeaders;
	private String currentLine;
	private StringBuilder currentValue;
	private String currentKey;
	
	public HeadersParser() {
		parsedHeaders = new HashMap<String, String>();
	}
	
	public Map<String, String> parseRawHeaders(Reader reader) throws IOException {
		BufferedReader br = new BufferedReader(reader);

		while ((currentLine = br.readLine()) != null) {
			
			// collapse rfc822 headers into one line
			if (currentLine.length() <= 1) {
				continue;
			}
			char first = currentLine.charAt(0);
			if (Character.isWhitespace(first)) {
				int nbSpaces = countLeadingWhiteSpaces();
				pushDataInValue(" ");
				pushDataInValue(currentLine.substring(nbSpaces));
			} else {
				saveCurrentAsParsed();
				prepareForNext();

				int split = currentLine.indexOf(':');
				if (split > 0) {
					currentKey = currentLine.substring(0, split).toLowerCase();
					String value = currentLine.substring(split + 1).trim();
					pushDataInValue(value);
				}

			}
		}
		saveCurrentAsParsed();
		return parsedHeaders;
	}

	private void prepareForNext() {
		currentKey = null;
		currentValue = new StringBuilder();
	}

	private void saveCurrentAsParsed() {
		if (currentKey != null) {
			parsedHeaders.put(currentKey, DOMUtils.stripNonValidXMLCharacters(currentValue.toString()));			
		}
	}

	private int countLeadingWhiteSpaces() {
		int nbSpaces = 1;
		while (nbSpaces<currentLine.length() && Character.isWhitespace(currentLine.charAt(nbSpaces))) {
			nbSpaces += 1;
		}
		return nbSpaces;
	}

	private void pushDataInValue(String part) {
		if (currentKey == null) {
			throw new IllegalStateException("value without a key");
		}
		if (currentValue == null) {
			currentValue = new StringBuilder();
		}
		currentValue.append(part);
	}
}
