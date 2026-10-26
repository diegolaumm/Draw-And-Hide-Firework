## 2026-10-26 — 已授權 RC01 GitHub 發布前整理

- 目標／範圍：沿用 Phase 07A 工作樹，僅完成作者 diegolaumm／MIT 授權、準確 metadata／README、第三方 notices、草稿排除、JAR 授權封裝與發布檢查，不開始生存或多人功能。
- 修改檔案：`.gitignore`、`build.gradle`、`src/main/resources/fabric.mod.json`、新增 `README.md`／`LICENSE`／`THIRD_PARTY_NOTICES.md`／`licenses/Gradle-Wrapper-Apache-2.0.txt`／`docs/GITHUB_PREPARATION.md`；補記 PROJECT_PLAN／PROGRESS／TEST_RESULTS／DECISIONS／API_COMPATIBILITY，最小修正既有 RC01 指引。Java／測試／語系不修改。
- 工作：先唯讀盤點與保存檔案 SHA256，再更新授權／介紹／操作／忽略規則；無 remote 證據，省略 contact；保留全部既有未提交成果。
- 測試／驗收：既有 Wrapper clean build、全部 JUnit、Phase02 三組純邏輯回歸、ZIP/JAR metadata／LICENSE／內容檢查、敏感資料與 Git 差異檢查。以上已完成；結果見 TEST_RESULTS 本輪節。
- 風險：啟發式秘密掃描不保證歷史完全無敏感資料；外部安裝驗收未完成，不將編譯成功宣稱產品 V1.0 全流程 PASS。
- 邊界：不暫存、不 commit、不 push、不建立 repo／release；完成後停止，等候使用者後續安排。

## 2026-10-25 — Phase 07A — Artist's Canvas GUI Redesign（正式結案）

Status: **Phase 07A — PASS / CLOSED**（依據使用者 6/6 人工實機驗收通過、Codex 獨立技術審查無阻斷缺陷，以及最後一項透明填色 Minor 修復與自動化測試通過正式結案）

Baseline: `54c657638dbb196e27189c44d3287f77eec48a0a`；乾淨工作樹開始。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 完成獨立限定唯讀審查（Critical 0 / Major 0 / Minor 1，Minor 已修復）。

核心成果：
1. **羊皮紙畫布與木質畫框**：中央繪圖區呈現暖米白／淡羊皮紙微格紋底色，立體淺木色畫框包覆於可互動畫布外側，完全不覆蓋任何可點擊像素、不干擾滑鼠落點座標，且羊皮紙底色純為 GUI 視覺效果，絕不寫入 FreehandCanvas ARGB 像素陣列。
2. **8 列緊湊圖示化工具列**：將右側原本 10 列充滿文字的按鈕重組為 8 列畫家工具面板（2×4 調色盤色塊、筆刷滑桿、畫筆/橡皮擦/填色純圖示、Undo/Redo 純圖示與停用暗化、柔和/銳利與預覽視圖切換、草稿庫與存稿、關閉與轉為煙火）。
3. **透明填色工具切換修復（Minor 1）**：修正 `selectFillTool()`，允許自橡皮擦切換至油漆桶時保留透明色（`color == 0`）進行大面積透明擦除，僅在明確切回畫筆時才恢復 `lastPaintColor`。
4. **多維度可用性與 Tooltip**：所有按鈕皆具備雙層亮金選中外框、Hover 亮框、停用暗化狀態以及 150ms 快速觸發的中英文 Hover Tooltip（含快捷鍵說明）。
5. **建置與驗證**：JUnit 189/189 PASS，Phase 02 三組獨立視覺回歸 PASS，`.\gradlew.bat clean build --no-daemon` exit 0 PASS，發佈候選 JAR 產出：`build/libs/drawfirework-1.0.0.jar`（193,996 bytes）。

## 2026-10-25 — Phase 06 高解析度草稿持久化（正式結案）

Status: **PASS / CLOSED**（依據 Codex 最終獨立唯讀審查與使用者 6/6 人工實機驗收通過正式批准結案）

Baseline: `a87e0aa`；乾淨工作樹開始。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 完成最終獨立限定唯讀審查（Critical 0 / Major 0 / Minor 1，文件 Minor 已修正）。

結案成果：
1. **正式結案依據**：
   - Codex 最終審查無阻斷性缺陷（Critical 0 / Major 0），文件措辭 Minor 1 已修正為忠於非原子替代移動與無斷電保證實作。
   - 使用者 6/6 人工實機驗收全數 PASS（儲存第一份草稿、重啟載入、多份草稿與另存、未儲存變更提醒、刪除二次確認、煙火輸出正常，使用者反饋「沒有感受到明顯問題」）。
2. **交付功能與資料安全保障**：
   - **256×256 原稿持久化**：完整儲存 65,536 個 ARGB 十六進位像素，關閉 GUI、離開世界及完整重啟 Minecraft 後均能無損還原繼續編輯。
   - **多草稿庫完整管理**：建立新稿、命名、列表展示（含修改時間與編輯中標記）、選擇載入、另存新檔（Save As）、重新命名（Rename）、刪除（原版確認視窗二次確認）。
   - **未儲存變更安全防護**：編輯中草稿關閉 GUI、切換草稿或新建畫布時彈窗防護，取消操作完整保留畫稿與 Dirty 狀態。
   - **阻止跨尺寸有損覆寫**：非 256×256 工作畫布無法無損呈現 256×256 草稿細節時嚴格拒絕載入並提示錯誤，絕不以有損資料覆寫原稿 UUID。
   - **備份與版本控制防護**：主檔完整校驗後才複製至備份，毀損主檔不覆蓋有效 .bak；較新 schemaVersion（> 1）嚴格禁止覆寫與強制復原。
   - **非原子替代移動與無斷電保證**：當檔案系統不支援原子移動時採用非原子替代移動；操作失敗時回報錯誤並依備份復原策略處理，不作斷電期間資料持久性保證。
   - **舊目錄平滑遷移與衝突保全**：啟動時自動遷移 `config/drawfirework/drafts/`；遇同 UUID 且內容相異時安全保留雙方檔案為 `.legacy_conflict.json`。
3. **品質不變量保留**：
   - 32×32 CanvasData 格式、ItemStack / C2S 安全驗證、76 ticks 五階段動畫、Bloom 130%、Fixed 世界朝向、Phase 05C Fullbright Exact 光照、Phase 05D Balanced + Detail-Preserving 預設 100% 保持。
4. **保留已知限制（Accepted Limits）**：
   - 草稿儲存為客戶端本機資料，不支援多人同步（多人同步屬未來 Phase 09/10 範疇）。
   - 256×256 草稿不代表世界煙火輸出已提高至 256×256（維持 32×32 CanvasData）。
   - 非原子檔案系統及斷電安全性不作絕對保證。
   - 128×128 舊尺寸編輯器可能拒絕無法無損表示的 256×256 草稿。
5. **建置與驗證結果**：
   - JUnit **184/184 PASS**（28 suites，0 failures/errors/skipped）。
   - Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
   - `.\gradlew.bat clean build --no-daemon`：BUILD SUCCESSFUL in 32s，exit 0。發佈候選 JAR：`build/libs/drawfirework-1.0.0.jar`（175,155 bytes）。
   - `git diff --check`：PASS。

## 2026-10-25 — Phase 06 最終資料完整性修復交付（Final Data Integrity Remediation）

Status: **PARTIAL**（3 Major 與 3 Minor 最終資料完整性修復全數完成，全部自動化測試與回歸檢驗 PASS，等待使用者實機驗收與 Codex 獨立唯讀複核）

Baseline: `a87e0aa`；乾淨工作樹開始。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 負責交付後的獨立限定唯讀審查。

核心目標：
修復 Codex 第二次複審確認之 3 項 Major 與 3 項 Minor 資料完整性問題，確保任何 Save、Load、Recover、切換草稿或跨尺寸操作，都不會默默破壞使用者最後一份有效的 256×256 原稿。

交付成果：
1. **阻止跨尺寸有損覆寫（Major 1）**：
   - `FreehandCanvas` 新增 `canLoadLosslessly(int[] draft256)` 檢查與嚴格拒絕機制：當任意 256×256 草稿在 $2 \times 2$ 採樣方塊內存在色彩或 Alpha 差異時，禁止載入至 128×128 工作畫布，拋出 `IllegalArgumentException`。
   - `CanvasEditorScreen` 與 `DraftManagerScreen` 於載入前校驗，若無法無損呈現則中斷載入並顯示 `editor.drawfirework.draft_lossy_load_error`，原稿 UUID 絕不被有損資料覆寫，且當前畫布、筆畫、Undo/Redo 歷史與 Dirty 狀態完好保留。
   - 保留舊 16×16 ItemStack 與 32×32 CanvasData 煙火物品輸出契約與 C2S 格式安全。
2. **備份前完整驗證主檔（Major 2）**：
   - `DraftStorage.saveDraft()` 於複製至 `.bak` 前全面調用 `readFullDraft()` 驗證 Schema、UUID 一致性、256×256 尺寸、65,536 像素長度與 ARGB Hex 編碼格式。
   - 損壞主檔（標頭有效但像素不足/超長、含非法 Hex、UUID 不相符）絕不複製至 `.bak`；若此時存在有效 `.bak`，拋出 `DraftStorageException` 安全拒絕覆寫，誓死守護最後一份有效備份。
3. **絕對保護較新版本草稿不受復原覆蓋（Major 3）**：
   - `DraftStorage.recoverFromBackup()` 嚴格拒絕以舊備份覆蓋 `schemaVersion > CURRENT_SCHEMA_VERSION` 的較新版本主檔，即使指定 `force=true` 亦拋出 `DraftVersionException`，主檔與備份位元組 100% 保持不變。
4. **路徑異常隔離與遷移衝突安全保留（Minor A & Minor B）**：
   - `ClientDraftStorage` 全面捕捉初始化與檔案系統異常，GUI 安全顯示錯誤通知不崩潰遊戲、不破壞畫布。
   - 舊目錄遷移遇到相同 UUID 且內容不同時，新舊兩檔皆不覆蓋亦不刪除，舊檔安全保留為 `<uuid>.legacy_conflict.json` 並顯示衝突提示。
5. **非原子寫入與容錯替代策略**：
   - 當檔案系統不支援原子移動時，系統採用非原子替代移動；操作失敗時回報錯誤，並依既有備份／復原策略處理。本機制不保證斷電期間的資料持久性。
6. **自動化測試與回歸**：
   - JUnit **184/184 PASS**（28 suites，新增 8 項嚴格測試，0 failures/errors/skipped）。
   - Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
   - `.\gradlew.bat clean build --no-daemon`：BUILD SUCCESSFUL in 28s，exit 0。JAR：`build/libs/drawfirework-1.0.0.jar`（175,155 bytes）。
   - `git diff --check`：PASS。

## 2026-10-25 — Phase 06 資料安全修復交付（Data Safety Remediation）

Status: **PARTIAL**（5 Major 與 3 Minor 安全問題修復全數完成，全部自動化測試與回歸檢驗 PASS，等待使用者實機驗收與 Codex 獨立唯讀複核）

Baseline: `a87e0aa`；乾淨工作樹開始。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 負責交付後的獨立限定唯讀審查。

核心目標：
修復 Codex 獨立審查回報之 5 項 Major 與 3 項 Minor 安全問題，完善高解析度草稿儲存之生命週期完成流程、失敗隔離、舊尺寸畫布相容性、備份與復原資料安全、符號連結與暫存檔防護、窄視窗排版適應性及精確錯誤回饋。

交付成果：
1. **儲存完成狀態機（Major 1 & Major 2）**：
   - 建立 `saveAndThen(callingScreen, onSuccess, onCancel)`，只有在確認資料成功寫入磁碟並驗證無誤後才執行關閉或切換。
   - 未命名草稿彈窗取消或寫入失敗時，完整保留畫布、Undo/Redo 歷史與 Dirty 狀態，絕不破壞使用者創作。
   - DraftManagerScreen 之 Load Draft、New Draft、Save Draft 全面委派 `saveAndThen`，杜絕吞噬異常或雙軌狀態機。
2. **舊尺寸工作畫布相容性（Major 3）**：
   - `FreehandCanvas` 新增 `toDraft256Pixels()`：16×16 合法舊物品產生之 128×128 工作畫布採確定性 $2 \times 2$ 鄰近方塊複製映射至 256×256 草稿；標準 256×256 畫布直接複製。
   - 擴充 `loadDraft(256x256)`：採確定性取樣降採樣回 128×128，實現 100% 雙向無損往返。
   - 煙火物品輸出 `snapshot()` 維持物品原尺寸（16×16 或 32×32），伺服器 `EditSession.save` 契約完全不受影響。
3. **備份、覆寫與復原安全（Major 4）**：
   - 覆寫前校驗版本：若目標檔 `schemaVersion > 1`，拋出 `DraftVersionException` 拒絕覆寫。
   - 損壞檔案不得覆蓋有效備份：僅在主檔為有效結構時才複製至 `.bak`，主檔毀損時誓死保留最後一份完好備份。
   - 安全復原檢查：主檔本身有效時拒絕無條件復原以防覆蓋新稿（需 `force` 明確指定）。
4. **符號連結防護與隨機暫存檔（Major 5）**：
   - 採用 `Files.createTempFile` 隨機唯一暫存檔並在 `finally` 中安全刪除。
   - `resolvePath` 強制 symlink 檢查與 realPath 邊界檢查，嚴防符號連結跳脫草稿目錄。
5. **窄視窗自適應排版（Minor 1）**：
   - 獨立排版計算器 `DraftManagerLayout`，保證 $[140 \times 140]$ 至 4K 螢幕清單與按鈕完全不重疊、不越界。
6. **儲存路徑規範化與舊目錄遷移（Minor 2）**：
   - 規範目錄為 `<gameDirectory>/drawfirework/drafts/`，並加入 `config/drawfirework/drafts/` 自動遷移。
7. **精確錯誤追蹤（Minor 3）**：
   - 新增 `lastListError` 與 UI 錯誤提示，澄清 `flush()` 與 `ATOMIC_MOVE` 行為。
8. **自動化測試與回歸**：
   - JUnit **176/176 PASS**（28 suites，新增 11 項測試，0 failures/errors/skipped）。
   - Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
   - `.\gradlew.bat clean build --no-daemon`：BUILD SUCCESSFUL in 26s，exit 0。JAR：`build/libs/drawfirework-1.0.0.jar`（172,572 bytes）。
   - `git diff --check`：PASS。

## 2026-10-25 — Phase 06 高解析度草稿持久化範圍與交付

Status: **PARTIAL**（實作與自動化測試已全數完成，等待使用者實機驗收與 Codex 獨立唯讀複核）

Baseline: `a87e0aa`；乾淨工作樹開始。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 負責交付後的獨立限定唯讀審查。

核心目標：
實作完整的 256×256 自由繪圖草稿持久化系統，讓玩家能永久保存高解析度原始畫稿、關閉 GUI 或重新啟動遊戲後繼續編輯、管理多份作品（建立、命名、列出、選擇、重新載入、刪除），並提供原子寫入、自動備份復原、格式版本校驗與未儲存變更安全防護。

交付成果：
1. **純 Java 草稿儲存核心（`com.drawfirework.draft`）**：
   - `DraftMetadata.java`：不可變中繼資料，含 UUID、名稱（長度上限 64，檔名路徑字符防護）、時間戳記、256×256 尺寸與 `schemaVersion = 1`。
   - `Draft.java`：封裝完整 256×256 原始 ARGB 像素（65,536 像素，保留直覺 Alpha），支援無損資料模型與純 Java 操作。
   - `DraftStorage.java`：檔案儲存服務，實作同目錄暫存檔寫入（`.json.tmp`）、原子替換（`ATOMIC_MOVE`）、覆蓋前自動備份（`.json.bak`）、備份復原（`recoverFromBackup`）、單檔 2MB 上限、草稿 200 份上限、目錄穿越防護與串流快讀中繼資料。
   - `DraftStorageException.java`、`DraftCorruptedException.java`、`DraftVersionException.java`：毀損檔案安全容錯不刪檔，較新未支援版本安全拒絕。
2. **編輯器畫布擴充（`FreehandCanvas.java`）**：
   - `loadDraft(int[] newPixels)`：載入草稿像素，安全重設 Undo/Redo 歷史堆疊與覆蓋率遮罩，防止工作階段歷史洩漏。
   - `resetBlank()`：重設為全透明空白，安全清空歷史堆疊與作用中筆畫。
3. **客戶端服務與管理介面（`com.drawfirework.client`）**：
   - `ClientDraftStorage.java`：綁定 Minecraft 實體 `gameDirectory` 之 `drawfirework/drafts/`。
   - `DraftListWidget.java`：雙行排版滾動清單，顯示名稱、最後修改時間、尺寸與 `[目前編輯中]` 標籤。
   - `DraftManagerScreen.java`：草稿庫管理主畫面，提供 Load、New、Save、Save As、Rename、Delete（原版 `ConfirmScreen` 二次確認）及 Back。
   - `DraftNamePromptScreen.java`：彈出式名稱輸入對話框。
   - `UnsavedChangesScreen.java`：未儲存修改安全彈窗（提供儲存草稿、捨棄變更與取消三選項）。
4. **主編輯器整合（`CanvasEditorScreen.java`）**：
   - 追蹤 `currentDraftId`、`currentDraftName` 與 `savedRevision`，即時判斷 `isDraftDirty`。
   - 頂部標題即時顯示當前草稿名稱與未儲存狀態標記（`*`）。
   - 右側按鈕自適應 10 列安全緊湊排版，窄視窗（240px）下絕不越出底緣或侵入畫布。
   - 清楚區分 `Save Draft`（本機 256×256 原稿）與 `To Firework`（輸出 32×32 `CanvasData` 至物品）。
   - 支援 `Ctrl+S` 快捷鍵即時快速存檔。
5. **雙語系文字補充（`en_us.json` & `zh_tw.json`）**：
   - 補充所有草稿管理、按鈕、對話框、確認訊息與錯誤回饋之繁中與英文翻譯。
6. **自動化測試與回歸**：
   - JUnit **165/165 PASS**（25 suites，新增 17 項純 Java 與邏輯測試，0 failures/errors/skipped）。
   - Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
   - `.\gradlew.bat clean build --no-daemon`：BUILD SUCCESSFUL in 24s，exit 0。
   - `git diff --check`：PASS。
7. **驗收文件**：
   - 撰寫 `docs/PHASE_06_ACCEPTANCE.md`，提供 8 步驟最短人工實機驗收指引。

## 2026-10-24 — Phase 05D 畫質檔位與採樣最佳化範圍與交付

Status: **PASS / CLOSED**（依使用者正式批准結案）

Baseline: `ae1e9fb`；乾淨工作樹為原實作開始前紀錄。
Developer Agent: Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 完成最終獨立限定唯讀複核（Critical 0 / Major 0 / Minor 0，前次 4 項 Minor 全數 RESOLVED）。

核心目標：
在保留現有 32×32 CanvasData、C2S 安全驗證、76 ticks 動畫、Bloom 130%、Fixed/Dynamic 世界朝向與 Phase 05C fullbright_exact 獨立光照之前提下，提高煙火圖案解析度與輪廓細節，建立可測試、可控制之畫質檔位與 A/B 採樣演算法。

交付成果：
1. **三檔畫質檔位（`ParticleQuality.java`）**：
   - Balanced（**正式生產預設**）：1100 顆核心粒子，上限 366 樣本（$366 \times 3 = 1098 \le 1100$）。經使用者實機驗證（以數字 6 為例）確認視覺效果優於 High，正式採用為預設。
   - Standard：800 顆核心粒子，上限 266 樣本（$266 \times 3 = 798 \le 800$）。輕量基準檔位。
   - High：1400 顆核心粒子，上限 466 樣本（$466 \times 3 = 1398 \le 1400$）。測試上限檔位。
2. **採樣策略 A/B（`DetailPreservingSampler.java` & `SamplingStrategy.java`）**：
   - Existing（策略 A）：現有基於佔用率之四叉剖分採樣，作為不變對照組。
   - Detail-Preserving（策略 B，正式預設）：外輪廓、封閉迴圈邊界、細斜線、端點與高對比色彩邊界優先保留，實心色塊內部平滑剖分填補；採動態目標分配（70% 輪廓／30% 內部，動態回補）。
3. **指令控制**：
   - `/drawfirework quality <standard|balanced|high|reset>` 與查詢 `/drawfirework quality`（reset 恢復 Balanced 預設）。
   - `/drawfirework sampling <existing|detail_preserving|reset>` 與查詢 `/drawfirework sampling`（reset 恢復 Detail-Preserving 預設）。
4. **預覽聯動與快取失效（`CanvasPreview.java`）**：
   - Quality 與 Sampling 變更時自動失效快取，Dust 預覽即時更新點位與統計字串（如「1098 粒子（366 樣本 × 3）」），Save 送出之 32×32 CanvasData 100% 相同。
5. **Codex 4 項 Minor 修正與測試健全度**：
   - 明確傳入 266 語意尊重呼叫端、70/30 動態目標分配契約、移除絕對過度宣稱（改列品質目標）、修正實機驗收指南（說明 Session 限制）。
   - JUnit **148/148 PASS**（22 suites，0 failures/errors/skipped）。
   - Phase 02 三組獨立視覺回歸各 exit 0 PASS；clean build exit 0；git diff --check exit 0。
   - 撰寫並更新 `docs/PHASE_05D_ACCEPTANCE.md` 人工驗收指南與結論。
6. **保留事實與已接受限制（Accepted Limits & Deferred Verification）**：
   - 60 FPS 為使用者特定筆電環境觀察，不是通用效能保證。
   - Detail-Preserving 並非對所有圖案都一定優於 Existing（實體粒子散布下視覺差異細微）。
   - 專業效能量測與採樣策略之進一步視覺比較記為可延後項目。
   - CanvasData 仍為 32×32；256×256 原稿永久保存未實作（屬 Phase 06 範疇）。

## 2026-10-24 — Phase 05C 最終整合範圍與驗收政策（歷史）

Status: **PASS / CLOSED**。本次clean build成功，124/124 JUnit與Phase02三組回歸PASS，git diff --check PASS；無已知Critical／Major。正式預設fullbright_exact已套用，reset一致。三類延伸未測情境按授權記Accepted Risk / Deferred Verification，不聲稱通過；完整結果見PROGRESS／TEST_RESULTS。

使用者明確授權Codex進行本次最小收尾：變更正式預設／reset／null fallback至fullbright_exact，同步必要JUnit與文件；保留所有既存工作，不commit/push。保留fullbright、baseline、exact_only。實作檔限HeartPrototype.java、DustLightingMode.java、DustLightingModeTest.java；不改Mixin或渲染架構。

本次驗收為clean build、全部JUnit、Phase02三組回歸及git diff --check通過，無已知Critical／Major。既有獨立靜態審查、兩項文件Minor修正、實際Mixin注入位元碼及使用者視覺觀察均作證據。原版Dust完整實機隔離、所有模式／日夜圖案矩陣及部分重進／重啟生命週期記Accepted Risk / Deferred Verification，不能冒稱PASS，但不再因此單獨維持PARTIAL。最終結果見PROGRESS及TEST_RESULTS最新段落。

此授權不包含05D或06，完成即停止。下文是歷史範圍與驗收政策，不再構成重複人工驗收門檻。

## 2026-10-24 — Phase 05C — Firework Visibility & Lighting（歷史）

Status: **Phase 05C PARTIAL — 文件 Minor 已修正；獨立靜態審查完成，Runtime 場景矩陣與隔離／清理驗收待完成**

Baseline: `94458d5` (code base `83062a9`)；乾淨工作樹為原實作開始前紀錄；目前有既有未提交 05C 變更，全部保留。
Developer Agent: Gemini / Antigravity (sole developer agent by explicit user authorization); Codex maintains independent read-only review role.

Goal:
Address the long-standing observation that Dust fireworks are dim during daytime and nearly invisible at night without external light sources (such as torches), while preserving Dust's soft natural texture, Bloom 130% physical sample distribution, and 5-phase 76-tick animation lifecycle.

Deliverables Summary:
1. **Source Code Diagnosis (05C.1)**:
   - Verified in Minecraft 26.3 bytecode: `DustParticle` inherits `Particle.getLightCoords(float)`, querying world block/sky light via `LightCoordsUtil.getLightCoords()`. Without torches at night, light coords are near 0, rendering particles near pitch-black.
   - Verified: Firework spark particles (`SimpleAnimatedParticle`) unconditionally return `15728880` (`0x00F000F0 = LightCoordsUtil.FULL_BRIGHT`), enabling self-illumination.
   - Verified: `DustParticle` randomly darkens RGB by $[0.48..1.00]$ (average 0.72) with independent channel shifts.
2. **Minimal Viable Fullbright Duck Interface & Mixin (05C.2)**:
   - `GlowingParticleAccess.java`: duck interface for per-particle fullbright tagging.
   - `ParticleMixin.java`: client-only mixin targeting `Particle.getLightCoords()`, returning `15728880` only when flagged. Default is false; untagged path retains original method. Runtime vanilla-particle isolation and third-party compatibility are separate checks, not guaranteed by static review.
3. **Four Orthogonal Comparison Modes (`DustLightingMode.java`)**:
   - `BASELINE` (world light + vanilla randomized color, **strict production default**).
   - `FULLBRIGHT` (15728880 light coords + vanilla randomized color).
   - `FULLBRIGHT_EXACT` (15728880 light coords + 100% exact source RGB via `sqp.setColor()`).
   - `EXACT_ONLY` (world light + exact source RGB).
4. **Command & Item Playback Integration**:
   - `/drawfirework lighting <baseline|fullbright|fullbright_exact|exact_only|reset>` and query `/drawfirework lighting`.
   - Active mode applies consistently to `/drawfirework heart`, `/drawfirework canvas ...`, and item right-click launches.
5. **Automated Test Suite (`DustLightingModeTest.java`)**:
   - 8 new unit tests cover enum flags/name, parsing aliases, packed constant, RGB extraction, mock boolean access, repeated identical sampling, and HeartPrototype mode getter/setter/null fallback. They do not exercise Mixin injection, actual Brigadier aliases, particle playback, or world lifecycle. Total JUnit tests: **123/123 PASS**.

Validation:
- Gradle clean build: `.\gradlew.bat clean build --no-daemon` succeeded in 26s (7 actionable tasks: 7 executed, exit 0).
- JUnit: **123/123 PASS** (19 suites, 0 failures, 0 errors, 0 skipped).
- Phase 02 three regressions: PASS (TimelineCheck, DustStyleCheck, BloomCheck).
- `git diff --check`: PASS (no whitespace errors).
- JAR produced: `build/libs/drawfirework-1.0.0.jar`.
- Manual acceptance guide: `docs/PHASE_05C_ACCEPTANCE.md`.

Next Steps:
Codex independent static review completed with no blocking code finding; two documentation Minors remediated. User daytime/hand-drawn feedback is preserved (night good, daytime slightly fluorescent; no color adjustment requested). Complete the four-mode/two-pattern/day-night matrix, vanilla Dust isolation and world re-entry checks in PHASE_05C_ACCEPTANCE.md. fullbright_exact is a production-default candidate; baseline remains the actual default. No Phase 05D or Phase 06 implementation authorized by this remediation.

## 2026-10-24 — Phase 05B.2 Formal Closure (Historical)

Status: **Phase 05B.2 — PASS / CLOSED**

Commit: `83062a995934ecd139bbb36e21eda7130a0a8534`; clean worktree verified.
Codex Final Review: **PASS** (Critical: 0, Major: 0, Minor: 0; all previous findings RESOLVED).
User Formal Approval: Phase 05B.2 officially approved and closed.

Deliverables Summary:
1. Smart Resampling (`FreehandResampler.java`): Soft & Crisp modes (8-way connected component dilation contour compensation without hard binarization).
2. Canvas & Dust Previews (`CanvasPreview.java` & ViewMode): 32x32 transparency checkerboard, Firework Bloom 130 3-particle sample simulation with boundary margin. Shared cached CanvasData.
3. Undo / Redo History (`FreehandCanvas.java`): strictly bounded to 16 steps (<= 4 MiB history pixel RAM, ~4.56 MiB with fixed raster arrays, ~4.81 MiB active peak). Active stroke finished before stack query, preventing NoSuchElementException.
4. Non-recursive Flood Fill: BFS iteration with 65,536 queue capacity, no StackOverflowError.
5. GUI Adaptive Safe Layout (`CanvasEditorLayout.java`): decoupled pure Java layout module. Dynamic board sizing based on text height, progressive stats label degradation (Full -> Short -> Compact -> Minimal -> Number -> Safe Omission `null`), removed 40px floor, mathematically impossible to encroach right buttons across window widths [50..800].

Validation:
- Gradle clean build: BUILD SUCCESSFUL in 27s (exit 0).
- JUnit: 115/115 PASS (18 suites).
- Phase 02 three regressions: PASS (TimelineCheck, DustStyleCheck, BloomCheck).
- User manual acceptance: 8/8 PASS (initial 5/5, remediation 3/3, improved in-game playback effect confirmed).
- `git diff --check`: PASS.

Reserved Tracking Items (Non-blocking):
- Extreme narrow window in-game retest not performed (guaranteed by pure Java tests across [50..800] widths).
- Dust night brightness deferred to post-GUI investigation.
- Soft/Crisp subtle perceptual difference marked for visual tuning.
- Session-only high-resolution draft storage deferred to Phase 06 (Draft Persistence).
- Dust preview 2D schematic disclaimer retained.

Next Phase: Phase 06 — Draft Persistence (`config/drawfirework/drafts/` JSON schema, atomic write, safe paths), awaiting explicit user authorization.

## 2026-10-24 — Phase 05B.2 Final Statistics Text Boundary Fix (Historical)

Baseline: `d6941bfd5c8d6ac0d107be8bc39d45f18dd14224`; clean worktree verified. Codex final review reported Critical 0, Major 0. User completed initial manual acceptance 5/5 PASS and remediation manual acceptance 3/3 PASS (confirming improved in-game playback effect).
Temporary Agent Exception: By explicit user authorization, Gemini/Antigravity is the sole code developer agent for Phase 05B.2 final statistics text boundary fix; Codex is paused for writing.
Goal: Complete closure of remaining 2 Minor findings:
- Minor 1 (Button encroachment on narrow windows): removed artificial 40px floor in `CanvasEditorLayout.maxTextWidth()`, strictly returning `Math.max(0, pStart - 4 - MARGIN_LEFT)`. Guaranteed non-negative, preventing text from spilling into right buttons on narrow screens ($w \le 140$).
- Minor 2 (Unify selection path & safe omission degradation): eliminated duplicate selection logic in `CanvasEditorScreen`; screen directly calls tested `resolveStatsText()` / `chooseStatsText()`. Implemented full degradation progression (Full $\to$ Short $\to$ Compact $\to$ Minimal $\to$ Number $\to$ Safe Omission `null` / `visible = false`). When space cannot accommodate any text, safely omits rendering.
- Test coverage: `DustShapeFidelityTest.java` directly covers `chooseStatsText` progression and omission, non-floor `maxTextWidth` numerical values, and window-wide button clearance invariant across widths [50..800]. Total JUnit tests expanded to 115/115 PASS.
Current status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**.
Clean build: `.\gradlew.bat clean build --no-daemon` succeeded (BUILD SUCCESSFUL in 27s, exit 0). All 115 JUnit tests PASS. Phase 02 three regressions PASS. `git diff --check` PASS.

## 2026-10-24 — Phase 05B.2 Final Minor Closure & Test Architecture Hardening (Historical)

Baseline: `2b5c5c8af9f37ee1a7be7439e9ec05f291001d19`; clean worktree verified. Codex final review reported Critical 0, Major 0. User completed initial manual acceptance 5/5 PASS and remediation manual acceptance 3/3 PASS (confirming improved in-game playback effect).
Temporary Agent Exception: By explicit user authorization, Gemini/Antigravity is the sole code developer agent for Phase 05B.2 final minor closure; Codex is paused for writing.
Goal: Complete closure of remaining 3 Minor findings:
- Minor 1 (Small-window preview text boundary & dynamic height): extracted pure Java `CanvasEditorLayout.java`, dynamically computes board height based on measured text wrapping lines (`textHeightNeeded`), clamps particle stats within left preview work area with short/compact fallbacks, caps disclaimer lines within window bounds.
- Minor 2 (Test effectiveness & regression sensitivity): `DustShapeFidelityTest.java` directly calls `CanvasEditorLayout` product methods (no duplicated math in tests), verifies 180..800 width, 160..600 height, and 1..6 disclaimer lines; includes negative regression tests showing old bug failures.
- Minor 3 (Documentation accuracy & state recording): corrected history memory to 4 MiB (4.56 MiB with fixed raster arrays, 4.81 MiB with active stroke); updated actual XML JUnit count to 112/112 PASS; recorded all completed user acceptance.
Current status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**.
Clean build: `.\gradlew.bat clean build --no-daemon` succeeded (BUILD SUCCESSFUL in 28s, exit 0). All 112 JUnit tests PASS. Phase 02 three regressions PASS. `git diff --check` PASS.

## 2026-10-24 — Phase 05B.2 Remediation & Final Dust Preview UI / Shape Fidelity Fix (Historical)

Baseline: `c8b1ac7dd8647f46502c628b1a5e85535cc25dd6`; clean worktree verified. User confirmed initial manual acceptance 5/5 PASS. Codex independent review reported 2 Major and 2 Minor findings.
Temporary Agent Exception: By explicit user authorization, Gemini/Antigravity is the sole code developer agent for Phase 05B.2 remediation and final preview refinement; Codex is paused for writing.
Goal: Complete remediation of review findings and user UI/shape feedback:
- Major 1: Undo/Redo active-stroke lifecycle ordering (end active stroke before stack check; prevent NoSuchElementException; allow immediate undo of in-progress stroke).
- Constant: `MAX_HISTORY` reduced from 32 to 16 by user authorization (bounded history <= 8 MB).
- Major 2: Dust preview reflects production 3-particle sample micro-offsets (`DustStyle.BLOOM_130`), rectangular aspect ratio scale fix (`Math.max(w,h) / CANVAS_SPAN`), total particle count display, 2D layout disclaimer.
- Minor 1: Crisp mode 8-way dilation on 64-bit mask for true connected component check; rejects disconnected scatter noise spanning rows/cols.
- Final Preview UI & Shape Fidelity:
  - Text layout: board height budget (`height - 88`) reserves bottom space; particle stats and 2D disclaimer vertically stacked (`textY2 = textY1 + 13`) with bounded wrap width (`width - panelWidth - 20`), guaranteed never to collide or spill into right buttons.
  - Number "6" & shape fidelity: mathematical proof of geometric center, vertical polarity, and aspect ratio; fixed perimeter particle truncation by adding margin so 100% of particles are rendered without downward boundary loss; documented 16x16 vs 32x32 micro-offset ratio (45.2% vs 90.5%).
- Minor 2: Update all project documentation.
Current status: **Phase 05B.2 PARTIAL — Remediation complete, awaiting manual retest and independent re-review**.
Clean build: `.\gradlew.bat clean build --no-daemon` succeeded (BUILD SUCCESSFUL in 30s, exit 0). All 109 JUnit tests PASS. Phase 02 three regressions PASS. `git diff --check` PASS.

## 2026-10-24 — Phase 05B.2 initial scope (Historical)

Baseline: `862497d`; clean worktree verified.
Temporary Agent Exception: By explicit user authorization, Gemini/Antigravity is the sole code developer agent for Phase 05B.2; Codex is paused.
Goal: GUI refinement: smart conversion algorithm (Soft vs Crisp), in-GUI output preview (32x32 canvas and Firework dust simulation), bounded Undo/Redo, and non-recursive Flood Fill.
Scope/files:
- `FreehandResampler.java`: Mode.SOFT, Mode.CRISP, bounded contour compensation.
- `FreehandCanvas.java`: bounded undo/redo history (max 32), non-recursive flood fill, snapshot with Mode.
- `CanvasPreview.java`: cached output for preview and save payload consistency.
- `FreehandInput.java`: tool management (Brush vs Fill).
- `CanvasEditorScreen.java`: Undo, Redo, Fill, Mode, View preview toggles, keyboard shortcuts (Ctrl+Z/Y, B/E/F/M/P), consistent save.
- `SmartResamplerTest.java`, `FreehandCanvasTest.java`, `FreehandInputTest.java`: test suites.
- `en_us.json`: translation keys.
- Project docs: PROJECT_PLAN, PROGRESS, DECISIONS, TEST_RESULTS, PHASE_05B2_ACCEPTANCE.
Current status: **Phase 05B.2 PARTIAL — Waiting for manual acceptance and independent review**.
Clean build: `.\gradlew.bat clean build --no-daemon` succeeded (BUILD SUCCESSFUL in 27s). All 95 JUnit tests PASS. Phase 02 three regressions PASS. `git diff --check` PASS.

## 2026-10-24 — Phase 05B.1 authorized scope (Historical)

Baseline: 0a00372187cf172a0ab39c175acc869b7ce97e13; clean worktree verified. Codex is the sole writer.
Goal: session-only high-resolution freehand drawing with no visible grid; preserve existing item dimensions and exact unedited pixels.
Scope/files: independent FreehandCanvas and FreehandResampler; product-method JUnit; CanvasEditorScreen/input and translations; minimum new-canvas default alignment in server/session; phase records and acceptance guide.
Order: bounded raster, continuous circular brush/interpolation, premultiplied-alpha area conversion, pure Java tests, GUI integration, full validation.
Validation: clean build, all JUnit, three Phase02 checks, git diff --check; quantitative thin/diagonal/arc/color-boundary quality checks; manual GUI/playback acceptance remains separate.
Risks: low-resolution detail loss, Dust alpha limitations, GPU/runtime behavior unverified until manual acceptance. No persistent high-resolution original, format/network expansion, toolchain changes, later phases, commit or push.
Current status: **Phase 05B.1 — PASS / CLOSED**. User reports running `.\gradlew.bat clean build --no-daemon` in ordinary PowerShell at `D:\minecraftmod` and receiving `BUILD SUCCESSFUL`; exit code, duration, and task count were not supplied. User confirmed core manual acceptance, JUnit 84/84, three Phase02 regressions, and Antigravity review 0 Critical / 0 Major / 0 Minor. Thin-line fading/blur, low Dust night visibility, and session-only high-resolution originals remain follow-up items. See PHASE_05B1_ACCEPTANCE.md and TEST_RESULTS.md. Phase 05B.2 not started in this closure update.
## Phase 05A — In-Game Canvas Editor MVP：PASS / CLOSED（2026-10-24）

正式結案依據：功能提交`5114606`；clean build、JUnit 70/70及Phase 02三組回歸均由使用者確認PASS；使用者已實機確認繪圖、伺服器儲存、右鍵播放自製煙火、clear及基本取消／重進／快捷鍵／舊功能操作。獨立唯讀審查Critical／Major／Minor皆為0，並確認GUI輸入修復、受限C2S與伺服器授權、token／防重放／ItemStack狀態保護及不同尺寸畫布程式碼與單元測試PASS。本次僅更新結案文件，沒有重跑建置或測試。

接受的未實機驗證風險：編輯中途由外部強制切Survival或更換手持物、開啟超過12,000 ticks後儲存，以及特殊矩形畫布的遊戲內呈現與儲存；這些由程式碼、單元測試及審查支持，不記為實機PASS。偶發JVM啟動閃退根因未確認，沒有證據證明與Phase 05A直接相關或無關，列非阻塞追蹤且不宣稱已修復。Phase 05B與06未開始。

## 2026-10-24 — Phase 05A Drawing Interaction Bugfix 授權（歷史狀態；已由正式結案取代）

- 狀態：Phase 05A PARTIAL — Waiting for manual retest。使用者確認GUI能開啟，但點擊繪圖沒有可見變化，修復前BLOCKED；其他人工驗收未測。
- 目標／範圍：只修復畫布左鍵／拖曳／放開、透明擦除及相關回歸，不變更網路、component、04C.2播放、模型格式或工具鏈，不開始05B。
- 預期檔案／工作：CanvasEditorScreen把正式筆畫事件委派新CanvasEditorInput，用本機26.3 MOUSE_BUTTON_LEFT常數；新CanvasEditorInputTest以真實MouseButtonEvent直接測產品分支；更新PROJECT_PLAN／PROGRESS／TEST_RESULTS／DECISIONS／API_COMPATIBILITY及PHASE_05A_ACCEPTANCE。其他未提交05A檔案保留。
- 根因／證據：Screen原三處硬編碼button==0；本機javap證實LEFT=1，原生AbstractWidget同樣驗1。直接委派的處理器保留舊predicate時新4tests全FAIL，改常數後70tests及clean build／Phase02三回歸PASS。
- 驗收：遊戲中紅藍黃點擊立即變色、快速拖曳連續、橡皮擦透明；工具區不畫、邊界安全、Esc／取消不保存。精確步驟見PHASE_05A_ACCEPTANCE最新補充。
- 風險／未完成：Screen真實派送、GUI Scale／按鈕與Esc需人工重測；單測不冒充視覺驗收。修復後仍PARTIAL，明確不commit／push，停止寫入。

## 2026-10-23 — Phase 05A 授權與計畫（歷史紀錄）

- 基準：D:\minecraftmod，HEAD 527db03f671ba1e7b878009202733c302fa11158，開始 git status --short 無輸出；Codex 唯一寫入，Antigravity 唯讀。已核對六份必讀文件與本機最新 reviews/phase-04a-review-fix-01.md；04C.1／04C.2 正式 PASS 依既有結案及使用者確認，沒有本機 04C 審查全文，不捏造。
- **Phase 04D: SKIPPED / NOT REQUIRED**。04C.1 已交付 component／ItemStack，04C.2 已交付伺服器授權播放，沒有剩餘必要的獨立 04D 交付；依使用者授權直接進入 05A，不執行額外 04D 開發。
- 目標／範圍：原生 Screen、可重設 G 快捷鍵、8 色／橡皮擦／連續拖曳、取消、真正伺服器儲存到原 component；新畫布16×16，既有1..32各維度完整保留。只限單人 Creative 主手 test_firework，右鍵沿用04C.2。
- 預期檔案：新增 editor/CanvasEditorModel、EditSession；network/OpenEditorPayload、EditorCanvasPayload、ServerCanvasEditor；client/CanvasEditorClient、CanvasEditorScreen；修改 DrawFirework、fabric.mod.json、en_us／zh_tw；新增 editor／network 產品方法 JUnit，更新五份階段文件與人工驗收說明。若最小修正需要其他檔案，於交付記錄精確差異。
- 工作：本機26.3 javap核對GUI／KeyMapping／網路、server開啟工作階段並綁定真實stack、內容比較、限時／限頻／單次token、C2S重用ItemCanvasCodecs、成功後broadcastChanges、回覆真實結果；既有renderer／UseGate／動畫／預算不變。
- 測試：clean build、全部JUnit、Phase02三組回歸、git diff --check；遊戲內操作與獨立審查另行驗收，不以編譯冒充。
- 驗收：點擊與快速拖曳正確；取消不寫物品；保存／重開／右鍵／存檔重載一致；非Creative與過期／換物／原值變更安全拒絕；舊指令與播放正常。
- 風險：純JUnit無完整Screen／registry／真實網路生命週期，GUI縮放／焦點與物品同步需實測；dust既有半透明／降採樣限制保留；10分鐘編輯期限以server ticks計。無工具鏈升級、草稿、配方、05B或多人廣播。
- 結束狀態固定：Phase 05A PARTIAL — Waiting for manual acceptance and independent review。交付後停止寫入，不push，不開始05B。

## 2026-10-23 — Phase 04C.2 正式結案（最新）

Phase: 04C.2 — Server-Authorized Firework Playback
Status: PASS
Changes: 本輪僅更新 PROGRESS.md、TEST_RESULTS.md、PROJECT_PLAN.md 正式結案紀錄；不修改 Java、測試或工具鏈。
Build result: 46e37c6 對應 clean build PASS（沿用既有證據，本輪不重跑）。
Tests performed: JUnit 53/53 PASS、Phase 02 回歸 3/3 PASS；使用者確認人工驗收 7/7 PASS，Antigravity 獨立唯讀審查 0 Critical／0 Major／0 Minor。使用者已補驗 /drawfirework heart、Canvas 指令及 clear 正常，原最後一項 UNTESTED 已改為 PASS。人工及審查結果依本次使用者明確回報補登，不宣稱本輪重新執行。
Tests not performed: 本輪未重新執行建置、JUnit、遊戲或多人測試；不將單人人工通過延伸為多人接收範圍已驗證。
Known problems: 沒有本輪回報之未解決階段缺陷；多人同步仍非本階段範圍。
Git commit: 46e37c6（已核對 HEAD）；審查時工作樹 clean（使用者確認），本輪開始 git status --short 亦無輸出。本輪文件未提交，不執行 commit 或 push。
Recommended next phase: 僅待使用者另行授權；不開始 Phase 04D 或 Phase 05。
GUI handoff: 文件更新後 Codex 停止寫入，無背景修改。

五階段總長為 **76 ticks = A20 + B8 + C14 + D20 + E14**。獨立審查報告中的 68 ticks 為筆誤，不作為產品時序；本輪只澄清文件，不修改動畫參數。

以下為歷史交付／驗證紀錄；其中 PARTIAL、等待人工／獨立審查及未驗收敘述已由本節正式 PASS 取代，保留當時實際測試紀錄。

## 2026-10-23 — Phase 04C.2 授權與驗收

04C.1 已由使用者確認正式 PASS（1d4ac78、人工驗收與 Antigravity 零缺失 PASS），舊 PARTIAL 已取代。04C.2 本輪由 Codex 唯一修改，Antigravity 唯讀。

目標：Creative 物品使用由 server 驗真實 stack/component，中心 eye+look*12，本人 S2C 一次事件，以共用 activate/canvasPattern 接入既有動畫。範圍檔案與測試見 TEST_RESULTS；不改工具鏈、降採樣、相機更新公式、時序或預算。工作包含 payload 邊界、弱引用每玩家 gate、client 世界檢查／去重／斷線清理及兩組 JUnit。驗收：clean build、全測試及三組回歸；人工須驗 A–E／不消耗／資料不變／拒絕條件／世界清理／舊入口。風險為真實網路與渲染 runtime 尚未執行，故 PARTIAL。沒有 C2S 繪圖、廣播、火箭實體、GUI、配方、草稿或 04D。

## 2026-10-23 — Phase 04C.1 授權範圍與交付（歷史）

- 目標：ItemStack 攜帶單一不可變 CanvasData，component 強制 1..32 尺寸及最多 1024 像素，驗證保存／同步邊界。
- 角色／基準：Codex 唯一修改者，Antigravity 唯讀；HEAD f4785b3，工作樹起始乾淨，版本全數不變。
- 範圍／檔案：DrawFirework 初始化、CanvasSerialization 共用 boundedCodec 工廠；新增 ItemCanvasCodecs、ModDataComponents、ModItems、TestFireworkItem、TestItemCommand、ItemStackDiagnostics、ItemCanvasCodecTest、items/test_firework.json 與 en_us／zh_tw 翻譯，以及五份階段文件。
- 工作：common 先 component 再 item 註冊；Creative-only 伺服器命令取得單件 Smiley；右鍵僅伺服器驗資料及回饋，不消耗；survival 拒絕。缺 component 不補預設。物品本身不帶預設畫布，授予時才設 component。
- 驗證：clean build、47/47 JUnit、三組 Phase 02 回歸通過。真正 ItemStack JUnit 卡於 bootstrap，移除不適用測試並提供 live registry 的 /drawfirework_test_item verify，尚待人工執行；不虛報 ItemStack 往返已通過。
- 人工驗收門檻：Creative 授予／模型／右鍵、verify PASS、存檔重載後 verify PASS、無 component／survival 拒絕、滿背包不授予或覆寫；獨立審查。詳見 TEST_RESULTS 最短步驟。
- 風險：26.3 registry lifecycle／component 初始化與實際存檔重載仍待執行，已存在的 NBT tree 外層配置不由 Codec 倒推限制。故 Status PARTIAL，等待人工驗收與獨立審查。
- 與規劃差異：network header 用三個 unsigned bytes（version,width,height）取代 VarInt，最大 4099 bytes；不引入任何自訂 payload。模型直接引用本機 JAR 已有的 minecraft:item/firework_rocket，不複製材質。
- 排除：04C.2／04D、S2C 動畫事件、A–E 物品播放、GUI、草稿、配方／生存消耗、多人。Fixed／Dynamic、Bloom130、時序、三粒 Dust、預算及 downsampler 完全未修改。

## 2026-10-23 — Phase 04B 結案與 04C 唯讀架構提案（歷史；04C.1 已另獲授權）

04B 正式 PASS：功能 commit 6569b56；38/38 JUnit、三組 Phase 02 動畫回歸、clean build PASS；Antigravity 獨立唯讀審查 PASS（無 Critical／Major／Minor），使用者最新人工 5/5 PASS。人工與審查結果依本次對話補登，細項見 TEST_RESULTS；不因審查時文件仍待驗收而要求重測。本輪僅修改三份結案文件，不重跑建置、不改原始碼／測試／依賴、不寫 Git、不 push。04C 以下內容僅為研究提案，未實作；04D 不定義、不開始。

### 04C 證據與 API 相容性查核（2026-10-23）

本輪允許修改僅限結案文件，因此將 04C API 證據集中於本節；實作獲准時再將實際編譯確認的簽章補入 API_COMPATIBILITY.md。已讀 AGENTS、五份專案紀錄與現有最新審查修正紀錄 docs/reviews/phase-04a-review-fix-01.md；本機無 04B 審查全文。依賴不變：Minecraft 26.3、Java 25、Loader 0.19.5、Fabric API 0.163.0+26.3、Loom 1.17.16、Wrapper 9.6.1。

本機未取得對應 sources JAR；本次以 javap 讀取實際二進位簽章、以 javap -c -p 核對指定實作，不宣稱完整原始碼審計或編譯驗證：

- Minecraft：`.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar`。
- Fabric 網路：`gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-networking-api-v1/6.3.10+5c1fee4d5d/35aef74e899203a5c3cd90cb27eb21cf42f71d56/fabric-networking-api-v1-6.3.10+5c1fee4d5d.jar`。
- `net.minecraft.core.component.DataComponentType.<T>builder()`；Builder 的 `persistent(Codec<T>)`、`networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf,T>)`、`build()`。bytecode 確認未設定 stream codec 時會呼叫 `ByteBufCodecs.fromCodecWithRegistries(codec)`；不能把僅 persistent 視為不會網路同步。
- `BuiltInRegistries.DATA_COMPONENT_TYPE`／`ITEM`；`Registry.register(Registry<V>, Identifier, T)` 或 ResourceKey overload；`Identifier.fromNamespaceAndPath(String,String)`。不使用舊 Yarn 的 ComponentType／Identifier 舊建構子名稱猜測。
- `Item.Properties.setId(ResourceKey<Item>)`、`stacksTo(int)`；Item 建構實作會呼叫 properties.itemIdOrThrow()，必須先設定正式 registry key，再建立並註冊物品。
- `new ItemStack(ItemLike)`／`new ItemStack(ItemLike,int)`；`ItemStack.set(DataComponentType<T>,T)`；`DataComponentHolder.get(DataComponentType<? extends T>)`；`ItemStack.CODEC`、`STREAM_CODEC`、`OPTIONAL_STREAM_CODEC` 存在。勿套用舊版本 save/parse 方法範例。
- `RegistryOps.create(DynamicOps<T>, HolderLookup.Provider)`；實際 ItemStack NBT 往返測試應使用帶 registries 的 ops。
- `Item.use(Level,Player,InteractionHand): InteractionResult`；`Level.isClientSide()`；`ItemStack.consume(int,LivingEntity)`／`shrink(int)`；InteractionResult 有 SUCCESS、SUCCESS_SERVER、CONSUME、FAIL、PASS。成功回傳值、creative 消耗政策及雙手呼叫行為仍須於實作時核對 bytecode 與遊戲驗證。
- `StreamCodec.of(StreamEncoder,StreamDecoder)`；`RegistryFriendlyByteBuf(ByteBuf,RegistryAccess)`；FriendlyByteBuf 的 readVarInt/writeVarInt、readInt/writeInt 已查到。此命名體系使用 StreamCodec，不需另外引進名為 PacketCodec 的舊映射 API。
- Fabric `PayloadTypeRegistry.clientboundPlay()`／`serverboundPlay()`，其 `register(Type<T>,StreamCodec<? super B,T>)`；`ServerPlayNetworking.send(ServerPlayer,CustomPacketPayload)`；`ClientPlayNetworking.registerGlobalReceiver(Type<T>,PlayPayloadHandler<T>)`；Context.client()。不能照搬舊 playS2C/playC2S 名稱。handler 執行緒語義尚待實作時核對，不在此冒稱已證實；渲染狀態只允許於 client 主執行緒變更。

官方補充：[26.3 公告](https://fabricmc.net/2026/09/15/263.html) 可讀；[Data Component 文件](https://docs.fabricmc.net/develop/items/custom-data-components) 標示 26.2，只供概念參考，不替代本機 26.3 簽章。未改工具鏈，也未研究無關 SDL／GUI API。

### 建議最小架構與資料表示

1. 保留不可變 CanvasData 為唯一圖案值。CanvasSerialization.CODEC 已是 `Codec<CanvasData>`，型別可直接供 persistent 使用；沒有理由把 JSON 字串再包入 component 或保存第二份 pixels。實際 ItemStack／NBT 往返尚未測試，不能宣稱已可完整運作。
2. Codec 的語義驗證可重用，但 `fromJson` 的 600,000 bytes／ASCII／未知欄位／重複欄位檢查不會自動套用到 CODEC。WIRE 的 Codec.STRING 先讀字串才檢查 hex 長度；僅 flatXmap 限尺寸不能保證 hostile NBT 的配置前限制。物品 persistent adapter 要限制編碼欄位長度、尺寸及格式；NBT 讀入層仍須有外層大小限制。不要把既有草稿 codec 當成完整的封包安全邊界。
3. 建議 04C 測試物品先限定 1..32 寬高、最多 1,024 像素，完整保存 ARGB 與透明空間；超限安全拒絕，不偷偷裁切或改 downsampler。一般 CanvasData 的 256 上限不變。此為待批准的測試物品政策，尚非最終 256 草稿轉物品方案；後續大圖以明確轉換產生有界顯示格式，完整草稿留本機，需另決策／視覺驗收。
4. persistent adapter 復用版本／格式／hex 語義，但加物品限制；network 明確提供受限 `StreamCodec<RegistryFriendlyByteBuf,CanvasData>`：建議 version／width／height 用 VarInt，驗證正值、version、面積及剩餘可讀 bytes 後，才配置精確 int[]，逐像素傳 ARGB int。無獨立像素 count，避免兩份長度矛盾；最多 4,096 pixel bytes＋有界標頭。建議 component 編碼上限 4,128 bytes，事件上限 4,256 bytes，實作時以實際編碼測試確認。拒絕截斷、未知版本、過長 VarInt、超大尺寸及不足 bytes；專用事件的尾隨 bytes 亦須拒絕，component decoder 不得誤吞後續 ItemStack 欄位。
5. 單一 component `drawfirework:canvas` 只存 CanvasData，不存 JSON、DustPattern、粒子或 renderer 狀態。一次成功使用才從已驗證 canvas 轉 DustPattern；不每 tick 序列化或降採樣。不同 stack 可安全引用不可變值；不新增無界全域快取。網路 component 與一次效果事件各有其傳輸責任，不在同一物品同時保存兩份畫布。
6. common 初始化先註冊 component，再註冊 item，最後註冊共用 payload codec／伺服器開發命令；全部在 registry freeze 前、兩端一致。client entrypoint 才註冊接收器與渲染；common 不引用 HeartPrototype／Minecraft client 類別。

### 最小物品使用流程與責任邊界

- 04C.1：伺服器開發命令以有限預設名選 DevCanvases，檢查權限與 creative，建立 `new ItemStack(TEST_FIREWORK)` 並 `set(CANVAS, validatedCanvas)`。建議 stacksTo(1)，缺 component 的裸 /give 物品安全失敗，禁止偷偷補預設圖。背包滿失敗不扣除、不亂丟物品；沒有生存免費授予或製作配方。命令註冊／權限 API 尚待授權後查證，這裡不捏造簽章。
- 04C.1 只驗保存、背包同步、重載與資料讀回；使用未接線時明確回饋未啟用，不聲稱能發射。最小物品名稱、翻譯、模型資源一併提供，避免缺材質冒充完成品。
- 04C.2：使用原版 item-use C2S 互動，不新增攜帶任意畫布的 C2S 自訂請求。伺服器讀當下手持 stack，檢查物品／component／creative 權限、玩家狀態、冷卻與單一活動事件限制；以伺服器眼睛／視線計算固定世界中心。建議 creative 測試物品不消耗，survival 使用拒絕；真正生存消耗留 Phase 07，不把測試入口當正式製作。
- 伺服器對每位玩家保留最多一個有界事件槽，建議至少 10 tick 請求冷卻、重觸發明確取代前事件；事件包含 version、session 內唯一 ID、dimension、固定中心、伺服器起始 tick、CanvasData，固定樣式不重複傳。尺寸／座標須有限、世界範圍合法。只 S2C 給使用者本人，明確限定整合伺服器；不提前做觀眾廣播、多人接收範圍或完整同步系統。
- 伺服器負責接受／拒絕與事件唯一性；客戶端不得先預測播放，也不從玩家本機草稿讀取物品圖。伺服器不播放一份音效再讓客戶端播放第二份；本測試事件由 client 唯一 A–E 入口播放聲音／粒子。沒有世界破壞或新火箭 entity，這是最小測試物品，非最終權威火箭生命週期。
- client 收到有界事件後切到主執行緒，檢查 dimension／世界、事件 ID／時效，呼叫同一播放入口；最多一個效果、替換先 clear。斷線／退出／切換世界清理效果及去重狀態；建議只保留當前／最後 ID，不建立無界 set。伺服器事件槽到期／斷線清理；若 client clear 需要伺服器狀態同步，另定語義，不增加臨時共享記憶體捷徑。

### HeartPrototype 接線與預算

目前 startCanvas/start 是 private 且依賴 FabricClientCommandSource，中心由 DustPlane.start(eye,look) 算出 eye＋12×look；不能直接從 common Item 呼叫，也不能只用 client 手持 stack 啟動動畫。

建議只抽一個 client 內小型播放入口，接收已驗證 CanvasData、伺服器世界中心及必要選項／事件時間，保留原指令 adapter 的 feedback。DustPlane 加小型「明確中心」工廠而不重建整個渲染器；既有指令 eye/look 路徑保持。Fixed 仍在 B 開始由本機相機捕捉平面，Dynamic 保持診斷選項，不宣稱 Fixed 各客戶端具有同一平面朝向。

沿用 DustPattern.fromCanvas／CanvasDownsampler／ParticleBudget，每樣本三粒，最多 266 樣本＝798 核心粒子，硬上限 800；裝飾獨立最多 200。保持 Bloom130、A20/B8/C14/D20/E14、預設 100%、Fixed 與最多一效果。事件不傳逐粒子位置；未來 GUI 與網路均提供同一受限資料值，轉換只在入口一次執行。

### 建議拆分、預計檔案、驗收與風險

| 子階段 | 最小範圍與預計檔案（名稱為提案） | 自動測試與人工驗收 |
| --- | --- | --- |
| 04C.1 | 新增 common component/ModDataComponents、canvas/ItemCanvasCodecs、item/ModItems、item/TestFireworkItem、開發授予命令及對應測試；修改 DrawFirework.onInitialize 接線，新增最小 item 定義／model／翻譯資源；更新階段文件 | Codec 的 JsonOps 與 registry-aware NBT 往返、真正 ItemStack.CODEC 與 STREAM_CODEC 往返、copy/相等性與不可變性、兩張圖不互相污染、缺 component、未知版本／格式／非法 hex／超限／截斷／惡意標頭拒絕；確認 32×32 邊界與 33／256 物品拒絕。遊戲測試授予權限、背包滿、背包／箱子存取、存檔退出重進後仍同圖；未接線使用明確失敗。clean build 與既有全部回歸 |
| 04C.2 | 新增 common network/TestFireworkPayload、server/TestFireworkUseService、client/接收器或小型播放 adapter；修改 TestFireworkItem.use、common/client 初始化、HeartPrototype 與 DustPlane 的最小入口；新增相應測試及文件 | 事件 codec 大小／版本／座標／dimension／截斷驗證，cooldown／重複／雙手／缺資料／survival 拒絕，去重／時效／退出清理；伺服器事件槽有界；ItemStack 複製或原資料異常不觸發。整合伺服器實測持物使用→單次 A–E／音效、Warm/Cool 及透明區／比例、重載後再使用、clear／替換／退出重入、既有心形與朝向回歸。clean build、全套回歸；多人不在此驗收 |

測試應分純資料測試與需要 registries/bootstrap 的 Minecraft 整合測試，不能以 CanvasData JsonOps 往返冒充 ItemStack 保存測試。04C.1 授權後先查現有環境可用的 bootstrap／GameTest 支援；若普通 JUnit 無法安全初始化 registry，不強行模擬 PASS，改用最小整合測試並記錄實際命令。人工物品存檔重載仍是獨立門檻。

主要風險：26.3 item initializer／資源格式／註冊順序、RegistryOps 與 bootstrap、codec 解碼前配置限制、Component 同步與自訂 payload 不同責任、client/server 互相引用、雙端 use 重播、事件時間與世界切換。已查到簽章不等於已編譯或 Runtime-tested；04C 所有實作驗證均未執行。04C.2 必須先確認 callback 執行緒與使用回傳語義。未來 Phase 05 的任意畫布 C2S 要另做權威／資源／大小驗證；Phase 09 才擴展觀眾範圍、事件共同 tick／過期與併發，不依賴整合伺服器的共用 JVM 物件。

需使用者批准的產品／範圍決策（建議預設）：分兩次授權 04C.1 再 04C.2；測試物品先 32×32 上限、超限拒絕；creative-only 且不消耗、survival 拒絕；04C.2 納入最小 S2C 使用事件、僅自己觀看、最多一效果、10 tick 冷卻。內部註冊順序、類別名稱、受限二進位 codec 與 client 入口抽取是建議工程方案，不需逐項請示。若要求 04C 即支援 256 圖或完整生存火箭，需先另定有界顯示格式與擴充範圍，不能默認加做。

本輪完成後停止，等待使用者批准；本節取代下方 04B PARTIAL／等待驗收狀態，不改寫歷史測試。

## 2026-10-23 — 正式子階段定義與 Phase 04B 授權（歷史）

- Phase 04A：CanvasData → Dust Renderer，正式 PASS。
- Phase 04B：執行時圖案生成、預設選擇、切換與渲染整合驗證；本輪唯一開發者 Codex，Antigravity 獨立唯讀審查。
- Phase 04C：Data Component 與最小測試煙火物品，未開始、未授權本輪實作。
- Phase 04D：暫待後續規劃，不補定義。

04A 收尾依使用者本輪明確確認：Review Fix 01 已修正；Antigravity 獨立複查 PASS，1 Major／2 Minor 全部 CLOSED；33 JUnit、三組 Phase 02 回歸及使用者 5/5 遊戲回歸通過。對應 commit 1243af9、9040a35、8246713。最新獨立複查全文尚未存入本機 docs/reviews；不捏造報告內容或逐項人工觀察。下方 PARTIAL／等待回歸敘述保留為歷史，由本節取代。

### Phase 04B 最小交付

- 目標：同一 CanvasData → DustPattern → HeartPrototype 入口接收執行時生成的有限配色變體。
- 範圍／檔案：DevCanvases.java 共用 Smiley 幾何與有限 enum 配色；HeartPrototype.java 新增 smiley_warm／smiley_cool supplier；DevCanvasesTest.java；五份專案紀錄。
- 工作：保留原四預設及選項順序；每次指令執行時建立新畫布；Warm／Cool 幾何與 alpha mask 相同、ARGB 不同；不新增通用編輯器或數值參數。
- 自動驗收：確定性、配色差異、原始尺寸／語義區域／透明度、無效配色拒絕、實際 DustPattern 轉換與預算；既有 JUnit、三組回歸及 clean build。
- 人工驗收：配色可辨、A–E 可讀、Launch/Hold/Dispersion 替換與 clear、重複觸發、世界切換、Fixed／Dynamic 及 heart 回歸。JUnit 不執行 Minecraft 指令註冊或粒子引擎。
- 維持：Java 25／Minecraft 26.3／所有依賴，Bloom 130%、A20/B8/C14/D20/E14、三粒微偏移、266 樣本／798 核心粒子、預設 100% 尺寸與既有 downsampler。
- 排除：Data Component、物品、權威發射、封包、GUI、背包／草稿保存、多人與 04C/04D。
- 風險：Dust alpha 以大小近似、原版隨機亮度、細線偏移／密集混疊、Fixed 側視透視縮短；沒有 frame time／GPU 量測。
- 狀態：PARTIAL，等待人工驗收及獨立審查。詳細結果見 TEST_RESULTS 最新段落。

## 2026-10-23 — Phase 04A Review Fix 01

- 目標：修正 `1243af9` 審查所列 1 Major（相機測試有效性）與 2 Minor（狀態／決策文件）；不開始 04B。
- 範圍／檔案：HeartPrototype 接入 DustPlane；將 CanvasOptions 搬至獨立檔案；替換 CameraOrientationStabilityTest；更新 PROGRESS、DECISIONS、TEST_RESULTS、API_COMPATIBILITY 與本次修正紀錄。
- 工作：抽取實際使用的朝向、世界中心及座標計算小元件；以行為測試覆蓋 Fixed、Dynamic、玩家位置輸入、防止 D/E 重設位置；更正文檔過度宣稱。
- 測試／驗收：全套 JUnit、Fixed 每 tick 更新之 mutation probe、clean build、三組 Phase 02 source-launcher 回歸、diff check。人工驗收尚未完成，不宣稱整體 PASS。
- 風險：純 JUnit 不執行 Minecraft 指令與粒子引擎；執行期接線仍需遊戲回歸。維持所有依賴、Bloom、時序、預算與功能範圍。

## 2026-10-23 — Phase 04A Finalization（已完成收尾）

使用者正式完成 Phase 04A.2 人工測試驗收，決定收尾 Phase 04A。寫入者為 Antigravity（AGENTS.md §16），Codex 暫停。收尾完成後寫入權交回 Codex。

- 目標：將 Fixed World Plane 正式設為 Canvas 動態圖案之預設朝向；保留 Dynamic 為對照模式；保留 Phase 02 愛心原始行為；維持 Bloom 130% 與動畫預算；更新自動化測試與專案文檔。
- 交付成果：
  1. `HeartPrototype.java`：預設採用 Fixed World Plane；支援 `dynamic` 參數作為對照。
  2. `CameraOrientationStabilityTest.java`：增補預設 Fixed 驗證與位移免疫性測試（8 個單元測試全數通過）。
  3. 測試：35 個 JUnit 全數通過；三個 Phase 02 動畫回歸全數通過；`clean build` exit 0。
  4. 文檔：更新 `DECISIONS.md`（修正時序推測、記錄人工測試反饋）、`PROGRESS.md`、`TEST_RESULTS.md`、`AGENTS.md`。
- 本階段不開始 Phase 04B，不註冊 Data Component，不新增物品與 GUI。

## 2026-10-23 — Phase 04A.2 相機朝向與幾何穩定性

使用者核准 Phase 04A.2（Camera Orientation & Geometry Stability）；寫入者為 Antigravity（AGENTS.md §15），Codex 暫停。任務結束後交回預設分工。

- 目標：排查使用者回報之「移動或視角 0°→45°→0° 後有時仍呈 45° 歪斜」現象；釐清 Design A（世界固定平面）與 Design B（Formation/Hold 動態面向）之時序差異；提供無侵入式的直接對比選項。
- 範圍：排查 7 大幾何與相機問題；新增自動化單元測試 `CameraOrientationStabilityTest`；在既有指令系統擴充 `fixed` 選項供玩家驗證 Design A；記錄使用者對 Grid Plain 與 Smiley 150% 之反饋。
- 嚴格禁止修改：Bloom 130%、Hold 14 ticks、五階段時間、800 核心粒子上限、CanvasDownsampler 演算法、整體顯示尺寸預設、三粒偏移參數、Data Component、物品或 GUI。
- 交付：33 個 JUnit 全數通過；三個 Phase 02 動畫回歸 exit 0；clean build exit 0；文件更新（DECISIONS、PROGRESS、TEST_RESULTS、AGENTS）。

## 2026-10-23 — Phase 04 拆分與 Phase 04A 範圍

使用者核准 Phase 04，為節省額度拆為 04A–04D；本次只授權 04A，寫入者為 Antigravity（AGENTS.md §13），Codex 暫停。04B–04D 的內容與角色由使用者後續指定。

- 目標：CanvasData → CanvasDownsampler → Dust 動畫核心，可接收任意樣本座標與 RGB。
- 範圍：純 Java 轉接層 `DustPattern`、開發用固定畫布 `DevCanvases`、`HeartPrototype` 最小接線與開發指令、JUnit。
- 預期修改：`firework/DustPattern.java`、`firework/DevCanvases.java`（新）、`client/HeartPrototype.java`、`test/firework/DustPatternTest.java`、docs、AGENTS.md §13。
- 禁止：Data Component、物品、網路封包、GUI、草稿、04B 以後工作、改動 Dust 動畫/Bloom 130%/五階段/工具鏈。
- 驗收：heart 63 點/189 粒逐點不變；alpha 0 不產生粒子；核心粒子 ≤ 800；不同畫布產生不同樣本；clean build 與測試通過；遊戲內視覺另由使用者確認。
- 風險：Dust 無真實 alpha；低預算混疊；798 粒遊戲內效能未量測。

## 2026-10-23 — Phase 03 授權範圍與驗收

使用者接受Phase02獨立審查PASS並明確授權Phase03。起始HEAD `7477eb8`，工作樹乾淨；已讀AGENTS、docs五份紀錄與docs/reviews/phase-02-review.md。Codex為本輪寫入者，完成後凍結交付審查。

- 目標：不可變CanvasData、版本化JSON/persistent Codec、確定性有界CanvasDownsampler、獨立粒子預算與可重現測試。
- 檔案：新增canvas套件的CanvasData、CanvasSerialization、CanvasDownsampler、ParticleBudget及src/test下CanvasTest；build.gradle僅增加JUnit4.13.2測試依賴與test設定；更新五份docs。Minecraft/Fabric/Java/Wrapper版本不變。
- 工作：核對本機DFU/Gson API、驗證尺寸/陣列/版本/編碼，研究nearest-neighbour、區域平均與空間分割；測試稀疏/細線/彩色/大圖/非正方形及惡意輸入；clean build、動畫回歸、Git範圍查核。
- 驗收：標準Gradle test實際執行且全部通過，clean build成功；模型/演算法不引用client/world/render；HeartPrototype、Bloom130與五階段不變。
- 風險：有限點數不能完美重建任意圖片，棋盤格/稀有色會混疊，Dust精確alpha映射仍待整合驗證；純邏輯測試不等於遊戲畫質。
- **本次授權更正：800為核心粒子數上限，非採樣點數；3粒/點最多266組=798粒，裝飾獨立200。** 下方舊「800點」不得沿用。
- 排除：Phase04整合、Data Component註冊、StreamCodec/封包、GUI、草稿檔案管理、物品與多人。persistent Codec已完成，網路codec待受限顯示格式定義後處理。
- 結果：Phase03資料邏輯範圍PASS、待獨立審查；Phase04僅建議，未開始。

## 2026-10-23 — Phase 02系列最終凍結與交付

使用者完成02.6人工比較，正式採Bloom130%；Normal100/Bloom115保留開發比較，不再預設。只調整HeartPrototype兩處預設與本次四份紀錄，候選Git提交合併既有02–02.6完整11檔；不遺漏未追蹤樣式/時間類別與三個測試。範圍不包括03、GUI、資料、物品、多人或新增視覺。
驗證/交付：clean build、三個獨立Java測試、實際遮罩/預算盤點、預設指令源碼/bytecode查核、差異與暫存審查。最終配置A20/B8/C14/D20/E14、每點3粒、固定微偏移、4tick Bloom130；動作/數量不變。
使用者已接受130飽滿度及Formation/Hold/Dispersion/Fade；視覺選型PASS。整體PARTIAL僅標示尚缺邊界/效能全項證據，不繼續開發；在此階段邊界停止交付唯讀審查。稀疏感與斜角變形列未來研究，根因未知，不擅自修復。Git結果見PROGRESS；不push。

## 2026-10-23 — Phase 02.6 授權及範圍（最新）

目標：以已被使用者接受的02.5作基準，比較normal100/Bloom115/Bloom130，單粒大小4 tick平滑綻放，Hold14/3粒/點/原色/座標與既有A–E保持；新預設115。範圍檔案：DustStyle、HeartPrototype、新Phase026BloomCheck及五份專案紀錄。工作/測試：核對26.3 scale、時間/大小/座標不變的純邏輯回歸、clean build、Git差異、人工比較指引；唯讀調查Gradle啟動慢與非破壞建議。風險：過亮、糊邊、overdraw、原版碰撞盒受scale影響；不改工具鏈或清理快取。視覺需使用者再驗收，PARTIAL；不開始03或GUI/資料/多人。

## 2026-10-23 — Phase 02.5 授權：Dust質感微調（最新）

目標：改善Hold固定稀疏格點感，保留Dust、A–E及已驗證26.3工具鏈。範圍：集中DustStyle預設（Hold/每點粒數/偏移/大小變化），固定種子一次生成局部偏移，預設14 tick/3粒，legacy指令保留Phase02的28 tick/1粒。修改檔案：HeartPrototype.java、FireworkTimeline.java、新DustStyle.java、新tests/Phase025DustStyleCheck.java及PROJECT_PLAN/PROGRESS/TEST_RESULTS/DECISIONS。保留既有未提交Phase02變更，不重建環境。
工作/測試：參數界限與確定性、時間邊界、非累積大小變化、fade及預算純Java測試；clean build；人工比較由使用者執行。驗收：較自然但輪廓/原色不失真，B→C→D無跳位、clear/重複/世界切換清理保持正常，無無限補生。風險：更密集可能增加亮度或糊邊、偏移可能影響色帶，實際畫質/效能未驗證；不以編譯代替驗收。結束Status PARTIAL，停止於02.5，不進03。

## 2026-10-23 — Phase 02 授權與交付範圍（最新）

使用者已審閱 docs/reviews/phase-01-review.md 並正式確認 Phase01 PASS；d1513ed 已提交，審查報告提交872bd52為本輪開始HEAD，工作樹乾淨。以下舊未提交/PARTIAL/未授權敘述保留作歷史，由本節及最新PROGRESS取代。

目標：保留已接受的原版Dust色彩、遮罩、粒子材質與比例，加入單人本機A–E；預設heart改Dust，image保留Phase01技術對照。修改範圍：HeartPrototype.java、純時間邏輯FireworkTimeline.java、tests/Phase02TimelineCheck.java與五份docs。工具鏈、metadata、共用初始化與審查報告不變。
工作：公開API延長壽命、Launch尾跡/聲音、Formation8/Hold28/Dispersion20/Fade14 tick、D解除位置鎖定、世界/clear/重複觸發清理。維持最多1效果；不因審查建議提前引入多煙火控制器。測試：clean build、時間/縮放邊界檢查、差異檢查；人工驗收依TEST_RESULTS。驗收須五階段可辨識、hold完整、散射連續、無重複聲音、完整清除與可接受效能。
風險：原版粒子引擎仍可能因全局粒子壓力移除粒子；Dust不透明層不支援本方案精確alpha淡出，改為軟粒子縮小；實際畫質與frame time待測。Status PARTIAL，未執行Phase03或GUI/草稿/資料系統/物品/製作/同步。

# Draw & Hide Firework 開發計畫

## 2026-10-23 — Phase 01 收尾授權與正式方向

本次範圍：記錄使用者A/B實測、正式選Dust、靜態評估效能/結構、clean build與準備Phase01 Git commit。僅新增文件收尾內容，保留原型Java/metadata；候選提交包含前輪Phase01原型與本輪紀錄，不含Phase02。
Dust視覺PoC PASS（使用者），Image顯示可用但風格不採用；clear及詳細清理/效能仍未驗證，整體PARTIAL。正式方向取代早先暫選Image；裸heart仍是舊Image alias，當前Dust入口為heart dust。
下一階段僅建議：取得明確授權後，以Dust為基礎做tick驅動A–E，先解決壽命、散射期間取消位置鎖定與清理驗證；保留自然粒子感。Phase02尚未授權或開始。

## 2026-10-23 — Phase 01 明確授權（目前範圍）

- 目標：固定彩色愛心的兩個可觸發原型，對照 formation / hold；僅單人。
- 修改檔案：新增 client/HeartPrototype.java、fabric.mod.json 的 client entrypoint、五份階段文件。共用初始化及所有工具鏈版本不變。
- 工作：核對本機 26.3 簽章與實作、建立相同 11×9 遮罩的 dust / batch quad 方案、8 tick 形成與28 tick 停留、清除與替換入口、clean build、runClient 啟動及人工測試指引。
- 驗收：編譯成功；遊戲內兩個入口可用；比較正常視角辨識、顏色、距離、hold、朝向與結束清除。未觀察項目維持未驗證。
- 風險：原版 dust 隨機色彩與壽命、26.3 deferred rendering、實際 GPU 顯示未驗證；不以建置取代視覺測試。
- 排除：GUI、CanvasData、codec、物品、網路、草稿、多人，以及 Phase 02 完整 A–E／音效／裝飾。
- 交付狀態見 PROGRESS.md 最新條目；Phase 02 未授權。

日期：2026-10-21（Asia/Taipei）。規格依根目錄 AGENTS.md V3.0；本次僅授權 Phase 00，不自動 commit/push。

## 既有基礎與架構

沿用 D:\minecraftmod 的 Gemini 專案，不重建、不重新初始化 Git/Wrapper。現有名稱 myfirstmod、入口 com.example.myfirstmod.MyFirstMod、版本 1.0.0 均為既有環境屬性，不代表 Draw & Hide Firework V1.0 已完成。正式目標為 drawfirework／Draw & Hide Firework；改名留待明確的後續範圍。

現有 main source set 僅有共用初始化及資源，無 client 專用引用；尚未建立 client source set、畫布、GUI、物品、網路或測試。後續分離共用註冊、client 初始化、不可變畫布、驗證/codec、草稿、GUI、網路、權威伺服器生命週期、圖像/裝飾渲染及診斷。優先組合，必要 Mixin 須記錄理由。

依賴以 API_COMPATIBILITY.md 的實際版本為準。核心最多 800 點、裝飾同時最多 200 點，兩者獨立；品質與總併發限制需在實作階段量測。單人 V1.0 為 00–08，多人 V1.1 為 09–10。

## 階段範圍

以下為順序規劃，不構成後續授權。未開始階段的檔案為預期類別/範圍，精確名稱在授權後依 26.3 實際 API 確認；每階段皆須更新五份文件並 clean build。

| Phase | 目標、範圍及工作 | 預期修改檔案 | 測試與驗收 | 風險 |
| --- | --- | --- | --- | --- |
| 00 | 唯讀審計後，核對工具鏈，執行 build/clean build，必要最小修復 | docs 五份；僅有證據時改建置設定 | 精確 26.3、Java 25、可建置；無基礎設施阻塞 | 權限、daemon、快取掩蓋缺件 |
| 01 | 固定彩色愛心，比較 dust 與暫時 client 圖像 formation/hold；無 GUI | client 入口、原型渲染、測試觸發及比較紀錄 | 正常視角可辨識、顏色正確；記錄距離/壽命/朝向/效能/成本/同步可行性；未實測明列 | 26.3 渲染 API、hold 不穩 |
| 02 | tick 驅動 A–E、尾跡聲音、形成8/停留28/解體20/淡出14 tick 初始目標 | 動畫控制、圖像與裝飾層、生命週期 | 順序、hold、解體差異、清除、無重複音效爆炸 | 階段重疊、殘留引用 |
| 03 | 不可變 ARGB、版本化 codec、驗證、降採樣 | 共用模型/codec/downsampler、純邏輯測試 | AGENTS.md §4 全部案例、往返、畸形/超大/溢位拒絕；確定性與比例 | 大圖配置、色偏、透明邊界 |
| 04 | 實際畫布、Data Component、物品整合及權威啟動 | 共用物品/component、必要的有界 payload、client 接收 | 不同圖、透明、物品存檔重載；權限/頻率/解碼量/伺服器執行緒驗證 | 客戶端資料不可信、重複消耗 |
| 05 | GUI 寬高、鉛筆橡皮、調色、清空、預覽與製作入口 | client screen/輸入/預覽 | 拖曳、縮放、座標、焦點、SDL 文字輸入；未完成功能明示 | 新輸入 API、預覽與實際不一致 |
| 06 | config/drawfirework/drafts 安全 JSON | client 草稿儲存/錯誤介面及測試 | 重啟還原、損壞/超大/路徑穿越拒絕、原子替換及 I/O 失敗保護 | 既有草稿破壞 |
| 07 | 明確記錄生存配方及權威背包交易 | server 製作服務、配方/材料、必要 payload | 正確扣料授予；失敗/重複/重入/背包變動不複製、不刪無關物品 | 非原子扣料、免費授予 |
| 08 | V1.0 全流程、畫質/效能/清理、RC JAR | 整合修復、品質設定、發布說明 | 單人 GUI→畫圖→存載→製作→生存發射→A–E；重載、透明、縮放、失敗背包、多煙火與退出清理；量測 frame time/配置/工作量 | 自動測試不能取代遊戲實測 |
| 09 | 專用伺服器及有界事件同步 | server 事件、payload codec、client 播放 | 專服啟動、至少兩客戶端、接收距離/封包/頻率/併發；事件識別、共同 tick/偏移、過期策略；不得逐 tick 傳每點 | client 類別誤載、重複/不同步 |
| 10 | V1.1 安全/效能回歸及發布 | 修復、包裝、可重現說明 | AGENTS.md V1.1 門檻，實際人數場景/版本/量測、可用 JAR | 併發負載與未測限制 |

本次 Phase 00 結束後停止。Phase 01 需使用者另行授權。

## 2026-10-23 — Phase 00.5 authorization and scope

User explicitly authorized Git initialization, a baseline commit, project identity rename and a rename commit only after clean build succeeds. No Phase 01 work is authorized.
Target files: .gitignore, Java initializer path/class, settings.gradle, gradle.properties, fabric.mod.json and phase records. Preserve all dependency versions and caches. Acceptance: separate baseline and rename commits, clean build, clean Git state. Risks: protected .git writes and Gradle daemon connectivity. Current result is PARTIAL; see PROGRESS.md and TEST_RESULTS.md.
