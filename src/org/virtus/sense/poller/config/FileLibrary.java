package org.virtus.sense.poller.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FileLibrary extends DeviceLibrary {

	private static final String pathSeparator = "/";
	private static final String fileTermination = ".json";
	private static final String mainFile = "/modbus" + fileTermination;
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private File file;

	public FileLibrary(File file) {
		super();
		this.file = file;
	}

	@Override
	public Device getDevice(int developerId, int deviceId) 
			throws DeviceNotFoundException, MappingErrorException {
		
		// load developers file for the first time
		if(this.developers == null) {
			this.loadDevelopers();			
		}
		
		try {
			return loadDevice(developerId, deviceId);			
		} catch (DeviceNotFoundException e) {
			// Try to load from definition files
			Developer dev = this.developers.stream()
					.filter(d -> d.id == developerId)
					.findFirst()
					.orElseThrow(() -> new DeviceNotFoundException("Developer not valid"));
			
			DeviceReference ref = dev.devices.stream()
					.filter(d -> d.id == deviceId)
					.findFirst()
					.orElseThrow(() -> new DeviceNotFoundException("DeviceId not valid"));
			
			File refFile = new File(file.getPath() + pathSeparator + dev.name + 
					pathSeparator + ref.name + fileTermination);
			
			try {
				Device loadedDev = mapper.readValue(refFile, Device.class);
				this.loadedDevices.add(loadedDev);
				return loadedDev;
				
			} catch (IOException e1) {
				throw new MappingErrorException(e1.getMessage());
			}
		}
	}

	@Override
	public List<Device> getAllDevices() throws MappingErrorException {
		
		// load developers file for the first time
		if(this.developers == null) {
			this.loadDevelopers();			
		}
		
		// load all devices
		this.developers.stream().forEach(dev -> {
			dev.devices.stream().forEach(d -> {
				try {
					getDevice(dev.id, d.id);
				} catch (DeviceNotFoundException | MappingErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		});
		
		return loadedDevices;
	}

	@Override
	protected Device loadDevice(int developerId, int deviceId) throws DeviceNotFoundException {
		return 
			this.loadedDevices.stream()
			.filter(d -> d.developerId == developerId && d.deviceId == deviceId)
			.findFirst()
			.orElseThrow(() -> new DeviceNotFoundException("Developer/Device key not loaded"));
	}

	@Override
	protected void loadDevelopers() throws MappingErrorException {
		try {
			this.developers = mapper.readValue(new File(file.getPath() + mainFile), new TypeReference<List<Developer>>(){});
		} catch (IOException e) {
			throw new MappingErrorException(e.getMessage());
		}	
	}
}
