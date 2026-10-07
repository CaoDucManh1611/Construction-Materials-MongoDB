package com.example.ht_vlxd;
import com.example.ht_vlxd.Repository.auth.NguoiDungRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired NguoiDungRepository users;
    @Test void anonymousCannotReadFinancialData() throws Exception {
        mvc.perform(get("/api/accounting/summary")).andExpect(status().isUnauthorized());
    }
    @Test void managerCanReadReportInputsWithoutAccountingOrSalesMutationRights() throws Exception {
        for (String path : new String[]{"/api/accounting/reports/revenue", "/api/accounting/reports/cashflow", "/api/sales/returns"}) {
            mvc.perform(get(path).with(user("giamdoc").roles("BAN_QUAN_LY"))).andExpect(status().isOk());
            mvc.perform(get(path).with(user("khachhang01").roles("KHACH_HANG"))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/accounting/reports/save").with(user("giamdoc").roles("BAN_QUAN_LY")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/sales/returns/approve").with(user("giamdoc").roles("BAN_QUAN_LY")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }
    @Test void clientCannotInventAManagementRole() throws Exception {
        mvc.perform(get("/api/management/summary").with(user("khachhang01").roles("BAN_QUAN_LY"))).andExpect(status().isForbidden());
    }
    @Test void mutationsRequireCsrf() throws Exception {
        mvc.perform(post("/api/sales/orders/approve").with(user("nvkd01").roles("NV_KINH_DOANH"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"maDonHang\":\"invalid\"}")).andExpect(status().isForbidden());
    }
    @Test void profileUsesSessionIdentity() throws Exception {
        mvc.perform(get("/api/khach_hang/profile?username=admin").with(user("khachhang01").roles("KHACH_HANG")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("khachhang01"));
    }
    @Test void fiveWrongPasswordsLockAccount() throws Exception {
        var account = users.findByUsername("khachhang01");
        try {
            for (int i = 0; i < 5; i++) mvc.perform(post("/api/khach_hang/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"khachhang01\",\"password\":\"incorrect-password\"}")).andExpect(status().isBadRequest());
            org.junit.jupiter.api.Assertions.assertEquals("BI_KHOA", users.findByUsername("khachhang01").getTrangThai());
        } finally {
            account = users.findByUsername("khachhang01"); account.setTrangThai("HOAT_DONG"); account.setSoLanDangNhapSai(0); users.save(account);
        }
    }
}
