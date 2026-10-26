# Phase 01 獨立技術審查報告

審查日期：2026-10-23（Asia/Taipei）  
審查員：Antigravity（獨立技術審查）  
審查目標：Draw & Hide Firework 專案 Phase 01 視覺概念驗證成果與相關文件紀錄  
受審 Commit：`d1513ed` (feat: phase 01 dust firework visual prototype)  

---

## 一、核心查核項目詳細分析

### 1. Dust 渲染架構是否適合 Phase 02 五階段動畫
* **形成（Formation, 8t）與停留（Hold, 28t）**：
  * 當前 `HeartPrototype` 使用 `positionDust()` 依每 tick 更新 `spread`（形成階段）及依相機方向旋轉，並藉由 `setParticleSpeed(0, 0, 0)` 硬性固定位置，在使用者實測中表現極佳、辨識度高且具自然煙火粒子質感。架構在 B、C 階段完全適用。
* **散射（Dispersion, 20t）與淡出（Fade Out, 14t）之擴充風險**：
  * **位置鎖定必須解除**：當前架構每 tick 都會將存活粒子強制拉回預設圖案位置。進入 Phase 02 的 Stage D（Artistic Dispersion）時，狀態機必須停止強制 `setPos` 與速度歸零，改為賦予粒子初速度（如向外擴散的徑向向量）、重力與阻力模擬，否則粒子無法自然解體散射。
  * **原版 Dust 粒子壽命（Lifetime）限制**：Minecraft 原版 `DustParticle` 的存活期約為 8~40 ticks（加成 1.2），目前 36 ticks 剛好在極限邊緣；然而 Phase 02 完整五階段（Launch + 8t + 28t + 20t + 14t）總時長達 70+ ticks。若直接依賴單次生成原版粒子，多數粒子將在 Hold 階段未結束或剛進入 Dispersion 時提前死亡（`isAlive() == false`）。Phase 02 必須設計自訂粒子壽命控制或階段性接替機制。

### 2. 是否存在重複生成粒子、無限制累積或生命週期管理問題
* **重複觸發保護**：`HeartPrototype.start()` 開頭明確呼叫 `clear()`，會逐一對現存粒子呼叫 `particle.remove()` 並清空清單，重複執行指令時舊效果會被即時移除並替換，無疊加洩漏問題。
* **生命週期結束清理**：達到 36 ticks 自動觸發 `clear()`；世界切換（`END_CLIENT_TICK` 偵測到 `owner != client.level`）亦會觸發清理。
* **併發架構限制**：目前採用單一實例全域狀態（單一 `owner`、`center`、`age`、`dust` 清單），僅能支援同時一個煙火。這符合 Phase 01 規格，但在 Phase 02 必須重構為物件導向的多煙火控制器（`FireworkAnimationController`），以便管理多發煙火之獨立生命週期。

### 3. 客戶端與伺服器職責劃分合理性
* `HeartPrototype` 類別嚴格標記 `@Environment(EnvType.CLIENT)`，實作 `ClientModInitializer`。
* `fabric.mod.json` 中 `HeartPrototype` 僅宣告於 `"entrypoints": { "client": [...] }`。
* 共用入口 `DrawFirework` 保持乾淨，無任何渲染或客戶端類別之引用。
* 指令透過 Fabric Client Command 註冊，並包含 `client.hasSingleplayerServer()` 單人世界限制。職責劃分乾淨，無污染伺服器環境之虞。

### 4. Minecraft 26.3 API 使用風險與執行穩定性
* **使用的 26.3 API**：
  * `ClientCommandRegistrationCallback`、`DustParticleOptions`、`ParticleEngine.createParticle`、`LevelRenderEvents.COLLECT_SUBMITS` 及 `OrderedSubmitNodeCollector.submitCustomGeometry` 均符合 26.3 原生命名與二進位簽章。
* **相機旋轉防禦**：`positionDust()` 直接調用 `Minecraft.getInstance().gameRenderer.mainCamera().rotation()`，在世界尚未完全就緒或切換瞬間可能偶發 NPE，後續階段建議加入防禦性空值檢查。
* **偶發 0xC0000005 崩潰報告**：測試紀錄中提及的使用者啟動偶發崩潰，經比對日誌與執行環境，屬於 JVM / SDL3 視窗初始化層級之圖形驅動相容問題，與 Dust 粒子邏輯無直接關聯。

### 5. Image Renderer 保留作為對照的影響
* **技術開銷**：`COLLECT_SUBMITS` 監聽器僅在 `image == true` 時執行，若為 Dust 模式則立即 return，CPU/GPU 額外負擔幾乎為 0。
* **指令混淆問題**：目前預設指令 `/drawfirework heart` 仍映射至已否決的 `image` 模式，測試 Dust 必須輸入 `/drawfirework heart dust`。這易造成測試與認知偏差，應在進入 Phase 02 時將預設指令導向 Dust，並考慮將 Image 相關代碼解耦或歸檔。

### 6. 文件、測試紀錄與 Git Commit 一致性
* **Git Commit 現況**：本地 Git 倉庫乾淨（working tree clean），且包含 Commit `d1513ed`（`feat: phase 01 dust firework visual prototype`）。
* **紀錄細微落差**：`docs/PROGRESS.md` 與 `docs/TEST_RESULTS.md` 的最新文字描述仍記載為「暫存中、未建立 commit、HEAD=b9140dd」，與實際已完成 Commit `d1513ed` 的狀態有些微落差，應於下次文件推進時同步更新 HEAD 雜湊。
* **測試結論一致性**：各文件皆明確記錄 Dust 視覺 PASS、Image 風格否決、清理未完全實測，整體狀態評定為 PARTIAL，邏輯一致。

---

## 二、問題分類

### 🔴 重大問題（Critical）
* **無**（Phase 01 之建置、核心視覺與原型目標皆已圓滿達成，無阻斷性重大架構缺陷）。

### 🟡 一般問題（Minor / 需於後續處理）
1. **預設指令未切換至 Dust（建議 Phase 02 前置微調）**：
   * 當前 `/drawfirework heart` 仍預設開啟 Image 渲染，與決策結論不符，應調整為預設執行 Dust 效果。
2. **Phase 02 動畫狀態機與散射解體架構需求（留待 Phase 02 實作）**：
   * `positionDust()` 目前的絕對位置約束需改為階段式驅動（Stage B/C 鎖定位置，Stage D 解除鎖定並賦予散射向量）。
   * 原版 Dust 隨機壽命短於 Phase 02 總時長（~70 ticks），需設計自訂粒子壽命或分段遞補機制。
3. **未完成之人工邊界驗收（留待 Phase 02 合併實測）**：
   * 手動 `/drawfirework clear`、動畫進行中退出重入世界之殘留檢查尚未有人工觀察報告。
4. **Git 狀態描述與最新 HEAD 雜湊不同步（留待後續文件同步）**：
   * 文件內紀錄停留在暫存狀態描述，需將目前已提交的 `d1513ed` 登記為最新 HEAD。

### 🟢 已通過項目（Verified Normal）
* **視覺概念驗證（PoC）**：Dust 粒子方案呈現之愛心輪廓清晰、顏色正確、具備自然煙火質感，經使用者實測確認為最佳方案。
* **建置驗證**：Minecraft 26.3、Java 25、Fabric Loader 0.19.5 工具鏈下 `clean build` 成功。
* **架構隔離**：Client 與 Server 職責嚴格隔離，無客戶端渲染類別洩漏至伺服器端。
* **重複觸發安全**：具備舊效果即時 `remove()` 與重置機制，無粒子無限制堆疊洩漏。

---

## 三、審查結論

# 審查結論：PASS（視覺 PoC 階段驗收通過）

**判定理由**：
Phase 01 的核心目標是「建立固定彩色愛心原型、對照 dust 與自訂渲染之形成/停留表現，並做出正式渲染方案選擇」。
Codex 成功建立兩套對照原型，經使用者遊戲內實測，正式拍板採納 Dust 為主要方向，且專案在不破壞相容性與無伺服器污染的前提下通過 `clean build`。雖然原版粒子壽命控制與手動清理仍留有待測項目，但已具備進入 Phase 02（A–E 完整動畫生命週期）所需的視覺基礎與架構雛形。

**後續建議**：
請使用者審閱本報告後，可正式授權 Codex 開始 **Phase 02（五階段藝術煙火動畫與生命週期）** 之設計與實作。
