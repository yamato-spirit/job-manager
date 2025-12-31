package com.example.job_manager;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class SiteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    public String username;

    public String password;

    public String email;

    // ユーザーのソート順設定をDBに保存するフィールド
    // 初期値は "manual" (自分だけの並び順)
    public String sortPreference = "manual";

    // ユーザーを削除すると、そのユーザーが書いたJobApplicationも全て自動削除されます
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<JobApplication> jobApplications;
}