package org.obm.push.impl;

public enum Base64ParameterCodes {
    
	AttachmentName,//0
	CollectionId,//1
	CollectionName,//2
	ItemId,//3
	LongId,//4
	ParentId,//5
	Occurrence,//6
	Options,//7
	User;//8

	public static Base64ParameterCodes getParam(int value) {
		switch (value) {
		case 0:
			return AttachmentName;
		case 1:
			return CollectionId;
		case 2:
			return CollectionName;
		case 3:
			return ItemId;
		case 4:
			return LongId;
		case 5:
			return ParentId;
		case 6:
			return Occurrence;
		case 7:
			return Options;
		case 9:
			return User;
		default:
			return null;
		}
	}

}
