package com.vlasovartem.pmdb.config.security;

import com.vlasovartem.pmdb.entity.User;
import com.vlasovartem.pmdb.entity.enums.UserRole;
import com.vlasovartem.pmdb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by artemvlasov on 04/12/15.
 */
@Component
public class PmdbUserDetailsService implements UserDetailsService {

    private static final int PROJECT_AUTHORITIES_COUNT = UserRole.values().length;
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginData) throws UsernameNotFoundException {
        User user = userRepository.loginUser(loginData);
        Set<GrantedAuthority> authorities = new HashSet<>(PROJECT_AUTHORITIES_COUNT);
        if(Objects.nonNull(user)) {
            if(UserRole.ADMIN.equals(user.getRole())) {
                authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.name()));
            }
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.name()));
        } else {
            throw new UsernameNotFoundException("User with login data is not exists");
        }
        return new PmdbUserDetails(user.getUsername(), user.getPassword(), authorities);
    }
}
