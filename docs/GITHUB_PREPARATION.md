# 2026-10-26 GitHub 發布前整理

本次只整理既有 Phase 07A／RC01。整理與建置／封裝驗證 PASS；公開發行驗收 PARTIAL，仍待獨立 Launcher 安裝測試。沒有開始生存合成或多人階段。

## 基準與保留

- 工作目錄：`D:\minecraftmod`；branch：`master`；HEAD：`54c657638dbb196e27189c44d3287f77eec48a0a`。
- 開始時有 12 個 tracked 修改、4 個 untracked 檔案，全部保留。已先記錄 121 個 tracked／可見 untracked 檔案 SHA256，完成後逐檔比較：112 個位元組完全不變、9 個僅為本輪允許的 metadata／建置／文件整理，沒有原有檔案遺失；另新增 5 檔。
- 原有 8 個 Java／語系修改檔與新增 CanvasEditorTheme.java／CanvasEditorThemeLayoutTest.java 均未變更；PHASE_07A_ACCEPTANCE.md 保留原樣（其 PARTIAL 歷史標示與最新 PROGRESS 結案狀態不一致，發布整理不改寫原驗收證據）。
- 既有 PROJECT_PLAN／PROGRESS／TEST_RESULTS／DECISIONS 僅前置本輪紀錄；RC01 指南僅修正發布 metadata 與可重現操作，保留其餘內容。
- 本輪不暫存、不 commit、不 push、不建立 Git repo／remote／release，不改 Git 身分設定，不重寫歷史。

## 已完成

1. `fabric.mod.json`：作者 diegolaumm、MIT、反映現況的介紹，移除 Fabric 範例 contact 及 another-mod；Minecraft 精確 26.3。
2. 根目錄 README／MIT LICENSE（2026 diegolaumm），標明單人創造 RC 與未完成項；LICENSE 隨 JAR 封裝。
3. `.gitignore` 排除現行與舊目錄草稿（整個目錄含備份）、遊戲資料及常見憑證檔。
4. 第三方 Wrapper 保留 Apache-2.0 標頭及 JAR 內 META-INF/LICENSE；逐字匯出其授權至 `licenses/Gradle-Wrapper-Apache-2.0.txt`，另補 THIRD_PARTY_NOTICES。下載的完整 Gradle distribution 不納入專案。
5. RC01 改為 `/drawfirework_test_item`；裸 `/give` 不含必要圖像資料，且沒有 `give` 子指令。未註冊創造物品分頁的內容不當作可重現入口。

## 發布產物與測試

可供外部安裝驗收／候選預發行的模組 JAR：`build/libs/drawfirework-1.0.0.jar`。

- 大小：**194754 bytes**；106 ZIP entries。
- SHA256：`817185DE858B31B99C1D708434364B2FA42B7279A7BC8EBE93D4480E0AE95FC9`。
- JAR 中作者、MIT、精確 Minecraft 26.3、version 1.0.0、入口、Mixin、語系、物品模型及 LICENSE 核對 PASS；逐 entry 可讀，無原始碼／JUnit test class／巢狀依賴／草稿／世界／日誌。
- 原工具鏈 clean build：exit 0（23s，7 tasks executed）。首次沙箱 daemon 連線失敗，經工具核准以相同命令沙箱外成功。
- JUnit：189 tests／29 suites，0 failures/errors/skipped；Phase02 三組純邏輯回歸 PASS。
- 本輪無遊戲、視覺、外部安裝或多人實測；既有人工紀錄不替代此外部安裝驗收。Gradle 10 棄用提示保留，不為此更新工具鏈。

## 敏感資料與第三方檢查範圍

對 Git 追蹤與未忽略文字檔掃描常見私鑰、GitHub／AWS／Slack／OpenAI token，以及引號賦值的 password／client_secret／api_key／access_token：零命中。可發布檔名未見憑證／私鑰／.env。對全部 29 個可達 commits 的增刪差異執行高辨識秘密格式 `git log --all -G`：零命中。這是啟發式檢查，不是完整秘密鑑識；未讀取／公開本機憑證、遊戲認證或被忽略的私人草稿。

既有 Git 歷史包含作者電子郵件（含非 noreply 地址）；改 fabric.mod.json 的作者不會改寫 commit 身分。若不希望公開個人郵件，請先自行決定歷史處理策略；本輪依指示保留歷史與 Git 設定。

Wrapper 是明確認出的第三方交付檔，原授權完整保留。src／tests 未發現其他授權標頭；資源只有語系及指向原版模型的 JSON，沒有複製原版材質。文件 PNG 已視覺檢查，為線條／圓形／取樣比較圖，未見帳號或遊戲畫面；未獨立確認其創作來源。第三方作者權利及所有歷史來源不能由一次靜態掃描完全證明。

## 使用者後續操作

1. 依 `RELEASE_CANDIDATE_01.md` 在獨立 Launcher 實例安裝上述精確版本與 JAR，完成繪圖、儲存、重啟載入、轉為煙火、發射與清理驗收，將真實結果補記；候選仍需作為預發行看待。
2. 檢查 `git status --short`、`git diff --check`、`git diff`，逐一閱讀新增檔案；本次差異包含原有 Phase 07A 與本輪發布整理，不能只提交 metadata 而漏掉已建入 JAR 的未追蹤程式檔。
3. 由使用者決定提交邊界與 Git 作者身分。對 tracked 檔可用 `git add -p` 逐段選取；新增檔案請具名暫存，避免 `git add -A`。審查 `git diff --cached`、`git diff --cached --check` 後才自行 commit。
4. 待使用者實際建立／指定 repository，確認 URL 可用後再填入 contact 的 sources／issues；目前 `git remote -v` 為空，沒有猜測網址。加入真實 URL 會改變 JAR metadata，應重新建置並更新雜湊。
5. 由使用者自行設定 remote、push 並建立預發行。release 附件只用正式模組 JAR；不要上傳 build 整目錄、Gradle／Minecraft／Fabric 依賴、run、世界、草稿或憑證。

本輪發布整理新增檔案：README.md、LICENSE、THIRD_PARTY_NOTICES.md、licenses/Gradle-Wrapper-Apache-2.0.txt、docs/GITHUB_PREPARATION.md。本輪改動的既有檔案：.gitignore、build.gradle、fabric.mod.json、五份專案紀錄文件與 RC01 指南；原 Phase 07A 修改另需完整核對。
