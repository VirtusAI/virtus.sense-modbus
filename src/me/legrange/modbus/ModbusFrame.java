/*
 * Copyright 2016 gideon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.legrange.modbus;

/**
 * An abstract representation of a Modbus frame. Subclassed for specifc use cases. 
 * 
 * @since 1.0
 * @author Gideon le Grange https://github.com/GideonLeGrange
 */
abstract public class ModbusFrame {

	public static final int READ_INPUT_REGISTERS = 3;
	public static final int READ_HOLDING_REGISTERS = 4;
	
	public static ModbusFrame readRegister(int func, int deviceId, int register, int size) throws CrcException {
		switch (func) {
		case READ_INPUT_REGISTERS:
			return new ReadInputRegisters(deviceId, register, size);
		case READ_HOLDING_REGISTERS:
			return new ReadHoldingRegisters(deviceId, register, size);
		default:
			return new ReadInputRegisters(deviceId, register, size);
		}
	}
    
    /** 
     * Create a new frame from the supplied raw Modbus data. 
     * @param frame The data.
     * @throws CrcException Thrown if the data fails the CRC check. 
     */
    ModbusFrame(byte frame[]) throws CrcException {
        this.frame = new byte[frame.length];
        System.arraycopy(frame, 0, this.frame, 0, frame.length);
        FrameUtil.validate(frame);
    }
    
    /** Return the Modbus function code */
    public byte getFunction() { 
        return frame[1];
    }
        
    public int getSlaveId() {
        return frame[0];
    }

    /** Return the frame as raw Modbus data. */
    byte[] asBytes() {
        return frame;
    }

    @Override
    public String toString() {
        return FrameUtil.hexString(frame);
    }

    protected final byte frame[];

}
