# 就活状況管理アプリ「Job Manager」

## 📖 概要
就職活動における企業ごとの選考状況、次回期限、志望動機などを一元管理できるWebアプリケーションです。
外出先や移動中でもスマホから素早く確認・編集できるよう、レスポンシブデザインおよびPWA（Progressive Web Apps）に対応しています。

## 🛠 使用技術

### バックエンド
* **Java 21**
* **Spring Boot 4.0.1**
    * Spring Data JPA (データベース操作)
    * Spring Security (ユーザー認証・認可)
    * Spring Web (MVC)
* **Thymeleaf** (テンプレートエンジン)

### フロントエンド
* **HTML5 / CSS3** (Flexboxによるレスポンシブ対応)
* **JavaScript (Vanilla JS)**
* **SortableJS** (ドラッグ&ドロップによる並び替え機能)

### インフラ・データベース
* **Render** (Webサーバー / PaaS)
* **Supabase** (PostgreSQL / DBaaS)
* **Docker** (デプロイ環境)

---

## 💡 こだわり・工夫した点

### 1. 無料クラウド環境におけるデータベース接続の安定化
PaaS（Render）とDBaaS（Supabase）の無料プランを組み合わせて運用していますが、開発中に「接続プールの枯渇」**や**「トランザクションエラー」による500エラーが多発しました。
これに対し、以下のチューニングを行うことで安定稼働を実現しました。

* **コネクションプールの最適化:** `HikariCP`の設定で `maximum-pool-size` を適切に制限し、同時アクセス時のDBパンクを回避。
* **アイドル接続の維持:** `minimum-idle=1` を設定し、コールドスタート時のレイテンシを排除。
* **トランザクションモード対策:** SupabaseのConnection Pooler (Port 6543) 使用時に発生する `prepared statement` エラーに対し、JDBC接続URLに `prepareThreshold=0` を付与して解決。
* **自動スリープ対策:** 定期的なPing監視に加え、アプリ側でも軽いクエリを発行するエンドポイントを作成し、DBの停止を防ぐ仕組みを導入。

### 2. スマホでのユーザビリティ向上 (PWA対応)
就活は移動中の確認が多いため、**「ネイティブアプリのような使い心地」**を目指しました。

* **PWA (Progressive Web Apps) 実装:** `manifest.json` とService Workerの設定を行い、ホーム画面に追加してアプリとして起動可能にしました。
* **iOS対応:** iOS独自のアイコン指定（`apple-touch-icon`）やメタタグを追加し、iPhoneでも崩れない表示を実現。
* **誤操作防止:** スマホ特有の「タップ連打」によるデータの二重登録を防ぐため、JavaScriptを用いて送信ボタンを即座に無効化（Disabled）する制御を実装。視覚的にも「処理中」であることを明示し、UXとデータ整合性を向上させました。
* **UI/UX:**
    * ドラッグ&ドロップでの直感的な優先順位変更。
    * 指で押しやすいボタンサイズや配置（更新ボタンの右下/右上配置など）の調整。
    * SVGを用いた環境依存しないアイコン表示。

### 3. マルチユーザー機能とセキュリティ
* Spring Securityを用いたログイン認証機能を実装。
* `BCrypt` によるパスワードのハッシュ化保存。
* 他人のデータが見えないよう、DBクエリレベルで `user_id` によるフィルタリングを徹底（`findByUser` メソッドの実装）。
* **アカウント削除機能:** 退会時にそのユーザーに関連する全ての企業データを物理削除し、データ残留リスクを排除（JPAの `CascadeType.ALL` を活用）。

---

## 🗂 機能一覧
* **ユーザー認証機能:** 新規登録、ログイン、ログアウト、アカウント削除（退会）
* **CRUD機能:** 企業情報の登録、参照、編集、削除
* **並び替え機能:** ドラッグ&ドロップによるカスタム並び替え、期限順ソート
* **自動整形機能:** 日付入力時のスラッシュ自動挿入
* **PWA機能:** ホーム画面への追加、オフラインキャッシュ（一部）

## 📂 データベース設計

**Users テーブル (site_user)**
| Column | Type | Description |
| --- | --- | --- |
| id | BIGINT | PK, Auto Increment |
| username | VARCHAR | ユーザー名 (Unique) |
| password | VARCHAR | ハッシュ化パスワード |
| email | VARCHAR | メールアドレス |

**Jobs テーブル (job_application)**
| Column | Type | Description |
| --- | --- | --- |
| id | VARCHAR | PK (UUID) |
| company_name | VARCHAR | 企業名 |
| status | VARCHAR | 選考状況 |
| deadline | VARCHAR | 期限 |
| sort_order | INTEGER | 並び順 |
| user_id | BIGINT | FK (Userテーブル) |
| ... | ... | (その他詳細情報) |

---

## 🚀 今後の展望
* **リマインド機能:** 期限が近づいた企業をLINE Notify等で通知する機能。
* **分析機能:** 選考通過率などをグラフで可視化するダッシュボードの実装。