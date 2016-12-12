package org.virtus.sense.poller;

public class LoadStoreErrorException  extends Exception {

	private static final long serialVersionUID = 3904161535211143549L;

	public LoadStoreErrorException(String message) {
        super(message);
    }

    public LoadStoreErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
