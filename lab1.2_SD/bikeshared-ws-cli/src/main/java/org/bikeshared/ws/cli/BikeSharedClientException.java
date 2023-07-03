package org.bikeshared.ws.cli;


public class BikeSharedClientException extends Exception {

	private static final long serialVersionUID = 1L;

	public BikeSharedClientException() {
    }

    public BikeSharedClientException(String message) {
        super(message);
    }

    public BikeSharedClientException(Throwable cause) {
        super(cause);
    }

    public BikeSharedClientException(String message, Throwable cause) {
        super(message, cause);
    }

}