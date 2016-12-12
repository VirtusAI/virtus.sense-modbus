package org.virtus.sense.poller.config;

public class MappingErrorException extends Exception {

	private static final long serialVersionUID = 8261829840134099179L;

	public MappingErrorException(String message) {
        super(message);
    }

    public MappingErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
