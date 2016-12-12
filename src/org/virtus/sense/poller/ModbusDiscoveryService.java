package org.virtus.sense.poller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.function.Predicate;

import org.virtus.sense.poller.config.Device;
import org.virtus.sense.poller.config.DeviceLibrary;
import org.virtus.sense.poller.config.MappingErrorException;
import org.virtus.sense.poller.config.ValidationStep;

import me.legrange.modbus.ModbusException;
import me.legrange.modbus.ModbusPort;
import me.legrange.modbus.ReadInputRegisters;
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
				ReadInputRegisters req = new ReadInputRegisters(pollingAddress, 
						e.register.address, e.register.size);
				
				ResponseFrame res = port.poll(req);
				
				if(!res.isError()) {
					System.out.println("Validate match of " + Arrays.toString(res.getBytes()) + " with " + e.match);
					return false;
				} else {
					System.out.println("Deu merda");
					return false;
				}							
			} catch (ModbusException err) {
				err.printStackTrace();
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
