package ord.techzonefun;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Cho phép frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "Authorization"));
        configuration.setAllowCredentials(true); // Cho phép gửi cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/"); // Đảm bảo cookie có path đúng
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // Thêm cấu hình COR

                //.csrf(csrf -> csrf.disable()) // Tắt CSRF nếu không cần

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Cho phép frontend đọc CSRF token
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/auth/**")) // Bỏ qua CSRF cho API login/logout
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/auth/csrf").permitAll() // Cho phép truy cập API auth
                        .requestMatchers("/api/testsuites").hasAnyRole("TESTDEV", "CUSTOMER") // Quy tắc cụ thể trước
                        .requestMatchers("/api/testsuites/**").hasRole("TESTDEV") // Quy tắc bao quát sau
                        .requestMatchers("/api/tests/**").hasAnyRole("TESTDEV", "CUSTOMER")
                        //.requestMatchers("/api/auth/login/**").hasRole("CUSTOMER") // CUSTOMER mới có quyền
                        .requestMatchers("/api/customers/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/auth/customers/**").hasRole("TESTDEV")
                        //.requestMatchers("/api/customers/**").permitAll() // Tạm thời cho phép
                        .anyRequest().authenticated() // Các request khác cần phải đăng nhập
                )
                .cors(cors -> {}) // Cần cho frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Dùng session-based auth
                        //.sessionFixation().none() // Không thay đổi session khi đăng nhập
                        //.sessionFixation().newSession()
                        .sessionFixation().migrateSession() // Giữ session cũ, chỉ cập nhật
                )
                .formLogin(login -> login.disable()) // Tắt form login mặc định
                .httpBasic(basic -> basic.disable()) // Tắt Basic Auth
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // API để logout
                        //.invalidateHttpSession(false) // Không xóa session mặc định
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN") // 🚀 Chỉ xóa cookie nếu cần
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String role = authentication != null ? authentication.getAuthorities().toString() : "";
                            if (role.contains("ROLE_TESTDEV")) {
                                request.getSession().invalidate(); // 🚀 Chỉ TESTDEV logout toàn bộ
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\": \"Đăng xuất thành công\"}");
                            response.getWriter().flush();
                        })
                );

        return http.build();
    }

}
