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

    // メイン画面表示
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {

        SiteUser user = getCurrentUser(userDetails);

        // URLパラメータではなく、DBに保存されたユーザー設定からソート順を取得
        // 初回などでnullの場合は "manual" をデフォルトにする
        String sort = (user.sortPreference != null) ? user.sortPreference : "manual";

        List<JobApplication> jobList;

        if ("asc".equals(sort)) {
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.ASC, "deadline"));
        } else if ("desc".equals(sort)) {
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.DESC, "deadline"));
        } else {
            // manualの場合は sortOrder で並び替える
            jobList = repository.findByUser(user, Sort.by(Sort.Direction.ASC, "sortOrder"));
        }

        model.addAttribute("jobs", jobList);
        // ビュー側でボタンの表示切替に使うため、現在のソート順を渡す
        model.addAttribute("currentSort", sort);
        return "index";
    }

    // 追加: ソート順ボタンが押されたときにDB設定を更新する処理
    @GetMapping("/sort")
    public String updateSort(@RequestParam("type") String type, @AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = getCurrentUser(userDetails);
        if (user != null) {
            // ユーザーの並び順設定を更新して保存
            user.sortPreference = type;
            userRepository.save(user);
        }
        // トップページにリダイレクト（設定はDBにあるのでパラメータ不要）
        return "redirect:/";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") String id, Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job == null || !job.user.id.equals(user.id)) {
            return "redirect:/";
        }

        model.addAttribute("job", job);
        // sortパラメータの受け渡しは不要になったため削除
        return "detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") String id, Model model,
                       @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job == null || !job.user.id.equals(user.id)) {
            return "redirect:/";
        }

        model.addAttribute("job", job);
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
        // DBの設定が効くのでパラメータ無しでリダイレクト
        return "redirect:/detail/" + id;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") String id,
                         @AuthenticationPrincipal UserDetails userDetails) {

        JobApplication job = repository.findById(id).orElse(null);
        SiteUser user = getCurrentUser(userDetails);

        if (job != null && job.user.id.equals(user.id)) {
            repository.deleteById(id);
        }

        return "redirect:/";
    }

    @GetMapping("/add")
    public String add() {
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
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SiteUser user = getCurrentUser(userDetails);
        JobApplication newJob = new JobApplication(companyName, status, deadline, motivation, selfPromotion, memo);
        newJob.user = user;

        // 新規追加時の並び順を設定（現在の件数 = 末尾に追加）
        long count = repository.countByUser(user);
        newJob.sortOrder = (int) count;

        repository.save(newJob);
        return "redirect:/";
    }

    // 並び替え（ドラッグ＆ドロップ）時の処理
    @PostMapping("/api/reorder")
    @ResponseBody
    public void reorder(@RequestBody List<String> idList, @AuthenticationPrincipal UserDetails userDetails) {
        SiteUser user = getCurrentUser(userDetails);

        // 1. 各企業の並び順(sortOrder)を更新
        for (int i = 0; i < idList.size(); i++) {
            String id = idList.get(i);
            JobApplication job = repository.findById(id).orElse(null);
            if (job != null && job.user.id.equals(user.id)) {
                job.sortOrder = i;
                repository.save(job);
            }
        }

        // 2. 重要: 手動で動かしたので、ユーザーの設定を強制的に「manual」で上書き保存する
        if (user != null && !"manual".equals(user.sortPreference)) {
            user.sortPreference = "manual";
            userRepository.save(user);
        }
    }

    @GetMapping("/ping")
    @ResponseBody
    public String ping() {
        repository.count();
        return "pong";
    }
}