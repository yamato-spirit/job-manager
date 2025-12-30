package com.example.job_manager;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id; // ユーザーID（自動で連番が振られます 1, 2, 3...）

    @Column(unique = true) // 同じユーザー名は登録できないようにする
    public String username;

    public String password; // 暗号化されたパスワードが入ります

    public String email;

    // ユーザー側から「自分の応募企業リスト」を参照できるようにする設定
    @OneToMany(mappedBy = "user")
    public List<JobApplication> jobApplications;
}