package javax.microedition.lcdui;

class VirtualKeyboardException extends Exception {
    /**
     * Constructs an <code>IOException</code> with <code>null</code>
     * as its error detail message.
     */
    public VirtualKeyboardException() {
    super();
    }

    /**
     * Constructs an <code>IOException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public VirtualKeyboardException(String s) {
    super(s);
    }
}

