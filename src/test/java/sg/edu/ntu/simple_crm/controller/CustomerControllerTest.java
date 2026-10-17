package sg.edu.ntu.simple_crm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import sg.edu.ntu.simple_crm.model.Customer;
import sg.edu.ntu.simple_crm.repository.CustomerRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CustomerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CustomerRepository customerRepository;

  private Customer customer;

  @BeforeEach
  void setUp() {
    customer = new Customer();
    customer.setFirstName("Security");
    customer.setLastName("Test");
    customer.setEmail("security@example.com");
    customer.setContactNo("81234567");
    customer.setJobTitle("Tester");
    customer.setYearOfBirth(1990);
    customer = customerRepository.save(customer);
  }

  @Test
  void allAuthenticatedUsersCanReadCustomers() throws Exception {
    mockMvc.perform(get("/customers"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/customers/{id}", customer.getId()))
        .andExpect(status().isUnauthorized());

    for (String[] credentials : credentials()) {
      mockMvc.perform(get("/customers").with(httpBasic(credentials[0], credentials[1])))
          .andExpect(status().isOk());
      mockMvc.perform(get("/customers/{id}", customer.getId())
          .with(httpBasic(credentials[0], credentials[1])))
          .andExpect(status().isOk());
    }
  }

  @Test
  void onlyAdminCanCreateCustomers() throws Exception {
    String requestBody = objectMapper.writeValueAsString(customer);

    mockMvc.perform(post("/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(post("/customers")
        .with(httpBasic("user", "password"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/customers")
        .with(httpBasic("manager", "manager123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/customers")
        .with(httpBasic("admin", "admin123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isCreated());
  }

  @Test
  void managerAndAdminCanUpdateCustomers() throws Exception {
    String requestBody = objectMapper.writeValueAsString(customer);

    mockMvc.perform(put("/customers/{id}", customer.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(put("/customers/{id}", customer.getId())
        .with(httpBasic("user", "password"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isForbidden());
    mockMvc.perform(put("/customers/{id}", customer.getId())
        .with(httpBasic("manager", "manager123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk());
    mockMvc.perform(put("/customers/{id}", customer.getId())
        .with(httpBasic("admin", "admin123"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk());
  }

  @Test
  void onlyAdminCanDeleteCustomers() throws Exception {
    mockMvc.perform(delete("/customers/{id}", customer.getId()))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(delete("/customers/{id}", customer.getId())
        .with(httpBasic("user", "password")))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/customers/{id}", customer.getId())
        .with(httpBasic("manager", "manager123")))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/customers/{id}", customer.getId())
        .with(httpBasic("admin", "admin123")))
        .andExpect(status().isOk());
  }

  private String[][] credentials() {
    return new String[][] {
        { "user", "password" },
        { "manager", "manager123" },
        { "admin", "admin123" }
    };
  }
}