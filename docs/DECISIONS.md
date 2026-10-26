## 2026-10-26 — RC01 授權與 GitHub 發布整理

- 使用者明確選擇原創內容 MIT，著作權 2026 diegolaumm；保留 Gradle Wrapper 原 Apache-2.0 標頭／內嵌授權，另提供其 LICENSE 原文副本，不對第三方重新授權。
- `git remote -v` 為空，沒有已確認本專案 repo URL；移除範例 contact，不以作者名稱猜測 URL。未建立 repo／remote／release。
- metadata 的 Minecraft `~26.3` 改為精確 `26.3`，符合既有固定版本契約；沒有改依賴。保留 Loader／Java 下限與 API 必要依賴宣告；README 記錄本次實際建置組合，沒有宣稱其他版本已測。
- 標準 `jar` task 僅補 root MIT LICENSE 封裝；README 坦誠標示 RC／單人創造／未實作生存與多人，修正錯誤的測試物品指令。現有版本號維持 1.0.0。
- `.gitignore` 排除任何實例的 `drawfirework/drafts/`（含舊 config 路徑與備份），並排除常見本機世界／秘密檔案；不刪除資料、不將 build 產物追蹤進 Git。

## 2026-10-25 — Phase 07A 畫家工作台與羊皮紙畫布 GUI 重設計決策（正式結案）

- **透明填色工具切換行為修正（Minor 1 修復）**：
  - 核心問題：先前 `selectFillTool()` 在使用者自橡皮擦切換至油漆桶時，因 `if (color == 0) color = lastPaintColor;` 將顏色覆蓋為非零色彩，導致無法進行連通分支的透明填色擦除。
  - 決策：移除強制復原邏輯，允許 `Tool.FILL` 承接 `color == 0`，保留在透明模式下填色以清除相連色塊的能力；僅在使用者明確點擊【畫筆（Brush）】圖示鈕時才還原 `lastPaintColor`。
- **視覺主題與核心資料嚴格解耦（`CanvasEditorTheme`）**：
  1. **羊皮紙底色純視覺化**：中央畫布採用暖米白（`#EFE3C6`）與淡羊皮紙色（`#E2D3B1`）構成低對比微格紋底板，僅在 `CanvasEditorScreen.extractRenderState` 階段透過 `GuiGraphicsExtractor` 繪製於透明動態貼圖（`DynamicTexture`）下方。`FreehandCanvas` 內部 ARGB 陣列完全不變，未上色區域維持 `0x00000000`（完全透明），純白（`#FFFFFF`）與淡黃（`#FFDF40`）筆觸在羊皮紙微格紋上仍清晰可辨，且橡皮擦、Flood Fill、Undo/Redo、256×256 草稿與 32×32 煙火輸出 100% 不受背景色影響。
  2. **立體木質畫框零遮擋保證**：木框（深棕外邊 `#422C1B`、淺木主體 `#B68B5B`、高光 `#D6AD7C`、陰影 `#7D5937`、內凹槽 `#593D26`）嚴格繪製於 `[left - thickness, top - thickness, left + boardWidth + thickness, top + boardHeight + thickness]` 外圈，內側開口精確對齊 `[left, top, boardWidth, boardHeight]`，不覆蓋任何一顆可繪圖像素，滑鼠點擊與拖曳座標換算公式維持 100% 不變。
  3. **程序化像素圖示取代外部大型貼圖**：所有工具圖示（畫筆、橡皮擦、油漆桶、Undo、Redo、柔和/銳利、繪圖/32×32/粒子、草稿資料夾、磁碟片存稿、煙火火箭、關閉 X）均以 `GuiGraphicsExtractor.fill` 繪製於 9×9 像素格內，確保在任意 Minecraft GUI Scale 與縮放比例下邊緣銳利、無貼圖載入失敗風險且不引入第三方 UI 函式庫。
- **右側工具列精簡與分組策略（`CanvasEditorLayout` & `CanvasEditorScreen`）**：
  1. **8 列緊湊分組取代 10 列長文字排**：
     - **Color 區（Row 1–2）**：8 色調色盤由 4×2 純文字按鈕改為 2×4 緊湊色塊（`SwatchButton`），以色彩本身直覺呈現，選中時加上雙層亮金外框與角落指示點。
     - **Size 區（Row 3）**：保留筆刷粗細滑桿與即時數值顯示。
     - **Tools 區（Row 4）**：新增獨立【畫筆（Brush）】按鈕，與【橡皮擦（Eraser）】、【填色（Fill）】並排為 3 顆純圖示按鈕；從橡皮擦或填色切回畫筆時自動恢復最後使用的畫筆顏色。
     - **History 區（Row 5）**：【Undo】與【Redo】改為左右彎箭頭純圖示按鈕，並依 `model.canUndo()` / `model.canRedo()` 即時切換 `active` 狀態，無歷史可退時自動呈現暗化停用（Disabled）外觀。
     - **View 區（Row 6）**：【Mode（柔和/銳利）】與【View（繪圖/32×32/粒子）】採用「圖示＋簡短狀態字」設計（窄視窗下自動退回純圖示），兼顧乾淨視覺與狀態可讀性。
     - **Drafts & Output 區（Row 7–8）**：
       - Row 7 為柔和藍綠色系（`DRAFT_STORAGE`）的【草稿庫（資料夾圖示）】與【存稿（磁碟片圖示）】；當畫布處於 Dirty 未存檔狀態時，存稿鈕自動加上金色高亮邊框提醒。
       - Row 8 為左側小型暗紅【關閉（X 圖示）】按鈕與右側寬幅暖金色（`FIREWORK_OUTPUT`）【轉為煙火（火箭圖示＋文字）】主行動按鈕，確保 `Save Draft` 與 `To Firework` 在色彩、圖示、尺寸與位置上完全不會混淆。
  2. **多維度可及性（不單靠顏色區分）**：每個純圖示或色塊按鈕均具備專屬圖示、雙層金色 Selected 邊框、Disabled 暗化狀態以及 150ms 快速觸發的中英文 Hover Tooltip（含快捷鍵說明）。

## 2026-10-25 — Phase 06 高解析度草稿持久化正式結案決策（PASS / CLOSED）

- **正式結案依據**：
  - Codex 最終獨立唯讀審查通過（Critical 0 / Major 0 / Minor 1，文件 Minor 已修正）。
  - 使用者 6/6 人工實機驗收全數通過（儲存、重啟載入、多草稿與另存、未儲存變更提醒、刪除二次確認、煙火輸出完全正常，「沒有感受到明顯問題」）。
  - Phase 06 正式批准 PASS / CLOSED。
- **高解析度草稿架構正式決策**：
  1. **256×256 原始畫稿永久保存**：完整保存 65,536 像素之 ARGB 十六進位字串，跨 Minecraft 重啟可重開與繼續編輯，杜絕從 32×32 放大冒充。
  2. **草稿與煙火物品輸出清晰分離**：草稿儲存為客戶端本機 JSON 格式（`drawfirework/drafts/<UUID>.json`）；「To Firework」獨立輸出 32×32 CanvasData 至主手煙火物品，伺服器 EditSession 儲存契約保持不變。
  3. **跨尺寸載入防護**：非 256×256 工作畫布若無法無損呈現 256×256 草稿細節，強制拒絕載入並保護原稿，防止有損結果覆寫同一 UUID。
  4. **備份與版本控制防護**：主檔完整校驗後才複製至備份，毀損主檔不覆蓋有效 .bak；較新 schemaVersion（> 1）嚴格禁止覆寫與強制復原。
  5. **非原子替代移動與無斷電保證**：當檔案系統不支援原子移動時採用非原子替代移動；操作失敗時回報錯誤並依備份復原策略處理，不作斷電期間資料持久性保證。
- **保留已知限制（Accepted Limits）**：
  1. 草稿儲存為客戶端本機資料，不支援多人同步（多人同步屬未來 Phase 09/10 範疇）。
  2. 256×256 草稿不代表世界煙火輸出已提高至 256×256（物品與空中粒子依規格維持 32×32 CanvasData）。
  3. 非原子檔案系統及斷電安全性不作絕對保證。
  4. 128×128 舊尺寸編輯器可能拒絕無法無損表示的 256×256 草稿。

## 2026-10-25 — Phase 06 最終資料完整性修復決策（Final Data Integrity Remediation）

- 核心資料安全決策：
  1. **阻止跨尺寸有損覆寫（Major 1）**：
     - 在 `FreehandCanvas` 新增 `canLoadLosslessly(int[] draft256)`：對於非 256×256 畫布（如 16×16 舊物品產生的 128×128 工作畫布），檢驗 256×256 草稿中每個取樣方塊（$2 \times 2$）內之像素是否完全一致。
     - 若包含細微斜線、不同顏色或 Alpha 差異，`canLoadLosslessly` 判定為 `false`，`loadDraft` 拋出 `IllegalArgumentException` 拒絕載入。
     - 在 `DraftManagerScreen.executeLoad` 與 `CanvasEditorScreen.loadDraftData` 攔截拒絕事件：
       - 發送清晰錯誤提示至玩家（`draft_lossy_load_error`）。
       - **完全保留當前正在編輯的畫布、像素、Dirty 狀態、Undo/Redo 歷史與預覽**。
       - 磁碟上原 256×256 高解析度草稿 100% 原封不動，杜絕因降採樣後 Save 覆寫同一 UUID 造成的資料毀損。
       - 16×16 煙火物品輸出合約與伺服器 `EditSession.save` 契約 100% 保持不變。
  2. **備份前完整主檔驗證（Major 2）**：
     - 將覆寫前僅讀標頭的 `readMetadata` 改為呼叫 `readFullDraft(targetPath, id)` 完整資料驗證。
     - 嚴格校驗：JSON 結構完整性、`schemaVersion`、檔名 UUID 與 JSON 內容 `id` 一致性、1..256 尺寸、精確像素長度（524,288 hex 字元）以及所有像素之 ARGB 十六進位編碼。
     - 遇到像素截斷、非法 hex 或 UUID 不一致等任何毀損情境，**絕不**將該主檔複製至 `.bak` 備份。
     - 若主檔已毀損但現有 `.bak` 備份有效，`saveDraft` 採取保守防護主動拒絕覆寫，提示使用者先復原備份或另存新檔，誓死守護最後一份有效備份。
  3. **新版 Schema 絕不可被自動復原覆蓋（Major 3）**：
     - 在 `recoverFromBackup` 單獨且優先攔截 `DraftVersionException`。
     - 若目標主檔之 `schemaVersion > CURRENT_SCHEMA_VERSION`，一般復原與帶 `force=true` 的強制復原均絕對拒絕，立即拋出 `DraftVersionException`。
     - 復原失敗後，新版主檔與舊版備份均保持 100% 位元組不變，絕不將「版本較新」誤判為「檔案損壞」。
  4. **初始化錯誤容錯與路徑安全（Minor A）**：
     - `ClientDraftStorage` 在遭遇路徑安全異常或初始化失敗時，透過 `initError` 記錄並安全回傳錯誤，不拋出未捕捉崩潰，不清除編輯器畫布或 Dirty 狀態。
  5. **舊目錄遷移 UUID 衝突安全保護（Minor B）**：
     - 在 `migrateLegacyDrafts` 中比對來源與目標內容：若內容相異，絕不覆寫或刪除任一檔案，自動生成 `.legacy_conflict.json` 備份並記錄於 `getMigrationConflicts()`。
  6. **非原子寫入與容錯替代策略**：
     - 當檔案系統不支援原子移動時，系統採用非原子替代移動；操作失敗時回報錯誤，並依既有備份／復原策略處理。本機制不保證斷電期間的資料持久性。

## 2026-10-25 — Phase 06 資料安全修復決策（Data Safety Remediation）

- 核心安全防護與生命週期決策：
  1. **儲存完成狀態機與流程安全（Major 1 & Major 2）**：
     - 在 `CanvasEditorScreen` 建立單一事實來源：`saveAndThen(Screen callingScreen, Runnable onSuccess, Runnable onCancel)`。
     - **Success 契約**：確認檔案已完整寫入磁碟並驗證無誤後，方才執行原本待辦之操作（例如關閉 GUI、載入新草稿或新建畫布）。
     - **Cancel 契約**：對於尚未命名之草稿，彈出 `DraftNamePromptScreen`；若使用者點擊 Cancel 或按 Esc，呼叫 `onCancel`，完整保留目前畫布與 Dirty 狀態，絕不強制關閉或切換。
     - **Failure 契約**：若磁碟寫入失敗或遭遇 I/O 異常，發送錯誤通知至玩家，保留畫布與 Dirty 狀態，絕不清除歷史，絕不執行後續破壞性動作。
     - `DraftManagerScreen` 中所有涉及 Dirty 畫布之操作（Load Draft、New Draft、Save Draft）統一委派至 `parentEditor.saveAndThen(...)`，徹底消滅雙軌邏輯與異常被吞噬的問題。
  2. **128×128 與 256×256 工作畫布相容（Major 3）**：
     - 合法舊物品若為 16×16 `CanvasData`，在編輯器中產生 128×128 之工作畫布（$16 \times 8$）。
     - 在 `FreehandCanvas` 新增 `toDraft256Pixels()`：針對 128×128 畫布，採確定性 $2 \times 2$ 鄰近方塊複製映射至 256×256 草稿緩衝區；標準 256×256 畫布則直接複製。
     - 擴充 `loadDraft(int[] newPixels)`：當傳入標準 256×256 草稿至 128×128 畫布時，採確定性左上取樣降採樣回 128×128。若畫稿未經修改，此過程為 100% 雙向無損往返。
     - 煙火輸出合約：`snapshot(mode)` 依然嚴格輸出 `outputWidth() x outputHeight()`（16×16 輸出 16×16，32×32 輸出 32×32），伺服器 `EditSession.save` 與網路 Payload 契約完全不變。
  3. **備份、覆寫與復原之資料完整性（Major 4）**：
     - **覆寫前版本保護**：寫入前先行透過 `readMetadata` 檢查目標主檔。若 `schemaVersion > CURRENT_SCHEMA_VERSION`，立即拋出 `DraftVersionException` 拒絕覆寫，杜絕舊客戶端意外破壞新格式畫稿。
     - **損壞檔案不得覆蓋有效備份**：寫入前若目標主檔為有效結構，方才複製至 `.bak` 備份；若目標主檔已損毀，**絕不**將損壞檔案複製至 `.bak`，全力保護現有最後一份完好備份。
     - **安全復原檢查**：呼叫 `recoverFromBackup` 時，若主檔本身結構完整有效，主動拒絕無條件復原以防以舊備份覆蓋新稿（需透過 `force` 參數明確覆寫）；復原過程一律經由暫存檔原子替換。
  4. **符號連結防護與隨機暫存檔（Major 5）**：
     - 放棄固定 `.json.tmp` 命名，改採 `Files.createTempFile(baseDir, id.toString() + "_", ".tmp")` 建立隨機且唯一的暫存檔，並於 `try ... finally` 中保證安全清理。
     - 於 `resolvePath` 強制執行 `Files.isSymbolicLink(path)` 檢查與 `toRealPath().startsWith(baseDir.toRealPath())` 雙重路徑穿越防護，嚴禁符號連結指向草稿目錄之外或偽造草稿檔。
  5. **窄視窗自適應排版（Minor 1）**：
     - 建立純 Java 獨立排版計算器 `DraftManagerLayout`，支援 $[140 \times 140]$ 至 4K 螢幕。
     - 保證 `listWidth >= 30`、`buttonWidth >= 40`、清單與按鈕完全不重疊、按鈕與文字絕不溢出螢幕邊界。
  6. **儲存路徑規格統一與舊目錄遷移（Minor 2）**：
     - 統一規範儲存目錄為 `<gameDirectory>/drawfirework/drafts/`。
     - 在 `ClientDraftStorage` 初始化時自動偵測舊版可能殘留之 `config/drawfirework/drafts/` 目錄並安全複製未遷移之草稿至標準目錄。
  7. **精確錯誤追蹤與可靠性宣告（Minor 3）**：
     - 目錄讀取異常時透過 `lastListError` 保存錯誤訊息並在 UI 即時提示，不把異常讀取誤報為「空草稿庫」。
     - 明確澄清：`writer.flush()` 保證將資料從使用者空間緩衝區沖刷至作業系統檔案快取，非保證實體磁碟斷電安全（`fsync`）；`ATOMIC_MOVE` 遇檔案系統不支援時具備覆蓋移動降級。

## 2026-10-25 — Phase 06 高解析度草稿持久化設計決策

- 核心儲存格式與目錄決策：
  1. **實體目錄位置**：客戶端遊戲目錄下的 `drawfirework/drafts/`（動態依據 `Minecraft.getInstance().gameDirectory` 定位）。完全獨立於伺服器與存檔，屬客戶端本地純資料庫。
  2. **檔案命名與識別解耦**：採用 `<uuid>.json` 作為內部實體檔名；使用者輸入之草稿名稱僅作為內部中繼資料，絕不直接拿使用者名稱拼接路徑，徹底避免作業系統檔名非法字元、大小寫敏感性與目錄穿越漏洞。
  3. **單檔自包含格式與版本控制**：
     - 明確規範 `schemaVersion = 1`。遇較新版本（`> 1`）拋出 `DraftVersionException` 安全拒絕，絕不自動覆蓋或破壞資料。
     - 包含 `id`、`name`（上限 64 字元）、`createdAt`、`updatedAt`、`width = 256`、`height = 256`、`format = "argb32-hex"` 與 524,288 字元之 hex 像素字串。
     - 100% 精確保留 256×256 原始 ARGB 與 Alpha 通道，無損儲存。單檔大小約 525 KB，符合 JSON 解析與檔案系統最佳區塊大小。
  4. **輕量快速中繼資料快讀**：`readMetadata` 使用 streaming `JsonReader`，遇 `pixels` 欄位直接呼叫 `skipValue()`，不配置龐大字串與陣列，使列表掃描 100 份草稿僅需數毫秒。
- 安全防護與容錯策略：
  1. **原子替換（Atomic Swap）**：寫入時先在同目錄建立 `<uuid>.json.tmp`，完成所有寫入與 flush 後再透過 `ATOMIC_MOVE` 取代目標檔，防止突發斷電或進程中斷導致毀損。
  2. **備份保留（.bak）**：覆蓋已存在之舊草稿前，自動將舊有效資料複製為 `<uuid>.json.bak`。若主要檔案遺失或毀損，可隨時透過 `recoverFromBackup` 安全復原。
  3. **錯誤隔離與保護**：遇截斷、無效 JSON 或像素長度不符時，拋出 `DraftCorruptedException`，絕不讓遊戲崩潰，**嚴格禁止刪除或更動毀損之原檔案**，完整保留手動救援機會。
  4. **資源上限防護**：單檔限制最大 2,000,000 bytes（2 MB），草稿總數限制最大 200 份，杜絕異常或惡意檔案造成 OOM 或磁碟耗盡。
  5. **路徑安全防護**：`resolvePath` 嚴格校驗 `target.getParent().equals(baseDir)`，防範任何 `../` 目錄穿越攻擊。
- GUI 草稿管理與互動決策：
  1. **雙通道清晰分離**：
     - `Save Draft`（快捷鍵 `Ctrl+S`）：儲存 256×256 高解析度原稿至本機。
     - `To Firework`（儲存至煙火）：繼續輸出 32×32 `CanvasData` 至物品，絕不混淆或冒稱 32×32 煙火能還原 256×256 原稿。
  2. **編輯歷史完全隔離**：載入草稿（`loadDraft`）或新建草稿（`resetBlank`）時，強制清空 Undo/Redo 歷史堆疊、活躍筆畫與預覽快取，確保不同草稿之歷史狀態絕不交叉洩漏。
  3. **未儲存防護（Unsaved Changes Dialog）**：
     - 狀態以 `model.revision() != savedRevision` 嚴格判定。
     - 在有未儲存變更時，按 Esc 退出、切換草稿或新建草稿時，彈出 `UnsavedChangesScreen`，提供【儲存草稿】、【捨棄變更】與【取消】三項操作，避免使用者意外遺失創作。
  4. **獨立管理介面（`DraftManagerScreen`）**：
     - 整合滾動列表（`DraftListWidget`），顯示名稱、最後修改時間（`yyyy-MM-dd HH:mm`）、尺寸及 `[目前編輯中]` 標籤。
     - 提供 Load、New、Save、Save As、Rename、Delete（原版 `ConfirmScreen` 二次確認）及 Back。
     - 支援雙擊列表項目直接載入。
  5. **自適應排版安全**：右側控制項採 10 列緊湊排列（列高 21/22px），自適應視窗高度（`controls = 15..26`），在 240px 窄視窗下絕不超出視窗底緣，且與左側繪圖板和統計文字完全保持安全邊界。

## 2026-10-24 — Phase 05D 畫質檔位與採樣最佳化決策（正式結案）

- 使用者實機驗收結論與正式產品決策：
  1. **正式結案（PASS / CLOSED）**：使用者依據實機驗收與 Codex 獨立唯讀複核結果（Critical 0 / Major 0 / Minor 0，前次 4 項 Minor 全數 RESOLVED），正式批准 Phase 05D 結案。
  2. **Balanced 正式預設**：經使用者在 Minecraft 實機針對關鍵圖案（如「數字 6」）進行 A/B 評測，**Balanced（1100 顆核心粒子／366 樣本上限）** 視覺飽滿、孔洞清晰，觀感優於 High（High 因點位過密線條略顯壅塞，無比例清晰度收益）。正式將 `ParticleQuality.DEFAULT` 設為 `BALANCED`，`/drawfirework quality reset` 恢復 Balanced。
  3. **三檔保留與切換**：保留 `STANDARD`（800 顆／266 樣本）作為輕量基準，`HIGH`（1400 顆／466 樣本）作為測試上限，供玩家透過 `/drawfirework quality <standard|balanced|high|reset>` 自由切換。
  4. **採樣策略對照結論**：使用者回報在實體 Dust 粒子散布下，`Existing` 與 `Detail-Preserving` 之肉眼差異並不明顯，Detail-Preserving 並非對所有圖案都一定優於 Existing。暫行保留 `Detail-Preserving` 為預設，`Existing` 為對照，不額外耗費工時重寫採樣演算法。
  5. **流暢度與效能界限聲明（Accepted Limits）**：使用者於文書筆電測試回報 Standard、Balanced、High 三檔均維持 60 FPS 且無可察覺掉幀。此紀錄屬特定硬體環境之人工目視觀測，**非通用效能保證，絕不代表模組已無效能上限、或已完成專業 Profiler 效能測量**。各檔位嚴格受限於預算上限，絕不無限制累積。專業效能量測與進一步視覺比較記為可延後項目。
  6. **畫布資料邊界**：`CanvasData` 嚴格維持 32×32 格式；手繪 GUI 之 256×256 原稿永久儲存未實作（屬 Phase 06 範疇）。
  7. **光照獨立性**：Phase 05C 之 `fullbright_exact` 獨立光照預設完全不受影響。正式預設組合為 **Balanced + Detail-Preserving + Fullbright Exact**。
- 核心架構與實作設計：
  1. **三檔畫質檔位（`ParticleQuality`）**：
     - `STANDARD`：800 顆核心粒子，上限 266 樣本（$266 \times 3 = 798 \le 800$）。輕量基準檔位。
     - `BALANCED`：1100 顆核心粒子，上限 366 樣本（$366 \times 3 = 1098 \le 1100$）。**正式生產預設**。
     - `HIGH`：1400 顆核心粒子，上限 466 樣本（$466 \times 3 = 1398 \le 1400$）。測試上限檔位。
     - `ParticleQuality` 採用純 Java 實作，獨立於 Minecraft 渲染類別，並置於 `com.drawfirework.canvas`，完全遵守 Client / Common 架構邊界。
  2. **細節優先採樣（`DetailPreservingSampler`）**：
     - **邊界與特徵加權**：像素相鄰透明區（或畫布邊緣）為輪廓（權重 80），具 $\ge 3$ 個透明鄰居為端點／孤立點（權重 100），具高色差鄰居（$\Delta C > 64$）為色彩邊緣（權重 60），內部實心為平滑填色（權重 10）。
     - **預算動態目標分配（非僵化契約）**：初始以 70% 輪廓、30% 內部為目標。當某類像素不足時，剩餘預算動態回補至另一類（例如純線稿無內部像素時，預算 100% 分配予線條；實心塊邊緣少時，剩餘預算回補予內部；整數取整具備正常離散偏差）；在預算充裕時完整保留邊緣，在像素超出預算時確保空間均勻覆蓋（此為有損採樣客觀物理限制）。
     - **確定性保證**：空間剖分與代表點選取均採用固定整數雜湊（`tieRank`），100% 確定性與可重現，永不使用隨機種子。
     - **像素忠實度**：代表點直接取自玩家繪製之原始像素 ARGB，嚴格不改變 RGB 與 Alpha。
  3. **明確預算與 Quality 語意分離**：
     - 未指定預算（如 `CanvasOptions.DEFAULT` 或 `!hasExplicitBudget()`）時，採用當前 Quality 對應的樣本上限（Standard 266、Balanced 366、High 466）。
     - 呼叫端明確指定預算時（`hasExplicitBudget() == true`，如傳入 266），嚴格尊重呼叫端數值（在 Balanced／High 下維持 266），絕不以數值 266 作為隱式 sentinel 覆蓋；同時以當前 Quality 上限作為安全鉗制。
  4. **正交指令控制**：
     - `/drawfirework quality <standard|balanced|high|reset>` 與 `/drawfirework quality`。
     - `/drawfirework sampling <existing|detail_preserving|reset>` 與 `/drawfirework sampling`。
     - 品質與採樣設定為客戶端記憶體狀態，同客戶端跨世界保留，完全獨立於 Lighting 設定，絕不覆蓋 Phase 05C 之 `fullbright_exact` 正式預設。
  5. **預覽聯動與快取一致性（`CanvasPreview`）**：
     - `CanvasPreview.get` 納入 `quality` 與 `strategy` 狀態，當設定切換時立即重新生成 `DustPattern`。
     - GUI 預覽統計列自動反映當前檔位之精確粒子與樣本數（例如 `1098 粒子（366 樣本 × 3）`）。
     - 點擊 Save 儲存之 32×32 `CanvasData` 資料維持 100% 一致，符合伺服器 C2S 驗證契約。

## 2026-10-24 — Phase 05C 正式預設與結案政策（歷史紀錄）

使用者正式批准05C最終整合，將fullbright_exact設為正式預設。fullbright保留自然色差；baseline保留舊版環境光比較與回退；exact_only保留診斷。初始化、reset及null fallback共用DustLightingMode.DEFAULT；parse工具的default別名亦指向正式預設（Brigadier仍只接受四種完整模式名及reset）。

模式仍為HeartPrototype的客戶端本地欄位，僅在記憶體存活；同一客戶端跨世界保留選擇，重新啟動使用新預設。不寫世界／設定檔、不傳送lighting C2S。不變更世界光照、Mixin標記範圍、800核心／200裝飾限制、76-tick動畫或網路資料。

使用者接受的未測風險（Accepted Risk / Deferred Verification）：原版Dust完整實機隔離、所有四模式及日夜圖案排列組合、部分重啟／重進世界生命週期。未測不記為PASS；這些延伸驗證不再單獨阻擋05C結案。若有真實編譯／Mixin／安全／播放回歸仍列阻斷。使用者視覺回饋保持不變：Exact忠實飽滿、稍螢光可接受，白天略假暫不調色。

下列候選／baseline預設／PARTIAL判定是歷史決策，已由本次明確授權取代；保留追溯，不作新的批准循環。

## 2026-10-24 — Phase 05C 預設選型與驗證界限（歷史）

`fullbright_exact` 記為正式預設候選：來源 RGB 較忠實，明亮飽滿，稍偏螢光可接受；白天略假，使用者暫不要求調色。保留 `fullbright` 自然色差模式。實際初始化／reset／null fallback 仍為 baseline；Runtime 場景與隔離／清理驗收完成前不改預設。

本輪僅修文件；runClient 匯出 Particle 位元碼確認實際 Mixin 注入，不代表四模式在世界內顯示、未標記原版 Dust 或重進世界清理已通過。完整操作與模式的記憶體生命週期見 PHASE_05C_ACCEPTANCE.md。

## 2026-10-24 — Phase 05C 煙火可見度與光照機制決策（實作紀錄，證據敘述已收斂）

- 核心問題與背景：
  - 使用者長期反饋 Dust 煙火在白天偏暗，且夜間無火把等外部光源時幾乎完全不可見；而在夜間有外部光源處，視覺辨識度則顯著改善。原本的 Smiley 與新版手繪 Canvas 煙火均存在此問題。
  - 使用者明確要求保留 Dust 自然、柔和的煙火質感，不要直接更換為完全不同外觀的星狀煙火粒子。
- 05C.1 原始碼診斷與證據對照：
  1. **光照座標機制**：
     - Minecraft 26.3 `DustParticle` 繼承 `DustParticleBase` 與 `SingleQuadParticle`，未覆寫 `getLightCoords(float)`。其光照完全依賴 `Particle.getLightCoords()`，透過 `LightCoordsUtil.getLightCoords(level, pos)` 即時查詢世界方塊座標之天光與方塊光。
     - 夜間無光源時，方塊光 = 0、天光 = 4（甚至洞穴／覆蓋處為 0），光照貼圖 LightTexture 取樣值幾近純黑。
     - 夜間施放點有火把時，方塊光提升至 14，`LightCoords` 之方塊光位移分量 `(14 << 4)` 大幅拉高，使粒子被火把光源照射發亮（100% 吻合使用者觀察）。
     - 原版煙火 `FireworkParticles.SparkParticle` 繼承 `SimpleAnimatedParticle`，其 `getLightCoords(float)` 無條件回傳常數 `15728880`（`0x00F000F0 = LightCoordsUtil.FULL_BRIGHT`），具備 Fullbright 自發光特性。
  2. **RGB 隨機暗化機制**：
     - `DustParticle` 建構子計算 `multiplier = random.nextFloat() * 0.4f + 0.6f`（區間 $[0.6, 1.0]$）。
     - `DustParticleBase.randomizeColor` 額外乘上 `(random.nextFloat() * 0.2f + 0.8f)`（區間 $[0.8, 1.0]$）。
     - 總乘數範圍為 $[0.48, 1.00]$，平均值為 $0.8 \times 0.9 = 0.72$（平均暗化 28%，最差暗化 52%），且 R/G/B 分別獨立隨機調暗，導致原色產生色彩偏移與飽和度下降。
  3. **渲染圖層**：
     - Dust 使用 `SingleQuadParticle.Layer.OPAQUE`，開啟深度寫入且無 Alpha 混合；Spark 使用 `TRANSLUCENT`（透明通道與 OIT）。
     - 由於本專案在 Phase 03/04 已將 Canvas 透明度映射為粒子尺寸縮放（`alphaSize`，保底 0.35），故保留 OPAQUE 搭配 Fullbright 即可完全保留 Dust 柔和的 `minecraft:generic_0`..`7` 蓬鬆圓形粒子質感。
- 05C.2 最小可行架構設計：
  1. **Duck Interface 與專屬 Mixin**：
     - 建立 `com.drawfirework.client.GlowingParticleAccess`。
     - 建立 `com.drawfirework.client.mixin.ParticleMixin`，注入 `Particle.getLightCoords(float)` 之 HEAD。
     - 僅當粒子帶有 `drawfirework$fullbright == true` 標籤時回傳 `15728880`（`LightCoordsUtil.FULL_BRIGHT`）。
     - 預設值為 `false`，未標記路徑不取消原方法；這是靜態及本輪轉換後位元碼證據，原版粒子與第三方相容性仍需分別實測。
  2. **純 Java 色彩還原**：
     - `SingleQuadParticle.setColor(float r, float g, float b)` 為原版 public 方法。
     - 在 Exact 模式下，粒子生成後直接呼叫 `sqp.setColor(r, g, b)` 還原玩家手繪之直覺 RGB，完全消除 $[0.48, 1.00]$ 隨機暗化，無需額外 Mixin。
  3. **四種實驗模式正交分離（DustLightingMode）**：
     - `BASELINE`：維持現有正式行為（世界環境光＋原版隨機暗化），**嚴格作為正式預設**，絕不默默覆蓋。
     - `FULLBRIGHT`：Fullbright 光照（15728880）＋保留原版隨機暗化，獨立隔離光照變因。
     - `FULLBRIGHT_EXACT`：Fullbright 光照＋100% 精確還原玩家所選 RGB，測試最高清晰度。
     - `EXACT_ONLY`：世界環境光＋100% 精確還原 RGB，獨立隔離色彩暗化變因。
  4. **指令控制與全場景套用**：
     - 透過 `/drawfirework lighting <baseline|fullbright|fullbright_exact|exact_only|reset>` 即時切換。
     - 切換後全面作用於 `/drawfirework heart`、`/drawfirework canvas ...` 以及玩家手持自製煙火物品右鍵施放。
  5. **嚴格不變量**：
     - 粒子位置、取樣演算法（`CanvasDownsampler`）、核心粒子預算（上限 800）、五階段動畫（A20/B8/C14/D20/E14 共 76 ticks）與 Bloom 130% 嚴格 100% 不變。

## 2026-10-24 — Phase 05B.2 正式結案決策（歷史紀錄）

- 結案決策依據：
  - 基於程式碼 Commit `83062a995934ecd139bbb36e21eda7130a0a8534`，Codex 獨立技術複審確認 Critical 0、Major 0、Minor 0，所有歷史審查項目（Undo/Redo 筆畫生命週期、16 步上限、Dust 預覽三粒微偏移、Crisp 8-連通分支、小視窗文字排版、`maxTextWidth` 移除 40px 保底、降級安全省略階梯）全數 RESOLVED。
  - 使用者正式批准 Phase 05B.2 結案（PASS / CLOSED）。
- 本階段架構與關鍵決策總結：
  1. **轉換演算法**：Soft（預乘 Alpha 降採樣）與 Crisp（8-連通分支檢查，連續跨度 $\ge 4$ 時有界幾何平均補償 Alpha，不粗暴二值化）。
  2. **輸出預覽**：ViewMode（DRAW, CANVAS_32, FIREWORK）。Dust 預覽重用 `DustStyle.BLOOM_130` 之真實 3 粒微偏移採樣，按 `Math.max(w,h) / CANVAS_SPAN` 等比縮放，邊界裕度消除硬裁切。預覽與儲存 100% 共用快取。
  3. **歷史堆疊與記憶體安全**：`MAX_HISTORY = 16`，歷史快照像素資料常駐記憶體嚴格 $\le 4\text{ MiB}$（加固定工作陣列合計約 4.56 MiB，活躍筆畫峰值約 4.81 MiB）。操作優先結束活躍筆畫再判斷出棧，Redo 分支安全截斷。
  4. **非遞迴填色**：BFS 迭代演算法，容量 65,536，杜絕 StackOverflowError。
  5. **GUI 排版純 Java 解耦**：`CanvasEditorLayout` 獨立純 Java 模組，畫板動態計算文字高度；統計文字支援 Full $\to$ Short $\to$ Compact $\to$ Minimal $\to$ Number $\to$ Safe Omission；移除 40px 保底，文字右界永不侵入右側按鈕；說明文字受視窗底緣安全限制。
- 正式保留事項：
  1. 極窄視窗實機排版重測尚未執行，經評估為非阻斷事項（自動化測試中遍歷 50..800 寬度不侵入按鈕幾何不變量與安全省略已 100% 驗證）。
  2. Dust 夜間偏暗留待整體 GUI 完成後另行調查。
  3. Soft／Crisp 肉眼差異有限，列為視覺品質後續優化。
  4. 高解析度原稿目前只在編輯 Session 暫存，永久保存屬 Phase 06。
  5. Dust 預覽為二維示意，不等同完整三維煙火動畫。

## 2026-10-24 — Phase 05B.2 最終統計文字邊界與安全降級決策（歷史紀錄）

- 審查修復邊界：針對 Codex 最新複審提出的 2 項 Minor（小視窗統計文字侵入按鈕、`chooseStatsText` 未被 Screen 呼叫）執行最小範圍修正，嚴格不修改煙火播放引擎、三粒微偏移公式、Bloom、粒子預算或 16 步歷史上限。
- 移除 `maxTextWidth` 人為 40px 保底：
  - 根因：`CanvasEditorLayout.maxTextWidth(screenWidth)` 原硬編碼 `Math.max(40, pStart - 4 - MARGIN_LEFT)`。當視窗在極窄狀態下（如 $w=120$ 時按鈕前可用空間僅 16px，$w \le 90$ 時 $\le 0$px），此 40px 保底導致計算出的 `maxW` 大於實際按鈕前邊界，造成文字右側侵入按鈕。
  - 決策：將保底修改為非負整數保底 `Math.max(0, pStart - 4 - MARGIN_LEFT)`。在可用空間不足時如實回傳真實可用寬度，絕不人為放大。
- 消除邏輯重複與實作安全省略階梯：
  - 根因：`CanvasEditorScreen` 原維護自有的 `if (font.width(full) <= maxW) ... else if ... else ...`，且在所有標籤均過寬時無條件回傳 `compact`，未執行進一步降級或省略。
  - 決策：
    1. Screen 直接呼叫 `CanvasEditorLayout.resolveStatsText(...)`，其內部直接調用 `chooseStatsText(...)`。
    2. 建立逐級降級清單：Full $\to$ Short $\to$ Compact $\to$ Minimal（`preview_dust_minimal`，例如 `348 pts`） $\to$ 純數字（例如 `348`） $\to$ 安全省略（`null`）。
    3. 當可用寬度小於最短候選或 $\le 0$ 時，`chooseStatsText` 回傳 `null`，`resolveStatsText` 產生 `visible = false` 且 `width = 0` 的結果。Screen 在 `visible` 為 false 時安全不繪製任何文字，寧可留白省略，絕不出界或侵入按鈕。
    4. 32x32 預覽與 Draw 模式亦套用同一降級與省略機制，維持一致。
- 測試覆蓋：
  - 測試直接覆蓋產品方法 `CanvasEditorLayout.chooseStatsText` 與 `CanvasEditorLayout.resolveStatsText`。
  - 驗證各級降級切換點、Compact 溢出降級、非正數寬度省略、移除 40px 保底之各視窗寬度數值，以及遍歷 50..800 寬度下文字絕對不侵入按鈕的幾何不變量（`x >= 8` 且 `x + width <= panelStart - 4`）。

## 2026-10-24 — Phase 05B.2 最終 Minor 修正與測試架構收尾決策（歷史紀錄）

- 審查修復邊界：針對 Codex 最終複審提出的 3 項 Minor（小視窗文字排版、測試有效性、文件數字精確度）執行精確收尾，嚴格不修改真正煙火的播放引擎、三粒微偏移公式、粒子預算、Bloom、動畫、夜間亮度或 16 步歷史上限。
- 抽取純 Java 排版與投影邏輯（`CanvasEditorLayout.java`）：
  - 根因：先前的排版公式直接寫在 `CanvasEditorScreen` 內部，測試無法直接呼叫產品代碼而重複抄寫公式；且畫板縮放保留的底部空間為固定值（`height - 88`），未依實際說明文字折行行數動態縮放。
  - 決策：
    1. 將畫板幾何計算（`computeBoard`）、文字排版（`computeTextPlacement`）、簡潔文字選擇（`chooseStatsText`）及粒子投影與可視性判定（`projectX`, `projectY`, `projectionScale`, `particleDotRadius`, `isParticleVisible`）全數抽取至純 Java 的 `CanvasEditorLayout`，完全脫鉤 Minecraft 執行期環境，使所有幾何邏輯均能直接接受自動化單元測試。
    2. 畫板高度計算動態傳入測量所得的說明文字所需高度（`textHeightNeeded`），使畫板尺寸能依文字行數自動收縮或展開。
    3. 粒子統計文字（Line 1）在窄視窗下依可用寬度自動選用簡潔標籤（`preview_dust` $\to$ `preview_dust_short` $\to$ `preview_dust_compact`），且水平座標 `statsX` 嚴格拘束在 `[8, panelStart - 4 - statsWidth]`，絕不穿入右側按鈕區域。
    4. 說明文字（Line 2）使用 `font.split` 測量折行，並以 `maxAllowedDisclaimerLines`（依可用剩餘高度計算）進行有界繪製，絕不溢出視窗底部。
- 記憶體精確數值修正：
  - 歷史快照記憶體：因新操作必清空 Redo，Undo 與 Redo 堆疊在任何時刻合計快照數上限為 16 份。256×256 畫布每份快照 256 KiB，**歷史像素常駐記憶體上限為 4 MiB**（更正先前誤記的 8 MiB）。
  - 固定工作陣列：`pixels` (256 KiB) + `strokeBase` (256 KiB) + `coverage` (64 KiB) = 576 KiB (~0.56 MiB)。畫布基底常駐總量約 **4.56 MiB**。
  - 活躍筆畫暫態快照：在途筆畫額外持有 `strokeSnapshot` (256 KiB = 0.25 MiB)，峰值總量約 **4.81 MiB**。
- 測試有效性升級（`DustShapeFidelityTest.java`）：
  - 消除測試抄寫公式之壞味道，所有幾何與排版測試直接呼叫 `CanvasEditorLayout` 產品方法。
  - 包含歷史邊界裁切與文字重疊錯誤的負向對照測試，確保若產品代碼回退為舊版缺陷公式，測試必然報錯失敗。

## 2026-10-24 — Phase 05B.2 獨立審查修復與預覽排版／幾何保真度修復決策（歷史紀錄）

- 審查修復邊界：針對 Codex 審查提出的 2 Major、2 Minor 及使用者實機截圖發現的文字重疊與「6」字輪廓漂移問題執行精確修復，不推翻既有架構，不修改真正煙火的播放引擎、三粒微偏移公式、粒子預算、Bloom、動畫或夜間亮度。
- Dust 預覽文字排版防重疊與邊界拘束（CanvasEditorScreen）：
  - 根因：Line 1（粒子統計）座標基於畫板底部 `top + boardHeight + 5`，而 Line 2（二維示意說明）座標基於視窗底部 `height - 34`。當畫板高度隨視窗放大而延展時，兩者垂直座標撞擊重疊。且 Line 2 使用 `width - 16` 作為折行寬度，直接延伸至右側操作按鈕下方。
  - 決策：
    1. 畫板高度縮放預留充足的文字工作空間：`Math.max(1, height - 88)`，確保 `boardBottom` 與視窗底部始終保持至少 56 像素安全裕度。
    2. 兩行文字改為**嚴格依序垂直單向排列**：Line 1 在 `textY1 = top + boardHeight + 4`，Line 2 在 `textY2 = textY1 + 13`。因為 Line 2 錨定於 Line 1 之下，數學上在任何解析度／GUI Scale 下永不重疊碰撞。
    3. 折行寬度嚴格限制於左側工作區：`maxTextWidth = Math.max(40, width - panelWidth - 20)`，起點為 `x = 8`，右端點 `8 + maxTextWidth <= width - panelWidth - 12`，嚴格終止於右側按鈕起始座標（`start = width - panelWidth - 8`）之前，絕不向右溢出。
- 數字「6」輪廓幾何數據稽核與邊界裁切修復：
  - 稽核結果：
    1. 幾何方向與質心：數學推導與數值測試證實，畫布 $row$ 增加對應世界座標 $-Y$（向下），預覽投影以 $-s.y() * scale$ 反轉回螢幕向下方向，上下極性與左右極性完全正確；筆畫質心與粒子質心偏差 $< 0.002$ 區塊。
    2. 下緣漂移根因：舊版預覽使用硬邊界條件 `screenX <= left + boardWidth && screenY <= top + boardHeight`。畫布最下緣像素（如 16×16 畫布的 row 15）在三粒微偏移朝下時，粒子座標會超出畫板底線。舊版將超出底線的粒子直接丟棄，導致下緣只剩朝上偏移的粒子被繪製，造成非對稱的向上漂移感。
    3. 邊界裁切修復：預覽繪製加入安全邊界裕度 `margin = Math.round(preset.localOffset() * scale) + r + 2`，確保全數 348 顆粒子（116 samples × 3）100% 完整繪製，不發生邊界粒子遺失。
    4. 16×16 粒子分散特性：微偏移固定為 `0.14f` 區塊，在 16×16 粗網格上（間距 0.3094f）佔間距達 45.2%，離散階梯像素自然呈現 3 粒微偏移點簇；32×32 網格（間距 0.1547f，微偏移佔 90.5%）則因相鄰粒子雲密集重疊而呈現平滑筆畫。真實物理公式不因預覽而竄改。
- Major 1（Undo/Redo 筆畫生命週期修復）：
  - 核心變更：`undo()` 與 `redo()` 均調整為**優先呼叫 `endStroke()` 結束／提交在途活躍筆畫**，再依最新堆疊狀態安全決定是否出棧。
  - 根因：先前版本在 `redo()` 內先檢查 `!redoStack.isEmpty()`，隨後呼叫 `endStroke()`，若有活躍新筆畫被提交，`commitHistory()` 清空了 `redoStack`，導致後續 `redoStack.removeLast()` 拋出 `NoSuchElementException`。修復後 `endStroke()` 在先，新筆畫清空 Redo 後，`if (redoStack.isEmpty()) return false` 立即安全返回。
  - 活躍筆畫 Undo：在第一筆或任意筆畫繪製途中（滑鼠按住）按 Ctrl+Z，`endStroke()` 先將筆畫前狀態入棧，隨後 `undo()` 立即出棧並復原畫布，使在途筆畫能即刻被乾淨撤銷。
  - `CanvasEditorScreen` 在快捷鍵與按鈕觸發時先呼叫 `input.reset()`，確保滑鼠輸入狀態與模型狀態完全同步。
- 歷史深度限制（16 步）：
  - 經使用者核准，`MAX_HISTORY` 由 32 步調整為 16 步。
  - 記憶體評估：單一快照 256 KB，`undoStack` 與 `redoStack` 各最多 16 個快照，常駐記憶體合計嚴格 $\le 8\text{ MB}$。
- Major 2（Dust 預覽三粒微偏移與矩形比例修復）：
  - 預覽重用實際產品 `DustStyle.BLOOM_130.sample(i, k, pt.x(), pt.y())`，完整繪製每個採樣點的 3 顆微偏移粒子，呈現與遊戲爆炸時一致的 Bloom 簇狀形態。
  - 矩形比例修正：因 `CanvasDownsampler` 將最長軸正規化為 `CANVAS_SPAN`，預覽投影縮放改用 `scale = Math.max(boardWidth, boardHeight) / CANVAS_SPAN`，使非正方形畫布（如 32×16）能完全按正確比例填滿畫板，消除額外縮小缺陷。
  - 視覺邊界透明化：底部清楚標示為二維粒子分布示意圖，明確聲明不包含 Minecraft 實際 3D 透視、夜間環境光、深度衰減、Bloom 動態呼吸及 A–E 五階段生命週期，不虛構三維一致性。
- Minor 1（Crisp 8-連通輪廓演算法升級）：
  - 原行列佔據計數（bitCount）升級為 64 位元點陣圖的 8 方向膨脹連通分支檢查（`hasContinuousContour(mask)`）。
  - 僅當同一個 8-連通分支的邊界跨度 $\ge 4$ 時才觸發 Alpha 增益；分散的噪聲單點不會因跨越行列被誤判為線條，完全保留原 Soft 模式輸出。
- 保留事項：
  - Soft／Crisp 轉換在低解析度小圖上的視覺差異相對細微，保留為後續視覺觀察。
  - Dust 夜間偏暗問題為原版 dust 渲染特性，繼續延後至整體 GUI 完成後另行調查。
  - 原稿永久儲存保留為 Phase 06。

## 2026-10-24 — Phase 05B.2 首次交付決策（歷史紀錄）

- 臨時角色調整：因 Codex 額度用盡，由使用者授權 Gemini／Antigravity 擔任唯一程式開發代理，Codex 暫停；嚴格保留既有安全規範、Java 25、Minecraft 26.3、不自動 commit/push，同一代理不自行宣稱通過獨立審查。
- 智慧轉換（FreehandResampler）：
  - 保留預設 `Mode.SOFT` 進行等面積預乘 Alpha 降採樣，維持半透明水彩感。
  - 新增 `Mode.CRISP` 模式：在 8×8 區塊中偵測具有方向連續性之細線或輪廓（佔用行數或列數 $\ge 4$）。若偵測到連續細線且面積平均 Alpha 低於尖峰值，以嚴格有界的幾何平均公式 `a = Math.min(max, Math.min(a * 2, (int) Math.round(Math.sqrt(a * (double) max))))` 進行輪廓增益。
  - 嚴格禁止粗暴二值化：增益後 Alpha 永不超過 2 倍、永不超過區塊最高 Alpha（`max`），細線絕不變為 100% 不透明。
  - 嚴格保護孤立雜點與完全透明區域：孤立雜點（span $< 4$）輸出與 Soft 完全相同；完全透明像素維持 0，RGB 嚴格不變。
- 煙火輸出預覽（CanvasPreview & CanvasEditorScreen）：
  - 實作三種視圖切換：`DRAW`（高解析手繪）、`CANVAS_32`（32×32 縮圖畫布與透明微棋盤）、`FIREWORK`（真實 Dust 粒子分布與點數統計）。
  - `CanvasPreview` 以 `(owner, revision, mode)` 快取輸出產物，避免每幀重複計算。
  - 儲存時發送的 `CanvasData` 100% 取自 `preview.get(model, mode).canvas()`，保證預覽與最終儲存資料絕對一致。
  - 預覽模式下阻斷畫布滑鼠點擊與拖曳，防止預覽時誤畫。
- 歷史復原與重做（Undo / Redo）：
  - `FreehandCanvas` 維護 `undoStack` 與 `redoStack`，最大深度嚴格限制為 32 步（`MAX_HISTORY = 32`），先進先出淘汰最舊歷史，避免記憶體無界膨脹。
  - 單次筆畫（按下至放開或出界）與單次填色均視為單一歷史操作。
  - 復原後若產生新繪製操作，立即清空 Redo 分支（截斷重做歷史）。
  - Cancel / Esc 退出直接關閉介面，不向伺服器發送封包，ItemStack 與伺服器 Session 絕不受影響。
- 油漆桶填色（Flood Fill）：
  - 使用固定容量為 65,536 的單一原始型別 `int[]` 佇列進行迭代 BFS 搜尋，完全非遞迴，杜絕 `StackOverflowError`。
  - 支援透明背景填色（遇到不透明筆畫停止）以及橡皮擦填色（將相連色塊一鍵重設為透明 0）。
  - 邊界防護：出界、非有限數值、點擊相同顏色自動 no-op，不新增無謂歷史。
- GUI 操作與快捷鍵：
  - 右側面板整合 8 色調色盤、筆刷粗細滑桿、橡皮擦、填色、Undo、Redo、Mode（Soft/Crisp）、View（Draw/32x32/Dust）、Cancel 及 Save。
  - 支援快速鍵：`Ctrl+Z`（Undo）、`Ctrl+Y`／`Ctrl+Shift+Z`（Redo）、`B`（筆刷）、`E`（橡皮擦）、`F`（填色）、`M`（轉換模式切換）、`P`（預覽視圖輪播）。

## 2026-10-24 — Phase 05B.1 自由手繪決策（歷史紀錄）

- Codex 唯一寫入；基準 HEAD `0a00372187cf172a0ab39c175acc869b7ce97e13`，起始工作樹乾淨。05A PASS / CLOSED 保持歷史結案；本次只實作正式授權的05B.1。
- 新 `FreehandCanvas` 不引用Minecraft：私有ARGB工作陣列、筆畫起始陣列、byte覆蓋率陣列，最大256×256。工作尺寸為輸出尺寸各乘8，預設32×32→256×256；1×32等矩形同比例放大，不把矩形拉伸成正方形。最大三個緩衝區合計589,824 bytes，不含物件標頭／原始CanvasData。
- 原始CanvasData不可變，未實際改變像素時snapshot直接回傳原始值，保留透明像素的隱藏RGB及所有舊尺寸。改動後才縮圖；不做反覆工作畫布→輸出→工作畫布的編輯循環。完整原稿只存在本次Screen，關閉後物品只保存輸出。
- 圓形筆刷直徑1..64工作像素，預設8。連續浮點座標按距離補點，最大間距max(0.25,diameter/8)，邊緣使用一工作像素的距離覆蓋率近似抗鋸齒；沒有宣稱是精確圓面積積分或曲線預測。沒有加入會延遲游標的濾波器。
- 同一筆畫採最大覆蓋率，從strokeBase做source-over，避免補點重疊使半透明墨色堆積。下一筆才重新合成；透明橡皮擦使用destination-out且支援同一直徑。有效範圍外輸入終止筆段，重新進入不跨越畫布補線；不把外部座標夾到邊緣造成長邊界線。
- `FreehandResampler` 對每個輸出像素積分精確8×8等面積樣本；先累加alpha與RGB×alpha，再還原straight ARGB並四捨五入。透明RGB不參與混色，不會帶入黑邊。採編碼RGB空間，未加入linear-light色彩管理；多色邊界仍可能混色。沒有不透明化的細線保護。
- 面積覆蓋率取捨：1px工作細線縮圖後淡，32×32亦存在階梯與細節損失。離線比較顯示nearest對細線明顯高估覆蓋率且圓周採樣不均；保留area方案，人工須比較8／16／32筆刷實際Dust可辨識度。Dust本身不支援完整alpha，既有取樣至266點／798核心粒子的限制保留；不聲稱GUI等於煙火預覽。
- GUI用單一DynamicTexture／NativeImage，不用逐格Widget／逐格fill。資料revision有改變才於下一次render extraction更新既有NativeImage並upload，同一幀多個輸入合併；未改動幀不掃描畫布、不配置畫布陣列。上傳整張最多256KiB，未實作dirty rectangle。Screen removed經TextureManager.release關閉紋理；resize重設筆畫但保留工作圖。
- 工具改到右側，保留畫板空間；固定灰色透明背景、無格線，8色／橡皮擦／滑桿／儲存／取消，底部換行警語明確說明不保存高解析度原稿。G／Esc與client/server儲存流程保留。
- 無component的新物品由ServerCanvasEditor與EditSession一致採32×32。只有這兩個新圖預設值改動；既有尺寸驗證、權限、token、stack綁定、時限、限頻、payload codec及component schema不變。沒有新增封包。
- 原05A CanvasEditorModel／CanvasEditorInput與其測試暫留作歷史相容程式；本次Screen改用FreehandCanvas／FreehandInput，新測試直接呼叫目前產品方法。舊測試不作為新GUI已實測證據。
## 2026-10-24 — Phase 05A 繪圖互動修復決策（最新）

- 具體缺陷來自上一版產品硬編碼左鍵為0；26.3本機InputConstants.MOUSE_BUTTON_LEFT=1。不是Minecraft API失效，也沒有證據顯示CanvasData、網路或renderer造成此次阻塞。
- 僅將Screen直接相關的click／drag／release／reset筆畫狀態抽成CanvasEditorInput；同一處理器用於實際Screen及JUnit，具名LEFT判斷共用，不在測試複製條件或座標公式。原CanvasEditorModel的indexAt／paint／pixel、渲染及保存內容均不改。
- 未命中畫布的click回false交還原生按鈕；只有畫布內左鍵click啟動drag。已啟動的drag在畫布外仍被消耗，由原model.paint安全重設筆畫；左鍵release即reset，即使游標在外也結束。初始化／調色盤／橡皮擦切換同樣reset，防止舊筆畫續接。
- 用本機MouseButtonEvent與LEFT常數的4項測試先確認舊predicate全部FAIL，再修為70JUnit／clean build／Phase02回歸PASS，補上舊測試直接paint繞過輸入條件的缺口。
- 使用者只回報修復前GUI開啟與繪圖FAIL；修復後所有runtime待重測。最新狀態固定Phase 05A PARTIAL — Waiting for manual retest，不以單測推定人工通過；不改網路／component／04C.2，不commit／push或做05B。

## 2026-10-23 — Phase 05A 編輯與權威儲存決策

- 採原生26.3 Screen／GuiGraphicsExtractor及原生Button；G為預設，可於Creative類別重設。client entrypoint獨立CanvasEditorClient，common只引用資料、item及server網路。既有HeartPrototype不改。
- 本階段新空白16×16（使用者05A明確要求，優先於舊全域DEFAULT_SIZE=32）；不改CanvasData.blank的舊語意。既有component每維1..32全部可編輯、原尺寸與完整ARGB保留，不裁切、不重採樣。無component開啟時不寫預設，只有保存才建立。
- client編輯狀態持有像素防禦性複本；鉛筆8種不透明色，橡皮擦ARGB=0；Bresenham補相鄰有效mouse採樣，越界重設筆畫，避免離開再進入畫出跨區線。GUI座標用Screen的縮放單位，不自行乘視窗DPI。取消／Esc無save封包。
- 使用server開啟握手而非相信client舊ItemStack；server產生UUID一次性session，綁定真實stack身分、slot、原CanvasData、完整stack copy、count及dimension。client不能自行宣稱原圖或物品身分。原圖用CanvasData.equals（Arrays.equals）比較；同型同值的另一stack也不能冒用session。
- C2S save及S2C opened共用ItemCanvasCodecs.STREAM_CODEC，不建立另一套ARGB資料格式。body上限4116bytes；open恰1byte，專用封包拒尾隨。server每次validate，禁止尺寸改變（05A無resize工具）。持久化沿用原component，不保存token或第二份canvas到物品。
- 開啟／保存各每玩家10ticks限頻，分開計避免剛開即存被擋；每玩家一個session，12000server ticks後過期。server END_SERVER_TICK偵測主手／slot／原值／components／mode／dimension等失效並清除；斷線及server停止清理。狀態不持有player／level強參照，不新增無界session歷史。
- 儲存前一次性消耗token，失敗不進寫入callback；只有server set component、setChanged、broadcastChanges後發送成功訊息。client的「已送出」與server「成功」分開；不以client預測顯示成功。保存關閉GUI，結果在聊天回覆，失敗須重新開啟。這是MVP取捨，沒有新增結果動畫／undo／草稿。
- 保存永不播放，原右鍵04C.2路徑完全保留；heart、Warm／Cool／Grid、Fixed／Dynamic、A20/B8/C14/D20/E14=76、Bloom130、三粒／最多798核心與獨立200裝飾、UseGate、S2C、clear及原verify程式均未改。verify仍要求原Smiley，任意編輯圖重載另作人工驗收。
- Phase04D SKIPPED / NOT REQUIRED：必要資料與播放由已PASS的04C.1／04C.2涵蓋，使用者確認沒有剩餘獨立交付，不增加開發。05B、06草稿、07配方、多人廣播均不進行。
- 自動驗證66JUnit及三組回歸／clean build PASS，只證明編譯與可隔離邏輯；GUI／真實stack／network／存檔視覺仍未驗收。最終PARTIAL，操作與限制見PHASE_05A_ACCEPTANCE.md，等待使用者及Antigravity唯讀審查。

## 2026-10-23 — Phase 04C.2 播放邊界

- 04C.1 已依使用者確認正式 PASS，包含 ItemStack 遊戲診斷與重載，不重測舊階段來阻擋授權。
- 單一 drawfirework:play_canvas S2C，不傳 ItemStack；header version=1、UUID 兩 long、dimension UTF(最多128字元)、中心三 double、既有 ItemCanvasCodecs.STREAM_CODEC。decode 最大4654 bytes，拒絕未知版本／尾隨／截斷、非法 Identifier、NaN/Infinity、絕對值超過30000000的任一座標；Canvas 1..32、1024 pixels、最多4099 bytes。metadata 先驗才配置 pixels。
- server 讀實際觸發手的 test_firework component，驗 creative/alive/channel capability；從眼睛＋視線12格取得中心，不接受 client 座標。對邊界外中心安全拒絕，不建立火箭實體，不消耗。UseGate 每玩家10 tick、保留一個 last tick，先保留再 send 防重入。WeakHashMap key不被value引用，不永久保留離線玩家，不新增完整冷卻系統。
- client handler 顯式 execute，播放前確認捕捉世界物件及 player 身分仍相同、dimension相同、整合伺服器存在、UUID 非最後事件；只保存一個 UUID。斷線清理及既有世界清理；不維護無界佇列。沒有時間同步系統：有效當前世界事件從 tick0 完整播放，沒有跨世界補播；網路可靠有序，本輪僅整合伺服器，不承諾多人延遲同步。
- 指令及物品共用 canvasPattern 和 activate；DustPlane 僅新增明確中心工廠，update/position 相機幾何不變。物品使用 DEFAULT Fixed、Bloom130、A20/B8/C14/D20/E14、最多266×3=798核心、獨立裝飾預算及單一效果替換。sound 只於 client 動畫呼叫，server不播放第二份。
- verify 指令語意保持只檢查資料／往返，不觸發播放；物品名稱移除已過時的尚無動畫文字。

## 2026-10-23 — Phase 04C.1 物品資料邊界

- 暫定 component ID drawfirework:canvas、item ID drawfirework:test_firework；單堆疊最多 1。只存一份 CanvasData，不存 JSON 字串、DustPattern 或第二份像素。一般 CanvasData.MAX_SIZE 維持 256。
- CanvasSerialization.boundedCodec(maxSize) 共用版本化 argb32-hex 格式、EXACT_INT 及 decode；原 CODEC 用 256，物品用 32。width/height 各自 1..32，乘積最多 1024；pixels 字串最多 8192 字元且須精確等於 area*8、有效 hex，format／version 必須支援，像素 int[] 在驗證後才配置。編碼端也拒絕超限。
- 網路明確指定 StreamCodec，避免 builder 自動從 persistent Codec 推導較大的表示。三個 unsigned byte 表示 version/width/height，其後 row-major ARGB int；最多 4099 bytes。先驗標頭與可讀資料量才配置；無獨立 count 欄位、無壓縮炸彈；不拒絕容器後續 bytes，因它們可能是下一個 ItemStack 欄位。這不是 04C.2 的事件封包。
- Persistent Codec 的 string bound 是已建 NBT／JSON 值到 CanvasData 的讀取邊界，不宣稱能阻止上游 NBT tree／字串的所有配置。未知額外欄位依 RecordCodecBuilder 語義忽略且不保存，不是草稿 fromJson 的嚴格未知欄位政策；遊戲 NBT loader 的外層讀取限制仍必要。沒有新增不受限檔案讀取。
- 取得方式選伺服器指令 /drawfirework_test_item；requires 與執行時均檢查真實 server player.isCreative，無任意目標／像素輸入。只有 creative 權限，不額外要求 operator；伺服器主執行緒處理指令，滿背包先拒絕，不掉落或覆寫其他格。標準 creative 庫存封包的遊戲模式邊界另以本機 bytecode 核對；不信任 client-only 判斷。
- 物品 use 的 client 分支只回 PASS；server 分支拒絕 survival、缺資料及超限資料。有效 creative 只回報尺寸／畫布 hash、尚無動畫、不消耗；伺服器回 SUCCESS_SERVER。沒有對渲染器或粒子程式的引用。
- 普通 JUnit 註冊真正 Minecraft component／item 失敗於 Not bootstrapped registry；不加入反射解凍或假的 ItemStack。改以 Creative-only verify 指令在 live registry 執行真正 ItemStack.CODEC、STREAM_CODEC、copy 與缺／壞 component 檢查，並驗主手實際持有的是原 Smiley 完整值。尚未執行，不列 PASS。此診斷僅配置暫時堆疊／buffer，不改背包／世界，buffer finally release。
- 保留既有動畫、粒子預算與 downsampler；未實作任何後續階段。

## 2026-10-23 — Phase 04B 有限配色與相同渲染入口

- 採 DevCanvases.SmileyPalette（ORIGINAL／WARM／COOL）；smiley() 委派 ORIGINAL，保留原始四區 ARGB。三配色共用唯一一份幾何迴圈，執行時配置新的 CanvasData；不快取可變像素，也不預建重複畫布。
- WARM：face FFFFB347、ring 80FF7043、eyes FFA83232、mouth FFFF4060；COOL：face FF45C9E8、ring 806988FF、eyes FF3040A0、mouth FFB875ED。透明背景仍為 00FFFFFF，alpha mask 不變。Dust 原版隨機明暗不能保證螢幕呈現精確 ARGB。
- 新增 /drawfirework canvas smiley_warm 與 smiley_cool，直接使用既有 CANVASES supplier、canvasNode、startCanvas、start 與 tickDust；保留選項次序 [100|125|150] [128|192|266] [plain] [dynamic|fixed]。有限 literal 不接受未知預設，enum 不接受 null；不暴露尺寸、任意顏色或不受限參數。
- 不修改 clear／生命週期：start 在接受有效樣本後先 clear，再指派新 pattern 與新 DustPlane；clear remove 所持核心／尾跡粒子並清空 velocities、plane、owner、center、age、pattern 與計時狀態。此為接線靜態核對，不代替 Launch/Hold/Dispersion 實際替換與世界切換驗收。
- Fixed 預設、Dynamic 對照、100% 尺寸、Bloom 130%、時序、三粒模式、預算與 Phase 02 heart 不變。沒有抽取大型渲染架構的必要。
- 使用者已確認 04A 獨立 PASS、1 Major／2 Minor CLOSED、33 JUnit／三組回歸／5/5 遊戲回歸通過；對應 1243af9、9040a35、8246713。04B 新變體畫質與執行期接線另待人工驗收，不沿用 04A 結果冒稱新功能 PASS。

## 2026-10-23 — Phase 04A 收尾：Fixed World Plane 正式決策

### 一、正式渲染決策

1. **Canvas 動態圖案預設使用 Fixed World Plane**：
   - 於 tick 20 爆炸瞬間依玩家視線建立顯示平面，在 Formation 與 Hold 期間保持固定世界朝向，不隨相機每 tick 旋轉。
   - Dispersion 與 Fade 保持原本的世界物理散射行為。
2. **保留 Dynamic 作為明確開發對照模式**：
   - 指令支援 `dynamic` 參數（例如 `/drawfirework canvas smiley dynamic`），保留 Dynamic 模式以利後續對照與分析。
3. **Phase 02 原始愛心保持不變**：
   - `/drawfirework heart` 及其所有子指令維持 Phase 02 原版視覺行為（Dynamic 模式），完全不受 Canvas 預設改動影響。
4. **既有動畫與質感參數維持**：
   - 嚴格保留 Bloom 130%、Hold 14 ticks、五階段動畫時序與 800 核心粒子預算。
   - 正式 Canvas 預設仍保留三粒微偏移（`BLOOM_130`）；`plain` 繼續作為診斷對照模式，不直接作為正式煙火預設。
   - 150% 整體尺寸保留為可選配置（指令參數 `150`，例如 `/drawfirework canvas smiley 150`），暫不直接改為全局預設。

### 二、使用者人工驗收紀錄與未完課題

- **Fixed World Plane 朝向穩定性**：人工驗收 PASS，確認圖案在世界中如同實體看版般穩定，轉向視角不會引起空中旋轉。
- **Fixed Smiley**：使用者反饋明顯優於 Dynamic。
- **Smiley 150%**：使用者偏好放大後的大小，但放大後粒子間距拉大，顯得較為分散；此現象留待後續階段（如 Phase 05/08）評估密度或採樣微調。
- **Plain 對照組**：Grid Plain 明顯較整齊，Rect Plain 稍有改善；證實微偏移在細線幾何圖案上有分散效果，但微偏移為煙火星芒所必需，故保留作為診斷模式。
- **Dense 266 點效能**：最近一次測試正常，無明顯卡頓。
- **Phase 02 原始愛心**：人工驗收正常，視覺回歸無損。

### 三、重要分析更正（時序分析之客觀性修正）

- **更正說明**：先前 04A.2 報告將 0°→45°→0° 歪斜現象「100% 歸因於相機旋轉與 Dispersion 時序競爭」的說法過於武斷與絕對，必須修正。
- **事實補充**：使用者測試時除了轉動視角，亦曾實際移動玩家世界座標。
- **客觀定論**：代碼中已證實 Dynamic 模式在進入 Dispersion 瞬間確實停止位置跟隨，但在玩家實際平移、透視投影縮放以及不同視角觀測的綜合交互作用下，也可能造成當時的視覺扭曲感。因此不能宣稱時序競爭已排除所有其他潛在因素。Fixed World Plane 在爆炸時保存世界朝向，Formation/Hold 不因後續相機旋轉而自轉；世界中心不隨玩家平移。它不保證螢幕投影形狀不變：側向觀看仍有透視縮短，距離改變仍影響投影大小；原先歪斜的單一成因未獲確認。

## 2026-10-23 — Phase 04A.2 相機朝向與幾何穩定性決策

### 任務 1：朝向異常定位（7 項排查結論）

| 排查項目 | 已確認結論 / 理論依據 |
| --- | --- |
| 1. 初始顯示平面建立 | 確定：指令觸發時鎖定世界中心 `center = eyePos + lookAngle * 12`；tick 20 爆炸時以相機當下 `camera.rotation()` 建立平面；+X 為相機右、+Y 為相機上、+Z 為相機深度微偏移。 |
| 2. 玩家轉動／位移更新 | 確定：Formation (20..27) 與 Hold (28..41) 期間，每 tick (20Hz) 以 `rotation.set(camera.rotation())` 重算粒子世界座標；但 Dispersion (42..61) 與 Fade (62..75) 期間 `positionDust()` 停止執行，粒子完全由速度與重力在世界中自由運動，**不再受相機旋轉影響**。 |
| 3. 各階段位置鎖定機制 | 確定：Launch（無核心粒子）→ Formation/Hold（動態跟隨相機）→ tick 42 瞬間（以當下最後一次 hold 角度初始化粒子速度 `v = offset * 0.02`）→ Dispersion/Fade（世界物理模擬，位置由速度推進）。 |
| 4. 粒子座標系性質 | 確定：粒子為原版 `Particle`，存放在 `ClientLevel`，其 `x, y, z` 為**真實世界座標**；不存在額外的頂點相機相對轉換矩陣。 |
| 5. 相機更新與粒子狀態同步 | 確定：`camera.rotation()` 為 Euler YXZ 旋轉（已由 bytecode 確認 `rotationYXZ(PI - yRot, -xRot, 0)`），每幀更新；`tickDust()` 每 tick (50ms) 更新粒子 `x, y, z`；兩 tick 之間由原版渲染管線以 `lerp(partialTick, xo, x)` 平滑內插。無狀態不同步或競爭。 |
| 6. 0° → 45° → 0° 可逆性 | **歷史分析（舊公式測試不足以證明產品控制流程；由 Review Fix 01 更正）**：<br>• **若在 Hold 結束前（tick 42 前，即爆炸後 1.1 秒內）轉回 0°**：旋轉四元數與世界座標完全可逆（誤差 < 1e-6），無累積漂移。<br>• **若轉回時已超過 tick 42（進入 Dispersion）**：動畫已凍結於 45° 的朝向並開始向外擴散！粒子處於物理散射狀態，不再跟隨鏡頭。當使用者在 tick 45 或 50 轉回 0° 時，看到的正是**凍結在 45° 的物理擴散星雲**。這是一種可能解釋，尚不能確認為唯一原因；使用者觀察到「有時（轉太慢時）呈現 45° 歪斜，而不是恢復原本樣子」！此為動態跟隨（Design B）過渡到散射的行為假設；先前自建公式測試不能排除實際渲染或座標接線問題。 |
| 7. 視線轉動 vs 橫向移動 | 確定：視線轉動改變 `camera.rotation()`，使圖案繞 `center` 自轉面向相機視線；而玩家實際橫向平移時，`center` 仍留在原地（不會跟著玩家位移），若玩家將十字準星重新對準 `center`，圖案亦會旋轉至正面面向玩家。 |

### 任務 2：渲染行為確立（Design A vs Design B 分析與診斷設計）

- **Design A（固定世界平面，Fixed Plane in World）**：
  - *機制*：在 tick 20 爆炸瞬間鎖定相機視線方向為固定平面；Formation、Hold、Dispersion 全程維持此世界朝向不變。
  - *優點*：圖案在世界中絕對穩定；鏡頭晃動不會造成煙火在空中自轉；0° → 45° → 0° 全程 100% 幾何不變；極度符合多人在同一個世界觀察同一個物理爆炸（V1.1 友好）。
  - *缺點*：若玩家在爆炸後大幅橫向走到側面（90°），會從側面看到扁平的側面視圖。
- **Design B（動態相機跟隨，Continuously Facing in Formation/Hold，既有預設）**：
  - *機制*：Hold 結束前（14 ticks = 0.7s）每 tick 跟隨相機視線旋轉；進入 Dispersion 瞬間凍結。
  - *優點*：玩家在爆炸初期移動時圖案始終正面朝向螢幕。
  - *缺點*：玩家轉頭時空中圖案跟著自轉；若 Hold 結束瞬間玩家視線偏斜，散射軌跡即永久凍結在偏斜角度（使用者回報的「有時卡在 45°」現象）。
- **診斷實作（最小、可逆、安全）**：
  - 新增指令旗標 `fixed`：`/drawfirework canvas <name> [scale] [budget] [plain] [fixed]`。
  - 不加 `fixed` 時保持 100% 原版 Design B 行為與 Phase 02/04A 預設；既有 `heart` 指令完全不變。
  - 加上 `fixed` 時啟用 Design A（鎖定 tick 20 朝向），供使用者在遊戲內直接對比 Design A 與 Design B 的視覺感受。

## 2026-10-23 — Phase 04A.1 幾何與辨識度診斷

### 資料流追查（已確認 = 有測試/程式證據；推測 = 無證據，待遊戲內確認）

| 項目 | 結論 |
| --- | --- |
| 2D→3D 映射 | 已確認正確：`offset = rotate(camera.rotation, (x·spread, y·spread, z))`，+Y 向上、中心 (0,0) 固定、X/Y 同一比例；僅 `HeartPrototype.positionDust`，無額外變形。 |
| 中心／軸／長寬比 | 已確認：降採樣座標為像素中心，除以長軸（`CanvasDownsampler`）；Rect 24×16 測得中心距比 23/15、Grid/Rect 左右上下鏡像對稱、x 落在均勻像素格點上（`DustGeometryDiagnosticsTest`）。 |
| 整數取整／精度／邊界 | 已確認無問題：無取整，float 誤差 <1e-4 pitch。 |
| 降採樣器不對稱 | Smiley(≈145 可見像素)、Grid(225)、Rect(144) 均 ≤266，**全像素精確取樣、無損**。僅 Dense(1024→266/192/128) 為自適應分格：最近鄰距（單位=原像素）budget266 mean 1.35 max 2.24；192 mean 1.53 max 3.16；128 mean 1.68 max 3.16，**非均勻格點，屬 Phase 03 設計行為，非資料錯誤**（未改演算法）。 |
| 三粒固定偏移 | **已確認為主要可量化放大因素**：`DustStyle.localOffset=0.14` 是絕對值（方塊），不隨取樣間距縮放。offset/pitch：heart 0.31、smiley 0.45、grid 0.71、dense32 0.91。pitch 越小，偏移越接近整個格距，直線與輪廓被打散；z 深度 ±0.042（localOffset×0.3）。Heart 因 pitch 0.45 較寬才看起來整齊。 |
| 相機方向／透視 | 推測（未證實，亦未排除）：平面每 tick 以 `camera.rotation()` 更新（20 Hz），轉頭時畫面更新率較低；玩家橫移時平面朝向視線方向而非朝向中心，斜看會透視縮短。**不認定為唯一原因。** Grid/Rect 與 `plain` 配置用於分離此因素。 |
| Dust 粒子本身大小 | 推測（未查證原版 quad 尺寸）：Dust 基礎大小每顆隨機，且 Bloom 130% 放大；dense pitch 0.155 方塊時相鄰粒子大量重疊，造成模糊。 |
| Smiley 模糊成因 | 可能組合（未量化）：(1) offset/pitch 0.45 打散輪廓（已確認）、(2) 粒子重疊與隨機尺寸（推測）、(3) 半透明環以較小粒子近似（Dust 無 alpha，已知）、(4) 4.95 方塊寬在 12 方塊距離可能偏小（推測，用 125/150% 比較）。顏色邊界：smiley 僅黃/藍/紅/青，邊界由降採樣精確保留，無證據是原因。 |

### 診斷配置（皆可逆，預設不變）

- `canvas <name> [100|125|150] [128|192|266] [plain]`：scale 只乘以樣本間距（`DustPattern.scaled`，範圍 1.0–1.5，中心固定、長寬比/顏色/點數/Dust 單顆大小不變）；budget 只能壓低 266，永不提高（`fromCanvas(canvas, copies, maxSamples)`，3 粒/點，最多 798）；`plain` = `DustStyle.BLOOM_130_PLAIN`（localOffset 0，其餘與 Bloom 130% 相同：hold 14、bloom 1.30、尺寸變化 .05）。
- 預設（無選項）= 100%/266/有偏移 = Phase 04A 行為；`BLOOM_130`、Heart、時間軸、Bloom、降採樣演算法、粒子上限均未改。
- 新診斷圖案：`grid`（25×25，黃直線/青橫線每 6px，紅色中心十字）、`rect`（24×16，白邊 2px，四角紅/綠/藍/黃）。
- 計時：CPU 端 `[04A.1 timing]` 日誌（prepare＝指令時降採樣；spawn＝爆炸 tick 的 createParticle 迴圈；hold＝每 hold tick 的 `positionDust` 平均/最大）。這**不是** FPS/frametime，渲染端 GPU 成本不在內。

### 尚未有證據的結論

Dense 卡頓是否來自生成瞬間、Hold 或 GPU 渲染未知；沒有 frametime 量測，不宣稱已修復。客戶端「啟動等待」屬 Gradle/runClient 載入，非本 mod 程式所能測量，未量測。視覺品質 PASS 尚未判定，等使用者人工比較。

## 2026-10-23 — Phase 04A 畫布接入 Dust 決策

- **介面**：新增純 Java record `com.drawfirework.firework.DustPattern(List<Point>)`，`Point(x, y, rgb, sizeScale)` 為 billboard 本地方塊座標（+X 右、+Y 上）。放在共用套件、不引用任何 Minecraft/客戶端類別，未來伺服器可產生同一定義。`HeartPrototype` 只把原本的 MASK 迴圈換成走訪 `pattern.points()`；`DustStyle.sample`、時間軸、Bloom、解體與淡出程式未改。
- **回歸基準**：`DustPattern.HEART` 以與 Phase 02 相同的 float 運算式（`(col-5)*.45f`、`(4-row)*.45f`、同色表、row-major 順序）產生 63 點；Dust 尺寸 `1.2f*1f` 與原值相同；image 比較模式也改讀同一常數，幾何不變。
- **尺寸**：畫布樣本座標（長軸 [-0.5, 0.5]）乘以 `CANVAS_SPAN = 11 × 0.45 = 4.95` 方塊，與愛心寬度一致；長寬比沿用降採樣器的長軸比例。
- **透明策略**：alpha 0 不產生點（由 CanvasDownsampler 排除，RGB 不影響）；alpha 1..254 保留 straight RGB（不預乘），因原版 Dust 為 OPAQUE、無 alpha 混合，改用面積比例的較小粒子 `sizeScale = max(0.35, sqrt(alpha/255))`；alpha 255 為 1。已查本機 26.3 `ScalableParticleOptionsBase` 尺寸 clamp 0.01..4.0，`1.2×0.35=0.42` 在範圍內。此為近似，需遊戲內視覺確認。
- **預算**：樣本上限 = `ParticleBudget(800, particlesPerPoint, 0).sampleLimit()`；Bloom 130（3 粒/點）最多 266 樣本 = 798 粒。`start` 前再以 `coreParticles()` 驗證，超過即拒絕。裝飾 200 預算未動用。
- **開發入口**：`/drawfirework canvas smiley|dense` 僅為 04A 遊戲內檢查用的固定畫布，限單人、沿用「一次一個效果」與 clear 清理；不是物品或玩家內容來源，04B 起可替換。

## 2026-10-23 — Phase 03 資料與降採樣決策

### 模型與序列化

CanvasData為final class，建構與pixels()各自防禦複製，pixel(x,y)無陣列複製；equals/hashCode以內容計算。尺寸先限1..256再以long乘法/toIntExact計算面積，預設32²、version1。Straight ARGB、row-major；alpha0的隱藏RGB保留但不採樣，alpha1..255保留原值、不混黑底、不隨機捨棄。

JSON範例：`{"version":1,"width":1,"height":1,"format":"argb32-hex","pixels":"80ff0000"}`。每像素8字元AARRGGBB，輸出小寫/輸入大小寫皆可。hex避免65536個裝箱Integer，代價為二進位資料的2倍。最大合法輸出524360 ASCII bytes；輸入硬限600000字元且ASCII-only，所以同為UTF-8 byte上限。拒絕缺欄/重複/未知欄、錯型別、非法尺寸/版本/格式/hex/長度、額外JSON、超大資料。

fromJson先檢查長度再用strict streaming JsonReader，不建整棵JSON tree，不遞迴skip未知資料；最多5欄與有界字串。驗證尺寸/長度才配置int[]，交CanvasData時再作必要防禦複製。**呼叫者已持有輸入String；Phase06仍須在讀檔前/過程限制bytes，不能先無界readString。** 本階段無檔案管理。

CODEC與JSON入口共用Wire→CanvasData驗證。EXACT_INT以BigDecimal.intValueExact拒絕小數/溢位，避免一般Number.intValue截斷。CODEC作用於已存在的DynamicOps資料，不限制外部parser先前配置，原始JSON須走有界入口。RecordCodecBuilder可忽略未知欄且Codec可接受數學整數1.0；草稿JSON入口拒絕未知欄並要求整數token1。這是明確的邊界差異，不宣稱Codec自行限制外部總輸入大小。未註冊Data Component/StreamCodec/封包。

### 演算法選型

採「非透明佔用空間自適應二分＋原色代表像素」。掃描可見包圍盒與alpha加權重心，選距重心最近的真實可見像素。優先切分可見像素最多的區域，沿較長軸中點分割，再收緊子區包圍盒；兩端有可見點，分割兩側必非空。達預算或所有像素獨立即停。等距候選採固定整數hash排名，消除固定取左上偏差；無Random/可變seed。輸出按原像素索引排序，List.copyOf不可變。

| 候選 | 取捨與實際合成測試 |
| --- | --- |
| nearest-neighbour固定網格 | 簡單、取到即保留原色；32²原點孤立紅像素→4²網格完全漏掉（0點） |
| 區域平均 | 保留平均覆蓋率但稀釋細線；同例alpha僅255/64=3.984375；紅藍棋盤格平均成紫(127.5,0,127.5) |
| 自適應二分（採用） | 孤立像素保留1點/alpha255，細斜線代表仍在線上，不在透明區造點，不跨色界混色；棋盤格32²→16點紅10/藍6，保留色盤但比例不精確 |

首輪固定左上tie令棋盤格16點全紅；測試揭露後改固定hash等距排名。有限預算仍可能漏小型分離特徵/稀有色、偏向區域內部、產生棋盤格混疊；不保證任意輪廓或色彩比例完美重建。策略優先可見支撐/原色，並非保持平均光能；weight為所代表可見原像素數，**不是粒子倍數或自動亮度放大係數**，不含透明面積。坐標為像素中心相對畫布中心、+Y向上、兩軸同除max(width,height)；透明邊框不裁切/放大圖形。

### 預算、效能及Phase04建議

- 原始最多65536 pixels，採樣budget0..800，0輸出空；其他輸出min(可見像素數,budget)，小圖不湊滿。ParticleBudget分開核心/副本/裝飾，BLOOM=800/3/200，最多266點/798核心；coreCount先驗證再乘，不借用裝飾額度。
- N=W×H、B=點數、D≤ceil(log2 W)+ceil(log2 H)≤16。每層掃描區域不重疊、所選軸每次至少減半，時間O(ND+B log B)，額外空間O(B)，不複製原畫布或配置每像素物件。模型/JSON時間空間O(N)，最大int[]每份262144 bytes，解碼含防禦複製短暫兩份；字串heap大小依JVM，不把字元數當heap。
- 本機探針256²→266約2.54ms/次、41939 allocated bytes/op，詳見TEST_RESULTS；不是遊戲frame time/GPU/FPS保證。
- Phase04建議小型轉接層：將Sample按明確世界尺寸縮放，RGB送Dust，ParticleBudget與實際DustStyle副本數核對；決定alpha映射（既有Dust opaque，不能宣稱直接支援精確半透明）與weight用途。FireworkTimeline/Bloom可保留，這輪不接線、不重構。
- 審查建議300–500點在3粒/點時超過800粒，不採用；後續調密度須同時計算副本。HeartPrototype/DustStyle/FireworkTimeline本輪完全未改。

## 2026-10-23 — Phase 02最終選型：Bloom130（取代預設115）

使用者完成02.6實際比較並正式選130%；其圖案更飽滿，綻放自然流暢，Hold14合適，Formation/Dispersion/Fade認可。02.5相較舊版明顯改善、14tick合適的既有觀察保留。100/115皆為開發比較，130為heart及heart dust預設；legacy/image繼續保留，不是主要方案。
本輪只有HeartPrototype初始化與無preset的start兩處改用BLOOM_130；不改DustStyle/FireworkTimeline、三粒/點/原色/位置/時間/清理或Bloom方程。最終20+8+14+20+14=76 tick，核心189，裝飾整段建立≤19，保守上界208；不是實測峰值或FPS保證。每粒大小增加不代表新增點數。
使用者仍見輕微稀疏及某些斜角圖案變形。兩者列後續研究，根因尚未查明；未證實與billboard、投影、原版碰撞或其他機制的因果關係，本輪不增加粒子/改朝向/重構。未來研究須先收集視角/距離/相機狀態與錄影證據。
02系列實作與視覺選型就此凍結；提交合併02–02.6的完整實作/測試/紀錄，保留歷史內容與已驗證工具鏈。完整清理/效能等未測範圍仍明確列出，不能以使用者「流暢漂亮」推定所有驗收PASS。

## 2026-10-23 — Phase 02.6 單粒Bloom比較

保留使用者已確認較佳且動畫自然的02.5；不改Hold14、每點3粒、顏色、固定seed/偏移、樣本間距、中心/平面比例、A/B/D/E時間與速度方程。只將原有逐粒size控制乘上Bloom envelope，沒有放大整幅圖的座標或增加粒子。

- DustStyle新增bloomTarget；SOFT（normal）=1、BLOOM_115=1.15、BLOOM_130=1.30，預設115；舊四參數建構子保留並預設1，LEGACY仍原樣。相同sample方法不讀bloomTarget，因此三配置點位完全相同。
- B結束age28開始由1以smoothstep(t)=t²(3−2t)過渡，age32達目標，4 ticks；原有±5%微變乘在Bloom上，不宣稱每幀總尺寸恆為115/130%。使用目標/上次size倍率避免指數累積。
- D開始不重置Bloom為1：記住最後Hold的size，elapsed0保持原值，前4 tick僅平滑將殘餘微變收斂至Bloom目標，D後續維持目標，E從放大後尺寸沿既有14 tick縮小倍率消失。沒有新粒子接替、位置重排或時間改動。normal/legacy保留原02.5過渡行為，便於原樣比較。
- 原生SingleQuadParticle.scale(float)乘quadSize，並呼叫Particle.scale更新碰撞盒尺寸；setSize不直接寫入x/y/z。這個API不是絕對size setter，故必須傳ratio。沒有動sample/center/setPos計算；仍在開闊天空驗收，原版碰撞行為與清理機制保留。
- 預算仍核心63×3=189、裝飾整段≤19，保守同時≤208；legacy≤82。Bloom增加sprite覆蓋面積（可能增加overdraw），不是增加點數；沒有GPU/frame time量測，不能從粒子數不變推論性能完全相同。
- 啟動等待只診斷，不改Gradle設定/快取/工具鏈。根據daemon14592，build執行12:00:48.412→Minecraft Loading12:01:29約41秒，→視窗12:01:54再25秒；daemon6136同一前段約7秒，確有差異，但既有非verbose日誌不足區分依賴解析、JVM或磁碟I/O。未找到Downloading/Resolving記錄不等於證明無網路。細節及非破壞性建議見TEST_RESULTS。

## 2026-10-23 — Phase 02.5 保守Dust質感配置

使用者已實測Phase02五階段正常、效果漂亮；但Hold呈現規則稀疏點陣。本輪只做藝術質感微調，不改Dust主方案/工具鏈/GUI/資料/多人。這是使用者已驗證的Phase02觀察，不是Codex自行觀看證據。

- 集中於DustStyle.SOFT：holdTicks=14、particlesPerPoint=3、localOffset=.14f、sizeVariation=.05f。可直接修改此單處預設重建；允許每點1–3粒、偏移0–.2、大小強度0–.1、Hold1–100，非有限數拒絕。未加入正式設定或GUI。
- 固定種子SplittableRandom按點及粒子索引生成圓盤局部偏移，只有建立圖像時一次配置；平面半徑≤.14、深度≤.042格，相對.45格間距保守擾動。三粒皆取原行COLORS，保留原版隨機色彩，不額外改色。輪廓邊緣可能稍軟，需實測。
- 從B開始就使用相同sample，連同局部偏移乘formation spread；C不再變動sample。大小微變以固定phase與tick正弦驅動、端點包絡歸零，scale使用新/舊倍率防止逐tick累積膨脹；D恢復基準尺寸並停止setPos，不生成/搬移新粒子，沿帶偏移的原位置接續速度。沒有新增位置抖動。
- SOFT區間A[0,20)、B[20,28)、C[28,42)、D[42,62)、E[62,76)，正常20TPS共3.8秒。壽命=76−20+10=66 tick。LEGACY保留28 tick Hold、1粒/點、零偏移/變化，總90 tick，壽命80；指令heart dust legacy。相同原版隨機色彩的每次具體取樣不保證一致，位置偏移則固定可重現。
- 核心189、裝飾整次最多19，保守同時上界208；LEGACY保守上界82。核心建立一次、裝飾僅A每tick1粒，B/C每tick最多189位置/大小更新；D首tick189向量、之後189速度更新，E189縮放。單效果替換與remove清理保留；原版引擎可提前剔除，不能將上界當作實際存活量或FPS承諾。無新增每tickRandom或sample重建。
- 舊靜態FireworkTimeline常數/方法保留供Phase02回歸測試，運行中的Dust使用preset所屬timeline，image仍維持Phase01對照。沒有新增Minecraft API，沿用已查證setLifetime/scale/setPos/setParticleSpeed。新畫面未觀看，選型暫待人工比較。

## 2026-10-23 — Phase 02 Dust生命週期

- Phase01使用者正式確認PASS，提交d1513ed與審查872bd52存在；沿用已接受的11×9遮罩、63點、0.45間距、scale1.2及原版隨機顏色/soft sprite。預設heart改Dust；image僅保持Phase01對照，不擴充成主方案。
- 明確tick區間：[0,20) Launch，從中心下6格升空；[20,28) Formation；[28,56) Hold；[56,76) Dispersion；[76,90) Fade；90清理。一般20TPS共4.5秒，lag不以牆鐘跳階段。Launch聲在start一次、blast在age20一次；沒有實體、世界爆炸或破壞。
- age20建立63個原版Dust並setLifetime(80)，超過圖像至結束的70 tick，避免原版隨機8–40乘scale的自然死亡。沒有補生；保留原版亮度/色彩。B/C每tick按camera旋轉校正，D固定最後hold朝向的釋放向量，不再setPos或歸零速度，以連續速度及.96阻力/柔和下落交給原版粒子位移。沒有瞬移到新軌跡。
- 本機DustParticleBase.getLayer為OPAQUE，setAlpha為protected；不使用反射/Mixin或更換材質。本階段E採原版公開scale逐tick縮小軟粒子，保留隨機顏色，最後remove；不是精確alpha淡出，也不宣稱已驗證其視覺柔和度。最終剩1/15尺寸後清除。若人工觀察不理想，只在本階段調整。
- 維持單實例/最多1煙火是刻意範圍選擇；新指令先remove舊粒子。審查建議的多煙火控制器不屬當前必要驗收，不引入多發系統。核心最多63、裝飾整次最多建立19（每Launch tick1個、壽命10、清單剔除死亡項），各自低於800/200。結束、clear、世界參考不同、玩家缺失或相機缺失皆清理。core/null建立失敗容錯，不假造成功點數。
- 靜態工作量：每次最多82粒子建立，B/C每tick63位置更新與Vector3f；D首tick63向量，D/E每tick最多63速度更新，E再63次scale。O(63)，無每tick補生或逐點封包；仍缺frame time/GC實測，不作FPS承諾。只替換不累積，未加入品質GUI/正式設定。純時間邏輯獨立測試，不更換測試依賴或工具鏈。

# 決策紀錄

## 2026-10-23 — Phase 01 正式選擇 A：Dust（取代下方暫選 B）

依使用者本次在單人世界的實際A/B測試，正式採Dust為Draw & Hide Firework主要視覺渲染方向。Dust能正常顯示，使用者評價視覺非常好、符合原先煙火構想；自然粒子感、顏色與圖案辨識度應優先保留。Image亦能顯示，但顏色/邊界過於鮮明、平面貼圖感強，與Minecraft煙火風格不協調，僅保留技術對照，不作產品預設方向。

本決策取代先前依靜態分析暫選B的結論。原版隨機亮度/壽命是需管理的技術特性，不能據此否定已獲使用者認可的Dust視覺。後續藝術效果以粒子動畫、閃爍、軌跡、散射及淡出為主；避免為了幾何整齊而變成天空中的硬邊平面圖。

本次不修改Java、metadata或工具鏈。**目前主方案測試請用 `/drawfirework heart dust`；裸指令 `/drawfirework heart` 仍是先前原型的Image alias，並不代表正式產品方向。** 改alias可留待下一次授權的程式變更；保留明確的image對照指令。

### Dust擴充評估（靜態審查，不是效能量測）

- 結構可作Phase02起點：client entrypoint隔離、tick驅動age、世界固定中心、共用固定遮罩、Particle引用清單與clear；無payload、伺服器狀態或Mixin。現有單類別符合原型最小範圍，不需本次重構。
- 每次63次粒子建立請求，99格遮罩遍歷；同時最多1效果，新指令先清除舊效果。核心最多63點，裝飾0點，低於800/200兩項獨立預算。粒子實際回傳可能null，位置更新會檢查null與isAlive。
- 每tick最多63次位置/速度校正及63個Vector3f配置，正常20TPS上限約1260個Vector3f/秒（從程式推算，非實測配置率）；總工作量O(99)。沒有每tick補生，但指令無冷卻，連續觸發仍會產生配置壓力；尚無frame time、GC或大量效果數據，不可外推到800點或多人。
- 目前positionDust每tick歸零速度並把存活粒子拉回圖案；它適用形成/停留，不能直接用於自由散射。Phase02授權後應將階段控制與位置更新分開，D/E階段改用受控軌跡/重力/淡出，保留自然粒子感；不在本次實作。
- 原版粒子獨立隨機壽命可能早於36 tick結束；延長到完整五階段前，須核對26.3可用生命週期控制，在不破壞既有外觀的前提驗證hold與淡出。使用者未提供逐tick完整率，不能宣稱28 tick全部點完整存活。
- clear會remove所持粒子並清空世界/中心/age；36 tick及世界引用變更亦呼叫clear。這是程式路徑檢查，指令clear、退出重入與殘留測試仍未通過實測。

偶發啟動0xC0000005目前僅有使用者報告，未提供可對應的原生崩潰堆疊；重啟後兩原型均可測。根因未知，沒有證據歸因Dust，本次不猜測或更換依賴。

## 2026-10-23 — Phase 01：暫選 B，自訂暫時圖像

單一 client 類別與 metadata 入口實作兩個原型；使用共同固定11×9像素遮罩、63個可見點、每格0.45格方塊間距，透明位置不繪製。中心在觸發時視線前12格，之後固定於世界，平面隨相機轉向。未加入自由繪圖或伺服器狀態。

| 比較項 | A：vanilla dust | B：自訂 client image |
| --- | --- | --- |
| 實作 | 63個 DustParticleOptions；每 tick 校正位置及速度，不重生、不覆寫原版壽命/顏色 | COLLECT_SUBMITS + submitCustomGeometry，一次提交63個彩色quad／252頂點 |
| 清晰度/顏色 | soft sprite；26.3 DustParticle 建構子有隨機亮度與各色通道乘數，非精確原色 | 固定RGB、硬邊色格；是否在實際畫面正確仍待驗證 |
| Hold/壽命 | 原版 DustParticleBase 的隨機壽命約8–40 tick再乘scale 1.2；可能在36 tick結束前缺點 | 8 tick形成，28 tick完整停留，36 tick清除；不依牆鐘 |
| 朝向 | 點陣每tick跟隨camera quaternion；個別sprite由原版處理 | 每frame使用camera render-state orientation，中心固定 |
| 距離 | 初始12格；實際12/24/48格可讀性、粒子選項影響待測 | 同尺寸與位置；實際距離、遮擋、第三人稱待測 |
| 效能 | 最多63粒子與每tick位置校正；沒有重生補點 | 1次geometry submit、63 quad，沒有粒子物件；每frame pose/callback配置 |
| 成本/相容 | 既有particle engine；本機26.3編譯確認 | 既有debugQuads管線，無texture/shader/mixin；本機26.3編譯確認 |
| 未來同步 | 可由有界事件觸發，但每客戶端原版隨機壽命/色彩不同 | 可由有界事件本機重建同一圖；尚未設計/實作同步 |

暫選B為預設 `/drawfirework heart`，因為可以直接控制完整hold、色彩與幾何；這是本機依賴實作及原型設計支持的暫定選擇，不宣稱已完成遊戲視覺A/B比較。A仍可透過 `/drawfirework heart dust`實測，避免沒有對照入口就定案。

並行效果上限1，新指令替換舊效果；核心63點、裝飾0點；不提供尚無量測依據的品質選單。36 tick、clear指令、客戶端世界引用改變會清除持有的粒子/世界/中心；沒有遠端封包、物品或世界破壞。Phase 02才處理完整發射、解體、淡出與音效。FPS、frame time與配置壓力尚未量測。

## 2026-10-21 — Phase 00

- 沿用 Gemini 的既有專案，不初始化 Git、不重建 Wrapper、不下載範本。build 與 clean build 已成功，沒有修改工具鏈的證據需求。
- 保留 myfirstmod、My First Mod、com.example、1.0.0。與 drawfirework 規格的差異僅記錄；產品版本不可從既有範例版本號推定。
- 保留 Loom 1.17.16、Gradle 9.6.1、Loader 0.19.5、Fabric API 0.163.0+26.3、Minecraft 26.3；不套用 V2 候選值。未使用 Loom snapshot。
- 原始 Wrapper 因使用者快取鎖檔無寫入權限失敗。本次程序將 GRADLE_USER_HOME 指向已存在的 D:\minecraftmod\gradle，沿用已有下載與快取；未改全域變數或專案設定。受限執行 build 無法連接 daemon；相同命令在放寬執行限制後成功。
- clean 靜態審計：build.gradle/settings.gradle 沒有自訂 clean/Delete 任務、apply 外部腳本或額外子專案，無刪除遊戲資料設定；僅執行既有 Gradle clean，不手動刪除任何世界或草稿。
- 未為缺失圖示擴大修改；這是尚未實測的資源問題，不阻止目前編譯。
- 渲染、配方、資料編碼及品質尚未選型，不預作 Phase 01–10 實作。核心800/裝飾200為規格上限，並非效能量測。

## 2026-10-23 — Phase 00.5 decisions

- Read AGENTS.md and the user-provided Antigravity Phase 00 read-only review. Core source/config hashes match the earlier Phase 00 record; no existing repository was found. Explicit current authorization supersedes the earlier prohibition on git init/commits.
- Initialize local Git only. Ignore gradle/* except wrapper/, and ignore everything in wrapper/ except gradle-wrapper.jar and gradle-wrapper.properties. This excludes wrapper/dists as well as caches/daemon/native/notifications/.tmp. No caches or user data were removed or moved.
- Rename identity to drawfirework / Draw & Hide Firework / com.drawfirework.DrawFirework; Maven group com.drawfirework, archive/root project drawfirework. Remove missing icon declaration. No asset files existed to migrate. Retain mod_version=1.0.0 as inherited metadata, not a release-completion claim. No gameplay/client code added; no dependency changes.
- Gradle User Home remains process-scoped D:\minecraftmod\gradle. Future safe separation: choose a writable directory outside the repository (for example D:\GradleUserHome\drawfirework), close builds/IDE Gradle activity, stop daemons from a working local shell, set GRADLE_USER_HOME in that shell/IDE, and let Wrapper populate the new location. Verify --version and clean build before adopting it. Keep the old location intact for rollback; never move/delete gradle/ wholesale because it contains versioned Wrapper files. Fresh dependency downloads may require network access. No migration performed now.
- Official five entry points were reopened on 2026-10-23: 26.3 migration announcement, docs, develop, fabric-api and fabric-example-mod. Documentation home still displays 26.2; no new Minecraft APIs introduced or inferred from it.
