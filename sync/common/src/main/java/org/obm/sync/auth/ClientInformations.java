package org.obm.sync.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obm.sync.auth.Version.Factory;

public class ClientInformations {

	public static class Parser {
		
		private Pattern thunderbirdStringSplitter;
		private Pattern connectorVersionPattern;

		public static class ParserException extends Exception {
			public ParserException() {
				super();
			}
		}
		
		public Parser() {
			super();
			thunderbirdStringSplitter = Pattern.compile("thunderbird\\[ext: (.+), light: (.+)\\]");
			connectorVersionPattern = Pattern.compile("(\\d+)\\.(\\d+)?(\\.(\\d+))?(\\.(\\d+))?(.+)?");
		}

		public ClientInformations parse(String thunderbirdVersionAsString) {
			ClientInformations clientInformations = new ClientInformations();
			String[] parts = splitThunderbirdVersionString(thunderbirdVersionAsString);
			if (parts != null) {
				Version connectorVersion = parseConnectorVersion(parts[0]);
				LightningVersion lightningVersion = parseLightningVersion(parts[1]);
				clientInformations.setLightningVersion(lightningVersion);
				clientInformations.setObmConnectorVersion(connectorVersion);
				return clientInformations;
			}
			return null;
		}

		// thunderbird[ext: 2.4.1.8-rc11, light: 1.0b2]
		private String[] splitThunderbirdVersionString(CharSequence thunderbirdVersionAsString) {
			Matcher matcher = thunderbirdStringSplitter.matcher(thunderbirdVersionAsString);
			if (matcher.matches()) {
				return new String[] {matcher.group(1), matcher.group(2)}; 
			}
			return null;
		}
		
		private LightningVersion parseLightningVersion(String lightningVersion) {
			return parseVersion(lightningVersion, new LightningVersion.FactoryImpl());
		}

		private Version parseConnectorVersion(String connectorVersion) {
			return parseVersion(connectorVersion, new Version.FactoryImpl());
		}
		
		// 2.4.1.8-rc11
		private <T extends Version> T parseVersion(String connectorVersion, Factory<T> factory) {
			Matcher matcher = connectorVersionPattern.matcher(connectorVersion);
			if (matcher.matches()) {
				int major = Integer.valueOf(matcher.group(1));
				int minor = Integer.valueOf(matcher.group(2));
				Integer releaseAsInt = getIntegerFromString(matcher.group(4));
				Integer subReleaseAsInt = getIntegerFromString(matcher.group(6));
				String suffix = matcher.group(7);
				return factory.create(major, minor, releaseAsInt, subReleaseAsInt, suffix);
			}
			return null;
		}

		private Integer getIntegerFromString(String release) {
			return release != null ? Integer.valueOf(release) : null;
		}

	}

	private LightningVersion lightning;
	private Version obmConnector;
	
	public ClientInformations() {
		super();
	}
	
	public void setLightningVersion(LightningVersion lightning) {
		this.lightning = lightning;
	}
	
	public void setObmConnectorVersion(Version obmConnector) {
		this.obmConnector = obmConnector;
	}
	
	public LightningVersion getLightningVersion() {
		return lightning;
	}
	
	public Version getObmConnectorVersion() {
		return obmConnector;
	}
}
