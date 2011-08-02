package org.obm.push.mail;

import java.util.LinkedList;
import java.util.List;

import org.minig.imap.Address;
import org.obm.push.bean.MSAddress;

public class AddressConverter {
	
	private AddressConverter() {
	}
	
	public static MSAddress convertAddress(Address add){
		if(add == null){
			return null;
		}
		MSAddress msAdd = new MSAddress(add.getDisplayName(),add.getMail());

		return msAdd;
	}
	
	public static List<MSAddress> convertAddresses(List<Address> adds){
		List<MSAddress> ret = new LinkedList<MSAddress>();
		for(Address add : adds){
			ret.add(convertAddress(add));
		}	
		return ret;
	}
	
	
}
