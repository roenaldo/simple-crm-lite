## Part 6: Refactoring Our `simple-crm`

We continue with the same **`simple-crm`** project from the previous lesson. Do not create a new project.

> **Folder structure reminder — standing rule for `simple-crm`.** Every class goes in a folder matching its layer, inside your base package (`sg.edu.ntu.simple_crm`):
> - `CustomerController` → `controller` folder
> - `Customer` → `model` folder
> - `CustomerService` / `CustomerServiceImpl` → `service` folder (create this folder now)
> - `CustomerRepository` → `repository` folder (create this folder now)
> - `CustomerNotFoundException` → `exceptions` folder
>
> Use **right-click on the class name → Refactor → Move** so the `package` line and all imports update automatically. If the app then fails to start with a `ConflictingBeanDefinitionException`, an old copy of the class is still in the original location — delete it and run `mvn clean`.

> 📝 **If your code looks slightly different from the examples below, that is fine.** In the previous lesson's activity you may have chosen `204 No Content` instead of `200 OK` for delete, or used `ResponseEntity<Object>` to return the exception message. Keep whichever version you built — just apply the same refactoring pattern to it.

### Repository Layer

Since the repository layer is responsible for CRUD operations, we will create a `CustomerRepository` class to handle all the CRUD operations on our `ArrayList`.

Only the repository should have access to the data store. Hence the list should be private and only accessible within the `CustomerRepository` class.

This class also needs to be annotated with `@Repository` to let Spring Boot know that it is a Spring Bean.

> 📝 **`@Component` vs `@Service` vs `@Repository` — What's the difference?**
>
> All three annotations register a class as a Spring Bean. The difference is in **intent and behaviour**:
>
> - `@Component` — The generic stereotype. Use it when the class doesn't clearly fit as a service or repository.
> - `@Service` — A specialization of `@Component`. It carries no extra technical behaviour today, but it communicates clearly that this class contains **business logic**.
> - `@Repository` — A specialization of `@Component` with one important technical addition: Spring automatically translates **persistence-layer exceptions** (e.g. database errors) into Spring's unified `DataAccessException` hierarchy. This makes error handling consistent regardless of whether you're using JDBC, JPA, or any other data access technology. Always use `@Repository` on your data access classes.
>
> In short: use the most specific annotation that fits.

```java
@Repository
public class CustomerRepository {

  private List<Customer> customers = new ArrayList<>();

  // Preload data here now — moved out of the controller
  public CustomerRepository() {
    customers.add(new Customer("Bruce", "Banner"));
    customers.add(new Customer("Peter", "Parker"));
    customers.add(new Customer("Stephen", "Strange"));
    customers.add(new Customer("Steve", "Rogers"));
  }

  // Create
  public Customer createCustomer(Customer customer) {
    customers.add(customer);
    return customer;
  }

  // Get One
  public Customer getCustomer(int index) {
    return customers.get(index);
  }

  // Get All
  public List<Customer> getAllCustomers() {
    return customers;
  }

  // Update (full replace of the customer's data)
  public Customer updateCustomer(int index, Customer customer) {
    Customer customerToUpdate = customers.get(index);
    customerToUpdate.setFirstName(customer.getFirstName());
    customerToUpdate.setLastName(customer.getLastName());
    customerToUpdate.setEmail(customer.getEmail());
    customerToUpdate.setContactNo(customer.getContactNo());
    customerToUpdate.setJobTitle(customer.getJobTitle());
    customerToUpdate.setYearOfBirth(customer.getYearOfBirth());
    return customerToUpdate;
  }

  // Delete
  public void deleteCustomer(int index) {
    customers.remove(index);
  }
}
```

Note the field is declared as `List<Customer>`, not `ArrayList<Customer>`. Coding to an interface applies to collections too — this keeps the flexibility to swap the underlying implementation later without changing any calling code.

> 📝 **Note the change to update.** Last lesson we used `customers.set(index, customer)`, which puts a new object into the list. The repository instead fetches the existing customer and copies the values onto it, field by field.

> 📝 **Production note — returning the internal list.** `getAllCustomers()` returns the repository's actual list, not a copy. That means anything holding that reference can add or remove customers directly, bypassing the repository entirely. In production you would return a copy (`new ArrayList<>(customers)`) or an unmodifiable view (`Collections.unmodifiableList(customers)`) to protect the data store. We leave it as-is here for simplicity, but this is exactly the kind of encapsulation leak that causes hard-to-trace bugs in real systems.

As you can see, the purpose of this layer is just to perform CRUD operations. It contains no business logic.

### Service Layer

Next, we will create a `CustomerService` class to handle all the business logic.

`CustomerService` will need to call our `CustomerRepository` to perform CRUD operations, since updating the data store is the responsibility of the repository layer. We also want to move our helper method `getCustomerIndex()` from `CustomerController` into the service, because finding a customer is business logic.

The service class needs to be annotated with `@Service` to let Spring Boot know that it is a Spring Bean.

```java
@Service
public class CustomerService {

  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public Customer createCustomer(Customer customer) {
    return customerRepository.createCustomer(customer);
  }

  public Customer getCustomer(String id) {
    return customerRepository.getCustomer(getCustomerIndex(id));
  }

  public List<Customer> getAllCustomers() {
    return customerRepository.getAllCustomers();
  }

  public Customer updateCustomer(String id, Customer customer) {
    return customerRepository.updateCustomer(getCustomerIndex(id), customer);
  }

  public void deleteCustomer(String id) {
    customerRepository.deleteCustomer(getCustomerIndex(id));
  }

  private int getCustomerIndex(String id) {
    for (Customer customer : customerRepository.getAllCustomers()) {
      if (customer.getId().equals(id)) {
        return customerRepository.getAllCustomers().indexOf(customer);
      }
    }
    throw new CustomerNotFoundException(id);
  }
}
```

> 📝 **Our exception handling does not change.** `CustomerNotFoundException` is now thrown from the service instead of the controller, but it is an **unchecked** exception (`extends RuntimeException`), so it travels up through the layers on its own — no `throws` clause needed anywhere. The `try`/`catch` blocks in the controller stay exactly as they are and keep working. Later in the module we will move this handling out of the controller entirely using `@ControllerAdvice`.

### Controller Layer

Finally, we modify `CustomerController` to use the `CustomerService` class. Notice we are using constructor injection — we let Spring manage the `CustomerService` instance for us instead of creating it with `new`.

The controller no longer needs the `ArrayList`, the preloaded data in its constructor, or the `getCustomerIndex()` helper. Delete all three — they now live in the repository and service.

> 📝 **You will get a compile error on `getAllCustomers` — this is expected.** From last lesson your method returns `ResponseEntity<ArrayList<Customer>>`, but the repository returns `List<Customer>`, so the types no longer match. Change the return type to `ResponseEntity<List<Customer>>` and add the `java.util.List` import (VS Code will offer it as a quick fix with `Ctrl+.`).
>
> This is the same principle as coding to an interface for the service layer: the controller should not care *which kind* of list it receives, only that it is a `List`. Always use the interface type, not the concrete collection class.

> 📝 **Why not use `new CustomerService()` here?** Service classes are designed to provide functionality — not to hold data. We only ever need one instance in the entire application. If every class that needed `CustomerService` called `new CustomerService()`, we'd end up with multiple unnecessary instances. By using constructor injection, Spring creates exactly one instance and reuses it everywhere — the **Singleton pattern**, which is the default behaviour for all Spring beans.

```java
@RestController
@RequestMapping("/customers")
public class CustomerController {

  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  // CREATE
  @PostMapping("")
  public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
    Customer newCustomer = customerService.createCustomer(customer);
    return new ResponseEntity<>(newCustomer, HttpStatus.CREATED);
  }

  // READ (GET ALL)
  @GetMapping("")
  public ResponseEntity<List<Customer>> getAllCustomers() {
    List<Customer> allCustomers = customerService.getAllCustomers();
    return new ResponseEntity<>(allCustomers, HttpStatus.OK);
  }

  // READ (GET ONE)
  @GetMapping("/{id}")
  public ResponseEntity<Customer> getCustomer(@PathVariable String id) {
    try {
      Customer foundCustomer = customerService.getCustomer(id);
      return new ResponseEntity<>(foundCustomer, HttpStatus.OK);
    } catch (CustomerNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  // UPDATE
  @PutMapping("/{id}")
  public ResponseEntity<Customer> updateCustomer(@PathVariable String id, @RequestBody Customer customer) {
    try {
      Customer updatedCustomer = customerService.updateCustomer(id, customer);
      return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    } catch (CustomerNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  // DELETE
  @DeleteMapping("/{id}")
  public ResponseEntity<HttpStatus> deleteCustomer(@PathVariable String id) {
    try {
      customerService.deleteCustomer(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (CustomerNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
```

Notice that `customerService` is declared `final`. This is a best practice with constructor injection — since the dependency is set once in the constructor and never changes, marking it `final` makes that explicit and prevents accidental reassignment.

Test all the endpoints again after refactoring. They should all work exactly as before.

### Coding to an Interface

**Coding to an interface** means writing our code to be dependent on an interface instead of a concrete class. This promotes loose coupling and makes our code more flexible and easy to change.

For our service layer, it is good practice to code to an interface, because we may want to change the implementation in the future.

Let's rename our `CustomerService.java` to `CustomerServiceImpl.java` and create a new interface called `CustomerService.java` with all the method signatures:

```java
public interface CustomerService {
  Customer createCustomer(Customer customer);
  Customer getCustomer(String id);
  List<Customer> getAllCustomers();
  Customer updateCustomer(String id, Customer customer);
  void deleteCustomer(String id);
}
```

Next, our `CustomerServiceImpl` class should implement the `CustomerService` interface. **Remember to add the `implements CustomerService` clause** — renaming the class alone does not make it implement the new interface. If this step is missed, `CustomerServiceImpl` will not be seen as a candidate for `CustomerService` injection, and the behaviour you get later will be confusing rather than a clear error.

```java
@Service
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;

  public CustomerServiceImpl(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Override
  public Customer createCustomer(Customer customer) {
    return customerRepository.createCustomer(customer);
  }

  @Override
  public Customer getCustomer(String id) {
    return customerRepository.getCustomer(getCustomerIndex(id));
  }

  @Override
  public List<Customer> getAllCustomers() {
    return customerRepository.getAllCustomers();
  }

  @Override
  public Customer updateCustomer(String id, Customer customer) {
    return customerRepository.updateCustomer(getCustomerIndex(id), customer);
  }

  @Override
  public void deleteCustomer(String id) {
    customerRepository.deleteCustomer(getCustomerIndex(id));
  }

  private int getCustomerIndex(String id) {
    for (Customer customer : customerRepository.getAllCustomers()) {
      if (customer.getId().equals(id)) {
        return customerRepository.getAllCustomers().indexOf(customer);
      }
    }
    throw new CustomerNotFoundException(id);
  }
}
```

Note that we do not have to change anything in `CustomerController.java`, as it is already using the `CustomerService` type:

```java
private final CustomerService customerService;

public CustomerController(CustomerService customerService) {
  this.customerService = customerService;
}
```

When Spring Boot encounters a `CustomerService` type dependency in the `CustomerController`, it looks for a bean that implements the `CustomerService` interface. Since we annotated `CustomerServiceImpl` with `@Service`, Spring Boot creates that bean and injects it into the controller.

Test the endpoints again to make sure they still work.

### @Primary and @Qualifier

Now let's say we want a second implementation of our service layer that logs method calls. Create `CustomerServiceWithLoggingImpl.java` in the `service` folder.

It must implement all five interface methods, but we only need logging on a couple of them to see it working:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerServiceWithLoggingImpl implements CustomerService {

  private static final Logger logger = LoggerFactory.getLogger(CustomerServiceWithLoggingImpl.class);
  private final CustomerRepository customerRepository;

  public CustomerServiceWithLoggingImpl(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Override
  public Customer createCustomer(Customer customer) {
    logger.info("CustomerServiceWithLoggingImpl.createCustomer() called");
    return customerRepository.createCustomer(customer);
  }

  @Override
  public List<Customer> getAllCustomers() {
    logger.info("CustomerServiceWithLoggingImpl.getAllCustomers() called");
    return customerRepository.getAllCustomers();
  }

  @Override
  public Customer getCustomer(String id) {
    return customerRepository.getCustomer(getCustomerIndex(id));
  }

  @Override
  public Customer updateCustomer(String id, Customer customer) {
    return customerRepository.updateCustomer(getCustomerIndex(id), customer);
  }

  @Override
  public void deleteCustomer(String id) {
    customerRepository.deleteCustomer(getCustomerIndex(id));
  }

  private int getCustomerIndex(String id) {
    for (Customer customer : customerRepository.getAllCustomers()) {
      if (customer.getId().equals(id)) {
        return customerRepository.getAllCustomers().indexOf(customer);
      }
    }
    throw new CustomerNotFoundException(id);
  }
}
```

Now when you try to run the application, you will get an error:

```
Parameter 0 of constructor in CustomerController required a single bean, but 2 were found
```

This is because Spring Boot does not know which bean to inject — we now have 2 beans implementing the `CustomerService` interface. There are two ways to resolve this.

The first way is to annotate `CustomerServiceImpl` with `@Primary` to mark it as the default implementation:

```java
@Primary
@Service
public class CustomerServiceImpl implements CustomerService {
  // ...
}
```

The second way is to use `@Qualifier` in the controller to specify exactly which implementation to inject. The bean name is the class name with the first letter in lowercase:

```java
public CustomerController(@Qualifier("customerServiceWithLoggingImpl") CustomerService customerService) {
  this.customerService = customerService;
}
```

Try the `@Qualifier` version and hit `GET /customers` — you should see the log line appear in your console, confirming the logging implementation is the one being used.

By coding to an interface, we can swap implementations without touching any of the controller's endpoint code.

> 📝 **Note:** `@Primary` and `@Qualifier` can be used together. If both are present, `@Qualifier` at the injection point wins over `@Primary` on the bean — it is a more specific instruction at the point of use.

> ⚠️ **Before you finish — clean up.** You are ending this lesson with two `CustomerService` implementations. If you leave both in place with **no** `@Primary` and **no** `@Qualifier`, `simple-crm` will not start next lesson. Do one of these before you close:
> - Keep `@Primary` on `CustomerServiceImpl`, **or**
> - Delete `CustomerServiceWithLoggingImpl` now that you have seen how it works