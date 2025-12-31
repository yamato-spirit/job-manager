package com.example.job_manager;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobApplication, String> {

    // 特定のユーザーのデータだけを探して、指定順に並べて返すメソッド
    List<JobApplication> findByUser(SiteUser user, Sort sort);

    // ユーザーごとの登録件数をカウントするメソッド（新規追加時の順序決定に使用）
    long countByUser(SiteUser user);
}