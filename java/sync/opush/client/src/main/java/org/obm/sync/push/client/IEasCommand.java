package org.obm.sync.push.client;

import org.apache.commons.httpclient.HttpClient;

public interface IEasCommand<T> {

	T run(AccountInfos ai, OPClient opc, HttpClient hc) throws Exception;
	
}
