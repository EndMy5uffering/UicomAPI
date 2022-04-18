package com.uicomapi.commands;

public class CMDCommandException extends Exception{

	public enum ErrorReason{
		COMMAND_NOT_FOUND {
			@Override
			public String toString() {
				return "Command not found";
			}
		},
		ROOT_MISSMATCH {
			@Override
			public String toString() {
				return "Command root missmatch";
			}
		},
		CMD_MISSMATCH {
			@Override
			public String toString() {
				return "Command missmatch";
			}
		},
		OTHER {
			@Override
			public String toString() {
				return "Other";
			}
		};

	}

	private final ErrorReason reason;
	
	public CMDCommandException(ErrorReason reason) {
		this("No details given.", reason);
	}
	
	public CMDCommandException(String message, ErrorReason reason) {
		super(message);
		this.reason = reason;
	}

	public ErrorReason getErrorReason(){
		return this.reason;
	}
	
}
