package com.polypadel.admin.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polypadel.admin.dto.AdminCreateUserRequest;
import com.polypadel.admin.dto.AdminCreateUserResponse;
import com.polypadel.admin.dto.AdminResetPasswordResponse;
import com.polypadel.admin.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private com.polypadel.security.JwtService jwtService;
    @MockBean
    private com.polypadel.auth.repository.JSONTokenRepository jsonTokenRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void create_ok() throws Exception {
        AdminCreateUserRequest req = new AdminCreateUserRequest("john.doe@example.com", "password");
        var resp = new AdminCreateUserResponse(UUID.randomUUID(), "john.doe@example.com", "John", "Doe");
        when(adminUserService.create(req)).thenReturn(resp);

        mvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk());
        verify(adminUserService).create(req);
    }

    @Test
    public void reset_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var resp = new AdminResetPasswordResponse("pwd");
        when(adminUserService.resetPassword(id)).thenReturn(resp);

        mvc.perform(post("/admin/users/" + id + "/reset-password"))
            .andExpect(status().isOk());
        verify(adminUserService).resetPassword(id);
    }
}
