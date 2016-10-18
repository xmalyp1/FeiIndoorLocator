package sk.stuba.fei.indoorlocator.database.exception;

/**
 * Created by Patrik on 14.10.2016.
 */

public class DatabaseException extends Exception {
    public DatabaseException() { super(); }
    public DatabaseException(String message) { super(message); }
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
    public DatabaseException(Throwable cause) { super(cause); }
}
