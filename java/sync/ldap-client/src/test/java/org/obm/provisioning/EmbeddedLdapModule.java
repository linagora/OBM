package org.obm.provisioning;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opends.server.core.DirectoryServer;
import org.opends.server.extensions.ConfigFileHandler;
import org.opends.server.types.DirectoryEnvironmentConfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class EmbeddedLdapModule extends AbstractModule {
	
	public static final String OPENDJ_FOLDER = "opendj/";
	
	@Override
	protected void configure() {
		bind(Configuration.class).to(StaticConfiguration.class);
	}
	
	@Provides @Singleton
	@VisibleForTesting DirectoryServer provideDirectoryServer() throws Exception {
		String tmpFolderPath = createOpenDJTemporaryEnvironment();
		String configPath = tmpFolderPath + "config/config.ldif";

		DirectoryServer directoryServer = DirectoryServer.getInstance();
		directoryServer.bootstrapServer();
		directoryServer.initializeConfiguration(ConfigFileHandler.class.getName(), configPath);
		DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
		File locksDir = new File(tmpFolderPath, "locks");
		File logsDir = new File(tmpFolderPath, "logs");
		File schemaDir = new File(tmpFolderPath, "config/schema");

		locksDir.mkdirs();
		schemaDir.mkdirs();
		logsDir.mkdirs();
		envConfig.setLockDirectory(locksDir);
		envConfig.setSchemaDirectory(schemaDir);
		directoryServer.setEnvironmentConfig(envConfig);
		return directoryServer;
	}
	
	private File mainTemporaryFolder() {
        return Files.createTempDir();
	}

	private String createOpenDJTemporaryEnvironment() throws IOException {
		File tmpFolder = mainTemporaryFolder();
		String tmpFolderPath = tmpFolder.getAbsolutePath() + "/";
		
		File resourcesFolder = new File(
				ClassLoader.getSystemClassLoader().getResource(OPENDJ_FOLDER).getPath());
		FileUtils.copyDirectory(resourcesFolder, tmpFolder);

		return tmpFolderPath;
	}
}
