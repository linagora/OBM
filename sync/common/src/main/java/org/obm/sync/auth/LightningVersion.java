package org.obm.sync.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LightningVersion extends Version {

	public static class FactoryImpl implements Version.Factory<LightningVersion> {
		public LightningVersion create(int major, int minor, Integer release, Integer subRelease, String suffix) {
			return new LightningVersion(major, minor, release, subRelease, suffix);
		}
	}
	
	private final Integer linagoraVersion;
	private final Pattern obmVersionPattern;
	private final Pattern linagoraVersionPattern;

	public LightningVersion(int major, int minor, Integer release, Integer subRelease, String suffix) {
		super(major, minor, release, subRelease, suffix);
		obmVersionPattern = Pattern.compile(".*?(\\d+)obm");
		linagoraVersionPattern = Pattern.compile(".*-LINAGORA-(\\d{2})");		
		linagoraVersion = parseSuffix();
	}

	private Integer parseSuffix() {
		Integer version = parseLinagoraSuffix(getSuffix());
		if (version == null) {
			version = parseObmSuffix(getSuffix());
			if (version == null) {
				version = parseObmSuffix(lastVersionPartAndSuffix());
				if (version != null) {
					mergeLastVersionAndSuffix();
				}
			}
		}
		return version;
	}

	private void mergeLastVersionAndSuffix() {
		if (getSubRelease() != null) {
			setSuffix(getSubRelease() + getSuffix());
			setSubRelease(null);
			return;
		}
		if (getRelease() != null) {
			setSuffix(getRelease() + getSuffix());
			setRelease(null);
			return;
		}
	}

	private String lastVersionPartAndSuffix() {
		return firstNonNull(getSubRelease(), getRelease()) + getSuffix();
	}
	
	private String firstNonNull(Integer... integers) {
		for (Integer i: integers) {
			if (i != null) {
				return String.valueOf(i);
			}
		}
		return "";
	}
	
	private Integer parseLinagoraSuffix(String suffix) {
		return extractVersionWithPattern(suffix, linagoraVersionPattern);
	}

	private Integer parseObmSuffix(String suffix) {
		return extractVersionWithPattern(suffix, obmVersionPattern);
	}

	private Integer extractVersionWithPattern(String suffix, Pattern pattern) {
		Matcher matcher = pattern.matcher(suffix);
		return getIntegerFromMatcher(matcher);
	}

	private Integer getIntegerFromMatcher(Matcher matcher) {
		if (matcher.matches()) {
			return Integer.valueOf(matcher.group(1));
		}
		return null;
	}

	
	public Integer getLinagoraVersion() {
		return linagoraVersion;
	}
	
}
