package org.virtus.sense.poller.config;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

public class Register {
    
	private static final String VARIABLE = "_";
	
    public enum Type { FLOAT, INT, STRING; }
	
	public String name;
	public int address;
	public int size;
	public int func;
	public String type;
	public String unit;
	public String transform = "_";
	
	public Register.Type getType() {
		switch (type) {
		case "float":
			return Type.FLOAT;
		case "int":
			return Type.INT;
		default:
			return Type.STRING;
		}
	}
	
	public Expression getTransform() {
        Expression trans = new ExpressionBuilder(transform).variables(VARIABLE).build().setVariable(VARIABLE, 0);
        ValidationResult val = trans.validate();
        if (!val.isValid()) {
            throw new RuntimeException(String.format("Invalid transform '%s': %s", transform, val.getErrors()));
        }
        return trans;
	}
	
	@Override
	public int hashCode() {		
		return new String( address + name + size + func ).hashCode();
	}

    
    /** 
     * Decode the given bytes received for the given register into a double value. 
     * 
     * @param reg The register for which the bytes was received. 
     * @param bytes The bytes to decode. 
     * @return The decoded value
     */
    public static double decode(Register reg, byte bytes[]) {
        switch (reg.getType()) {
            case FLOAT :
                return decodeFloat(reg, bytes);
            case INT :
            	return decodeInt(reg, Utils.toIntArray(bytes));
            default : return 0.0;
        }
    }
    
    static double decodeFloat(Register reg, byte bytes[]) {
        float f = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
        return reg.getTransform().setVariable("_", f).evaluate();
    }
    
    static double decodeInt(Register reg, int words[]) {
        long lval = 0;
        for (int i = 0; i < words.length; ++i) {
            lval = (lval << 8) | words[i];
        }
        return reg.getTransform().setVariable("_", lval).evaluate();
    }
}
