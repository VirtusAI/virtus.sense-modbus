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
package org.virtus.sense.poller;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.virtus.sense.poller.config.Register;

/**
 * A modbus poll operation. 
 * @author gideon
 */
class Poll {
 
    private final int address;
    private final int func;
    private int size;
    private final Map<Integer, Register> registers = new HashMap<>();
    
    private Poll(Register reg) {
        this.address = reg.address;
        this.func = reg.func;
        this.size = reg.size;
        registers.put(0, reg);
    }
    
    private void add(Register reg) { 
        size = size + reg.size;
        registers.put(reg.address - address, reg);
    }
    
    int getAddress() {
        return address;
    }

    int getSize() {
        return size;
    };
    
    int getFunc() {
    	return func;
    }
    
    /** call listener.received() for each register and it's subset of bytes in the response received 
     * 
     * @param bytes The bytes received for all registers polled. 
     * @param listener The listener to call. 
     */
    void applyBytes(byte bytes[], ModbusListener listener) {
        registers.values().forEach((Register reg) -> {
            byte buf[] = new byte[reg.size*2];
            System.arraycopy(bytes, (reg.address - address)*2, buf, 0, reg.size*2);
            listener.received(reg, buf);
        });
    }
    
    /** return all registers and each subset of bytes in a Map 
     * 
     * @param bytes The bytes received for all registers polled. 
     */
    
    Map<Register, byte[]> getPollResult(byte[] bytes) {
    	Map<Register, byte[]> res = new HashMap<>();
    	
        registers.values().forEach((Register reg) -> {
            byte buf[] = new byte[reg.size*2];
            System.arraycopy(bytes, (reg.address - address)*2, buf, 0, reg.size*2);
            res.put(reg, buf);
        });
    	
    	return res;
    }
    
    /**
     * Group continuous registers into the same polling request
     * @param registers list of registers to be polled
     * @return list of polling requests to be made
     */
    static List<Poll> generatePolls(List<Register> registers) {
        Collections.sort(registers, (Register o1, Register o2) -> o1.address - o2.address);
        Poll poll = null;
        int currentFunction  = -1;
        List<Poll> result = new LinkedList<>();
        for (Register reg : registers) {
            if ((poll != null) && ((poll.address + poll.size) == reg.address) && reg.func == currentFunction) { 
                poll.add(reg);
            }
            else {
                poll = new Poll(reg);
                result.add(poll);
                currentFunction = reg.func;
            }
        }
        return result;
    }
    
}
