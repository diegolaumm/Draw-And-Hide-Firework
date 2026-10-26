# Phase 05B.2 — 結案報告（Formal Closure）

Status: **Phase 05B.2 — PASS / CLOSED**

結案 Commit：`83062a995934ecd139bbb36e21eda7130a0a8534`
結案依據：Codex 最終獨立技術複審 PASS（Critical 0、Major 0、Minor 0，所有先前問題 RESOLVED），使用者正式批准結案。

---

## 一、結案與審查依據概述

本階段由使用者授權 Gemini／Antigravity 擔任唯一程式開發代理，Codex 擔任獨立唯讀技術審查者。在完成核心功能、審查缺陷修復（Undo/Redo 筆畫生命週期、16 步堆疊限制、Bloom 130 三粒微偏移預覽、Crisp 8-連通分支）、預覽幾何保真度及文字邊界收尾後，經 Codex 進行最終獨立複審：

- **審查 Commit**：`83062a995934ecd139bbb36e21eda7130a0a8534`
- **Git 狀態**：工作樹乾淨、暫存區乾淨
- **審查結論**：
  - Critical：0
  - Major：0
  - Minor：0
  - 先前所有 Major／Minor 缺陷全數 **RESOLVED**
  - Codex 建議：`PASS — 可正式標記 Phase 05B.2 PASS / CLOSED`
  - 使用者正式接受並批准結案。

### 本階段完整交付項目總覽：

| 交付項目 | 規格與實作摘要 | 驗證與結果 |
| --- | --- | --- |
| **智慧轉換演算法（FreehandResampler）** | 支援 `Mode.SOFT`（等面積預乘 Alpha 降採樣）與 `Mode.CRISP`（8×8 區塊 64 位元點陣圖 8 方向膨脹連通分支檢查，連續跨度 $\ge 4$ 時以有界幾何平均補償 Alpha，絕不粗暴二值化，孤立雜點不增益）。 | SmartResamplerTest 6/6 PASS；單元測試涵蓋連續斜線增益、分散雜點拒絕、色相一致性及 null 安全。 |
| **煙火輸出預覽（CanvasPreview & ViewMode）** | 支援 `DRAW`、`CANVAS_32`、`FIREWORK` 三種檢視。32×32 具微棋盤透明底；Dust 預覽重用 `DustStyle.BLOOM_130` 之 3 粒微偏移物理採樣，長寬比以 `Math.max(w,h) / CANVAS_SPAN` 精確縮放，外圍邊界裕度消除硬裁切。預覽與儲存 100% 共用快取。 | CanvasPreviewTest 4/4 PASS；DustShapeFidelityTest 12/12 PASS（含數字 6、圓形、斜線、矩形幾何保真度）。 |
| **歷史復原／重做（Undo / Redo）** | `FreehandCanvas` 實作有界雙向歷史堆疊，上限嚴格鎖定為 **16 步**。新操作提交自動截斷 Redo 分支；`undo()` / `redo()` 先結束活躍在途筆畫再判斷出棧，徹底解決滑鼠按住快捷鍵拋出 `NoSuchElementException` 缺陷。 | FreehandCanvasTest 16/16 PASS（含活躍筆畫 Undo、活躍筆畫 Redo 分支截斷防崩潰、16 步上限淘汰）。 |
| **非遞迴油漆桶填色（Flood Fill）** | 迭代非遞迴 BFS 演算法（queue 容量 65,536），杜絕 `StackOverflowError`；支援透明背景填色、相同顏色防護及橡皮擦清除填色。 | FreehandInputTest 4/4 PASS、FreehandCanvasTest 填色測試 PASS。 |
| **GUI 自適應安全排版（CanvasEditorLayout）** | 抽取純 Java `CanvasEditorLayout`。畫板尺寸依文字測量動態計算；統計文字支援 Full $\to$ Short $\to$ Compact $\to$ Minimal $\to$ Number $\to$ Safe Omission 逐級降級階梯；移除 40px 人為保底，文字右界絕對不侵入按鈕；說明文字受視窗底緣限制。 | DustShapeFidelityTest 排版邊界測試全數 PASS；遍歷 50..800 寬度不侵入按鈕幾何不變量成立。 |

---

## 二、記憶體精確估算（Undo / Redo 16 步）

在目前 256×256 高解析度工作畫布下（經 Codex 稽核核對）：
- 單一快照大小：$256 \times 256 \times 4\text{ bytes} = 262,144\text{ bytes} = 256\text{ KiB}$。
- 歷史堆疊總量：因新筆畫提交時必先清空 `redoStack`，而 `undo()` 僅是將快照由 `undoStack` 轉移至 `redoStack`，兩者合計快照數在任何時刻均嚴格 $\le 16$ 份。
- **歷史堆疊像素常駐記憶體上限**：$16 \times 256\text{ KiB} = 4,096\text{ KiB} = 4\text{ MiB}$（更正先前誤寫的 8 MiB）。
- **固定工作陣列記憶體**：
  - `pixels`：$256 \times 256 \times 4\text{ bytes} = 256\text{ KiB}$
  - `strokeBase`：$256 \times 256 \times 4\text{ bytes} = 256\text{ KiB}$
  - `coverage`：$256 \times 256 \times 1\text{ byte} = 64\text{ KiB}$
  - 固定工作陣列合計：$576\text{ KiB} \approx 0.56\text{ MiB}$。
- **畫布基底總常駐記憶體**：$4\text{ MiB} + 0.56\text{ MiB} \approx 4.56\text{ MiB}$。
- **活躍筆畫期間暫態記憶體**：進行中的筆畫會暫存 `strokeSnapshot`（$256\text{ KiB} = 0.25\text{ MiB}$），繪製期間峰值合計 $\approx 4.81\text{ MiB}$（不計 JVM 物件標頭小量開銷），完全有界且安全。

---

## 三、數字「6」與幾何形狀資料流分析結論

經對原始畫布 → Soft/Crisp → CanvasData → CanvasDownsampler → DustPattern → DustStyle.BLOOM_130.sample() → 2D 投影進行完整數據流稽核：

1. **投影方向與幾何中心**：
   - 垂直方向：畫布 $row$ 增加對應世界座標 $-Y$（向下），預覽轉換 $screenY = top + boardHeight / 2 - s.y * scale$ 正負抵消，完全維持由上至下的自然繪圖方向。
   - 水平方向：畫布 $col$ 增加對應世界座標 $+X$（向右），預覽轉換維持由左至右。
   - 質心：16×16 與 32×32 筆畫質心與 Dust 粒子質心誤差 $< 0.002$ 區塊（對稱無偏差）。
2. **邊界裁切（Hard Boundary Clipping）修正**：
   - 舊版預覽使用 `screenX <= left + boardWidth && screenY <= top + boardHeight` 嚴格判定。
   - 畫布最底層（row 15）像素中心在 `top + boardHeight - 0.5 * pitch`。當粒子微偏移朝下時，其螢幕位置會接近或超過 `top + boardHeight`。
   - 舊版在此情況下會將粒子直接丟棄（Drop），導致底排像素的粒子只剩朝上偏移的粒子被繪製出來，造成視覺上的下緣向上漂移與缺損感。
   - 修正：預覽邊界判定加入 `margin = Math.round(preset.localOffset() * scale) + r + 2`，確保全數 348 顆粒子 100% 繪製，消除人為不對稱裁切。
3. **16×16 畫布粒子分散現象的本質**：
   - 產品微偏移固定為 `localOffset = 0.14f` 區塊。
   - 16×16 畫布像素間距為 $4.95 / 16 = 0.3094f$，微偏移約佔像素間距的 **45.2%**。
   - 32×32 畫布像素間距為 $4.95 / 32 = 0.1547f$，微偏移約佔像素間距的 **90.5%**。
   - 在 16×16 的離散網格上，每個階梯像素的 3 顆粒子呈星芒狀散開，粒子間隔明顯；而在 32×32 網格上，相鄰像素的微偏移粒子大量重疊融合，呈現出連續密實的筆畫感。此為解析度與既定物理採樣公式的真實表現。

---

## 四、自動化驗證結果

1. **Gradle Clean Build**：
   `.\gradlew.bat clean build --no-daemon`：**BUILD SUCCESSFUL in 27s**（7 actionable tasks: 7 executed，exit code 0）。
2. **自動化測試（JUnit）**：
   **115 / 115 全部 PASS**（0 failures, 0 errors, 0 skipped）。
   - `DustShapeFidelityTest`（12 項，包含數字 6 幾何保真度、圓形對稱與 1:1 比例、45度斜線單調性、矩形比例、Bloom 130 偏移特性、邊緣粒子無裁切丟棄、GUI Scale 動態排版邊界無重疊／無溢出、窄視窗統計文字選擇退回與安全省略、候選文字逐級降級與省略測試、`maxTextWidth` 移除 40px 保底數值驗證、各視窗寬度下文字絕對不侵入按鈕之 `resolveStatsText` 驗證、舊版排版重疊回歸對照）。
   - `CanvasPreviewTest`（4 項，包含 3 粒微偏移、矩形比例、快取失效、Save 一致性）。
   - `FreehandCanvasTest`（16 項，含活躍筆畫 Undo、活躍筆畫 Redo 分支截斷防崩潰、16 步上限測試）。
   - `SmartResamplerTest`（6 項，含 8-連通分離雜點拒絕、連續斜線增益測試）。
   - 其他 83 項既有測試全數 PASS。
3. **Phase 02 三組獨立回歸**：
   - `Phase02TimelineCheck.java`：PASS
   - `Phase025DustStyleCheck.java`：PASS
   - `Phase026BloomCheck.java`：PASS
4. **格式與空白檢查**：
   `git diff --check`：PASS（exit code 0，無任何空白錯誤）。
5. **產出 JAR**：
   `build/libs/drawfirework-1.0.0.jar`。

---

## 五、已完成驗收與保留事項清單

### 使用者已確認 PASS 之人工驗收（8 項全數通過）：
1. **初次 5/5 人工驗收 PASS**：無格線手繪、筆刷粗細、調色盤選色、雙向撤銷重做基礎操作、伺服器儲存。
2. **修復後 3/3 人工驗收 PASS**：
   - 活躍筆畫進行中按快捷鍵安全撤銷與重做分支截斷 PASS。
   - Dust 預覽切換、3 粒微偏移點簇 PASS。
   - 儲存後在世界施放，五階段生命週期與色彩正常，**使用者確認實際煙火效果已改善**。

### 正式結案保留事項（明列非阻塞追蹤項目，不得將未執行測試記為 PASS）：
1. **極窄視窗實機排版重測**：尚未進行遊戲內真人實機重測，但經 Codex 獨立複審評估為非阻斷事項（已由 JUnit 115/115、`DustShapeFidelityTest` 遍歷 50..800 寬度之幾何不變量與安全省略演算法 100% 通過保證）。
2. **Dust 夜間偏暗**：原版 dust 粒子無光照發光屬性所致，按既定規劃留待整體 GUI 完成後另行調查處理。
3. **Soft／Crisp 肉眼差異有限**：低解析度 32×32 網格下邊界平滑度感知差異較細微，保留為後續視覺品質優化項目。
4. **高解析度原稿 Session 暫存**：原稿目前僅在開啟編輯器期間暫存於 Session，永久儲存至 JSON 屬 Phase 06 範圍。
5. **Dust 預覽二維示意性質**：預覽僅為二維平面粒子排布示意，不等同包含 3D 透視、Billboard 深度、夜間環境光與完整 A–E 五階段生命週期的世界實體煙火。

---

## 六、精確 Git 暫存候選清單（本次結案文件）

```text
docs/DECISIONS.md
docs/PHASE_05B2_ACCEPTANCE.md
docs/PROGRESS.md
docs/PROJECT_PLAN.md
docs/TEST_RESULTS.md
```
