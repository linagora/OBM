package org.obm.push.mail.greenmail;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;


public class JavaExternalProcess extends ExternalProcess {
	
	private static final String JAVA_PATH = 
			System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
	
	public JavaExternalProcess(Class<?> mainClass, Config config) {
		super(JAVA_PATH, config.processTimeout, config.processStartTimeNeeded);
		setHeapMaxSize(config);
		setDebugMode(config);
		setClasspath();
		setMainClass(mainClass);
	}
	
	public void setCommandLineArgs(Map<String, String> cliArgs) {
		for (Entry<String, String> arg : cliArgs.entrySet()) {
			addTaggedCliArgument(arg.getKey(), arg.getValue());
		}
	}

	private void setHeapMaxSize(Config config){
		if (config.useConfigHeapSize) {
			addSimpleCliArgument(String.format("-Xmx%dm", bytesToMegaBytes(config.heapMaxSizeInByte)));
		}
	}
	
	private int bytesToMegaBytes(long bytes) {
		return Long.valueOf(bytes >> 20).intValue();
	}
	
	private void setDebugMode(Config config) {
		if (config.debug) {
			addSimpleCliArgument("-Xdebug");
			addSimpleCliArgument("-Xrunjdwp:transport=dt_socket,address=8008,server=y,suspend=n");
		}
	}
	
	private void setMainClass(Class<?> mainClass) {
		addSimpleCliArgument(mainClass.getName());
	}

	private void setClasspath() {
		addEnvironmentVariable("CLASSPATH", System.getProperty("java.class.path"));
	}
	
	public static class Config {
		public final boolean debug;
		public final boolean useConfigHeapSize;
		public final long heapMaxSizeInByte;
		public final int processTimeout;
		public final int processStartTimeNeeded;
		
		public Config(boolean debug, boolean useConfigHeapSize, long heapMaxSizeInByte, 
				int processTimeout, int processStartTimeNeeded) {
			this.debug = debug;
			this.useConfigHeapSize = useConfigHeapSize;
			this.heapMaxSizeInByte = heapMaxSizeInByte;
			this.processTimeout = processTimeout;
			this.processStartTimeNeeded = processStartTimeNeeded;
		}
	}
}
