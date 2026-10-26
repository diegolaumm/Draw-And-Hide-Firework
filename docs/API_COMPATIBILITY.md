## 2026-10-26 — 發布整理核對（無新增遊戲 API）

實際建置：Minecraft 26.3／Loader 0.19.5／Fabric API 0.163.0+26.3／Loom 1.17.16／Wrapper 9.6.1／Microsoft Java 25.0.3+9-LTS；clean build 成功。本輪不變更 Java、API 呼叫、SDL 輸入或渲染。

重新讀取官方入口：
- https://fabricmc.net/2026/09/15/263.html ：可讀，說明 26.3 未混淆命名及建議 Loom 1.17／Gradle 9.6.0；不因此替換既有成功建置的 9.6.1。
- https://docs.fabricmc.net/ ：首頁仍標 26.2，不作 26.3 簽章證據。
- https://fabricmc.net/develop/ ：版本表需 JavaScript；不由此猜版本。
- https://github.com/FabricMC/fabric-api 與 https://github.com/FabricMC/fabric-example-mod ：可讀入口，不以預設分支代替既有本機 26.3 依賴。

本機程式靜態核對：CanvasEditorClient 使用 G 並檢查單人／創造／主手物品；TestItemCommand 的 `/drawfirework_test_item` 無 `give` 子指令、授予含 canvas 的物品；TestFireworkItem 拒絕無效 component 及非創造模式；ClientDraftStorage 現行目錄為 `<gameDirectory>/drawfirework/drafts/`，舊 config 路徑為遷移來源。這些只用於修正文件，沒有宣稱新的遊戲實測。

## 2026-10-24 — Phase 05C Codex 獨立位元碼查核（歷史）

最終整合補記：新增專案內DustLightingMode.DEFAULT=FULLBRIGHT_EXACT，HeartPrototype初始化、resetLightingMode與null fallback共用此值。這是本地模式設定，沒有新增Minecraft／Fabric API；工具鏈及Mixin均未改動。先前runClient匯出證據仍適用於未變更的Mixin／注入設定，不宣稱最終預設版本再次跑過遊戲場景；Runtime延伸項目依使用者最新政策記Accepted Risk / Deferred Verification。

補記：本次文件Remediation以程序級mixin.debug.export／filter啟動實際runClient，本次匯出Particle.class已包含duck與handler。證實注入在該啟動環境完成，尚未驗證世界內粒子行為。證據存 docs/reviews/phase-05c-runtime-evidence.txt。指令入口核對為客戶端HeartPrototype ClientCommandRegistrationCallback；有效物品取得為伺服器TestItemCommand `/drawfirework_test_item`，要求創造玩家與空背包格；GUI為CanvasEditorClient的G鍵及單人／創造／主手／payload檢查。沒有新增API或修改程式。

本機精確 26.3 merged JAR，以 javap -p -c 查核，非完整原始碼審計；沒有新增或修改程式。版本維持 Java25.0.3、Loader0.19.5、API0.163.0+26.3、Loom1.17.16、Wrapper9.6.1。

- `Particle.protected int getLightCoords(float)`：正常分支呼叫 LightCoordsUtil.getLightCoords(level,BlockPos)，缺少 chunk 時回傳15728640。
- DustParticle／DustParticleBase／SingleQuadParticle 未覆寫此方法，符合 ParticleMixin HEAD cancellable 注入目標。
- `SingleQuadParticle.public void setColor(float,float,float)` 存在；Dust 建構子進行隨機暗化，tick 未再次修改 RGB。
- `SimpleAnimatedParticle.public int getLightCoords(float)` 固定15728880，與本模組 fullbright 常數一致。
- client Mixin 設定與 defaultRequire=1 靜態核對；clean build及123測試通過不等同遊戲注入驗證。

更多證據、SHA256、限制及審查項目見 [phase-05c-review.md](reviews/phase-05c-review.md)。尚無其他模組相容性或專用伺服器實測保證。

## 2026-10-24 — Phase 05B.1 API查核（歷史）

版本與JVM設定均未修改：Minecraft26.3、Java25.0.3、Loader0.19.5、API0.163.0+26.3、Loom1.17.16、Wrapper9.6.1。

首次修改前重讀官方五入口：
- https://fabricmc.net/2026/09/15/263.html （可讀26.3公告，原生未混淆命名及遷移背景）
- https://docs.fabricmc.net/ （當前首頁標26.2，不當作26.3簽章依據）
- https://fabricmc.net/develop/ （版本列表需JavaScript，未以此推定依賴）
- https://github.com/FabricMC/fabric-api
- https://github.com/FabricMC/fabric-example-mod （兩個GitHub入口可讀，預設分支不取代本機版本）

本機來源仍為 `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar`。沒有可用對應Minecraft sources JAR；本次使用javap及javap -c核對指定實作，**不是完整原始碼審計**。全Java source以本機依賴直接javac通過；Gradle正式建置因daemon連線失敗而BLOCKED。

- `DynamicTexture(String,int,int,boolean)`、`getPixels():NativeImage`、`upload()`。
- `NativeImage.setPixel(int,int,int)` bytecode會呼叫ARGB.toABGR，輸入是ARGB，沒有手動再交換紅藍。
- `TextureManager.register(Identifier,AbstractTexture)`、`release(Identifier)`；release先移除map再safeClose，Screen.removed使用release，避免只丟失Java參照而洩漏GPU資源。
- `GuiGraphicsExtractor.blit(Identifier,int,int,int,int,float,float,float,float)`：bytecode委派GUI_TEXTURED及innerBlit，參數排列x1,y1,x2,y2,u1,u2,v1,v2。本次使用0,1,0,1整張UV。
- `GuiGraphicsExtractor.textWithWordWrap(Font,FormattedText,int,int,int,int)`用於原稿警語；既有fill／centeredText及Screen.extractRenderState沿用。
- `AbstractSliderButton(int,int,int,int,Component,double)`、protected value、applyValue／updateMessage；原生鍵盤／滑鼠控制沿用。
- MouseButtonEvent與InputConstants.MOUSE_BUTTON_LEFT沿用05A已核對的SDL事件契約；FreehandInput不硬編碼0、不二次乘DPI。
- 沒有新增網路API、codec、component、世界渲染或SDL文字輸入；真實Screen、紋理上傳／回收及GUI Scale行為仍待Minecraft人工驗收。
## 2026-10-24 — Phase 05A 滑鼠左鍵契約更正（最新）

本機同一merged26.3 JAR，以javap -constants／-c／-c -p核對，非完整原始碼審計；版本及工具鏈沒有變更。本次問題不需要新的API、Mixin或依賴。

- InputConstants.MOUSE_BUTTON_LEFT=**1**、MIDDLE=2、RIGHT=3。上一版Screen用button==0屬產品錯誤；先前「不使用舊GLFW mouse數值」敘述不完整，實際三處仍遺漏0，已更正為具名LEFT。
- MouseButtonEvent.button() bytecode回傳buttonInfo().button()，MouseButtonInfo.button()回傳本身欄位，沒有減1。MouseHandler.onButton把該info放入event傳給Screen；移動時用activeButton建立event呼叫Screen.mouseDragged。
- ContainerEventHandler.mouseClicked／mouseReleased／mouseDragged及AbstractWidget.isValidClickButton bytecode均將左鍵與iconst_1比較；原生顏色按鈕因此可作用，但原自訂畫布==0分支不會執行。
- MouseHandler.getScaledXPos(Window,double)使用x*getGuiScaledWidth()/getScreenWidth()；Y同理。點擊及拖曳都用縮放後event座標；Screen繪圖／命中共用left／top／cell，所以本次無證據要求再乘GUI Scale或更改座標公式。
- Screen三個override簽章及boolean參數正確，無需替換；只委派新CanvasEditorInput，真左鍵畫布內事件return true，其他事件回super維持原生按鈕處理。測試真實MouseButtonEvent／MouseButtonInfo不需啟動Screen或Minecraft registry。
- 新4tests用舊==0時全部FAIL，改具名LEFT後全測70PASS與clean buildPASS；證明輸入處理器對本機事件契約正確，仍不代表完整OS→Screen→render遊戲路徑已實測。

官方五入口已於本階段初次修改前讀取（歷史紀錄見下）；本輪API問題直接依本機26.3二進位契約定位，沒有改用26.2文件或上游預設分支推測。

## 2026-10-23 — Phase 05A GUI／KeyMapping／C2S核對

版本全數維持：Minecraft26.3、Java Microsoft25.0.3+9-LTS、Loader0.19.5、FabricAPI0.163.0+26.3、Loom1.17.16、Wrapper9.6.1。最終clean build及66JUnit PASS；GUI／網路runtime未執行。

查核來源：既有 `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar`，Fabric `fabric-key-mapping-api-v1/2.0.8+3434d6d95d`、`fabric-networking-api-v1/6.3.10+5c1fee4d5d`、`fabric-lifecycle-events-v1/4.1.9+ffef5f675d`。先查本機快取，沒有相應sources JAR；以javap、javap -c與最小實作編譯確認，非完整原始碼審計。

- `Screen(Component)`、`init()`、`extractRenderState(GuiGraphicsExtractor,int,int,float)`；`GuiGraphicsExtractor.fill(int,int,int,int,int)`、`centeredText(Font,Component,int,int,int)`。本機不存在舊GuiGraphics類別。Screen.extractRenderState bytecode只遍歷renderables，故先畫自訂網格後呼叫super不會被另一張背景覆蓋。
- `mouseClicked(MouseButtonEvent,boolean)`、`mouseDragged(MouseButtonEvent,double,double)`、`mouseReleased(MouseButtonEvent)`；event.x()/y()/button()。Screen.keyPressed的bytecode在Escape呼叫onClose；onClose呼叫Minecraft.gui.setScreen(null)，產品沿用原生Esc取消，不使用GLFW key／mouse數值。
- `Minecraft.gui`是Gui，`Gui.screen()`／`Gui.setScreen(Screen)`；`LocalPlayer.sendSystemMessage(Component)`、`sendOverlayMessage(Component)`。首輪漏查舊Minecraft.screen/setScreen/displayClientMessage導致5編譯錯誤，核對後全部改為此實際API，最終編譯通過。
- `KeyMapping(String,InputConstants.Type,int,KeyMapping.Category)`；`InputConstants.Type.KEYBOARD`、`InputConstants.KEY_G=10`（javap -constants，SDL scancode，非舊GLFW G值）；`KeyMapping.Category.CREATIVE`、`consumeClick()`。`net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(KeyMapping)`；bytecode委派KeyMappingRegistryImpl，實際控制選單可見／改綁仍待遊戲驗收。沒有文字輸入框，本階段不使用／不宣稱測過TextInputManager。
- 原生Button.builder(Component,Button.OnPress).bounds(...).build，addRenderableWidget沿用Screen。無自訂渲染管線／Mixin。
- `PayloadTypeRegistry.serverboundPlay()/clientboundPlay().register`、`ServerPlayNetworking.registerGlobalReceiver(Type<T>,PlayPayloadHandler<T>)`、`Context.server()/player()`；明確ctx.server().execute後讀玩家與寫stack。ServerPlayNetworkAddon.receive bytecode把payload交給handler，schedule使用server.execute；不把callback執行緒當未驗證假設。
- `ClientPlayNetworking.canSend(Type)`／send(payload)／registerGlobalReceiver、context.player/client與client.execute沿用既有API並本次編譯確認。Opened與Save各自有正確CustomPacketPayload.Type，傳相同有界canvas bytes，沒有PacketCodec或Yarn命名。
- `Inventory.getSelectedSlot()`、`ServerPlayer.hasDisconnected()`、`ItemStack.copy/getCount/isSameItemSameComponents`簽章已javap；既有getMainHandItem、Canvas component get/set、setChanged及containerMenu.broadcastChanges本次編譯確認。實際同物件及完整stack比較在runtime接線，非JUnit Minecraft registry測試。
- `ServerTickEvents.END_SERVER_TICK`、`ServerLifecycleEvents.SERVER_STOPPED`、`ServerPlayConnectionEvents.DISCONNECT(handler,server)`本次編譯確認；清理session狀態，不影響既有client粒子清理。
- ItemCanvasCodecs／CanvasData／persistent schema無改動。4116bytes外層專用payload上限、32×32及1024pixels、metadata先驗／尾隨拒絕由新產品codec JUnit確認；原codec與playback回歸全PASS。

首次修改前重新開啟五個官方來源：[26.3遷移公告](https://fabricmc.net/2026/09/15/263.html)、[Fabric docs](https://docs.fabricmc.net/)、[develop](https://fabricmc.net/develop/)、[Fabric API](https://github.com/FabricMC/fabric-api)、[example](https://github.com/FabricMC/fabric-example-mod)。公告可讀；docs首頁仍26.2；develop版本連結需JavaScript；GitHub入口可讀。它們不能取代本機精確26.3簽章／實作證據，沒有套範本或更新依賴。SDL輸入只核對本階段使用的scancode／event／keymapping，未宣稱完成整個SDL後端審計。

## 2026-10-23 — Phase 04C.2 本機 API 驗證

版本沿用26.3/Java25/FabricAPI0.163.0+26.3，無依賴變更。本機同一 merged26.3 JAR及 fabric-networking-api-v1 6.3.10+5c1fee4d5d，以 javap/javap -c -p 查核，非完整原始碼審計。

已編譯確認：PayloadTypeRegistry.clientboundPlay().register；ServerPlayNetworking.canSend/send(ServerPlayer,payload)；ClientPlayNetworking.registerGlobalReceiver，Context.player/client；ClientPlayConnectionEvents.DISCONNECT(handler,client)。接收程式不依賴 callback 執行緒假設，明確 client.execute 後檢查世界／player。

ResourceKey.identifier()、Identifier.tryParse/fromNamespaceAndPath、CustomPacketPayload.Type(Identifier)、FriendlyByteBuf UTF上限及double/long讀寫均編譯與產品測試確認。初次測試發現 createType(String) 使用 withDefaultNamespace，完整 drawfirework: ID 因冒號無效；已改明確 Type(Identifier)，最終53測試PASS。首次兩次測試失敗均是同一修正前版本，非 registry bootstrap；未掩蓋失敗。Minecraft原生未混淆名稱為準，不使用舊 PacketCodec/playS2C。

04C.1遊戲API／ItemStack驗收已由使用者確認PASS；04C.2真實網路與client runtime仍未執行。未新增SDL或渲染API、未改工具鏈。

## 2026-10-23 — Phase 04C.1 實際編譯與邊界核對

版本不變：Minecraft 26.3、Java Microsoft 25.0.3+9-LTS、Fabric Loader 0.19.5、Fabric API 0.163.0+26.3、Loom 1.17.16、Wrapper 9.6.1。本次 clean build 與 47 JUnit 通過；不將編譯宣稱為遊戲 registry／物品往返通過。

本機查核來源：`.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar`；Fabric command API `fabric-command-api-v2/3.1.2+fcdff87f5d` JAR；DFU 10.0.21。沒有相應 sources JAR，以 javap／javap -c -p 查簽章及以下指定 bytecode，不冒稱完整原始碼審計。

- DataComponentType.builder/persistent(Codec<T>)/networkSynchronized(StreamCodec<? super RegistryFriendlyByteBuf,T>)/build；BuiltInRegistries.DATA_COMPONENT_TYPE、ITEM；Registry.register，Identifier.fromNamespaceAndPath。
- Item.Properties.setId(ResourceKey<Item>)/stacksTo；ResourceKey.create(Registries.ITEM,Identifier)。Item 建構 bytecode 呼叫 itemIdOrThrow，先設 key 再 new Item。
- ItemStack(ItemLike)、set/get/remove component、copy、is(Item)、CODEC／STREAM_CODEC；RegistryOps.create(DynamicOps,HolderLookup.Provider)、RegistryFriendlyByteBuf(ByteBuf,RegistryAccess)。編譯確認存在；真正 ItemStack 遊戲診斷仍待人工執行。
- DFU Codec.string(min,max)、Codec.validate；NbtOps／JsonOps 產品 boundedCodec 往返 JUnit 通過。字串長度限制與精確 pixels 長度都在 pixel int[] 配置前執行。
- StreamCodec.of、read/write byte、read/write int；產品測試驗證 32x32 編碼為 4099 bytes、保留後續欄位，無引入 PacketCodec 或 payload 註冊。
- CommandRegistrationCallback.register(CommandDispatcher<CommandSourceStack>,CommandBuildContext,Commands.CommandSelection)；Commands.literal、CommandSourceStack.getPlayer/getPlayerOrException、Player.isCreative、Inventory.getFreeSlot/setItem/setChanged、containerMenu.broadcastChanges，編譯確認。命令執行於伺服器，不使用 client command 授予。
- Item.use(Level,Player,InteractionHand):InteractionResult；Level.isClientSide()；Player.sendSystemMessage；SUCCESS_SERVER/PASS/FAIL，編譯確認。ItemStack.use bytecode 確認委派 Item.use；server creative inventory packet handler 先 ensureRunningOnSameThread，再 hasInfiniteMaterials 檢查。不是自訂封包授權替代品，04C.1 沒有自訂 C2S。
- Bootstrap.bootStrap 會呼叫 BuiltInRegistries.bootStrap，後者 createContents 後 freeze；普通 JUnit 提前觸碰 registry 會 Not bootstrapped。失敗紀錄見 TEST_RESULTS，不以反射解凍繞過生命週期。
- 本機 JAR assets/minecraft/items/firework_rocket.json 實際為 model type minecraft:model 指向 minecraft:item/firework_rocket；新增 test_firework 資源沿用此模型。實際外觀仍未驗收。

首次修改前重開官方五入口：[26.3 公告](https://fabricmc.net/2026/09/15/263.html)、[docs](https://docs.fabricmc.net/)、[develop](https://fabricmc.net/develop/)、[Fabric API](https://github.com/FabricMC/fabric-api)、[example](https://github.com/FabricMC/fabric-example-mod)。公告可讀、docs 標 26.2、develop 版本矩陣需 JS、GitHub 入口可讀；不把預設分支或 26.2 文件當本機 26.3 證據。未新增 SDL／渲染 API。

## 2026-10-23 — Phase 04B 專案介面

新增專案介面 DevCanvases.SmileyPalette（ORIGINAL、WARM、COOL）及 public static CanvasData smiley(SmileyPalette)，null 以 NullPointerException 拒絕；既有 smiley()、CanvasData、DustPattern、CanvasOptions、DustPlane 簽章不變。沒有新增 Minecraft／Fabric／Brigadier API 呼叫；literal 與 supplier 接線沿用已驗證程式，新增分支的遊戲操作仍待驗收。

所有版本不變：Minecraft 26.3、Java 25.0.3+9-LTS、Fabric Loader 0.19.5、Fabric API 0.163.0+26.3、Loom 1.17.16、Wrapper 9.6.1。未新增 SDL、註冊、封包或 StreamCodec，也未宣稱重新完成 Minecraft 原始碼審計。

首次修改前重新閱讀官方五入口：[26.3 公告](https://fabricmc.net/2026/09/15/263.html)、[文件](https://docs.fabricmc.net/)、[develop](https://fabricmc.net/develop/)、[Fabric API](https://github.com/FabricMC/fabric-api)、[example](https://github.com/FabricMC/fabric-example-mod)。公告可讀；文件首頁仍為 26.2；develop 版本連結需 JavaScript；GitHub 入口可讀。上游預設分支不作精確本機 API 的替代證據。本次新增的純 Java enum／產生器由編譯與直接呼叫產品程式的 JUnit 核對；結果見 TEST_RESULTS。

## 2026-10-23 — Phase 04A Review Fix 01 相容性

版本與依賴不變；沒有新增 Minecraft／Fabric API。產品使用既有 camera.rotation()、Particle.setPos/setParticleSpeed；只將 JOML 朝向／座標與狀態規則抽至 DustPlane，CanvasOptions 從巢狀 record 搬出以避免純邏輯測試依賴 HeartPrototype。簽章以本次編譯與 JUnit 驗證，未宣稱完成 Minecraft 原始碼審計。

首次修改前重開五個官方入口：26.3 公告可讀（原生未混淆命名與 Loom/Gradle 建議）；docs 首頁仍顯示 26.2；develop 版本矩陣需 JavaScript；API 與 example 頁可讀。它們不替代精確本機依賴證據。未新增 SDL 輸入、渲染事件或封包功能。

## 2026-10-23 — Phase 04A Dust 尺寸查核

版本不變（Minecraft 26.3、Java 25.0.3、Loader 0.19.5、Fabric API 0.163.0+26.3、Loom 1.17.16、Wrapper 9.6.1）。本輪只新增使用既有已驗證 API `new DustParticleOptions(int rgb, float scale)`。以 `javap -c` 查本機 `minecraft-merged-deobf-26.3.jar` 的 `ScalableParticleOptionsBase(float)`：建構時 `Mth.clamp(scale, 0.01f, 4.0f)`（`MIN_SCALE`/`MAX_SCALE`）。04A 尺寸範圍 0.42..1.2，未觸及 clamp。無新事件、註冊或網路 API。

## 2026-10-23 — Phase 03 Codec / JSON 查核

沿用Minecraft26.3、Java25.0.3+9-LTS、Loader0.19.5、FabricAPI0.163.0+26.3、Loom1.17.16、Wrapper9.6.1。build.gradle僅加testImplementation JUnit4.13.2（transitive hamcrest-core1.3）與test設定，不打入模組JAR。

本機無相關sources JAR，先查快取，再以javap及javap -c核對實際依賴二進位，不冒稱完整原始碼閱讀；已編譯及執行15個JUnit測試：

- DFU：`gradle/caches/modules-2/files-2.1/com.mojang/datafixerupper/10.0.21/b6b2ae770c02e0c1eb90f9985b151e9085a38d0b/datafixerupper-10.0.21.jar`。
- `Codec<A>.flatXmap(Function<? super A, ? extends DataResult<? extends S>>, Function<? super S, ? extends DataResult<? extends A>>)`；RecordCodecBuilder.create/group/fieldOf/forGetter/apply實際編譯確認。
- `PrimitiveCodec<A>.<T>read(DynamicOps<T>,T): DataResult<A>`、`write(DynamicOps<T>,A): T`；DynamicOps.getNumberValue/createInt簽章確認。EXACT_INT實測拒絕1.5、4294967297、1e10000000。
- DataResult.success/error(Supplier<String>)/getOrThrow/isError、Codec.encodeStart/parse於JsonOps.INSTANCE往返測試通過。JsonOps.getNumberValue bytecode在未壓縮模式要求數字JsonPrimitive，壓縮模式可接受字串；測試用未壓縮模式。
- Gson2.14.0的JsonReader(Reader)、setStrictness(Strictness.STRICT)、begin/endObject、nextName/nextString/peek由javap核對；有界streaming JSON解析不skip未知巢狀資料。
- 沒有StreamCodec/封包或Data Component註冊；使用者允許延後。Phase04針對受限顯示格式另查ByteBuf/StreamCodec與配置限制，不傳完整草稿。

首次修改前重開五個入口：[26.3公告](https://fabricmc.net/2026/09/15/263.html)、[docs](https://docs.fabricmc.net/)、[develop](https://fabricmc.net/develop/)、[API](https://github.com/FabricMC/fabric-api)、[example](https://github.com/FabricMC/fabric-example-mod)。公告可讀，docs仍顯示26.2，develop需JS版本矩陣；上游預設分支未作本機精確API證據。本輪不使用SDL/GUI/新渲染API，不擴大聲稱已查核其實作。

## 2026-10-23 — Phase 02.6 size/scale再確認

本機相同merged26.3 JAR，javap -c / -c -p重新查核SingleQuadParticle.scale(float)、Particle.scale(float)/setSize(float,float)，不是猜測舊API或完整原始碼審計。SingleQuadParticle.scale會quadSize *= argument，再呼叫Particle.scale；後者setSize(.2f*argument,.2f*argument)更新bounding box，不直接改x/y/z。getQuadSize仍沿用DustParticleBase既有age/lifetime的初期放大。本輪只傳新size/上次size，不改圖像世界座標或DustParticleOptions的scale1.2。原版API亦影響粒子碰撞盒，並非獨立render-only setter；無反射或Mixin。清理/生命週期/顏色API無變化。clean build及純邏輯測試通過；Bloom畫面待測。

## 2026-10-23 — Phase 02 本機26.3查核

保持既有所有版本。本機無sources JAR，使用同一merged26.3 JAR的javap -p / -c -p及clean build核對，屬二進位實作查核，不冒充完整原始碼審計。

- Particle.setLifetime(int) / getLifetime()公開；覆寫自然壽命為80 tick。Particle.setParticleSpeed(double,double,double)、scale(float)、remove/isAlive沿用原生API。SingleQuadParticle.scale(float)覆寫並控制quad大小。
- DustParticleBase建構子bytecode設定friction=.96、隨機lifetime；tick呼叫super並setSpriteFromAge。getQuadSize依age/lifetime初期放大，getLayer回傳SingleQuadParticle.Layer.OPAQUE。本方案不以alpha淡出；SingleQuadParticle.setAlpha(float)為protected，未使用。
- ClientLevel.playLocalSound(double,double,double,SoundEvent,SoundSource,float,float,boolean)公開。SoundEvents.FIREWORK_ROCKET_LAUNCH及FIREWORK_ROCKET_BLAST為SoundEvent；每事件一次，不呼叫世界explode。
- GameRenderer.mainCamera()可能缺失時取消效果；B/C取rotation，D/E只使用最後hold朝向的向量，不再依相機改軌跡。世界引用變動於END_CLIENT_TICK/END_LEVEL_TICK防禦清理。
- 首次修改前重新開啟五個官方入口：26.3公告、docs、develop、fabric-api及example-mod（連結見下方既有來源）。公告可讀；docs仍顯示26.2；develop靜態頁未提供版本矩陣；未用上游預設分支推測API。沒有新增SDL/GUI輸入API。新呼叫以本機簽章及成功編譯為證，畫面與聲音尚未運行驗證。

# API 與依賴相容性

## 2026-10-23 — Phase 01 收尾補充

使用者已在單人世界確認dust與image指令均正常顯示；Dust視覺獲接受、Image僅留對照。這補充下方先前「渲染待觸發」紀錄，不新增API相容性推論。本次沒有改動程式、API簽章或依賴版本。clear尚未實測，啟動0xC0000005根因未知，不能據此歸因任何API或Dust實作。

## 2026-10-23 — Phase 01 使用的 API

版本完全沿用：Minecraft26.3 / Java25.0.3 / Fabric API0.163.0+26.3 / Loader0.19.5 / Loom1.17.16 / Wrapper9.6.1。
本機沒有 sources JAR；以既有 `.gradle/loom-cache/minecraftMaven/.../26.3/` merged JAR與`gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/`二進位的 javap / javap -c -p 核對簽章及必要方法實作，並 clean build。不是完整原始碼審計。

- ClientCommandRegistrationCallback.EVENT：register(CommandDispatcher<FabricClientCommandSource>, CommandBuildContext)。使用 Brigadier LiteralArgumentBuilder<FabricClientCommandSource>，本版未找到舊 ClientCommandManager。
- ClientTickEvents.END_LEVEL_TICK：onEndTick(ClientLevel)；END_CLIENT_TICK用於世界更換後清理。
- DustParticleOptions(int,float)；ParticleEngine.createParticle(ParticleOptions,double,double,double,double,double,double) 回傳Particle，可為null；Particle.setPos / setParticleSpeed / remove / isAlive。
- DustParticle / DustParticleBase建構子及getQuadSize/tick的bytecode確認：隨機色彩、壽命與sprite更新，不能推定原版dust有固定hold。
- LevelRenderEvents.COLLECT_SUBMITS：collectSubmits(LevelRenderContext)。Fabric LevelRendererMixin.afterCollectSubmits確實呼叫事件；context.levelState().cameraRenderState 提供pos / orientation，worldPartialTicks用於形成插值。
- OrderedSubmitNodeCollector.submitCustomGeometry(PoseStack, RenderType, SubmitNodeCollector.CustomGeometryRenderer)，callback render(PoseStack.Pose, VertexConsumer)。捕捉當frame pose與spread，不在延遲callback讀遊戲世界。
- PoseStack.translate(double,double,double)、rotate(Quaternionfc)；不是舊mulPose(Quaternion)。GameRenderer.mainCamera()；不是getMainCamera()。
- RenderTypes.debugQuads()：RenderType建立時使用RenderPipelines.DEBUG_QUADS，sortOnUpload與OIT管線；pipeline為無背面裁切的position/color quad。VertexConsumer.addVertex(Pose,float,float,float).setColor(int)。只用既有管線，不註冊shader。
- Minecraft.hasSingleplayerServer() 限制原型入口；common初始化沒有引用client類別，metadata僅client entrypoint載入原型。

首次編譯發現未先核對的舊輔助API名稱（command builder、camera accessor與PoseStack rotation），已依上述簽章修正；最終clean build通過。實際圖像渲染仍待世界內觸發驗證。

官方五個指定入口於本次重開：26.3公告 https://fabricmc.net/2026/09/15/263.html 、https://docs.fabricmc.net/ 、https://fabricmc.net/develop/ 、https://github.com/FabricMC/fabric-api 、https://github.com/FabricMC/fabric-example-mod 。docs首頁仍標26.2；develop靜態頁無精確矩陣，上游預設分支未作本機簽章證據。SDL運行另見本次runClient log；本階段不使用鍵盤/文字輸入API。

查證日期：2026-10-21（Asia/Taipei）。本階段無新增遊戲 API 呼叫。

| 元件 | 設定/實際證據 | 結果 |
| --- | --- | --- |
| Minecraft | gradle.properties 26.3；Loom 合併 JAR version.json id/name 26.3、stable true、java_version 25、build_time 2026-09-15T11:20:48+00:00 | 既有依賴與 clean build 通過；不是 snapshot |
| Java | Microsoft OpenJDK 25.0.3+9-LTS；JAVA_HOME C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot | java -version、Wrapper launcher/daemon JVM 一致 |
| Gradle | 既有 Wrapper 9.6.1；revision 309d128bd9fe8c0b71311878fc660b9cbaa07c51 | --version、build、clean build 通過 |
| Loom | net.fabricmc.fabric-loom 1.17.16，建置輸出同值 | 穩定版設定；無改用候選 snapshot |
| Loader | net.fabricmc:fabric-loader:0.19.5 | 現有依賴建置通過；未遊戲啟動 |
| Fabric API | net.fabricmc.fabric-api:fabric-api:0.163.0+26.3 | 現有依賴建置通過；未驗證每個模組 API |

既有 build.gradle 使用 implementation、Java toolchain/release 25；gradle.properties 啟用 fabric.loom.disableObfuscation=true。原樣驗證，沒有新增 Yarn mappings。

## 本機簽章

以 Java 25 javap 讀取既有二進位依賴，非完整原始碼審計：

- Loader 0.19.5：`net.fabricmc.api.ModInitializer` 的 `public abstract void onInitialize()`；現有入口實作此方法，編譯通過。
- `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-7e9a32a5b8/26.3/minecraft-merged-7e9a32a5b8-26.3.jar` 含原生命名 `net.minecraft.client.Minecraft`、`com.mojang.blaze3d.platform.InputConstants`、`TextInputManager`。
- `TextInputManager`：建構子 `(Window)`；`setTextInputArea(int,int,int,int)`、`startTextInput(Object)`、`stopTextInput(Object)`、`stopTextInput()`、`onTextInputFocusChange(Object,boolean)` 均回傳 void。

未取得/閱讀完整 26.3 Minecraft 原始碼；SDL 後端實作、完整渲染管線、GUI/Codec/StreamCodec/註冊/網路簽章尚未驗證。官方公告的示例不可直接取代本機簽章，後續使用前須查確切依賴實作及最小編譯。

## 官方來源與限制

本次五個指定入口均可讀取，未下載範本或替換專案：

1. https://fabricmc.net/2026/09/15/263.html — 26.3 公告，提供未混淆命名、Loom 1.17/Gradle 9.6.0/Loader 0.19.5 建議，描述 SDL 及渲染遷移；公告不是全部 API 的相容性保證。
2. https://docs.fabricmc.net/ — 讀取時首頁版本顯示 26.2，不能當作 26.3 實作證據。
3. https://fabricmc.net/develop/ — 開發入口，版本欄需要 JavaScript；靜態頁未提供本次完整精確版本矩陣。
4. https://github.com/FabricMC/fabric-api — 上游 repository 入口可讀，未將預設分支當作本機 0.163.0+26.3 原始碼。
5. https://github.com/FabricMC/fabric-example-mod — 上游範例入口可讀，沒有套用其範本；未將預設分支當作 26.3 簽章證據。

本次確認的是既有快取環境可建置，不是空快取下載、上游每個 artifact 的完整性或遊戲運行驗證。
