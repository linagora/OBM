package org.obm.sync.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LightningVersion extends Version {

	public static class FactoryImpl implements Version.Factory<LightningVersion> {
		public LightningVersion create(int major, int minor, Integer release, Integer subRelease, String suffix) {
			return new LightningVersion(major, minor, release, subRelease, suffix);
		}
	}
	
	private Integer linagoraVersion;

	public LightningVersion(int major, int minor, Integer release,
			Integer subRelease, String suffix) {
		super(major, minor, release, subRelease, suffix);
		parseSuffix(suffix);
	}

	private void parseSuffix(String suffix) {
		Pattern linagoraVersionPattern = Pattern.compile(".*-LINAGORA-(\\d{2})");
		Matcher matcher = linagoraVersionPattern.matcher(suffix);
		if (matcher.matches()) {
			linagoraVersion = Integer.valueOf(matcher.group(1));
		} else {
			linagoraVersion = null;
		}
	}

	public Integer getLinagoraVersion() {
		return linagoraVersion;
	}
	
}
