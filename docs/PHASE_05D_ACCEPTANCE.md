# Phase 05D — Particle Quality & Sampling Optimization 人工驗收指南

本文件提供 Phase 05D 粒子畫質檔位（Standard / Balanced / High）與採樣演算法（Existing vs Detail-Preserving）之人工操作指引與驗收標準。

---

## 一、交付成果概述

1. **三種獨立客戶端核心粒子畫質檔位（`ParticleQuality`）**：
   - **`Balanced`（正式預設）**：核心粒子預算 **1100 顆**，Bloom 130 嚴格對應最大 **366 個樣本**（$366 \times 3 = 1098 \le 1100$）。經使用者實機驗收確認為兼顧清晰度與筆觸飽滿感之最佳檔位，設為正式預設。
   - **`Standard`**：核心粒子預算 **800 顆**，Bloom 130（3 粒／樣本）嚴格對應最大 **266 個樣本**（$266 \times 3 = 798 \le 800$）。保留為輕量基準檔位。
   - **`High`**：核心粒子預算 **1400 顆**，Bloom 130 嚴格對應最大 **466 個樣本**（$466 \times 3 = 1398 \le 1400$）。保留為測試上限檔位。
2. **採樣演算法 A/B（`SamplingStrategy`）**：
   - **`Existing`（策略 A）**：現有基於佔用率之空間四叉剖分採樣，作為不變對照組。
   - **`Detail-Preserving`（策略 B，正式預設）**：外輪廓、封閉迴圈邊界、細線、端點與色彩邊界優先採樣，實心色塊內部平滑剖分填補。
     - **動態目標分配（非僵化契約）**：初始以 70% 輪廓、30% 內部為目標；當某類像素不足時，剩餘預算動態回補至另一類（例如純線稿無內部像素時，預算 100% 分配予線條；實心大色塊邊緣少時，剩餘預算回補予內部）；整數取整 `Math.round(budget * 0.70)` 具備正常離散偏差。
     - **幾何品質目標**：在預算充裕時完整保留邊界特徵；當圖案複雜度或線條總數超出預算時，以空間二分確保均勻覆蓋（此為有損採樣之客觀物理限制，單元測試已驗證極限邊界）。
3. **指令控制**：
   - `/drawfirework quality`（查詢目前畫質檔位）
   - `/drawfirework quality <standard|balanced|high|reset>`（切換或重設為 balanced 正式預設）
   - `/drawfirework sampling`（查詢目前採樣策略）
   - `/drawfirework sampling <existing|detail_preserving|reset>`（切換或重設為 detail_preserving 預設）
4. **預覽聯動與快取失效**：
   - `CanvasPreview` 於畫質或採樣策略改變時自動失效快取並即時重新生成 `DustPattern`。
   - 繪圖 GUI（按 G）之 Dust 預覽即時更新粒子點位，並於統計列顯示精確之粒子與樣本數（例如 `1098 粒子（366 樣本 × 3）`）。
   - 按 `Save` 儲存之 32×32 `CanvasData` 資料維持 100% 相同與安全驗證。
5. **嚴格不變量**：
   - 保持 32×32 CanvasData、C2S 封包安全驗證、76 ticks 五階段動畫、Bloom 130%、Fixed/Dynamic 世界朝向與 Phase 05C `fullbright_exact` 獨立光照。

---

## 二、人工驗收步驟

### 1. 啟動遊戲
在 PowerShell 終端機（路徑 `D:\minecraftmod`）執行：
```powershell
Set-Location D:\minecraftmod
$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'
.\gradlew.bat runClient
```
進入任一允許作弊指令的單人創造世界。

---

### 2. 指令功能驗證

1. **品質查詢與切換**：
   - 執行 `/drawfirework quality`，確認回傳預設：
     `Current particle quality: balanced (Balanced quality (1100 core particles, 366 samples))`
   - 執行 `/drawfirework quality standard`，確認回傳：
     `Particle quality set to: standard (Standard quality (800 core particles, 266 samples))`
   - 執行 `/drawfirework quality high`，確認回傳：
     `Particle quality set to: high (High quality (1400 core particles, 466 samples))`
   - 執行 `/drawfirework quality reset`，確認回傳恢復為正式預設：
     `Particle quality reset to balanced.`
2. **採樣策略切換**：
   - 執行 `/drawfirework sampling`，確認預設為 `detail_preserving`。
   - 執行 `/drawfirework sampling existing`，確認切換至舊版對照組。
   - 執行 `/drawfirework sampling detail_preserving`，確認切回細節優先。
   - 執行 `/drawfirework sampling reset`，確認恢復預設。

---

### 3. 繪圖與單一物品多次施放對照（Session 限制說明）

> [!IMPORTANT]
> **GUI Session 限制**：目前 256×256 手繪畫稿僅存在於當前 GUI Session 中（高解析度草稿持久化屬 Phase 06 範圍）。一旦關閉 GUI，未儲存的原稿不會跨 Session 保留。因此驗收應採以下「先存檔、後施放」流程：

1. 主手持有 `drawfirework:test_firework`（若無可使用 `/drawfirework_test_item` 取得），按 **G** 開啟繪圖 GUI。
2. 在 256×256 手繪板上繪製驗收圖案（如**圓形**、**數字 6**或**細線**）。
3. 點擊右側 **View** 切換至 **Dust** 預覽模式，確認下方統計列正確顯示粒子與樣本數。
4. 點擊 **Save**，確認畫布資料已正式寫入該煙火物品（伺服器回傳確認訊息）。
5. 按 Esc 關閉 GUI。**後續對照無須重新繪製，亦請勿期待重開 GUI 能恢復手繪稿**：
   - 直接手持同一個已儲存物品。
   - 在聊天框輸入 `/drawfirework quality standard`，右鍵發射，觀察夜空中的爆炸效果。
   - 等待 76 ticks 動畫完全結束後，輸入 `/drawfirework quality balanced`，再次右鍵發射同一物品，觀察同一 CanvasData 在提升至 366 樣本時的細節密度變化。
   - 輸入 `/drawfirework quality high`，再次右鍵發射同一物品，觀察 466 樣本上限下的表現。
   - 亦可切換 `/drawfirework sampling existing` 與 `/drawfirework sampling detail_preserving` 進行策略對照。

---

### 4. 關鍵圖案 A/B 視覺對照指標（品質目標）

在世界中固定視角、時間設為夜間（`/time set midnight`），依序對照以下圖案：

| 圖案 | Existing 策略觀感 | Detail-Preserving 策略目標 | Balanced (1100) vs Standard (800) 差異 |
| :--- | :--- | :--- | :--- |
| **圓形（實心與空心）** | 圓周邊界易出現齒狀凹陷或缺點 | 圓周輪廓採樣密度提高，邊緣較平滑連續（待實機驗收） | 實心內部粒子填補更均勻飽滿 |
| **數字 6** | 底部圓環內部易被周圍色塊侵入或斷線 | 頂部弧線與底部中空孔洞保留良好（待實機驗收） | 線條筆觸更厚實，遠距離辨識度提高 |
| **細對角線** | 斜線中間易被分割中點拉扯出現斷節 | 斜線連續性改善，減少中間斷節（可見像素 $\le$ 預算時完全保留） | 32 像素斜線忠實對齊，無多餘雜點 |
| **封閉幾何圖案** | 轉角或封閉邊界有凹角斷點 | 封閉邊緣採樣優先保留，減少凹角斷點 | 邊框完整，內部過渡平滑 |
| **實心色塊（大正方形）** | 邊界因等權重剖分略有鋸齒 | 外圍邊界方正平直，內部粒子均勻排布 | 內部粒子間距更緊密，色塊密度感提升 |
| **多色漸層／對比面孔** | 眼睛等小特徵易被大面積底色稀釋 | 眼睛／嘴巴等色彩邊緣被優先鎖定 | 特徵邊界較清晰，底色飽和 |

---

### 5. 效能與資源觀察（待人工驗收項目）

> [!NOTE]
> 以下效能表現為基於架構分析之理論估計，實際流暢度與幀時間請在您的硬體環境上實地體驗並回報：

1. **Standard（800 顆）**：
   - 觀察爆炸瞬間是否有任何微幅掉幀（Stutter）。
   - 品質目標：流暢無阻礙。
2. **Balanced（1100 顆）**：
   - 觀察爆炸瞬間生成之幀時間與動畫過程平滑度。
   - 品質目標：流暢無卡頓，視覺細節相較 800 應有可感知提升。
3. **High（1400 顆）**：
   - 觀察粒子引擎尖峰生成時之負載（尤其在低階內顯設備上）。
   - 檢視連續施放或多煙火時之系統穩定性。
4. **清理驗證**：
   - 煙火施放 76 ticks（約 3.8 秒）完全結束後，確認世界中無任何殘留效果點。
   - 執行 `/drawfirework clear`，確認效果立即清除。

---

## 三、使用者實機驗收紀錄與正式結案結論

Phase 05D 於 2026-10-24 獲得正式批准 **PASS / CLOSED**。
Codex 最終獨立唯讀複核確認：Critical 0 / Major 0 / Minor 0，前次 4 項 Minor 全數 RESOLVED。

1. **畫質檔位偏好**：
   - 觀察以「數字 6」為例：使用者確認 **Balanced（1100 顆核心粒子／366 樣本上限）** 視覺效果最佳，筆觸飽滿且孔洞清晰；相較之下，**High（1400 顆核心粒子／466 樣本上限）** 在數字 6 的表現中並未顯得更好看（線條密度過高反而略顯壅塞，無比例清晰度收益）。
   - **產品決策**：將 **Balanced** 設為正式 `ParticleQuality` 預設；`/drawfirework quality reset` 恢復 Balanced；保留 Standard 與 High 供使用者手動切換。不額外新增更高粒子檔位。
2. **採樣策略對照**：
   - 在實機遊戲視覺中，現有採樣（Existing）與細節優先採樣（Detail-Preserving）之差異在實體 Dust 粒子散布下**並不明顯**，Detail-Preserving 並非對所有圖案都一定優於 Existing。
   - **產品決策**：暫時保留 Detail-Preserving 作為正式預設，保留 Existing 作為比對，不另行投入重寫採樣演算法。
3. **流暢度與效能記錄（已接受限制）**：
   - 使用者在文書筆電環境下測試，**Standard、Balanced、High 三種檔位均未出現可察覺掉幀，且均觀察到 60 FPS**。
   - **審查警語／限制說明（Accepted Limits）**：此記錄為特定硬體環境之人工目視觀測，**非通用效能保證，絕不能將此記錄解讀為模組已無效能上限、或已完成專業 Profiler 效能測量**。各檔位之核心粒子預算（800 / 1100 / 1400）與 Bloom 130 樣本上限（266 / 366 / 466）依然為不可逾越的嚴格資源保護線。專業效能量測可延後。
4. **畫布資料邊界**：
   - `CanvasData` 依然嚴格維持 32×32 規格；GUI Session 內之 256×256 原稿永久保存未實作（屬 Phase 06 範疇）。
5. **光照獨立性與正式組合**：
   - Phase 05C 之 `fullbright_exact` 繼續維持為獨立光照正式預設，完全不受本次畫質檔位變更影響。
   - 最終產品預設組合為：**Balanced + Detail-Preserving + Fullbright Exact**。
