package ord.techzonefun.Services;

import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer createCustomer(String name) {
        log.info("Creating customer with name: {}", name); //  Log thông tin đầu vào

        Customer customer = new Customer();
        customer.setName(name);
        String customerId = UUID.randomUUID().toString(); // Tạo ID
        customer.setId(customerId);
        try {
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer created successfully with ID: {}", savedCustomer.getId()); //  Log ID của customer mới
            return savedCustomer;
        } catch (Exception e) {
            log.error("Error creating customer with name: {}", name, e); //  Log lỗi, bao gồm cả name và stack trace
            throw e; // Re-throw exception để controller xử lý (trả về 500)
        }
    }

    public Customer getCustomer(String id) {
        log.info("Retrieving customer with ID: {}", id); //  Log ID
        try {
            return customerRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Customer not found with ID: {}", id); //  Log warning (không tìm thấy)
                        return new NoSuchElementException("Customer not found with id: " + id);
                    });
        } catch (NoSuchElementException e){
            log.warn("Customer not found with ID: {}", id);
            throw e; // Re-throw
        }
        catch (Exception e)
        {
            log.error("Error retrieving customer with ID: " + id, e); // Log lỗi và stack trace
            throw e;
        }

    }
}