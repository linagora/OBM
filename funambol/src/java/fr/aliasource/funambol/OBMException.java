package fr.aliasource.funambol;

public class OBMException extends Exception {

	 /**
	 * 
	 */
	private static final long serialVersionUID = -441463969289693752L;


	/**
     * Creates a new instance of <code>EntityException</code> without
     * detail message.
     */
    public OBMException() {

    }

    /**
     * Constructs an instance of <code>EntityException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public OBMException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>EntityException</code> with the
     * specified exception.
     *
     * @param msg the detail message.
     */
    public OBMException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs an instance of <code>EntityException</code> with the
     * specified detail message and the given cause.
     *
     * @param msg the detail message.
     * @param cause the cause.
     */
    public OBMException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
