package com.example.job_manager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private SiteUserRepository userRepository;

    // 設定画面を表示
    // sortパラメータを受け取り、モデルに渡すように変更
    @GetMapping("/settings")
    public String settings(Model model, @RequestParam(name = "sort", defaultValue = "manual") String sort) {
        model.addAttribute("currentSort", sort);
        return "settings";
    }

    // アカウント削除処理
    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        if (userDetails != null) {
            // ログイン中のユーザーを探す
            SiteUser user = userRepository.findByUsername(userDetails.getUsername());
            if (user != null) {
                // ユーザーを削除（SiteUser.javaの設定により、Jobデータも一緒に消える）
                userRepository.delete(user);
            }

            // 強制ログアウトさせる
            try {
                request.logout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ログイン画面に戻る
        return "redirect:/login?deleted";
    }
}