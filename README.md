# Draw & Hide Firework

在 Minecraft 26.3 中繪製像素畫、儲存本機草稿，並播放自訂圖像煙火。
作者：**diegolaumm**。目前為 **1.0.0 / Release Candidate 01**，供單人創造模式測試；尚未完成產品 V1.0 的完整生存流程驗收。

## 目前功能

- 羊皮紙畫布與木框介面：畫筆、橡皮擦、填色、調色盤、筆刷大小、Undo / Redo。
- 本機高解析度草稿（256×256 ARGB）、草稿庫、另存、重新命名及刪除。
- 原稿轉為 32×32 煙火物品資料；預覽為示意，實際煙火另有取樣預算。
- 上升、展開、停留、解體、淡出的五階段動畫；中英文介面。

## 安裝與操作

目前建置組合為 Minecraft Java Edition **26.3**、**Java 25**、Fabric Loader **0.19.5**、Fabric API **0.163.0+26.3**。其他組合未在本次驗證。

1. 準備符合上述版本的 Fabric 遊戲實例，在該實例 `mods/` 放入 Fabric API 和 `drawfirework-1.0.0.jar`。
2. 進入單人創造世界，聊天輸入 `/drawfirework_test_item`；背包須有空格。此指令給予附有有效圖像資料的測試煙火，沒有 `give` 子指令。
3. 主手持有測試煙火，按 **G** 開啟畫布（可在按鍵設定重綁）。
4. 繪圖後按 **Ctrl+S** 或「存稿」儲存本機草稿；「草稿庫」可重新載入。
5. 按「轉為煙火」更新手持物品，關閉介面後右鍵使用以播放煙火；夜空較容易觀察。

請勿用裸 `/give` 取代上述測試指令：未附圖像 component 的物品不能直接發射。

## 草稿與備份

目前位置是遊戲實例目錄的 `drawfirework/drafts/`，不是專案原始碼目錄。舊位置 `config/drawfirework/drafts/` 會嘗試安全遷移，衝突檔保留。換電腦或重裝前請備份草稿目錄；不要上傳私人畫作與備份至 GitHub。

原稿可保留 256×256，物品／播放資料仍為 32×32；細節可能因降採樣減少。檔案系統不支援原子移動時會使用替代移動，沒有斷電安全保證。

## 已知限制與驗證狀態

- 目前 GUI 和煙火使用需要創造模式；**生存合成、材料扣除尚未實作**。
- **專用伺服器與多人同步尚未完成驗收**；`environment: "*"` 僅是載入宣告。
- Phase 06／07A 的既有文件記錄使用者遊戲內驗收；本次發布整理另執行 clean build、單元測試與 JAR 檢查，不代表新一次遊戲／視覺驗收。
- 獨立 Launcher 安裝驗收仍待完成，請依 [RC01 驗收指南](docs/RELEASE_CANDIDATE_01.md) 操作；完成前以預發行候選看待。
- 版本欄仍為 `1.0.0`，不因名稱含 RC01 就代表正式穩定版。

## 從原始碼建置（Windows PowerShell）

使用 JDK 25 與既有 Wrapper（Gradle 9.6.1／Fabric Loom 1.17.16），不要重新產生專案。首次建置需要下載依賴。

```powershell
cd D:\minecraftmod
java -version
# 本專案沿用此本機 Gradle 快取位置；只影響目前 PowerShell 程序。
$env:GRADLE_USER_HOME = Join-Path $PWD 'gradle'
.\gradlew.bat --version
.\gradlew.bat clean build --no-daemon
```

成功產物：`build/libs/drawfirework-1.0.0.jar`；JUnit 報告：`build/reports/tests/test/index.html`。發布此模組 JAR，不要發布快取、Minecraft／Fabric 依賴、世界、草稿或整個 `build/`。

測試與限制見 [TEST_RESULTS](docs/TEST_RESULTS.md)，發布前檢查見 [GITHUB_PREPARATION](docs/GITHUB_PREPARATION.md)。尚未確認本專案 GitHub repo URL，所以未提供來源／問題回報網址。

## 授權

專案原創內容採 [MIT License](LICENSE)，Copyright (c) 2026 diegolaumm。第三方檔案維持原授權，見 [THIRD_PARTY_NOTICES](THIRD_PARTY_NOTICES.md)。本專案非 Mojang 或 Microsoft 官方產品。
