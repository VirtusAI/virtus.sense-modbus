package org.virtus.sense.poller;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.function.Predicate;

import org.virtus.sense.poller.config.Device;
import org.virtus.sense.poller.config.DeviceLibrary;
import org.virtus.sense.poller.config.MappingErrorException;
import org.virtus.sense.poller.config.ValidationStep;

import me.legrange.modbus.ModbusException;
import me.legrange.modbus.ModbusFrame;
import me.legrange.modbus.ModbusPort;
import me.legrange.modbus.ResponseFrame;

public class ModbusDiscoveryService extends TimerTask {

	private static int MIN_ADDRESS = 1;
	private static int MAX_ADDRESS = 255;
	
	private Map<Integer, Device> activeDevices;
	private DeviceLibrary lib; 
	private List<ModbusListener> listeners;
	
	private ModbusPort port;
	
	private int pollingAddress;
	private Predicate<ValidationStep> validationPred;
	
	public ModbusDiscoveryService(Map<Integer, Device> activeDevices, 
			List<ModbusListener> listeners, DeviceLibrary lib, ModbusPort port) {
		this.activeDevices = activeDevices;
		this.listeners = listeners;
		this.lib = lib;
		this.port = port;
		this.pollingAddress = MIN_ADDRESS;
		
		this.validationPred = e -> {
			try {				
				ResponseFrame res = this.port.poll(ModbusFrame.readRegister(e.register.func, pollingAddress, 
						e.register.address, e.register.size));
				
				if(!res.isError()) {
					boolean valid = e.validate(res.getBytes());
					
					if(valid)
						System.out.println("Validated device " + e.register.name);
					return true;
				} else {
					System.out.println("Deu merda");
					return false;
				}							
			} catch (ModbusException err) {
				System.err.println("Skipping this address");
				//err.printStackTrace();
				return false;
			}
		};
	}

	@Override
	public void run() {
		// Skip active devices
		if(!activeDevices.containsKey(pollingAddress)) {
			// Try to verify if there is any device here
			try {
				
				System.out.println("Testing addr " + pollingAddress);
				
				// poll each device from library
				lib.getAllDevices().forEach(dev -> {
					if(dev.validation.stream().allMatch(validationPred)) {
						activeDevices.put(pollingAddress, dev);
						
						listeners.forEach(l -> l.deviceDetected(dev));
					}
						
				});
			} catch (MappingErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		pollingAddress = (pollingAddress + 1) % MAX_ADDRESS;
	}

}
