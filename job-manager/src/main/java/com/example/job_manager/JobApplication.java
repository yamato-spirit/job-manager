package com.example.job_manager;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class JobApplication {
    @Id
    public String id;
    public String companyName;
    public String status;
    public String deadline;
    public String motivation;
    public String selfPromotion;
    public String memo;
    public Integer sortOrder;

    // このデータはどのユーザーのものか？を紐付ける
    @ManyToOne
    @JoinColumn(name = "user_id")
    public SiteUser user;

    public JobApplication() {

    }
    public JobApplication(String companyName, String status, String deadline, String motivation, String selfPromotion, String memo) {
        this.id = UUID.randomUUID().toString();
        this.companyName = companyName; this.status = status; this.deadline = deadline; this.motivation = motivation; this.selfPromotion = selfPromotion; this.memo = memo;
    }
}