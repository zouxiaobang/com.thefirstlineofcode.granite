package com.thefirstlineofcode.granite.xeps.muc;

public class DuplicateRoomException extends Exception {
	private static final long serialVersionUID = -8764239393518879879L;

	public DuplicateRoomException() {
		super();
	}

	public DuplicateRoomException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DuplicateRoomException(String arg0) {
		super(arg0);
	}

	public DuplicateRoomException(Throwable arg0) {
		super(arg0);
	}
	
}
