package com.example.job_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignUpController {

    @Autowired
    private SiteUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 登録画面を表示する
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    // ログイン画面を表示する
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 登録ボタンが押されたときの処理
    @PostMapping("/signup")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("email") String email) {

        // 既に同じ名前のユーザーがいないかチェック
        if (userRepository.findByUsername(username) != null) {
            return "redirect:/signup?error";
        }

        SiteUser newUser = new SiteUser();
        newUser.username = username;
        newUser.email = email;

        // パスワードは必ず暗号化して保存する
        newUser.password = passwordEncoder.encode(password);

        userRepository.save(newUser);

        // 登録完了したらログイン画面へ
        return "redirect:/login";
    }
}