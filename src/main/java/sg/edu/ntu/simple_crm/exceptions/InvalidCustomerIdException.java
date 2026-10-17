package sg.edu.ntu.simple_crm.exceptions;

public class InvalidCustomerIdException extends RuntimeException {

  public InvalidCustomerIdException(Long id) {
    super("Customer id must be positive: " + id);
  }
}
