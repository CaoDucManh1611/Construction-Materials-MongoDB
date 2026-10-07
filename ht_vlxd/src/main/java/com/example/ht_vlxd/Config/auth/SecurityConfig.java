package com.example.ht_vlxd.Config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(com.example.ht_vlxd.Repository.auth.NguoiDungRepository users) {
        return username -> {
            var account = users.findByUsername(username);
            if (account == null || account.getRole() == null) throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
            return org.springframework.security.core.userdetails.User.withUsername(username).password(account.getPasswordHash())
                .roles(account.getRole().getName()).disabled(!"HOAT_DONG".equals(account.getTrangThai())).build();
        };
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, com.example.ht_vlxd.Repository.auth.NguoiDungRepository users) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/san_pham/**", "/login", "/register", "/auth", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error", "/api/csrf", "/api/khach_hang/login", "/api/khach_hang/register", "/api/khach_hang/products", "/dung_chung/calculator", "/api/vat-lieu/tinh-toan", "/api/vat-lieu/danh-sach-san-pham").permitAll()
                .requestMatchers("/api/admin/**", "/quan_tri_vien/**").hasRole("QUAN_TRI_VIEN")
                .requestMatchers("/api/management/**", "/ban_quan_ly/**").hasRole("BAN_QUAN_LY")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/accounting/reports/revenue", "/api/accounting/reports/cashflow").hasAnyRole("NV_KE_TOAN", "BAN_QUAN_LY")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sales/returns").hasAnyRole("NV_KINH_DOANH", "BAN_QUAN_LY")
                .requestMatchers("/api/sales/**", "/kinh_doanh/**", "/api/vat-lieu/dat-hang-nhap").hasRole("NV_KINH_DOANH")
                .requestMatchers("/api/accounting/**", "/ke_toan/**").hasRole("NV_KE_TOAN")
                .requestMatchers("/api/warehouse/**", "/quan_ly_kho/**").hasRole("NV_KHO")
                .requestMatchers("/api/khach_hang/profile/**", "/api/khach_hang/change-password", "/dung_chung/**").authenticated()
                .requestMatchers("/api/khach_hang/**", "/khach_hang/**").hasRole("KHACH_HANG")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler()))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((req, res, ex) -> res.sendError(401))
                .accessDeniedHandler((req, res, ex) -> res.sendError(403)))
            .logout(logout -> logout.logoutUrl("/api/logout").logoutSuccessHandler((req, res, auth) -> res.setStatus(204)))
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            );
        http.addFilterBefore(new AccountSessionFilter(users), org.springframework.security.web.access.intercept.AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
