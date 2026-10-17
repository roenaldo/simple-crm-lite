package sg.edu.ntu.simple_crm.exceptions;

public class InvalidCustomerException extends RuntimeException {

  public InvalidCustomerException(String message) {
    super(message);
  }
}
