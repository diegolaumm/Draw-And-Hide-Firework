## 2026-10-26 — GitHub 發布前整理（本輪最新）

Phase: 已授權的 RC01 發布整理；不開始後續功能階段。
Status: **PASS（整理／建置／封裝）／PARTIAL（公開發行驗收）**。
Changes: 作者 diegolaumm／MIT 2026、移除範例 contact 與 another-mod、精確 Minecraft 26.3 metadata、README／第三方授權、草稿 ignore、JAR 內 LICENSE、修正 RC01 取得物品指令。所有原有 Java／測試／語系未提交變更完整保留。
Build result: `clean build --no-daemon` 沙箱外 exit 0，23s，7 tasks executed；沙箱內先前因 daemon 連線失敗 exit 1，未修改工具鏈解決。
Tests performed: JUnit 189/189（29 suites，0 failure/error/skipped）；Phase02 三組純邏輯回歸 exit 0；JAR 106 entries／metadata／LICENSE／內容檢查 PASS；敏感資料啟發式掃描未發現命中；diff check PASS。
Tests not performed: 本輪未啟動遊戲、未作視覺／外部 Launcher／多人驗收；既有人工驗收保留為歷史證據。
Known problems: 外部安裝驗收待做；生存製作與多人未實作完成；無已確認 repo URL；Gradle 10 棄用提示不在本輪改工具鏈。
Git commit: 未建立（使用者禁止 commit／push／建立 repo 或 release；未暫存）。HEAD `54c657638dbb196e27189c44d3287f77eec48a0a`，master，無 remote。
Recommended next phase: 先按 RC01 指南完成獨立 Launcher 安裝驗收，再由使用者決定預發行；不代表授權實作下一階段。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行（以上均指本輪）。
GUI handoff: Codex 完成本輪並停止寫入；可交 Antigravity 唯讀審查固定差異。產物 SHA256／後續操作見 `docs/GITHUB_PREPARATION.md`。

## 2026-10-25 — Phase 07A — Artist's Canvas GUI Redesign（正式結案 PASS / CLOSED）

Phase: 07A — Artist's Canvas GUI Redesign
Status: **PASS / CLOSED**（依據使用者 6/6 人工實機驗收通過、Codex 獨立審查無阻斷缺陷，以及 Minor 1 透明填色修復完成正式結案）
Changes:
- **Codex 獨立審查回報之 Minor 1 修復（`CanvasEditorScreen.java`）**：
  - 修正 `selectFillTool()`：移除 `if (color == 0) color = lastPaintColor;` 的強制覆蓋。當使用者從橡皮擦（`color == 0`）切換到油漆桶填色（`Tool.FILL`）時，保留透明色以支援透明區域連通分支清除；僅在使用者明確點擊【畫筆（Brush）】按鈕時才恢復最後選用的色彩。
- **透明填色單元測試（`CanvasEditorThemeLayoutTest.java`）**：
  - 新增 `testEraserToFillAllowsTransparentFloodFillAndUndoRedo`：測試大面積填色後切換至橡皮擦再切至填色，點擊目標區域驗證能成功將相連像素填為透明 `0`，並驗證 Undo 還原為原色、Redo 重新填為透明，以及切回畫筆時色彩正確復原。
- **結案依據確立**：
  - 使用者 6/6 人工實機驗收全數通過（羊皮紙質感、木質畫框、簡潔工具列、Tooltip 提示、按鈕選中/停用狀態、筆觸對比均正常）。
  - Codex 審查 Critical 0 / Major 0 / Minor 1，唯一 Minor 1 已修復並驗證。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 26s，7 tasks executed，exit 0）。發佈正式 JAR：`build/libs/drawfirework-1.0.0.jar`（193,996 bytes）。
Tests performed:
- JUnit **189/189 PASS**（29 suites，0 failures/errors/skipped）。
- Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
- `git diff --check` PASS（無空白錯誤）。
Tests not performed:
- Release Candidate 01 獨立 Launcher 實例外部安裝驗收（由使用者依發布指引實測）。
Known problems: 無已知阻斷性缺陷。
Git commit: 未建立（依規範禁止代理自動 commit / push）。
Recommended next phase: Release Candidate 01 首次公開發行準備（等待使用者實機安裝驗收）。
Compiled: PASS；Unit-tested: 189/189 PASS；Runtime-tested: PASS（Phase 07A 6/6 人工驗收通過）；Visually verified: PASS；Multiplayer-tested: 未執行（Phase 09/10 範圍）。
GUI handoff: Phase 07A 正式結案（PASS / CLOSED），Release Candidate 01 檔案與發布說明就緒。

## 2026-10-25 — Phase 06 高解析度草稿持久化正式結案（PASS / CLOSED）

Phase: 06 — High-Resolution Draft Persistence
Status: **PASS / CLOSED**（Codex 最終獨立唯讀審查通過，使用者 6/6 人工實機驗收全數通過，正式批准結案）
Changes:
- **Codex 最終審查結果**：Critical 0 / Major 0 / Minor 1（唯一文件措辭 Minor 已修正為忠於非原子替代移動與無斷電保證實作，非阻斷性問題）。
- **使用者 6/6 人工實機驗收全數通過**：
  1. 儲存第一份草稿：PASS
  2. 完整重啟 Minecraft 後重新載入：PASS
  3. 多份草稿與另存：PASS
  4. 未儲存變更提醒：PASS
  5. 刪除與返回：PASS
  6. 煙火輸出：PASS
  - 使用者補充反饋：沒有感受到明顯問題。
- **核心成果確立**：
  - 完整 256×256 原始 ARGB 畫稿跨 Minecraft 重啟保存與重新編輯。
  - 草稿庫多作品管理、另存新檔、重新命名、刪除與舊目錄遷移衝突保護。
  - 原有 32×32 煙火物品輸出與 76 ticks 五階段動畫視覺效果完全正常。
- **保留已知限制（Accepted Limits）**：
  - 草稿儲存為客戶端本機資料，不支援多人同步。
  - 256×256 草稿不代表世界煙火輸出已提高至 256×256（維持 32×32 CanvasData）。
  - 非原子檔案系統及斷電安全性不作絕對保證。
  - 128×128 舊尺寸編輯器可能拒絕無法無損表示的 256×256 草稿。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 32s，7 tasks executed，exit 0）。發佈候選 JAR：`build/libs/drawfirework-1.0.0.jar`（175,155 bytes）。
Tests performed:
- JUnit **184/184 PASS**（28 suites，0 failures/errors/skipped）。
- Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
- `git diff --check` PASS（無空白錯誤）。
- 使用者 Minecraft 6/6 人工實機驗收 PASS。
Tests not performed:
- 多人專用伺服器草稿共享（本模組草稿為客戶端本地資料，依規格不實作伺服器同步）。
Known problems: 無已知阻斷性缺陷。
Git commit: 未建立（交付候選檔案就緒，依指示交由使用者建立 Git Commit）。
Recommended next phase: Phase 07 — 生存模式製作與配方整合（等待使用者正式授權）。
Compiled: PASS；Unit-tested: 184/184 PASS；Runtime-tested: PASS（使用者 6/6 實機驗收通過）；Visually verified: PASS（煙火施放 76 ticks 動畫正常）；Multiplayer-tested: 未執行（Phase 09/10 範圍）。
GUI handoff: Phase 06 正式結案（PASS / CLOSED），所有交付檔案就緒，停止寫入。

## 2026-10-25 — Phase 06 最終資料完整性修復交付（Final Data Integrity Remediation）

Phase: 06 — High-Resolution Draft Persistence
Status: **PARTIAL**（Codex 第二次複審確認之 3 項 Major 與直接相關 Minor 全數修復完成，全部自動化測試 184/184 PASS 與 Phase 02 回歸檢驗 PASS，等待使用者實機驗收與 Codex 獨立唯讀複核）
Changes:
- **Major 1 — 阻止跨尺寸有損覆寫（`FreehandCanvas.java`、`CanvasEditorScreen.java`、`DraftManagerScreen.java`、`CanvasCompatibilityTest.java`）**：
  - 在 `FreehandCanvas` 新增 `canLoadLosslessly(int[] draft256)`，判定 256×256 草稿在當前尺寸下是否每組取樣方塊完全相同。
  - 若 256×256 草稿包含細緻斜線、不同顏色或 Alpha 差異，`canLoadLosslessly` 返回 `false`，`loadDraft` 拋出 `IllegalArgumentException` 拒絕載入。
  - 在 `DraftManagerScreen.executeLoad` 與 `CanvasEditorScreen.loadDraftData` 攔截拒絕事件：
    - 發送清晰錯誤提示至玩家（`draft_lossy_load_error`）。
    - **完全保留當前正在編輯的畫布、像素、Dirty 狀態、Undo/Redo 歷史與預覽**。
    - 磁碟上原 256×256 高解析度草稿 100% 原封不動，杜絕因降採樣後 Save 覆寫同一 UUID 造成的資料毀損。
    - 16×16 煙火物品輸出合約與伺服器 `EditSession.save` 契約 100% 保持不變。
- **Major 2 — 備份前完整主檔驗證（`DraftStorage.java`、`DraftSafetyTest.java`）**：
  - 將覆寫前僅讀標頭的 `readMetadata` 改為呼叫 `readFullDraft(targetPath, id)` 完整資料驗證。
  - 嚴格校驗：JSON 結構完整性、`schemaVersion`、檔名 UUID 與 JSON 內容 `id` 一致性、1..256 尺寸、精確像素長度（524,288 hex 字元）以及所有像素之 ARGB 十六進位編碼。
  - 遇到像素截斷、非法 hex 或 UUID 不一致等任何毀損情境，**絕不**將該主檔複製至 `.bak` 備份。
  - 若主檔已毀損但現有 `.bak` 備份有效，`saveDraft` 採取保守防護主動拒絕覆寫，提示使用者先復原備份或另存新檔，誓死守護最後一份有效備份。
- **Major 3 — 新版 Schema 絕不可被自動復原覆蓋（`DraftStorage.java`、`DraftSafetyTest.java`）**：
  - 在 `recoverFromBackup` 單獨且優先攔截 `DraftVersionException`。
  - 若目標主檔之 `schemaVersion > CURRENT_SCHEMA_VERSION`，一般復原與帶 `force=true` 的強制復原均絕對拒絕，立即拋出 `DraftVersionException`。
  - 復原失敗後，新版主檔與舊版備份均保持 100% 位元組不變，絕不將「版本較新」誤判為「檔案損壞」。
- **Minor A — 初始化錯誤容錯與路徑安全（`ClientDraftStorage.java`、`CanvasEditorScreen.java`）**：
  - `ClientDraftStorage` 在遭遇路徑安全異常或初始化失敗時，透過 `initError` 記錄並安全回傳錯誤，不拋出未捕捉崩潰，不清除編輯器畫布或 Dirty 狀態。
- **Minor B — 舊目錄遷移 UUID 衝突安全保護（`ClientDraftStorage.java`、`DraftGuiLogicTest.java`）**：
  - 在 `migrateLegacyDrafts` 中比對來源與目標內容：若內容相異，絕不覆寫或刪除任一檔案，自動生成 `.legacy_conflict.json` 備份並記錄於 `getMigrationConflicts()`。
- **Minor C — 真實產品路徑測試（`DraftGuiLogicTest.java`、`CanvasCompatibilityTest.java`）**：
  - 補足真實 `DraftStorage` 寫入失敗／成功路徑、跨尺寸拒絕／保留路徑以及舊目錄遷移 UUID 衝突測試，擺脫單純 boolean 模擬。
- **非原子寫入與容錯替代策略**：
  - 當檔案系統不支援原子移動時，系統採用非原子替代移動；操作失敗時回報錯誤，並依既有備份／復原策略處理。本機制不保證斷電期間的資料持久性。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 28s，7 tasks executed，exit 0）。JAR：`build/libs/drawfirework-1.0.0.jar`（175,155 bytes）。
Tests performed:
- JUnit **184/184 PASS**（28 suites，0 failures/errors/skipped；新增 8 項嚴格資料完整性測試，包含像素截斷、非法 hex、UUID 不一致之 .bak 保全測試、force recover 保護測試、跨尺寸有損載入拒絕與畫布保全測試、遷移衝突備份測試等）。
- Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
- `git diff --check` PASS（無空白錯誤）。
Tests not performed:
- 多人專用伺服器草稿共享（本模組草稿為客戶端本地資料，依規格不實作伺服器同步）。
- 真人實機遊戲內操作驗證（待使用者依 `docs/PHASE_06_ACCEPTANCE.md` 指南執行）。
Known problems: 無已知阻斷性缺陷。
Git commit: 未建立（嚴格禁止代理自動 commit / push）。HEAD 保持 `a87e0aa`。
Recommended next phase: 等待使用者依驗收指南進行實機驗收，並由 Codex 進行獨立技術審查；通過後再進行 Git 提交並授權 Phase 07。
Compiled: PASS；Unit-tested: 184/184 PASS；Runtime-tested: 待使用者人工驗收；Visually verified: 待使用者人工驗收；Multiplayer-tested: 未執行。
GUI handoff: Phase 06 最終資料完整性修復與自動化驗證已全部完成，Antigravity 停止寫入；維持 Phase 06 PARTIAL，等待使用者人工驗收與 Codex 獨立複核。

## 2026-10-25 — Phase 06 資料安全修復交付（Data Safety Remediation）

Phase: 06 — High-Resolution Draft Persistence
Status: **PARTIAL**（5 項 Major 與 3 項 Minor 安全問題全數修復完成，全部自動化測試與回歸檢驗 PASS，等待使用者實機驗收與 Codex 獨立唯讀複審）
Changes:
- **Major 1 — 儲存成功後才能關閉 GUI（`CanvasEditorScreen.java`、`DraftNamePromptScreen.java`、`UnsavedChangesScreen.java`）**：
  - 建立統一儲存完成狀態機 `saveAndThen(Screen callingScreen, Runnable onSuccess, Runnable onCancel)`。
  - Success：確認資料成功寫入磁碟並驗證無誤後，方才執行原本待辦之關閉 GUI 或切換動作。
  - Cancel：未命名草稿彈出命名視窗時，使用者若按 Cancel 或 Esc 取消命名，呼叫 `onCancel` 回到原畫面，保留原始畫布與 Dirty 狀態，絕不強制關閉。
  - Failure：若磁碟寫入失敗或遭遇 I/O 異常，發送錯誤通知至玩家，保留畫布與 Dirty 狀態，絕不關閉。
- **Major 2 — 儲存失敗不得切換或新建草稿（`DraftManagerScreen.java`）**：
  - 將 Load Draft、New Draft、Save Draft 之未儲存處理全面委派至 `parentEditor.saveAndThen(...)`，只有在確認成功儲存時才執行後續載入或清空畫布操作。
  - 儲存失敗或取消時，不清除目前草稿、像素、Undo/Redo 或 Dirty，徹底消滅雙軌邏輯與吞噬異常問題。
- **Major 3 — 128×128／256×256 工作畫布相容（`FreehandCanvas.java`、`CanvasCompatibilityTest.java`）**：
  - 在 `FreehandCanvas` 新增 `toDraft256Pixels()`：對 16×16 合法舊物品產生之 128×128 工作畫布，採確定性 $2 \times 2$ 鄰近方塊複製映射至 256×256；標準 256×256 畫布則直接複製。
  - 擴充 `loadDraft(int[] newPixels)`：當傳入標準 256×256 草稿至 128×128 畫布時，採確定性左上取樣降採樣回 128×128。若畫稿未經修改，此過程為 100% 雙向無損往返。
  - 煙火輸出合約：`snapshot(mode)` 依然嚴格輸出 `outputWidth() x outputHeight()`（16×16 輸出 16×16，32×32 輸出 32×32），伺服器 `EditSession.save` 與網路 Payload 契約完全不受影響。
- **Major 4 — 備份、覆寫與復原之資料完整性（`DraftStorage.java`、`DraftSafetyTest.java`）**：
  - 覆寫前版本保護：寫入前先行透過 `readMetadata` 檢查目標主檔。若 `schemaVersion > CURRENT_SCHEMA_VERSION`，立即拋出 `DraftVersionException` 拒絕覆寫，杜絕舊客戶端意外破壞新格式畫稿。
  - 損壞檔案不得覆蓋有效備份：寫入前若目標主檔為有效結構，方才複製至 `.bak` 備份；若目標主檔已損毀，**絕不**將損壞檔案複製至 `.bak`，全力保護現有最後一份完好備份。
  - 安全復原檢查：呼叫 `recoverFromBackup` 時，若主檔本身結構完整有效，主動拒絕無條件復原以防以舊備份覆蓋新稿（需透過 `force` 參數明確覆寫）；復原過程一律經由暫存檔原子替換。
- **Major 5 — 符號連結防護與隨機暫存檔（`DraftStorage.java`、`DraftSafetyTest.java`）**：
  - 放棄固定 `.json.tmp` 命名，改採 `Files.createTempFile(baseDir, id.toString() + "_", ".tmp")` 建立隨機且唯一的暫存檔，並於 `try ... finally` 中保證安全清理。
  - 於 `resolvePath` 強制執行 `Files.isSymbolicLink(path)` 檢查與 `toRealPath().startsWith(baseDir.toRealPath())` 雙重路徑穿越防護，嚴禁符號連結指向草稿目錄之外或偽造草稿檔。
- **Minor 1 — 窄視窗自適應排版（`DraftManagerLayout.java`、`DraftManagerLayoutTest.java`）**：
  - 建立純 Java 獨立排版計算器 `DraftManagerLayout`，支援 $[140 \times 140]$ 至 4K 螢幕。
  - 保證 `listWidth >= 30`、`buttonWidth >= 40`、清單與按鈕完全不重疊、按鈕與文字絕不溢出螢幕邊界。
- **Minor 2 — 儲存路徑規格統一與舊目錄遷移（`ClientDraftStorage.java`）**：
  - 統一規範儲存目錄為 `<gameDirectory>/drawfirework/drafts/`。
  - 在 `ClientDraftStorage` 初始化時自動偵測舊版可能殘留之 `config/drawfirework/drafts/` 目錄並安全複製未遷移之草稿至標準目錄。
- **Minor 3 — 精確錯誤追蹤與可靠性宣告（`DraftStorage.java`、`DraftManagerScreen.java`）**：
  - 目錄讀取異常時透過 `lastListError` 保存錯誤訊息並在 UI 即時提示，不把異常讀取誤報為「空草稿庫」。
  - 明確澄清：`writer.flush()` 保證將資料從使用者空間緩衝區沖刷至作業系統檔案快取，非保證實體磁碟斷電安全（`fsync`）；`ATOMIC_MOVE` 遇檔案系統不支援時具備覆蓋移動降級。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 26s，7 tasks executed，exit 0）。JAR：`build/libs/drawfirework-1.0.0.jar`（172,572 bytes）。
Tests performed:
- JUnit **176/176 PASS**（28 suites，0 failures/errors/skipped；新增 11 項測試，包含 `DraftSafetyTest` 4 項、`CanvasCompatibilityTest` 3 項、`DraftManagerLayoutTest` 2 項、`DraftGuiLogicTest` 2 項狀態機測試）。
- Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
- `git diff --check` PASS（無空白錯誤）。
Tests not performed:
- 多人專用伺服器草稿共享（本模組草稿為客戶端本地資料，依規格不實作伺服器同步）。
- 真人實機遊戲內操作驗證（待使用者依 `docs/PHASE_06_ACCEPTANCE.md` 指南執行）。
Known problems: 無已知阻斷性缺陷。
Git commit: 未建立（嚴格禁止代理自動 commit / push）。HEAD 保持 `a87e0aa`。
Recommended next phase: 等待使用者依驗收指南進行實機驗收，並由 Codex 進行獨立技術審查；通過後再進行 Git 提交並授權 Phase 07。
Compiled: PASS；Unit-tested: 176/176 PASS；Runtime-tested: 待使用者人工驗收；Visually verified: 待使用者人工驗收；Multiplayer-tested: 未執行。
GUI handoff: Phase 06 資料安全修復與自動化驗證已全部完成，Antigravity 停止寫入；維持 Phase 06 PARTIAL，等待使用者人工驗收與 Codex 獨立複核。

## 2026-10-25 — Phase 06 高解析度草稿持久化實作交付（等待驗收與審查）

Phase: 06 — High-Resolution Draft Persistence
Status: **PARTIAL**（實作與自動化測試已全數完成，等待使用者實機驗收與 Codex 獨立唯讀複核）
Changes:
- **純 Java 草稿儲存核心（`com.drawfirework.draft`）**：
  - `DraftMetadata.java`：不可變草稿中繼資料標頭（UUID、名稱、建立／更新時間戳記、寬度、高度、`schemaVersion = 1`）。名稱支援格式安全消毒（長度上限 64，檔名路徑字符防護）。
  - `Draft.java`：不可變高解析度草稿資料模型，完整封裝 256×256 原始 ARGB 像素（65,536 像素，保留直覺 Alpha 通道），提供 `createNew`、`withName`、`withPixels`、`copyAsNew` 等純資料操作。
  - `DraftStorage.java`：本機檔案安全儲存服務。支援：
    - 同目錄暫存檔寫入（`.json.tmp`）與原子替換（`ATOMIC_MOVE`）。
    - 覆蓋舊草稿前自動備份有效檔案至 `.json.bak`，並提供 `recoverFromBackup` 復原機制。
    - 檔案大小上限（2 MB）與草稿總數量上限（200 份）防護，杜絕記憶體與磁碟過度配置。
    - 嚴格目錄穿越檢查（`resolvePath` 確保嚴格位於 `drawfirework/drafts/` 內）。
    - 毀損或未知較新 `schemaVersion`（> 1）安全拒絕載入，絕不崩潰遊戲，絕不自動刪除或損毀損壞檔案。
    - 列表快讀：`readMetadata` 使用 streaming `JsonReader`，不載入 pixels 陣列即可快速排序取得所有草稿清單。
  - `DraftStorageException.java`、`DraftCorruptedException.java`、`DraftVersionException.java`：清晰之階層式例外模型。
- **編輯器畫布擴充（`FreehandCanvas.java`）**：
  - 新增 `loadDraft(int[] newPixels)`：載入 256×256 草稿像素，安全重設 Undo/Redo 歷史堆疊與覆蓋率遮罩，防止跨草稿工作階段歷史洩漏；更新 `revision` 與 `changed` 標記。
  - 新增 `resetBlank()`：重設畫布為全透明空白，安全清空歷史堆疊與作用中筆畫。
- **草稿客戶端存取點（`ClientDraftStorage.java`）**：
  - 動態綁定 Minecraft 實體 `gameDirectory` 之 `drawfirework/drafts/` 目錄。
- **草稿管理介面（`DraftManagerScreen.java` & `DraftListWidget.java`）**：
  - `DraftListWidget`：雙行排版滾動清單，顯示草稿名稱、最後修改時間（`yyyy-MM-dd HH:mm`）、尺寸及 `[目前編輯中]` 標籤，支援雙擊項目直接載入。
  - `DraftManagerScreen`：提供載入草稿（Load）、建立新草稿（New）、儲存草稿（Save）、另存新檔（Save As...）、重新命名（Rename...）、刪除草稿（Delete，結合 `ConfirmScreen` 二次確認）與返回編輯（Back）。
- **未儲存變更防護（`UnsavedChangesScreen.java` & `DraftNamePromptScreen.java`）**：
  - `UnsavedChangesScreen`：草稿有未儲存變更時，關閉 GUI、切換載入其他草稿或新建草稿時，提供【儲存草稿】、【捨棄變更】與【取消】三項明確操作，杜絕畫作遺失。
  - `DraftNamePromptScreen`：通用彈出式名稱輸入對話框，支援命名、另存與重新命名。
- **主編輯器整合（`CanvasEditorScreen.java`）**：
  - 新增草稿追蹤欄位（`currentDraftId`、`currentDraftName`、`savedRevision`）與髒狀態判斷（`isDraftDirty`）。
  - 頂部標題即時反映當前草稿名稱與未儲存狀態（例如 `煙火像素畫布 — Dragon*`）。
  - 右側按鈕排版擴充為 10 列，垂直安全緊湊排列（列高 21/22px），小視窗（240px）下絕不越出視窗底緣或侵入畫布。
  - 清楚區分 `Save Draft`（儲存草稿至本機 256×256）與 `To Firework`（儲存至煙火 32×32，送往伺服器物品）。
  - 支援 `Ctrl+S` 快捷鍵即時快速存檔。
- **雙語系文字補充（`en_us.json` & `zh_tw.json`）**：
  - 補充所有草稿管理、按鈕、對話框、確認訊息與錯誤回饋之繁中與英文翻譯。
- **嚴格不變量保留**：
  - 32×32 `CanvasData`、ItemStack / C2S 安全驗證、76 ticks 五階段動畫、Bloom 130%、Fixed 朝向、Phase 05C `fullbright_exact` 光照與 Phase 05D `Balanced` / `Detail-Preserving` 正式預設 100% 保留。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 24s，7 tasks executed，exit 0）。JAR：`build/libs/drawfirework-1.0.0.jar`。
Tests performed:
- JUnit **165/165 PASS**（25 suites，0 failures/errors/skipped；新增 17 項測試，含 `DraftStorageTest` 10 項、`DraftCanvasIntegrationTest` 4 項、`DraftGuiLogicTest` 3 項）。
- Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）。
- `git diff --check` PASS（無空白錯誤）。
Tests not performed:
- 多人專用伺服器草稿共享（本模組草稿為客戶端本地資料，依規格不實作伺服器同步）。
- 真人實機遊戲內操作驗證（待使用者依 `docs/PHASE_06_ACCEPTANCE.md` 指南執行）。
Known problems: 無已知阻斷性缺陷。
Git commit: 未建立（嚴格禁止代理自動 commit / push）。HEAD 保持 `a87e0aa`。
Recommended next phase: 等待使用者依驗收指南進行實機驗收，並由 Codex 進行獨立技術審查；通過後再進行 Git 提交並授權 Phase 07。
Compiled: PASS；Unit-tested: 165/165 PASS；Runtime-tested: 待使用者人工驗收；Visually verified: 待使用者人工驗收；Multiplayer-tested: 未執行。
GUI handoff: Phase 06 實作與自動化驗證已全部完成，Antigravity 停止寫入；維持 Phase 06 PARTIAL，等待使用者人工驗收與 Codex 獨立複核。

## 2026-10-24 — Phase 05D 畫質檔位與採樣最佳化（正式結案）

Phase: 05D — Particle Quality & Sampling Optimization
Status: **PASS / CLOSED**（依使用者正式批准結案）
Changes:
- 核心畫質檔位（ParticleQuality.java）：建立 Standard（800 顆核心粒子／266 樣本）、Balanced（1100 顆／366 樣本）、High（1400 顆／466 樣本）三種客戶端畫質檔位；**正式將 Balanced 設為生產預設（`ParticleQuality.DEFAULT = BALANCED`）**，`/drawfirework quality reset` 恢復 Balanced。
- 細節優先採樣（DetailPreservingSampler.java & SamplingStrategy.java）：實作確定性 A/B 採樣演算法，外輪廓、封閉迴圈邊界、端點與高對比色彩邊界優先保留，實心色塊內部平滑剖分，維持 Detail-Preserving 為預設，Existing 為對照。
- 指令控制（HeartPrototype.java）：`/drawfirework quality <standard|balanced|high|reset>` 與 `/drawfirework sampling <existing|detail_preserving|reset>` 查詢、切換與重設，與 Lighting 設定完全正交獨立。
- 預覽與播放一致性（CanvasPreview.java）：Quality 與 Sampling 變更時自動失效快取，Dust 預覽即時更新點位與統計字串（如「1098 粒子（366 樣本 × 3）」），Save 送出之 32×32 CanvasData 100% 相同。
- Codex 最終獨立唯讀複核與 4 項 Minor 解決確認：
  - HEAD 基準：`ae1e9fbb0c084139b27b6e6573d4b3e10ee9dfef`。
  - 審查結果：Critical 0 / Major 0 / Minor 0。
  - 前次 4 項 Minor（明確指定 266 語意、70/30 動態目標分配契約、過度宣稱修正、GUI Session 限制說明）全部 RESOLVED。
- 使用者實機 A/B 測試與畫質選型：
  - 使用者已完成 Quality / Sampling 實機驗收。
  - 正式預設組合：**Balanced + Detail-Preserving + Fullbright Exact**。
- 保留事實與已接受限制（Accepted Limits & Deferred Verification）：
  - 60 FPS 為使用者特定筆電環境觀察，不是通用效能保證；各檔位仍受核心預算上限嚴格鉗制。
  - Detail-Preserving 在實體粒子散布下差異較為細微，並非對所有圖案都一定優於 Existing。
  - 專業 Profiler 效能量測（CPU/GPU 記憶體配置量）與採樣策略之延伸視覺比較可延後進行。
  - CanvasData 仍為 32×32 規格；256×256 原稿永久保存未實作（屬 Phase 06 範疇）。
- 嚴格不變量：32×32 CanvasData 格式、手繪 Session、C2S 驗證、76 ticks 動畫、Bloom 130%、Fixed 朝向與 Phase 05C fullbright_exact 100% 保留。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 28s，7 tasks executed，exit 0）。JAR：`build/libs/drawfirework-1.0.0.jar`，133607 bytes。
Tests performed: JUnit 148/148 PASS（22 suites，0 failures/errors/skipped）；Phase 02 三組獨立視覺回歸各 exit 0 PASS（TimelineCheck、DustStyleCheck、BloomCheck）；git diff --check PASS。
Tests not performed: 專業 Profiler 效能測量（CPU/GPU 記憶體配置量），記為 Deferred Verification。
Known problems: 無已知阻斷性缺陷；各檔位皆受嚴格核心預算上限鉗制。
Git commit: 未建立（嚴格禁止自動 commit / push）。HEAD 保持 ae1e9fb。
Recommended next phase: 由使用者執行 17 個檔案的 Git commit；其後等待 Phase 06 正式授權。
Compiled: PASS；Unit-tested: 148/148 PASS；Runtime-tested: 使用者實機 A/B 測試已完成；Visually verified: 使用者確認 Balanced 視覺優於 High；Multiplayer-tested: 未執行。
GUI handoff: Phase 05D 正式結案，Antigravity 停止寫入；等待使用者 Git 提交與下一階段授權。

## 2026-10-24 — Phase 05C 正式預設整合與結案（歷史紀錄）

Phase: 05C — Finalize and Close
Status: PASS / CLOSED（依使用者本次正式授權及調整後驗收政策）。
Changes: 新正式預設fullbright_exact；初始化、reset、null fallback共用DustLightingMode.DEFAULT，parse工具default別名同步。保留fullbright自然色差、baseline舊版回退、exact_only診斷。新增從四模式reset到新預設的JUnit；只改三個既有05C Java／測試檔及必要文件，所有既有未提交工作保留。
Build result: `.\gradlew.bat clean build --no-daemon` exit0，BUILD SUCCESSFUL in1m55s，7 tasks executed；版本及工具鏈未變。JAR：build/libs/drawfirework-1.0.0.jar，119490bytes。
Tests performed: JUnit124/124 PASS（19 suites，0 failures/errors/skipped）；DustLightingModeTest9項；Phase02TimelineCheck、Phase025DustStyleCheck、Phase026BloomCheck各exit0 PASS；git diff --check exit0。
Tests not performed: 原版Dust完整實機隔離、所有四模式／日夜圖案矩陣、部分重啟／重進生命週期；均為使用者核准的Accepted Risk / Deferred Verification，沒有寫成PASS。最終預設版本未重新執行runClient場景驗收。
Known problems: 無已知Critical／Major或實際編譯、Mixin、安全、播放回歸阻斷。既有Mixin靜態審查、實際轉換位元碼、兩項文件Minor修復及使用者畫面觀察均保留。Mixin與注入JSON SHA256未變，覆寫仍只在本模組明確標記true時作用，不更改世界光照。
Git commit: 未建立；未暫存、commit或push。HEAD94458d5e2b93fba29754e37d2824ea7b90744c6f。精確15檔候選清單與本次／既有差異分組見docs/reviews/phase-05c-review.md最新結案節。
Recommended next phase: 無本次後續執行；05D／06未開始，須另行明確授權。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 先前runClient啟動與注入已確認；Visually verified: 使用者既有改善與色彩偏好已確認，延伸矩陣Deferred；Multiplayer-tested: 未執行。
GUI handoff: 本輪建置與回歸已結束；Codex更新結案紀錄後停止。模式是客戶端記憶體狀態，同客戶端跨世界保留選擇，重啟回fullbright_exact；未新增持久化或C2S。

## 2026-10-24 — 05C 文件 Minor 修正與 Runtime 啟動查核（歷史）

Phase: 05C — Review Remediation and Runtime Acceptance
Status: PARTIAL；不能 PASS / CLOSED。
Changes: 僅修文件：GUI／物品入口、指令條件、測試覆蓋範圍、模式跨世界／重啟預期及候選預設。兩項文件 Minor 已處置，未改 Java／JSON／Gradle、渲染架構或預設。
Build result: `.\gradlew.bat clean build --no-daemon`（GRADLE_USER_HOME=D:\minecraftmod\gradle，沙箱外）：exit0，18s，7 tasks executed。
Tests performed: JUnit123/123 PASS；實際 runClient 啟動、Mixin 匯出 Particle 位元碼確認注入；git diff --check。
Tests not performed: Minecraft 世界內16組四模式／雙圖／日夜矩陣、未標記原版Dust、退出／重進／重啟後模式與清理；原生GUI控制不可用，需使用者依指南手測。三組Phase02回歸沿用前輪PASS，本輪文件修正未重跑。
Known problems: 注入成功不代表場景／隔離／清理已驗收；runClient 以Ctrl+C主動中斷（exit1），不是正常GUI關閉測試。使用者視覺回饋保留：Exact忠實飽滿、夜間不錯，白天略螢光，暫不調色。
Git commit: 未建立；未暫存／push，HEAD仍94458d5。保留全部既存未提交變更；精確候選清單見 [審查補記](reviews/phase-05c-review.md)。
Recommended next phase: 先補05C人工Runtime驗收與正式預設決策；不開始05D／06。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 啟動／注入已確認，世界場景未執行；Visually verified: 只有使用者既有定性回饋；Multiplayer-tested: 未執行。
GUI handoff: 本輪runClient及build均已結束；Codex完成紀錄後停止寫入。正式預設baseline，fullbright_exact候選，保留fullbright。

## 2026-10-24 — Codex 05C 獨立審查與建置複驗（歷史）

Phase: 05C
Status: PARTIAL — 未結案；未開始 05D 實作。
Changes: 僅補審查／驗證紀錄；程式、預設、工具鏈均未修改。Gemini／Antigravity 保持唯一程式實作者；使用者確認其停止寫入後，Codex 執行本輪建置。
Build result: clean build PASS（29s，exit 0）；沙箱首次 daemon 連線失敗，沙箱外同命令成功。
Tests performed: 123/123 JUnit PASS，19 suites，0 failures/errors/skipped；三組 Phase 02 回歸 PASS；git diff --check PASS。
Tests not performed: Codex 未親自遊戲驗收、未驗證未標記原版粒子與本次 Mixin 的 runtime 隔離、未測多人及 frame time。
Known problems: 靜態審查無阻斷程式缺陷，2 Minor（指南 GUI 入口、測試覆蓋描述）；詳見 [獨立報告](reviews/phase-05c-review.md)。使用者已確認白天／手繪多色發射測過：夜間效果不錯，白天偏螢光略假，暫不要求調整；不記為全情境畫質 PASS。
Git commit: 未建立（明確禁止自動 commit/push）；HEAD 94458d5e2b93fba29754e37d2824ea7b90744c6f，起始 6 tracked 修改與 6 untracked 項目，非乾淨工作樹；原變更保留。
Recommended next phase: 先處置 05C 審查／runtime 證據及結案決定，再授權 Gemini 實作 05D；05D 預算／採樣／預覽／量測方案已存獨立報告，不代表已開始。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 使用者定性回報，Codex 未執行；Visually verified: 使用者已測且保留白天觀感；Multiplayer-tested: 未執行。
GUI handoff: Codex 本輪紀錄完成即停止寫入；未啟動背景改碼或遊戲。正式預設仍 baseline；fullbright_exact 僅候選，保留 fullbright。

## 2026-10-24 — Phase 05C 煙火可見度與光照改善（前次實作紀錄）

Phase: 05C — Firework Visibility & Lighting
Status: **Phase 05C PARTIAL — Awaiting user visual A/B acceptance and independent technical review**
Changes:
- 實作代理：Gemini / Antigravity（依使用者明確授權擔任唯一程式修改代理）；Codex 維持獨立唯讀技術審查角色。
- 基準 Commit：`94458d5`（功能基準 `83062a9`），乾淨工作樹。
- 本階段完整交付成果：
  1. **原始碼診斷與根因確認（05C.1）**：
     - Minecraft 26.3 `DustParticle` 繼承 `Particle.getLightCoords()`，即時查詢方塊座標之世界環境光。夜間無火把等光源時，天光與方塊光趨近於 0，取樣 LightTexture 呈近乎全黑；夜間有火把時方塊光拉高至 14，使粒子被火把照亮（100% 原始碼證實使用者長期觀察）。
     - 原版煙火 `FireworkParticles.SparkParticle` 繼承 `SimpleAnimatedParticle`，`getLightCoords()` 固定回傳 `15728880`（`0x00F000F0 = LightCoordsUtil.FULL_BRIGHT`），具備 Fullbright 自發光。
     - `DustParticle` 建構子對 RGB 進行平均 28%（乘數 $[0.48, 1.00]$）的隨機暗化與通道偏移。
  2. **Duck Interface 與專屬 Mixin（05C.2）**：
     - 新增 `GlowingParticleAccess.java` 與 `ParticleMixin.java`，僅在粒子持有 `drawfirework$fullbright` 標籤時回傳 `15728880`。
     - 預設值為 `false`，未標記路徑保留原方法；靜態證據不等於原版及第三方粒子已經 Runtime 驗收。
  3. **四種正交實驗對照模式（`DustLightingMode.java`）**：
     - `BASELINE`：世界環境光＋原版隨機暗化（**嚴格作為正式預設**，不覆蓋原版表現）。
     - `FULLBRIGHT`：Fullbright（15728880）光照＋保留原版隨機暗化（獨立隔離光照變因）。
     - `FULLBRIGHT_EXACT`：Fullbright 光照＋100% 還原玩家所選 RGB（消除暗化，測試最高清晰度）。
     - `EXACT_ONLY`：世界環境光＋100% 還原 RGB（獨立隔離色彩暗化變因）。
  4. **指令與播放整合**：
     - 新增 `/drawfirework lighting <baseline|fullbright|fullbright_exact|exact_only|reset>` 與查詢 `/drawfirework lighting`。
     - 切換後即時套用於 `/drawfirework heart`、`/drawfirework canvas ...` 與玩家手持自製煙火物品右鍵施放。
  5. **嚴格不變量保留**：
     - 粒子位置、取樣（`CanvasDownsampler`）、上限 800 粒子預算、五階段動畫（A20/B8/C14/D20/E14 共 76 ticks）、Bloom 130%、Fixed/Dynamic 朝向與 05B.2 GUI 100% 保留。
  6. **人工驗收文件**：
     - 撰寫 `docs/PHASE_05C_ACCEPTANCE.md`，提供日間開闊天空、夜間無光源、夜間有火把及自訂手繪圖案之 A/B 操作指引與回報清單。
- 自動化與回歸驗證結果：
  - `.\gradlew.bat clean build --no-daemon`：**BUILD SUCCESSFUL in 26s**（7 actionable tasks: 7 executed，exit 0）。
  - JUnit：**123/123 PASS**（19 suites，0 failures, 0 errors, 0 skipped，新增 8 項純 Java 測試）。
  - Phase 02 三組獨立視覺回歸：全部 PASS（TimelineCheck, DustStyleCheck, BloomCheck）。
  - `git diff --check`：PASS（無空白錯誤）。
  - JAR 產物：`build/libs/drawfirework-1.0.0.jar`。
- 保留事項（非阻塞項目，依規定記錄）：
  - 實機真人視覺 A/B 評估待使用者在 Minecraft 中親自體驗並回報感受。
  - Codex 獨立技術複審待交付後執行。
  - 高解析度原稿草稿持久化保留為 Phase 06。
Git commit: 未建立（嚴格禁止自動 commit / push）。
Recommended next phase: 使用者實機視覺 A/B 驗收與 Codex 獨立技術複審；不開始 Phase 06。
Compiled: PASS；Unit-tested: 123/123 PASS；Runtime-tested: 既有 8 項 PASS，05C 實機待人工對照；Visually verified: 05C 待人工 A/B 驗收；Multiplayer-tested: 未執行。

## 2026-10-24 — Phase 05B.2 正式結案（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Smart Resampling, Preview, Undo/Redo, Flood Fill)
Status: **Phase 05B.2 — PASS / CLOSED**
Changes:
- 結案依據：
  - 結案程式碼 Commit ID：`83062a995934ecd139bbb36e21eda7130a0a8534`。
  - Codex 最終獨立技術複審結果：**Critical 0、Major 0、Minor 0**，先前所有審查問題全數 RESOLVED，建議標記 PASS / CLOSED。
  - 使用者正式接受複審建議，正式批准 Phase 05B.2 結案。
- 本階段完整交付成果：
  1. **智慧轉換（FreehandResampler）**：提供 Soft（等面積預乘降採樣）與 Crisp（8×8 區塊 8 方向膨脹連通分支檢查，連續跨度 $\ge 4$ 時有界幾何平均補償 Alpha，不粗暴二值化，孤立雜點不增益）。
  2. **輸出預覽（CanvasPreview & ViewMode）**：DRAW、32×32（微棋盤透明底）、FIREWORK（重用 `DustStyle.BLOOM_130` 之 3 粒微偏移物理採樣，長寬比精確縮放，外圍邊界裕度消除硬裁切）。預覽與儲存 100% 共用快取。
  3. **歷史復原／重做（Undo / Redo）**：上限嚴格鎖定為 **16 步**，歷史快照像素資料常駐記憶體最多約 **4 MiB**（加固定工作陣列約 4.56 MiB，活躍筆畫峰值約 4.81 MiB）。操作先結束活躍筆畫再判斷出棧，Redo 分支安全截斷，杜絕異常。
  4. **非遞迴油漆桶填色（Flood Fill）**：迭代 BFS 演算法（容量 65,536），杜絕 StackOverflowError，支援透明背景與相同顏色防護。
  5. **GUI 排版與自適應安全邊界（CanvasEditorLayout）**：抽取純 Java 排版模組。畫板尺寸依文字動態計算；統計文字支援 Full $\to$ Short $\to$ Compact $\to$ Minimal $\to$ Number $\to$ Safe Omission（`null` / `visible = false`）降級階梯；移除 40px 人為保底，文字右界絕對不侵入按鈕；說明文字受視窗底緣限制。
- 自動化與回歸驗證結果：
  - `.\gradlew.bat clean build --no-daemon`：**BUILD SUCCESSFUL in 27s**（7 actionable tasks: 7 executed，exit 0）。
  - JUnit：**115/115 PASS**（18 suites，0 failures, 0 errors, 0 skipped）。
  - Phase 02 三組獨立視覺回歸：全部 PASS（TimelineCheck, DustStyleCheck, BloomCheck）。
  - `git diff --check`：PASS（無空白錯誤）。
  - 使用者人工驗收：初次 5/5 PASS、修復後 3/3 PASS，並確認世界中煙火施放視覺效果改善。
- 保留事項（非阻塞項目，依規定記錄）：
  1. 極窄視窗實機排版重測尚未執行，經 Codex 獨立審查評估為非阻斷事項（自動化測試已 100% 覆蓋幾何不變量）。
  2. Dust 夜間偏暗為原版粒子無光照發光屬性所致，留待整體 GUI 完成後另行處理。
  3. Soft／Crisp 肉眼差異有限，列為視覺品質後續優化。
  4. 高解析度原稿目前只在編輯 Session 暫存，永久保存屬 Phase 06。
  5. Dust 預覽為二維示意，不等同完整三維煙火動畫。
Git commit: `83062a995934ecd139bbb36e21eda7130a0a8534`。本輪僅更新結案文件，不執行 commit/push。
Recommended next phase: Phase 06（草稿持久化），等待使用者明確授權後啟動。
Compiled: PASS；Unit-tested: 115/115 PASS；Runtime-tested: 既有 8 項 PASS，極窄視窗實機重測未執行（不記為 PASS）；Visually verified: 既有 PASS，極窄視窗實機重測未執行（不記為 PASS）；Multiplayer-tested: 未執行。

## 2026-10-24 — Phase 05B.2 最終統計文字邊界收尾（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Final Statistics Text Boundary Fix)
Status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**
Changes:
- 審查基準 Commit：`d6941bfd5c8d6ac0d107be8bc39d45f18dd14224`；Codex 複審回報 Critical 0、Major 0，僅剩小視窗邊界與方法呼叫兩項 Minor。
- 本輪針對 Codex 提出的兩項 Minor 進行徹底修復：
  1. **消除 `maxTextWidth` 人為 40px 保底造成之按鈕侵入**（`CanvasEditorLayout.java`）：
     - 將 `maxTextWidth` 由 `Math.max(40, pStart - 4 - MARGIN_LEFT)` 改為 `Math.max(0, pStart - 4 - MARGIN_LEFT)`。
     - 在窄視窗（$w \le 140$）下，嚴格回傳真實可用寬度（如 $w=120$ 為 16px，$w=90$ 為 0px），杜絕文字右界侵入右側按鈕。
  2. **消除 Screen 邏輯重複並落實降級至安全省略**（`CanvasEditorLayout.java` & `CanvasEditorScreen.java`）：
     - `CanvasEditorScreen` 廢除自建之判斷分支，全數直接呼叫純 Java 且受測之 `CanvasEditorLayout.resolveStatsText(...)`，內部委派 `chooseStatsText(...)`。
     - 實作完整降級序列：Full $\to$ Short $\to$ Compact $\to$ Minimal（`preview_dust_minimal`，如 `348 pts`） $\to$ 純數字（如 `348`） $\to$ 安全省略（`null` / `visible = false`）。
     - 當可用空間小於最短標籤或 $\le 0$ 時，`chooseStatsText` 回傳 `null`，Screen 寧可安全不顯示文字，絕不出界或侵入按鈕。
     - 32x32 與 Draw 模式亦納入候選降級清單，全數一致。
  3. **自動化測試直接覆蓋產品方法**（`DustShapeFidelityTest.java`）：
     - `testStatsLabelCandidateProgressionAndSafeOmission`：驗證 Full $\to$ Short $\to$ Compact $\to$ Minimal $\to$ Number $\to$ Safe Omission 各級閾值與負數／0 寬度處理。
     - `testMaxTextWidthNoArtificialFloor`：驗證 800 (652), 180 (76), 120 (16), 90 (0), 50 (0) 無人為保底。
     - `testResolvedStatsTextNeverEncroachesButtonsAcrossWindowSizes`：遍歷 50..800 各種寬度，數學與數值證明 visible 時 `x >= MARGIN_LEFT` 且 `x + width <= panelStart - 4`，non-visible 時 `width == 0`，永不侵入按鈕。
     - `testStatsLabelSelectionUnderTightWorkWidth`：擴充 Compact 溢出（90px）與非正數寬度回傳 null 之安全省略驗證。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 27s，7 actionable tasks: 7 executed，exit 0）。產出 JAR `build/libs/drawfirework-1.0.0.jar`。
Tests performed: JUnit **115/115 PASS**（0 failures, 0 errors, 0 skipped）；Phase 02 三組回歸全部 PASS；`git diff --check` PASS。
Tests not performed: 極窄視窗遊戲內真人實機重測、Codex 獨立複審。
Known problems / follow-up: Soft/Crisp 肉眼差異有限，屬後續視覺品質觀察；Dust 夜間偏暗繼續延後至整體 GUI 完成後調查；高解析度畫稿仍只在 Session 暫存（屬 Phase 06 範圍）。
Git commit: 未建立（明確不自動 commit / push）。審查基準為 `d6941bf`。
Recommended next phase: 使用者人工驗收與 Codex 獨立複審；不開始 Phase 06。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 既有 8 項 PASS，極窄視窗文字待人工重測；Visually verified: 既有 PASS，極窄視窗文字待人工重測；Multiplayer-tested: 未執行。

## 2026-10-24 — Phase 05B.2 最終 Minor 修正與測試有效性收尾（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Remediation & Final Minor Closure)
Status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**
Changes:
- 審查基準 Commit：`2b5c5c8af9f37ee1a7be7439e9ec05f291001d19`；Codex 最終複審回報 Critical 0、Major 0，原 Undo/Redo、Dust 預覽三粒分布、Crisp 連通性均已 RESOLVED。
- 使用者完成初次 5/5 人工驗收及修復後 3/3 人工驗收（文字、輪廓、施放均 PASS，並確認施放煙火視覺效果改善）。
- 本輪針對 Codex 提出的 3 項 Minor（小視窗文字排版、測試有效性、文件數字精確度）進行徹底修復：
  1. **小視窗預覽文字邊界與動態折行高度計算**（`CanvasEditorLayout.java` & `CanvasEditorScreen.java`）：
     - 抽取純 Java `CanvasEditorLayout`，畫板尺寸計算 `computeBoard` 動態傳入實際測量折行所需高度（`textHeightNeeded`），非固定常數。
     - 粒子統計文字 Line 1 增加 `chooseStatsText` 逐級簡短標籤（`preview_dust` $\to$ `preview_dust_short` $\to$ `preview_dust_compact`），且 `statsX` 嚴格拘束於 `[8, panelStart - 4 - statsWidth]`，絕不穿入右側按鈕區域。
     - 說明文字 Line 2 依 `font.split` 實際寬度測量折行，行數受 `maxAllowedDisclaimerLines` 嚴格限制，絕不溢出視窗底部。
     - 粒子投影計算 `projectX`, `projectY`, `projectionScale`, `particleDotRadius`, `isParticleVisible` 全數收斂至 `CanvasEditorLayout`。
  2. **回歸測試有效性**（`DustShapeFidelityTest.java`）：
     - 消除測試中重複抄寫 Screen 數學公式的壞味道，測試直接呼叫實際產品類別 `CanvasEditorLayout`。
     - 覆蓋 180..800 寬度、160..600 高度及 1..6 行說明的各尺寸邊界驗證。
     - 包含歷史錯誤回歸對照測試（`testOldLayoutBugRegressionDetectsOverlap` 與 `testNoParticleDroppedAtPerimeterWithProductMethod`），證明舊版重疊與邊界裁切會被測試精確抓出。
  3. **文件數字精確度更正**：
     - 更正歷史像素上限為 4 MiB（16 份快照合計 $\le 16$），加上固定工作陣列為 4.56 MiB，活躍筆畫為 4.81 MiB。
     - 更新最新實際 XML 統計之 JUnit 測試總數為 **112 / 112 PASS**。
     - 補登使用者初次 5/5 與修復後 3/3 人工驗收 PASS 紀錄。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（BUILD SUCCESSFUL in 28s，7 actionable tasks: 7 executed，exit 0）。產出 JAR `build/libs/drawfirework-1.0.0.jar`。
Tests performed: JUnit **112/112 PASS**（0 failures, 0 errors, 0 skipped）；Phase 02 三組回歸全部 PASS；`git diff --check` PASS。
Tests not performed: 本輪新文字排版之遊戲內真人實機重測、Codex 獨立複審。
Known problems / follow-up: Soft/Crisp 肉眼差異有限，屬後續視覺品質觀察；Dust 夜間偏暗繼續延後至整體 GUI 完成後調查；高解析度畫稿仍只在 Session 暫存（屬 Phase 06 範圍）。
Git commit: 未建立（明確不自動 commit / push）。審查基準為 `2b5c5c8`。
Recommended next phase: 使用者人工驗收與 Codex 獨立複審；不開始 Phase 06。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 既有 8 項 PASS，新文字排版待人工重測；Visually verified: 既有 PASS，新文字排版待人工重測；Multiplayer-tested: 未執行。

## 2026-10-24 — Phase 05B.2 首次交付（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Smart Resampling, Preview, Undo/Redo, Flood Fill)
Status: **Phase 05B.2 PARTIAL — Waiting for manual acceptance and independent review**
Changes:
- 臨時角色調整：因 Codex 額度用盡，由使用者授權 Gemini／Antigravity 擔任唯一程式開發代理，Codex 暫停。
- 智慧轉換（FreehandResampler）：新增 Mode.SOFT 與 Mode.CRISP。Crisp 模式於 8x8 區塊中偵測具方向連續性之細線（span >= 4），以有界幾何平均 `a = min(max, min(2*a, sqrt(a*max)))` 補償輪廓 Alpha，RGB 嚴格不變，孤立雜點與完全透明像素嚴格不放大，禁止粗暴二值化。
- 煙火輸出預覽（CanvasPreview & CanvasEditorScreen）：實作 ViewMode（DRAW、CANVAS_32、FIREWORK），32x32 畫布帶有微棋盤透明底，Dust 粒子預覽使用真實 DustPattern 與粒子計數。預覽與儲存 100% 共用快取 CanvasData，杜絕公式不一致。
- 歷史復原／重做（Undo/Redo）：FreehandCanvas 實作上限 32 步有界歷史堆疊，筆畫與填色均以完整單次操作記錄，新操作自動截斷 Redo 分支，支援 Ctrl+Z / Ctrl+Y / Ctrl+Shift+Z 與介面按鈕。Cancel / Esc 不修改 ItemStack。
- 油漆桶填色（Flood Fill）：以迭代非遞迴 BFS 演算法（queue 容量 65,536）實作，永無 StackOverflowError，支援透明背景填色及橡皮擦清除填色，邊界與相同顏色自動防護。
- GUI 操作整合：右側工具面板整合調色盤、筆刷粗細滑桿、橡皮擦、填色、Undo、Redo、Mode（Soft/Crisp）切換、View（Draw/32x32/Dust）預覽切換、Cancel 及 Save；支援 B/E/F/M/P 等快捷鍵。
- 本地化：新增 lang/en_us.json 相關字串。
- 單元測試：新增 SmartResamplerTest（4 項）、擴充 FreehandCanvasTest（6 項）及 FreehandInputTest（1 項），涵蓋演算法、邊界、填色與歷史管理。
Build result: `.\gradlew.bat clean build --no-daemon` 成功（exit 0，耗時 27s，7 tasks executed）。產出 JAR `build/libs/drawfirework-1.0.0.jar`（104,876 bytes，SHA256: `B4693BE474B5B4F824F34AA09346AC038504EE9FB1CCE579CC1A15F64A3675C7`）。
Tests performed: JUnit **95/95 PASS**（0 failures, 0 errors, 0 skipped）；Phase 02 三組回歸全部 PASS（TimelineCheck、DustStyleCheck、BloomCheck）；`git diff --check` PASS。
Tests not performed: 遊戲內真人實際操作、不同視窗解析度／GUI Scale 人工視覺驗證、獨立審查（將另行安排）。
Known problems / follow-up: 原稿僅於 Session 暫存（屬 Phase 06 範圍）；Dust 夜間亮度待整體 GUI 完成後調查；獨立審查待安排。
Git commit: 未建立（明確不自動 commit / push）。HEAD 仍為 `862497d`。
Recommended next phase: 使用者人工驗收與 Phase 05B.2 獨立審查；不開始 Phase 06。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行。

## 2026-10-24 — Phase 05B.1 正式結案（歷史紀錄）

Phase: 05B.1 — High-Resolution Freehand Drawing
Status: **Phase 05B.1 — PASS / CLOSED**
Build result: 使用者在一般 PowerShell、`D:\minecraftmod` 執行 `.\gradlew.bat clean build --no-daemon`，回報 **BUILD SUCCESSFUL**。未提供退出碼、耗時與任務數，不作推定。
Tests performed: 使用者確認全部 JUnit 84/84 PASS、Phase 02 三組回歸 PASS；無格線自由手繪／筆刷／擦除及繪圖儲存與煙火播放人工 PASS；Antigravity 獨立唯讀審查 0 Critical／0 Major／0 Minor。本輪只依使用者提供結果更新文件，未重跑。
Known problems / follow-up: 極細線縮圖後變淡或輪廓模糊，留待智慧轉換品質改善；舊 Smiley 與手繪煙火夜間可見度不足，留待 GUI 完成後調查 Dust；高解析度原稿只保存在編輯 Session，永久保存屬 Phase 06。特殊伺服器情境及其他未實測項目不宣稱人工 PASS。
Git: 功能 Commit `49fd5ad2ae534f7bb9524474a92318a120b69e3d`；文件結案 Commit `c2c01e0` 為本輪起始 HEAD。未執行 Git add／commit／push。
Recommended next phase: Phase 05B.2（另依授權執行）；本次未開始。
## 2026-10-24 — Phase 05A 正式結案（最新）

Phase: 05A — In-Game Canvas Editor MVP
Status: **Phase 05A — PASS / CLOSED**
功能提交／本次起始HEAD：`51146068b1e7e12bb58667f2fcb5c3fb288380d1`。獨立唯讀審查：Critical 0、Major 0、Minor 0；GUI滑鼠事件修復、C2S資料驗證與伺服器授權、token／防重放／ItemStack狀態保護、不同尺寸畫布程式碼與單元測試均PASS。
驗證（依使用者提供的已確認結果，本次文件結案未重跑）：clean build PASS；JUnit 70/70 PASS；Phase 02三組回歸PASS。使用者實機確認可繪圖、儲存、右鍵播放自製煙火及clear；取消、重進世界、快捷鍵與舊功能操作亦回報基本正常。
接受但未經實機驗證的剩餘風險：編輯期間由外部強制切換Survival、外部更換手持物、編輯超過12,000 ticks後儲存，以及特殊矩形畫布的遊戲內呈現與儲存。這些有程式碼、單元測試及審查支持，但不記為實機PASS。
非阻塞待追蹤：偶發JVM啟動閃退，根因未確認；目前沒有證據顯示與Phase 05A直接相關，也沒有證據證明無關。未宣稱已修復。
文件結案從乾淨工作樹及上述HEAD開始；不建立commit、不push。Phase 05B及06未開始。

## 2026-10-24 — Phase 05A GUI Drawing Interaction Bugfix（歷史狀態；已由正式結案取代）

Phase: 05A — GUI Drawing Interaction Bugfix
Status: Phase 05A PARTIAL — Waiting for manual retest
Changes: 保留既有未提交05A全部工作。使用者回報GUI可開啟，但選色後畫布無可見變化，修復前因此BLOCKED；其他人工測試尚未執行，不作PASS或FAIL。具體根因為原CanvasEditorScreen三個滑鼠分支硬編碼button==0，但本機26.3 MOUSE_BUTTON_LEFT=1；改用具名常數，將直接相關的筆畫狀態及事件處理隔離成CanvasEditorInput，Screen實際委派，便於不啟動Minecraft的回歸測試。未變更網路／component／04C.2／renderer／工具鏈。
Build result: 最終clean build --no-daemon exit0，22秒、7 tasks executed。JAR build/libs/drawfirework-1.0.0.jar，87743 bytes，SHA256 9DDC04327242002F56695E27F5AA3F59067A72F1F5DB6DFFD2E6E8382F8AC8A7；不是release驗收通過宣稱。
Tests performed: 先保留舊button==0執行正式CanvasEditorInput的4個新測試，4/4 FAIL、exit1，重現阻塞；修正單一左鍵predicate後最終JUnit70/70 PASS、0 failures/errors/skipped；Phase02三組回歸exit0；最終git diff --check PASS。
Tests not performed: 本次沒有啟動Minecraft。使用者先前只確認GUI開啟；修復後實際滑鼠／按鈕／GUI Scale／Esc取消、保存／重開／物品播放／重載及安全人工驗收均未測。本輪自動測試不代表這些通過。
Known problems: 修復後的真人繪圖尚待重測；上一版純CanvasEditorModel測試繞過左鍵分支，不能證明完整互動。GUI修復前FAIL保留，不能追溯改為PASS。最短重測及調查證據見PHASE_05A_ACCEPTANCE.md最新補充。
Git commit: 未建立，明確不commit／push。HEAD仍527db03f671ba1e7b878009202733c302fa11158，index空白。起始已有21檔未提交05A變更；本次只改Screen、6份相關docs，新增CanvasEditorInput及CanvasEditorInputTest，累計23檔。未還原其他05A變更。
Recommended next phase: 只做05A繪圖人工重測，之後才繼續原驗收／獨立唯讀審查；沒有05B或新階段。
GUI handoff: 本輪文件及最終範圍查核後停止寫入，無背景遊戲／建置／監看。

Compiled: PASS；Unit-tested: PASS；Runtime-tested: 修復後未執行（修復前使用者確認GUI可開，繪圖FAIL）；Visually verified: 修復後未執行；Multiplayer-tested: 未執行。

以下05A初次交付與66測試結果保留作歷史，不取代本節最新狀態與70測試結果。

## 2026-10-23 — Phase 05A 交付（歷史紀錄）

Phase: 05A — In-Game Canvas Editor MVP
Status: Phase 05A PARTIAL — Waiting for manual acceptance and independent review
Changes: 原生26.3 Screen、可重綁G鍵、8色／透明橡皮擦／快速拖曳補點／取消／Esc；16×16新圖及既有1..32矩形完整載入。server開啟一次性工作階段，C2S保存經重新驗證才set原Canvas Component，回饋真實結果；右鍵直接走既有04C.2。Phase04D在PROJECT_PLAN標為SKIPPED / NOT REQUIRED，無額外開發。
Build result: 最終 clean build --no-daemon PASS，30秒、7 tasks executed；同既有工具鏈，沒有升級。JAR build/libs/drawfirework-1.0.0.jar，86811 bytes，SHA256 1A90D17715AF4FAB7CAA8CBF46F395815072D4EF0535825C6D54CA7AF055DAC6。這是待驗收開發產物，不代表V1.0已發布。
Tests performed: JUnit 66/66 PASS（原53＋新13），0 failures/errors/skipped；Phase02三組source-launcher回歸exit0；git diff --check最終PASS。正式產品模型／授權閘門／payload codec直接受測。
Tests not performed: 本輪未啟動Minecraft，Screen／按鍵／真實ServerPlayer及ItemStack／C2S handler／物品同步／存檔重載／視覺／聲音／效能／多人／Antigravity獨立審查未執行。人工步驟及逐項預期見PHASE_05A_ACCEPTANCE.md。
Known problems: 以上runtime邊界待驗；編輯期限12000 server ticks、原始verify命令仍僅驗Smiley；Dust透明度與採樣限制保留；Gradle既有棄用警告。沒有宣稱畫質／FPS或真人網路拒絕測試通過。
Git commit: 未建立，等待人工驗收及獨立審查；實際HEAD仍527db03f671ba1e7b878009202733c302fa11158。起始工作樹乾淨；本輪僅21個明列檔案，index空白，未add／commit／push。精確暫存候選清單見PHASE_05A_ACCEPTANCE.md。
Recommended next phase: 本階段人工驗收與獨立唯讀審查；沒有開始05B，不視為授權下一階段。
GUI handoff: Codex本次文件及最終差異查核完成後停止寫入；無背景遊戲／建置／監看工作。Antigravity僅審查固定HEAD＋21檔差異，完成審查後再由使用者授權處理。

Compiled: PASS；Unit-tested: PASS；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行。

以下為歷史階段紀錄，不將04C的PASS延伸為05A的新功能已通過人工驗收。

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

## 2026-10-23 — Phase 04C.2 交付（歷史）

Phase: 04C.2 — Server-Authorized Firework Playback
Status: PARTIAL — 等待人工驗收與獨立審查
Changes: test_firework 真實 ItemStack → server 驗證 → 本人 S2C → 共用 Canvas/Dust A–E。伺服器決定中心、10 tick 簡單重複閘門；client Fixed/Bloom130/三粒/原時序與預算，clear 替換保持。沒有 04D。
Build result: 最終 clean build --no-daemon exit 0，15s，7 tasks executed；工具核准沙箱外執行。
Tests performed: 53/53 JUnit（47 原有＋6 新增），三組 Phase 02 回歸 exit 0；diff check PASS。產品 payload、UseGate、實際共用轉換與 DustPlane 入口受測。
Tests not performed: 真實網路 handler／物品使用、世界切換、聲音／視覺、多人接收範圍與獨立審查。JUnit 不冒充完整 Minecraft runtime。
Known problems: runtime 接線仍待人工；非整合伺服器 client 會拒收本原型；沒有多人支援宣稱。既有 Gradle 棄用警告保留。
Git commit: 基準 1d4ac78，起始工作樹乾淨；本輪未提交、未 push；精確檔案與提交訊息見 TEST_RESULTS。
Recommended next phase: 本階段人工與獨立審查；不開始 04D。
GUI handoff: Codex 完成文件後停止寫入，無背景遊戲／監看工作；可對固定差異進行唯讀審查。

Phase 04C.1 正式 PASS：依使用者本輪確認，1d4ac78、47/47 JUnit、三組回歸及 Creative／Survival／ItemStack 診斷／存檔還原／滿背包人工驗收均 PASS；Antigravity PASS，0 Critical/Major/Minor。本機沒有該輪審查全文，不捏造；下方待驗收為歷史。

## 2026-10-23 — Phase 04C.1 交付（歷史）

Phase: 04C.1 — Data Component & ItemStack Foundation
Status: PARTIAL — 等待人工驗收與獨立審查
Changes: 新增 drawfirework:canvas component、受限 persistent／stream codecs、drawfirework:test_firework 物品、Creative-only 伺服器授予／verify 指令、最小資源與 9 項邊界 JUnit。重用 CanvasSerialization 格式與解碼，CanvasData 全域 256 上限不變。無動畫接線、無事件封包、無 04C.2。
Build result: 最終 clean build --no-daemon exit 0，16s、7 tasks executed。原沙箱 daemon 無法連線；經工具核准於沙箱外執行，沒有修改工具鏈。
Tests performed: 47/47 JUnit（0 failure/error/skipped）；三組 Phase 02 回歸各 exit 0；git diff --check。產品 Codec 的 JsonOps／NbtOps、32 邊界、超限／畸形／截斷拒絕與 4099 bytes 網路上限通過。
Tests not performed: 遊戲啟動／物品外觀／授予／使用／背包滿／存檔重載、live ItemStack diagnostics、獨立審查及多人。普通 ItemStack JUnit 曾失敗於 registry bootstrap，已改為遊戲內 verify，不能將其列為通過。
Known problems: 真正 ItemStack 往返與初始化生命週期仍待遊戲驗證；persistent NBT 已建樹的外層記憶體限制由遊戲讀取器負責，產品 Codec 在像素陣列配置前拒絕超限。Gradle 既有棄用警告保留。
Git commit: 起始 HEAD f4785b3（04B 功能 6569b56），起始工作樹乾淨。本輪未提交，未嘗試寫 Git index、未 push；精確檔案及一般 PowerShell 提交方式見 TEST_RESULTS。
Recommended next phase: 完成本階段人工驗收及 Antigravity 唯讀審查；04C.2 未授權，不開始。
GUI handoff: Codex 完成本次交付後停止寫入，無背景遊戲或監看工作。Antigravity 僅唯讀審查 f4785b3 加本輪列明差異。

## 2026-10-23 — Phase 04B 正式結案（歷史，04B 仍為 PASS）

Phase: 04B — Dynamic Canvas Presets & Renderer Integration
Status: PASS
Changes: 本輪僅更新 PROGRESS、TEST_RESULTS、PROJECT_PLAN；沒有修改原始碼、測試、依賴或審查報告。04C 僅唯讀研究，規劃見 PROJECT_PLAN 最新段落，未授權實作；04D 未開始。
Build result: 沿用 6569b56 對應既有 clean build PASS（exit 0），本輪未重跑。
Tests performed: 既有 38/38 JUnit、三組 Phase 02 回歸 PASS；本輪讀取既有 XML 核對 38 tests／0 failures／0 errors。使用者於本次對話確認最新人工驗收 5/5 PASS，及 Antigravity 獨立唯讀審查 PASS、Critical／Major／Minor 均無。逐項結果見 TEST_RESULTS。
Tests not performed: 本輪未重新執行建置、JUnit、遊戲或視覺測試；未量測效能、未測多人、未執行任何 04C 原型。
Known problems: 保留 Dust alpha 大小近似、隨機亮度與 Fixed 側視透視限制；沒有 frame time／FPS 結論。docs/reviews 尚無 04B 完整審查報告；本輪依使用者明確確認補登結果，不捏造報告全文。
Git commit: 功能基準 6569b56（已核對 HEAD）；本輪文件未提交，未嘗試 Git 寫入、未 push。建議提交訊息：docs: close phase 04b and outline phase 04c architecture。
Recommended next phase: 使用者批准後進行 04C.1；04C.2 另行授權。規劃不代表授權。
GUI handoff: Codex 完成本輪文件後停止寫入，無背景修改；Antigravity 維持唯讀。

以下 04B 交付段落是歷史紀錄，其 PARTIAL、未提交與待人工驗收敘述由本節取代，舊提交腳本勿再用於本輪文件提交。

## 2026-10-23 — Phase 04B 交付（歷史）

Phase: 04B — Dynamic Canvas Presets & Renderer Integration
Status: PARTIAL — 等待人工驗收及獨立審查
Changes: DevCanvases 共用 Smiley 幾何並提供 ORIGINAL／WARM／COOL；HeartPrototype 增加兩個 supplier 入口；新增 DevCanvasesTest 5 項；更新五份專案文件。所有依賴、預設大小、朝向、Bloom、A–E、三粒模式、預算及 downsampler 不變。
Build result: clean build --no-daemon exit 0（27s、7 tasks executed）；首次沙箱內嘗試無法連接 daemon，放寬沙箱後同命令成功，沒有修改設定。
Tests performed: 38 JUnit 全數通過（原 33＋新 5）；三組 Phase 02 source-launcher 回歸各 exit 0；diff check 通過。接線靜態核對：同一 canvasNode/startCanvas/start/tickDust，start clear 後才指派新 pattern/plane，既有清理路徑未修改。
Tests not performed: 04B 遊戲指令／粒子引擎、Launch/Hold/Dispersion 實際替換、clear／重複觸發／世界切換、視覺與效能量測、多人。
Known problems: Dust 非精確 alpha／RGB 顯示；單元測試不能證明指令與粒子引擎運行；既有 Gradle 棄用警告仍存在。沒有確認的新功能故障，人工驗收仍必要。
Git commit: 未建立，刻意保留未提交變更供使用者審查與提交；沒有嘗試 git add/commit/push，沒有刪除鎖檔。
Recommended next phase: 先完成人工驗收及 Antigravity 獨立唯讀審查；04C 未開始，本輪不授權。
GUI handoff: Codex 完成本次交付後停止寫入；無遊戲／監看背景工作。Antigravity 僅唯讀審查此差異。

### 正式階段與 04A 收尾補登

04A 正式 PASS；04B 執行時生成／預設選擇／切換／渲染整合；04C Data Component 與最小測試煙火物品；04D 待規劃。04A 審查與人工通過依使用者 2026-10-23 明確確認：Review Fix 01 已完成，Antigravity 獨立複查 PASS，1 Major／2 Minor 全 CLOSED，33 JUnit／三組回歸／5/5 遊戲回歸通過；對應 1243af9、9040a35、8246713。本機尚無最新獨立複查全文，不補造；下方 04A PARTIAL、未提交與待回歸敘述均為歷史，由本節取代。

### 新指令與選項

```text
/drawfirework canvas smiley_warm
/drawfirework canvas smiley_cool
/drawfirework canvas smiley_warm 150 192 plain fixed
/drawfirework canvas smiley_cool 125 128 plain dynamic
```

兩個新入口都支援既有次序 `[100|125|150] [128|192|266] [plain] [dynamic|fixed]`；每項可省略但不得換序。預設 100%／266／三粒微偏移／Fixed。舊 smiley/dense/grid/rect、heart、clear 不變。未知預設如 smiley_neon 沒有註冊，應由 Brigadier 拒絕；遊戲內錯誤顯示尚待驗收。

最短人工驗收步驟見 TEST_RESULTS 本輪段落；進入遊戲：

```powershell
Set-Location D:\minecraftmod
$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'
.\gradlew.bat runClient
```

### 精確提交建議（人工審查後由一般 PowerShell 執行）

```powershell
Set-Location D:\minecraftmod
$phase04bFiles = @(
  'src/main/java/com/drawfirework/firework/DevCanvases.java'
  'src/main/java/com/drawfirework/client/HeartPrototype.java'
  'src/test/java/com/drawfirework/firework/DevCanvasesTest.java'
  'docs/PROJECT_PLAN.md'
  'docs/PROGRESS.md'
  'docs/DECISIONS.md'
  'docs/TEST_RESULTS.md'
  'docs/API_COMPATIBILITY.md'
)
git status --short
git add -- $phase04bFiles
if ($LASTEXITCODE -ne 0) { throw 'git add failed; stop without deleting locks' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git diff --cached --stat
git diff --cached
git commit -m "feat: add phase 04b runtime smiley palette presets"
if ($LASTEXITCODE -ne 0) { throw 'git commit failed' }
git status --short
```

提交本身不代表 04B PASS。不 push。若工作樹已出現其他人的改動，先重新核對，勿夾帶；index.lock 權限錯誤時停止，不強制修復或刪除 .git。

## 2026-10-23 — Phase 04A Review Fix 01（最新）

Phase: 04A Review Fix 01
Status: PARTIAL（核心視覺設計已獲使用者驗收，但最新預設入口與完整生命週期尚待最終遊戲回歸）
Changes: DustPlane 抽取並接入真實執行期；CanvasOptions 搬出；相機測試改驗產品狀態／座標；更正文件。完整覆蓋與限制見 [修正紀錄](reviews/phase-04a-review-fix-01.md)。
Build result: clean build --no-daemon exit 0（29s、7 tasks executed）。
Tests performed: 33 JUnit 全通過；Phase 02 三組回歸 exit 0；Fixed 每 tick 更新之 mutation 被 4 個測試攔下，已還原；diff check 通過。
Tests not performed: 本輪遊戲／視覺／世界切換／多人；精確人工清單見修正紀錄，不宣稱人工 PASS。
Known problems: JUnit 未執行 Minecraft 指令註冊／粒子引擎，呼叫鏈之外的執行期仍須人工驗收。
Git commit: 基準已有 1243af9；本次 git add 因 .git/index.lock Permission denied 失敗，未提交、未 push；一般 PowerShell 精確提交命令見上述修正紀錄。
Recommended next phase: 此修正獨立審查及人工回歸；未開始 04B。
GUI handoff: Codex 完成本輪後停止寫入；Antigravity 可唯讀審查固定差異。

# 最新交接 — 2026-10-23 Phase 04A Finalization

Phase: 04A — CanvasData → Dust Renderer 整合（收尾完成，等待獨立審查）
Status: PARTIAL（核心視覺設計已獲使用者驗收，但最新預設入口與完整生命週期尚待最終遊戲回歸）
Changes:
1. `HeartPrototype.java`:
   - Canvas 動態圖案正式採用 **Fixed World Plane** 作為預設方向（在 tick 20 爆炸瞬間鎖定世界朝向，Formation/Hold 期間維持固定物理平面，不隨玩家鏡頭每 tick 旋轉）。
   - 保留 `dynamic` 參數作為開發對照模式：`/drawfirework canvas <name> [scale] [budget] [plain] [dynamic|fixed]`。
   - 保留 Phase 02 `/drawfirework heart` 既有視覺行為，完全不受影響。
   - 保留 Bloom 130%、Hold 14 ticks、五階段動畫與 800 核心粒子預算上限。
   - 正式 Canvas 預設保留三粒微偏移（BLOOM_130）；`plain` 繼續作為診斷對照組；150% 整體尺寸保留為可選配置。
2. `CameraOrientationStabilityTest.java`:
   - 新增 Canvas 預設 Fixed 驗證測試、Dynamic 明確選擇測試、相機旋轉與玩家平移之免疫性測試（8 個單元測試全數通過）。
3. 專案文檔更新：
   - `AGENTS.md`：記錄 §16 限時授權；收尾後寫入權交回 Codex。
   - `DECISIONS.md`：更正先前將歪斜問題「100% 歸因於時序競爭」的過度肯定敘述，記錄玩家位移與視角綜合影響；記錄使用者選定 Fixed、Smiley 150% 尺寸偏好及 Plain 測試成果。
   - `PROJECT_PLAN.md`、`TEST_RESULTS.md`：記錄 35 個 JUnit 測試全過、Phase 02 動畫回歸全過、最新 JAR 資訊。

Build result: `clean build --no-daemon` exit 0（20s，7 tasks executed），JAR `drawfirework-1.0.0.jar` SHA256 `DB04A04A61806E9372F102A4AD3C468C6A0369D1A009FDD479A1D661BD774387`。
Tests performed: 35 個 JUnit 全數通過（CanvasTest 15, CameraOrientationStabilityTest 8, DustGeometryDiagnosticsTest 6, DustPatternTest 6）；三個 Phase 02 動畫回歸 exit 0。
User manual verification:
- Fixed World Plane 穩定性通過（正式選定為預設）。
- Fixed Smiley 明顯優於 Dynamic。
- Smiley 150% 尺寸較佳（但粒子稍分散，留待後續階段微調）。
- Grid Plain 與 Rect Plain 整齊度提升（微偏移保留在正式預設，Plain 保留為診斷）。
- Dense 266 點效能正常，無卡頓。
- Phase 02 愛心效果正常。
Tests not performed: 最新預設入口與完整 A–E、clear、世界切換的最終遊戲回歸；多人伺服器同步（屬 Phase 09/10 範圍）。
Git commit: `1243af9`（已核對）；未 push。下方舊提交命令僅為歷史紀錄，不應重做。
GUI handoff: Antigravity 本輪工作完成，已停止寫入。**寫入權全面交回 Codex。**

---

### 指令速查（正式預設與對照）

- **正式預設（Fixed World Plane，世界固定平面）**：
  ```
  /drawfirework canvas smiley
  /drawfirework canvas dense
  /drawfirework canvas grid
  /drawfirework canvas rect
  ```
- **開發對照模式（Dynamic，動態面向相機）**：
  ```
  /drawfirework canvas smiley dynamic
  /drawfirework canvas grid dynamic
  ```
- **可選尺寸與診斷模式**：
  ```
  /drawfirework canvas smiley 150
  /drawfirework canvas grid plain
  /drawfirework canvas grid plain dynamic
  ```
- **Phase 02 愛心回歸**：
  ```
  /drawfirework heart
  ```

---

### 建議的 Git Commit 清單（涵蓋 Phase 04A 全部子階段）

請使用者在一般 PowerShell 中執行下列命令完成 Phase 04A 成果提交：

```powershell
Set-Location D:\minecraftmod
$phase04aFiles = @(
  'AGENTS.md'
  'docs/PROJECT_PLAN.md'
  'docs/PROGRESS.md'
  'docs/DECISIONS.md'
  'docs/TEST_RESULTS.md'
  'docs/API_COMPATIBILITY.md'
  'src/main/java/com/drawfirework/client/HeartPrototype.java'
  'src/main/java/com/drawfirework/client/DustStyle.java'
  'src/main/java/com/drawfirework/firework/DustPattern.java'
  'src/main/java/com/drawfirework/firework/DevCanvases.java'
  'src/test/java/com/drawfirework/firework/CameraOrientationStabilityTest.java'
  'src/test/java/com/drawfirework/firework/DustGeometryDiagnosticsTest.java'
  'src/test/java/com/drawfirework/firework/DustPatternTest.java'
)
git add -- $phase04aFiles
if ($LASTEXITCODE -ne 0) { throw 'git add failed' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git diff --cached --stat
git commit -m "feat: complete phase 04a canvas data to dust renderer with fixed world plane"
if ($LASTEXITCODE -ne 0) { throw 'commit failed' }
git status --short --branch
```

# 前次交接 — Phase 04A.2

Phase: 04A.2 — Camera Orientation & Geometry Stability
Status: PARTIAL（Compiled、Unit-tested PASS；Runtime／視覺未驗證，等待使用者人工測試 Design A vs B）
Changes:
1. `HeartPrototype.java`:
   - 擴充 `CanvasOptions` 與指令結構，支援 `fixed` 參數：`/drawfirework canvas <name> [scale] [budget] [plain] [fixed]`。
   - 實作 Design A（`fixed`）：在 tick 20 爆炸瞬間鎖定世界朝向，Hold 期間不隨相機旋轉，解決轉頭凍結在 45° 的現象。
   - 預設（未加 `fixed`）100% 保持既有 Design B（動態面向鏡頭）及 Phase 02 愛心效果。
2. `CameraOrientationStabilityTest.java`:
   - 新增 6 個嚴密數學單元測試，全數通過：證實相機 Euler YXZ 旋轉無累積漂移、0°→45°→0° 在 Hold 期間嚴格可逆、等距保持比例、證明 45° 歪斜是「轉回 0° 時已跨入 Dispersion 階段」的必然物理凍結現象。
3. `AGENTS.md`、`DECISIONS.md`、`PROJECT_PLAN.md`、`TEST_RESULTS.md` 同步記錄授權、7 項排查結論與測試數據。
4. 未修改 Bloom 130%、Hold 14 ticks、五階段時間、800 核心粒子上限、CanvasDownsampler、預設顯示比例、三粒偏移參數、工具鏈。

Build result: `clean build --no-daemon` exit 0（20s，7 tasks executed），JAR `drawfirework-1.0.0.jar` SHA256 `54BC356A4DF802BFAAF5343650F9D6C11725D1E144BDE97E00AAD1AEBC3DB8FF`。
Tests performed: 33 個 JUnit 全數通過（CanvasTest 15, CameraOrientationStabilityTest 6, DustGeometryDiagnosticsTest 6, DustPatternTest 6）；三個 Phase 02 動畫回歸 exit 0。
Tests not performed: 遊戲內真實視覺比較、多人測試。
Confirmed findings（詳見 DECISIONS.md）:
- 0° → 45° → 0°「有時呈 45° 歪斜」的根本原因已確認：**時序競爭**。從爆炸到 Hold 結束僅 22 ticks（1.1 秒）。若使用者轉動視角並在 tick 42（Hold 結束）之後才轉回，煙火已進入 Dispersion（Phase D），粒子轉為世界自由物理散射，不再跟隨鏡頭。因此使用者在 tick 45-50 轉回 0° 時，看到的正是凍結在 45° 的擴散星雲。在 tick 42 之前轉回則 100% 可逆。
- 使用 `fixed` 模式（Design A）可徹底消除此競爭，使煙火成為世界中的固定物理物件。

Git commit: 未建立；未 push。提交清單（04A + 04A.1 + 04A.2 累計）：
- `AGENTS.md`
- 5 份 docs (`PROGRESS.md`, `DECISIONS.md`, `PROJECT_PLAN.md`, `TEST_RESULTS.md`, `API_COMPATIBILITY.md`)
- `src/main/java/com/drawfirework/client/HeartPrototype.java`
- `src/main/java/com/drawfirework/client/DustStyle.java`
- `src/main/java/com/drawfirework/firework/DustPattern.java`
- `src/main/java/com/drawfirework/firework/DevCanvases.java`
- `src/test/java/com/drawfirework/firework/CameraOrientationStabilityTest.java`
- `src/test/java/com/drawfirework/firework/DustGeometryDiagnosticsTest.java`
- `src/test/java/com/drawfirework/firework/DustPatternTest.java`
GUI handoff: 本輪 Antigravity 寫入工作完成，已停止寫入；角色權限交回 Codex。

---

### 遊戲內 Design A (fixed) vs Design B (動態) 比較指令

請在單人世界執行（`$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; .\gradlew.bat runClient`）：

1. **驗證固定世界平面（Design A，不隨視角旋轉，解決 45° 卡死問題）**：
   ```
   /drawfirework canvas grid plain fixed
   /drawfirework canvas rect plain fixed
   /drawfirework canvas smiley 150 plain fixed
   ```
   *操作*：發射後立即大幅轉動滑鼠（0° → 45° → 0°，甚至 90°）。
   *預期*：煙火在世界中像一座靜止雕塑或看版，不會隨你的準星旋轉；轉向 45° 看到的是斜角透視，轉回 0° 必定正對，且進入 Dispersion 時永遠沿原始平面散開，絕不發生 45° 殘留歪斜！

2. **驗證動態鏡頭跟隨（Design B，既有預設）**：
   ```
   /drawfirework canvas grid plain
   /drawfirework canvas rect plain
   /drawfirework canvas smiley 150 plain
   ```
   *操作 A（快速轉動）*：發射後 1 秒內快速擺動滑鼠（0° → 45° → 0°）。
   *預期*：在 Hold 結束前轉回，圖案會正確恢復 0°。
   *操作 B（刻意超時）*：發射後轉向 45° 保持 1.5 秒，等看到火花開始往外散開（Dispersion）後再轉回 0°。
   *預期*：重現先前的現象——圖案在 45° 處解體擴散，轉回 0° 仍看到 45° 的散射殘骸（證明確實為時序跨入 Dispersion 所致）。

3. **原始愛心回歸確認**：
   ```
   /drawfirework heart
   ```
   *預期*：維持 Phase 02 Bloom 130% 效果不變。

# 前次交接 — Phase 04A.1

Phase: 04A.1 — Pattern Geometry & Visual Quality Diagnostics
Status: PARTIAL（Compiled、Unit-tested PASS；Runtime／視覺／效能未驗證，等使用者人工比較；不宣稱視覺 PASS）
Changes: `DustPattern` 新增 `scaled(1.0–1.5)` 與 `fromCanvas(canvas, copies, maxSamples)`；`DevCanvases` 新增 `grid`、`rectangle`；`DustStyle` 新增診斷用 `BLOOM_130_PLAIN`（不影響既有預設）；`HeartPrototype` 新增指令選項與 CPU 計時日誌；新增 `DustGeometryDiagnosticsTest`；AGENTS.md §14 記錄限時授權。Heart、Bloom 130%、hold 14、五階段、降採樣演算法、粒子上限、工具鏈均未改。
Build result: 最終 `clean build --no-daemon` exit 0；前兩次失敗均為新測試檔自身錯誤（已修正，記於 TEST_RESULTS）。27 JUnit 通過；三個 Phase02 動畫檢查 exit 0。
Confirmed findings（詳見 DECISIONS.md）: 2D→3D 映射、中心、長寬比、取整均正確；Smiley/Grid/Rect 為無損全像素取樣；三粒偏移為絕對 0.14 方塊，offset/pitch 達 0.45（smiley）／0.91（dense），為已確認的主要放大因素；Dense 降採樣為不均勻格點（設計行為，未改）。透視／20Hz 更新／Dust 粒子重疊／尺寸偏小僅為推測。
Tests not performed: 遊戲內視覺、FPS/frametime、多人。
Known problems: Dense 卡頓原因（生成／Hold／GPU）未知；`[04A.1 timing]` 僅 CPU 端；半透明仍為縮小粒子近似。
Git commit: 未建立；未 push。提交清單：AGENTS.md、docs 5 份、`HeartPrototype.java`、`DustStyle.java`、`firework/DustPattern.java`、`firework/DevCanvases.java`、`test/.../firework/DustPatternTest.java`、`test/.../firework/DustGeometryDiagnosticsTest.java`（04A 與 04A.1 合併於工作樹，含 04A 的未提交變更）。
GUI handoff: Antigravity 為本輪唯一寫入者，已停止；寫入權回到 Codex 預設。

遊戲內比較指令（單人世界，`$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; .\gradlew.bat runClient`；格式 `canvas <smiley|dense|grid|rect> [100|125|150] [128|192|266] [plain]`，選項依序可省略）：
```
/drawfirework heart
/drawfirework canvas grid
/drawfirework canvas grid plain
/drawfirework canvas grid 125
/drawfirework canvas grid 150 266 plain
/drawfirework canvas rect
/drawfirework canvas rect plain
/drawfirework canvas rect 150
/drawfirework canvas smiley
/drawfirework canvas smiley plain
/drawfirework canvas smiley 125
/drawfirework canvas smiley 150
/drawfirework canvas smiley 150 266 plain
/drawfirework canvas dense 128
/drawfirework canvas dense 192
/drawfirework canvas dense 266
/drawfirework canvas dense 125 192
/drawfirework canvas dense 150 192 plain
```
觀察重點：(1) grid/rect 的橫豎線是否筆直、plain 是否明顯較整齊（→偏移是主因）；(2) 正面與斜 45° 觀察差異（→透視／朝向）；(3) 125/150% 是否較易辨識；(4) dense 128/192/266 的卡頓發生在爆炸瞬間、Hold 或持續；請附 `run/logs/latest.log` 的 `[04A.1 timing]` 行（CPU 端數字，非 FPS；可另用 F3 回報 FPS）。

# 前次交接 — Phase 04A

Phase: 04A — CanvasData → Dust Renderer 整合（Phase 04 拆為 04A–04D，本次僅 04A）
Status: PARTIAL（Compiled、Unit-tested PASS；Runtime-tested／Visually verified 未執行，需使用者遊戲內確認）
Changes: 新增純 Java `firework.DustPattern`（樣本座標+RGB+alpha 尺寸，800 核心預算檢查，HEART 回歸常數）與 `firework.DevCanvases`（smiley/dense 開發用固定畫布）；`HeartPrototype` 改由 `DustPattern` 餵入 Dust 核心，新增開發指令 `/drawfirework canvas smiley|dense`（固定 Bloom 130%）；heart 及所有既有指令輸出不變。`AGENTS.md` 加第 13 節：僅 04A 由 Antigravity 寫入。未改 DustStyle、FireworkTimeline、build.gradle、工具鏈。無 Data Component、物品、封包、GUI、草稿。
Build result: `clean build --no-daemon` exit 0（17s，7 tasks executed），JAR `drawfirework-1.0.0.jar` SHA256 `46A8D15B…BE52371`。
Tests performed: 21 個 JUnit 全通過（CanvasTest 15 + DustPatternTest 6）；含 Phase 02 heart 63 點/189 粒逐點回歸、透明策略、800 預算、長寬比、確定性、非法輸入。
Tests not performed: 遊戲內 runClient、canvas 指令畫質、heart 視覺回歸、效能、多人。
Known problems: 半透明以「較小不透明 Dust」近似，非真實 alpha；低預算混疊沿用 Phase 03；dense 798 粒的遊戲內效能未量測；開發指令仍限單人。
Git commit: 未建立（等待使用者決定；未 push）。差異：AGENTS.md、HeartPrototype.java、新增 firework/DustPattern.java、firework/DevCanvases.java、test/firework/DustPatternTest.java、5 份 docs。
Recommended next phase: 使用者先執行下列遊戲內檢查並由 Codex/Antigravity 審查 04A；之後才授權 04B。
GUI handoff: 本輪 Antigravity 為唯一寫入者（AGENTS.md §13），已停止寫入；寫入權回到預設（Codex）。

遊戲內檢查（單人世界、夜間、`$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; .\gradlew.bat runClient`）：
1. `/drawfirework heart` — 應與 Phase 02 Bloom 130% 完全相同（訊息 `Dust 130%: A20 / B8 / C14 / D20 / E14 | 3 particles/point`）。
2. `/drawfirework canvas smiley` — 訊息顯示樣本數×3；應見黃色臉、藍眼、紅嘴，外圈青色點較小，邊框無粒子；五階段相同。
3. `/drawfirework canvas dense` — 訊息 `266 samples x 3 = 798 core particles`；彩虹漸層可辨識；回報是否卡頓。
4. 播放中 `/drawfirework clear` 及退出世界後無殘留。

# 前次交接 — 2026-10-23 Phase 03

Phase: 03 — Canvas Data Model & Downsampling
Status: PASS（本次授權的資料/演算法/編譯與單元測試範圍；等待Antigravity獨立審查）
Changes: 新增不可變CanvasData、版本化有界JSON/persistent Codec、CanvasDownsampler、ParticleBudget及標準Gradle/JUnit測試。build.gradle僅加test依賴/設定；Minecraft26.3與既有工具鏈、HeartPrototype、Dust/Bloom130及五階段均不變。無Phase04接線、GUI、草稿檔案管理、網路、物品、多人。
Build result: 最終clean build --no-daemon exit0，31s，7 tasks executed；test確實執行，非NO-SOURCE。
Tests performed: 15個JUnit通過（0失敗/錯誤/跳過）、三個既有動畫source-launcher回歸通過、JAR內容及SHA256、差異/版本範圍查核。最大圖本機診斷平均2.536815ms/次、41939 allocated bytes/op；不是遊戲效能。
Tests not performed: 本輪Runtime-tested、Visually verified、Multiplayer-tested未執行；Compiled及Unit-tested PASS。新資料尚未接入煙火，不能宣稱任意畫布遊戲畫質合格。
Known problems: 低預算可能遺失小輪廓/稀有色與棋盤格混疊；既有Dust opaque需Phase04決定半透明映射。JSON入口有界，但未來讀檔須另限制讀取量；Codec不限制外部parser先前配置。原Phase02清理/效能未測項保持原紀錄。
Git commit: 未建立；起始HEAD7477eb8，工作樹乾淨。明確11檔git add失敗：Unable to create D:/minecraftmod/.git/index.lock: Permission denied。未暫存/commit/push，不刪lock或放寬Git權限重試；這是檔案權限錯誤，非自動審核拒絕。下方提供使用者命令。本輪候選11檔：4個canvas類、1個CanvasTest、build.gradle與5份docs；不混入其他階段或產物。
Recommended next phase: Antigravity唯讀審查Phase03，取得新授權後才進Phase04。建議Phase04以小型轉接層使用樣本/世界尺寸/顏色，明確alpha及副本預算，不必重構時間/Bloom管線。
GUI handoff: 本輪Codex為唯一已知寫入者，未觀察到外部變動；收尾後停止寫入，沒有啟動遊戲、watch或背景修改。Antigravity對HEAD加本輪11檔差異審查；使用者提交後改以新commit為基準。既有審查報告未修改。

800為核心粒子上限，3副本採樣最多266點=798粒；原始畫布像素最多65536，裝飾獨立200。63點/189粒只是舊固定愛心數值。所有細節與限制見DECISIONS/API_COMPATIBILITY/TEST_RESULTS本輪節。

使用者可在一般PowerShell檢查並提交（不push；任何步驟失敗即停）：

```powershell
Set-Location D:\minecraftmod
$phase03Files = @(
  'build.gradle'
  'src/main/java/com/drawfirework/canvas/CanvasData.java'
  'src/main/java/com/drawfirework/canvas/CanvasSerialization.java'
  'src/main/java/com/drawfirework/canvas/CanvasDownsampler.java'
  'src/main/java/com/drawfirework/canvas/ParticleBudget.java'
  'src/test/java/com/drawfirework/canvas/CanvasTest.java'
  'docs/PROJECT_PLAN.md'
  'docs/PROGRESS.md'
  'docs/DECISIONS.md'
  'docs/API_COMPATIBILITY.md'
  'docs/TEST_RESULTS.md'
)
git add -- $phase03Files
if ($LASTEXITCODE -ne 0) { throw 'git add failed' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git diff --cached --stat
git commit -m "feat: add phase 03 canvas model codecs and downsampling"
if ($LASTEXITCODE -ne 0) { throw 'commit failed' }
git status --short --branch
```

以下保留歷史紀錄；舊「Phase03未授權」由本輪明確授權及此節取代。

# 最新交接 — 2026-10-23 Phase 02 Finalization

Phase: 02系列（02、02.5、02.6）最終收尾，實作凍結
Status: PARTIAL（最終視覺選型PASS；清理邊界與效能完整驗收未執行，不能宣稱全項PASS）
Changes: 使用者正式選Bloom130；本輪程式只將HeartPrototype兩處預設由115改130。heart/heart dust均走130，normal/bloom115/bloom130/legacy/image/clear比較入口全部保留。不新增粒子或改動時間/軌跡/顏色/座標。14tick Hold、A20/B8/C14/D20/E14、4tick Bloom沿用。
Build result: 最終clean build --no-daemon exit0，13s，5 tasks executed；26.3及工具鏈未改。
Tests performed: 三個獨立Java邏輯測試exit0；遮罩實際計數63、核心63×3=189、裝飾最多19、保守同時上界208；源碼指令鏈及javap確認預設130與比較配置存在；git diff --check通過。Gradle test NO-SOURCE。
Tests not performed: 本輪未啟動遊戲；新預設alias尚未單獨遊戲重測。使用者已遊戲驗證相同bloom130配置及五階段，但clear/世界切換/相機失效/密集重複/逐項音效與frame time/GC仍缺明確驗收。多人不在本階段。
Known problems: 使用者仍見輕微稀疏感、部分斜角圖案視覺變形；根因未定位，列未來研究，不在本輪增加粒子或重構。Dust縮小替代alpha淡出、原版引擎可能剔除粒子與scale影響碰撞盒之限制保留。
Git commit: 收尾提交結果及精確命令見本節後續Git記錄。開始HEAD872bd52、暫存區空；6個已追蹤修改檔與5個未追蹤階段檔完整保留，候選提交11檔。
Recommended next phase: Antigravity唯讀審查本固定差異；Phase03未授權，未開始。
GUI handoff: Codex停止寫入後才交付審查；無遊戲/監看/背景修改工作。保持用戶世界、快取、既有歷史與審查報告。

最終使用者驗收：偏好130%、圖案較飽滿、Bloom自然流暢；Normal/115只作開發比較；Hold14合適，Formation/Dispersion/Fade認可。此為使用者實測，Codex不冒充直接觀看。02原始五階段→02.5固定局部三粒/短Hold→02.6粒子Bloom130為最終選型；既有觀察與舊記錄保留。
審查重點：預設/比較指令、壽命與階段邊界、D解除setPos/size過渡、clear/null/世界替換清理、800/200獨立預算與單效果替換、文件測試證據範圍；稀疏與斜角失真尚無根因，不把推測當結論。

# 最新交接 — 2026-10-23 Phase 02.6

Phase: 02.6 — Dust Particle Bloom Tuning
Status: PARTIAL（Compiled/純邏輯測試PASS；Bloom新視覺待使用者比較）
Changes: 預設Bloom115；新增heart dust normal/bloom115/bloom130，保留legacy/image/clear。每點3粒、原色/偏移/中心/比例不變，Hold14不變；4 tick smoothstep增加單粒尺寸，D保留Bloom並用4 tick平滑收斂尾端微變，E沿既有縮小倍率淡出。核心189/裝飾≤19，保守總上界208。
Build result: clean build --no-daemon exit0，12s，5 tasks executed；既有26.3工具鏈不變。
Tests performed: 本機javap重新確認scale/getQuadSize/setSize；Phase02TimelineCheck、Phase025DustStyleCheck、Phase026BloomCheck三個獨立Java測試exit0；git diff --check通過；唯讀Gradle歷史/快取/遊戲日誌診斷。
Tests not performed: 02.6未啟動遊戲；新Bloom清晰度/柔和度/視覺轉場/清理回歸與效能未實測；多人未測且超出範圍。
Known problems: 130%可能過亮/糊邊；保留原有±5%微變，標示倍率是其基礎Bloom倍率。Dust仍不透明層、採縮小消失。啟動耗時分段有證據，41秒前置等待根因尚未定位，未證實重複下載。
Git commit: 未建立。開始HEAD872bd52、已有02–02.5未提交差異，辨識並保留；未再次嘗試先前已被權限阻擋的index寫入，不混入快取或世界。
Recommended next phase: 使用者比較normal/bloom115/bloom130，不進Phase03。
GUI handoff: 完成後停止寫入，沒有啟動遊戲或監看工作；可對固定狀態唯讀審查。

使用者Phase02.5視覺回報已記錄：新版明顯優於舊版、格點改善但仍稀疏；Hold14剛好；Formation/Dispersion/Fade自然流暢、無明顯頓挫。此範圍Runtime-tested/Visually verified PASS（使用者），不擴大推定clear/世界清理或效能量測已過關。
本輪修改：DustStyle.java、HeartPrototype.java、新tests/Phase026BloomCheck.java，以及PROGRESS/TEST_RESULTS/DECISIONS/PROJECT_PLAN/API_COMPATIBILITY。先前文件與測試保留。
以下為歷史紀錄。

# 最新交接 — 2026-10-23 Phase 02.5

Phase: 02.5 — Dust視覺質感微調
Status: PARTIAL（Compiled/純邏輯測試PASS；新畫面待使用者人工比較）
Changes: 新增集中DustStyle.SOFT/LEGACY；預設Hold14 ticks、每有效點3粒、固定種子平面半徑0.14格/深度±0.042格偏移、最多±5%大小變化。原版Dust顏色與材質保留；所有圖像粒子只於age20生成一次。舊版入口 `/drawfirework heart dust legacy`，原image對照與clear保留。時間邏輯支援每組preset，A20/B8/D20/E14不變。
Build result: 最終clean build --no-daemon exit0，11s，5 tasks executed；Java25/Minecraft26.3及既有所有工具鏈不變。
Tests performed: 新舊兩組時間、確定性/偏移界限、大小非累積/強度界限、淡出與數量預算的獨立Java測試通過；原Phase02TimelineCheck通過；git diff --check通過。Gradle test仍NO-SOURCE。
Tests not performed: 02.5未啟動遊戲；粒子密度/格點改善/輪廓/色彩/轉場/效能/清理回歸皆待使用者；無FPS、frame time或GC數據；多人未測且超出範圍。
Known problems: 更密集圖像可能更亮或糊邊，需比較；原版Dust縮小替代alpha淡出與全局粒子引擎可能剔除粒子的限制保留。樣本偏移固定不逐tick抖動，D沿最後位置釋放，不重建粒子。
Git commit: 未建立。本輪開始HEAD872bd52，工作樹已包含上輪未提交Phase02（已辨識並保留）；上輪.git/index.lock權限阻塞仍記於歷史，本輪未再嘗試暫存/提交，不將既有Phase02差異當成本輪新增。
Recommended next phase: 使用者比較02.5與legacy；不進Phase03。
GUI handoff: Codex停止寫入；沒有遊戲/監看或背景修改工作，供固定狀態唯讀審查及使用者測試。

Phase02使用者實測補充：本次使用者明確回報五階段均正常、整體漂亮，記為Runtime-tested與Visually verified PASS（使用者回報）。Hold稀疏且規則的點陣感是02.5改善目標；未另提供clear/世界清理/逐tick/效能數據，不擴大推定已驗證範圍。
預算：63有效點×3=189核心；裝飾整段最多建立19粒，因此同時可見量保守上界208（實際更低；不依賴粒子tick排程推定精確峰值）。legacy核心63、同樣保守總上界82。兩預算獨立低於800/200；最多1效果，替換與clear逐一remove。參數限制每點1–3粒；沒有每tick重生圖像或配置Random。
以下保留歷史紀錄；舊「最新」由本節取代。

# 最新交接 — 2026-10-23 Phase 02

Phase: 02 — Animated Firework Lifecycle
Status: PARTIAL（Compiled與純時間邏輯測試PASS；待使用者人工測試）
Changes: 預設heart為Dust；保留heart dust、heart image、clear。Dust加入A20/B8/C28/D20/E14 tick；原版粒子公開setLifetime延壽、尾跡與單次發射/爆炸聲、D停止setPos、E逐步縮小軟粒子；世界/相機失效、結束及替換清理。Image仍是Phase01的36 tick對照，不是五階段主方案。
Build result: 最終clean build --no-daemon exit0，BUILD SUCCESSFUL in 10s，5 tasks executed；Minecraft26.3/Java25/Wrapper9.6.1/Loom1.17.16/Loader0.19.5/API0.163.0+26.3未改。
Tests performed: 本機26.3 javap簽章/bytecode、兩次成功clean build、獨立Java時間邏輯測試exit0、git diff --check。測試細節及JAR雜湊見TEST_RESULTS。
Tests not performed: 本輪未啟動遊戲；五階段/聲音/clear/中途退出重入/切維度/重複指令/粒子設定與視角/效能量測，皆待使用者人工測試；多人超出範圍。
Known problems: Dust為OPAQUE，採縮小替代alpha淡出，效果需人工確認；壽命覆寫防止自然提早死亡，但不能保證在全局粒子引擎過載時不被剔除。無FPS或GC量測。既有Gradle棄用警告保留。
Git commit: Phase01=d1513ed已提交；本輪開始HEAD=872bd52（審查報告），工作樹乾淨。Phase02提交結果見下方本輪Git收尾記錄。
Recommended next phase: 先人工驗收Phase02及Antigravity唯讀審查；未進Phase03。
GUI handoff: Codex為本輪唯一已知寫入者；完成後停止寫入，沒有啟動遊戲或監看工作。Antigravity只審查固定差異；其他GUI狀態無法直接確認。

修改檔案：src/main/java/com/drawfirework/client/HeartPrototype.java、src/main/java/com/drawfirework/client/FireworkTimeline.java、tests/Phase02TimelineCheck.java、docs/PROJECT_PLAN.md、docs/PROGRESS.md、docs/DECISIONS.md、docs/API_COMPATIBILITY.md、docs/TEST_RESULTS.md。以下為歷史紀錄。

# 最新交接 — 2026-10-23 Phase 01 收尾

Phase: 01 — Visual Proof of Concept
Status: PARTIAL（Dust視覺PoC與正式選型PASS；clear/完整清理仍未驗證）
Changes: 使用者實測dust正常且符合構想，正式採Dust為主要視覺方案；image可顯示但平面貼圖感強，僅保留技術對照。更新五份文件，完成Dust效能預算與結構靜態審查；本輪未修改渲染程式、metadata或工具鏈。
Build result: 本次clean build --no-daemon退出0，BUILD SUCCESSFUL in 14s，5 tasks executed；工具鏈未變，test NO-SOURCE。
Tests performed: 使用者遊戲內觸發dust/image並比較視覺；Codex讀取指示、計畫、進度、API/決策/測試與程式，核對Git差異與範圍，執行clean build。
Tests not performed: clear、退出重入清理、28 tick完整hold量測、距離/視角矩陣、frame time/GC量測；單元測試NO-SOURCE；多人未測。
Known problems: 原版Dust隨機壽命需於完整動畫前驗證；每tick位置鎖定不能直接用於散射，靜態估算不代表效能驗收。使用者回報偶發啟動0xC0000005，重啟後能測兩效果，原因未知、沒有證據歸因Dust。裸heart仍是歷史image alias，請使用 `/drawfirework heart dust`測主要方向。
Git commit: 準備Phase01限定提交；包含前輪原型/metadata與五份文件。依本次「準備」要求暫存並檢查，不建立commit、不push；HEAD=b9140dd。
Recommended next phase: 先補clear/清理驗證；未來經明確授權後進Phase02，採Dust的A–E粒子動畫，維持自然感。目前Phase02未開始。
GUI handoff: 本次Codex為寫入者；未找到本機review檔，未遇已知其他GUI改檔。收尾後停止寫入，不啟動遊戲/監看，供Antigravity唯讀審查。以下為歷史紀錄，最新狀態以本節為準。

## 2026-10-23 — Phase 00.5 current status update（歷史）

Phase: 00.5
Status: PARTIAL overall; build verification PASS (user-executed).
Changes: This update changes documentation only. The user resolved the earlier baseline-commit and build blockers in an ordinary PowerShell. Earlier blocked reports below are historical and superseded by this update.
Build result: User reports running `.\gradlew.bat clean build --no-daemon` in D:\minecraftmod with the existing GRADLE_USER_HOME=D:\minecraftmod\gradle. Result: BUILD SUCCESSFUL; all 5 tasks successful. Codex did not rerun the build or independently observe the terminal output in this update.
Tests performed: User-executed clean build; Codex read-only verification that baseline commit 4c7f4fb exists with subject "chore: baseline existing Phase 00 Fabric project".
Tests not performed: unit, runtime, visual and multiplayer testing; no new JAR inspection in this update.
Known problems: No current build blocker reported by the user. Rename changes remain uncommitted; overall Phase 00.5 is therefore PARTIAL, while its build gate is PASS. No cache migration, source or toolchain change performed in this update.
Git commit: Baseline 4c7f4fb (user-created, locally verified). No rename/documentation commit created; the user explicitly prohibited commits in this turn. Existing working-tree changes preserved.
Recommended next phase: Complete any remaining Phase 00.5 handoff/version-control work only when authorized. Phase 01 has not started and requires explicit authorization.
GUI handoff: Documentation update complete; Codex stops writing. No Gradle, game or background task started in this update.

---
# 進度與交接

## 2026-10-23 — 最新狀態：Phase 01

Phase: 01 — Visual Proof of Concept
Status: PARTIAL（原型已編譯，遊戲內觸發與視覺A/B驗收未完成）
Changes: 新增一個client專用HeartPrototype類別及metadata client入口。固定彩色愛心63點，兩方案 `/drawfirework heart image`、`/drawfirework heart dust`；預設heart使用image。只做8 tick形成與28 tick停留及清除，不含Phase02完整A–E。更新計畫、決策、API與測試紀錄。
Build result: `clean build --no-daemon` 放寬沙箱後退出0，BUILD SUCCESSFUL in 12s，5 tasks executed。所有依賴/Wrapper/Java版本未改。
Tests performed: 本機依賴javap/bytecode查核、clean build、JAR內容與SHA256、git diff --check、runClient啟動及日誌查核。runClient載入26.3、drawfirework，建立SDL視窗；日誌含整合伺服器與單人世界存檔。未直接觀察遊戲畫面或替使用者執行世界內指令。
Tests not performed: 愛心指令實際觸發、正常視角/多距離/顏色/hold/遮擋/退出重入的視覺驗收、frame time/配置量測、單元測試（NO-SOURCE）、多人。
Known problems: dust使用原版隨機色彩與壽命，預期可能提早缺點；image暫定主方案，視覺驗證仍欠缺。Windows效能計數器與開發帳號Realms/401警告見log，未阻止此次世界載入。啟動測試用Ctrl+C中止，未驗證正常退出。Gradle既有棄用警告保留。
Git commit: 未建立；沿用本專案既有不自動commit/push設定。開始時工作樹乾淨，HEAD=b9140dd；本次差異為client類別、metadata與五份文件。
Recommended next phase: 先完成本Phase01人工A/B清單並回報；Phase02僅建議，未授權，未開始。
GUI handoff: Codex完成本次原始碼與文件寫入後停止；沒有原始碼監看工作。Antigravity可唯讀審查本次固定差異。開始時沒有docs/reviews或其他本機review檔；未遇已知外部檔案修改。

Phase00.5補充：使用者本次明確回報26.3啟動、drawfirework載入、主選單及單人世界、中文介面無再次閃退、原版有色煙火正常，runtime verification視為PASS（使用者實測，非Codex視覺觀察）。本機也確認改名提交b9140dd存在；舊PARTIAL/未提交/未授權Phase01敘述皆為歷史紀錄，由本條更新。

日期：2026-10-21（Asia/Taipei）

Phase: 00
Status: PASS

Changes: 新增 docs/PROJECT_PLAN.md、PROGRESS.md、DECISIONS.md、API_COMPATIBILITY.md、TEST_RESULTS.md。未修改原始碼、建置設定、名稱或 Wrapper。

Build result: build 與 clean build 退出碼 0；clean build 實際執行 compileJava 等五項工作。使用既有 Wrapper 9.6.1、Loom 1.17.16、Java 25.0.3、Minecraft 26.3。

Tests performed: 唯讀盤點、Java/Wrapper 版本、建置、clean 行為腳本檢查、本機 Minecraft version.json、API class 簽章檢查。詳見 TEST_RESULTS.md。

Tests not performed: 無測試來源，test NO-SOURCE；未啟動遊戲/整合伺服器/專用伺服器，未驗證視覺或多人；未從空快取重建。

Known problems: 無 Git repository；metadata 圖示缺失；Gradle 棄用警告；原始 mod identity 為 myfirstmod，功能仍為 Hello Fabric；不存在完整 Draw & Hide Firework 功能。預設使用者快取權限及受限執行 daemon 連線問題已透過本次程序環境/放寬執行限制繞過，未改全域設定。

Git commit: 未建立，使用者要求不自動 commit/push，且目錄不是 Git repository；無 Git 差異可供比對。

Recommended next phase: Phase 01 僅建議，未授權；應先檢查 26.3 實作，建立並比較兩個最小視覺原型，完成遊戲內觀察紀錄。

## GUI 交接

本次寫入者為 Codex。未偵測/保證其他 GUI 的編輯狀態；本次未遇已知外部修改。完成文件後停止寫入；Antigravity 可對本次固定狀態唯讀審查，不執行寫入型建置。既有 Gradle daemon 可能閒置存在，沒有啟動遊戲、監看或自動修改工作。後續 Codex 修改前應重新比對基準。

本次新增差異只有 docs 五份；Gradle 另正常更新既有 gradle/、.gradle/ 與 build/ 快取/產物。由於無 Git，不宣稱能還原先前使用者的未提交差異。檢查基準與 JAR SHA256 見 TEST_RESULTS.md。

## 2026-10-23 — Current handoff: Phase 00.5

Phase: 00.5
Status: PARTIAL (build and Git commit BLOCKED)
Changes: Local git init; cache-safe .gitignore; drawfirework / Draw & Hide Firework / com.drawfirework.DrawFirework identity; Gradle project/archive/group rename; missing icon declaration removed; phase plan and records appended. No Phase 01 or gameplay work.
Build result: clean build attempted before and after rename; failed before compilation because launcher cannot connect to Gradle daemon. Versions unchanged. Historical Phase 00 PASS remains historical evidence only.
Tests performed: source/config hash comparison, JSON/entrypoint/static identity checks, ignore/tracked-file inspection. See TEST_RESULTS.md for exact commands and failures.
Tests not performed: current compilation, unit/game/visual/multiplayer tests.
Known problems: execution environment cannot create .git/index.lock for commit; daemon connectivity blocked. No remote/push. Cache separation documented in DECISIONS.md; no migration/deletion.
Git commit: none. Original pre-rename baseline is staged (16 files); rename and new docs remain unstaged, new initializer untracked. Branch master has no commits. Do not stage current work before committing the preserved baseline.
Recommended next phase: finish Phase 00.5 verification and commits in a normal writable shell; Phase 01 still requires explicit authorization.

Recovery in a normal user PowerShell at D:\minecraftmod:
1. Inspect git diff --cached --stat and git diff: index is baseline, worktree is rename.
2. git commit -m "chore: baseline existing Phase 00 Fabric project" (uses already staged baseline).
3. Set $env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; run .\gradlew.bat clean build and check $LASTEXITCODE.
4. Only after success, update this report with actual result, then git add .gitignore settings.gradle gradle.properties src docs; git diff --cached; git commit -m "chore: formalize Draw & Hide Firework identity".
5. git status --short --branch; git log -2 --oneline. Do not push.

Client smoke test after build succeeds: same working directory and GRADLE_USER_HOME, run .\gradlew.bat runClient. Check main menu, create/open a disposable single-player world, then quit normally. Inspect run/logs/latest.log for the drawfirework initialization line and errors. This is initialization testing only; no drawing UI/firework is implemented. Report exceptions, actual versions and whether the world opened.

GUI handoff: Codex stopped source changes at Phase 00.5 boundary; no game/watch task started. Antigravity may review this fixed state read-only. External concurrent GUI state cannot be guaranteed. Old phase records above are preserved, not current status.
## 本輪 Git 收尾

Phase02未建立commit：明確列出8個階段檔案執行git add時exit128，無法建立D:/minecraftmod/.git/index.lock（Permission denied）；未暫存、未commit、未push。依使用者指示保留變更，由使用者在一般PowerShell提交。這是檔案寫入權限阻塞，沒有自動審核拒絕事件。開始HEAD仍872bd52；沒有工具鏈/快取/世界檔案混入差異。

使用者完成人工驗收後可在D:\minecraftmod執行：
```powershell
git add src/main/java/com/drawfirework/client/HeartPrototype.java src/main/java/com/drawfirework/client/FireworkTimeline.java tests/Phase02TimelineCheck.java docs/PROJECT_PLAN.md docs/PROGRESS.md docs/DECISIONS.md docs/API_COMPATIBILITY.md docs/TEST_RESULTS.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat: add phase 02 dust firework lifecycle"
```

程式停止於Phase02 PARTIAL，等待人工畫面/清理/效能驗收；不開始Phase03。
## Finalization Git收尾（2026-10-23）

明確列出11檔執行git add時，回報fatal: Unable to create 'D:/minecraftmod/.git/index.lock': Permission denied。依使用者指示立即停止所有Git寫入；沒有commit/push，也未放寬權限重試或刪除lock。暫存區仍空，6個tracked修改與5個untracked階段檔保留；HEAD872bd52。這是實際Git檔案寫入權限錯誤，不是自動審核拒絕。文件補記之後僅作唯讀狀態/差異檢查。

完整候選提交11檔：3個client Java（HeartPrototype、DustStyle、FireworkTimeline），3個tests（Phase02TimelineCheck、Phase025DustStyleCheck、Phase026BloomCheck），5份docs（PROJECT_PLAN、PROGRESS、TEST_RESULTS、DECISIONS、API_COMPATIBILITY）。本輪只另改HeartPrototype預設及四份必需紀錄，其他檔案為先前02–02.6實作/證據，全部納入同一階段提交。

使用者一般PowerShell精確命令（每一步失敗即停，不push）：
```powershell
Set-Location D:\minecraftmod
$phase02Files = @(
  'src/main/java/com/drawfirework/client/HeartPrototype.java'
  'src/main/java/com/drawfirework/client/DustStyle.java'
  'src/main/java/com/drawfirework/client/FireworkTimeline.java'
  'tests/Phase02TimelineCheck.java'
  'tests/Phase025DustStyleCheck.java'
  'tests/Phase026BloomCheck.java'
  'docs/PROJECT_PLAN.md'
  'docs/PROGRESS.md'
  'docs/TEST_RESULTS.md'
  'docs/DECISIONS.md'
  'docs/API_COMPATIBILITY.md'
)
git add -- $phase02Files
if ($LASTEXITCODE -ne 0) { throw 'git add failed' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'staged diff check failed' }
git diff --cached --stat
git commit -m "feat: finalize phase 02 animated dust fireworks"
if ($LASTEXITCODE -ne 0) { throw 'git commit failed' }
git status --short --branch
git log -1 --oneline
```

Antigravity以目前HEAD872bd52加上述11檔工作樹差異為審查基準；使用者完成提交後改用新commit。Codex停止寫入，無背景修改；未開始Phase03。
