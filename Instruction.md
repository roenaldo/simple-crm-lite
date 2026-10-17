create a **new** Spring Boot project titled `simple-crm`, using Spring Initializr exactly as you did earlier for `spring-demo`.

> **⚠️ Important — this project carries forward.** `simple-crm` is the base project we will build on for the rest of the Spring Boot lessons in this module. Every subsequent lesson starts from it.
>
> **If you do not finish this in class, it becomes homework.** You must have `simple-crm` created and running with the `/customer` endpoint working **before the next lesson**, otherwise you will not be able to follow along.

Set it up with the same choices as before: Maven project, Java, latest stable Spring Boot version, `groupId` `sg.edu.ntu`, `artifactId` `simple-crm`, `jar` packaging, Java 21. When prompted for dependencies, select **Spring Web** and **Spring Boot DevTools**, exactly as you did for `spring-demo`.

Add a Customer POJO class with the following properties:
```java
private String id;
private String firstName;
private String lastName;
private String email;
private String contactNo;
private String jobTitle;
private int yearOfBirth;
```

Remember to add getters and setters — Jackson needs the getters to produce the JSON.

Use the `@Component` annotation to tell Spring Boot that this class is a component that can be injected into other classes.

Create a controller file `CustomerController.java`. Inject the customer bean into the controller using the `@Autowired` annotation.

Create a new endpoint `/customer` that returns a Customer object with some preset values.

Test it at `localhost:8080/customer`. You should see the Customer returned as JSON.