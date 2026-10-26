# Phase 05C — Firework Visibility & Lighting 人工驗收指南

本文件提供 Phase 05C 煙火在白天、夜間與外部光源下的可見度與光照對照實驗操作說明。

## 正式結案決策與證據界限（2026-10-24）

Status: **PASS / CLOSED**；clean build、124/124 JUnit、Phase02三組回歸與差異檢查通過，無已知Critical／Major；以下未測事項依使用者授權接受風險。

使用者已正式批准最終整合與結案政策：正式預設改為 `fullbright_exact`；保留 `fullbright` 自然色差、`baseline` 舊版環境光回退、`exact_only` 診斷模式。使用者已測白天及手繪多色圖案，回報夜間效果不錯、Exact 明亮飽滿且稍偏螢光可接受，白天略假，暫不要求色彩調整。這些觀察保留，不代表下列所有矩陣格均已驗證。

原版 Dust 完整實機隔離、四模式／所有日夜圖案排列組合、部分重進／重啟生命週期未全面實測，按使用者最新政策記為 **Accepted Risk / Deferred Verification**，不冒稱 PASS，不再單獨阻擋結案。以下流程是保留的後續驗證方式，非本次新增結案門檻；若發現實際編譯、Mixin、安全或播放回歸，仍須處理。

### 最短 Runtime 流程

1. 使用本專案 `runClient`，進入允許指令的一般單人創造世界。保持粒子設定、距離、相機與天氣一致。`/drawfirework lighting` 查詢模式；乾淨啟動預期 `fullbright_exact`。
2. 先 `/time set day`，再 `/time set midnight`；每種時段依次執行 `/drawfirework lighting baseline`、`/drawfirework lighting fullbright`、`/drawfirework lighting fullbright_exact`、`/drawfirework lighting exact_only`。每次切換後執行 `/drawfirework heart`，等完整動畫結束，再右鍵發射同一張手繪圖案。共 2 時段 × 4 模式 × 2 圖案 = 16 組。記錄輪廓／色彩／亮度／A–E 清理與聲音，不預填 PASS。
3. 手繪準備：先 `/drawfirework_test_item` 取得含有效 Smiley Canvas 的測試煙火（需要創造模式及空背包格）；主手持有，關閉聊天與其他 GUI，按 **G**（若重綁則用控制設定中的鍵）開啟編輯器。畫紅綠藍白、細線及透明空洞，按 Save 更新手持物品，再右鍵發射。Save 不是本機 JSON 草稿儲存。
4. 原版 Dust 隔離：以相同原版 `/particle` Dust 指令在同一無外部光源位置生成同色粒子，先 baseline 再 fullbright_exact 比較，原版粒子不應隨本模組模式變亮或被還原 RGB。原版粒子仍應受粒子所在位置的環境光影響。Dust 指令參數以當前 26.3 遊戲自動完成為準；操作與實際指令需保存，不能以 mock 測試代替此項。
5. 設 baseline，動畫中退出至主選單，再進同一世界：`/drawfirework lighting` 預期仍為 baseline；舊效果／尾跡／聲音不重播。新煙火應完整播放並在 76 tick 結束清除。切換另一世界也不應殘留舊效果。`/drawfirework clear` 只清效果；`/drawfirework lighting reset` 恢復 fullbright_exact。
6. 正常關閉並重新啟動客戶端後查詢，預期 fullbright_exact。模式是客戶端本地記憶體狀態，未寫入設定檔或傳送 C2S。模式只在粒子建立時套用，測試中等前次結束後再切，避免把舊粒子誤認為切換失敗。

指令註冊：`HeartPrototype.onInitializeClient()` 的 `ClientCommandRegistrationCallback` 將 `lightingCommand()` 接到 `/drawfirework`。lighting 查詢／四種完整模式名／reset 不含伺服器權限判斷，也不傳 C2S；需在已連線世界使用。`DustLightingMode.parse()` 的別名未接到 Brigadier 註冊，請用上述完整模式名稱。GUI 額外檢查單人、創造、主手物品與可用編輯 payload；物品發射另受伺服器驗證及頻率限制。固定愛心使用 `/drawfirework heart`，不要用繞過 Dust 的 `heart image` 比較光照。

回報最少包含：實際版本與模式、16 組各結果或未測、原版 Dust 指令與隔離結果、重進／重啟後查詢值、殘留／重播情況。JUnit、Mixin 位元碼與肉眼觀察分開記錄。

---

## 一、實驗背景與核心機制對照

### 1. 原始碼診斷結論
- **原版 Dust 粒子（Baseline）**：
  - 繼承 `Particle.getLightCoords(float)`，即時查詢粒子所在世界方塊位置的環境光（天光與方塊光）。
  - **夜間且無外部光源時**：環境光可能使 Dust 偏暗；實際天光與畫面亮度取決於位置與渲染條件，不以固定數值代替量測。
  - **夜間且有火把時**：只有光照傳到粒子所在位置才可能提亮；地面火把不保證照亮高空圖案。
  - **RGB 隨機暗化**：`DustParticle` 建構子乘上 $[0.6, 1.0]$，`DustParticleBase.randomizeColor` 額外乘上 $[0.8, 1.0]$，平均暗化約 28%（乘數 $0.48 \sim 1.00$，平均 0.72），且三色通道隨機分離。
- **原版煙火 Spark 粒子**：
  - 繼承 `SimpleAnimatedParticle.getLightCoords(float)`，無條件回傳 `15728880`（`0x00F000F0 = LightCoordsUtil.FULL_BRIGHT`），具備自體發光特性（Fullbright），不受世界環境黑夜影響。
- **本專案解決策略**：
  - 不更換為尖銳星狀的 Spark 粒子，嚴格保留 Dust 柔和圓潤的粒子貼圖（`minecraft:generic_0`..`7`）、Bloom 130% 簇狀物理分布與 76 ticks 五階段生命週期。
  - 透過 Duck Interface 與專屬 Mixin，對本模組標記粒子覆寫 `getLightCoords()`；未標記路徑保留原方法，仍需原版粒子 Runtime 回歸確認。

---

## 二、可用實驗模式與切換方式

本專案提供 4 種可切換的光照與色彩實驗模式：

| 模式名稱 | 說明 | 光照值（LightCoords） | RGB 色彩機制 |
| :--- | :--- | :--- | :--- |
| `baseline` | **舊版環境光比較／回退模式** | 世界即時環境光（天光＋方塊光） | 原版隨機暗化（平均 72%） |
| `fullbright` | **高亮度光照實驗版** | 固定 Fullbright（15728880） | 保留原版隨機暗化（平均 72%） |
| `fullbright_exact` | **正式預設** | 固定 Fullbright（15728880） | 粒子 RGB 設回來源值；螢幕觀感仍受渲染影響 |
| `exact_only` | **純色彩對照版** | 世界即時環境光（天光＋方塊光） | 粒子 RGB 設回來源值；螢幕觀感仍受渲染影響 |

### 指令切換
- 查看當前模式：
  `/drawfirework lighting`
- 切換為高亮度實驗版：
  `/drawfirework lighting fullbright`
- 切換為高亮度＋精確色彩實驗版：
  `/drawfirework lighting fullbright_exact`
- 切換為純色彩對照版：
  `/drawfirework lighting exact_only`
- 恢復正式預設 fullbright_exact：
  `/drawfirework lighting reset`
- 明確切換舊版環境光比較／回退模式：
  `/drawfirework lighting baseline`

切換後，後續生成的本模組 Dust 尾跡及核心粒子套用該模式；既存粒子不重新著色。適用於 `/drawfirework heart`、`/drawfirework canvas ...` 及經伺服器核准的測試煙火物品右鍵播放。

---

## 三、A/B 實機對照實驗矩陣

請在一般單人世界中，挑選開闊平原或平坦地形進行以下情境的對比；上述最短流程的四模式矩陣為完整補測清單：

### 情境 1：日間開闊天空（Daytime, Open Sky）
- **操作**：輸入 `/time set day`。
- **對照測試**：
  1. `/drawfirework lighting baseline` $\to$ `/drawfirework heart`
  2. `/drawfirework lighting fullbright` $\to$ `/drawfirework heart`
  3. `/drawfirework lighting fullbright_exact` $\to$ `/drawfirework heart`
- **觀察重點**：
  - `baseline` 在白天因有太陽光照，是否仍略感暗淡？
  - `fullbright` 是否顯著清晰？
  - `fullbright_exact` 的色彩鮮豔度與輪廓辨識度是否最佳？
  - 是否會過曝或失去 Dust 柔和質感？

### 情境 2：夜間無光源（Nighttime, No External Light）
- **操作**：輸入 `/time set midnight`，確保身邊及周圍 30 格內**沒有任何火把、螢石或燈籠**。
- **對照測試**：
  1. `/drawfirework lighting baseline` $\to$ `/drawfirework heart`
     - *預期現象*：煙火升空後幾近漆黑，肉眼很難辨識心形與色彩。
  2. `/drawfirework lighting exact_only` $\to$ `/drawfirework heart`
     - *預期現象*：在缺乏光照座標下，純色彩提升對黑夜環境的改善依然有限。
  3. `/drawfirework lighting fullbright` $\to$ `/drawfirework heart`
     - *待驗證目標*：無火把夜空中可辨識圖案，且完整播放五階段與 Bloom 簇狀形態。
  4. `/drawfirework lighting fullbright_exact` $\to$ `/drawfirework heart`
     - *預期現象*：色彩最為飽和鮮豔，對比最強烈。

### 情境 3：夜間有火把（Nighttime with Torch Nearby）
- **操作**：維持 `/time set midnight`，在施放點地面插上 1 支火把。
- **對照測試**：
  1. `/drawfirework lighting baseline` $\to$ `/drawfirework heart`
     - *待驗證目標*：若粒子位置實際受到火把照明，環境光會影響其亮度；未受照明不能判為失敗。
  2. `/drawfirework lighting fullbright` $\to$ `/drawfirework heart`
     - *預期現象*：不受火把距離限制，整片粒子群亮度均勻一致。

### 情境 4：手繪 Canvas 彩色煙火驗證
- **操作**：
  1. 一般單人創造模式，主手持有效 `drawfirework:test_firework`，按 G 開啟 GUI，繪圖後按 Save 更新物品資料；不是右鍵開 GUI，也不是本機草稿儲存。
  2. 在夜間無火把環境下，依序切換 `/drawfirework lighting baseline` 與 `/drawfirework lighting fullbright` / `fullbright_exact` 並右鍵施放。
  3. 驗證自訂手繪圖案在 Fullbright 下的線條清晰度、長寬比及色彩保留。

---

## 四、驗收評估回報項目

請使用者在實機測試後，依據肉眼感受回報以下問題：
1. **可見度改善幅度**：夜間無火把時，Fullbright 是否徹底解決了看不見的問題？
2. **視覺質感保留**：Fullbright 是否依然保有 Dust 柔和、蓬鬆的煙火質感，還是變成了死板的貼圖？
3. **刺眼程度**：在全黑的夜空中，`fullbright` 或 `fullbright_exact` 是否會過度刺眼或過曝？
4. **色彩偏好**：您更偏好保留原版隨機暗化顆粒感（`fullbright`），還是偏好 100% 精準色彩（`fullbright_exact`）？
5. **正式預設決定（已批准）**：fullbright_exact；保留 fullbright 自然色差。後續驗證只需回報實際異常，無需重複要求使用者批准同一預設決定。
