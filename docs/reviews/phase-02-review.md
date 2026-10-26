# Phase 02 / 02.5 / 02.6 獨立技術審查報告

審查日期：2026-10-23（Asia/Taipei）  
審查員：Antigravity（獨立技術審查員）  
審查目標：Draw & Hide Firework 專案 Phase 02（02、02.5、02.6）動畫生命週期與 Dust Bloom 視覺成果  
受審 Commit：`5af5187` (feat: finalize phase 02 animated dust fireworks)  

---

## 一、核心查核項目詳細分析

### 1. 五階段動畫時序與運作邏輯（A20／B8／C14／D20／E14 ticks）
* **時序定義**：
  * 在 `FireworkTimeline` 與 `DustStyle` 中，時序區間嚴格定義：
    * **Stage A (Launch)**：`[0, 20)`，共 20 ticks。尾跡粒子自中心下方 6 格等速上升，並播放發射音效。
    * **Stage B (Formation)**：`[20, 28)`，共 8 ticks。在中心點播放爆炸音效，粒子隨 `spread = clamp((tick - 20)/8f, 0, 1)` 自中心平滑展開至目標座標。
    * **Stage C (Image Hold)**：`[28, 42)`，共 14 ticks（經 Phase 02.5 使用者實測評定為最佳停留時長）。`spread == 1`，維持完整形狀，面向相機旋轉。
    * **Stage D (Artistic Dispersion)**：`[42, 62)`，共 20 ticks。解除位置鎖定（停止呼叫 `positionDust`），賦予向外散射初速度，並以 `drag = 0.96^elapsed` 與柔和重力模擬自由運動。
    * **Stage E (Fade Out)**：`[62, 76)`，共 14 ticks。沿用 Phase 02.6 之 Bloom 基礎，逐 tick 以 `fadeScaleAt(age)` 縮小軟粒子尺寸直至約 1/15，於 76 tick 自動呼叫 `clear()`。
* **判定**：動畫狀態轉換嚴謹，純邏輯單元測試（`Phase02TimelineCheck`）全數通過，邏輯與規格完全吻合。

### 2. 粒子壽命、大小綻放（Bloom）、散射與清理邏輯
* **粒子壽命延續**：
  * 原版 `DustParticle` 隨機壽命約 8~40 ticks，若不處理將在 Stage C/D 提前死亡。程式在 `age == 20` 生成時明確調用 `p.setLifetime(timeline.end() - LAUNCH_END + 10)`（共 66 ticks），徹底解決提前缺點問題。
* **大小綻放（Bloom 130%）**：
  * 在 Stage B 結束後（age 28~32），透過 `DustStyle.smoothStep` 於 4 ticks 內將單粒尺寸平滑綻放至 1.30 倍。
  * 縮放使用 `p.scale(target / current)` 比率方式調用，避免每 tick 累積性指數膨脹。
  * Stage D 開始時，記錄 `releaseSize` 並在 4 ticks 內平滑收斂至基準 Bloom 大小，無瞬間跳變或尺寸縮回。
* **散射與清理**：
  * Stage D 首 tick 依據相機朝向計算世界座標散射初速度，隨後不再覆寫坐標。
  * `clear()` 逐一對粒子執行 `particle.remove()` 並清空列表與世界參照，結束機制完整。

### 3. 預設效果與比較選項保留狀態
* **預設效果**：
  * `HeartPrototype.java` 內 `style` 預設初始化為 `DustStyle.BLOOM_130`。
  * `/drawfirework heart` 與 `/drawfirework heart dust` 均導向 `DustStyle.BLOOM_130`，符合使用者決策。
* **比較選項完整保留**：
  * `/drawfirework heart dust normal`：100% 基礎尺寸。
  * `/drawfirework heart dust bloom115`：115% 綻放比較。
  * `/drawfirework heart dust bloom130`：130% 正式預設。
  * `/drawfirework heart dust legacy`：Phase 02 原始單粒/28t 停留對照。
  * `/drawfirework heart image`：Phase 01 Quad 貼圖對照。
  * `/drawfirework clear`：立即停止並清除。
  * 判定：指令結構清晰且完全相容先前所有測試路徑。

### 4. 粒子預算與生成受控性
* **核心粒子數**：
  * 固定遮罩為 63 點。在 `BLOOM_130` 下每點 3 粒，核心粒子總數為 `63 × 3 = 189` 粒。
  * 僅在 `age == 20` 建立一次，無每 tick 重生或補生路徑。
* **裝飾粒子數**：
  * 僅在 `age < 20`（升空階段）每 tick 生成 1 粒尾跡粒子，整段動畫最多生成 19 粒，且 `trail.removeIf(p -> !p.isAlive())` 自動剔除死粒子。
* **預算檢核**：
  * 核心 189 粒（預算上限 800），裝飾最多 19 粒（預算上限 200），兩者獨立且遠低於上限。同時存在上限保守估計不超過 208 粒，無未受控生成。

### 5. 生命週期與異常邊界保護
* **相機失效防護**：`tickDust()` 開頭檢查 `client.gameRenderer.mainCamera()`，若為空立即調用 `clear()` 中止，防止 NPE。
* **世界切換與玩家脫離**：
  * 在 `END_CLIENT_TICK` 檢查 `owner != client.level || client.player == null`，觸發 `clear()`。
  * 在 `END_LEVEL_TICK` 檢查 `level != Minecraft.getInstance().level`，觸發 `clear()`。
* **重複觸發**：`start()` 前段必先執行 `clear()`，新指令替換舊動畫，杜絕重複堆疊。
* **限制說明**：上述機制已在代碼層落實，但目前缺少使用者在遊戲內手動觸發邊界（例如動畫中斷線、切換世界）之實測報告。

### 6. 客戶端／伺服器職責隔離與 26.3 API 相容性
* **架構邊界**：
  * 全部動畫與樣式類別（`HeartPrototype`、`DustStyle`、`FireworkTimeline`）位於 `com.drawfirework.client`，標記 `@Environment(EnvType.CLIENT)`，僅在 `fabric.mod.json` 的 `client` 入口註冊。
  * 共用端 `DrawFirework.java` 無任何渲染或客戶端類別之引用，未污染伺服器端環境。
* **API 相容性**：
  * 使用之 `SingleQuadParticle.scale`、`Particle.setLifetime`、`ClientLevel.playLocalSound` 及 `SoundEvents` 均經本機 26.3 二進位反組譯查核，無已棄用或臆測 API。

### 7. 對 Phase 03 可變解析度資料結構的適配性
* `FireworkTimeline` 與動畫狀態機與圖像內容完全解耦。
* `DustStyle.sample(pointIndex, particleIndex, x, y)` 為純幾何局部擾動算法，接受任意 `(x, y)` 座標輸入。
* 當 Phase 03 實作 `CanvasData` 與 `CanvasDownsampler` 後，Phase 04 僅需將動態畫布非透明像素之座標與顏色傳入生成迴圈，現有動畫與縮放管線即可直接復用。

### 8. 文件、測試結果與 Git Commit 一致性
* 最新 Commit `5af5187` 包含完整 11 個檔案（3 個 Client 類別、3 個測試類別、5 份專案文檔）。
* 本地工作樹乾淨（clean）。
* `docs/PROGRESS.md`、`TEST_RESULTS.md`、`DECISIONS.md` 與 `PROJECT_PLAN.md` 內容彼此呼應，測試數據與檔案雜湊記錄詳實。

### 9. 測試邊界誠實性核對
* 文件明確將狀態標定為 **PARTIAL**，未因視覺效果良好而冒稱全項通過。
* 誠實標註手動 `/drawfirework clear`、切換世界清理、密集重複觸發、音效次數以及 FPS/GC 等項目「尚未實測」，完全符合規格要求。

### 10. 稀疏感與特定視角形狀變化分析
* **可觀察事實**：
  * 使用者回報：雖然 Bloom 130% 較為飽滿，但整體仍有些微「稀疏感」；且從部分斜向視角觀看時，圖案呈現透視變形。
* **可能原因分析**：
  1. *稀疏感*：目前圖案為固定的 63 點，點與點間距為 0.45 格。3 粒粒子主要在 0.14 格半徑內微幅聚集，無法填補 0.45 格的離散間隔。在 12 格外的視野中，點陣間隙仍然顯著。
  2. *斜角變形*：點陣旋轉以中心點面向攝影機（`rotation.set(camera.rotation())`）。當視角處於大角度俯仰或偏側時，透視投影（Perspective Projection）的近大遠小效應會使平面形狀在螢幕空間產生自然的梯形或壓扁透視失真；此外，微小的深度偏移（z 軸 ±0.042）在側角觀察時亦會造成平面線條的視覺錯位。
* **後續改善建議（留待後續階段）**：
  * 稀疏感可在 Phase 03/04 引入高解析度畫布與降採樣器時，提升採樣點數（例如 300~500 點）自然解決。
  * 斜角變形可在後續評估是否需改用面向視線的 Billboard（Spherical/Cylindrical Billboard）或維持世界固定平面。目前階段維持現狀是合理的，避免在缺乏多角度量測前引發旋轉抖動。

---

## 二、問題分類

### 🔴 Critical（重大問題）
* **無**。

### 🟠 Major（主要問題）
* **無**。

### 🟡 Minor（次要問題 / 待後續驗證項目）
1. **清理邊界與極端情境實測留存**：
   * 動畫中途手動 `/drawfirework clear`、連點重複觸發、切換世界或退出重入，尚需在遊戲中進一步驗證無粒子殘留。
2. **缺乏硬體效能量測數據**：
   * 缺少客觀的 FPS、Frame Time 及 GC 配置量測數據（目前僅依粒子數與算法推算）。
3. **斜向視角透視變形**：
   * 大角度觀察時的透視壓縮現象，列為 Phase 04/08 渲染優化之研究議題。

### 🟢 Verified（已確認正常項目）
* **五階段時序精準**：A20 / B8 / C14 / D20 / E14 ticks 與平滑插值運作正確。
* **粒子生命週期與綻放**：76 ticks 內無提前缺點，4 ticks 內自然綻放至 130%，Stage D 平滑釋放無突變。
* **預設配置正確**：Bloom 130% 設為正式預設，各對照組選項完整保留。
* **嚴格受控之粒子數量**：核心 189 粒、裝飾最多 19 粒，遠低於 800/200 預算上限。
* **乾淨的架構分離**：Client/Server 邊界明確，未污染共用端或引入相容性風險。
* **文檔與 Git 高度一致**：文件、測試、提交雜湊與代碼完全吻合，未做虛假宣稱。

---

## 三、審查結論

# 審查結論：PASS（Phase 02 動畫與視覺選型合格）

**判定理由**：
Phase 02（含 02.5、02.6 微調）的核心目標——「五階段確定性動畫時序、Launch 尾跡與聲音、Formation 展開、Hold 停留、Dispersion 散射與 Fade 縮小淡出、解決粒子隨機死亡並完成視覺風格選型」——已全面且紮實地達成。使用者實測確認 Bloom 130% 效果自然流暢且圖案飽滿。未實測之邊界與效能已誠實揭露，且完全不影響核心動畫邏輯與架構之健全性。

**Phase 03 准入判定**：
# ✅ 可以進入 Phase 03（畫布資料模型與純邏輯降採樣器）

**進入 Phase 03 理由**：
Phase 03 的工作範圍為純 Java 資料模型（不可變 ARGB 畫布、版本化 Codec、驗證邏輯與 `CanvasDownsampler` 純演算法及單元測試），完全不依賴遊戲內實體渲染或伺服器網路，亦不會受目前未測之遊戲邊界清理所阻塞。現有動畫架構對動態畫布具有良好的擴展介面，已完全具備開始 Phase 03 開發的條件。
