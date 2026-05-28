package com.brenluz.fairshare.security;

import com.brenluz.fairshare.domain.user.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userService.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
