package com.example.ht_vlxd.Service.auth;
import com.example.ht_vlxd.Model.auth.NguoiDung;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Repository.auth.NguoiDungRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final NguoiDungRepository users;
    public CurrentUser(NguoiDungRepository users) { this.users = users; }
    public NguoiDung get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()))
            throw new AccessDeniedException("Bạn cần đăng nhập.");
        var user = users.findByUsername(auth.getName());
        if (user == null || !"HOAT_DONG".equals(user.getTrangThai()))
            throw new AccessDeniedException("Tài khoản không hoạt động.");
        return user;
    }
    public String username() { return get().getUsername(); }
    public void owns(DonHang order) {
        var user = get();
        if (order == null || order.getKhachHang() == null || order.getKhachHang().getNguoiDung() == null
            || !user.getId().equals(order.getKhachHang().getNguoiDung().getId()))
            throw new AccessDeniedException("Bạn không có quyền truy cập đơn hàng này.");
    }
}
