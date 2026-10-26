# Phase 05C — Codex 獨立技術審查與 05D 準備

## 最終整合結案（最新，2026-10-24）

**Phase05C PASS / CLOSED**。使用者明確批准由Codex最小整合並調整結案政策。本節取代下文候選／baseline預設／PARTIAL門檻及「本輪未修程式」的歷史狀態；原獨立審查內容保留，不假稱本次修改經另一個獨立代理審查。

- 正式預設／reset／null fallback：fullbright_exact（DustLightingMode.DEFAULT）。保留fullbright、baseline、exact_only；客戶端記憶體欄位及既有指令接線，不改全域光照／其他原版粒子。parse工具的default別名同步至正式預設，未擴增Brigadier指令別名。
- 本次程式修改僅HeartPrototype.java、DustLightingMode.java、DustLightingModeTest.java；既有實作全部保留。新增產品reset方法供指令及JUnit共用，確認四模式reset均恢復新預設；不是重構渲染。
- 本次clean build exit0（1m55s）、124/124 JUnit（19 suites、0 failure/error/skipped）、Phase02三組回歸各exit0、git diff --check exit0。完整紀錄見TEST_RESULTS最新節。
- Mixin.java及注入JSON與原獨立審查SHA256完全相同；仍僅true旗標取消原光照方法。舊HeartPrototype SHA256屬歷史審查基準，因本次最小預設／reset修改已不再是當前檔案雜湊。
- 使用者可見度／色彩觀察保留；原版Dust完整實機隔離、所有模式／日夜圖案矩陣、部分重啟／重進生命週期依本次授權為Accepted Risk / Deferred Verification。沒有冒稱PASS，不再單獨阻擋結案。無已知Critical／Major或實際編譯／Mixin／安全／播放回歸。
- HEAD仍94458d5；未暫存、commit或push。05D／06未開始；本次完成後停止。

### 最終精確暫存候選（共15檔，路徑相對D:\minecraftmod）

本次在既有05C實作上另做最小收尾（3檔）：

```text
src/main/java/com/drawfirework/client/HeartPrototype.java
src/main/java/com/drawfirework/client/DustLightingMode.java
src/test/java/com/drawfirework/client/DustLightingModeTest.java
```

保留的既有05C程式／設定，本次內容未改（4檔）：

```text
src/main/java/com/drawfirework/client/GlowingParticleAccess.java
src/main/java/com/drawfirework/client/mixin/ParticleMixin.java
src/main/resources/fabric.mod.json
src/main/resources/drawfirework.mixins.json
```

累積階段文件及證據（8檔；runtime-evidence本次未改，其餘補記結案，保留前次內容）：

```text
docs/API_COMPATIBILITY.md
docs/DECISIONS.md
docs/PROGRESS.md
docs/PROJECT_PLAN.md
docs/TEST_RESULTS.md
docs/PHASE_05C_ACCEPTANCE.md
docs/reviews/phase-05c-review.md
docs/reviews/phase-05c-runtime-evidence.txt
```

使用者提交前依具名路徑核對／暫存，檢查staged diff與check；不使用git add -A，不納入build、run、快取或世界。此清單是完整05C提交候選，不代表每檔皆為本次新增。沒有替使用者執行Git寫入。

## 2026-10-24 補記：文件 Remediation 與 Runtime 啟動

本節取代下文「待修正／尚未啟動」的當前狀態敘述，保留原審查證據與發現。

- Minor 1：已修正指南為 `/drawfirework_test_item` 取得有效測試物品、主手持有後 G 開 GUI、Save 更新物品、右鍵發射；補充單人／創造／payload 條件、模式註冊位置與完整名稱，不宣稱 parse 別名可直接作 Brigadier 指令。
- Minor 2：已直接修正 TEST_RESULTS 8 項測試的範圍與 PROJECT_PLAN 的過度摘要；mock／重複採樣／getter-setter 均不再宣稱驗證 Mixin、光照接線或粒子生命週期。API_COMPATIBILITY 前次已補實際簽章，本輪補實際注入證據。屬文件修正完成，不是假稱獲得另一次獨立審查。
- runClient 使用暫時程序環境 JAVA_TOOL_OPTIONS 匯出 Particle。22:07:46 載入26.3／Loader0.19.5，22:08:00 產出轉換類別，22:08:05–07 圖集初始化。`javap -p -c` 顯示 duck interface、旗標及 getLightCoords 的 handler 呼叫，handler true 分支回傳15728880，false 分支仍走原方法。實際啟動注入已確認；原版 Dust 的世界行為未實測。
- 本次以 Ctrl+C 結束 runClient（工具 exit 1 為主動中斷）；沒有正常 GUI 關閉成功結論。沒有進入或修改世界。既有 Realms 開發帳號認證失敗、Perflib 計數器警告及缺少可選 Fernflower decompiler 已見於日誌；匯出 class 成功，不將它們當作本模組注入失敗。
- 原生 Minecraft GUI 控制在此代理環境不可用；四模式 × 愛心／手繪 × 白天／夜間、未標記 Dust、重進與重啟模式／清理仍需使用者按指南執行。使用者既有視覺觀察原樣保留。
- `fullbright_exact` 仍僅為候選，保留 fullbright；正式預設 baseline 不變。Phase 05C PARTIAL，不能 PASS / CLOSED；不開始05D／06。

啟動／轉換位元碼證據： [phase-05c-runtime-evidence.txt](phase-05c-runtime-evidence.txt)。本次程式 SHA256 與下文一致，未修改 Java／JSON／Gradle。

### 精確暫存候選清單（只提供，未執行暫存）

既有 Phase 05C 實作（本輪內容未變）：

```text
src/main/java/com/drawfirework/client/HeartPrototype.java
src/main/java/com/drawfirework/client/DustLightingMode.java
src/main/java/com/drawfirework/client/GlowingParticleAccess.java
src/main/java/com/drawfirework/client/mixin/ParticleMixin.java
src/main/resources/fabric.mod.json
src/main/resources/drawfirework.mixins.json
src/test/java/com/drawfirework/client/DustLightingModeTest.java
```

本次文件修正／證據（前七份已有未提交內容，須保留並逐段檢查，不可整檔當作全新變更）：

```text
docs/PHASE_05C_ACCEPTANCE.md
docs/TEST_RESULTS.md
docs/PROJECT_PLAN.md
docs/PROGRESS.md
docs/DECISIONS.md
docs/API_COMPATIBILITY.md
docs/reviews/phase-05c-review.md
docs/reviews/phase-05c-runtime-evidence.txt
```

建議先檢查上述具名路徑差異；文件與既有實作可分開審核。使用者自行決定是否以具名路徑或互動 hunk 暫存，再檢查 staged diff／check／stat；任何提交描述須保持05C PARTIAL。不要納入 run/、.mixin.out、build/ 或 Gradle 快取。本輪未暫存、commit或push。

日期：2026-10-24（Asia/Taipei）。角色：Gemini／Antigravity 為程式實作者；Codex 本輪唯讀審查程式，僅寫入審查／驗證文件，不修程式、不 commit、不 push。

## 基準與結論

- HEAD：`94458d5e2b93fba29754e37d2824ea7b90744c6f`。
- 工作樹起始並非乾淨：6 個 tracked 修改、6 個 untracked 項目（其中 mixin 目錄包含 1 個 Java 檔）。審查對象為 HEAD 加現有 05C 差異；保留全部既有修改。
- 使用者已確認 Gemini／Antigravity 停止寫入，允許本輪建置。
- Phase 05B.2：既有結案文件為 PASS / CLOSED；本輪沒有重開該階段。
- 05C 靜態 Mixin 審查：未發現阻斷程式缺陷；不等於遊戲注入與其他模組相容性已實測。兩項 Minor 見下。
- Phase 05C 仍為 PARTIAL；本輪不宣告 CLOSED、不改正式預設、不實作 05D。
- 最新使用者決策：`fullbright_exact` 為正式預設候選，保留 `fullbright` 自然色差模式。目前程式初始化、reset、null fallback 均仍為 `baseline`。

## 人工觀察（使用者回報，非 Codex 親自觀察）

使用者確認已完成白天開闊天空及手繪多色圖案實際發射驗收：「已完成 有點偏螢光 night時效果不錯 但白天可能有點假 所以我目前不打算現在調整」。此前回報 exact 較飽滿、較還原手繪色，fullbright 有自然煙火色差。

記錄為已測、有保留的視覺回饋；不改寫成全情境畫質 PASS，也不視為現在修改正式預設的批准。尚無每模式／每情境完整矩陣、原版未標記 Dust 回歸或世界切換清理的新人工證據。

## Mixin 技術證據

本輪建置：沙箱首次 daemon 連線失敗（exit 1）；同命令 `.\gradlew.bat clean build --no-daemon` 沙箱外成功（29s，7 tasks executed，exit 0）。本輪 XML：123 tests／19 suites，0 failures/errors/skipped。Phase02TimelineCheck、Phase025DustStyleCheck、Phase026BloomCheck 三組各 exit 0 PASS。程式 SHA256 前後相同。詳見 TEST_RESULTS.md 最新段落。

查核來源為本機 `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar`，使用 `javap -p -c` 讀取實際位元碼，非完整原始碼審計，未以網站舊版簽章代替。

1. `Particle.getLightCoords(float): int` 為 protected，查所在位置環境光；無 chunk 分支回傳 15728640。DustParticle → DustParticleBase → SingleQuadParticle 沒有覆寫此方法，故目前注入能涵蓋 Dust 的繼承路徑。
2. `ParticleMixin` 在 HEAD cancellable 注入；唯一 boolean 欄位預設 false，只有 true 才回傳 15728880。false 路徑不取消原方法。欄位使用 `@Unique`，duck 方法使用模組前綴。
3. `drawfirework.mixins.json` 使用 client 清單、JAVA_25、defaultRequire=1；fabric.mod.json 正確引用設定。共用初始化沒有新增 client import。這是靜態隔離證據，非專用伺服器啟動驗證。
4. `HeartPrototype.applyLighting` 只在本模組建立的 Dust 尾跡／核心粒子呼叫。exact 模式於建立後使用公開 `SingleQuadParticle.setColor(float,float,float)`；Dust tick 不重新隨機化 RGB。
5. Dust 建構子共用亮度因子 [0.6,1.0)，各通道再乘 [0.8,1.0)，平均乘數 0.72；SimpleAnimatedParticle.getLightCoords 回傳 15728880。此證據支持機制，不能保證最終螢幕色彩百分之百相同。
6. 光照修改未變更 CanvasData、payload、ItemStack、採樣、800 核心預算、200 裝飾上限或 76-tick 動畫。模式在粒子生成時套用；已存在粒子不會在切換時重新著色。

查核時 SHA256：

```text
HeartPrototype.java B4C753BE32264E00BF7064D398B3A0BA2018A776D92206E4CFC983EAD6360BEE
ParticleMixin.java C62AE3997A90A3379D128BAC494ADF55B638B0339EBB2389A4A9E453AF6C4666
drawfirework.mixins.json 8A9953D65A45AD1699FE9E6E72EC39633297015D6DF346A5A234961F10AA37D8
```

## 審查項目

### Minor 1 — 人工指南的 GUI 入口錯誤

`docs/PHASE_05C_ACCEPTANCE.md:88` 寫右鍵開 GUI；實際 `CanvasEditorClient` 使用 G（可重綁），右鍵在 `TestFireworkItem.use` 發射。Save 更新伺服器手持物品資料，並非 Phase 06 本機 JSON 草稿。建議實作者修正文案，避免操作無法重現。

### Minor 2 — 測試結論強於實際覆蓋

`DustLightingModeTest:97` mock 自己實作 boolean，未執行 Mixin；`:121` 以相同參數兩次呼叫採樣，沒有套用任何 lighting mode；`:139` 只驗證模式存取。這些測試不能證明注入成功、原版粒子不受影響或真實播放接線。應收斂 TEST_RESULTS 的描述，並補遊戲回歸證據。API_COMPATIBILITY 目前也尚未收錄 05C 的新簽章與查核紀錄；本報告提供本輪來源。

## 可重現人工補測

1. 一般單人創造世界，取得 `drawfirework:test_firework` 並主手持有，按 G 開編輯器。繪製紅綠藍白及細線／透明空洞圖案，Save 更新物品，再右鍵發射。沒有本機草稿保存承諾。
2. 固定距離、視角、圖案、粒子設定與天氣，分別 `/time set day` 與 `/time set midnight`。每次先等前次煙火結束，再切 `baseline`、`fullbright`、`fullbright_exact`，右鍵比較；`/drawfirework heart` 可作固定圖對照。
3. 回報可讀性、輪廓、RGB 偏差、白天螢光感與夜間刺眼程度；沿用使用者暫不調整的決定，不把視覺偏好變成程式修改。
4. 比較原版未標記 Dust 是否仍隨環境光變化，確認模式只影響本模組粒子；退出／重進世界，確認沒有殘留效果或重複聲音。保存含版本與場景的回報。
5. 火把測試必須考慮粒子實際位置；只在地面放火把不保證照亮高空爆炸中心。不能據此宣稱固定天光值或整片圖案必然被照亮。

## Phase 05D 後續方案（尚未動工）

啟動條件：05C 驗證／結案狀態明確、審查事項處置記錄完成、使用者明確授權下一階段及唯一程式寫入者。Codex 不接管 Gemini 的實作角色。

目標：僅客戶端 A/B 原型，Standard／Balanced／High 核心上限約 800／1100／1400。保留 32×32 CanvasData、既有 C2S／ItemStack 編碼及驗證、權限／頻率限制、76-tick 動畫、獨立 200 裝飾上限。上限提高屬有條件原型，需量測後再決定正式值。

預期最小範圍：新增 client quality／sampling 設定與純邏輯策略，調整 DustPattern 的顯示預算介面、HeartPrototype 播放與診斷、CanvasPreview 快取鍵及預覽，新增對應純邏輯測試與紀錄。先查所有 MAX_CORE 呼叫點，不直接把共用常數 800 全域改成 1400；不修改來源資料與網路安全界限。

Bloom 3 粒／樣本，三檔最多 266／366／466 個來源樣本，實際最多 798／1098／1398 粒；可見來源像素少時不補點。提高粒子預算只減少 CanvasData 之後的採樣損失，無法還原高解析度原稿轉 32×32 時已丟失的細節。

A/B：A 保留現有 occupancy-adaptive 分割採樣；B 使用確定性的輪廓／色彩邊界優先策略，固定 tie-break，兼顧內部填色與孤立細線，不膨脹透明區、不改 RGB、不因極低 alpha 雜點耗盡預算。每張同圖同設定比較輪廓完整度、連通性、色塊保留與內部覆蓋；不只看樣本數。

預覽與實際播放共用 quality＋strategy＋style 的顯示結果；快取鍵納入這些設定，避免切檔後預覽過期。保留預覽為 2D 示意的標示。

測試集：透明圖、單點、愛心、細對角線、多色棋盤、透明邊框、半透明圖、密集 32×32 及手繪圖。驗證每檔上限、決定性、透明無點、小圖不膨脹、比例／顏色保留、快取失效與安全 codec 回歸。

量測：同硬體／視窗／距離下每組暖機後重複 10 次；記錄準備 CPU、生成 CPU、每 tick 更新 CPU、實際 createParticle 非 null 數、isAlive 核心／尾跡分項峰值與結束後零殘留。另採樣 frame time p50/p95/p99 與配置／GC，說明取得方式；現有 hold CPU 日誌不能當 frame time 或 FPS。維持目前單效果取代／clear 規則，連續觸發驗證不累積，勿先新增多人或多效果並發。

驗收：三檔與兩策略可切換、預覽一致、全部資料安全測試與 clean build 成功、人工 A/B 與量測完成、使用者選擇後才決定正式設定。風險為過度強調邊緣、畫面空洞、生成尖峰與粒子引擎淘汰；High 不承諾所有電腦更流暢。
