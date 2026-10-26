# Phase 06 — High-Resolution Draft Persistence 人工驗收指南

**狀態**：**PASS / CLOSED**（依據 Codex 最終獨立唯讀審查通過與使用者 6/6 人工實機驗收通過正式批准結案）

本文件提供 Phase 06 自由繪圖草稿持久化系統（Draft Persistence）及其最終資料完整性修復（Final Data Integrity Remediation）之交付成果概述、安全儲存架構、最短實機驗收步驟與結案結論。

---

## 一、交付成果概述

1. **純 Java 高解析度草稿核心（`com.drawfirework.draft`）**：
   - 完整保存 **256×256 原始 ARGB 像素**（65,536 個像素），絕不從 32×32 降採樣圖放大冒充原稿，100% 無損保留透明通道與色彩細節。
   - 儲存位置：客戶端遊戲目錄下的 `drawfirework/drafts/`（動態透過 `Minecraft.getInstance().gameDirectory` 定位）。
   - 自動相容遷移與衝突保護（Minor A & B）：啟動時自動偵測舊版可能殘留之 `config/drawfirework/drafts/` 目錄。若遷移來源與目標存在同 UUID 且內容相異，絕不覆寫或刪除任一檔案，自動生成 `.legacy_conflict.json` 安全保留雙方資料，並透過 `ClientDraftStorage.getMigrationConflicts()` 記錄提示。
   - 單檔自包含 JSON 格式：檔名使用 `UUID.json`，內部包含 `schemaVersion`、`id`、`name`、`createdAt`、`updatedAt`、`width`、`height`、`format`（`argb32-hex`）與完整 524,288 字元之 hex 像素字串。
   - 終極資料完整性策略（Major 2 & Major 3）：
     - **備份前完整主檔驗證（Major 2）**：在將現有主檔複製至 `.bak` 之前，強制執行 `readFullDraft` 完整校驗（包含 JSON 嚴格結構、schemaVersion、檔名與內容 UUID 一致性、1..256 尺寸、精確 524,288 字元 hex 像素長度及有效 ARGB 十六進位字元）。若主檔因像素截斷、非法 hex 或 UUID 不一致而損壞，**絕不**複製至 `.bak`；若主檔損壞且有效備份存在，主動拒絕一般覆寫以防備份遺失，誓死捍衛最後一份有效資料。
     - **新版 Schema 絕對保護（Major 3）**：若現有主檔之 `schemaVersion > 1`，一律拋出 `DraftVersionException`。一般 Save、一般 Recover 乃至傳入 `force=true` 的強制復原均絕對拒絕覆寫，主檔與備份維持 100% 位元組不變，絕不將「版本較新」誤判為「檔案損壞」。
     - **安全暫存檔與原子替換**：採用隨機不可預測之唯一暫存檔（`Files.createTempFile(baseDir, id + "_", ".tmp")`），在 `try ... finally` 中保證安全清理；執行原子移動（`ATOMIC_MOVE`），當檔案系統不支援原子移動時，系統採用非原子替代移動；操作失敗時回報錯誤，並依既有備份／復原策略處理。本機制不保證斷電期間的資料持久性。
     - **符號連結與路徑防護**：檔案解析時強制執行 `Files.isSymbolicLink` 檢查與 `toRealPath().startsWith(baseDir.toRealPath())` 雙重路徑穿越防護，嚴禁符號連結指向草稿目錄之外或偽造草稿檔。
     - **容量與記憶體限制**：嚴格限制單檔大小（2 MB）、草稿總量上限（200 份）；檔案毀損時拋出安全異常並完整保留磁碟檔案，不崩潰遊戲也不刪檔。
     - **目錄讀取錯誤追蹤**：目錄存取異常時透過 `lastListError` 保存錯誤訊息並在 UI 即時提示，不把異常讀取誤報為「空草稿庫」。

2. **阻止跨尺寸有損覆寫（Major 1）**：
   - 在 `FreehandCanvas` 實作 `canLoadLosslessly(int[] draft256)` 嚴格檢驗：
     - 若工作畫布為 128×128（來自 16×16 合法舊物品），僅當 256×256 草稿中每組 $2 \times 2$ 方塊內部像素完全相同（即源自 128×128 無損展開）時，才允許載入。
     - 若 256×256 草稿包含細緻斜線、像素級色差或 Alpha 差異，`canLoadLosslessly` 返回 `false`，`loadDraft` 拋出 `IllegalArgumentException` 拒絕載入。
   - 在 `DraftManagerScreen.executeLoad` 與 `CanvasEditorScreen.loadDraftData` 中嚴格阻斷有損載入：
     - 拒絕載入時，發送錯誤提示，**完全保留當前正在編輯的畫布、像素、Dirty 狀態、Undo/Redo 歷史與預覽**。
     - 磁碟上的原始 256×256 草稿維持 100% 原貌，絕不允許被有損結果覆蓋同一 UUID。
     - 舊 16×16 ItemStack、現有 32×32 CanvasData 與 C2S 安全契約完全不變，`snapshot()` 繼續精確輸出對應物品尺寸。

3. **繪圖編輯器草稿管理整合（`CanvasEditorScreen` & `DraftManagerScreen`）**：
   - **安全儲存完成流程**：
     - 統一 `saveAndThen(callingScreen, onSuccess, onCancel)` 狀態機。
     - 儲存成功：寫入磁碟與驗證無誤後，方才執行原本待辦之關閉 GUI、載入新稿或建立草稿動作。
     - 命名取消：使用者在命名彈窗按 Cancel 或 Esc 時，完整保留當前未命名畫稿與 Dirty 狀態，不執行關閉或切換。
     - 儲存失敗：I/O 寫入或磁碟錯誤時，顯示錯誤通知，保留畫布與 Dirty 狀態，絕不執行切換或關閉。
   - **窄視窗自適應排版**：
     - 引入 `DraftManagerLayout` 純 Java 獨立排版計算器，支援 $[140 \times 140]$ 至 4K 螢幕。
     - 保證 `listWidth >= 30`、`buttonWidth >= 40`、清單與按鈕完全不重疊、按鈕與文字絕不溢出螢幕邊界。
   - **雙通道清晰分離**：
     - `Save Draft`（儲存草稿）：保存 256×256 原稿至本機草稿庫。
     - `To Firework`（儲存至煙火）：輸出 32×32 `CanvasData` 至主手煙火物品，絕不混淆或覆蓋高解析度草稿。
   - **標題狀態反饋**：頂部即時顯示當前草稿名稱（例如 `煙火像素畫布 — Dragon*`），有未儲存修改時顯示星號 `*`。
   - **快捷鍵**：支援 `Ctrl+S` 即時快速儲存目前草稿。
   - **未儲存防護（Unsaved Changes）**：在草稿有未儲存修改時，按 Esc 關閉 GUI、在草稿庫切換載入其他草稿、或建立新草稿時，主動彈出確認視窗，提供【儲存草稿】、【捨棄變更】與【取消】三種選項，杜絕誤失畫作。
   - **草稿管理介面（`DraftManagerScreen`）**：
     - 點擊 `Drafts...` 開啟獨立草稿庫。
     - 滾動列表顯示所有草稿名稱、最後修改時間（`yyyy-MM-dd HH:mm`）、尺寸及 `[目前編輯中]` 標籤。
     - 提供六大管理功能：【載入草稿（Load）】、【建立新草稿（New）】、【儲存草稿（Save）】、【另存新檔（Save As...）】、【重新命名（Rename...）】及【刪除草稿（Delete，含二次確認視窗）】。
     - 支援雙擊列表項目直接載入。
     - 載入草稿時自動安全清空上一份畫稿的 Undo/Redo 歷史與預覽快取，確保工作階段完全隔離。

4. **嚴格不變量保留**：
   - 32×32 `CanvasData` 格式、ItemStack / C2S 安全驗證、76 ticks 五階段動畫、Bloom 130%、Fixed 朝向、Phase 05C `fullbright_exact` 光照與 Phase 05D `Balanced` / `Detail-Preserving` 正式預設 100% 保留。

---

## 二、最短實機驗收程序

請依序在遊戲內執行以下 8 項核心操作：

### 1. 繪製並儲存第一份 256×256 草稿
1. 啟動遊戲：在 PowerShell 執行：
   ```powershell
   Set-Location D:\minecraftmod
   $env:GRADLE_USER_HOME='D:\minecraftmod\gradle'
   .\gradlew.bat runClient
   ```
2. 進入創造模式世界，主手手持 `drawfirework:test_firework`，按 **G** 開啟畫布。
3. 畫一張包含不同顏色與透明邊界的圖案（例如多色臉譜、數字或愛心）。
4. 點擊右側 **Save Draft**（或按 **Ctrl+S**），彈出命名視窗輸入 `MyArtwork`，點擊儲存。
5. 確認聊天室顯示：`草稿「MyArtwork」已成功儲存。`，且頂部標題更新為 `煙火像素畫布 — MyArtwork`。

### 2. 關閉 GUI 並重新載入
1. 按 **Esc** 關閉畫布（此時無未儲存變更，應直接關閉）。
2. 再次按 **G** 重新開啟畫布。
3. 點擊右側 **Drafts...** 開啟草稿管理介面。
4. 在列表中看到 `MyArtwork`，點擊選取後點擊 **Load Draft**（或直接雙擊）。
5. **驗收點**：畫布 100% 還原剛才繪製之 256×256 筆觸與細節，不是 32×32 放大。

### 3. 重啟 Minecraft 驗證持久化
1. 完全退出 Minecraft（點擊遊戲內的 Save and Quit to Title，再點擊 Quit Game）。
2. 在 PowerShell 重新執行 `.\gradlew.bat runClient` 再次啟動 Minecraft。
3. 進入世界，手持測試煙火按 **G**，點擊 **Drafts...**。
4. **驗收點**：`MyArtwork` 依然完好存在於列表中，點擊載入後圖案完整無缺。

### 4. 建立第二份草稿（驗證互不覆蓋）
1. 在草稿庫點擊 **New Draft**，命名為 `SecondDraft`。
2. 畫布重設為全透明空白。使用綠色繪製不同圖案。
3. 點擊 **Save Draft** 儲存。
4. 點擊 **Drafts...**，確認列表中同時存在 `MyArtwork` 與 `SecondDraft` 兩份獨立作品。

### 5. 測試未儲存變更防護與取消安全
1. 在當前草稿上隨意點擊畫上一筆，頂部標題出現星號（例如 `SecondDraft*`）。
2. 按 **Esc** 嘗試關閉 GUI。
3. **驗收點**：彈出【未儲存的變更】確認視窗，顯示【儲存草稿】、【捨棄變更】與【取消】。
4. 點擊【取消】：返回編輯器，畫稿完好保留，不關閉 GUI。
5. 若為新建立未命名畫稿，點擊【儲存草稿】時彈出命名視窗，此時按 Esc 取消命名：確認返回編輯器，畫稿保留，不被強制關閉。
6. 點擊【Drafts...】並嘗試載入 `MyArtwork`：再次彈出未儲存變更確認視窗。
7. 點擊【捨棄變更】：載入 `MyArtwork`，放棄剛才的隨意筆畫。

### 6. 測試另存新檔（Save As）與重新命名（Rename）
1. 在 `MyArtwork` 畫布上加畫幾筆，點擊 **Drafts...** -> **Save As...**，輸入 `MyArtwork_V2`。
2. 確認草稿庫中新增 `MyArtwork_V2`，原本的 `MyArtwork` 保持原樣。
3. 選取 `MyArtwork_V2`，點擊 **Rename...**，修改為 `DragonDesign`。
4. 確認列表名稱更新，再次載入確認像素未遺失。

### 7. 測試刪除草稿與二次確認
1. 在草稿庫選取不需要的測試草稿，點擊 **Delete**。
2. **驗收點**：彈出原版確認視窗：`確定要刪除草稿「...」嗎？此操作無法復原。`
3. 點擊【Cancel】：草稿未被刪除。
4. 再次點擊 **Delete** 並點擊【Yes】：草稿從列表與磁碟中安全刪除。

### 8. 輸出至煙火物品與施放回歸
1. 載入任一草稿後，點擊右側 **To Firework**（儲存至煙火）。
2. 聊天室顯示送出與伺服器儲存成功訊息。
3. 在夜空（`/time set midnight`）右鍵發射煙火。
4. **驗收點**：夜空中完整播放 76 ticks 五階段動畫，Balanced + Detail-Preserving + Fullbright Exact 視覺效果完全正常。

---

## 三、正式結案結論（2026-10-25）

- **驗收結果**：**PASS / CLOSED**（正式批准結案）
- **Codex 最終審查**：Critical 0 / Major 0 / Minor 1（唯一文件措辭 Minor 已修正為忠於非原子替代移動與無斷電保證實作）。
- **使用者 6/6 人工實機驗收全數通過**：
  1. 儲存第一份草稿：PASS
  2. 完整重啟 Minecraft 後重新載入：PASS
  3. 多份草稿與另存：PASS
  4. 未儲存變更提醒：PASS
  5. 刪除與返回：PASS
  6. 煙火輸出：PASS
  - 使用者回報：沒有感受到明顯問題。
- **核心交付成效**：
  - 完整 256×256 原始畫稿可跨 Minecraft 重啟安全保存並重新開啟編輯。
  - 草稿庫多草稿管理、另存、重新命名、刪除與舊目錄遷移衝突防護全數就緒。
  - 原有 32×32 煙火物品輸出、伺服器 EditSession 儲存契約與 76 ticks 五階段動畫視覺效果完全正常。
- **保留已知限制（Accepted Limits）**：
  1. 草稿儲存為客戶端本機資料，不支援多人同步（依產品規格規劃）。
  2. 256×256 草稿不代表世界煙火輸出已提高至 256×256（物品與空中粒子依規格維持 32×32 CanvasData）。
  3. 非原子檔案系統及斷電安全性不作絕對保證（當檔案系統不支援原子移動時採替代移動，不保證斷電期間資料持久性）。
  4. 128×128 舊尺寸編輯器可能拒絕無法無損表示的 256×256 草稿（以保護高解析度原稿不被有損覆寫）。
