package org.virtus.sense.poller.config;

public class ValidationStep {
	
	public Register register;
	
	public String match;
	
	public boolean validate(byte[] bytes) {
		
		switch (register.getType()) {
		case FLOAT:
			return Double.parseDouble(match) == Register.decode(register, bytes);
		case INT:
			return Integer.parseInt(match) == Register.decode(register, bytes);
		default:
			return false;
		}
		
		
	}

}
