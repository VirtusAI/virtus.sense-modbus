package examples;

import java.io.File;

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
					"/dev/ttyUSB1", 
					9600, 
					8, 
					1, 
					SerialPort.PARITY_EVEN, 
					5000, 
					20000, 
					new FileLibrary(new File("devices")), 
					new MemoryStore());
			
			poller.addListener(new ModbusListener() {
				
				@Override
				public void received(Register reg, byte[] bytes) {
					System.out.println("Data received");
					
				}
				
				@Override
				public void error(Throwable e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void deviceDetected(Device dev) {
					System.out.println("New device was detected");
					
				}
			});
			
			poller.open();
		} catch (SerialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
