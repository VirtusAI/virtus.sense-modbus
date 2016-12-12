package org.virtus.sense.poller.config;

public class DeviceNotFoundException extends Exception {

	private static final long serialVersionUID = 8261829840134099179L;

	public DeviceNotFoundException(String message) {
        super(message);
    }

    public DeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
