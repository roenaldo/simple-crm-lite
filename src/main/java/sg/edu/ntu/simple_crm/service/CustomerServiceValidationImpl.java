package sg.edu.ntu.simple_crm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sg.edu.ntu.simple_crm.exceptions.CustomerNotFoundException;
import sg.edu.ntu.simple_crm.exceptions.InvalidCustomerException;
import sg.edu.ntu.simple_crm.exceptions.InvalidCustomerIdException;
import sg.edu.ntu.simple_crm.model.Customer;
import sg.edu.ntu.simple_crm.model.Interaction;
import sg.edu.ntu.simple_crm.repository.CustomerRepository;
import sg.edu.ntu.simple_crm.repository.InteractionRepository;

@Service
public class CustomerServiceValidationImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final InteractionRepository interactionRepository;

    public CustomerServiceValidationImpl(CustomerRepository customerRepository,
            InteractionRepository interactionRepository) {
        this.customerRepository = customerRepository;
        this.interactionRepository = interactionRepository;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        validateCustomer(customer);
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomer(Long id) {
        if (id <= 0) {
            throw new InvalidCustomerIdException(id);
        }
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        validateCustomer(customer);
        Customer customerToUpdate = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customerToUpdate.setFirstName(customer.getFirstName());
        customerToUpdate.setLastName(customer.getLastName());
        customerToUpdate.setEmail(customer.getEmail());
        customerToUpdate.setContactNo(customer.getContactNo());
        customerToUpdate.setJobTitle(customer.getJobTitle());
        customerToUpdate.setYearOfBirth(customer.getYearOfBirth());
        return customerRepository.save(customerToUpdate);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Interaction addInteractionToCustomer(Long id, Interaction interaction) {
        Customer selectedCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        interaction.setCustomer(selectedCustomer);
        return interactionRepository.save(interaction);
    }

    @Override
    public List<Customer> searchCustomers(String firstName) {
        return customerRepository.findByFirstName(firstName);
    }

    @Override
    public List<Customer> searchCustomersByFirstNameContaining(String firstName) {
        return customerRepository.findByFirstNameContaining(firstName);
    }

    @Override
    public List<Customer> searchCustomersByJobTitle(String jobTitle) {
        return customerRepository.findByJobTitleJPQL(jobTitle);
    }

    @Override
    public List<Customer> searchCustomersByLastName(String lastName) {
        return customerRepository.findByLastNameJPQL(lastName);
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidCustomerException("Customer cannot be null");
        }
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            throw new InvalidCustomerException("First name cannot be empty");
        }
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            throw new InvalidCustomerException("Last name cannot be empty");
        }
        if (customer.getEmail() == null || !customer.getEmail().contains("@")) {
            throw new InvalidCustomerException("Email must contain '@'");
        }
    }

}
