## 2026-10-26 — GitHub 發布前整理驗證（本輪最新）

環境：D:\minecraftmod；HEAD `54c657638dbb196e27189c44d3287f77eec48a0a`；master／無 remote／index 無 staged 差異；Java Microsoft 25.0.3+9-LTS，Windows 11 amd64。程序級 `GRADLE_USER_HOME=D:\minecraftmod\gradle`。Minecraft 26.3／Loader 0.19.5／Fabric API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1 均未升降版。

| 實際命令／檢查 | 結果 |
| --- | --- |
| `java -version`、`.\gradlew.bat --version` | Java 25.0.3+9-LTS／Gradle 9.6.1，exit 0 |
| `.\gradlew.bat clean build --no-daemon`（沙箱內） | exit 1，Could not connect to the Gradle daemon；非編譯失敗 |
| 相同命令（經工具核准沙箱外重跑） | exit 0，BUILD SUCCESSFUL in 23s，7 tasks executed；存在 Gradle 10 棄用提示 |
| 解析新產生 `build/test-results/test/TEST-*.xml` | 29 suites，189 tests，0 failures/errors/skipped |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS，時間軸純邏輯 |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS，樣式／預算純邏輯 |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS，Bloom 純邏輯；三者不是遊戲視覺測試 |
| .NET ZipFile 開啟正式 JAR、逐 entry 讀取 | 106 entries；作者／MIT／版本 1.0.0／Minecraft 精確 26.3／無 contact、suggests 均核對 PASS |
| JAR LICENSE 比對 root LICENSE | 完整文字相同；必要 entrypoints class、theme class、Mixin JSON、語系及物品 JSON 均存在 |
| JAR entry 清單 | 無 Java 原始碼、JUnit test class、巢狀依賴 JAR、草稿、世界、日誌或金鑰檔案 |
| `git check-ignore -v`（假設路徑，不建立檔案） | 現行／舊 config／其他遊戲實例 drafts 與 .env.local、saves 均排除；無已追蹤檔被新增規則誤排除 |
| 可發布文字檔 Select-String／檔名掃描；`git log --all -G` | 私鑰／GitHub／AWS／Slack／OpenAI 常見秘密格式零命中；現行文字另掃引號包住的 password/client_secret/api_key/access_token 賦值零命中；29 個可達 commits 的高辨識格式增刪差異未命中。僅啟發式，不保證任意格式秘密不存在 |
| `git diff --check` | exit 0；僅 LF/CRLF 正常提示 |
| 原有檔案 SHA256 前後逐檔比對 | 121 檔中 112 檔完全不變；9 檔僅為本輪 metadata／建置／文件修改，0 遺失；原 Java、測試、語系與 Wrapper 均位元組不變 |

產物：`build/libs/drawfirework-1.0.0.jar`，**194754 bytes**。
SHA256：`817185DE858B31B99C1D708434364B2FA42B7279A7BC8EBE93D4480E0AE95FC9`。
未混淆架構由標準 `jar` task 產生，非 remapped JAR；不因修改發行文件重跑無關功能驗收。

Compiled PASS；Unit-tested PASS；Runtime-tested／Visually verified／Multiplayer-tested **本輪未執行**。獨立 Launcher 安裝仍待使用者依 RC01 指引回報。既有 Phase06／07A 人工觀察保留，沒有虛構新結果。第三方檔案來源與授權限制見 THIRD_PARTY_NOTICES 及 GITHUB_PREPARATION。

## 2026-10-25 — Phase 07A — Artist's Canvas GUI Redesign 正式結案驗證（PASS / CLOSED）

Status: **PASS / CLOSED**（依據使用者 6/6 人工實機驗收通過、Codex 獨立技術審查無阻斷缺陷，以及 Minor 1 透明填色修復完成正式結案）

環境：D:\minecraftmod；Baseline HEAD `54c657638dbb196e27189c44d3287f77eec48a0a`；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 26s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **29 suites、189 tests、0 failures、0 errors、0 skipped**（全數 PASS，新增 1 項透明填色測試） |
| `CanvasEditorThemeLayoutTest` | **5/5 PASS**；新增 `testEraserToFillAllowsTransparentFloodFillAndUndoRedo` 驗證自橡皮擦切換至油漆桶填色可成功執行透明區域消除、Undo/Redo 正常往返，以及切換回畫筆時色彩正確復原；既有 4 項響應式排版、木框不遮擋畫布、羊皮紙底色不污染像素、按鈕語意分離測試全數 PASS |
| 既有 28 個測試套件（184 項測試） | **184/184 PASS**；包含 `DustShapeFidelityTest`, `CanvasPreviewTest`, `FreehandInputTest`, `CanvasEditorInputTest`, `DraftGuiLogicTest`, `DraftManagerLayoutTest`, `DraftStorageTest`, `DraftSafetyTest`, `CanvasCompatibilityTest` 等全數通過 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，193,996 bytes |

**結案核對與人工驗收成果：**
- **使用者 6/6 人工實機驗收通過**：羊皮紙質感、立體木框、精簡 8 列圖示工具列、Tooltip 提示、按鈕選中/停用狀態、白色與淡黃筆觸對比均符合畫家工作台氛圍。
- **Codex 獨立審查結果**：Critical 0 / Major 0 / Minor 1，唯一 Minor 1（透明 Fill 切換回歸）已修正並由 `testEraserToFillAllowsTransparentFloodFillAndUndoRedo` 自動化測試覆蓋。
- **Release Candidate 01 準備狀態**：Phase 07A 正式標記 PASS / CLOSED，產出 193,996 bytes 之 remapped 正式發佈 JAR。

## 2026-10-25 — Phase 06 高解析度草稿持久化正式結案驗證（PASS / CLOSED）

Status: **PASS / CLOSED**（依據 Codex 最終獨立唯讀審查通過與使用者 6/6 人工實機驗收通過正式批准結案）

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 32s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **28 suites、184 tests、0 failures、0 errors、0 skipped**（全數 PASS） |
| `CanvasCompatibilityTest` | **5/5 PASS**；任意 256×256 精細原稿在 128×128 工作畫布拒絕載入並拋出例外、原稿與備份位元組 100% 保持未修改、拒絕時當前畫布筆畫、像素、Undo/Redo 歷史與 Dirty 狀態完好保留、16×16 煙火物品輸出快照不受影響；標準 256×256 畫布 100% 無損載入；舊 16×16 / 32×32 物品往返維持 100% 雙向相容 |
| `DraftSafetyTest` | **9/9 PASS**；完整草稿校驗阻絕損壞主檔覆蓋有效 .bak；`schemaVersion > 1` 的主檔即使 `force=true` 也絕對禁止被復原覆蓋；合法損壞主檔正常從備份復原；主檔有效時拒絕無條件復原 |
| `DraftGuiLogicTest` | **6/6 PASS**；舊目錄遷移遇相同 UUID 且內容不同時安全保留兩檔為 `.legacy_conflict.json`；真實 DraftStorage 儲存失敗保留畫布與 Dirty 狀態；未命名草稿彈窗取消保留畫布；Dirty 狀態生命週期與歷史堆疊隔離 |
| `DraftStorageTest` | **10/10 PASS**；完整 256×256 ARGB 無損儲存與還原、多草稿管理、Save As 隔離、Rename 保留像素、備份復原、版本拒絕、毀損容錯、路徑穿越防護、超大檔案防護、名稱消毒 |
| `DraftCanvasIntegrationTest` | **4/4 PASS**；載入草稿重設歷史堆疊與 revision、resetBlank 清空、resample 至 32×32 煙火輸出、非法長度拒絕 |
| `DraftManagerLayoutTest` | **2/2 PASS**；$[140 \times 140]$ 至 4K 螢幕自適應排版無重疊、清單與按鈕安全邊界 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，175155 bytes |

**結案核對與人工驗收成果：**
1. **Codex 最終獨立唯讀審查**：
   - 審查結果：Critical 0 / Major 0 / Minor 1。
   - 唯一 Minor（`docs/PHASE_06_ACCEPTANCE.md` 原子寫入安全措辭）已修正為忠於非原子替代移動與無斷電保證實作，非阻斷性問題。
2. **使用者 6/6 人工實機驗收全數 PASS**：
   - 儲存第一份草稿：PASS
   - 完整重啟 Minecraft 後重新載入：PASS
   - 多份草稿與另存：PASS
   - 未儲存變更提醒：PASS
   - 刪除與返回：PASS
   - 煙火輸出：PASS
   - 使用者反饋：沒有感受到明顯問題。
3. **保留已知限制（Accepted Limits）**：
   - 草稿儲存為客戶端本機資料，不支援多人同步（多人同步屬未來 Phase 09/10 範疇）。
   - 256×256 草稿不代表世界煙火輸出已提高至 256×256（維持 32×32 CanvasData）。
   - 非原子檔案系統及斷電安全性不作絕對保證。
   - 128×128 舊尺寸編輯器可能拒絕無法無損表示的 256×256 草稿。

## 2026-10-25 — Phase 06 最終資料完整性修復驗證交付（Final Data Integrity Remediation）

Status: **PARTIAL**（3 Major 與 3 Minor 最終資料完整性修復全數完成，全部自動化測試與回歸檢驗 PASS，等待使用者實機驗收與 Codex 獨立唯讀複核）

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 28s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **28 suites、184 tests、0 failures、0 errors、0 skipped**（全數 PASS，新增 8 項測試） |
| `CanvasCompatibilityTest` | **5/5 PASS**（新增 2 項）；`testArbitrary256DraftWithFineDetailsRefusesToLoadOn128Canvas` 驗證任意 256×256 精細原稿在 128×128 工作畫布拒絕載入並拋出例外、原稿檔與備份位元組 100% 保持未修改、拒絕載入時當前畫布筆畫、像素、Undo/Redo 歷史與 Dirty 狀態完好保留、16×16 煙火物品輸出快照不受影響；`testArbitrary256DraftLoadsLosslesslyOnStandard256Canvas` 驗證標準 256×256 畫布可 100% 無損載入；舊 16×16 / 32×32 物品往返維持 100% 雙向相容 |
| `DraftSafetyTest` | **9/9 PASS**（新增 5 項）；完整草稿校驗阻絕主檔覆蓋有效 .bak：JSON 標頭有效但像素長度錯誤、JSON 標頭有效但含非法 Hex 字元、檔名 UUID 與內容 UUID 不符時絕不覆蓋 .bak 且拋出 DraftStorageException；`testRecoveryNeverOverwritesNewerSchemaEvenWithForce` 驗證 `schemaVersion > 1` 的主檔即使 `force=true` 也絕對禁止被復原覆蓋，主檔與備份保持 100% 未更動；合法損壞主檔正常從備份復原；主檔有效時拒絕無條件復原 |
| `DraftGuiLogicTest` | **6/6 PASS**（新增 1 項）；`testMigrationUuidConflictSafelyPreservesBothFiles` 驗證舊目錄遷移遇相同 UUID 且內容不同時，新舊兩檔皆不覆蓋亦不刪除，安全保留舊檔為 `.legacy_conflict.json` 並記錄衝突提示；真實 DraftStorage 儲存失敗保留畫布與 Dirty 狀態；未命名草稿彈窗取消保留畫布；Dirty 狀態生命週期與歷史堆疊隔離 |
| `DraftStorageTest` | **10/10 PASS**；完整 256×256 ARGB 無損儲存與還原、多草稿管理、Save As 隔離、Rename 保留像素、備份復原、版本拒絕、毀損容錯、路徑穿越防護、超大檔案防護、名稱消毒 |
| `DraftCanvasIntegrationTest` | **4/4 PASS**；載入草稿重設歷史堆疊與 revision、resetBlank 清空、resample 至 32×32 煙火輸出、非法長度拒絕 |
| `DraftManagerLayoutTest` | **2/2 PASS**；$[140 \times 140]$ 至 4K 螢幕自適應排版無重疊、清單與按鈕安全邊界 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，175155 bytes |

**最終資料完整性修復核對（Final Data Integrity Remediation Verification）：**
1. **Major 1（阻止跨尺寸有損覆寫）**：
   - `FreehandCanvas` 新增 `canLoadLosslessly(int[] draft256)` 與嚴格載入防護：檢查 $2 \times 2$ 區塊內像素是否全同，若任意 256×256 原稿無法無損載入至 128×128 工作畫布，拋出 `IllegalArgumentException` 拒絕載入。
   - `CanvasEditorScreen` 與 `DraftManagerScreen` 在載入前全面調用 `canLoadLosslessly`，若有損則發送 `editor.drawfirework.draft_lossy_load_error` 錯誤提示、中止載入，絕不修改或覆寫原稿 UUID，且當前畫布、筆畫、Undo/Redo 歷史與 Dirty 狀態完好保留。
   - 舊 16×16 ItemStack 與 32×32 CanvasData 煙火輸出契約與 C2S 格式嚴格保持不變。
2. **Major 2（備份前完整驗證主檔）**：
   - `DraftStorage.saveDraft()` 於複製至 `.bak` 前全面調用 `readFullDraft()` 執行完整驗證（Schema、UUID 一致性、256×256 尺寸、65,536 像素長度與 ARGB Hex 編碼格式）。
   - 若主檔毀損（標頭有效但像素不足/超長、含非法 Hex、UUID 不相符），絕不複製至 `.bak`；若此時存在有效 `.bak`，拋出 `DraftStorageException` 安全拒絕覆寫，誓死守護最後一份有效備份。
3. **Major 3（絕對保護較新版本草稿不受復原覆蓋）**：
   - `DraftStorage.recoverFromBackup()` 於覆原前檢查主檔版本，若 `schemaVersion > CURRENT_SCHEMA_VERSION`，即使傳入 `force=true` 也絕對禁止以舊備份覆蓋較新版本主檔，主檔與備份位元組 100% 保持不變。
4. **Minor A（ClientDraftStorage 初始化與路徑錯誤隔離）**：
   - `CanvasEditorScreen` 與 `DraftManagerScreen` 對 `ClientDraftStorage.getInstance()` 進行全方位 `try ... catch` 防護，若檔案系統路徑異常或權限受限，發送錯誤提示並安全阻斷，不崩潰遊戲、不清除畫布與 Dirty 狀態。
5. **Minor B（舊目錄遷移 UUID 衝突安全保留）**：
   - `ClientDraftStorage.migrateLegacyDrafts()` 處理 `config/drawfirework/drafts/` 遷移時，若新舊目錄存在相同 UUID 但內容不同（`Files.mismatch != -1L`），絕不覆蓋亦不刪除任何一方，自動將舊檔另存為 `<uuid>.legacy_conflict.json`，並於 GUI 顯示衝突提示訊息。
6. **Minor C（實體產品路徑測試證據）**：
   - 測試套件加入針對實體檔案讀寫與遷移之直接測試案例，提供確鑿的真實路徑測試證據。
7. **非原子寫入與容錯替代策略**：
   - 當檔案系統不支援原子移動時，系統採用非原子替代移動；操作失敗時回報錯誤，並依既有備份／復原策略處理。本機制不保證斷電期間的資料持久性。

## 2026-10-25 — Phase 06 資料安全修復驗證交付（Data Safety Remediation）

Status: **PARTIAL**（5 Major 與 3 Minor 安全問題修復全數完成，等待使用者實機驗收與 Codex 獨立唯讀複核）

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 26s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **28 suites、176 tests、0 failures、0 errors、0 skipped**（全數 PASS，新增 11 項測試） |
| `DraftSafetyTest` | **4/4 PASS**；較新版本（schemaVersion > 1）安全拒絕覆寫、損壞主檔絕不覆蓋有效 .bak 備份、主檔有效時拒絕無條件復原以防覆蓋新稿、隨機唯一暫存檔安全清理 |
| `CanvasCompatibilityTest` | **3/3 PASS**；16×16 舊物品畫布（128×128）確定性 $2 \times 2$ 展開至 256×256 草稿與降採樣回 128×128 雙向無損往返、32×32 標準畫布無損往返、煙火物品輸出尺寸（16×16／32×32）與伺服器 EditSession.save 契約嚴格保留、非法長度安全拒絕 |
| `DraftManagerLayoutTest` | **2/2 PASS**；$[140 \times 140]$ 至 4K 螢幕自適應排版無重疊、清單與按鈕安全邊界、按鈕與文字絕不溢出視窗底緣或右側邊界 |
| `DraftGuiLogicTest` | **5/5 PASS**（新增 2 項）；儲存失敗狀態機保留畫布與 Dirty 狀態、未命名草稿彈窗取消完整保留畫布與 Dirty 狀態不關閉 GUI、Dirty 狀態生命週期、歷史隔離、CRUD 業務邏輯 |
| `DraftStorageTest` | **10/10 PASS**；256×256 無損往返、多草稿管理、Save As 隔離、Rename 保留像素、備份復原、版本拒絕、毀損容錯、路徑穿越防護、超大檔案防護、名稱消毒 |
| `DraftCanvasIntegrationTest` | **4/4 PASS**；載入草稿重設歷史堆疊與 revision、resetBlank 清空、resample 至 32×32 煙火輸出、非法長度拒絕 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，172572 bytes |

**安全修復核對（Remediation Verification）：**
1. **Major 1（儲存成功後才能關閉 GUI）**：已建立 `saveAndThen`，確認寫入成功才執行關閉；取消或失敗均保留畫布與 Dirty 狀態。
2. **Major 2（儲存失敗不得切換或新建草稿）**：DraftManagerScreen 全面委派 `saveAndThen`，只有成功儲存才執行切換，失敗與取消保留畫布。
3. **Major 3（128×128／256×256 工作畫布相容）**：`FreehandCanvas.toDraft256Pixels()` 與 `loadDraft` 實作確定性 $2 \times 2$ 擴展與取樣，保持 100% 雙向無損往返相容，且 16×16 煙火物品輸出規格與伺服器契約不變。
4. **Major 4（備份、覆寫與復原之資料完整性）**：拒絕覆寫較新 schemaVersion；主檔損壞時絕不覆蓋既有有效 .bak；主檔有效時拒絕無條件復原以防覆蓋新稿。
5. **Major 5（符號連結防護與隨機暫存檔）**：`resolvePath` 強制 symlink 檢查與 realPath 邊界檢查；改用唯一隨機暫存檔並在 `finally` 中安全刪除。
6. **Minor 1（窄視窗排版）**：`DraftManagerLayout` 經由多種視窗尺寸自動測試驗證，保證不重疊、不越界。
7. **Minor 2（儲存路徑規範化）**：統一使用 `<gameDirectory>/drawfirework/drafts/`，並加入 `config/drawfirework/drafts/` 自動遷移。
8. **Minor 3（精確錯誤追蹤）**：新增 `lastListError` 與 UI 錯誤反饋，澄清 `flush()` 與 `ATOMIC_MOVE` 降級行為。

## 2026-10-25 — Phase 06 高解析度草稿持久化驗證交付（等待驗收與審查）

Status: **PARTIAL**（實作與自動化測試已全數完成，等待使用者實機驗收與 Codex 獨立唯讀複核）

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 24s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **25 suites、165 tests、0 failures、0 errors、0 skipped**（全數 PASS，新增 17 項測試） |
| `DraftStorageTest` | **10/10 PASS**；完整 256×256 ARGB 無損儲存與還原、多色與透明像素、多草稿獨立管理、Save As 不覆蓋原稿、重新命名不遺失像素、覆蓋自動備份（.bak）與復原、毀損檔案容錯不刪檔、不支援較新版本（schemaVersion > 1）安全拒絕、超大檔案（> 2MB）防護、名稱消毒與路徑穿越安全阻斷 |
| `DraftCanvasIntegrationTest` | **4/4 PASS**；載入草稿重設歷史堆疊與 revision 更新、resetBlank 清空像素與歷史、載入草稿 resample 至 32×32 煙火輸出、非法長度安全拒絕 |
| `DraftGuiLogicTest` | **3/3 PASS**；Dirty 狀態生命週期（初始非 dirty、繪圖 dirty、存檔非 dirty、擦除 dirty、載入草稿非 dirty）、切換草稿完全隔離 Undo/Redo 歷史、草稿 CRUD 業務流程驗證 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，151111 bytes |

**交付物與限制說明：**
1. **已自動化驗證**：純 Java 草稿持久化、原子寫入、備份復原、路徑安全、容量限制、格式版本控制、歷史堆疊隔離、Dirty 狀態判定、GUI 整合編譯通過。
2. **待人工實機驗收（Deferred to Manual Acceptance）**：
   - 依據 `docs/PHASE_06_ACCEPTANCE.md` 於 Minecraft 遊戲中實際體驗：繪圖 -> 存檔 -> 重新開啟 -> 退出重啟載入 -> 建立第二份草稿 -> 未儲存變更提示彈窗 -> 刪除二次確認 -> 輸出煙火物品施放回歸。
3. **多人草稿同步**：依產品規格，草稿為本機用戶端資料，不涉及伺服器同步或新增 C2S 封包。

## 2026-10-24 — Phase 05D 畫質檔位與採樣最佳化驗證（正式結案）

Status: **PASS / CLOSED**（依使用者正式批准結案）

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3／Loader 0.19.5／API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1；程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，未變更依賴。

| 實際命令／證據 | 結果與指標 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL in 28s，7 tasks executed |
| 本輪 build/test-results/test/TEST-*.xml | **22 suites、148 tests、0 failures、0 errors、0 skipped**（全數 PASS） |
| `DetailPreservingSamplerTest` | **13/13 PASS**；圓形、數字 6、細斜線、封閉圖案、實心方塊、多色對比、各檔位預算上限、確定性、空畫布與單像素邊界；70/30 動態目標分配邊界回補、超過預算之複雜線條極限抽樣測試 |
| `SamplingStrategyTest` | **4/4 PASS**；預設 Strategy 檢查、字串解析、Downsampler 委派分流、HeartPrototype 生命週期與 reset |
| `ParticleQualityTest` | **6/6 PASS**；Standard (800/266)、Balanced (1100/366)、High (1400/466) 預算與上限計算、超限拒絕、字串解析與 reset 恢復 Balanced 預設驗證；各檔位明確傳入 266 尊重呼叫端數值測試（未指定展開為 366，明確指定維持 266） |
| `CanvasPreviewTest` | **5/5 PASS**；品質切換與採樣策略切換之快取自動失效驗證，預覽與 Save 共用 32×32 資料驗證 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0，無空白錯誤，僅正常 LF/CRLF 提示 |
| 發佈候選 JAR | `build/libs/drawfirework-1.0.0.jar`，133607 bytes |

**Codex 最終獨立唯讀複核結果（2026-10-24）：**
- HEAD 基準：`ae1e9fbb0c084139b27b6e6573d4b3e10ee9dfef`
- 缺陷計數：Critical 0 / Major 0 / Minor 0
- 前次 4 項 Minor（266 明確語意、70/30 動態契約、過度宣稱、GUI Session 限制）全數 RESOLVED
- 結論：正式批准 Phase 05D 結案。

**使用者實機驗收回報與正式決策：**
1. **流暢度**：文書筆電實機測試，Standard、Balanced、High 三檔均觀察到 60 FPS 且無可察覺掉幀。（*限制說明：屬特定筆電環境目視觀測，絕非無效能上限或通用效能保證*）。
2. **視覺評鑑**：以「數字 6」為例，使用者確認 Balanced 比 High 好看（線條密度適中，孔洞清晰無壅塞感）。
3. **採樣策略**：實體 Dust 粒子散布下，Existing 與 Detail-Preserving 肉眼差異並不明顯，Detail-Preserving 並非對所有圖案都一定優於 Existing。
4. **產品決策組合**：正式預設為 **Balanced + Detail-Preserving + Fullbright Exact**。
5. **已接受限制（Accepted Limits & Deferred Verification）**：
   - 專業 Profiler 效能測量（CPU/GPU 記憶體配置量）與採樣策略延伸視覺比較記為可延後項目。
   - CanvasData 仍為 32×32 格式；256×256 原稿永久保存未實作（屬 Phase 06 範疇）。

## 2026-10-24 — Phase 05C 正式結案驗證（歷史紀錄）

Status: PASS / CLOSED，按使用者正式批准的最終整合與驗收政策；本節取代以下歷史PARTIAL／baseline預設狀態，不回寫歷史測試為成功。

環境：D:\minecraftmod；Java25.0.3+9-LTS；Minecraft26.3／Loader0.19.5／API0.163.0+26.3／Loom1.17.16／Wrapper9.6.1；程序級GRADLE_USER_HOME=D:\minecraftmod\gradle，沒有更換依賴。

| 實際命令／證據 | 結案結果 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon`（沙箱外） | exit0，BUILD SUCCESSFUL in1m55s，7 tasks executed |
| 本輪build/test-results/test/TEST-*.xml | 19 suites、124 tests、0 failures、0 errors、0 skipped |
| DustLightingModeTest | 9/9；新增testResetRestoresProductionDefaultFromEveryMode，直接呼叫指令reset使用的產品方法；初始化／null fallback／default解析同步測試 |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit0 PASS |
| `git diff --check` | exit0，只有既有LF/CRLF提示 |
| Mixin／注入設定SHA256與setter呼叫範圍 | 與前次獨立審查相同；唯一產品setFullbright呼叫仍為HeartPrototype.applyLighting，作用於本模組新建的Dust核心與尾跡 |
| 發佈候選JAR | build/libs/drawfirework-1.0.0.jar，119490bytes |

此次JUnit不執行真實Brigadier聊天回饋或Minecraft世界；指令reset委派resetLightingMode的接線由原始碼核對。模式仍是客戶端欄位，不寫世界光照、全域粒子、設定檔或傳送lighting封包。Mixin原始碼及設定未改；先前實際runClient匯出位元碼證據有效，但本次不宣稱再次遊戲驗證最終預設。

**Accepted Risk / Deferred Verification（未測，不是PASS）：**

- 原版Dust的完整實機隔離測試尚未進行。
- 四模式及所有日夜圖案排列組合未全面測試。
- 重啟／重進世界的部分生命週期情境未全面實測。

上述風險由使用者本次明確接受，不再單獨阻擋結案。使用者已確認Fullbright／Exact可見度改善、Exact顏色更忠實飽滿且稍螢光可接受，白天已觀察且暫不要求調色；不擴張為所有情境PASS。無已知真正阻斷。前次獨立靜態審查不等於本次收尾自我驗證被宣稱為新的獨立複審。

## 2026-10-24 — 05C 文件 Remediation 與 Runtime 注入（歷史）

本輪程式及依賴未變。兩項文件 Minor 已修正；JUnit不代表Mixin或遊戲場景測試。使用者既有視覺回饋保留，未補填未測矩陣。

| 命令／證據 | 本輪結果與限制 |
| --- | --- |
| `.\gradlew.bat runClient --no-daemon`，程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle，JAVA_TOOL_OPTIONS=`-Dmixin.debug.export=true -Dmixin.debug.export.filter=net.minecraft.client.particle.Particle` | 沙箱外啟動；22:07:46載入Minecraft26.3／Loader0.19.5，資源圖集初始化；之後Ctrl+C主動停止，exit1，不是正常結束PASS |
| `javap -p -c run/.mixin.out/class/net/minecraft/client/particle/Particle.class` | 本次實際匯出的類別包含GlowingParticleAccess、boolean欄位、getLightCoords攔截呼叫、true回傳15728880及false保留原分支；確認runtime轉換注入，不是粒子場景行為測試 |
| `.\gradlew.bat clean build --no-daemon`（相同GRADLE_USER_HOME，無debug環境選項，沙箱外） | exit0，BUILD SUCCESSFUL in18s，7 tasks executed |
| 本輪JUnit XML | 19 suites，123 tests，0 failures/errors/skipped |
| 程式SHA256 | HeartPrototype／ParticleMixin／Mixin JSON與獨立審查基準相同；本輪所有編輯均為docs |

保存啟動摘錄及轉換後位元碼：[phase-05c-runtime-evidence.txt](reviews/phase-05c-runtime-evidence.txt)。缺少可選Fernflower僅影響反編譯Java，class匯出成功；另見Perflib／Realms開發帳號警告，未見本模組MixinApplyError或InvalidMixinException。未把「無錯誤」單獨當作注入證據。

未執行：16組場景矩陣、原版Dust隔離、重進／重啟模式與生命週期清理、多人。此環境無原生Minecraft GUI控制能力；未進入／修改世界。最短手測見 [PHASE_05C_ACCEPTANCE.md](PHASE_05C_ACCEPTANCE.md)。三組Phase02純Java回歸使用前輪PASS證據，本輪未重跑。正式預設不變，05C PARTIAL。

## 2026-10-24 — Codex 05C 獨立複驗（歷史）

環境：D:\minecraftmod；Microsoft Java 25.0.3+9-LTS；Minecraft 26.3、Loader 0.19.5、API 0.163.0+26.3、Loom 1.17.16、Wrapper 9.6.1。程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle；沒有更換版本。

| 實際命令／檢查 | 本輪結果 |
| --- | --- |
| `java -version` | exit 0，25.0.3+9-LTS |
| `.\gradlew.bat clean build --no-daemon`（沙箱） | exit 1；Could not connect to the Gradle daemon，pid 14580；尚未執行編譯任務 |
| 同命令（沙箱外） | exit 0，BUILD SUCCESSFUL in 29s，7 tasks executed |
| 讀取本輪 build/test-results/test/TEST-*.xml | 19 suites，123 tests，0 failures/errors/skipped |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0 PASS |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0 PASS |
| `git diff --check` | exit 0；僅 LF/CRLF 提示 |
| 本機 26.3 JAR `javap -p -c` | 核對 Particle、DustParticle、DustParticleBase、SingleQuadParticle、SimpleAnimatedParticle；見獨立報告 |

使用者本輪確認白天開闊天空及手繪多色圖案發射已測：夜間效果不錯，白天偏螢光略假，暫不調整。此為人工定性回饋，非完整矩陣 PASS。Codex 未親自啟動遊戲；Mixin 注入、未標記原版粒子回歸、清理與多人未於本輪實測。現有 run/logs/latest.log 為既有遊戲紀錄，不當作本輪新測試；其中 Realms 認證與 minimized window SurfaceException 未見證據指向本模組 Mixin。

覆蓋修正：mock duck 測試並未執行注入；兩次相同採樣呼叫未測 lighting 接線；模式 getter/setter 測試不代表真實粒子生命週期。123 項通過只代表各實際斷言通過。沒有新 frame time／FPS 結論。

獨立技術審查與 05D 方案：[phase-05c-review.md](reviews/phase-05c-review.md)。05C PARTIAL，未修改正式預設或實作 05D，未 commit/push。

## 2026-10-24 — Phase 05C 煙火可見度與光照改善驗證（前次實作紀錄）

Phase: 05C — Firework Visibility & Lighting
Status: **Phase 05C PARTIAL — Awaiting user visual A/B acceptance and independent technical review**

基準 Commit：`94458d5`（功能基準 `83062a9`）；工作樹起始乾淨；Gemini/Antigravity 唯一程式開發代理。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -3 --oneline` | 起始確認 HEAD 為 `94458d5`；工作樹乾淨、暫存區乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 26s**，7 actionable tasks: 7 executed |
| `.\gradlew.bat test --no-daemon` | exit 0，**123/123 PASS**（19 suites，0 failures, 0 errors, 0 skipped） |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR | `build/libs/drawfirework-1.0.0.jar` |
| Codex 獨立技術複審 | 待交付後審查 |
| 使用者人工驗收 | 待實機執行（指南見 `docs/PHASE_05C_ACCEPTANCE.md`） |

### 產品測試涵蓋總結（共 19 套件、123 項 JUnit 全部通過）：
- **DustLightingModeTest**（新增 8 項，純 Java 測試）：
  1. `testDefaultModeIsBaseline`：驗證 BASELINE 列舉的 fullbright 與 exactRgb 均為 false 及其指令名稱；此測試不建立遊戲或驗證啟動預設。
  2. `testAllModeProperties`：驗證 `FULLBRIGHT`、`FULLBRIGHT_EXACT`、`EXACT_ONLY` 旗標與指令名稱一致性。
  3. `testSafeParsing`：驗證指令參數別名（world, default, bright, fb, full, fbe, exact, eo）解析與無效／null 安全回傳。
  4. `testFullbrightConstantMatchesMinecraftFormat`：驗證常數 `15728880 == 0x00F000F0`，天光 15 與方塊光 15 正確解包。
  5. `testRgbComponentExtraction`：驗證 0xRRGGBB 轉浮點 RGB 之無損換算。
  6. `testMockGlowingParticleAccess`：僅驗證測試內 mock boolean 存取；未載入 Mixin 或 Minecraft 粒子。
  7. `testLightingModeDoesNotAlterCanvasDownsamplingOrBudget`：僅比較相同參數兩次建立 DustPattern 的結果；未切換 lighting 模式，不能證明光照接線或粒子預算在遊戲中不變。
  8. `testHeartPrototypeLightingModeLifecycle`：驗證 HeartPrototype 模式初值、getter/setter 與 null fallback；未驗證遊戲事件、粒子生命週期或跨世界行為。
- **既有 18 套件（共 115 項，全數 PASS）**：
  - `DustShapeFidelityTest`（12 項）
  - `CanvasPreviewTest`（4 項）
  - `FreehandCanvasTest`（16 項）
  - `SmartResamplerTest`（6 項）
  - `CanvasTest`, `ItemCanvasCodecTest`, `CanvasEditorInputTest`, `FreehandInputTest`, `PlaybackPlaneTest`, `CanvasEditorTest`, `EditSessionTest`, `FreehandPipelineTest`, `CameraOrientationStabilityTest`, `DevCanvasesTest`, `DustGeometryDiagnosticsTest`, `DustPatternTest`, `EditorPayloadTest`, `PlaybackTest` 全部 PASS。

### 測試執行與保留邊界：
- **Tests performed**：
  - Compiled: PASS (`.\gradlew.bat clean build --no-daemon`)
  - Unit-tested: PASS (123/123 JUnit)
  - Phase 02 三組回歸: PASS (TimelineCheck, DustStyleCheck, BloomCheck)
  - JAR 封裝: PASS (包含 mixin 與 classes)
- **Tests not performed**：
  - 遊戲內真人肉眼 A/B 評估（指南見 `docs/PHASE_05C_ACCEPTANCE.md`）。
  - Codex 獨立技術複審。
  - 多人專用伺服器測試（屬 Phase 09/10）。

## 2026-10-24 — Phase 05B.2 正式結案驗證（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Smart Resampling, Preview, Undo/Redo, Flood Fill)
Status: **Phase 05B.2 — PASS / CLOSED**

結案 Commit：`83062a995934ecd139bbb36e21eda7130a0a8534`；Codex 最終獨立技術複審 Critical 0、Major 0、Minor 0，所有先前問題 RESOLVED；使用者正式批准結案。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -3 --oneline` | 起始確認 HEAD 為 `83062a9`；工作樹乾淨、暫存區乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat test --no-daemon` | exit 0，**115/115 PASS**（18 suites，0 failures, 0 errors, 0 skipped） |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 27s**，7 actionable tasks: 7 executed |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR | `build/libs/drawfirework-1.0.0.jar` |
| Codex 獨立技術複審 | PASS（Critical: 0, Major: 0, Minor: 0） |
| 使用者人工驗收 | 初次 5/5 PASS、修復後 3/3 PASS，施放效果改善已確認 |

### 產品測試涵蓋總結（共 18 套件、115 項 JUnit 全部通過）：
- **DustShapeFidelityTest**（12 項）：幾何保真度、Bloom 130 偏移特性、邊緣粒子無裁切、文字逐級降級與安全省略、`maxTextWidth` 無保底數值驗證、50..800 寬度不侵入按鈕不變量驗證、舊版重疊回歸對照全數 PASS。
- **CanvasPreviewTest**（4 項）：3 粒微偏移分布、矩形長寬比縮放、快取失效、Save 一致性全數 PASS。
- **FreehandCanvasTest**（16 項）：Undo/Redo 16 步上限、在途筆畫撤銷、Redo 分支截斷防崩潰、填色、多筆畫全數 PASS。
- **SmartResamplerTest**（6 項）：8-連通分支增益、分散雜點拒絕、色相一致性、null 安全全數 PASS。
- **其餘 14 個測試套件（共 77 項）**：CanvasTest, ItemCanvasCodecTest, CanvasEditorInputTest, FreehandInputTest, PlaybackPlaneTest, CanvasEditorTest, EditSessionTest, FreehandPipelineTest, CameraOrientationStabilityTest, DevCanvasesTest, DustGeometryDiagnosticsTest, DustPatternTest, EditorPayloadTest, PlaybackTest 全部 PASS。

### 測試執行與保留邊界：
- **Tests performed**：
  - Compiled: PASS
  - Unit-tested: 115/115 PASS
  - Runtime-tested: 8 項 PASS（初次 5/5 與修復後 3/3 使用者實機驗收）
  - Visually verified: 8 項 PASS（Dust 預覽切換、Bloom 簇狀分布、世界施放五階段動畫改善）
  - Multiplayer-tested: 未執行（屬 Phase 09-10 範圍）
- **Tests not performed**（依規定明確記錄，不記為 PASS）：
  - 極窄視窗實機文字排版重測（自動化測試已保證幾何不變量，經 Codex 審查列為非阻斷）。
  - 夜間環境光照下粒子亮度實測（留待整體 GUI 完成後另行調查）。

## 2026-10-24 — Phase 05B.2 最終統計文字邊界收尾驗證（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Final Statistics Text Boundary Fix)
Status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**

基準 HEAD：`d6941bfd5c8d6ac0d107be8bc39d45f18dd14224`；Codex 複審 Critical 0、Major 0；使用者初次人工驗收 5/5 PASS、修復後人工驗收 3/3 PASS。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -3 --oneline` | 起始確認 HEAD 為 `d6941bf`；工作樹乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat test --no-daemon` | exit 0，**115/115 PASS**（18 suites，0 failures, 0 errors, 0 skipped） |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 27s**，7 actionable tasks: 7 executed |
| `java -cp build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java -cp build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java -cp build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR | `build/libs/drawfirework-1.0.0.jar` |

### 產品測試涵蓋（共 18 套件、115 項 JUnit 全部通過）：
- **DustShapeFidelityTest**（12 項，全數直接呼叫產品類別 `CanvasEditorLayout`）：
  - `testStatsLabelCandidateProgressionAndSafeOmission`（新增）：直接呼叫 `chooseStatsText` 驗證 Full（250px） $\to$ Short（200px） $\to$ Compact（120px） $\to$ Minimal（60px） $\to$ Number（30px） $\to$ Safe Omission（$<18$px、0px、負數寬度均回傳 `null`）。
  - `testMaxTextWidthNoArtificialFloor`（新增）：驗證移除 40px 保底後，各視窗寬度下的可用寬度數值：800 (652), 180 (76), 120 (16，舊版誤判為 40), 90 (0), 50 (0)。
  - `testResolvedStatsTextNeverEncroachesButtonsAcrossWindowSizes`（新增）：遍歷 50..800 各種寬度（50, 80, 100, 120, 140, 160, 180, 240, 320, 480, 640, 800），直接呼叫 `resolveStatsText` 驗證：可見時 `width <= maxW`、`x >= MARGIN_LEFT` 且 `x + width <= panelStart - 4`；不可見時 `width == 0`、`item == null`，絕不侵入按鈕。
  - `testStatsLabelSelectionUnderTightWorkWidth`（擴充）：驗證 Compact 仍過寬（90px）時退回為 `null`，非正數寬度退回為 `null`。
  - `testNumberSixGeometricFidelity`：以 16×16 與 32×32「6」字呼叫 `CanvasEditorLayout` 投影方法驗證上下極性、質心座標對齊。
  - `testCircleGeometricFidelity`：驗證圓形在世界平面與預覽中完全對稱於 (0, 0)，長寬比嚴格為 1.0。
  - `testDiagonalLineGeometricFidelity`：驗證 45 度連續斜線在世界座標與螢幕座標呈單調連續性，斜率為 1:1。
  - `testRectangleGeometricFidelity`：驗證 32×16 矩形畫布長寬比在 DustPattern 採樣中精確保留。
  - `testDustStyleBloom130SampleProperties`：驗證 3 粒微偏移半徑嚴格 $\le 0.14f$，大樣本平均偏差 $(0, 0)$ 無方向偏向。
  - `testNoParticleDroppedAtPerimeterWithProductMethod`：直接呼叫產品方法 `CanvasEditorLayout.isParticleVisible` 驗證外圍邊界粒子全數 100% 繪製，無邊界硬裁切丟棄。
  - `testTextLayoutBoundariesUnderAllGuiScalesAndNarrowWindows`：測試各視窗寬高與 1..6 行多行說明折行下，Line 1 與 Line 2 嚴格垂直分開無重疊，且文字永不侵入右側按鈕區域、永不溢出視窗底部。
  - `testOldLayoutBugRegressionDetectsOverlap`：回歸對照驗證，以數值證明舊版固定座標重疊公式必然失敗，證明測試之有效性。
- **CanvasPreviewTest**（4 項）：全數通過。
- **FreehandCanvasTest**（16 項）：全數通過。
- **SmartResamplerTest**（6 項）：全數通過。
- **其餘 14 個測試套件（共 77 項）**：全部通過。

Tests not performed:
- 極窄視窗文字排版之遊戲內真人實機重測、Codex 獨立複審。

## 2026-10-24 — Phase 05B.2 最終 Minor 修正與測試有效性收尾驗證（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Remediation & Final Minor Closure)
Status: **Phase 05B.2 PARTIAL — Final minor fixes implemented, awaiting verification**

基準 HEAD：`2b5c5c8af9f37ee1a7be7439e9ec05f291001d19`；Codex 最終複審 Critical 0、Major 0；使用者初次人工驗收 5/5 PASS、修復後人工驗收 3/3 PASS。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -5 --oneline` | 起始確認 HEAD 為 `2b5c5c8`；工作樹乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat test --no-daemon` | exit 0，**112/112 PASS**（18 suites，0 failures, 0 errors, 0 skipped） |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 28s**，7 actionable tasks: 7 executed |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR | `build/libs/drawfirework-1.0.0.jar` |

### 產品測試涵蓋（共 18 套件、112 項 JUnit 全部通過）：
- **DustShapeFidelityTest**（9 項，全數直接呼叫產品類別 `CanvasEditorLayout`）：
  - `testNumberSixGeometricFidelity`：以 16×16 與 32×32「6」字呼叫 `CanvasEditorLayout` 投影方法驗證上下極性（row 越小越朝上、row 越大越朝下）、質心座標對齊（誤差 $< 0.5$ 像素間距）。
  - `testCircleGeometricFidelity`：驗證圓形在世界平面與預覽中完全對稱於 (0, 0)，長寬比嚴格為 1.0。
  - `testDiagonalLineGeometricFidelity`：驗證 45 度連續斜線在世界座標與螢幕座標呈單調連續性，斜率為 1:1。
  - `testRectangleGeometricFidelity`：驗證 32×16 矩形畫布長寬比（23:11）在 DustPattern 採樣中精確保留。
  - `testDustStyleBloom130SampleProperties`：驗證 3 粒微偏移半徑嚴格 $\le 0.14f$，大樣本平均偏差 $(0, 0)$ 無方向偏向；定量計算 16×16 下微偏移佔網格間距 45.2%、32×32 下佔 90.5%。
  - `testNoParticleDroppedAtPerimeterWithProductMethod`：直接呼叫產品方法 `CanvasEditorLayout.isParticleVisible` 驗證外圍邊界粒子（包括 row 15 最下緣粒子）全數 100% 繪製，無邊界硬裁切丟棄。
  - `testTextLayoutBoundariesUnderAllGuiScalesAndNarrowWindows`：測試各視窗寬高（180..800 寬、160..600 高）與 1..6 行多行說明折行下，Line 1 與 Line 2 嚴格垂直分開無重疊，且文字永不侵入右側按鈕區域、永不溢出視窗底部。
  - `testStatsLabelSelectionUnderTightWorkWidth`：直接呼叫 `CanvasEditorLayout.chooseStatsText` 驗證可用寬度縮小時，自動由 full 標籤退回為 short 標籤，進一步退回為 compact 標籤。
  - `testOldLayoutBugRegressionDetectsOverlap`：回歸對照驗證，以數值證明舊版固定座標重疊公式（240 高度下重疊 4 像素）必然失敗，證明測試之有效性。
- **CanvasPreviewTest**（4 項）：
  - `previewAndSaveShareIdenticalCanvasData`：驗證預覽產物與儲存 `snapshot()` 資料 100% 同一。
  - `threeParticleSampleDistributionMatchesBloom130`：驗證每個採樣點產生 3 顆符合 `DustStyle.BLOOM_130` 微偏移的粒子，總粒子數符合預期且有界。
  - `rectangularCanvasPreservesAspectRatioInPreview`：驗證 32×16 等矩形畫布長寬比在預覽中正確保留（X 跨度為 Y 跨度的兩倍），無壓縮變形。
  - `cacheInvalidationOnModeAndRevisionChange`：驗證無修改時使用快取，Mode 或 Revision 變更時正確失效。
- **FreehandCanvasTest**（16 項）：
  - `historyBoundedByMaxLimit`：驗證上限為 16 步（`MAX_HISTORY = 16`），先進先出淘汰最舊記錄。
  - `undoWhileStrokeActiveUndoesInProgressStroke`：驗證在途活躍筆畫（滑鼠按住中）按 Undo 立即撤銷回到筆畫前狀態。
  - `redoWhileNewStrokeActiveSafelyClearsRedoWithoutException`：驗證在途活躍筆畫期間按 Redo 安全提交並清空 Redo 分支，回傳 false，絕不拋出 `NoSuchElementException`。
  - 既有單步、多步、填色、邊界、透明擦除測試全數通過。
- **SmartResamplerTest**（6 項）：
  - `disconnectedScatterSpanningFourRowsNotBoosted`：驗證 8 方向連通性檢查能精確識別跨 4 行列的分散孤立雜點，拒絕不當增益。
  - `continuousDiagonalStrokesGainCoverage`：驗證 8-連通斜線與細輪廓正確獲得 Alpha 增益。
  - 既有孤立點、曲線色相一致性、null 安全等測試全數通過。
- **其餘 14 個測試套件（共 77 項）**：全部通過。

Tests not performed:
- 本輪新文字排版之遊戲內真人實機重測、Codex 獨立複審。

## 2026-10-24 — Phase 05B.2 獨立審查修復與預覽排版／幾何保真度修復驗證（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Remediation & Final Refinement)
Status: **Phase 05B.2 PARTIAL — Remediation complete, awaiting manual retest and independent re-review**

基準 HEAD：`c8b1ac7dd8647f46502c628b1a5e85535cc25dd6`；使用者完成人工驗收 5/5 PASS；Codex 獨立審查回報 2 Major、2 Minor。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -5 --oneline` | 起始確認 HEAD 為 `c8b1ac7`；工作樹乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat test --no-daemon` | exit 0，**109/109 PASS**（0 failures, 0 errors, 0 skipped） |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 30s**，7 actionable tasks: 7 executed |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR | `build/libs/drawfirework-1.0.0.jar` |

### 產品測試涵蓋（共 109 項 JUnit）：
- **DustShapeFidelityTest**（新增 7 項）：
  - `testNumberSixGeometricFidelity`：以 16×16 與 32×32「6」字驗證從 FreehandCanvas 到 DustPattern 的上下極性（row 越小越朝上、row 越大越朝下）、質心座標對齊（誤差 $< 0.5$ 像素間距）。
  - `testCircleGeometricFidelity`：驗證圓形在世界平面與預覽中完全對稱於 (0, 0)，長寬比嚴格為 1.0。
  - `testDiagonalLineGeometricFidelity`：驗證 45 度連續斜線在世界座標與螢幕座標呈單調連續性，斜率為 1:1。
  - `testRectangleGeometricFidelity`：驗證 32×16 矩形畫布長寬比（23:11）在 DustPattern 採樣中精確保留。
  - `testDustStyleBloom130SampleProperties`：驗證 3 粒微偏移半徑嚴格 $\le 0.14f$，大樣本平均偏差 $(0, 0)$ 無方向偏向；定量計算 16×16 下微偏移佔網格間距 45.2%、32×32 下佔 90.5%。
  - `testNoParticleDroppedAtPerimeter`：驗證外圍邊界粒子（包括 row 15 最下緣粒子）全數 100% 繪製，無邊界硬裁切丟棄。
  - `testTextLayoutNoOverlapUnderAllGuiScales`：測試各視窗寬高（200..600）與 GUI Scale 下，Line 1 與 Line 2 嚴格垂直分開無重疊，且折行文字永不侵入右側按鈕區域。
- **CanvasPreviewTest**（4 項）：
  - `previewAndSaveShareIdenticalCanvasData`：驗證預覽產物與儲存 `snapshot()` 資料 100% 同一。
  - `threeParticleSampleDistributionMatchesBloom130`：驗證每個採樣點產生 3 顆符合 `DustStyle.BLOOM_130` 微偏移的粒子，總粒子數符合預期且有界。
  - `rectangularCanvasPreservesAspectRatioInPreview`：驗證 32×16 等矩形畫布長寬比在預覽中正確保留（X 跨度為 Y 跨度的兩倍），無壓縮變形。
  - `cacheInvalidationOnModeAndRevisionChange`：驗證無修改時使用快取，Mode 或 Revision 變更時正確失效。
- **FreehandCanvasTest**（擴充 8 項）：
  - `historyBoundedByMaxLimit`：驗證上限為 16 步（`MAX_HISTORY = 16`），先進先出淘汰最舊記錄。
  - `undoWhileStrokeActiveUndoesInProgressStroke`：驗證在途活躍筆畫（滑鼠按住中）按 Undo 立即撤銷回到筆畫前狀態。
  - `redoWhileNewStrokeActiveSafelyClearsRedoWithoutException`：驗證在途活躍筆畫期間按 Redo 安全提交並清空 Redo 分支，回傳 false，絕不拋出 `NoSuchElementException`。
  - 既有單步、多步、填色、邊界、透明擦除測試全數通過。
- **SmartResamplerTest**（擴充 6 項）：
  - `disconnectedScatterSpanningFourRowsNotBoosted`：驗證 8 方向連通性檢查能精確識別跨 4 行列的分散孤立雜點，拒絕不當增益。
  - `continuousDiagonalStrokesGainCoverage`：驗證 8-連通斜線與細輪廓正確獲得 Alpha 增益。
  - 既有孤立點、曲線色相一致性、null 安全等測試全數通過。
- **原有 84 項測試**：全部通過。

Tests not performed:
- 遊戲內真人實際重測操作、Codex 獨立複審。

## 2026-10-24 — Phase 05B.2 首次交付驗證（歷史紀錄）

Phase: 05B.2 — GUI Refinement (Smart Resampling, Preview, Undo/Redo, Flood Fill)
Status: **Phase 05B.2 PARTIAL — Waiting for manual acceptance and independent review**

基準 HEAD：`862497d`，起始工作樹乾淨。由使用者授權 Gemini／Antigravity 擔任唯一程式修改者，Codex 暫停。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status`、`git log -5 --oneline` | 起始確認 HEAD 為 `862497d`；工作樹乾淨 |
| `.\gradlew.bat --no-daemon -version` | exit 0，Gradle 9.6.1，Java 25.0.3+9-LTS |
| `.\gradlew.bat test --no-daemon` | exit 0，**95/95 PASS**（0 failures, 0 errors, 0 skipped） |
| `.\gradlew.bat clean build --no-daemon` | exit 0，**BUILD SUCCESSFUL in 27s**，7 actionable tasks: 7 executed |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit 0，PASS: phase durations, formation/hold boundaries, progressive size fade |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit 0，PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit 0，PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release |
| `git diff --check` | exit 0，PASS（無行尾空白錯誤） |
| 產出 JAR 雜湊與大小 | `build/libs/drawfirework-1.0.0.jar`：104,876 bytes，SHA256: `B4693BE474B5B4F824F34AA09346AC038504EE9FB1CCE579CC1A15F64A3675C7` |

### 產品測試涵蓋：
- **SmartResamplerTest**（4 項）：
  - `thinContoursGainCoverageButNotOpaquePixels`：驗證細線在 Crisp 下獲得 Alpha 增益（`b > a`），且增益有界（`b <= 2*a`），不粗暴二值化（`b < 255`），且 RGB 完全相同。
  - `isolatedNoiseAndTransparentPixelsNotPromoted`：驗證單點雜訊不被增益，全空畫布轉換結果完全相同。
  - `modesShareHueOnCurvesDiagonalsAndColorBoundaries`：驗證多角度弧線與對角線上 Soft 與 Crisp 色相 100% 一致。
  - `nullSafetyAndEnumCompatibility`：驗證 null 參數檢查、Mode.CLEAR 與 Mode.CRISP 相容性、預設為 Soft。
- **FreehandCanvasTest**（擴充 6 項）：
  - `undoRedoSingleStroke`：單次筆畫 Undo/Redo 驗證。
  - `undoRedoMultipleStrokesAndRedoBranchTruncation`：多次筆畫撤銷、復原與新筆畫自動截斷 Redo 分支。
  - `historyBoundedByMaxLimit`：驗證歷史深度上限 32 步，超過時 FIFO 淘汰舊資料。
  - `floodFillEmptyCanvasAndUndo`：驗證全畫布填色與撤銷。
  - `floodFillBoundedRegion`：驗證封閉圖案內部填色、外部透明保持、橡皮擦清除填色與撤銷。
  - `floodFillIdenticalColorOrOutOfBoundsNoOp`：驗證相同顏色、出界座標與非有限數值之安全防護。
- **FreehandInputTest**（擴充 1 項）：
  - `fillToolWorksThroughInputAndIgnoresDrag`：驗證填色工具透過 Screen 事件觸發，拖曳不重覆觸發填色。
- **原有 84 項測試**：全部持續通過，涵蓋 Codec、Payload、UseGate、DustPattern、CanvasDownsampler、CameraOrientation 等。

Tests not performed:
- 遊戲內真人實際操作、不同視窗解析度／GUI Scale 人工視覺驗證、獨立審查（將另行安排）。

## 2026-10-24 — Phase 05B.1 正式結案（歷史紀錄）

Phase: 05B.1 — High-Resolution Freehand Drawing
Status: **PASS / CLOSED**
Build result: 使用者在一般 PowerShell 的 `D:\minecraftmod` 實際執行 `.\gradlew.bat clean build --no-daemon`，並取得 **BUILD SUCCESSFUL**。這是使用者提供的執行結果；未提供退出碼、耗時、任務數或終端全文，不補寫。先前 Gradle daemon 連線失敗保留為歷史紀錄，不再代表目前建置閘門狀態。
Tests performed: 使用者確認 JUnit 84/84 PASS、Phase 02 三組回歸 PASS、核心手繪／保存／煙火播放人工驗收 PASS；Antigravity 獨立唯讀審查 0 Critical／0 Major／0 Minor。此次文件結案未重跑以上項目。
Tests not performed: 本輪未重新執行 Gradle、JUnit、Phase 02 回歸、遊戲操作或審查；特殊伺服器情境、GPU/frame time、多人仍未驗證。
Known problems: 細線縮圖後變淡／模糊；舊 Smiley 與新手繪煙火夜間 Dust 可見度不足；高解析度原稿只在編輯 Session 暫存，永久保存留待 Phase 06。未測特殊情境不記為人工 PASS。
Git: 起始 HEAD `c2c01e0`，工作樹乾淨（本輪開始檢查）；功能 Commit `49fd5ad`。本輪只改四份指定文件，未暫存、未 commit、未 push。
## 2026-10-24 — Phase 05B.1 結案證據補充（歷史狀態；已由正式結案取代）

功能 Commit／目前 HEAD：`49fd5ad2ae534f7bb9524474a92318a120b69e3d`。開始本次文件更新前，工作樹與index均乾淨。使用者回報審查時工作樹clean。

使用者確認：無格線自由手繪、筆刷粗細、透明擦除人工PASS；自由繪圖儲存及煙火播放人工PASS；84/84 JUnit PASS；Phase 02三組回歸PASS；Antigravity獨立唯讀審查0 Critical、0 Major、0 Minor。這些是使用者提供的驗收結果，本次沒有重跑或重新實測。

**正式 clean build 尚未確認。** 本次指定文件與可找到的建置紀錄沒有這次的實際命令輸出或 `BUILD SUCCESSFUL`。既有TEST_RESULTS紀載先前Wrapper因Gradle daemon連線錯誤而未進建置任務；不能把該失敗改記成功，也不能以直接javac代替Wrapper clean build。依使用者指示，clean build僅在實際成功證據存在時標PASS，所以本階段維持：

`Phase 05B.1 PARTIAL — Waiting for successful clean build evidence`

這不是人工驗收或獨立審查的阻塞：二者均依使用者回報完成PASS。只剩正式clean build證據未確認。本次只更新階段文件，未修改程式、測試、資源或工具鏈；未執行Gradle、Git add、commit或push；不開始05B.2／05B.3。

結案保留事項：細線縮圖後變淡、輪廓模糊，留待智慧轉換品質優化；舊Smiley與新手繪煙火夜間均偏暗，待GUI完成後調查Dust可見度；高解析度原稿只於編輯Session暫存，永久保存留待Phase 06；外部切換遊戲模式／手持物、編輯超時及其他未實測特殊場景，不宣稱人工PASS。
## 2026-10-24 — Phase 05B.1 驗證（最新）

Phase: 05B.1 — High-Resolution Freehand Drawing
Status: **Phase 05B.1 PARTIAL — Waiting for manual acceptance and independent review**
Build gate: **BLOCKED**（Gradle daemon連線；尚須正式clean build成功，不能只等人工驗收）。

基準HEAD `0a00372187cf172a0ab39c175acc869b7ce97e13`；開始工作樹乾淨。Microsoft Java25.0.3+9-LTS、既有全部工具鏈／JVM設定不變；GRADLE_USER_HOME=`D:\minecraftmod\gradle`。

| 實際命令／檢查 | 真實結果 |
| --- | --- |
| `git status --short`、`git log -1 --format=fuller` | 起始無差異；HEAD為使用者的05A結案commit |
| `gradlew.bat test --tests com.drawfirework.editor.FreehandCanvasTest --no-daemon` | exit1，Could not connect to the Gradle daemon，pid8740；未進compile/test |
| 直接javac編譯CanvasData、FreehandCanvas、FreehandResampler及FreehandCanvasTest，再執行JUnitCore | exit0，8/8 PASS；在GUI整合之前完成 |
| `gradlew.bat clean build --no-daemon` | exit1，相同daemon連線失敗，pid3528；未進clean/compile/test。原始log在build/phase05b1-clean-build.log。沒有修改工具鏈或升權重試 |
| `javac @build/phase05b1-javac.args` | 最終exit0，全部main及test Java，使用本機merged26.3及既有依賴JAR；不是Gradle/Loom完整建置 |
| `java @build/phase05b1-junit.args` | 最終exit0，**84/84 PASS**，1.907秒；70原有＋8畫布／縮圖＋3原生事件＋3封包／session整合。log在build/phase05b1-junit.log |
| `java --class-path build/phase05b1-check tests/Phase02TimelineCheck.java` | exit0 PASS |
| `java --class-path build/phase05b1-check tests/Phase025DustStyleCheck.java` | exit0 PASS |
| `java --class-path build/phase05b1-check tests/Phase026BloomCheck.java` | exit0 PASS |
| `java --class-path build/phase05b1-check tests/Phase05B1QualityCheck.java` | exit0，產品方法產生對照PNG及CPU量測，數據如下 |
| `git diff --check` | PASS；只有既有autocrlf提示，非whitespace錯誤 |
| `git diff --cached --name-only` | 空；沒有add／commit／push |

工具失敗記錄：apply_patch兩次無法寫入專案檔，改用正常授權的PowerShell寫入成功。第一次全量javac引數檔因PowerShell字串運算括號錯誤回exit2（invalid flag），未編譯；修正引數產生方式後編譯與JUnit通過。沒有把工具／引數錯誤記成產品測試失敗，也沒有隱藏失敗。

### 產品測試涵蓋

- 全部1024種1..32寬高組合：工作副本比例、未改動時回傳相同不可變CanvasData、改動後尺寸保持。
- 快速兩點拖曳不斷線，2／24直徑差異、1..64範圍、邊界、非有限／極大座標拒絕。
- 同一筆畫alpha不累積；分筆source-over精確結果；透明橡皮擦與邊緣alpha、來源隔離。
- 1px細線保留低覆蓋率與紅色，斜線／圓弧輸出連續，透明隱藏藍色不污染紅線，alpha面積守恆容差，多色交界混色、確定性重算。
- 真正MouseButtonEvent／具名LEFT走Screen使用的FreehandInput：縮放浮點座標、按下／快拖／放開、工具區、右中鍵、出界重入、reset及擦除。
- 256工作圖轉換後經真正EditorCanvasPayload.CODEC roundtrip仍4116bytes／1024pixels；矩形經EditSession一次性寫入，重播拒絕；新空白僅接受32×32、舊16×16不會偷偷放大。

### 離線影像品質比較（非遊戲截圖）

圖檔：`docs/phase05b1-quality-comparison.png`。由產品FreehandCanvas／snapshot產生；左256工作圖、中32面積平均、右32 nearest對照，32結果放大至256便於比較。

| 圖案 | 原始alpha總和/64 | 面積平均alpha總和／可見格數 | nearest總和／可見格數 |
| --- | ---: | ---: | ---: |
| 1px細線 | 924.38 | 928／30 | 7395／29 |
| 3px斜線 | 3877.25 | 3896／88 | 7650／30 |
| 8px圓周 | 16002.38 | 15996／136 | 15206／71 |
| 32px色彩／Alpha交界 | 21925.31 | 21926／116 | 22045／112 |

Codex已實際開啟PNG檢查：細線area結果連續但明顯較淡；nearest將抽中的細線過度增粗／增亮。area圓周較均勻但32格階梯仍明顯，色彩交界有合理混色，未見黑邊。此結果只證明離線ARGB輸出，不代表Dust亮度、粒子降採樣與真人手繪品質已通過。

### CPU與配置量

同一Java25程序，30次暖機／120筆樣本，最大64px筆刷從(1,1)拖到(254,254)，每次結束筆畫後重測。此為重複同色路徑，後續多數像素不變，不代表所有不同色／半透明使用情境。

- 筆畫p50 **2.120ms**、p95 **2.863ms**。
- 轉換p50 **0.117ms**、p95 **0.166ms**。
- ThreadMXBean實測：筆畫 **0 bytes/op**；轉換 **8248 bytes/op**（輸出陣列＋CanvasData防禦性複本／物件）。量測不包含模型建構、紋理NativeImage／GPU、Screen UI與Minecraft執行緒負載。
- 工作緩衝區上限589,824bytes，native像素另262,144bytes，GPU紋理另計；每個變動顯示幀最多一次全圖上傳，未變動幀零畫布上傳。這是程式路徑／大小推算，**未量測GPU／frame time／FPS／整合顯示晶片操作延遲**。

Compiled: PASS（直接javac）；Unit-tested: PASS（JUnitCore84）；Gradle clean build: BLOCKED；Runtime-tested: 未執行；Visually verified: 僅離線對照PNG已檢查，Minecraft未驗證；Multiplayer-tested: 未執行／不在範圍。

沒有新Gradle發佈JAR；`build/libs`若有檔案屬舊建置，不能當作05B.1產物。需在一般PowerShell成功執行既有Wrapper clean build後才使用新JAR／runClient。完整手動流程與精確暫存候選清單見PHASE_05B1_ACCEPTANCE.md。

### 直接編譯／JUnit重現（只在Gradle阻塞時作輔助，不替代clean build）

```powershell
$cp = @((Get-ChildItem .gradle/loom-cache/minecraftMaven -Recurse -Filter '*.jar').FullName) + @((Get-ChildItem gradle/caches/modules-2/files-2.1 -Recurse -Filter '*.jar').FullName)
$cpString = $cp -join ';'
New-Item -ItemType Directory -Force build/phase05b1-check | Out-Null
$sources = Get-ChildItem src/main/java,src/test/java -Recurse -Filter '*.java' | ForEach-Object { '"'+$_.FullName.Replace('\','/')+'"' }
$compileArgs = @('-encoding','UTF-8','-cp',('"'+$cpString.Replace('\','/')+'"'),'-d','build/phase05b1-check') + $sources
[IO.File]::WriteAllLines((Join-Path $pwd 'build/phase05b1-javac.args'),$compileArgs)
javac '@build/phase05b1-javac.args'
if ($LASTEXITCODE -ne 0) { throw 'compile failed' }
$tests = Get-ChildItem src/test/java -Recurse -Filter '*Test.java' | ForEach-Object { $_.FullName.Substring((Join-Path $pwd 'src/test/java').Length+1).Replace('\','.').Replace('.java','') }
$runArgs = @('-cp',('"build/phase05b1-check;'+$cpString.Replace('\','/')+'"'),'org.junit.runner.JUnitCore') + $tests
[IO.File]::WriteAllLines((Join-Path $pwd 'build/phase05b1-junit.args'),$runArgs)
java '@build/phase05b1-junit.args'
```
## 2026-10-24 — Phase 05A 正式結案驗證紀錄（最新）

Phase: 05A — In-Game Canvas Editor MVP
Status: **PASS / CLOSED**（按使用者已確認之結果結案；本次僅更新文件，未重新執行建置或測試）
功能提交／結案基準HEAD：`51146068b1e7e12bb58667f2fcb5c3fb288380d1`。獨立唯讀審查為Critical 0、Major 0、Minor 0，且GUI滑鼠事件修復、C2S資料驗證及伺服器授權、token／防重放／ItemStack狀態保護、不同尺寸畫布程式碼與單元測試均PASS。

| 驗證項目 | 結果與證據來源 |
| --- | --- |
| clean build | PASS（使用者確認；本次未重跑） |
| 全部JUnit | 70/70 PASS（使用者確認；本次未重跑） |
| Phase 02三組回歸 | PASS（使用者確認；本次未重跑） |
| 遊戲內主要流程 | 使用者確認可繪圖、儲存、右鍵播放自製煙火及clear；取消、重進世界、快捷鍵與舊功能操作回報基本正常 |
| 獨立唯讀審查 | Critical 0、Major 0、Minor 0；指定的互動與伺服器安全項目PASS |
| 特定伺服器狀態／尺寸情境 | 未經實機驗證，接受剩餘風險；詳見下方 |

**未經實機驗證但由程式碼、單元測試及審查支持的接受風險：**編輯中途由外部強制切Survival；編輯中途由外部強制換物；編輯器開啟超過12,000 ticks後儲存；特殊矩形畫布在遊戲內的實際呈現及儲存。以上均不得記為Runtime-tested或實機PASS。

**JVM待追蹤：**偶發JVM啟動閃退，根因未確認。沒有發現直接關聯Phase 05A的證據，但亦未證明與模組無關；未宣稱修復，列為非阻塞事項。

## 2026-10-24 — Phase 05A GUI Drawing Interaction Bugfix（歷史狀態；已由正式結案取代）

Phase: 05A — GUI Drawing Interaction Bugfix
Status: Phase 05A PARTIAL — Waiting for manual retest
Changes: 原Screen點擊／拖曳／放開三處button==0造成左鍵事件不進model.paint。新增直接產品處理器CanvasEditorInput，使用InputConstants.MOUSE_BUTTON_LEFT（本機值1）；Screen委派click／drag／release，原model／render／save路徑不變。變更及8項調查證據見PHASE_05A_ACCEPTANCE.md最新補充。
Build result: GRADLE_USER_HOME=D:\minecraftmod\gradle，既有Wrapper `.\gradlew.bat clean build --no-daemon`，沙箱外工具核准執行，exit0，22秒、7tasks executed。版本／依賴／標準clean設定未變。
Tests performed: JUnit70/70 PASS（12 suites，0 failures/errors/skipped），Phase02三組回歸各exit0，最終git diff --check PASS。先保留舊左鍵判斷跑新測試4/4 FAIL，確認回歸測試能攔住實際根因，再修復至PASS。
Tests not performed: 純JUnit沒有建立Minecraft Screen／原生按鈕、視窗或完整事件派送。測試以真正MouseButtonEvent／MouseButtonInfo及本機InputConstants常數呼叫Screen實際使用的產品處理器，涵蓋輸入條件、座標、工作像素與筆畫狀態，但不是遊戲內實測。未重新啟動遊戲、未重測GUI Scale／Esc／保存／C2S／ItemStack／重載／視覺／多人。
Known problems: 使用者原始回報僅GUI開啟及繪圖阻塞；其他人工項目仍UNTESTED。修復後繪圖待重測，不標PASS；舊模型測試未含button判斷的覆蓋缺口已補。
Git commit: 未commit／push，未暫存，HEAD527db03f671ba1e7b878009202733c302fa11158不變；保留原21檔05A變更，加2新檔，累計23檔。原有05A工作樹保留；本輪修改範圍依實際寫入操作及最終檔案清單核對。開始時hash快照寫入暫存目錄，但收尾時該檔案已不可取得，未宣稱SHA256基準比對成功。
Recommended next phase: 僅本階段人工重測；交付後停止寫入，不開始05B。

| 實際命令 | 結果 |
| --- | --- |
| `git status --short`／`git rev-parse HEAD` | 原05A未提交工作保留；527db03f671ba1e7b878009202733c302fa11158 |
| `javap -constants` InputConstants；`javap -c` MouseButtonEvent／MouseButtonInfo／ContainerEventHandler／AbstractWidget；`javap -c -p` MouseHandler | LEFT=1／MIDDLE=2／RIGHT=3；原生widget左鍵判斷1；event.button直接回傳buttonInfo.button；MouseHandler使用GUI縮放座標並直接呼叫Screen.mouseClicked／mouseDragged／mouseReleased，無轉為0 |
| `.\gradlew.bat test --tests com.drawfirework.client.CanvasEditorInputTest --no-daemon`（保留舊predicate） | exit1、23秒；4tests全部AssertionError，非registry bootstrap／編譯失敗 |
| `.\gradlew.bat clean build --no-daemon`（predicate改具名常數後） | exit0、22秒；70tests全部PASS，7tasks executed |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit0 PASS |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit0 PASS |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit0 PASS |
| `git diff --check`（最終） | exit0 PASS；另檢查新增檔案whitespace |
| `git diff --cached --name-only` | 無輸出，未暫存 |

新CanvasEditorInputTest的4項直接產品測試：nativeLeftClickUsesSelectedColorAndWorkingCopy（紅藍ARGB及來源不變）；nativeDragInterpolatesAndReleaseStopsPainting（真左鍵、快速拖曳、外部放開後不再畫）；eraserAndResetKeepCancelIsolated（透明擦除、reset、來源隔離）；toolsOutsideBoundsAndNonLeftButtonsNeverPaint（0／中／右鍵拒絕，工具區／四邊界不畫、不啟動drag，越界再入不跨區連線）。原66測試全部通過，既有模型／Codec／C2S安全／播放回歸保留。

Compiled: PASS；Unit-tested: PASS；Runtime-tested: 修復後未執行；Visually verified: 修復後未執行；Multiplayer-tested: 未執行。

最新JAR：build/libs/drawfirework-1.0.0.jar，87743bytes，SHA256 `9DDC04327242002F56695E27F5AA3F59067A72F1F5DB6DFFD2E6E8382F8AC8A7`；舊JAR hash與66tests以下保留為歷史。Gradle既有棄用警告未改。

## 2026-10-23 — Phase 05A 驗證（歷史紀錄）

Phase: 05A — In-Game Canvas Editor MVP
Status: Phase 05A PARTIAL — Waiting for manual acceptance and independent review
Changes: 完整程式路徑、安全政策、21檔精確暫存候選與人工操作見PHASE_05A_ACCEPTANCE.md；04D SKIPPED / NOT REQUIRED。起始HEAD 527db03f671ba1e7b878009202733c302fa11158，git status --short無輸出，Codex唯一修改、Antigravity唯讀；不改既有播放引擎或工具鏈。
Build result: 最終 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'` 後 `.\gradlew.bat clean build --no-daemon`，工具核准沙箱外執行，exit0，30秒，7 tasks executed。標準clean任務已檢查，沒有自訂刪除或手動刪除資料。Java Microsoft25.0.3+9-LTS／Minecraft26.3／FabricAPI0.163.0+26.3／Loader0.19.5／Loom1.17.16／Wrapper9.6.1全數不變。
Tests performed: JUnit **66/66** PASS、0 failures/errors/skipped（11個testsuite XML，build/test-results/test）；Phase02三組獨立回歸各exit0；git diff --check最終exit0。新13個測試實際呼叫產品方法，不複製GUI公式或建立假的第二套授權邏輯。
Tests not performed: Screen實例／按鍵註冊及GUI縮放輸入的runtime、真實registry／ServerPlayer／ItemStack／receiver整合、存檔重載、視覺、聲音、效能、多人与獨立審查。JUnit對傳入閘門的eligible／sameStack boolean做拒絕測試，不能冒充已在遊戲中驗證Minecraft adapter與玩家狀態。原GUI取消測試證明資料模型隔離，實際Esc／取消按鈕仍待測。
Known problems: 待人工項目詳見PHASE_05A_ACCEPTANCE.md；原verify命令要求原始Smiley，回歸請用新授予未編輯物品。沒有新Minecraft bootstrap hack、registry反射解凍或測試替代ItemStack。
Git commit: 未建立；index空白，HEAD仍527db03f671ba1e7b878009202733c302fa11158，未push。交付21檔，不含cache／build／run世界；待人工與唯讀審查。
Recommended next phase: 完成05A人工與獨立審查，停止於05A，未開始05B。

Compiled: PASS；Unit-tested: PASS；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行。

### 真實執行命令及結果

| 命令 | 結果 |
| --- | --- |
| `git status --short`／`git rev-parse HEAD`（開始） | clean；527db03f671ba1e7b878009202733c302fa11158 |
| `java -version` | exit0，Microsoft OpenJDK25.0.3+9-LTS |
| `javap`／`javap -c`（本機merged26.3及Fabric key/network/lifecycle JAR） | 查核API與指定bytecode，細節見API_COMPATIBILITY；沒有sources JAR，不冒稱完整原始碼審計 |
| `.\gradlew.bat build --no-daemon`（沙箱內初次） | exit1，Could not connect to the Gradle daemon，尚未編譯；沿用既有沙箱外驗證方式 |
| `.\gradlew.bat build --no-daemon`（沙箱外初次） | exit1，5個舊式Minecraft.screen/setScreen/displayClientMessage名稱錯誤；隨即查本機Gui／LocalPlayer及Screen.onClose bytecode，改用gui.screen()/gui.setScreen()/sendSystemMessage/sendOverlayMessage |
| `.\gradlew.bat build --no-daemon`（修正後） | exit0，33秒，64個JUnit PASS；其後新增限頻及最小／矩形payload測試 |
| `.\gradlew.bat clean build --no-daemon`（最終） | exit0，30秒，66個JUnit PASS，7 tasks executed |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit0 PASS，durations／formation-hold邊界／fade |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit0 PASS，soft/legacy／deterministic offsets／modulation／fade／budgets |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit0 PASS，4tick bloom／原samples-count-timing／scale／release連續性 |
| `git diff --check`（最終文件完成後） | exit0 PASS；初次曾指出PROJECT_PLAN末尾多一空行，已修正；既有LF→CRLF提示非whitespace錯誤 |
| `git diff --cached --name-only` | 無輸出，沒有實際暫存檔案；精確候選清單另列 |

補充：最初用系統python alias產生翻譯時該alias無法執行，未產生檔案；已改用PowerShell的JSON工具完成，沒有變更Python／Java或安裝新工具。失敗不隱藏，也不算測試通過。

### 產品測試覆蓋

- CanvasEditorTest 4：正式indexAt的首末格／小數／邊界／NaN／infinity；paint快速對角插值、透明橡皮擦、越界不繞格及重新進入不跨區補畫；全部1..32×1..32的1024種來源完整保留、取消隔離、快照防禦性複製及33拒絕。
- EditSessionTest 5：真正session.save授權、相同內容的新CanvasData可通過（非陣列參照）；重送／重入最多寫一次；eligible=false、換物件／slot／component／像素／缺原值拒絕；錯token、期限、無效畫布及改尺寸拒絕；無component只明確保存16×16；正式EditorRequestGate獨立open/save10tick邊界與立即保存。
- EditorPayloadTest 4：最大32×32 payload恰4116 bytes、clientbound snapshot同格式往返；1×1／1×32／32×1／7×19／16×16逐像素往返；錯版本／0／33／截斷／超大／尾隨拒絕；open請求恰1byte。
- 原53個全部維持：CanvasTest15、ItemCanvasCodecTest9、PlaybackPlaneTest2、CameraOrientationStabilityTest6、DevCanvasesTest5、DustGeometryDiagnosticsTest6、DustPatternTest6、PlaybackTest4。涵蓋既有persistent／stream codec、UseGate、播放轉換、Fixed/Dynamic、幾何及預算，不代表本輪runtime已測。

產物：`build/libs/drawfirework-1.0.0.jar`（86811 bytes）；SHA256 `1A90D17715AF4FAB7CAA8CBF46F395815072D4EF0535825C6D54CA7AF055DAC6`。既有檔名1.0.0沿用metadata，不當作產品V1.0完整發布。Gradle棄用警告仍存在，未擅自升級。

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

## 2026-10-23 — Phase 04C.2 驗證（歷史）

Phase: 04C.2 — Server-Authorized Firework Playback
Status: PARTIAL — 等待人工驗收與獨立審查
Changes: component→Item.use→ServerPlayback.use→UseGate.send→本人 ServerPlayNetworking.send→PlaybackPayload.CODEC→client receiver/execute→canvasPattern→activate→既有 tickDust；明確 server 世界中心經 DustPlane.atCenter 使用。
Build result: GRADLE_USER_HOME=D:\minecraftmod\gradle，工具核准沙箱外 `.\gradlew.bat clean build --no-daemon` 最終 exit0，15s、7tasks executed。起始1d4ac78、工作樹乾淨；所有依賴維持。
Tests performed: 首次編譯建置47既有測試通過；新增測試後兩次 clean build 因相同的 CustomPacketPayload.createType 完整ID錯誤失敗（各3項payload測試失敗），查明withDefaultNamespace後改Type(Identifier)。最終53/53，0 failures/errors/skipped；新增PlaybackTest4項、PlaybackPlaneTest2項。涵蓋實際payload往返／32上界／超限／缺資料／非法metadata／截斷／尾隨；真實產品UseGate拒絕及重入只發一次；HeartPrototype.canvasPattern直接呼叫與既有DustPattern結果一致；DustPlane.atCenter固定朝向及D階段不定位。不能把gate單測當完整Item.use網路實測。
Commands: `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java`、`tests/Phase025DustStyleCheck.java`、`tests/Phase026BloomCheck.java`（後兩者同java前綴）各exit0 PASS；git diff --check PASS。
Compiled: PASS；Unit-tested: PASS；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行。
Tests not performed: 物品use真實接線、S2C派送／雙手行為、client runtime／音效／世界切換／離線、多人本人範圍、效能與獨立審查。需依下列整合驗收補足，不以JUnit冒充。
Known problems: 僅整合伺服器原型，遠端多人client明確拒收；沒有延遲同步或事件跨世界重播。位置界限為各軸±30000000；超界安全拒絕。既有Gradle棄用警告不改。
Git commit: 本輪未提交、未push，不寫入受保護index；基準1d4ac78。工作樹僅下列檔案。
Recommended next phase: 先人工與獨立審查，不開始04D。Codex交付後停止寫入／無背景遊戲工作。

04C.1補登：依使用者本輪明確回報，1d4ac78、47/47 JUnit、三回歸、Creative/Survival/ItemStack診斷/存檔還原/滿背包人工驗收PASS；Antigravity PASS、零Critical/Major/Minor，正式PASS。完整審查全文未在本機，不捏造；以下舊未驗收是歷史。

### 最短人工驗收（04C.2尚未執行）

在 D:\minecraftmod 設 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`，`.\gradlew.bat runClient`，進開啟指令的測試單人世界。

1. Creative，`/drawfirework_test_item`，持主手，先`/drawfirework_test_item verify`，右鍵空氣；預期Smiley A20/B8/C14/D20/E14完整、Fixed/Bloom130/三粒質感一致，單次發射／爆炸聲，物品數量不變。verify仍只診斷、不播放。
2. 再verify確認完整component仍原值；間隔至少10tick重複、主副手交換、在Launch/Hold/Dispersion觸發取代，應最多一效果；10tick內重複可被拒絕。`/drawfirework clear`應清除，沒有舊動畫再生。
3. Survival右鍵拒絕不播放；Creative用`/give @s drawfirework:test_firework`裸物品右鍵亦安全拒絕。不補預設，不消耗。
4. 原Smiley物品放箱取回、存檔離開重進，再verify與右鍵，應同圖且數量/component不變。
5. 播放中切換維度、退出世界與離線，再進世界不得殘留／補播；新物品使用正常。
6. `/drawfirework heart`、原四Canvas及Warm/Cool、Fixed/Dynamic、clear正常。物品和指令交替取代應同樣正常。請回報各項及錯誤log；單人無法證明多人範圍，仍列未測，程式只send使用者本人且沒有廣播呼叫。

### 精確提交清單

本輪17檔（不包含build／快取），一般PowerShell可用以下清單；有未知差異先停下核對：

```powershell
Set-Location D:\minecraftmod
$files = @(
 'docs/PROGRESS.md', 'docs/PROJECT_PLAN.md', 'docs/DECISIONS.md',
 'docs/API_COMPATIBILITY.md', 'docs/TEST_RESULTS.md',
 'src/main/java/com/drawfirework/DrawFirework.java',
 'src/main/java/com/drawfirework/client/HeartPrototype.java',
 'src/main/java/com/drawfirework/client/DustPlane.java',
 'src/main/java/com/drawfirework/item/TestFireworkItem.java',
 'src/main/java/com/drawfirework/item/TestItemCommand.java',
 'src/main/java/com/drawfirework/network/PlaybackPayload.java',
 'src/main/java/com/drawfirework/network/ServerPlayback.java',
 'src/main/java/com/drawfirework/network/UseGate.java',
 'src/main/resources/assets/drawfirework/lang/en_us.json',
 'src/main/resources/assets/drawfirework/lang/zh_tw.json',
 'src/test/java/com/drawfirework/network/PlaybackTest.java',
 'src/test/java/com/drawfirework/client/PlaybackPlaneTest.java'
)
git status --short
git add -- $files
if ($LASTEXITCODE -ne 0) { throw 'git add failed; do not delete index.lock' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git diff --cached
git commit -m "feat: authorize creative canvas playback through bounded s2c event"
```

不push，commit不等於人工驗收通過。可交Antigravity對1d4ac78加本輪固定差異獨立唯讀審查。

## 2026-10-23 — Phase 04C.1 驗證與交付（歷史）

Phase: 04C.1 — Data Component & ItemStack Foundation
Status: PARTIAL — 等待人工驗收與獨立審查
Changes: 真正 Data Component／測試 Item 與有界讀取 Codec；最小 Creative 伺服器命令；遊戲內 ItemStack 診斷；9 項產品 Codec 測試。不接 A–E，不註冊自訂事件封包。
Environment: D:\minecraftmod；起始 HEAD f4785b3、工作樹乾淨；04B 功能 6569b56。Java 25.0.3+9-LTS／Minecraft 26.3／Fabric API 0.163.0+26.3／Loader 0.19.5／Loom 1.17.16／Wrapper 9.6.1 不變。Gradle 程序環境 GRADLE_USER_HOME=D:\minecraftmod\gradle。

| 實際命令／檢查 | 結果 |
| --- | --- |
| java -version | exit 0，Microsoft OpenJDK 25.0.3+9-LTS |
| .\gradlew.bat compileJava --no-daemon（沙箱內） | exit 1，Could not connect to the Gradle daemon，未到編譯 |
| .\gradlew.bat test --no-daemon（工具核准沙箱外） | exit 0，41s；當時 46 JUnit 通過 |
| .\gradlew.bat test --tests com.drawfirework.item.ItemStackFoundationTest --no-daemon | exit 1，classMethod 在初始化失敗：IllegalArgumentException: Not bootstrapped (called from registry minecraft:game_event)，來源 BuiltInRegistries.internalRegister；沒有執行 ItemStack 個別測試 |
| .\gradlew.bat clean build --no-daemon（改 live diagnostics 後） | exit 0，16s，7 tasks executed，46 JUnit |
| .\gradlew.bat clean build --no-daemon（最終，增加 NbtOps 與主手核對） | exit 0，16s，7 tasks executed，47 JUnit 全通過 |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase026BloomCheck.java | exit 0 PASS |
| build/test-results/test/TEST-*.xml | CanvasTest 15、ItemCanvasCodecTest 9、CameraOrientationStabilityTest 6、DevCanvasesTest 5、DustGeometryDiagnosticsTest 6、DustPatternTest 6；共 47，0 failure/error/skipped |

普通 ItemStack JUnit 嘗試已移除：該 bootstrap 不適用 Fabric registry 註冊生命週期，沒有以 @Ignore、假 ItemStack 或反射解凍掩蓋。替代為遊戲內 server 指令執行真正 ItemStack API；此診斷目前只有 Compiled，尚未執行，不能以 47 JUnit 宣稱 ItemStack 往返已驗證。

新增 9 JUnit 直接呼叫 ItemCanvasCodecs：JsonOps／NbtOps 往返、32x32 上界及一般 256 保留、33x1／1x33／256x256 編碼與解碼拒絕、0／負數／整數溢位／小數／缺失尺寸／版本／格式、非 hex／像素長度不符／8193 字元負載、stream 32x32 精確 4099 bytes、保留下個欄位、非法標頭及截斷在 pixel array 配置前拒絕、編碼無效值不寫入資料。

Artifact: build/libs/drawfirework-1.0.0.jar；SHA256 A548C1BE5DAE9C7F4531F2E0979F37A6F703D6EA367DCCB2DAF4367514EEB92D。開發驗收產物，非完整 V1.0 發佈版。
Compiled: PASS；Unit-tested: PASS（47＋三組回歸）；Runtime-tested: 未執行；Visually verified: 未執行；Multiplayer-tested: 未執行／超範圍。
Tests not performed: 遊戲啟動、註冊 lifecycle、live ItemStack NBT／JSON／stream 往返、存檔重載、模型／翻譯、Creative／Survival 伺服器行為、滿背包、獨立審查及效能量測。
Known problems: 真正遊戲整合尚待驗收；persistent Codec 不控制已被外層 NBT parser 建好的 tree／字串配置；沿用遊戲外層 NBT 限制。既有 Gradle 棄用警告未修改。
Git commit: 本輪未建立，保留固定工作樹交付；未寫 Git index、未 push、不處理 index.lock。
Recommended next phase: 先本階段人工驗收與唯讀獨立審查，04C.2 未開始。

### 最短人工驗收（尚未執行）

使用既有啟動方式：PowerShell 在 D:\minecraftmod，設定 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'` 後執行 `.\gradlew.bat runClient`；進入開啟指令的測試單人世界，不改動重要世界。

1. `/gamemode creative`，空出一格，`/drawfirework_test_item`。應取得單件「畫布測試煙火（創造限定／尚無動畫）」且有正常原版火箭外觀。放主手，右鍵空氣：伺服器回報 16x16、hash 與尚無動畫，不消耗、不出現粒子／聲音。副手先留空以隔離原版其他物品互動。
2. 保持主手該物品，`/drawfirework_test_item verify`。預期 `04C.1 ItemStack diagnostics PASS`；會驗主手完整值等於原始 Smiley，並用暫時 stack 測 Smiley／32x32 的 write/read、copy 隔離、NBT／JSON／stream 真正 ItemStack 往返、缺 component、錯誤尺寸與超大 payload 拒絕。出現 FAIL 時提供 run/logs/latest.log 對應錯誤，不視作通過。
3. 先放箱子再取回，存檔退出、重新進同世界，再右鍵比較相同 hash、再執行 verify，兩者均應正常。verify 的主手核對才驗到實際存檔後資料，暫時 stack 往返不能取代此步。
4. `/give @s drawfirework:test_firework` 取得無 component 的裸物品。右鍵應回缺失／無效資料且不消耗、不崩潰、不默補 Smiley。切 `/gamemode survival`，右鍵兩種物品都應拒絕；授予及 verify 指令不可用，無免費測試功能。
5. 回 creative 填滿一般背包格，再執行授予，應回 inventory full，其他格不變且不掉落額外物品。必要時將授予物品放箱子存取；最後確認原有 heart／canvas 指令仍正常。本輪程式未改任何原有渲染檔。

請回報上述 5 項、verify 輸出、存檔重載 hash 是否一致，以及任何 log 錯誤。尚未收到結果前保持 PARTIAL。

### 精確提交檔案（人工審查後，一般 PowerShell）

```powershell
Set-Location D:\minecraftmod
$phase04c1Files = @(
  'src/main/java/com/drawfirework/DrawFirework.java'
  'src/main/java/com/drawfirework/canvas/CanvasSerialization.java'
  'src/main/java/com/drawfirework/canvas/ItemCanvasCodecs.java'
  'src/main/java/com/drawfirework/component/ModDataComponents.java'
  'src/main/java/com/drawfirework/item/ModItems.java'
  'src/main/java/com/drawfirework/item/TestFireworkItem.java'
  'src/main/java/com/drawfirework/item/TestItemCommand.java'
  'src/main/java/com/drawfirework/item/ItemStackDiagnostics.java'
  'src/main/resources/assets/drawfirework/items/test_firework.json'
  'src/main/resources/assets/drawfirework/lang/en_us.json'
  'src/main/resources/assets/drawfirework/lang/zh_tw.json'
  'src/test/java/com/drawfirework/canvas/ItemCanvasCodecTest.java'
  'docs/PROGRESS.md'
  'docs/TEST_RESULTS.md'
  'docs/PROJECT_PLAN.md'
  'docs/DECISIONS.md'
  'docs/API_COMPATIBILITY.md'
)
git status --short
git add -- $phase04c1Files
if ($LASTEXITCODE -ne 0) { throw 'git add failed; do not remove lock files' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git diff --cached --stat
git diff --cached
git commit -m "feat: add phase 04c1 canvas component and creative test item"
if ($LASTEXITCODE -ne 0) { throw 'commit failed' }
git status --short
```

只提交上述本階段檔案；有未知差異先核對。commit 不代表人工驗收通過，不 push。

## 2026-10-23 — Phase 04B 正式驗收補登（歷史）

Phase: 04B — Dynamic Canvas Presets & Renderer Integration
Status: PASS
Changes: 僅文件結案，基準 HEAD 6569b56，開始時工作樹乾淨。
Build result: 既有 clean build --no-daemon exit 0（27s）；本輪未重跑。
Tests performed: 既有 38/38 JUnit、三組 Phase 02 動畫回歸 PASS。本輪唯讀核對 build/test-results/test 五份 XML：15＋6＋5＋6＋6＝38，0 failures／0 errors；這是讀取既有結果，並非重新執行測試。

使用者於 2026-10-23 本次對話明確確認以下最新人工驗收，5/5 PASS：

1. 原始、Warm、Cool Smiley 輪廓相同且配色不同。
2. Warm／Cool 150% 尺寸與形狀正常。
3. Launch／Hold／Dispersion 切換沒有異常殘留。
4. clear、重複觸發及重新進入世界正常。
5. Fixed／Dynamic、四種原預設及 heart 均正常。

Antigravity 獨立唯讀審查：PASS，無 Critical／Major／Minor，依使用者本次確認補登。本機沒有 04B 審查全文；審查當下文件所載「等待人工驗收」已由上述最新結果取代，不構成重測需求。沒有把舊人工清單中未逐項回報的未知指令拒絕、特定維度切換、音效或效能量測額外推定為已測。

Compiled: PASS（既有建置）；Unit-tested: PASS（既有 38 項及三組回歸）；Runtime-tested: PASS（使用者上述範圍）；Visually verified: PASS（使用者上述範圍）；Multiplayer-tested: 未執行／不在 04B 範圍。
Tests not performed: 本輪未重新執行建置／遊戲／JUnit；04C 編譯、component／ItemStack 往返、網路及物品使用均未執行。04C 研究只讀取本機依賴，以 javap／javap -c -p 查簽章及必要 bytecode，未產生原型或測試檔。
Known problems: Dust 既有限制及效能未量測；無使用者回報之新增故障。
Git commit: 04B 功能 6569b56；本輪三份文件未提交、未 push。
Recommended next phase: 04C 架構提案見 PROJECT_PLAN；等待批准實作。

下方清單及 PARTIAL 是先前交付歷史，由本節更新其狀態，不改寫當時實際執行紀錄。

## 2026-10-23 — Phase 04B 測試與人工清單（歷史）

Phase: 04B — Dynamic Canvas Presets & Renderer Integration
Status: PARTIAL — 等待人工驗收及獨立審查
Changes: 執行時有限 Smiley 配色生成與兩個 Canvas 指令入口；五項直接產品程式測試。無 04C/04D 功能。
Environment: D:\minecraftmod；起始 HEAD 8246713；git status --short 無輸出。Java Microsoft OpenJDK 25.0.3+9-LTS；Loom 1.17.16／Gradle Wrapper 9.6.1；所有版本維持原值。Codex 唯一修改者，Antigravity 唯讀審查。

| 實際命令／證據 | 結果 |
| --- | --- |
| java -version | exit 0，25.0.3+9-LTS |
| $env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; .\gradlew.bat clean build --no-daemon（沙箱內） | exit 1，Could not connect to the Gradle daemon，尚未執行編譯 |
| 同一 clean build --no-daemon（沙箱外核准執行） | exit 0，BUILD SUCCESSFUL in 27s，7 tasks executed；含 test |
| build/test-results/test/TEST-*.xml | 38 tests、0 failures、0 errors、0 skipped |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase026BloomCheck.java | exit 0 PASS |
| git diff --check | exit 0；僅有 Git 自動換行提示 |

JUnit：CanvasTest 15、CameraOrientationStabilityTest 6、DustGeometryDiagnosticsTest 6、DustPatternTest 6、DevCanvasesTest 5。新增測試直接呼叫 DevCanvases.smiley(palette) 及 DustPattern.fromCanvas：Warm/Cool 差異、重複確定性與像素副本隔離、16×16／原始語義色塊與 alpha mask、不合法 enum 名稱及 null 拒絕、相同樣本座標／尺寸與不同 RGB、128/192/266 預算及 ≤798 核心粒子、交替生成不污染先前資料。產品不接受額外尺寸／數值參數；未知命令 literal 的遊戲拒絕另列人工測試。純資料交替測試不冒充粒子引擎替換測試。

Artifact: build/libs/drawfirework-1.0.0.jar，SHA256 C2FFCC81139CF4245E72EC72235AC1171F66070F36FA415C0F9131E617FE4AD0。開發驗收產物，不是 V1.0 完整發佈版。

Compiled: PASS；Unit-tested: PASS；Runtime-tested: 本輪未執行；Visually verified: 本輪未執行；Multiplayer-tested: 未執行／超範圍。
Known problems: Gradle 棄用警告未改；Dust 原版隨機亮度、半透明大小近似、Fixed 側視透視；無 GPU/frame time/FPS 量測。
Git commit: 未建立（保留供審查與人工提交）；未 push。沒有嘗試寫入 Git index，無鎖檔修復。
Recommended next phase: 人工驗收與獨立唯讀審查；不開始 04C。

### 最短人工驗收（本輪尚未執行，請回報每項結果）

單人開闊天空、粒子設為全部，先完成每個效果再比較：

1. `/drawfirework canvas smiley`、`/drawfirework canvas smiley_warm`、`/drawfirework canvas smiley_cool`：同尺寸、同輪廓、相同透明環／背景，配色明顯不同，A20/B8/C14/D20/E14 連續，沒有重複聲音。Warm 橙色臉／紅眼，Cool 青色臉／深藍眼；Dust 色彩允許原版明暗變化。
2. Warm → Cool → Warm：分別在 Launch（尾跡上升）、Hold（完整臉）、Dispersion（散開）時重新輸入另一指令。可先完整播放熟悉時點；以實際看到的階段判斷，不把聊天輸入耗時當 tick 證據。新效果應取代舊效果，沒有舊配色殘留、舊粒子繼續生成或例外。快速重複相同命令亦只保留一個效果。
3. 對新圖在上述各階段執行 `/drawfirework clear`；Hold／Dispersion 退出重入、切換維度或世界，確認沒有殘留與後續重生。
4. `/drawfirework canvas smiley_warm 150 192 plain fixed` 與 `/drawfirework canvas smiley_cool 125 128 plain dynamic`：驗證選項順序、大小與回饋預算；轉頭確認 Fixed 保持世界平面、Dynamic 僅形成／停留面向相機；裸指令仍 100%／Fixed。側視透視縮短不是平面自轉。
5. `/drawfirework canvas dense`、`/drawfirework canvas grid`、`/drawfirework canvas rect`、`/drawfirework heart` 正常且維持舊效果；`/drawfirework canvas smiley_neon` 應拒絕且不啟動新效果。回報卡頓／錯誤與粒子設定，不推算 FPS。

### Phase 04A 完成證據補登（依使用者確認）

使用者在本次授權明確確認 04A 正式 PASS：Codex Review Fix 01 已完成，Antigravity 獨立複查 PASS，1 Major／2 Minor 全 CLOSED；33 JUnit、三組 Phase 02 回歸、使用者 5/5 遊戲回歸通過。對應 1243af9、9040a35、8246713。本機尚無最新複查全文／逐項觀察原文，不捏造它們；本段記錄的是使用者確認，非本輪重新執行。下方舊 04A 待回歸／PARTIAL 紀錄由此補登取代。04B 新功能仍須上述人工驗收。

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

## 2026-10-23 — Phase 04A Finalization 測試結果（歷史；覆蓋範圍由 Review Fix 01 更正）

目錄 D:\minecraftmod；工具鏈不變（Minecraft 26.3／Java 25.0.3／Wrapper 9.6.1／Loom 1.17.16）；寫入者 Antigravity；HEAD `94a4d21`。

| 命令/檢查 | 結果 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL 20s，7 tasks executed |
| JUnit 總計 | 35/35 通過（0 失敗、0 錯誤、0 略過）：<br>• `CanvasTest`: 15 通過<br>• `CameraOrientationStabilityTest`: 8 通過（新增：Canvas 預設 Fixed 驗證、Dynamic 選擇、相機旋轉與玩家平移之免疫性、正交映射、可逆性、無漂移、等距保持比例、散開對比、物理散射獨立性證明）<br>• `DustGeometryDiagnosticsTest`: 6 通過<br>• `DustPatternTest`: 6 通過 |
| Phase 02 動畫回歸 | `Phase02TimelineCheck` exit 0、`Phase025DustStyleCheck` exit 0、`Phase026BloomCheck` exit 0 |
| JAR SHA256 | `DB04A04A61806E9372F102A4AD3C468C6A0369D1A009FDD479A1D661BD774387` |

| 類別 | 狀態 |
| --- | --- |
| Compiled | PASS |
| Unit-tested | 35 個測試曾執行通過；舊相機測試未覆蓋產品狀態更新，不能據此宣稱相機穩定性已驗證 |
| Runtime-tested | PARTIAL（使用者已驗收核心視覺設計；最新預設入口及完整生命週期尚待最終遊戲回歸） |
| Visually verified | 人工驗收 Fixed World Plane 正式選定；粒子密度微調與尺寸預設留待後續評估 |
| Multiplayer-tested | 不在範圍 |

## 2026-10-23 — Phase 04A.2 測試結果

目錄 D:\minecraftmod；工具鏈不變（Minecraft 26.3／Java 25.0.3／Wrapper 9.6.1／Loom 1.17.16）；寫入者 Antigravity；HEAD `94a4d21`。

| 命令/檢查 | 結果 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL 20s，7 tasks executed |
| JUnit 總計 | 33/33 通過（0 失敗、0 錯誤、0 略過）：<br>• `CanvasTest`: 15 通過<br>• `CameraOrientationStabilityTest`: 6 通過（初始正交映射、0°→45°→0° 嚴格可逆、100 次擺動無累積漂移、移動相機等距保持縮放比例、Formation/Hold 散開與固定/動態對比、Dispersion 物理散射獨立性證明）<br>• `DustGeometryDiagnosticsTest`: 6 通過<br>• `DustPatternTest`: 6 通過 |
| Phase 02 動畫回歸 | `Phase02TimelineCheck` exit 0、`Phase025DustStyleCheck` exit 0、`Phase026BloomCheck` exit 0 |
| JAR SHA256 | `54BC356A4DF802BFAAF5343650F9D6C11725D1E144BDE97E00AAD1AEBC3DB8FF` |

| 類別 | 狀態 |
| --- | --- |
| Compiled | PASS |
| Unit-tested | PASS（相機旋轉數學、幾何正交性、Euler YXZ 無漂移、Dispersion 凍結證明均已自動化驗證） |
| Runtime-tested | 未執行（等待使用者於遊戲內對比 fixed 與 dynamic 指令） |
| Visually verified | 未執行（等待使用者人工比較） |
| Multiplayer-tested | 不在範圍 |

## 2026-10-23 — Phase 04A.1 測試結果

目錄 D:\minecraftmod；工具鏈不變（26.3／Java 25／Wrapper 9.6.1／Loom 1.17.16）；寫入者 Antigravity；HEAD `94a4d21`，04A 及 04A.1 變更均未提交（工作樹有未提交差異）。

| 命令/檢查 | 結果 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon`（第 1 次） | exit 1：測試檔自身問題（`-0.0000` 格式化鏡像鍵）；非產品碼 |
| 同上（第 2 次） | exit 1：測試期望錯誤（像素中心距比應為 23/15 而非 1.5）；非產品碼 |
| 同上（第 3 次，最終） | exit 0，BUILD SUCCESSFUL 17s |
| JUnit | 27/27 通過：CanvasTest 15、DustPatternTest 6、DustGeometryDiagnosticsTest 6（Grid/Rect 精確與鏡像對稱、Smiley 全像素精確、scaled 保持中心/長寬比/顏色/點數、預算 128/192/266 ≤798 粒、offset/pitch、Dense 不均勻度）|
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java`／`Phase025DustStyleCheck`／`Phase026BloomCheck` | 三者 exit 0 |
| JAR SHA256 | `A9C89B9869828454F92F7029E870CAC6EB856F21C3BA2CAA4AF10F7C199A36F3` |
| 診斷輸出 | offset/pitch heart 0.31／smiley 0.45／grid 0.71／dense32 0.91；dense 最近鄰距 128→mean1.68 max3.16、192→1.53/3.16、266→1.35/2.24（原像素單位） |

| 類別 | 狀態 |
| --- | --- |
| Compiled | PASS |
| Unit-tested | PASS |
| Runtime-tested | 未執行（新指令、計時日誌尚未在遊戲內跑過） |
| Visually verified | 未執行，等使用者人工比較 |
| Performance (FPS/frametime) | 未量測；不宣稱任何改善 |
| Multiplayer-tested | 不在範圍 |

## 2026-10-23 — Phase 04A 測試結果

目錄 D:\minecraftmod；起始 HEAD `94a4d21`、`master`、工作樹乾淨。工具鏈未變（Minecraft 26.3、Java 25、Wrapper 9.6.1、Loom 1.17.16）。寫入者 Antigravity。

| 命令/檢查 | 實際結果 |
| --- | --- |
| `git status --short --branch` | 開始時乾淨 |
| `javap -c net.minecraft.core.particles.ScalableParticleOptionsBase`（本機 26.3 merged JAR） | 尺寸 clamp 0.01f..4.0f，確認 alpha 尺寸範圍合法 |
| `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'; .\gradlew.bat clean build --no-daemon` | exit 0，BUILD SUCCESSFUL 17s，7 tasks executed；僅既有 Gradle 10 棄用警告 |
| JUnit | 21/21 PASSED：CanvasTest 15、DustPatternTest 6（heartMatchesPhase02Regression、transparencyStrategy、coreBudgetNeverExceeds800、smallImagesAreNotInflated、geometryScaleAspectAndDeterminism、invalidInputsRejected） |
| JAR | `build/libs/drawfirework-1.0.0.jar` SHA256 `46A8D15BFDA02D26E14B760816963DF758357801B5EC62A38AEA2EB60BE52371`；含 firework/DustPattern、DevCanvases，無測試類 |
| `firework` 套件 import 檢查 | 無 `net.minecraft`/`fabricmc` 引用（client/server 分離） |

| 類別 | 狀態 |
| --- | --- |
| Compiled | PASS |
| Unit-tested | PASS（heart 63 點/189 粒逐點一致；alpha 0 無點；partial alpha 保留 RGB 且縮小；256×256 全彩 copies 1/2/3 → 800/400/266 點，Bloom 798 粒；>800 拒絕） |
| Runtime-tested | 未執行（未啟動 runClient） |
| Visually verified | 未執行；操作步驟見 PROGRESS.md Phase 04A |
| Multiplayer-tested | 未執行（不在範圍） |

Phase 02 的三個 source-launcher 動畫檢查未重跑；其受測檔 `DustStyle.java`、`FireworkTimeline.java` 本輪未修改。

## 2026-10-23 — Phase 03 測試結果

目錄D:\minecraftmod；起始HEAD7477eb8、工作樹乾淨。已讀Phase02 review PASS及必要文件。Java Microsoft25.0.3+9-LTS，既有工具鏈不變；每次Gradle程序設定 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`。

| 命令/檢查 | 實際結果 |
| --- | --- |
| `.\gradlew.bat clean build --no-daemon`（沙箱） | exit1，無法連daemon19604，未編譯 |
| 同命令（核准放寬限制，首輪） | exit0，27s，7 tasks executed；14個JUnit通過，非NO-SOURCE |
| 首輪比較 | 孤立像素nearest漏掉、area alpha3.984375、自適應alpha255；棋盤格16點全紅，發現並修正tie偏差 |
| 同命令（最終程式，核准放寬限制） | exit0，31s，7 tasks executed；15個JUnit，0 skipped/failures/errors；既有Gradle10棄用警告保留 |
| `java --class-path build/classes/java/main tests/Phase02TimelineCheck.java` | exit0 PASS |
| `java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java` | exit0 PASS |
| `java --class-path build/classes/java/main tests/Phase026BloomCheck.java` | exit0 PASS |
| `git diff -- src/main/java/com/drawfirework/client gradle.properties settings.gradle gradle/wrapper` | 無輸出；動畫及版本/Wrapper未改 |
| `jar tf build/libs/drawfirework-1.0.0.jar` | 含canvas與原動畫，無JUnit/測試類/世界/快取 |
| `git diff --check` | 通過，只有LF/CRLF提示 |

來源src/test/java/com/drawfirework/canvas/CanvasTest.java，由build→check→test執行標準JUnit4。XML `build/test-results/test/TEST-com.drawfirework.canvas.CanvasTest.xml`：tests15、failures0、errors0、skipped0、suite0.818s；HTML `build/reports/tests/test/index.html`。可重建報告不提交，重要結果留此。

覆蓋：1×1、全透明含隱藏RGB、單一alpha1像素、8/16愛心、32/256密集全彩、細斜線、棋盤格、透明邊框/高透明比例、8×2及1×256/256×1；0/1/2/266/800預算、預算不足/非法、原色/座標/權重/無重複、輸出不可變/確定性及60組固定seed隨機矩形。尺寸/面積溢位極值/像素長度/null/建構與存取複製/equals/hashCode；JSON/Codec透明與半透明往返，缺欄/壞hex/長度/版本/格式/超大/深巢狀/重複/未知欄/型別/小數/溢位/巨大指數拒絕。800核心與200裝飾獨立，3副本266點798粒、267點拒絕。

本機最大尺寸診斷（非JMH）：256²固定seed全彩，5次暖機/20次降至266點，平均 **2.536815ms/op**，ThreadMXBean當前執行緒 **41939 allocated bytes/op**；最大JSON **524360 ASCII bytes**。僅此JVM情境，無速度門檻、硬體保證、遊戲frame time、GPU/GC暫停或FPS結論。

最終棋盤格32²→16點紅10/藍6，保留原色但比例有偏差。比較只是合成樣本，不能代表所有圖像。未進行不同解析度/透明度/距離的遊戲畫質驗收；Phase04整合後才可同圖同視角比較，目前沒有動態煙火觸發命令。

JAR SHA256：`38E769F383B8F6F13D98944E43CFC397990A0AC9FDB588D45688E8B5E23FB36E`；`build/libs/drawfirework-1.0.0.jar`是開發驗證產物，不是完整V1.0發布。

Compiled PASS；Unit-tested PASS（15標準JUnit＋3既有source-launcher）；Runtime-tested、Visually verified、Multiplayer-tested本輪未執行。Phase03資料邏輯PASS、待獨立審查。Git收尾：明確11檔git add失敗，.git/index.lock Permission denied；未暫存/commit/push，沒有自動審核拒絕，停止Git寫入；使用者命令見PROGRESS。

重現：設定上述GRADLE_USER_HOME後執行 `.\gradlew.bat clean build --no-daemon`；僅重跑Phase03可用 `.\gradlew.bat test --rerun-tasks --no-daemon`。另跑表中3個Java命令，逐步檢查 `$LASTEXITCODE`，不可把NO-SOURCE當測試通過。

## 2026-10-23 — Phase 02 Finalization（最終交付）

### 使用者最終人工視覺結論

本次使用者已完成02.6比較：最喜歡Bloom130%，圖案較飽滿、綻放自然流暢；Hold14合適，Formation/Dispersion/Fade已認可。Normal100/Bloom115保留比較但不預設。仍輕微稀疏，部分斜角圖案可能明顯變形；只記觀察，不宣稱根因或已解決。這補充02五階段正常、02.5較佳/無明顯頓挫等既有使用者證據，舊紀錄完整保留。

### 本輪實際命令與結果

環境D:\minecraftmod，開始HEAD872bd52、暫存區空；已有6個tracked修改與5個untracked階段檔（新Java2、新測試3），未覆蓋或還原。讀取AGENTS、計畫/進度/測試/決策與最新phase-01-review。程式只改HeartPrototype兩處BLOOM_115→BLOOM_130，其餘02–02.6實作保留；工具鏈/metadata與HEAD無差異。

| 命令/檢查 | 結果 |
| --- | --- |
| 程序GRADLE_USER_HOME=D:\minecraftmod\gradle；.\gradlew.bat clean build --no-daemon（核准放寬沙箱） | exit0，13s，5 tasks executed；Gradle test NO-SOURCE；既有Gradle棄用警告保留 |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit0，PASS |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java | exit0，PASS |
| java --class-path build/classes/java/main tests/Phase026BloomCheck.java | exit0，PASS |
| 源碼MASK計數及生成迴圈核對 | 63有效點；normal/115/130每點3→189核心；legacy63；age1–19每tick1裝飾，整段≤19；保守總上界208/legacy82，低於核心800/裝飾200獨立上限；無每tick圖像補生 |
| heart/heart dust指令鏈、start預設及javap -c -p HeartPrototype | 無preset的start和初始化皆BLOOM_130；比較配置SOFT/BLOOM_115/BLOOM_130/LEGACY仍存在；這是靜態/編譯驗證，不冒稱本輪遊戲alias觸發測試 |
| git diff --check | exit0，僅LF/CRLF提示；最後文件再檢查 |
| Get-FileHash build/libs/drawfirework-1.0.0.jar | SHA256 B4DBE191A189F895325EFBB1D2326FA5C227D6449D2010B33F98939DEE0AADEE |

Compiled PASS；Unit-tested純邏輯PASS（三個source-launcher測試，非Gradle test）；Runtime-tested/Visually verified：使用者對02–02.6上述視覺範圍PASS，本輪未啟動遊戲；Multiplayer-tested未執行且超範圍。仍未逐項實測clear、切世界/退出重入、相機缺失、密集重複、聲音次數及frame time/GC。全項驗收維持PARTIAL，視覺選型PASS；系列開發依使用者要求收尾凍結，不藉未測項提前開始03。

比較命令：heart與heart dust預設130；heart dust normal / bloom115 / bloom130均保留；heart dust legacy、heart image、clear不變。正常20TPS總3.8秒，Hold0.7秒，Bloom4tick；未改方程或粒子數。

Antigravity應核對：完整11檔差異（包含原02/02.5/02.6未提交檔）、預設與比較指令、phase/壽命/size比率、D解除定位、null/世界/clear清理、單效果替換與預算、client/common隔離、使用者觀察與未測項的界線。稀疏/斜角變形僅研究項，根因未證實。Git收尾結果見PROGRESS最新節及本文件後續記錄。
以下為歷史紀錄。

## 2026-10-23 — Phase 02.6（最新，PARTIAL）

### 保留Phase02.5使用者觀察

本次使用者已比較：新版明顯優於舊版，格點感改善但仍稀疏；Hold14剛好；Formation/Dispersion/Fade自然流暢，無明顯動畫頓挫。記為上述範圍Runtime-tested/Visually verified PASS（使用者，非Codex觀看）；沒有個別clear/世界清理/性能量測證據。本輪Bloom另列待驗證，不覆蓋此紀錄。

### 實際驗證

工作目錄D:\minecraftmod，HEAD872bd52；開始已存在02–02.5未提交差異，讀取AGENTS/最新計畫/進度/決策/測試/原型及最新審查phase-01-review；保留所有既有內容。本輪無新增Minecraft API，重新以本機merged26.3 javap查核scale及其setSize實作；詳細見API_COMPATIBILITY。

| 命令 | 結果 |
| --- | --- |
| 程序GRADLE_USER_HOME=D:\minecraftmod\gradle；.\gradlew.bat clean build --no-daemon（核准放寬沙箱） | exit0，12s，5 tasks executed；Gradle test NO-SOURCE；既有棄用警告保留 |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit0，舊時間與fade回歸 |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java | exit0，舊SOFT/LEGACY、seed/偏移/微變/預算回歸 |
| java --class-path build/classes/java/main tests/Phase026BloomCheck.java | exit0，三組相同sample/數量/Hold、4tick綻放、倍率界限、不累積、D不縮回/端點連續；不包含真實粒子引擎/指令/渲染測試 |
| git diff --check | exit0，僅換行提示；最終文件再次檢查 |
| Get-FileHash build/libs/drawfirework-1.0.0.jar | SHA256 52ECFB9498132A0978A99FC06E32E80FEB0F71545417A02AE1F358A7E6164787 |

02.6 Compiled PASS；Unit-tested純邏輯PASS（獨立Java source-launcher，非Gradle test）；Runtime-tested/Visually verified未執行；Multiplayer-tested未執行。清理路徑保持原有，但本輪未遊戲回歸。三配置核心189、裝飾≤19、保守總上界208；更大sprite可能增加overdraw，未量測FPS/GC/frame time。未建立commit，保留此前與本輪差異；無背景遊戲或監看。

### 人工比較指令

- `/drawfirework heart dust normal`：原Phase02.5，100%基礎尺寸。
- `/drawfirework heart dust bloom115`：115%（預設，heart/heart dust亦使用此組）。
- `/drawfirework heart dust bloom130`：130%。
- `/drawfirework heart dust legacy`：仍保留Phase02舊單粒28tick對照；image/clear不變。

單人開闊天空、粒子全部、固定視線/位置/時間，依序觸發並等每次3.8秒結束。比較剛形成→Hold前4tick是否柔和綻放、填隙是否改善、輪廓/色帶是否過亮或糊成塊；愛心外框座標不應整體放大，只有每粒可見面積變大。觀察Hold→D沒有突然縮回，D軌跡與E消失仍自然；錄影回報偏好100/115/130。另回歸clear、快速交替指令、退出/重入/切世界，確認無殘留。新三配置尚無人工比較，不標PASS。

### 啟動等待：唯讀診斷

來源：既有gradle/daemon/9.6.1/daemon-{14592,6136,12644}.out.log、run/logs/latest.log、gradle.properties、Wrapper properties及快取目錄。未執行runClient、下載/清除/搬移快取或更換工具鏈。

- daemon14592：12:00:48.412開始執行build → 12:01:29 Minecraft Loading（約41秒，含Gradle設定/任務/遊戲JVM前段，非單一可定位原因）；12:01:51模組初始化、12:01:54建立SDL視窗（Loading後約25秒）；12:01:56資源重載；12:02:21開始整合伺服器；12:04:36 Stopping。BUILD SUCCESSFUL in 5m2s包含遊戲開啟/遊玩/退出，不能當作啟動時間或下載耗時。
- daemon6136：11:40:29.261 build執行 → 11:40:36 Loading，前段約7秒；BUILD SUCCESSFUL in 10m26s同樣包含遊戲時間。存在啟動前段變慢的證據，但缺任務級時間/verbose輸出，根因仍未確認。
- 所查日誌未找到Downloading/Resolving訊息，且已有Wrapper9.6.1、modules-2、Loom26.3快取；不能據非verbose輸出排除依賴重新解析或下載。使用兩處不同GRADLE_USER_HOME可能導致各用一套快取：專案D:\minecraftmod\gradle已完整使用；C:\Users\user\.gradle亦存在Wrapper目錄，本輪shell未設定此變數。無證據使用者該次用了錯誤目錄；遊戲Mixin來源明確指向D盤專案快取。
- daemon記錄RequestStopIfSingleUsedDaemon，確實每次使用single-use JVM；--no-daemon仍需符合-Xmx2G等設定的新JVM。這是可辨識開銷，但日誌沒有足夠數據將41秒全部歸因daemon。401/Realms在Download-2背景執行緒報錯，仍有視窗/資源/世界載入，不能據此斷定是主因。
- 建議（未套用）：固定同一PowerShell程序的GRADLE_USER_HOME=D:\minecraftmod\gradle，保持JAVA_HOME一致；日常比較直接runClient，無需每次clean build或--refresh-dependencies。可試 `runClient --daemon --info --console=plain` 重用相容Gradle JVM並取得任務輸出，記錄開始/Loading/視窗/主選單時刻，與--no-daemon比較；這僅可能減少Gradle前段，無法保證減少遊戲載入。快取完整時可另試--offline區分網路解析因素，缺件則正常失敗，回復原命令，不刪快取。
- 本次嘗試開啟Gradle9.6.1官方daemon/CLI文件，web工具回傳不可存取；以上診斷以本機日誌與已執行命令為證，不宣稱已完成官方頁面閱讀。未變更任何啟動配置；下一次如仍慢，保留--info輸出才能定位Gradle任務與遊戲啟動前段。

以下歷史紀錄保留。

## 2026-10-23 — Phase 02使用者視覺驗證及02.5微調（最新）

Phase02：使用者本次明確回報「五階段動畫均能正常運作，整體效果漂亮」，Runtime-tested/Visually verified於此範圍PASS。另指出Hold過於稀疏/規則、可見固定格點；授權02.5改善。未提供個別clear/退出重入/切世界/效能結果，不將它們擴充標PASS。此證據保留且與02.5待驗收區分。

02.5環境D:\minecraftmod、既有Java25/Minecraft26.3及所有工具鏈不變。開始HEAD872bd52，工作樹包含上輪Phase02未提交變更；讀取AGENTS、計畫/進度/測試/決策與最新phase-01-review（沒有Phase02 review）。沒有其他已知GUI寫入。只沿用已查證Minecraft API，未引入新API。

| 實際命令/操作 | 結果 |
| --- | --- |
| GRADLE_USER_HOME=D:\minecraftmod\gradle；.\gradlew.bat clean build --no-daemon（核准放寬沙箱） | 首次exit0，12s，5 tasks executed |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | PASS，舊時間常數/形成/淡出回歸 |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java（首次） | exit1；模擬浮點縮放終值以==1斷言過嚴，先前容差斷言已通過；改為1e-5容差，不改產品行為 |
| 同02.5測試修正後 | exit0，PASS |
| 最終clean build --no-daemon（同環境） | exit0，11s，5 tasks executed；Gradle test NO-SOURCE；既有Gradle棄用警告保留 |
| 最終再次依序執行以上兩個Java source-launcher測試 | 兩者exit0；02.5驗證新舊時間、固定偏移可重現/半徑/深度界限、大小倍率不累積/強度範圍/轉場端點、淡出單調與預算、非法參數拒絕 |
| git diff --check | exit0，僅LF/CRLF提示；最終文件再檢查 |
| Get-FileHash build/libs/drawfirework-1.0.0.jar | SHA256 9641748ABAAE24A7A7C8BB526E7DB946CB9271D494C4F1BCFCC305A7D4B9CAC6 |

02.5 Compiled PASS；Unit-tested純邏輯PASS（獨立測試，非Gradle test）；Runtime-tested/Visually verified未執行；Multiplayer-tested未執行。沒有frame time/GC/FPS量測。核心上界63×3=189、裝飾整段最多19，保守同時上界208；legacy核心63/總上界82；實際引擎null/剔除/短壽命會降低存活量。無每tick圖像補生；一次僅1效果。測試不宣稱粒子引擎、遊戲清理與實際視覺連續性已通過。

### 02.5 人工比較（待執行）

沿用單人開闊天空、粒子全部、相同視線/位置與光照；如需啟動，D:\minecraftmod下設定 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'` 後 `.\gradlew.bat runClient --no-daemon`。
1. `/drawfirework heart dust legacy`：Phase02舊樣式，1粒/點、Hold1.4秒、總4.5秒；觀察舊格點感。
2. `/drawfirework heart` 或 `/drawfirework heart dust`：02.5新樣式，3粒/點、Hold0.7秒、總3.8秒；比較格點感是否減輕、輪廓仍清楚、色帶未偏移或過亮、三粒不糊成塊。兩指令依序執行，前一效果結束後再觸發。
3. 錄影比較Formation→Hold→Dispersion：偏移不應每tick跳動或在階段間突然重排；大小僅微變，D接續原位置自由運動；最後縮小清除。短Hold是否仍足以辨識請回報。
4. 中途clear、連續交替soft/legacy/image、退出重入與切世界：不得累積/殘留或例外；轉動視角時C billboard、D世界釋放行為維持。image仍只有Phase01對照，不拿來替代Dust。
5. 回報偏好soft或legacy、密度/偏移/大小變化/Hold時間是否合適，以及卡頓/清理/聲音異常；若有frame time紀錄附設定與硬體，不以主觀漂亮代替量測。

Phase: 02.5；Status: PARTIAL；Git commit: 未建立，保留上輪及本輪差異；Recommended next phase: 本階段人工比較，不進Phase03。以下保留歷史紀錄。

## 2026-10-23 — Phase 02（最新，PARTIAL）

Phase01由使用者正式確認PASS；核對d1513ed存在，開始HEAD=872bd52且git status --short無輸出。完整閱讀AGENTS、五份docs、phase-01-review.md及現有實作；只修改Phase02範圍。前述Phase01未提交/HEAD=b9140dd是歷史，已被本紀錄更新。

環境：D:\minecraftmod、PowerShell、既有Java25/Minecraft26.3與依賴。每次Gradle只在程序設定GRADLE_USER_HOME=D:\minecraftmod\gradle。未修改工具鏈/Wrapper/build.gradle、metadata或審查報告。

| 實際命令/操作 | 結果 |
| --- | --- |
| git status/log/show d1513ed、讀取指示/文件/程式 | 開始乾淨，提交及審查存在 |
| javap -p / -c -p merged26.3的Particle、SingleQuadParticle、DustParticleBase、ClientLevel與SoundEvents | 公開壽命/縮放/音效簽章及OPAQUE層確認，見API紀錄 |
| 首次編輯使用系統python alias | exit1，WindowsApps/python.exe不可執行；沒有寫入任何檔案；改用apply_patch成功 |
| 修改前既有clean build --no-daemon（沙箱） | exit1，無法連Gradle daemon PID23524，未編譯；同既有環境限制 |
| 動畫修改後clean build --no-daemon（核准放寬沙箱） | exit0，11s，5 tasks executed |
| 最終含FireworkTimeline的clean build --no-daemon（核准放寬沙箱） | exit0，10s，5 tasks executed；Gradle test NO-SOURCE |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit0，PASS：五階段時間長度、形成單調與完整hold邊界、縮小單調/累乘與結束值。獨立source-launcher測試，非Gradle test |
| git diff --check | exit0，僅LF/CRLF提示；最終收尾再檢查 |
| Get-FileHash build/libs/drawfirework-1.0.0.jar | SHA256 62A618EB2659BF493A5F00974ECFAA5CB630C8E4B6AADF5B49243AE263E28A2A |

JAR為開發驗收產物，非V1.0發佈版；未啟動本輪遊戲。Compiled PASS；Unit-tested（純時間/縮放）PASS，粒子引擎及清理未自動測試；Runtime-tested/Visually verified待使用者人工測試；Multiplayer-tested未執行且超出範圍。既有Gradle棄用警告保留；無FPS、frame time、GC或實際配置量測。

### Phase02 人工驗收

1. PowerShell進D:\minecraftmod，設定 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`，執行 `.\gradlew.bat runClient --no-daemon`。一般單人測試世界，粒子設全部，前方開闊、視線水平略朝天空，愛心中心在視線前12格，升空起點在中心下6格；測試場地避免地面/牆擋住起點。
2. `/drawfirework heart`：A約1秒金色升空尾跡/發射聲 → B約0.4秒中心展開彩心/一次爆炸聲 → C約1.4秒完整彩心 → D約1秒由原位置柔和向外與下落 → E約0.7秒軟粒子縮小後全清除。合計約4.5秒；遊戲lag時以tick為準。請錄影檢查hold有無大片缺點、散射有無瞬移、消失是否柔和、聲音是否各一次。
3. `/drawfirework heart dust`應與預設相同。`/drawfirework heart image`仍是舊8+28 tick平面技術對照（不帶A/D/E），不得據此驗收Dust生命週期。
4. 分別在A/B/C/D/E期間執行 `/drawfirework clear`，應立即移除所有本效果粒子，沒有後續爆炸聲/重生；已播放的短音效不會被clear倒回或截斷。重複執行heart/dust/image應替換前一效果，不累積；命令密集觸發時也觀察聲音/效能。
5. 分別在hold及dispersion期間退出重入世界、切換維度或另一世界；不得帶入舊圖或例外。轉動視角：B/C隨相機朝向，D/E保持釋放後世界運動，不再跳回圖案。測日/夜、第三人稱與後退至24/48格，記錄輪廓、顏色、完整hold、移動與遮擋。
6. 觀察frame time圖與連續觸發時是否明顯卡頓，提供實際設定/硬體/錄影與錯誤log；沒有量測前效能維持未驗證。確認正常退出。回報五階段順序、缺點、跳動、尾跡、聲音、淡出、clear與世界清理各項結果。

Phase: 02；Status: PARTIAL；Git收尾見PROGRESS；Recommended next phase: 先完成上述人工驗收與獨立審查，不進Phase03。以下保留歷史紀錄。

# Phase 00 驗證紀錄

## 2026-10-23 — Phase 01 收尾：使用者遊戲內驗證（最新）

本次收尾建置：在D:\minecraftmod設定程序環境 `GRADLE_USER_HOME=D:\minecraftmod\gradle`，放寬沙箱執行 `.\gradlew.bat clean build --no-daemon`，exit0，BUILD SUCCESSFUL in 14s，5 actionable tasks: 5 executed。test NO-SOURCE；既有Gradle棄用警告仍在。沒有runClient或新的遊戲測試。
本輪開始/收尾比對Java SHA256應為1266F7A782B87C99BC68B221F1BE756AE09A72A9DB29CE6A289A8E55B9F0E32B、metadata為F3ABDD34B06634EF8EC6A62ED7839672B368A7C2C109454BB7246778F2BB2A1B，用於確認本輪未變動可用原型。Git準備範圍：client/HeartPrototype.java、fabric.mod.json、docs五份；建議訊息 `feat: add Phase 01 heart prototypes and select dust rendering`。只準備/暫存，不建立commit或push。

收尾核對：上述兩個SHA256實際一致。首次git add因沙箱index.lock權限失敗，放寬沙箱後僅暫存上述七檔成功；`git diff --cached --check` exit0，`git diff --name-only`無輸出，暫存範圍無快取/世界/Phase02實作。HEAD仍b9140dd，未建立新commit。

證據來源：使用者本次明確回報；Codex沒有自行觀察畫面，未收到截圖、逐tick紀錄、距離/粒子設定矩陣或FPS。下方「尚未觸發兩原型」是先前歷史狀態，由本條更新。

| 項目 | 結果與範圍 |
| --- | --- |
| `/drawfirework heart dust` | PASS（使用者實測）：正常顯示，視覺非常好，非常符合原先煙火構想；正式接受為主要方向 |
| `/drawfirework heart image` | 顯示功能PASS；產品風格不採用：像貼圖、顏色/邊界太鮮明、平面感強，與Minecraft煙火不協調 |
| `/drawfirework clear` | 未測；不得標PASS |
| 形成/hold詳細時間、距離/視角、粒子選項、世界退出/重入清理 | 未逐項回報；不從「效果好」推定所有情境通過 |
| 偶發啟動崩潰0xC0000005 | 使用者回報重啟後可正常進世界測試；原因未確認，沒有證據與Dust相關 |

本次只更新文件；保留上一輪Java/metadata。靜態審查：63粒子、1效果、99格遍歷、最多63 Vector3f/tick；詳見DECISIONS.md。這些是工作量推算，並非效能profiling。沒有新增或執行Phase02功能。

驗證層級：Compiled見本次clean build收尾紀錄；Unit-tested未執行（NO-SOURCE）；Runtime-tested兩種原型觸發PASS（使用者），clear/清理仍未測；Visually verified Dust PASS（使用者，產品方向已接受），Image顯示可用但風格被否決；Multiplayer-tested未執行、超出範圍。

剩餘最小人工清單：單人開闊天空先觸發 `heart dust`，效果顯示期間立即執行 `/drawfirework clear`，確認當下停止且後續無殘點；重複一次；另測效果期間退出/重進世界。若要精確確認28 tick hold完整率，需錄影或逐tick觀察，尚無此證據。Phase01整體維持PARTIAL，Dust視覺PoC與正式選型已通過。

## 2026-10-23 — Phase 01（目前結果）

環境：D:\minecraftmod；PowerShell、Microsoft OpenJDK25.0.3+9-LTS、Minecraft26.3、Loader0.19.5、Fabric API0.163.0+26.3、Loom1.17.16、Wrapper9.6.1。Git開始乾淨，HEAD=b9140dd。所有Gradle命令先設定本程序 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`；沒有全域變更。

| 實際操作 | 結果 |
| --- | --- |
| 讀AGENTS、五份docs、Git狀態/歷史、src與build設定、列docs檔 | 無review檔；不重建專案；只授權Phase01 |
| java -version、javap / javap -c -p 本機merged與Fabric模組 | Java25.0.3；詳細簽章與限制見API_COMPATIBILITY.md |
| clean build --no-daemon（沙箱內） | exit1，Could not connect to Gradle daemon，PID17844；未編譯 |
| 同命令（放寬沙箱）第一次 | exit1，compileJava錯誤：舊command builder、camera accessor、PoseStack rotation名稱；依本機簽章修正 |
| 同命令（放寬沙箱）修正後 | exit0；BUILD SUCCESSFUL in 12s；5 actionable tasks: 5 executed；test NO-SOURCE |
| jar tf與Get-FileHash build/libs/drawfirework-1.0.0.jar | 包含DrawFirework.class、client/HeartPrototype.class、fabric.mod.json；SHA256=20E8A1E2011B612EBE12F789ECDE1826227ADA9F712CAE34598FE9A99CEC06DC |
| git diff --check | 當時exit0，只有LF轉CRLF提示；最終文件再次檢查見本次工具紀錄 |
| runClient --no-daemon（放寬沙箱） | 載入26.3、drawfirework1.0.0、API0.163.0+26.3；10:44:22共用初始化、10:44:23 SDL3.4.14/OpenGL/Intel UHD與視窗建立；後續log有整合伺服器及世界存檔 |
| 結束runClient smoke | Ctrl+C中止，啟動程序exit1；不是正常退出驗收或建置失敗 |

run/logs/latest.log為未追蹤的可覆寫運行紀錄，重要觀察保存在此。此次有Windows Perflib/OSHI系統資訊警告及開發帳號Realms/401錯誤；後續仍有資源載入與10:44:58起世界渲染、10:45:04/10:45:41單人世界存檔訊息。未直接觀察畫面，不從log推論愛心指令曾被觸發或顏色正確。沒有聲稱正常退出通過。

### 人工A/B操作及回報清單（仍待執行）

1. 在D:\minecraftmod執行 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`，再執行 `.\gradlew.bat runClient --no-daemon`。或使用本次JAR與既有相同版本Fabric環境測試。
2. 進入一般單人測試世界，選開闊空間，視線略朝天空，前方12格內無障礙，粒子設「全部」。不用開作弊；這是本機client指令。
3. `/drawfirework heart image`（或 `/drawfirework heart`）：預期彩色愛心在前12格由中心8 tick展開，28 tick完整停留，36 tick消失。重複觸發會替換；沒有火箭、聲音、解體或淡出，這些屬Phase02。
4. `/drawfirework heart dust`：相同遮罩、位置、尺寸和形成目標，但原版粒子壽命/顏色保留；觀察是否變暗、模糊、提前缺點。沒有補生粒子掩蓋差異。
5. 分別於日/夜、正面/側移/轉動視角/第三人稱、約12/24/48格觀察。要改觀察距離，先觸發再後退；中心不跟著玩家平移。回報輪廓、上下/左右方向、色帶、完整hold、遮擋與可讀距離。若看不見，回報指令回饋、log例外、粒子設定及圖形後端。
6. 重複觸發、`/drawfirework clear`、效果期間退出世界/重新進入，確認沒有舊圖或粒子殘留；正常退出遊戲。視覺截圖/觀察及效能量測另行記錄，不以建置通過取代。

| 驗證層級 | 目前狀態 |
| --- | --- |
| Compiled | PASS，clean build exit0 |
| Unit-tested | 未執行，NO-SOURCE；固定原型沒有新增資料演算法 |
| Runtime-tested | PARTIAL：客戶端/世界載入有log證據；兩個指令及清理尚未驗證 |
| Visually verified | 未執行，兩方案僅完成可編譯原型及實作層比較 |
| Multiplayer-tested | 未執行，超出授權且指令限制單人 |

Phase: 01；Status: PARTIAL；Git commit: 未建立（沿用不自動commit/push設定）；Recommended next phase: 先補Phase01人工驗收，未開始Phase02。

Phase00.5 runtime補充：使用者本次回報精確26.3啟動、模組載入、主選單/單人世界、中文介面正常無再現閃退、原版有色煙火正常。記為使用者實測PASS，沒有補造執行時間、log、截圖或FPS。本機git log確認b9140dd改名提交存在。

日期：2026-10-21（Asia/Taipei）；Windows 11 amd64；PowerShell；工作目錄 Get-Location 實測 D:\minecraftmod。

## 第一段唯讀查核

讀取 AGENTS.md 全文及 build.gradle/settings.gradle/gradle.properties/Wrapper properties/bat、src 全部兩個檔案及 .gitignore；未執行 Wrapper 或改檔。D:\AGENTS.md 未找到，src 下未找到其他 AGENTS.md。根目錄必要檔案存在，docs 不存在。

`git status --short --branch` 退出碼1，fatal: not a git repository；未初始化 Git。既有 build/、.gradle/、gradle/ 快取、run/ 保留。初始成品 myfirstmod-1.0.0.jar 為1965 bytes、2026-10-20時間戳，並非本次成功證據。

src 只有 MyFirstMod.java 與 fabric.mod.json。main 入口只寫日誌，environment=*，無 client 專用 import、Mixin 或分離 client source set。沒有單元測試。metadata 的 assets/myfirstmod/icon.png 不存在，保留為已知問題。命名仍為 myfirstmod/My First Mod，不等於規格 drawfirework。

clean 靜態查核：兩個建置腳本未定義自訂 clean/Delete、外部 apply 或多專案；未見刪除遊戲資料指令。聊天已先報告以上查核結果，才開始第二段。

## 實際命令及結果

| 命令 | 退出碼 | 關鍵輸出/解讀 |
| --- | --- | --- |
| java -version | 0 | Microsoft OpenJDK 25.0.3+9-LTS |
| .\gradlew.bat --version（原始環境） | 1 | FileNotFoundException，C:\Users\user\.gradle\wrapper\dists\gradle-9.6.1-bin\4ticwg1pgcbps2hj28r8so764\gradle-9.6.1-bin.zip.lck，存取被拒 |
| .\gradlew.bat --version（本次程序設定下述快取） | 0 | Gradle 9.6.1；Launcher/Daemon Java25.0.3；build time 2026-06-26 14:25:50 UTC |
| .\gradlew.bat build（受限執行） | 1 | Could not connect to the Gradle daemon；PID23664；log gradle/daemon/9.6.1/daemon-23664.out.log；尚未進入編譯 |
| .\gradlew.bat build（放寬限制） | 0 | Fabric Loom 1.17.16；BUILD SUCCESSFUL in 1m 7s；4 actionable tasks: 4 up-to-date |
| .\gradlew.bat clean build（放寬限制） | 0 | BUILD SUCCESSFUL in 17s；5 actionable tasks: 5 executed；clean/compileJava/processResources/processIncludeJars/jar 實際執行 |

後四項 Gradle 命令在各次 PowerShell 程序先執行：

```powershell
$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'
```

這是既有專案快取位置；沒有修改全域環境或 Wrapper。每次直接讀取命令 $LASTEXITCODE，非僅使用包含後續輸出之 shell 狀態。放寬限制沒有遭自動審核拒絕。無程式/設定修復；不以不同版本繞過錯誤。

clean build 工作：compileTestJava/processTestResources/test 為 NO-SOURCE，不能視為單元測試通過。警告：使用 Gradle 棄用功能，未來不相容 Gradle 10；本次未為此升版或擴大修復。詳細 problems report 在 build/reports/problems/problems-report.html。

本機 merged Minecraft JAR 的 version.json：id/name=26.3、stable=true、java_version=25；javap 簽章見 API_COMPATIBILITY.md。既有另一份全域 Loom manifest 停留26.2，不能取代實際合併JAR及本次建置證據。

## 產物及基準

成品：D:\minecraftmod\build\libs\myfirstmod-1.0.0.jar（1965 bytes）。這是現有範例模組環境成品，不是已完成煙火產品的發布版。

建置後用唯讀 ZIP 檢查確認含 MyFirstMod.class、META-INF/MANIFEST.MF 與 fabric.mod.json。中繼資料變數已展開：version=1.0.0、fabricloader>=0.19.5、minecraft=~26.3、java>=25；宣告的圖示仍缺失。此 metadata 使用 ~26.3，而本次建置依賴精確為26.3；未擴大修改相容性宣告。

SHA256：

| 檔案 | SHA256 |
| --- | --- |
| build/libs/myfirstmod-1.0.0.jar | 9C0E01EC159C9396A84D8C05994D2311D844FA9538A53A651C4A7F512620479B |
| build.gradle | 548E66209EA9BFAF4E43B5C01F3750ADA60E21667A509E92E1B36077CC18E42A |
| settings.gradle | BD088D6979747C456E6852CCED1649A5E7D4CDB5DA54F64EE55486B9F865C5F8 |
| gradle.properties | 5F0E97DFF56F372D9AD1655F9608FC4E02EF5D18C2F41D40F53A6184DCD1DF10 |
| gradlew.bat | D539676C48B596AFDA64C963EC8F7EE56C7B3FE7E3B81D1DBE2D1A1E3DD9E9F8 |
| gradle/wrapper/gradle-wrapper.jar | 497C8C2A7E5031F6AA847F88104AA80A93532EC32EE17BDB8D1D2F67A194A9C7 |
| gradle/wrapper/gradle-wrapper.properties | C94D41B634A5E0FF8A2F812023697B1337EEBAC18E9962610356A1793F72896D |
| src/main/resources/fabric.mod.json | 4DDE238503382A9C72C7895C279D7AD7344335FC0B1C9535556B4BFCB20F7C60 |
| src/main/java/com/example/myfirstmod/MyFirstMod.java | C059688E38AB4FFC4E69DD1DB9FE1A09E9AA9475E76C303FA52167437C7E4A01 |

本次未編輯以上來源/設定；無 Git，雜湊為本次完成基準，不能替代先前歷史差異。

## 驗證層級

| 層級 | 狀態 | 證據/限制 |
| --- | --- | --- |
| Compiled | 通過 | clean build exit0，實際 compileJava |
| Unit-tested | 未執行 | test NO-SOURCE |
| Runtime-tested | 未執行 | 未啟動遊戲或整合/專用伺服器 |
| Visually verified | 未執行 | 無 Phase01 原型或遊戲觀察 |
| Multiplayer-tested | 未執行 | 本次不屬多人階段 |

Phase00 Status: PASS（環境建置門檻）；不代表產品功能或遊戲運行通過。未做空快取重建、完整依賴原始碼審計、FPS/配置量測。後續要驗證既有範例啟動，可在同一工具鏈執行既有 Wrapper runClient，觀察標題/模組載入及 latest.log 的初始化訊息並記錄錯誤；本次未執行，且尚無煙火可供視覺驗收。Phase01 比較需另行授權。

## 2026-10-23 — Phase 00.5 execution results (supersedes current-status claims above)

Working directory D:\minecraftmod. Read-only audit: no Git repository, source/config SHA256 matches Phase 00 recorded values. Git identity was already configured. Java reports Microsoft OpenJDK 25.0.3+9-LTS. Existing build scripts have no custom clean actions. Dependencies unchanged: Minecraft 26.3, Loader 0.19.5, API 0.163.0+26.3, Loom 1.17.16, Wrapper 9.6.1, Java target 25.

All Gradle invocations used process-scoped GRADLE_USER_HOME=D:\minecraftmod\gradle:

| Command / environment | Exit | Result |
| --- | --- | --- |
| .\gradlew.bat clean build (before rename) | 1 | Could not connect to Gradle daemon, PID 21096; no compilation started |
| JAVA_OPTS=-Xmx2G -Dfile.encoding=UTF-8 -Duser.country=TW -Duser.language=zh -Duser.variant; .\gradlew.bat --no-daemon '-Dorg.gradle.jvmargs=' clean build | 1 | Still forks single-use daemon; connection failed, PID 9108 |
| JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.country=TW -Duser.language=zh -Duser.variant; same command plus '-Dorg.gradle.internal.instrumentation.agent=false' | 1 | Still forks; connection failed, PID 23116 |
| .\gradlew.bat clean build (after rename; no JAVA_OPTS override) | 1 | Same infrastructure failure, PID 7724; no compilation started |
| git init | 0 | Created local repository; no remote configured |
| git add (explicit 16 project files) | 0 | Original pre-rename baseline staged, including improved .gitignore |
| git commit -m "chore: baseline existing Phase 00 Fabric project" | 128 | Unable to create D:/minecraftmod/.git/index.lock: Permission denied |

Daemon evidence remains in gradle/daemon/9.6.1/daemon-{21096,9108,23116,7724}.out.log. Logs show listening on localhost but the launcher cannot connect. Earlier Phase 00 had the same restricted-execution symptom; current cause is suspected execution-environment restriction, not proven dependency/source failure. No escalation or permission bypass attempted. Stop retrying this environment issue; next diagnostic is the standard command in an ordinary user PowerShell.

Static checks: fabric.mod.json parses; entrypoint resolves to com.drawfirework.DrawFirework; source has no remaining old identity; no actual assets existed. git check-ignore confirms caches, wrapper/dists, build, run and .gradle excluded, while both Wrapper files are tracked. git diff --cached --check flags only existing Markdown hard-break trailing spaces in AGENTS.md; left unchanged.

Compiled: BLOCKED (current rename not compiled). Unit-tested: not run/no tests. Runtime-tested, Visually verified, Multiplayer-tested: not run. No renamed JAR validated. No rename commit attempted because clean build failed. Baseline index deliberately retained; working tree contains unstaged rename/docs and an untracked new initializer. Do not run git add -A before completing the baseline commit.

## 2026-10-23 — Phase 00.5 blocker resolution (user-executed evidence)

This entry supersedes the earlier current BLOCKED build status; failed attempts above remain historical evidence.

- Executor: user, in ordinary PowerShell at D:\minecraftmod.
- Environment: existing GRADLE_USER_HOME=D:\minecraftmod\gradle; no toolchain change reported.
- Command: `.\gradlew.bat clean build --no-daemon`.
- User-reported result: BUILD SUCCESSFUL; all 5 tasks successful. Numeric exit code, individual task names, duration and a full terminal transcript were not supplied in this turn and are not independently asserted here.
- Baseline commit: 4c7f4fb, created by the user. Codex verified read-only using `git log -1 --oneline` and `git show -s --format='%h %s' 4c7f4fb`; both resolve to "4c7f4fb chore: baseline existing Phase 00 Fabric project".
- Git state observed: branch master; baseline exists, rename and earlier documentation changes remain uncommitted. No Git commit/add, build or source/toolchain modification executed in this documentation-only update.

| Verification level | Current status | Evidence / limits |
| --- | --- | --- |
| Compiled / Phase 00.5 build gate | PASS (user-executed) | User reports successful clean build with 5 successful tasks; not rerun by Codex |
| Unit-tested | Not performed | No unit-test result supplied; successful Gradle build alone does not establish unit coverage |
| Runtime-tested | Not performed | No client/world launch result supplied |
| Visually verified | Not performed | No visual test supplied |
| Multiplayer-tested | Not performed | Outside current scope |

No renamed JAR content/hash verification added. Phase 00.5 build verification is PASS; overall phase remains PARTIAL pending the separate rename commit, which is explicitly prohibited in this turn. Phase 01 remains unauthorized.

Phase02 Git收尾：git add（明確8檔）exit128，.git/index.lock Permission denied；未暫存或commit，保留變更由使用者提交。最終git diff --check通過，JAR包含HeartPrototype與FireworkTimeline，無測試類別/快取/世界資料。

Finalization Git結果：11檔git add因.git/index.lock Permission denied失敗，依指示停止Git寫入；未commit/push，暫存區空且全部變更保留。精確PowerShell恢復命令與11檔清單見PROGRESS的Finalization Git收尾。
