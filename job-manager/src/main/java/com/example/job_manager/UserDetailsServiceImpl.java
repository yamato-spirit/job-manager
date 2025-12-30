package com.example.job_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SiteUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. データベースからユーザーを検索する
        SiteUser siteUser = userRepository.findByUsername(username);

        if (siteUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 2. 見つかったら、Spring Security用の「User」オブジェクトに詰め替えて返す
        // (ID, パスワード, 権限リスト)
        return new User(siteUser.username, siteUser.password, Collections.emptyList());
    }
}