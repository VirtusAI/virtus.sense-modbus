package org.virtus.sense.poller.config;

import java.util.ArrayList;
import java.util.List;

public abstract class DeviceLibrary {
	
	protected List<Developer> developers;	
	protected List<Device> loadedDevices;
	
	protected DeviceLibrary() {
		this.loadedDevices = new ArrayList<>();
	}
	
	abstract public Device getDevice(int developerId, int deviceId) 
			throws DeviceNotFoundException, MappingErrorException;
	
	abstract public List<Device> getAllDevices() throws MappingErrorException;
	
	abstract protected Device loadDevice(int developerId, int deviceId) 
			throws DeviceNotFoundException;
	
	abstract protected void loadDevelopers() 
			throws MappingErrorException;
	
}
