package org.virtus.sense.store;

public class StoredDevice {
	
	public int developerId;
	
	public int deviceId;
	
	public int networkAddress;
	
	public StoredDevice() {
	}
	
	public StoredDevice(int developerId, int deviceId, int networkAddress) {
		this.developerId = developerId;
		this.deviceId = deviceId;
		this.networkAddress = networkAddress;
	}

}
