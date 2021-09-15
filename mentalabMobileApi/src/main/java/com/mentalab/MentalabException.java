package com.mentalab;

//changed access modifier public to private
public class MentalabException extends Exception {
    public MentalabException(String errorMessage, Throwable err) {
    }
}

class NoBluetoothException extends MentalabException {
    public NoBluetoothException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
    class NoConnectionException extends com.mentalab.MentalabException {
        public NoConnectionException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }


    class InvalidCommandException extends com.mentalab.MentalabException {
        public InvalidCommandException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }
    class CommandFailedException extends com.mentalab.MentalabException {
        public CommandFailedException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }


    class InvalidDataException extends com.mentalab.MentalabException {
        public InvalidDataException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }

