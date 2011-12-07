package org.obm.opush;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePortFinder {

	public static final int findFreePort() {
		for (int i = 8000; i < 8100; i++) {
			ServerSocket socket;
			try {
				socket = new ServerSocket(i);
				socket.close();
				return i;
			} catch (IOException portInUse) {
			}
		}
		throw new RuntimeException("Can't find a free port");
	}

	
}
