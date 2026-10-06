package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Собственная реализация UserDetailsService")
@DataJpaTest
@Import(UserDetailsServiceImpl.class)
class UserDetailsServiceImplTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @DisplayName("должен загружать пользователя по имени")
    @Test
    void shouldLoadUserByUsername() {
        var userDetails = userDetailsService.loadUserByUsername("user");

        assertThat(userDetails.getUsername()).isEqualTo("user");
        assertThat(userDetails.getPassword()).isEqualTo("user");
    }

    @DisplayName("должен выдавать исключение для несуществующего пользователя")
    @Test
    void shouldThrowExceptionForMissingUser() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

}
