package com.example.job_manager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private SiteUserRepository userRepository;

    // 設定画面を表示
    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    // アカウント削除処理
    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        if (userDetails != null) {
            SiteUser user = userRepository.findByUsername(userDetails.getUsername());
            if (user != null) {
                userRepository.delete(user);
            }

            try {
                request.logout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "redirect:/login?deleted";
    }
}