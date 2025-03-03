package ord.techzonefun.Controllers;

import ord.techzonefun.Entities.Test;
import ord.techzonefun.Entities.TestSuite;
import ord.techzonefun.Services.TestService;
import ord.techzonefun.Services.TestSuiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerTestSuiteController {
    @Autowired
    private TestService testService;

    @GetMapping("/testsuites")
    public ResponseEntity<List<Test>> getTestSuitesForCustomer() {
        // Lấy danh sách Test công khai từ TestService
        List<Test> tests = testService.getPublicTest();
        return ResponseEntity.ok(tests);
    }
}