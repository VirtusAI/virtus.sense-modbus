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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * A serial Modbus port
 *
 * @since 1.0
 * @author Gideon le Grange https://github.com/GideonLeGrange
 */
public class SerialModbusPort extends ModbusPort {

    /**
     * Open the named serial Modbus port at the given baud rate.
     *
     * @param port The port to open.
     * @param baud The speed at which to access the port.
     * @return Thrown if there a an error opeing the port.
     * @throws SerialException
     */
    public static SerialModbusPort open(String port, int baud, int databits, int stopbits, int parity) throws SerialException {
        SerialModbusPort con = new SerialModbusPort();
        con.findAndOpen(port, baud, databits, stopbits, parity);
        return con;
    }

    /**
     * Do a Modbus poll and return the received response.
     *
     * @param req The Modbus request packet
     * @return The Modbus response packet
     * @throws ModbusException Thrown if an error occurs.
     */
    public ResponseFrame poll(ReadInputRegisters req) throws ModbusException {
        return sendFrame(req);
    }

      /**
     * Do a Modbus write and return the received response.
     *
     * @param req The Modbus request packet
     * @return The Modbus response packet
     * @throws ModbusException Thrown if an error occurs.
     */
    public ResponseFrame write(WriteRegister req) throws ModbusException {
        return sendFrame(req);
    }
    
    private synchronized ResponseFrame sendFrame(ModbusFrame req) throws ModbusException {
        try {
            out.write(req.asBytes());
            out.flush();
            if (debug) {
                System.out.println(String.format("SEND: %s", FrameUtil.hexString(req.asBytes())));
            }
            int i = 10;
            while (i > 0) {
                byte buf[] = new byte[256];
                if (in.available() > 0) {
                    int len = in.read(buf);
                    byte data[] = new byte[len];
                    System.arraycopy(buf, 0, data, 0, len);
                    if (debug) {
                        System.out.println(String.format("RECV: %s ", FrameUtil.hexString(data)));
                    }
                    return new ResponseFrame(data);
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    i--;
                }
            }
            throw new NoDataException("No data received from Modbus");
        } catch (IOException ex) {
            throw new SerialException(String.format("Error sending Modbus request [%s]", ex.getMessage()));
        }
    }

    @Override
    public void close() throws SerialException {
        try {
            out.close();
            in.close();
            com.close();
        } catch (IOException ex) {
            throw new SerialException(String.format("Error closing Modbus port [%s]", ex.getMessage()));
        }
    }

    /**
     * Hidden constructor
     */
    private SerialModbusPort() {
    }

    /**
     * Find the serial port and open it at the specifed baud rate. The streams
     * used to access serial data are also created.
     *
     * @param port The name of the serial port
     * @param baud The speed at which to access the port.
     * @throws SerialException
     */
    private void findAndOpen(String port, int baud, int databits, int stopbits, int parity) throws SerialException {
        CommPortIdentifier portId;
        try {
            portId = CommPortIdentifier.getPortIdentifier(port);
        } catch (NoSuchPortException ex) {
            throw new SerialException(String.format("Could not find serial port '%s'", port), ex);
        }
        try {
            com = (SerialPort) portId.open(getClass().getSimpleName(), timeout);
            com.setSerialPortParams(baud, databits, stopbits, parity);
            com.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            //port.enableReceiveTimeout(MAX_TIMEOUT);
            //port.enableReceiveThreshold(0);
            in = com.getInputStream();
            out = com.getOutputStream();
            out.flush();
        } catch (PortInUseException ex) {
            throw new SerialException(String.format("Serial port '%s' is in use", port), ex);
        } catch (UnsupportedCommOperationException | IOException ex) {
            throw new SerialException(String.format("Error accessing serial port '%s': %s", port, ex.getMessage()), ex);
        }
    }

    private InputStream in;
    private OutputStream out;
    private SerialPort port;

    private SerialPort com;
    private static final int timeout = 60000;
  //  private static final boolean debug = Boolean.valueOf(System.getProperty(SerialModbusPort.class.getPackage().getName() + ".debug", "false"));
    private static final boolean debug = true;

}
