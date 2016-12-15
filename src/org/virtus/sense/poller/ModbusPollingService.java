package org.virtus.sense.poller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.virtus.sense.poller.config.Device;
import org.virtus.sense.poller.config.Register;

import me.legrange.modbus.ModbusError;
import me.legrange.modbus.ModbusException;
import me.legrange.modbus.ModbusFrame;
import me.legrange.modbus.ModbusPort;
import me.legrange.modbus.ResponseFrame;

public class ModbusPollingService extends TimerTask {

	private Map<Integer, Device> activeDevices;
	private Map<Integer, List<Poll>> devicePolls;
	private List<ModbusListener> listeners;
	
	private ModbusPort port;
	
	public ModbusPollingService(Map<Integer, Device> activeDevices, 
			List<ModbusListener> listeners, ModbusPort port) {
		this.activeDevices = activeDevices;
		this.listeners = listeners;
		this.port = port;
		
		this.devicePolls = new HashMap<>();
	}

	@Override
	public void run() {
		// Poll active devices
		activeDevices.entrySet().stream()
			.forEach(e -> {
				
				// generate poll requests one time
				if(!devicePolls.containsKey(e.getKey())) {
					devicePolls.put(e.getKey(), Poll.generatePolls(e.getValue().registers));
				}
				
				// iterate device poll lists
				devicePolls.entrySet().stream().forEach(polls -> {
					
					// poll result map gatherer
					Map<Register, byte[]> pollsRes = new HashMap<>();
					
					// iterate polls
					polls.getValue().stream().forEach(poll -> {
						try {							
							ResponseFrame res = port.poll(ModbusFrame.readRegister(poll.getFunc(), polls.getKey(), 
									poll.getAddress(), poll.getSize()));
							
							// gather poll result
							if(!res.isError())
								pollsRes.putAll(poll.getPollResult(res.getBytes()));
							else
								System.out.println("Modbus error: " + ModbusError.valueOf(res.getFunction()));
							
							/*listeners.stream().forEach(l -> {
								if(!res.isError()) {
		                            poll.applyBytes(res.getBytes(), l);
								} else {
									l.error(new ModbusReaderException(
											String.format("Modbus error: %s", ModbusError.valueOf(res.getFunction()))));
								}
							});*/
						} catch (ModbusException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					});
					
					// call listeners with result
					listeners.forEach(l -> l.pollingComplete(pollsRes));					
				});
				
			});
	}

}
