/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
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
	public static final String JAR_FILE_PREFIX = "jar:file:";

	@Override
	protected void configure() {
		bind(Configuration.class).toInstance(new LdapConfiguration("cn=directory manager", "secret", 0));
	}

	@Provides
	@Singleton
	protected DirectoryServer provideDirectoryServer() throws Exception {
		File tmpFolder = createOpenDJTemporaryEnvironment();

		TimeThread.start();

		DirectoryServer directoryServer = DirectoryServer.getInstance();
		directoryServer.bootstrapServer();
		directoryServer.initializeConfiguration(ConfigFileHandler.class.getName(), new File(tmpFolder, "config/config.ldif").getAbsolutePath());
		DirectoryEnvironmentConfig envConfig = new DirectoryEnvironmentConfig();
		File locksDir = new File(tmpFolder, "locks");
		File logsDir = new File(tmpFolder, "logs");
		File schemaDir = new File(tmpFolder, "config/schema");

		locksDir.mkdirs();
		schemaDir.mkdirs();
		logsDir.mkdirs();
		envConfig.setLockDirectory(locksDir);
		envConfig.setSchemaDirectory(schemaDir);
		directoryServer.setEnvironmentConfig(envConfig);

		return directoryServer;
	}

	private File createOpenDJTemporaryEnvironment() throws IOException {
		File tmpFolder = Files.createTempDir();
		URL resource = ClassLoader.getSystemClassLoader().getResource(OPENDJ_FOLDER);

		if ("file".equals(resource.getProtocol())) {
			FileUtils.copyDirectory(FileUtils.toFile(resource), tmpFolder);
		} else if ("jar".equals(resource.getProtocol())) {
			String url = resource.toString();
			JarFile jar = new JarFile(StringUtils.substringBetween(url, JAR_FILE_PREFIX, "!"));

			copyDirectoryFromJar(jar, OPENDJ_FOLDER, tmpFolder);
		}

		mkFolder(tmpFolder, "locks");
		mkFolder(tmpFolder, "logs");

		return tmpFolder;
	}

	private void mkFolder(File tmpFolder, String folderName) {
		new File(tmpFolder, folderName).mkdir();
	}

	private void copyDirectoryFromJar(JarFile jar, String folder, File destDir) throws IOException {
		for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();

			if (entry.getName().startsWith(folder) && !entry.isDirectory()) {
				File destFile = new File(destDir, StringUtils.removeStart(entry.getName(), folder));
				FileOutputStream out = FileUtils.openOutputStream(destFile);

				IOUtils.copy(new AutoCloseInputStream(jar.getInputStream(entry)), out);
				IOUtils.closeQuietly(out);
			}
		}
	}
}
