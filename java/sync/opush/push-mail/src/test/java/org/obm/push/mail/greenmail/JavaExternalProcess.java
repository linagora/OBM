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
			addSimpleCliArgument(String.format("-Xmx%dm", config.heapMaxSizeInMo));
		}
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
		public boolean debug = false;
		public boolean useConfigHeapSize = false;
		public int heapMaxSizeInMo = 0;
		public int processTimeout = 0;
		public int processStartTimeNeeded = 0;
		
		public Config(boolean debug, boolean useConfigHeapSize, int heapMaxSizeInMo, 
				int processTimeout, int processStartTimeNeeded) {
			this.debug = debug;
			this.useConfigHeapSize = useConfigHeapSize;
			this.heapMaxSizeInMo = heapMaxSizeInMo;
			this.processTimeout = processTimeout;
			this.processStartTimeNeeded = processStartTimeNeeded;
		}
	}
}
