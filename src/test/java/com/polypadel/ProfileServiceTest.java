package com.polypadel;

import com.polypadel.model.User;
import com.polypadel.model.Role;
import com.polypadel.service.ProfileService;
import com.polypadel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getProfile() {
        User user = userRepository.findByEmail("admin@padel.com").orElseThrow();
        var profile = profileService.getProfile(user);
        assertNotNull(profile);
        assertEquals("admin@padel.com", profile.user().email());
        assertEquals("ADMINISTRATEUR", profile.user().role());
    }
}
