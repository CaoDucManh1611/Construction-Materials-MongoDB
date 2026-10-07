package com.example.ht_vlxd.Controller.common;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.OptimisticLockingFailureException;

@RestController
class CsrfController {
    @GetMapping("/api/csrf") public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }
}
@RestControllerAdvice
public class ApiSupport {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalid(IllegalArgumentException ex) { return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage())); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> forbidden(AccessDeniedException ex) { return ResponseEntity.status(403).body(Map.of("message", ex.getMessage())); }
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<?> conflict(OptimisticLockingFailureException ex) { return ResponseEntity.status(409).body(Map.of("message", "Chứng từ vừa được thay đổi. Hãy tải lại trước khi thao tác.")); }
    @ExceptionHandler({org.springframework.dao.DuplicateKeyException.class, org.springframework.data.mongodb.MongoTransactionException.class})
    ResponseEntity<?> concurrent(Exception ex) { return ResponseEntity.status(409).body(Map.of("message", "Giao dịch trùng hoặc chứng từ đang được xử lý. Hãy tải lại; dùng cùng mã giao dịch nếu cần thử lại.")); }
}
