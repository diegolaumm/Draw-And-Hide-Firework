# Release Candidate 01 — Draw & Hide Firework 發布指南與驗收說明

本文件為 **Draw & Hide Firework v1.0.0 (Release Candidate 01)** 的公開發行準備文件，包含發布前技術稽核結果、安裝指南、驗收程序、README 草稿與 Release Notes 草稿。

> 2026-10-26 發布整理：作者已確定為 diegolaumm、授權為 MIT；正式介紹與操作以根目錄 README.md 為準。尚無確認的專案 repo URL，contact 已省略。本頁下方歷史文案不構成外部安裝或生存／多人驗收通過；最新產物大小、SHA-256 與驗證結果見 GITHUB_PREPARATION.md。

---

## 一、發布前技術稽核報告（Pre-release Audit）

### 1. 模組元數據與依賴稽核 (`fabric.mod.json`)
- **Mod ID**: `drawfirework`（與規格完全一致）
- **版本**: `1.0.0`
- **名稱**: `Draw & Hide Firework`
- **運行環境 (`environment`)**: `*`（支援 Client 與 Server 載入）
- **進入點 (`entrypoints`)**:
  - `client`: `com.drawfirework.client.HeartPrototype`, `com.drawfirework.client.CanvasEditorClient`
  - `main`: `com.drawfirework.DrawFirework`（伺服器權威註冊與網路接收通道）
- **依賴契約**:
  - `fabricloader`: `>=0.19.5`
  - `minecraft`: `26.3`（精確相等限制；已移除原先的 `~` 範圍）
  - `java`: `>=25`
  - `fabric-api`: `*`（需要 Fabric API，專案建置基準為 `0.163.0+26.3`）
- **2026-10-26 已整理**：`authors` 為 `["diegolaumm"]`；`license` 為 `MIT`；移除 Fabric 範例 contact 及 `another-mod`，沒有臆造專案網址。

### 2. 正式產物稽核
- **JAR 檔案路徑**: `D:\minecraftmod\build\libs\drawfirework-1.0.0.jar`
- **檔案大小**: `193,996 bytes` 為 2026-10-25 歷史產物；更新授權後以 GITHUB_PREPARATION.md 的新結果為準。
- **JAR 類型**: Loom 未混淆建置的標準 `jar` 產物；本專案停用 obfuscation，不稱為 remapped JAR。

### 3. 目前功能範圍與支援模式限制
- **支援模式**:
  - **單人創造世界（Singleplayer Integrated Server）**：目前實作與既有人工驗收範圍；外部 Launcher 安裝仍待驗收。
  - **生存模式材料消耗與合成表**：**尚未實作（屬未來 Phase 07B 規劃）**。目前煙火物品僅能透過創造模式背包或測試指令取得。
  - **專用伺服器多人廣播與同步**：**尚未實作（屬未來 Phase 09–10 規劃）**。本版本為單人優先（Phase 00–08），草稿庫為本機資料庫。
- **煙火物品取得方式**：
  1. 進入單人創造模式，確保背包有空格。
  2. 聊天指令：`/drawfirework_test_item`，取得附有有效圖像 component 的物品；沒有 `give` 子指令，裸 `/give` 物品缺少必要圖像資料。
- **草稿儲存路徑與備份建議**：
  - 本機路徑：`<gameDirectory>/drawfirework/drafts/<UUID>.json`。
  - 建議玩家更換電腦或重裝遊戲時備份此目錄。

---

## 二、一般 Launcher 獨立實機安裝驗收程序

請在非開發環境（如官方 Minecraft Launcher、Prism Launcher、CurseForge 等）進行正式驗收：

1. **環境準備**：
   - Minecraft 版本：**26.3**
   - Mod Loader：**Fabric Loader 0.19.5+**
   - Java 環境：**Java 25 LTS**
   - 必備依賴：**Fabric API 0.163.0+26.3**（請放入 `mods/` 資料夾）
2. **安裝模組**：
   - 將 `D:\minecraftmod\build\libs\drawfirework-1.0.0.jar` 複製至該遊戲實例的 `mods/` 資料夾。
3. **啟動與載入檢查**：
   - 啟動 Minecraft，確認標題畫面或模組清單中成功顯示 `Draw & Hide Firework 1.0.0`。
4. **進入世界與取得物品**：
   - 建立或載入一個單人創造模式世界。
   - 執行 `/drawfirework_test_item` 取得有效畫布測試煙火。
5. **畫布編輯與繪圖操作**：
   - 主手持煙火按 **G**，確認進入**羊皮紙畫布與立體木框**編輯器。
   - 測試調色盤選色、筆刷粗細滑桿拖曳。
   - 測試 **油漆桶填色（Fill）**：先畫一個封閉圖案填滿色彩。
   - 測試 **透明區域清除（Eraser → Fill 回歸修復驗收）**：點擊橡皮擦（E）切換為透明色，再點擊油漆桶填色（F），點擊剛才的彩色區塊，確認整個連通區塊成功被清除為透明背景。
   - 測試 **Undo / Redo**：按 `Ctrl+Z` 確認填色被復原，按 `Ctrl+Y` 確認重做。
   - 測試切回 **畫筆（Brush）**：確認畫筆自動恢復最後選取的顏色。
6. **草稿儲存與載入**：
   - 點擊右側藍綠色【存稿（磁碟片）】（或按 `Ctrl+S`），命名為 `ReleaseDraft` 儲存。
   - 點擊【草稿庫（資料夾）】開啟草稿管理畫面，確認列表中存在 `ReleaseDraft` 且帶有 `[目前編輯中]` 標籤。
   - 點擊【建立新草稿】，確認畫布重設為空白。
   - 重新進入草稿庫載入 `ReleaseDraft`，確認圖稿 100% 還原。
7. **煙火輸出與空中施放**：
   - 點擊右側暖金色【轉為煙火】（火箭圖示），聊天室提示儲存成功。
   - 將世界時間調至夜間（`/time set midnight`），右鍵點擊地面發射煙火。
   - 確認空中播放清晰的五階段（上升、展開、Hold、解體、淡出）76 ticks 藝術煙火動畫，外觀保留 Balanced + Detail-Preserving + Fullbright Exact 發光質感。
8. **退出與重啟持久化**：
   - 正常儲存世界並退出遊戲，完全重啟 Minecraft。
   - 再次進入世界按 G，檢查草稿庫中的 `ReleaseDraft` 依然完好存在。

---

## 三、發布文案草稿（README / Release Notes）

### 1. `README.md` 草稿

```markdown
# Draw & Hide Firework

Custom pixel-art fireworks in Minecraft 26.3! Draw your own artwork on an in-game artist's easel, save high-resolution drafts locally, and launch vibrant 5-phase fireworks into the night sky.

## Features
- **Artist's Canvas GUI**: Warm parchment canvas texture, wooden easel frame, and clean icon-based toolbars.
- **Rich Drawing Tools**: 8-color palette, adjustable brush sizes (1–64), eraser, flood fill (including transparent clearing), and full undo/redo history.
- **High-Resolution Draft Persistence**: Store full 256×256 master drafts on your local machine with automatic backup protection and draft library management (New, Load, Save As, Rename, Delete).
- **5-Phase Artistic Firework Animation**: Deterministic 76-tick lifecycle featuring smooth formation, sharp hold, particle dispersion, and soft fade-out.
- **Self-Illuminating Dust Particles**: Fullbright exact rendering ensures fireworks remain vivid at night without needing ground torches.

## Requirements
- Minecraft Java Edition: **26.3**
- Mod Loader: **Fabric Loader >= 0.19.5**
- Dependencies: **Fabric API**
- Java Runtime: **Java 25**

## Getting Started
1. Install Fabric Loader and Fabric API for Minecraft 26.3.
2. Drop `drawfirework-1.0.0.jar` into your `.minecraft/mods` folder.
3. In a singleplayer Creative world with a free inventory slot, obtain a valid canvas firework using `/drawfirework_test_item`.
4. Hold the firework in your main hand and press **G** to open the canvas editor.
5. Draw your design, click **Save Draft** (`Ctrl+S`), then click **To Firework**.
6. Right-click the ground at night to launch your customized firework!

## Known Limitations (v1.0.0)
- **Singleplayer Only**: Firework creation and draft persistence are currently singleplayer-focused. Dedicated server multiplayer synchronization will arrive in future updates.
- **Creative Mode Item**: Survival crafting recipes are not implemented. Use `/drawfirework_test_item` for a valid item with canvas data.
```

### 2. Modrinth / CurseForge 發布說明 (Release Notes)

```markdown
### Draw & Hide Firework v1.0.0 — Release Candidate 01

Welcome to the initial release of **Draw & Hide Firework** for Minecraft 26.3!

#### Highlights
- **Artist Canvas GUI**: Intuitive parchment drawing workspace with a 3D-styled wooden easel frame and streamlined icon toolbars.
- **Draft Management**: Save, rename, duplicate, and restore 256×256 high-resolution drafts directly from your game directory (`drawfirework/drafts/`).
- **Smooth 5-Phase Fireworks**: Watch your custom drawings launch, form, hold shape, disperse into sparks, and fade over 76 game ticks.
- **Fullbright Exact Glow**: Night fireworks shine clearly against the dark sky with authentic fullbright self-illumination.

#### Platform Notice (Modrinth / CurseForge)
- **Fabric Loader 0.19.5+** and **Fabric API** are required.
- **Java 25** is required for Minecraft 26.3.
- **AI Content Disclosure**: This mod's icon set and UI widgets use procedural runtime code. No generative AI images are included in the mod assets.
```
