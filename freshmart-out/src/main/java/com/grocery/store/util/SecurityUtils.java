package com.grocery.store.util;

import com.grocery.store.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
