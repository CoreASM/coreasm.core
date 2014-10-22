package CompilerRuntime;

public class InvalidLocationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidLocationException(String string) {
		super(string);
	}

	public InvalidLocationException(Exception e) {
		super(e);
	}

}
