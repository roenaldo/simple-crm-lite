package sg.edu.ntu.simple_crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.edu.ntu.simple_crm.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Derived query — Spring generates the SQL from the method name
    List<Customer> findByFirstName(String firstName);

    List<Customer> findByFirstNameContaining(String firstName);

    // JPQL — uses the entity name (Customer) and field name (jobTitle)
    @Query("SELECT c FROM Customer c WHERE c.jobTitle = :jobTitle")
    List<Customer> findByJobTitleJPQL(@Param("jobTitle") String jobTitle);

    @Query("SELECT c FROM Customer c WHERE c.lastName = :lastName")
    List<Customer> findByLastNameJPQL(@Param("lastName") String lastName);

    // Native SQL — uses the table name (customers) and column name (job_title)
    @Query(value = "SELECT * FROM customers WHERE job_title = :jobTitle", nativeQuery = true)
    List<Customer> findByJobTitleNative(@Param("jobTitle") String jobTitle);
}
