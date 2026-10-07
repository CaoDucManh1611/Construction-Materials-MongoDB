package com.example.ht_vlxd.Config.auth;
import com.example.ht_vlxd.Repository.auth.NguoiDungRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AccountSessionFilter extends OncePerRequestFilter {
    private final NguoiDungRepository users;
    public AccountSessionFilter(NguoiDungRepository users) { this.users = users; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            var user = users.findByUsername(auth.getName());
            if (user == null || !"HOAT_DONG".equals(user.getTrangThai()) || user.getRole() == null) {
                SecurityContextHolder.clearContext();
                if (req.getSession(false) != null) req.getSession(false).invalidate();
                res.sendError(401); return;
            }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), null, AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().getName())));
        }
        chain.doFilter(req, res);
    }
}
