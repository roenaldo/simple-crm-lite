package sg.edu.ntu.simple_crm.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@JsonPropertyOrder({ "id", "firstName", "lastName", "email", "contactNo", "jobTitle", "yearOfBirth" })
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long  id;


  @NotBlank(message = "First name is mandatory")
  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Email(message = "Email should be valid")
  @Column(name = "email")
  private String email;
  
  @Pattern(regexp = "\\d{8}", message = "Contact number must be exactly 8 digits")
  @Column(name = "contact_no")
  private String contactNo;

  @Size(min = 2, max = 50, message = "Job title must be between 2 and 50 characters long")
  @Column(name = "job_title")
  private String jobTitle;

  @Column(name = "year_of_birth")
  private int yearOfBirth;

  // Keep this constructor — it has custom logic used for preloading data
  public Customer(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}