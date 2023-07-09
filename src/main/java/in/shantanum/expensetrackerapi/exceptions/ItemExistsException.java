package in.shantanum.expensetrackerapi.exceptions;

public class ItemExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ItemExistsException(String message) {
		super(message);
	}
}
