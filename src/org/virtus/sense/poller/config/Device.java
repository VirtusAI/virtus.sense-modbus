package org.virtus.sense.poller.config;

import java.util.List;

public class Device {

	public int developerId;
	
	public int deviceId;
	
	public String password;
	
	public String label;
	
	public Register uniqueRegister;
	
	// result of fetching uniqueId;
	private String uniqueId;
	
	public List<ValidationStep> validation;
	
	public List<Register> registers;
	
	public boolean hasId() {
		return uniqueId != null;
	}
	
	public void setId(String id) {
		this.uniqueId = id;
	}
	
	public String getUniqueId() {
		return developerId + "-" + deviceId + "-" + uniqueId;
	}

}
