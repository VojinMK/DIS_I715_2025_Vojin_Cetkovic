package util.exceptions;

public class DataIntegrityViolationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
   public DataIntegrityViolationException() {
	   
   }
   
   public DataIntegrityViolationException(String message) {
	   super(message);
   }

}
