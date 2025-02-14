package ord.techzonefun.Controllers;

import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestParam String name) {
        log.info("Received request to create customer with name: {}", name); //  Log đầu vào
        try {
            Customer customer = customerService.createCustomer(name);
            log.info("Customer created successfully. Customer ID: {}", customer.getId()); //  Log thành công
            return new ResponseEntity<>(customer, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating customer", e); //  Log lỗi, bao gồm cả stack trace
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        log.info("Received request to get customer with ID: {}", id); //  Log đầu vào
        try {
            Customer customer = customerService.getCustomer(id);
            log.info("Customer found. Returning customer: {}", customer); //  Log thông tin customer
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            log.warn("Customer not found with ID: {}", id); //  Log cảnh báo (không tìm thấy)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error retrieving customer with ID: " + id, e); // Log lỗi, bao gồm ID và stack trace
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}