package org.virtus.sense.poller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.virtus.sense.poller.config.Device;
import org.virtus.sense.poller.config.DeviceLibrary;
import org.virtus.sense.poller.config.DeviceNotFoundException;
import org.virtus.sense.poller.config.MappingErrorException;
import org.virtus.sense.store.DeviceStore;

import me.legrange.modbus.ModbusPort;
import me.legrange.modbus.SerialException;
import me.legrange.modbus.SerialModbusPort;

public final class ModbusPoller implements AutoCloseable {
	
	private ModbusPort port;
	
	private int discoveryPoll;
	private int pollingInterval;
	private DeviceLibrary lib; 
	private DeviceStore store;
	
	private Map<Integer, Device> activeDevices;
	private List<ModbusListener> listeners;
	
	private Timer timer;
	
	public static ModbusPoller createRTUPoller(String port, int baud,
			int databits, int stopbits, int parity,
			int discoveryPoll, int pollingInterval,
			DeviceLibrary lib, DeviceStore store) throws SerialException {
		
		ModbusPoller poller = new ModbusPoller(discoveryPoll, pollingInterval, lib, store);
		poller.port = SerialModbusPort.open(port, baud, databits, stopbits, parity);
		
		return poller;
	}
	
	public ModbusPoller(int discoveryPoll, int pollingInterval,
			DeviceLibrary lib, DeviceStore store) {
		
		this.discoveryPoll = discoveryPoll;
		this.pollingInterval = pollingInterval;
		this.lib = lib;
		this.store = store;
		
		this.timer = new Timer();
		
		this.loadStoredDevices();
		
		this.listeners = Collections.synchronizedList(new ArrayList<>());
	}
	
	public void addListener(ModbusListener list) {
		this.listeners.add(list);
	}
	
	private void loadStoredDevices() {
		activeDevices = new ConcurrentHashMap<Integer, Device>();
		
		store.getCachedDevices().stream()
			.forEach(d -> {
				try {					
					activeDevices.put(d.networkAddress, lib.getDevice(d.developerId, d.deviceId));
				} catch (DeviceNotFoundException | MappingErrorException e) {
					// TODO Handle loading store error
					e.printStackTrace();
				}
			});
	}
	
	public void open() {
		
		this.timer.scheduleAtFixedRate(
				new ModbusPollingService(activeDevices, listeners, port),
				(long) 0, 
				(long) pollingInterval);
		
		this.timer.scheduleAtFixedRate(
				new ModbusDiscoveryService(activeDevices, listeners, lib, port),
				(long) 0, 
				(long) discoveryPoll);
	}

	@Override
	public void close() throws Exception {
		this.store.close();
		
	}
}
