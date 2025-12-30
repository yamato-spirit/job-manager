package com.example.job_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class JobController {

    @Autowired
    private JobRepository repository;

    @Autowired
    private SiteUserRepository userRepository;

    // 共通処理: ログイン中のユーザー情報をDBから取得
    private SiteUser getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername());
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "sort", defaultValue = "manual") String sort,
                        @AuthenticationPrincipal UserDetails userDetails) {

        SiteUser user = getCurrentUser(userDetails);
        List<JobApplication> jobList;

        if ("asc".equals(sort)) {
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.ASC, "deadline"));
        } else if ("desc".equals(sort)) {
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.DESC, "deadline"));
        } else {
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.ASC, "sortOrder"));
        }

        model.addAttribute("jobs", jobList);
        model.addAttribute("currentSort", sort);
        return "index";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") String id, Model model,
                         @RequestParam(name = "sort", defaultValue = "manual") String sort,
                         @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job == null || !job.user.id.equals(user.id)) {
            return "redirect:/";
        }

        model.addAttribute("job", job);
        model.addAttribute("currentSort", sort);
        return "detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model,
                       @RequestParam(name = "sort", defaultValue = "manual") String sort,
                       @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job == null || !job.user.id.equals(user.id)) {
            return "redirect:/";
        }

        model.addAttribute("job", job);
        model.addAttribute("currentSort", sort);
        return "edit";
    }

    @PostMapping("/update")
    public String update(
            @RequestParam("id") String id,
            @RequestParam("companyName") String companyName,
            @RequestParam("status") String status,
            @RequestParam("deadline") String deadline,
            @RequestParam("motivation") String motivation,
            @RequestParam("selfPromotion") String selfPromotion,
            @RequestParam("memo") String memo,
            @RequestParam(name = "sort", defaultValue = "manual") String sort,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SiteUser user = getCurrentUser(userDetails);
        JobApplication original = repository.findById(id).orElse(null);

        if (original == null || !original.user.id.equals(user.id)) {
            return "redirect:/";
        }

        JobApplication job = new JobApplication(companyName, status, deadline, motivation, selfPromotion, memo);
        job.id = id;
        job.sortOrder = original.sortOrder;
        job.user = user;

        repository.save(job);
        return "redirect:/detail/" + id + "?sort=" + sort;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") String id,
                         @RequestParam(name = "sort", defaultValue = "manual") String sort,
                         @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job != null && job.user.id.equals(user.id)) {
            repository.deleteById(id);
        }

        return "redirect:/?sort=" + sort;
    }

    @GetMapping("/add")
    public String add(Model model, @RequestParam(name = "sort", defaultValue = "manual") String sort) {
        model.addAttribute("currentSort", sort);
        return "add";
    }

    @PostMapping("/add")
    public String create(
            @RequestParam("companyName") String companyName,
            @RequestParam("status") String status,
            @RequestParam("deadline") String deadline,
            @RequestParam("motivation") String motivation,
            @RequestParam("selfPromotion") String selfPromotion,
            @RequestParam("memo") String memo,
            @RequestParam(name = "sort", defaultValue = "manual") String sort,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SiteUser user = getCurrentUser(userDetails);
        JobApplication newJob = new JobApplication(companyName, status, deadline, motivation, selfPromotion, memo);
        newJob.user = user;

        repository.save(newJob);
        return "redirect:/?sort=" + sort;
    }

    @PostMapping("/api/reorder")
    @ResponseBody
    public void reorder(@RequestBody List<String> idList, @AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = getCurrentUser(userDetails);
        for (int i = 0; i < idList.size(); i++) {
            String id = idList.get(i);
            JobApplication job = repository.findById(id).orElse(null);
            if (job != null && job.user.id.equals(user.id)) {
                job.sortOrder = i;
                repository.save(job);
            }
        }
    }

    // Supabaseへのアクセスを追加
    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        // データベースに軽いクエリ(件数カウント)を投げることで、Supabaseへの接続を維持する
        repository.count();
        return "pong";
    }
}