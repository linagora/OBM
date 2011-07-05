package org.minig.imap.mime.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.minig.imap.EncodedWord;
import org.minig.imap.mime.BodyParam;

import com.google.common.base.Function;


public class BodyParamParser {
	
	public static BodyParam parse(String key, String value) {
		return new BodyParamParser(key, value, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input;
			}
		}).parse();
	}
	
	private final String key;
	private final String value;
	private String decodedKey;
	private String decodedValue;
	private final Function<String, String> keyRewriter;
	
	public BodyParamParser(String key, String value, Function<String, String> keyRewriter) {
		this.key = key;
		this.value = value;
		this.keyRewriter = keyRewriter;
	}
	
	public BodyParam parse() {
		if (key.endsWith("*")) {
			decodedKey = key.substring(0, key.length() - 1);
			decodedValue = decodeAsterixEncodedValue();
		} else {
			decodedKey = key;
			decodedValue = decodeQuotedPrintable();
		}
		return new BodyParam(keyRewriter.apply(decodedKey), decodedValue);
	}
	
	
	private String decodeAsterixEncodedValue() {
		final int firstQuote = value.indexOf("'");
		final int secondQuote = value.indexOf("'", firstQuote + 1);
		final String charsetName = value.substring(0, firstQuote);
		final String text = value.substring(secondQuote + 1);
		try {
			Charset charset = Charset.forName(charsetName);
			return URLDecoder.decode(text, charset.displayName());
		} catch (UnsupportedEncodingException e) {
		} catch (IllegalCharsetNameException e) {
		} catch (IllegalArgumentException e) {
		}
		return text;
	}

	private String decodeQuotedPrintable() {
		return EncodedWord.decode(value).toString();
	}

}
