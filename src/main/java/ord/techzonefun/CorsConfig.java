package ord.techzonefun;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Cho phép tất cả các endpoint
                .allowedOrigins("http://localhost:3000") // Cho phép frontend ở cổng 3000 truy cập
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Cho phép các phương thức
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(true); // Nếu bạn cần gửi cookie, session, ...
    }
}