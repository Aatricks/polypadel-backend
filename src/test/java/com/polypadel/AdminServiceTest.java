package com.polypadel;

import com.polypadel.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Test
    void createAccountPlayerNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> adminService.createAccount(99999L, "JOUEUR"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void resetPasswordUserNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> adminService.resetPassword(99999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
