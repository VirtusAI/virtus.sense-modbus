package examples;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.virtus.sense.poller.ModbusListener;
import org.virtus.sense.poller.ModbusPoller;
import org.virtus.sense.poller.config.Device;
import org.virtus.sense.poller.config.FileLibrary;
import org.virtus.sense.poller.config.Register;
import org.virtus.sense.store.MemoryStore;

import me.legrange.modbus.SerialException;
import purejavacomm.SerialPort;

public class TestMemoryStore {
	
	public static void main(String[] args) {
		try {
			ModbusPoller poller = ModbusPoller.createRTUPoller(
					"COM7", 
					9600, 
					8, 
					1, 
					SerialPort.PARITY_NONE, 
					30000, 
					10000, 
					new FileLibrary(new File("devices")), 
					new MemoryStore());
			
			poller.addListener(new ModbusListener() {
				
				@Override
				public void received(Register reg, byte[] bytes) {
					System.out.println("Data received -> " + Arrays.toString(bytes));
					System.out.println(Register.decode(reg, bytes));
					
				}
				
				@Override
				public void error(Throwable e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void deviceDetected(Device dev) {
					System.out.println("New device was detected");
					
				}

				@Override
				public void pollingComplete(Map<Register, byte[]> map) {
					
					System.out.println("Polling complete");
					
					map.entrySet().forEach(e -> System.out.println("\t" + e.getKey().name + " = " + Register.decode(e.getKey(), e.getValue())));
					
				}
			});
			
			poller.open();
		} catch (SerialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
