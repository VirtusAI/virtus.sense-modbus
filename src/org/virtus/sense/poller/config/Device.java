package org.virtus.sense.poller.config;

import java.util.List;

public class Device {

	public int developerId;
	
	public int deviceId;
	
	public String label;
	
	public List<ValidationStep> validation;
	
	public List<Register> registers;

}
