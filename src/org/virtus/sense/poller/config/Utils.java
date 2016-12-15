package org.virtus.sense.poller.config;

abstract class Utils {

	static public int[] toIntArray(byte[] bytes) {
		int[] ints = new int[bytes.length];
		
		for (int i = 0; i < bytes.length; i++) {
			ints[i] = (int) bytes[i];
		}
		
		return ints;
	}

}
