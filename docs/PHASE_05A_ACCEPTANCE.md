# Phase 05A — 人工驗收與固定審查交付

Status: **Phase 05A — PASS / CLOSED**

## 正式結案（2026-10-24）

功能提交：`5114606`。使用者已確認clean build PASS、JUnit 70/70 PASS及Phase 02三組回歸PASS；實機主要流程可繪圖、儲存、右鍵播放自製煙火及clear，取消、重進世界、快捷鍵與舊功能操作亦回報基本正常。獨立唯讀審查Critical 0、Major 0、Minor 0；GUI滑鼠事件修復、C2S資料驗證及伺服器授權、token／防重放／ItemStack狀態保護、不同尺寸畫布程式碼及單元測試均PASS。因此正式結案為**Phase 05A — PASS / CLOSED**。

以下情境未經實機驗證，作為本階段接受的剩餘風險；單元測試與審查支持不等於遊戲內PASS：編輯中途由外部強制切換Survival、編輯中途由外部換手持物、編輯器開啟超過12,000 ticks後儲存、特殊矩形畫布的實際遊戲內呈現及儲存。

偶發JVM啟動閃退仍為非阻塞待追蹤事項，根因未確認；沒有證據發現其直接關聯Phase 05A，也沒有證據證明與模組無關。未宣稱問題已修復。

以下2026-10-24 GUI修復章節記錄當時等待人工重測的狀態，現已由本結案紀錄取代；其技術調查與歷史測試結果仍保留。

## 2026-10-24 — GUI繪圖阻塞修復與最短重測（歷史狀態；已由正式結案取代）

使用者已確認修復前GUI能開啟，但點選顏色後無法繪圖，因此當時Phase05A **BLOCKED**。其他人工測試沒有執行，不能視為PASS或FAIL。以下為已完成程式修復、尚待實際遊戲重測的狀態；本頁後續66測試與舊JAR資訊保留為初次交付歷史。

**根因：** 原CanvasEditorScreen.mouseClicked／mouseDragged／mouseReleased三處用 `event.button()==0`。本機26.3的 `InputConstants.MOUSE_BUTTON_LEFT=1`，MouseButtonEvent沒有減1，原生按鈕也檢查1。真左鍵跳過畫布分支，故沒有呼叫model.paint，drawing未啟動；回super時畫布也不是其child widget，無法補做繪圖。這是上一版產品判斷錯誤，不是Minecraft API故障。

**修復流程：** Screen三個事件→實際共用CanvasEditorInput→用具名LEFT檢查→有效畫布click呼叫原model.paint並啟動筆畫→drag同一路徑連續更新→左鍵release結束筆畫→extractRenderState讀同一model.pixel。工具區click回false交給原生按鈕；切工具／初始化reset筆畫。資料／渲染／取消／保存及C2S程式不變。

| 調查項目 | 程式碼證據及結論 |
| --- | --- |
| 選色 | Screen顏色callback直接設定color=COLORS[index]、tool名稱；橡皮擦color=0。沒有使用舊ItemStack顏色。原生AbstractWidget左鍵判斷為1；真正選色顯示仍需重測。 |
| 事件派送 | 本機MouseHandler把縮放座標與MouseButtonInfo傳給Screen的點擊／拖曳／放開；event.button直接取info.button。原自訂三處==0和實際LEFT=1不符，是阻斷點。 |
| 方法與回傳值 | override簽章正確；修復後只有畫布內左鍵click被消耗，其他click回super。已啟動筆畫的drag在畫布外仍消耗，release結束；不更換Screen API。 |
| GUI Scale與座標 | MouseHandler先縮放座標；繪製／命中共用left、top、cell，indexAt接受相同GUI單位。沒有源碼證據指向二次縮放，不改公式。 |
| 重疊／攔截 | controls=top+canvasHeight*cell+18，工具列位於畫布下；畫布未命中時才交回super。沒有發現按鈕覆蓋畫布。 |
| 工作畫布更新 | 原model.paint會寫私有pixels，但舊button分支使其未被真左鍵呼叫；修復後輸入處理器實際呼叫該方法。 |
| 最新資料渲染 | Screen每次extractRenderState取model.pixel，與輸入處理器持有同一model；未從ItemStack重新載入舊像素，無需改renderer。 |
| 連續拖曳 | 正確LEFT click啟動後drag進原Bresenham；出界由原model切斷筆畫，重新進入不跨區補線，外部release亦reset。 |

本輪只修改／新增以下9檔；其餘原05A未提交變更保留，未執行還原或覆寫：

```text
src/main/java/com/drawfirework/client/CanvasEditorScreen.java
src/main/java/com/drawfirework/client/CanvasEditorInput.java (new)
src/test/java/com/drawfirework/client/CanvasEditorInputTest.java (new)
docs/API_COMPATIBILITY.md
docs/DECISIONS.md
docs/PHASE_05A_ACCEPTANCE.md
docs/PROGRESS.md
docs/PROJECT_PLAN.md
docs/TEST_RESULTS.md
```

新增4個測試直接使用真MouseButtonEvent／MouseButtonInfo及原生LEFT常數呼叫Screen實際使用的CanvasEditorInput，不複製座標公式。保留舊==0時4/4 FAIL；改具名LEFT後最終 **70/70 JUnit PASS**、clean build exit0（22秒／7tasks）、Phase02三組回歸PASS、git diff --check PASS。測試涵蓋點擊紅藍色／工作副本、拖曳／外部release、透明橡皮擦／reset、工具區與四邊界及0／中／右鍵拒絕。純JUnit沒有啟動真正Screen／原生Button／OS事件，不能替代以下人工重測。

最短重測（本輪修復後全部待使用者回報）：

1. 關閉舊遊戲程序，使用既有 `.\gradlew.bat runClient` 重新啟動，以載入修復後類別；單人Creative主手持test_firework，按G。
2. 選紅色，點畫布一格；應立即紅色。切藍色／黃色再點不同格，像素與工具提示應一致。
3. 按住左鍵快速拖一條橫線與對角線；應連續。移出畫布、再移入，不應跨區補畫；在外放開後移回，不應繼續畫。
4. 選橡皮擦點及拖過已畫色格，應顯示透明棋盤格；點工具／按鈕區不得額外修改畫布。試最左上與最右下格及外側邊界，不越界、不崩潰。
5. 先按取消，重開原圖應不變；再畫一次並Esc，重開仍不變。尚勿以此推定保存／物品播放等未測項目通過。
6. 變更一次GUI Scale後重做點擊／拖曳，游標和格子應一致。回報PASS／FAIL、GUI Scale與畫布尺寸；若失敗附位置／操作及latest.log。

最新待驗收JAR：`build/libs/drawfirework-1.0.0.jar`，87743bytes，SHA256 `9DDC04327242002F56695E27F5AA3F59067A72F1F5DB6DFFD2E6E8382F8AC8A7`。HEAD仍 `527db03f671ba1e7b878009202733c302fa11158`，index空白，未commit／push。Codex完成本輪查核後停止寫入；沒有開始05B。

日期：2026-10-23（Asia/Taipei）。基準 HEAD：`527db03f671ba1e7b878009202733c302fa11158`，開始工作樹乾淨。Codex 唯一寫入；本次交付後停止寫入及背景工作，Antigravity 可唯讀審查。本文件不代表已通過人工測試。

## 操作流程

1. 在 `D:\minecraftmod` 的 PowerShell 設定 `$env:GRADLE_USER_HOME='D:\minecraftmod\gradle'`，執行 `.\gradlew.bat runClient`。使用可開指令的單人測試世界。
2. Creative，執行 `/drawfirework_test_item`，把獲得的煙火移到主手。這件已有 Smiley，開啟時應保留其原尺寸及所有像素。
3. 按 **G**。可在「選項 → 控制 → 按鍵設定 → 創造模式」找到「開啟煙火畫布編輯器」並重新指定。右鍵仍為發射。
4. 選紅／藍／黃等色，左鍵點擊或按住拖曳；工具提示顯示目前選色。橡皮擦寫入透明 ARGB=0，棋盤背景表示透明區。
5. 「取消」及 Esc 關閉並丟棄本次客戶端編輯；不傳儲存封包、不修改物品。要保存則按「儲存至煙火」。介面關閉後先顯示「已送出」，**須看到伺服器的「已成功儲存」才算成功**；拒絕、過期、限頻或通道失效都有不同回饋。
6. 再按 G 檢查保存像素。關閉後右鍵，應播放自己畫的圖，沿用原有 Fixed／Bloom130／76 ticks／三粒 Dust。
7. 測試無 Component 的新物品：`/give @s drawfirework:test_firework`，持該件按 G，應為透明 **16×16**；只有成功保存後才獲得 Canvas Component。既有合法畫布不強制變為16×16。

## 原始完整人工測試清單（目前狀態以上方修復／重測紀錄為準）

| 項目 | 操作與預期結果 |
| --- | --- |
| 開啟／重新綁鍵 | Creative 主手正確物品可開啟；空手、其他物品、副手限定、Survival 按鍵均拒絕並提示。改綁鍵仍正常，原右鍵播放不變。 |
| 繪圖／輸入／縮放 | 紅、藍、黃等點擊及快速對角拖曳連續；畫布外拖曳不繞到另一側，不在重新進入時補畫跨區直線；GUI Scale／視窗調整後格子與游標一致，按鈕／鍵盤焦點正常。 |
| 透明／原圖 | 橡皮擦出現透明格；未改動區保留既有半透明 ARGB、透明像素 RGB、尺寸及比例；1×1、1×32、32×1及32×32用合法測試物品另驗。純模型已遍歷全部1024種合法尺寸，但不等於GUI均實測。 |
| 取消／Esc | 修改後分別取消、Esc；再開仍是原圖。右鍵播放原圖，不是取消的暫存圖。 |
| 真正保存／重開 | 改成明顯不同的紅藍黃圖；點保存，聊天顯示伺服器成功；再開像素一致，右鍵播放新圖而非預設Smiley。全透明圖可保存，但播放應沒有可見核心圖案。 |
| 重載 | 保存後把物品放箱取回，退出世界、重新進入；再次開啟及右鍵，圖案仍一致。沒有寫入本機草稿檔案。 |
| Survival 儲存授權 | 先Creative開啟GUI，再用預先設置、有延遲紅石的命令方塊執行 `gamemode survival @p`；點保存應顯示拒絕，物品無變更。回Creative重新開啟可正常保存。普通Survival按G也應拒絕。 |
| 換主手／失效工作階段 | 在測試世界預先配置延遲命令方塊 `item replace entity @p weapon.mainhand with minecraft:stone`；開GUI後等換物，再保存必須拒絕，不能把stone變成煙火或加Canvas。亦測換成另一件test_firework，舊請求不可覆寫新物品。 |
| 原圖變更／重送／逾時 | 產品JUnit已直接驗證同stack但原像素改變、錯token、重送、重入及逾時不執行寫入。真實網路重送及「同一stack原值被另一來源改變」需審查者使用封包／server debugger整合測試；目前無一般GUI可觸發，明列未測。正常GUI可停留12000 server ticks（20TPS約10分鐘）後保存，應拒絕，重新開啟才可再編輯。 |
| 世界／斷線 | 編輯時離開或切換世界，舊請求不生效；重新進入可開啟。播放中clear／離線／換世界仍清除效果。 |
| 舊功能回歸 | 新授予的原始Smiley持主手執行 `/drawfirework_test_item verify`；原命令仍刻意驗證Smiley，不用編輯後圖作該命令的原始Smiley檢查。測 `/drawfirework heart`、Smiley／Warm／Cool／Grid、Fixed／Dynamic、`/drawfirework clear`；76 tick、Bloom130、聲音／預算／單效果替換維持。 |

回報每項 PASS／FAIL、畫布尺寸、GUI Scale、物品來源及相關 `run/logs/latest.log` 錯誤；不從建置推論畫質、FPS或多人行為。

## 實際呼叫路徑與安全邊界

1. `CanvasEditorClient` 的註冊 KeyMapping → END_CLIENT_TICK → 本機條件與通道檢查 → `OpenEditorPayload` C2S。
2. common `DrawFirework.onInitialize` → `ServerCanvasEditor.initialize` 註冊三個有界通道；open receiver → `ctx.server().execute` → `open`。重新驗證玩家alive／not removed／not disconnected／Creative及實際主手test_firework，檢查原component。
3. server建立隨機UUID `EditSession`，綁定真實ItemStack物件身分、主手slot、原CanvasData、原完整stack copy及dimension；只存每位玩家一個session。`editor_opened` S2C傳token與原圖（無component則16×16空圖）。client檢查捕捉世界／player、單人、主手與目前沒有Screen，再用 `client.gui.setScreen` 開啟。
4. `CanvasEditorScreen` → `CanvasEditorModel`，以防禦性複製暫存；`indexAt`／`paint`／`endStroke` 是正式輸入方法；Bresenham補齊兩次有效拖曳採樣間的格子，越界切斷筆畫。
5. Save → `model.snapshot()` → `EditorCanvasPayload`／`ItemCanvasCodecs.STREAM_CODEC` → `save_canvas` C2S。沒有在client上set ItemStack，也沒有把提交當播放事件。
6. server receiver → `server.execute` → `save` → 每玩家儲存限頻 → `EditSession.save`。重新檢查真實玩家、物品、同slot／同stack身分、count、完整components、dimension、有效期與token；`Objects.equals(original,current)`最終使用既有 `CanvasData.equals` 的尺寸及 `Arrays.equals`，不使用int[]參照相等。替換圖再次validate，禁止改尺寸；token在寫入前消耗，防重入與重送。
7. 通過後，才 `held.set(ModDataComponents.CANVAS, payload.canvas())` → `inventory.setChanged()` → `containerMenu.broadcastChanges()` → server `sendSystemMessage(saved)`。失敗不執行寫入callback，回覆拒絕，不顯示成功。
8. 保存不播放；關閉GUI後原本 `TestFireworkItem.use` → `ServerPlayback.use` → `UseGate` → `PlaybackPayload` S2C → `HeartPrototype.canvasPattern`／`activate`／`tickDust`。這些既有檔案及渲染器本輪沒有修改。

| 通道／限制 | 實際上限 |
| --- | --- |
| `drawfirework:open_editor` C2S | payload body恰1 byte，version=1；多／少／未知版本拒絕。 |
| `drawfirework:editor_opened` S2C | 與保存相同有界canvas格式，僅送本人。 |
| `drawfirework:save_canvas` C2S | body最多**4116 bytes**＝version1＋UUID16＋既有component最多4099；不含通道ID及外層Minecraft封包框架。 |
| 畫布 | 每維1..32、最多1024 ARGB int；先驗版本／尺寸／足夠bytes再配置像素陣列，沒有壓縮、JSON或無界清單；專用封包拒絕尾隨資料。編碼與產品入口再次驗證。 |
| 配置量 | 單次decoder最多1024像素；CanvasData防禦性複製會有暫時第二份int[]，不宣稱整個網路封包只有4096 bytes heap。 |
| 頻率／事件量 | 開啟與保存各自每玩家10 server ticks最多一次允許處理，互不阻礙立即保存；其他請求回限頻。每玩家最多一session，保存一次即消耗／清除；不生成任何煙火播放事件。網路解碼先於應用層限頻，限頻不代表拒絕封包零成本。 |
| 失效／清理 | 每END_SERVER_TICK偵測失效並清除session／snapshot，12000 ticks期限；斷線移除玩家狀態、SERVER_STOPPED清空，WeakHashMap value不保留player或level物件。Cancel不儲存，server端閒置session在逾時／下次開啟／斷線清除。 |

## 自動驗證與限制

- 最終 `.\gradlew.bat clean build --no-daemon`：PASS，30秒，7 tasks executed；Java25.0.3／Minecraft26.3／FabricAPI0.163.0+26.3／Loader0.19.5／Loom1.17.16／Wrapper9.6.1，全數沿用。
- JUnit **66/66**、0 failures/errors/skipped：原53＋新13。新測試直接呼叫正式CanvasEditorModel（4）、EditSession／EditorRequestGate（5）、網路payload codec（4）；原ItemCanvasCodecTest／PlaybackTest／PlaybackPlaneTest及其餘圖形測試皆通過。
- 三組Phase02 Java source-launcher回歸全部exit0；`git diff --check`結果見TEST_RESULTS最後紀錄。
- 純JUnit不啟動Screen、Minecraft registry、ServerPlayer、真實ItemStack receiver或inventory同步。`eligible`及完整stack比較的Minecraft接線由本次編譯／本機API核對支撐，**仍需上表runtime測試**；gate單測不能冒稱真人Survival／真實換物封包測過。取消測試證明模型隔離與不呼叫寫入，真實Esc／按鈕行為待驗。
- 既有Gradle棄用警告保留；不升級工具鏈。本次沒有啟動遊戲或進行畫面／聲音／效能／多人／獨立審查。
- 舊 `/drawfirework_test_item verify` 仍是原始Smiley診斷，保留其原語意；編輯後任意圖的存檔重載用本頁操作另驗。
- 無Undo/Redo、填色、尺寸切換、本機草稿、生存配方、多人廣播或新播放引擎。全透明圖可保存；Dust對半透明的既有限制與266採樣點／798核心粒子預算不變。

## 精確 Git 暫存候選檔案清單

實際index保持空白，未執行git add／commit／push。下列為本階段完整候選清單（含繪圖修復共23檔），供人工驗收後逐檔暫存；不使用 `git add .`，不含build／快取／世界。

```text
docs/API_COMPATIBILITY.md
docs/DECISIONS.md
docs/PHASE_05A_ACCEPTANCE.md
docs/PROGRESS.md
docs/PROJECT_PLAN.md
docs/TEST_RESULTS.md
src/main/java/com/drawfirework/DrawFirework.java
src/main/java/com/drawfirework/client/CanvasEditorClient.java
src/main/java/com/drawfirework/client/CanvasEditorInput.java
src/main/java/com/drawfirework/client/CanvasEditorScreen.java
src/main/java/com/drawfirework/editor/CanvasEditorModel.java
src/main/java/com/drawfirework/editor/EditSession.java
src/main/java/com/drawfirework/editor/EditorRequestGate.java
src/main/java/com/drawfirework/network/EditorCanvasPayload.java
src/main/java/com/drawfirework/network/OpenEditorPayload.java
src/main/java/com/drawfirework/network/ServerCanvasEditor.java
src/main/resources/assets/drawfirework/lang/en_us.json
src/main/resources/assets/drawfirework/lang/zh_tw.json
src/main/resources/fabric.mod.json
src/test/java/com/drawfirework/client/CanvasEditorInputTest.java
src/test/java/com/drawfirework/editor/CanvasEditorTest.java
src/test/java/com/drawfirework/editor/EditSessionTest.java
src/test/java/com/drawfirework/network/EditorPayloadTest.java
```

修改既有檔案：五份階段docs、common初始化、client entrypoint metadata及兩份語言。其餘為新增。沒有改動04C播放、Data Component格式、CanvasData／Downsampler、工具鏈或原測試。
