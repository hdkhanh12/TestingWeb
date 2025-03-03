// CustomerService.java
package ord.techzonefun.Services;

import ord.techzonefun.Entities.Customer;
import ord.techzonefun.Entities.CustomerInfoDTO;
import ord.techzonefun.Entities.User;
import ord.techzonefun.Repositories.CustomerRepository;
import ord.techzonefun.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Customer createCustomer(String id, String name, String userId, String phoneNumber) {
        log.info("Creating customer with ID: {}, Name: {}, UserId: {}, PhoneNumber: {}", id, name, userId, phoneNumber);
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setUserId(userId);
        customer.setPhoneNumber(phoneNumber);
        customer.setTestAttempts(10); // Mặc định 10 lần
        return customerRepository.save(customer);
    }

    public Customer getCustomerByUserId(String userId) {
        log.info("Retrieving customer for userId: {}", userId);
        return customerRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.warn("Customer không tồn tại, tạo mới cho userId: {}", userId);
                    Customer newCustomer = new Customer();
                    newCustomer.setId(userId);
                    newCustomer.setUserId(userId);
                    newCustomer.setName("Người dùng mới");
                    return customerRepository.save(newCustomer);
                });
    }


    // Lấy thông tin Customer bằng ID
    public Customer getCustomerById(String id) {
        log.info("Retrieving customer with ID: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
    }

    // Cập nhật thông tin Customer
    public Customer updateCustomer(String id, Customer updatedCustomer) {
        log.info("Updating customer with ID: {}", id);
        return customerRepository.findById(id).map(customer -> {
            customer.setName(updatedCustomer.getName());
            return customerRepository.save(customer);
        }).orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
    }

    public void deleteCustomer(String customerId) {
        log.info("Deleting customer with ID: {}", customerId);

        // Kiểm tra Customer tồn tại
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + customerId));

        // Xóa User liên kết nếu có userId
        if (customer.getUserId() != null) {
            if (userRepository.existsById(customer.getUserId())) {
                userRepository.deleteById(customer.getUserId());
                log.info("Deleted linked user with ID: {}", customer.getUserId());
            } else {
                log.warn("User with ID: {} not found for customer: {}", customer.getUserId(), customerId);
            }
        }

        // Xóa Customer
        customerRepository.deleteById(customerId);
        log.info("Successfully deleted customer with ID: {}", customerId);
    }

    public List<CustomerInfoDTO> getAllCustomersWithUserInfo() {
        log.info("Fetching all customers with user information");

        // Lấy tất cả Customer
        Iterable<Customer> customers = customerRepository.findAll();
        List<Customer> customerList = StreamSupport.stream(customers.spliterator(), false)
                .collect(Collectors.toList());

        // Ánh xạ thông tin từ User, bỏ qua nếu userId null
        List<CustomerInfoDTO> customerInfos = customerList.stream()
                .filter(customer -> customer.getUserId() != null) // Lọc bỏ customer không có userId
                .map(customer -> {
                    User user = userRepository.findById(customer.getUserId()).orElse(null);
                    if (user == null) {
                        log.warn("User not found for customer ID: {}", customer.getId());
                        return new CustomerInfoDTO(customer.getId(), "N/A", "N/A", customer.getPhoneNumber());
                    }
                    return new CustomerInfoDTO(
                            customer.getId(),
                            user.getUsername(),
                            user.getPassword(),
                            user.getPhoneNumber()
                    );
                })
                .collect(Collectors.toList());

        log.info("Returning {} customers with user info", customerInfos.size());
        return customerInfos;
    }

    public String resetPassword(String customerId) {
        log.info("Resetting password for customer with ID: {}", customerId);

        // Tìm Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + customerId));

        // Tìm User liên kết
        User user = userRepository.findById(customer.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + customer.getUserId()));

        // Tạo mật khẩu tạm (ví dụ: chuỗi ngẫu nhiên)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8); // 8 ký tự ngẫu nhiên
        log.info("Generated temporary password for user {}: {}", user.getUsername(), tempPassword);

        // Cập nhật mật khẩu mới (mã hóa)
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        log.info("Updated password for user ID: {}", user.getId());

        return tempPassword; // Trả về mật khẩu tạm để gửi cho khách hàng
    }

    public void rechargeTestAttempts(String customerId, int amount) {
        log.info("Recharging test attempts for customer: {}, amount: {}", customerId, amount);
        Customer customer = getCustomerById(customerId);
        customer.setTestAttempts(customer.getTestAttempts() + amount);
        customerRepository.save(customer);
        log.info("Recharged test attempts. New total: {}", customer.getTestAttempts());
    }
}

