# Phase 04A Review Fix 01 — 2026-10-23

Phase: 04A Review Fix 01
Status: PARTIAL（程式／自動測試修正完成；核心視覺設計已獲使用者驗收，但最新預設入口與完整生命週期尚待最終遊戲回歸）
Baseline: 1243af9（git log 已核對；開始時工作樹乾淨）
Writer: Codex 唯一寫入者；Antigravity 獨立唯讀審查。

## 審查依據與修正

專案 docs/reviews 未找到上一輪針對 1243af9 的完整唯讀審查報告；本次以使用者訊息重述的 1 Major + 2 Minor 作為修正依據，不宣稱讀過不存在的報告檔。

- Major 已確認：舊 worldPos 為測試自建公式；yaw/pitch 迴圈未驅動產品狀態；玩家位置未傳入產品；常數檢查無法攔住 Fixed 每 tick 更新。已移除這些假覆蓋。
- 抽取 DustPlane：由 HeartPrototype.start 實際呼叫 start(options, eye, look, timeline) 建立唯一世界中心，tickDust 呼叫 update(age, camera.rotation()) 決定 B/C 定位；positionDust 直接使用 position，散射速度使用 rotate。D/E update 回傳 false，position 另有階段防護。clear 釋放 plane。
- CanvasOptions 搬出原巢狀 record，內容／預設不變，避免純邏輯測試載入 Minecraft Client 入口。canvasCommand 沿用 DEFAULT，startCanvas 傳遞實際 options；heart 路徑傳 null，工廠維持 Dynamic。
- Minor：PROGRESS 改 PARTIAL 並記錄既有 commit 1243af9；DECISIONS 更正 Fixed 保證、側視透視縮短、歪斜成因未定及正式參數 150；TEST_RESULTS 更正舊測試覆蓋與人工驗收宣稱。歷史建置數字保留，不代表目前測試數。
- 沒有更動依賴、Bloom 130%、動畫時間、三粒 Dust、Plain、粒子上限、CanvasData／DustPattern／CanvasDownsampler，沒有 04B 功能。

## 真正的產品邏輯覆蓋與盲點

6 個 CameraOrientationStabilityTest 直接呼叫執行期使用的 DustPlane 工廠、update、position、rotate，不在測試自行計算世界座標：

1. DEFAULT options 經同一工廠後，20..holdEnd-1 任意變更 yaw/pitch 都不改樣本世界座標。
2. 朝向在爆炸而非 Launch 捕捉；Formation 用實際 spread 映射到已知座標。
3. 真正把玩家 eye/look 向量傳入工廠後再修改它們，驗證防禦性保存的中心／樣本不移動；後續 update 不接受玩家位置，結構上避免平移重算中心。
4. 明確 Dynamic options 與 heart 的 null 路徑在相機變更時更新，轉回時無累積漂移。
5. Fixed 與 Dynamic 在 D/E 拒絕定位，保留最後朝向供釋放速度使用。

Mutation probe：暫將生產條件 `positioning && (!initialized || !fixed)` 改為 `positioning && true`，執行相機測試 6 個中 4 個失敗、exit 1；finally 還原後 clean build 全通過。可攔下共享元件內 Fixed 被改為每 tick 更新的回歸。

剩餘盲點：JUnit 未執行 HeartPrototype 的 Brigadier 註冊、Minecraft 相機更新時機、真實玩家實體、Particle.setPos／插值／移動、音效及世界切換。產品呼叫鏈由差異檢查確認，未宣稱已用 JUnit 跑完整 Minecraft 接線。若未來入口繞過元件、錯傳 options／重建 plane，單元測試可能仍通過；需下列遊戲回歸補足。D/E 測試驗證定位規則，不能證明粒子引擎真實運動與清理。

## 驗證結果

環境：D:\minecraftmod；Java 25.0.3+9-LTS；Minecraft 26.3；Loader 0.19.5；API 0.163.0+26.3；Loom 1.17.16；Wrapper 9.6.1。
Gradle 使用程序級 GRADLE_USER_HOME=D:\minecraftmod\gradle。

| 命令 | 結果 |
| --- | --- |
| .\gradlew.bat test --no-daemon（沙箱） | exit 1，daemon 18712 連線失敗；未編譯 |
| 同命令（放寬執行限制） | exit 0，33 測試全通過，23s |
| .\gradlew.bat test --tests '*CameraOrientationStabilityTest' --no-daemon（mutation） | 預期 exit 1，6 個中 4 個失敗；已還原 |
| .\gradlew.bat clean build --no-daemon（正式實作） | exit 0，29s，7 tasks executed，33/33 通過、0 failure/error/skipped |
| java --class-path build/classes/java/main tests/Phase02TimelineCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase025DustStyleCheck.java | exit 0 PASS |
| java --class-path build/classes/java/main tests/Phase026BloomCheck.java | exit 0 PASS |
| git diff --check | exit 0；僅既有 LF/CRLF 提示 |

JUnit：CanvasTest 15、CameraOrientationStabilityTest 6、DustGeometryDiagnosticsTest 6、DustPatternTest 6。
JAR：build/libs/drawfirework-1.0.0.jar；SHA256 18B2C08BBFC132266ACB544F94A5322092335F6B31383E1F35EF2ECAA23FB1C4。
既有 Gradle 10 棄用警告仍存在，未改工具鏈。

Compiled: PASS；Unit-tested: PASS（上述範圍）；Runtime-tested: 本輪未執行；Visually verified: 核心設計先前由使用者驗收，最新版本回歸未執行；Multiplayer-tested: 未執行／超出範圍。

## 最終人工驗收（未執行）

使用本次 JAR 或既有 Wrapper runClient，單人開闊天空、粒子全部；請逐項回報：

1. `/drawfirework canvas smiley`、`grid`、`rect`、`dense`（各加完整前綴）：確認省略 fixed 時正式採 Fixed；B/C 轉 yaw/pitch、平移玩家及重新對準中心，圖案中心保持世界位置，無跟隨自轉。從側面觀看容許透視縮短。
2. `/drawfirework canvas smiley dynamic` 與 `/drawfirework heart`：確認 B/C 隨相機更新，原始愛心質感／時序無損。
3. `/drawfirework canvas smiley 150`、`/drawfirework canvas grid plain`：確認尺寸選項與 Plain 診斷仍可使用。
4. 完整觀察 A20/B8/C14/D20/E14：聲音各一次、B 形成、C 可讀、D 自由散射不跳回圖案、E 消失；D/E 持續轉頭與移動，錄影回報跳位／殘留。
5. 各階段 clear、連續替換效果、Hold/Dispersion 退出重入與切換維度／世界，確認無殘留、例外或後續重生。記錄粒子設定、場景與實際觀察，不推定效能 PASS。

Known problems: 上述遊戲回歸未完成；單元測試無法覆蓋完整 Minecraft 執行期接線。
Recommended next phase: 先完成此修正之獨立審查與人工回歸；不代表授權 04B。
## Git 收尾與一般 PowerShell 提交命令

本次 git add（明確 10 檔）exit 128：無法建立 D:/minecraftmod/.git/index.lock，Permission denied。未建立新 commit，未 push；保留所有變更，沒有重試 Git 寫入。基準 commit 1243af9 存在。

在一般 PowerShell 執行：

```powershell
Set-Location 'D:\minecraftmod'
$fixFiles = @(
  'docs/API_COMPATIBILITY.md'
  'docs/DECISIONS.md'
  'docs/PROGRESS.md'
  'docs/PROJECT_PLAN.md'
  'docs/TEST_RESULTS.md'
  'docs/reviews/phase-04a-review-fix-01.md'
  'src/main/java/com/drawfirework/client/HeartPrototype.java'
  'src/main/java/com/drawfirework/client/CanvasOptions.java'
  'src/main/java/com/drawfirework/client/DustPlane.java'
  'src/test/java/com/drawfirework/firework/CameraOrientationStabilityTest.java'
)
git diff --check
if ($LASTEXITCODE -ne 0) { throw 'diff check failed' }
git add -- $fixFiles
if ($LASTEXITCODE -ne 0) { throw 'git add failed' }
git diff --cached --check
if ($LASTEXITCODE -ne 0) { throw 'staged diff check failed' }
git diff --cached --stat
git commit -m "fix: validate runtime camera orientation and correct phase 04a records"
if ($LASTEXITCODE -ne 0) { throw 'commit failed' }
git status --short --branch
```
