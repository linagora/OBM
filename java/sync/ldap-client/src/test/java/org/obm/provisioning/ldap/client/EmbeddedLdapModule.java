/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.provisioning.ldap.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.obm.provisioning.ldap.client.Configuration;
import org.opends.server.core.DirectoryServer;
import org.opends.server.extensions.ConfigFileHandler;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.util.TimeThread;

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
	protected DirectoryServer provideDirectoryServer() throws Exception {
		String tmpFolderPath = createOpenDJTemporaryEnvironment();
		String configPath = tmpFolderPath + "config/config.ldif";

		TimeThread.start();
		
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
		mkFolder(tmpFolderPath, "locks");
		mkFolder(tmpFolderPath, "logs");

		return tmpFolderPath;
	}

	private void mkFolder(String tmpFolderPath, String folderName) {
		new File(tmpFolderPath, folderName).mkdir();
	}
}
