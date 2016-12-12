package org.virtus.sense.poller;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.virtus.sense.poller.config.Device;

import me.legrange.modbus.ModbusError;
import me.legrange.modbus.ModbusException;
import me.legrange.modbus.ModbusPort;
import me.legrange.modbus.ReadInputRegisters;
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
				
				// iterate devices
				devicePolls.entrySet().stream().forEach(polls -> {
					
					// iterate polls
					polls.getValue().stream().forEach(poll -> {
						try {
							ReadInputRegisters req = new ReadInputRegisters(polls.getKey(), 
									poll.getAddress(), poll.getSize());
							
							ResponseFrame res = port.poll(req);
							
							listeners.stream().forEach(l -> {
								if(!res.isError()) {
		                            poll.applyBytes(res.getBytes(), l);
								} else {
									l.error(new ModbusReaderException(
											String.format("Modbus error: %s", ModbusError.valueOf(res.getFunction()))));
								}
							});
						} catch (ModbusException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					});
				});
				
			});
	}

}
