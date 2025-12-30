package com.example.job_manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
    // ユーザー名でDBを検索するためのメソッドを定義
    SiteUser findByUsername(String username);
}