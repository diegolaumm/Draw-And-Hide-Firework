# Phase 03 獨立技術審查報告

審查日期：2026-10-23（Asia/Taipei）  
審查員：Antigravity（獨立技術審查員）  
審查目標：Draw & Hide Firework 專案 Phase 03 — Canvas Data Model & Downsampling 成果審查  
受審 Commit：`9c534b2` (feat: phase 03 canvas data and downsampling)  

---

## 一、六大核心領域詳細審查

### 審查一：CanvasData 不可變性與資料安全
1. **防禦性複製與內部封裝**：
   * `CanvasData` 為 `final class`（非 record），內部維護私有 `final int[] pixels`。
   * 建構子中強制執行 `this.pixels = pixels.clone()`；讀取陣列方法 `pixels()` 亦回傳 `pixels.clone()`；單點像素讀取 `pixel(x, y)` 則進行邊界檢查後回傳 primitive `int`。外部完全無法藉由物件參照變更內部像素狀態，不可變性無懈可擊。
2. **尺寸與長度驗證**：
   * `checkedArea(width, height)` 嚴格限定寬高於 `[1, 256]` 範圍內，超出即拋出 `IllegalArgumentException`。
   * 面積計算採用 `(long) width * height` 並以 `Math.toIntExact` 轉回整數，徹底防止整數溢位攻擊。
   * 建構子檢查 `pixels.length != area` 與空指標，確保陣列長度嚴格等於寬高乘積。
3. **解析度與色彩格式**：
   * 正確支援最大 `256 × 256`（65,536 像素，記憶體僅 256 KB）。
   * 採用 Straight ARGB（非預乘），alpha 為 0 時仍完整保留原始 RGB 位元；alpha > 0 之原色與透明度皆完整保留。

### 審查二：Codec 與 JSON 序列化
1. **序列化與雙向往返**：
   * `CanvasSerialization.toJson()` 與 `fromJson()` 可完全無失真還原 `CanvasData`。
   * Mojang DFU `CanvasSerialization.CODEC` 經 `JsonOps.INSTANCE` 序列化與反序列化測試，亦完全通過雙向往返驗證。
2. **版本控制與邊界防禦**：
   * 明確標示 `VERSION = 1` 與 `FORMAT = "argb32-hex"`。遇到未知版本或格式直接拒絕。
   * `fromJson()` 設有 `MAX_JSON_BYTES = 600,000` 上限檢查與 ASCII-only 驗證，並在解析前立即阻擋超大輸入，防止記憶體炸彈。
   * 採用 Gson `Strictness.STRICT` 串流解析，嚴格禁止重複欄位、未知欄位、型別錯誤、缺少欄位及 JSON 結尾額外字元。
3. **Minecraft 26.3 Codec 相容性**：
   * 自訂 `EXACT_INT` 解決了原生 `Codec.INT` 截斷小數或溢位整數的問題，嚴格要求精確 32-bit 整數。
   * Codec 架構完全符合 26.3 DFU 標準，可無縫用於未來的物品 Data Component 註冊。

### 審查三：CanvasDownsampler 確定性與降採樣品質
1. **演算法確定性**：
   * 採用自適應空間二分法（Occupancy-adaptive spatial subdivision），依據可見像素數量、長軸中點進行嚴格確定性切分，且平手（tie-breaking）時採用確定性整數雜湊排序（`tieRank`），無任何偽隨機（Random）或依賴執行緒之不確定因素。相同畫布輸入永遠產出完全相同的採樣點陣列。
2. **長寬比與座標映射**：
   * 統一採用最大軸作為比例尺（`scale = Math.max(width, height)`），中心點置中且 `+Y` 向上，完全保留非正方形圖像（如 8×2、1×256）之幾何長寬比例，無變形拉伸。
3. **色彩代表性與無混濁保證**：
   * 採樣點直接選取區域內真實存在的可見像素作為代表點（`representative`），徹底避免傳統區域平均（Area Average）導致紅藍相間變成混濁紫色、或細線透明度被稀釋至消失的問題。
4. **極端輸入與預算退化**：
   * 全透明圖形或預算為 0 時安全回傳空列表；孤立單點像素（即使透明度為 1/255）亦能被自適應選出；輸出點數恆等於 `min(可見像素數, budget)`，嚴格遵守預算限制。
5. **複雜度與效能**：
   * 最大深度受到 $\lceil\log_2 W\rceil + \lceil\log_2 H\rceil \le 16$ 限制，時間複雜度為 $O(N \cdot D + B \log B)$，實測 256×256 全彩圖像降採樣至 266 點平均耗時僅 2.54 ms，配置僅約 41 KB，極為高效。

### 審查四：粒子預算嚴格受控
1. **核心粒子預算與採樣點數之換算**：
   * 依 Phase 02 選定之 Bloom 130% 規範，每個採樣點由 3 顆 Dust 構成。
   * `ParticleBudget.BLOOM` 明確定義核心粒子上限為 800 顆，採樣點上限為 `800 / 3 = 266` 點。
   * 實際最大核心粒子產生量為 `266 × 3 = 798` 顆（低於 800 顆預算上限）。
2. **預算獨立性與防呆**：
   * `ParticleBudget` 將核心粒子（800）與裝飾尾跡粒子（200）明確分開，嚴禁挪用借調。
   * `coreCount(samples)` 當傳入大於 266 點時立即拋出例外，杜絕 off-by-one 與未受控外洩。

### 審查五：自動化測試與回歸驗證
1. **Gradle 測試真實執行**：
   * `build.gradle` 新增 `testImplementation 'junit:junit:4.13.2'`，且 `test { useJUnit() }` 成功生效。
   * 查驗 `build/test-results/test/TEST-com.drawfirework.canvas.CanvasTest.xml`：共 15 個測試用例，0 失敗、0 錯誤、0 略過，耗時 0.818s。
2. **測試覆蓋深度**：
   * 測試包含：不可變性防禦、尺寸邊界溢位、單像素極限、愛心圖形、256x256 密集彩圖、對角線與棋盤格、非正方形縮放、JSON/Codec 雙向往返、各類惡意畸形 JSON 拒絕、粒子獨立預算、60 組隨機矩形確定性不變量驗證、以及演算法策略對比。
3. **既有動畫回歸測試**：
   * 獨立重新執行 `Phase02TimelineCheck`、`Phase025DustStyleCheck` 與 `Phase026BloomCheck`，全部通過（Exit code 0），既有動畫與 Bloom 130% 管線完全未受影響。
4. **客觀紀錄**：
   * 文件誠實標註純邏輯測試通過不等於遊戲內畫質通過，未做不實之遊戲視覺宣稱。

### 審查六：架構邊界與 Phase 04 整合適配
1. **模組純度與無污染**：
   * `com.drawfirework.canvas` 套件僅依賴標準 Java、Gson 與 Mojang Codec，完全不依賴任何 Minecraft Client 渲染類別，亦無引入 Level、Particle 等遊戲環境類別。專用伺服器與客戶端皆可安全載入。
2. **動態接入能力**：
   * `CanvasDownsampler.Sample` 提供了 `x, y, argb, weight`，可直接與 Phase 02 的 `HeartPrototype` 進行對接。
   * Phase 04 僅需將固定 `MASK` 替換為 `CanvasDownsampler.sample(canvas, budget.sampleLimit())`，其餘 5 階段時序、Bloom 綻放與生命週期管線可完全復用。

---

## 二、問題分類

### 🔴 Critical（重大問題）
* **無**。

### 🟠 Major（主要問題）
* **無**。

### 🟡 Minor（次要問題 / 建議於後續處理）
1. **Phase 04 半透明像素之 Dust 映射規則需明確化**：
   * 原版 `DustParticle` 屬於 `OPAQUE` 不透明材質，不支援真實 Alpha 混合。當畫布存在半透明像素（如 Alpha 128）時，Phase 04 需明確制定應對策略（例如以 Alpha 閥值過濾、微調粒子縮放大小或粒子分佈密度），此屬 Phase 04 實作範圍，不阻礙 Phase 03 交付。
2. **低預算下高頻棋盤格之混疊（Aliasing）現象**：
   * 在極端高頻圖樣（如 32×32 棋盤格降採樣至 16 點）下，空間二分法保留了紅藍原色，但紅藍點數比例會出現輕微偏差。此為有限採樣下的理論取捨，已詳實記錄於文檔，後續可視遊戲內視覺反饋評估。
3. **Phase 06 草稿檔案讀取防禦**：
   * `CanvasSerialization.fromJson()` 已限制字串上限，但 Phase 06 實際讀取硬碟檔案時，應於串流層級（Stream）先限制讀取位元組數，防止讀入惡意巨型檔案消耗記憶體。

### 🟢 Verified（已確認正常）
* `CanvasData` 達成嚴格不可變性（Private final + 防禦性複製），溢位防護完整，支援 1..256 尺寸。
* `CanvasSerialization` 雙向 JSON / Codec 序列化往返精準，嚴格拒絕畸形與未知輸入。
* `CanvasDownsampler` 純邏輯確定性運作，保證幾何長寬比，輸出嚴格受控於預算上限。
* `ParticleBudget` 明確分離 800 核心粒子（266 點 × 3 粒 = 798）與 200 裝飾粒子，防呆機制健全。
* 15 個 Gradle JUnit 單元測試與 3 個既有動畫回歸測試真實執行並全數通過。
* 零客戶端渲染污染，架構清晰解耦，與 Minecraft 26.3 完全相容。

---

## 三、審查結論與下一步判定

# 審查結論：PASS（Phase 03 畫布模型與降採樣完全合格）

# ✅ 可以進入 Phase 04（資料驅動煙火、Data Component 與物品整合）

**進入 Phase 04 理由**：
Phase 03 嚴格遵照規格要求，完成不可變畫布資料模型、有界版本化 Codec、確定性空間降採樣器以及獨立粒子預算系統。自動化測試覆蓋充分且真實通過，既有 Phase 02 動畫回歸無損，架構設計優雅解耦，**已完全具備進入 Phase 04 開發之所有前提條件**。
