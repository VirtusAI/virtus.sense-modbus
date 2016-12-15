package me.legrange.modbus;

public abstract class ModbusPort implements AutoCloseable {
	
	public abstract ResponseFrame poll(ModbusFrame req) throws ModbusException;
	
	public abstract ResponseFrame write(WriteRegister req) throws ModbusException;

}
