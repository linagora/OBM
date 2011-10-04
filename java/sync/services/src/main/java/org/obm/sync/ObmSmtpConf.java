package org.obm.sync;

public interface ObmSmtpConf {

	int getServerPort(String domain);

	String getServerAddr(String domain);

}