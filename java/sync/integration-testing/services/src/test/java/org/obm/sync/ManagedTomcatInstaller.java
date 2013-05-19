/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;

public final class ManagedTomcatInstaller {

	private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	private final static String TOMCAT_VERSION = "6.0.20";
	private final static File TOMCAT_ARCHIVE = new File("../../../tomcat/obm-tomcat-" + TOMCAT_VERSION + ".tar.bz2");
	private final static String TOMCAT_USERS_RESOURCE = "tomcat-users.xml";
	private final static String TOMCAT_MANAGER_RESOURCE = "tomcat-manager.tar.bz2";
	private final static File TOMCAT_TEMPORARY_MAIN_FOLDER = new File(TMP_DIR, "tomcat");
	private final static File TOMCAT_TEMPORARY_VERSION_FOLDER = new File(TOMCAT_TEMPORARY_MAIN_FOLDER, "apache-tomcat-" + TOMCAT_VERSION);
	
	public static void install() {
		if (!TOMCAT_TEMPORARY_MAIN_FOLDER.exists()) {
			try {
				TOMCAT_TEMPORARY_MAIN_FOLDER.mkdir();
				
				uncompressTomcat();
				copyTomcatUsers();
				copyTomcatManager();
				replaceFilesByFolders();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
	}
	
	private static void replaceFilesByFolders() {
		replaceFileByFolder("work");
		replaceFileByFolder("temp");
		replaceFileByFolder("logs");
	}
	
	private static void replaceFileByFolder(String fileName) {
		File file = new File(TOMCAT_TEMPORARY_VERSION_FOLDER, fileName);
		if (!file.exists()) {
			file.mkdir();
			return;
		}
		if (!file.isDirectory()) {
			file.delete();
			file.mkdir();
		}
	}

	private static void copyTomcatUsers() throws IOException {
		URL resource = Resources.getResource(TOMCAT_USERS_RESOURCE);
		File tomcatUsers = new File(TOMCAT_TEMPORARY_VERSION_FOLDER, "conf/tomcat-users.xml");
		if (tomcatUsers.exists()) {
			tomcatUsers.delete();
		}
		FileOutputStream outputStream = new FileOutputStream(tomcatUsers);
		IOUtils.copy(resource.openStream(), outputStream);
	}

	private static void copyTomcatManager() throws IOException, ArchiveException {
		URL resource = Resources.getResource(TOMCAT_MANAGER_RESOURCE);
		File tarFile = uncompressBZ2File(new FileInputStream(new File(resource.getFile())));

		File tomcatWebapps = new File(TOMCAT_TEMPORARY_VERSION_FOLDER, "webapps/");
		uncompressTarFile(tomcatWebapps, tarFile);
	}

	private static void uncompressTomcat() throws IOException, ArchiveException {
		FileInputStream input = new FileInputStream(TOMCAT_ARCHIVE);
		File tarFile = uncompressBZ2File(input);

		uncompressTarFile(TOMCAT_TEMPORARY_MAIN_FOLDER, tarFile);
	}
	
	private static File uncompressBZ2File(FileInputStream inputStream) throws IOException {
		File tarFile = File.createTempFile("uncompress", "bz2");
		FileOutputStream outputStream = new FileOutputStream(tarFile);
		BZip2CompressorInputStream bzInputStream = new BZip2CompressorInputStream(inputStream);
		IOUtils.copy(bzInputStream, outputStream);
		
		outputStream.close();
		bzInputStream.close();
		return tarFile;
	}
	
	private static void uncompressTarFile(File parent, File inputFile) throws FileNotFoundException, IOException, ArchiveException {
		FileInputStream inputStream = new FileInputStream(inputFile); 
		ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, inputStream);
		
	    ArchiveEntry entry = null; 
	    while ((entry = archiveInputStream.getNextEntry()) != null) {
	        File outputFile = new File(parent, entry.getName());
	        if (entry.isDirectory()) {
	            if (!outputFile.exists()) {
	            	outputFile.mkdir();
	            }
	        } else {
	        	FileOutputStream outputFileStream = new FileOutputStream(outputFile); 
	            IOUtils.copy(archiveInputStream, outputFileStream);
	            outputFileStream.close();
	        }
	    }
	    archiveInputStream.close(); 
	}
}
