package vodmordia.modtabs.utils;

/**
 * Central registry of fully-qualified class names for screens (and a few items) from every
 * integrated mod. When a mod renames or restructures a class, this file is the only place
 * that needs to change — instead of grepping across 35 tab classes plus the TabsMenu switch.
 *
 * Constants are grouped by mod and named so the call site reads naturally:
 * {@code ScreenClasses.FTB_LIBRARY_WRAPPER} rather than a raw string literal.
 *
 * All values are {@code public static final String} so they remain valid as
 * {@code switch} case labels and as compile-time constants.
 */
public final class ScreenClasses {
    private ScreenClasses() {}

    // -- Vanilla ----------------------------------------------------------
    public static final String VANILLA_INVENTORY =
            "net.minecraft.client.gui.screens.inventory.InventoryScreen";
    public static final String VANILLA_ADVANCEMENTS =
            "net.minecraft.client.gui.screens.advancements.AdvancementsScreen";
    public static final String VANILLA_CONTAINER =
            "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen";

    // -- AppleSkin --------------------------------------------------------
    public static final String APPLESKIN_FOOD_STATS =
            "squeek.appleskin.client.gui.screen.FoodStatsScreen";

    // -- Ars Nouveau ------------------------------------------------------
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI =
            "com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook";
    public static final String ARS_NOUVEAU_SPELLBOOK_ITEM =
            "com.hollingsworth.arsnouveau.common.items.SpellBook";
    // Legacy / alternate names also present in TabsMenu switch
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY1 =
            "com.dhanantry.arsnouveau.client.gui.SpellBookGUI";
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY2 =
            "com.dhanantry.arsnouveau.client.gui.SpellBookScreen";

    // -- Backpacked (mrcrayfish) ------------------------------------------
    public static final String BACKPACKED_SCREEN =
            "com.mrcrayfish.backpacked.client.gui.screen.inventory.BackpackScreen";
    public static final String BACKPACKED_ITEM =
            "com.mrcrayfish.backpacked.item.BackpackItem";
    public static final String BACKPACKED_SCREEN_ALT =
            "net.backpacked.client.screen.BackpackScreen";
    public static final String BACKPACKED_FLYWHEEL_TRANSFORM =
            "com.jozufozu.flywheel.util.transform.TransformStack";

    // -- Better Advancements ----------------------------------------------
    public static final String BETTER_ADVANCEMENTS =
            "betteradvancements.common.gui.BetterAdvancementsScreen";

    // -- Body Damage (Legendary Survival Overhaul) ------------------------
    public static final String LSO_BODY_HEALTH =
            "sfiomn.legendarysurvivaloverhaul.client.gui.BodyHealthScreen";

    // -- Brassworks Missions ----------------------------------------------
    public static final String BRASSWORKS_MISSIONS_UI =
            "net.swzo.brassworksmissions.client.gui.UiScreen";

    // -- Cobblemon --------------------------------------------------------
    public static final String COBBLEMON_PARTY =
            "com.cobblemon.mod.common.client.gui.party.PartyGUI";
    public static final String COBBLEMON_PARTY_LEGACY =
            "com.cobblemon.mod.common.client.gui.PartyGUI";

    // -- Cosmetic Armor Reworked ------------------------------------------
    public static final String COSMETIC_ARMOR_GUI =
            "lain.mods.cos.impl.client.gui.GuiCosArmorInventory";

    // -- Draconic Evolution / SCGuns ---------------------------------------
    public static final String DRACONIC_EVOLUTION_GUI =
            "com.brandon3055.draconicevolution.client.gui.GuiDraconicEvolution";
    public static final String SCGUNS_ATTACHMENT =
            "com.dhanantry.scguns.client.screen.AttachmentScreen";
    public static final String SCGUNS_PASSIVE_SKILL =
            "com.dhanantry.scguns.client.screen.PassiveSkillScreen";

    // -- Eccentric Tome ---------------------------------------------------
    public static final String ECCENTRIC_TOME_SCREEN =
            "website.eccentric.tome.client.TomeScreen";
    public static final String ECCENTRIC_TOME_ITEM =
            "website.eccentric.tome.TomeItem";

    // -- FTB Library / Quests / Teams -------------------------------------
    public static final String FTB_LIBRARY_WRAPPER =
            "dev.ftb.mods.ftblibrary.ui.ScreenWrapper";
    public static final String FTB_QUESTS_QUEST_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen";
    public static final String FTB_QUESTS_QUESTION_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.QuestionScreen";
    public static final String FTB_QUESTS_QUESTS_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.QuestsScreen";
    public static final String FTB_QUESTS_QUEST_SCREEN_ALT1 =
            "dev.ftb.mods.ftbquests.client.screens.QuestScreen";
    public static final String FTB_QUESTS_QUEST_SCREEN_ALT2 =
            "dev.ftb.mods.ftbquests.client.QuestScreen";
    public static final String FTB_TEAMS_SCREEN =
            "dev.ftb.mods.ftbteams.client.gui.TeamsScreen";
    public static final String FTB_TEAMS_SCREEN_ALT1 =
            "dev.ftb.mods.ftbteams.client.screens.TeamsScreen";
    public static final String FTB_TEAMS_SCREEN_ALT2 =
            "dev.ftb.mods.ftbteams.client.TeamsScreen";

    // -- JourneyMap -------------------------------------------------------
    public static final String JOURNEYMAP_FULLSCREEN =
            "journeymap.client.ui.fullscreen.Fullscreen";

    // -- Map Atlases ------------------------------------------------------
    public static final String MAP_ATLASES_OVERVIEW =
            "pepjebs.mapatlases.client.screen.AtlasOverviewScreen";
    public static final String MAP_ATLASES_ACCESS_UTILS =
            "pepjebs.mapatlases.client.ui.MapAtlasesAccessUtils";
    public static final String MAP_ATLASES_ITEM =
            "pepjebs.mapatlases.item.MapAtlasItem";

    // -- Pufferfish Skills ------------------------------------------------
    public static final String PUFFERFISH_SKILLS =
            "net.puffish.skillsmod.client.gui.SkillsScreen";
    public static final String PUFFERFISH_SKILLS_ALT1 =
            "puffish.skillsmod.client.screen.SkillScreen";
    public static final String PUFFERFISH_SKILLS_ALT2 =
            "net.puffish.skillsmod.client.screen.SkillScreen";

    // -- Xaero's World Map ------------------------------------------------
    public static final String XAEROS_MAP =
            "xaero.map.gui.GuiMap";

    // -- Cobblemon (additional screens for tab registration) --------------
    public static final String COBBLEMON_SUMMARY =
            "com.cobblemon.mod.common.client.gui.summary.Summary";
    public static final String COBBLEMON_STARTER_SELECTION =
            "com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen";

    // -- FTB Chunks -------------------------------------------------------
    public static final String FTB_CHUNKS_MAP =
            "dev.ftb.mods.ftbchunks.client.map.MapScreen";
    public static final String FTB_CHUNKS_MAP_ALT1 =
            "dev.ftb.mods.ftbchunks.client.gui.MapScreen";
    public static final String FTB_CHUNKS_MAP_ALT2 =
            "dev.ftb.mods.ftbchunks.client.screens.MapScreen";

    // -- Memory of the Past (MOTP) ----------------------------------------
    public static final String MOTP_PLAYER_STATS =
            "tn.mbs.memory.client.gui.PlayerStatsGUIScreen";

    // -- Passive Skill Tree (daripher) ------------------------------------
    public static final String PASSIVE_SKILL_TREE =
            "daripher.skilltree.client.screen.SkillTreeScreen";

    // -- L2 family --------------------------------------------------------
    public static final String L2_ARTIFACTS_SET_EFFECT =
            "dev.xkmc.l2artifacts.content.client.tab.SetEffectScreen";
    public static final String L2_HOSTILITY_DIFFICULTY =
            "dev.xkmc.l2hostility.content.menu.tab.DifficultyScreen";

    // -- Modular Golems ---------------------------------------------------
    public static final String MODULAR_GOLEMS_INFO =
            "dev.xkmc.modulargolems.content.client.tracker.GolemInfoScreen";

    // -- RPG Crafting -----------------------------------------------------
    public static final String RPG_CRAFTING_HAND =
            "com.github.theredbrain.rpgcrafting.gui.screen.ingame.HandCraftingScreen";

    // -- Body Damage / LSO (modern path) ----------------------------------
    public static final String LSO_BODY_HEALTH_SCREENS =
            "sfiomn.legendarysurvivaloverhaul.client.screens.BodyHealthScreen";

    // -- Draconic Evolution (configurable item GUI) -----------------------
    public static final String DRACONIC_EVOLUTION_CONFIGURABLE_ITEM =
            "com.brandon3055.draconicevolution.client.gui.modular.itemconfig.ConfigurableItemGui$Screen";

    // -- Travelers Backpack -----------------------------------------------
    public static final String TRAVELERS_BACKPACK_SCREEN =
            "com.tiviacz.travelersbackpack.client.screens.BackpackScreen";

    // -- Biology Dictionary -----------------------------------------------
    public static final String BIOLOGY_DICTIONARY_HOME_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdHomeScreen";
    public static final String BIOLOGY_DICTIONARY_ABOUT_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdAboutScreen";
    public static final String BIOLOGY_DICTIONARY_CONFIG_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdConfigScreen";
    public static final String BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen";
    public static final String BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdEntityDetailScreen";
    /** {@code BiologyDictionaryItem.createBook()} / {@code isBook(ItemStack)}. */
    public static final String BIOLOGY_DICTIONARY_ITEM =
            "io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem";
    /** Static {@code openBookScreen(Minecraft)} entry point used by the keybind and right-click. */
    public static final String BIOLOGY_DICTIONARY_EVENT =
            "io.github.xienaoban.biologydictionary.client.BiologyDictionaryEvent";
    /** {@code ConfigsManager.getServer()} returns {@code Configs.ServerConfigs} which exposes {@code isBookItemRequired()}. */
    public static final String BIOLOGY_DICTIONARY_CONFIGS_MANAGER =
            "io.github.xienaoban.biologydictionary.config.ConfigsManager";

    // -- Reliable Backpacks -----------------------------------------------
    public static final String RELIABLE_BACKPACKS_ITEM =
            "com.evandev.reliable_backpacks.common.items.BackpackItem";
    public static final String RELIABLE_BACKPACKS_CONTAINER =
            "com.evandev.reliable_backpacks.common.items.BackpackItemContainer";

    // -- Quark Backpack (Quark "Oddities" addon, mod id "quark") -----------
    /** Chest-slot {@code ArmorItem} that is itself a {@code MenuProvider}. */
    public static final String QUARK_BACKPACK_ITEM =
            "org.violetmoon.quark.addons.oddities.item.BackpackItem";
    public static final String QUARK_BACKPACK_SCREEN =
            "org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen";

    // -- Wildex Bestiary --------------------------------------------------
    public static final String WILDEX_SCREEN =
            "de.coldfang.wildex.client.screen.WildexScreen";
    /** Static {@code open()} entry point, same path the mod's own keybind uses. */
    public static final String WILDEX_SCREEN_OPENER =
            "de.coldfang.wildex.client.WildexScreenOpener";
    /** {@code WildexClientConfigView.requireBookForKeybind()} respects synced server config. */
    public static final String WILDEX_CLIENT_CONFIG_VIEW =
            "de.coldfang.wildex.client.WildexClientConfigView";

    // -- Apothic Attributes -----------------------------------------------
    /** Renderable overlay attached to {@link InventoryScreen} via the mod's own ScreenEvent.Init.Post listener.
     *  Holds the static {@code wasOpen} flag we flip before opening the inventory. */
    public static final String APOTHIC_ATTRIBUTES_GUI =
            "dev.shadowsoffire.apothic_attributes.client.AttributesGui";
    /** Static {@code enableAttributesGui} field — server admins can disable the panel entirely. */
    public static final String APOTHIC_ATTRIBUTES_CONFIG =
            "dev.shadowsoffire.apothic_attributes.ALConfig";

    // -- Curios -----------------------------------------------------------
    /** Legacy curios inventory (V1). Used when {@code CuriosConfig.SERVER.enableLegacyMenu = true}. */
    public static final String CURIOS_SCREEN =
            "top.theillusivec4.curios.client.gui.CuriosScreen";
    /** Revamped curios inventory (V2). Default on modern Curios — {@code CuriosContainerProvider}
     *  picks V2 unless the legacy-menu config flag is on, so this is what most players see. */
    public static final String CURIOS_SCREEN_V2 =
            "top.theillusivec4.curios.client.gui.CuriosScreenV2";
    /** Serverbound packet with a single {@code ItemStack carried} field; the server's handler
     *  opens the curios menu for the player. Same path the mod's CuriosButton uses. */
    public static final String CURIOS_OPEN_PACKET =
            "top.theillusivec4.curios.common.network.client.CPacketOpenCurios";

    // -- The Aether ------------------------------------------------------
    /** {@code EffectRenderingInventoryScreen<AetherAccessoriesMenu>} — the accessory inventory. */
    public static final String AETHER_ACCESSORIES_SCREEN =
            "com.aetherteam.aether.client.gui.screen.inventory.AetherAccessoriesScreen";
    /** Serverbound packet with a single {@code ItemStack carryStack} field; the server's handler
     *  calls {@code serverPlayer.openMenu(...)} with an {@code AetherAccessoriesMenu}. Same path
     *  the mod's own "I" keybind uses in {@code GuiHooks.openAccessoryMenu()}. */
    public static final String AETHER_OPEN_ACCESSORIES_PACKET =
            "com.aetherteam.aether.network.packet.serverbound.OpenAccessoriesPacket";

    // -- Epic Fight ------------------------------------------------------
    /** {@code Screen} (not a container screen) opened by Epic Fight's "K" keybind via
     *  {@code ControlEngine.openSkillEditor()}. Constructor takes ({@link net.minecraft.world.entity.player.Player},
     *  {@code PlayerSkills}). */
    public static final String EPIC_FIGHT_SKILL_EDIT_SCREEN =
            "yesman.epicfight.client.gui.screen.SkillEditScreen";
    /** Static helpers: {@code getLocalPlayerPatch(LocalPlayer)} returns the patch attached
     *  via {@code EpicFightAttachmentTypes.ENTITY_PATCH}; the patch's {@code getPlayerSkills()}
     *  supplies the second {@link #EPIC_FIGHT_SKILL_EDIT_SCREEN} constructor argument. */
    public static final String EPIC_FIGHT_CAPABILITIES =
            "yesman.epicfight.world.capabilities.EpicFightCapabilities";

    // -- Epic Fight Skill Tree (epicskills addon) ------------------------
    /** {@code Screen} (not a container screen) opened by epicskills' keybind. Constructor
     *  takes a single {@code LocalPlayerPatch}. Overrides {@code render()} without calling
     *  {@code super.render()} → same plain-Screen-override quirk as
     *  {@link #EPIC_FIGHT_SKILL_EDIT_SCREEN}; add this FQN to the manual-renderables
     *  list in {@code ClientNeoForgeEvents.onScreenRenderPost} or tabs won't draw. */
    public static final String EPIC_SKILLS_SKILL_TREE_SCREEN =
            "com.yesman.epicskills.client.gui.screen.SkillTreeScreen";
    /** Epic Fight's local-player patch class — second {@link #EPIC_SKILLS_SKILL_TREE_SCREEN}
     *  constructor argument type. Note the {@code capabilites} typo (sic) in the package. */
    public static final String EPIC_FIGHT_LOCAL_PLAYER_PATCH =
            "yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch";

    // -- Completionist's Index (Fuzs, NeoForge 1.21.1) -------------------
    /** Abstract parent of {@code ModsIndexViewScreen} / {@code ItemsIndexViewScreen}.
     *  On 1.21.1 it extends {@code StatsUpdateListener} → {@code StatsScreen} (vanilla),
     *  and its {@code render()} calls {@code super.render()} so tabs added as children
     *  DO draw without needing the manual-renderables list in
     *  {@code ClientNeoForgeEvents.onScreenRenderPost}. */
    public static final String COMPLETIONISTS_INDEX_VIEW_SCREEN =
            "fuzs.completionistsindex.client.gui.screens.inventory.IndexViewScreen";
    /** Top-level index screen — constructor is {@code (Screen lastScreen, boolean fromInventory)}.
     *  Note the {@code boolean} arg is new on 1.21.1 (1.20.1 Forge only took the {@code Screen}). */
    public static final String COMPLETIONISTS_INDEX_MODS_SCREEN =
            "fuzs.completionistsindex.client.gui.screens.inventory.ModsIndexViewScreen";
    /** Drill-down screen (per-mod item list). Constructor is {@code (Screen, boolean, List<ItemStack>)};
     *  we never open it directly but track it for the "currently used" check so cycling skips
     *  the tab when the player is browsing a mod's items. */
    public static final String COMPLETIONISTS_INDEX_ITEMS_SCREEN =
            "fuzs.completionistsindex.client.gui.screens.inventory.ItemsIndexViewScreen";
    /** Static handler that adds the inventory + pause-menu buttons. On NeoForge 1.21.1 the
     *  method names are clean ({@code onAfterInventoryScreenInit} / {@code onAfterPauseScreenInit})
     *  — different from the 1.20.1 Forge build's {@code onScreenInit$Post$1/2}. */
    public static final String COMPLETIONISTS_INDEX_BUTTON_HANDLER =
            "fuzs.completionistsindex.client.handler.IndexButtonHandler";

    // -- SDM Shop (sdmshop, "sdmshopa" 3.x line, NeoForge 1.21.1) ----------
    // Built from DeusSixik/SDMShop:master (3.2.4+) — NOT the 1.21.1 branch (still on
    // v1.3.1). Note the package typo `sixk` (vs correctly-spelled `sixik` on 1.20.1's
    // 7.x line — they're parallel codebases, not a straight port). No SDMShopClient
    // openGui() helper exists; the keybind constructs a screen directly, so we mirror
    // that. ShopPage / ShopPageModern both extend FTB Library's BaseScreen → the host
    // {@link net.minecraft.client.gui.screens.Screen} is {@link #FTB_LIBRARY_WRAPPER}
    // with the shop page as the {@code wrappedGui} field.
    /** Default shop screen (when {@code ConfigFile.CLIENT.style == false}, the default).
     *  No-arg constructor, inherits {@code openGui()} from {@code BaseScreen}. */
    public static final String SDM_SHOP_DEFAULT_SCREEN =
            "net.sixk.sdmshop.shop.ShopPage";
    /** "Modern" style shop. Picked when {@code ConfigFile.CLIENT.style == true}. */
    public static final String SDM_SHOP_MODERN_SCREEN =
            "net.sixk.sdmshop.shop.modern.ShopPageModern";
    /** Static config holder — {@code CLIENT} is a public static instance and
     *  {@code style} is a public boolean. We read it reflectively to mirror the
     *  keybind's choice, and fall back to the default screen if reflection fails. */
    public static final String SDM_SHOP_CONFIG_FILE =
            "net.sixk.sdmshop.data.config.ConfigFile";

    // -- Jobs+ (DAQEM, NeoForge 1.21.1) ---------------------------------
    // JobsScreen needs server-supplied {@code JobsScreenOptions} (jobs + coins) to
    // construct, so we can't open it directly client-side. Instead we mirror the
    // mod's own keybind path in {@code EventKeyPressed}: send an empty
    // {@code ServerboundOpenJobsScreenPacket} and the server replies with
    // {@code ClientboundOpenJobsScreenPacket} that constructs and opens the screen
    // with proper data.
    /** {@code AbstractScreen} subclass (uilib library) — not a container screen.
     *  Its {@code render()} does NOT call vanilla {@code super.render()} but DOES
     *  iterate {@code uilib$getRenderables()} (a mixin accessor on {@code Screen.renderables}),
     *  so tabs added as children DO draw without needing the manual-renderables list
     *  in {@code ClientNeoForgeEvents.onScreenRenderPost}. */
    public static final String JOBS_PLUS_SCREEN =
            "com.daqem.jobsplus.client.screen.job.JobsScreen";
    /** Architectury {@code CustomPacketPayload} — no-arg constructor; server-side handler
     *  builds {@code ClientboundOpenJobsScreenPacket} from the player's jobs/coins. Registered
     *  via architectury's {@code NetworkManager} on NeoForge as a native NeoForge payload,
     *  so {@code PacketDistributor.sendToServer((CustomPacketPayload) packet)} dispatches it. */
    public static final String JOBS_PLUS_OPEN_PACKET =
            "com.daqem.jobsplus.networking.c2s.ServerboundOpenJobsScreenPacket";

    // -- Quest Log (Infernal Studios, NeoForge 1.21.1) -------------------
    // Plain {@link net.minecraft.client.gui.screens.Screen} subclass (NOT a container screen)
    // opened by the mod's own keybind via {@code Minecraft.setScreen(new QuestlogScreen(prev))}.
    // QuestlogScreen.render() calls super.render() so tabs added as children draw without
    // needing the manual-renderables list in {@code ClientNeoForgeEvents.onScreenRenderPost}.
    // The mod adds its own QuestlogOpenButton to the inventory via InventoryScreenMixin; we
    // suppress that mixin so the tab is the only entry point.
    /** {@code Screen} (single-arg {@code Screen previousScreen} constructor). */
    public static final String QUEST_LOG_SCREEN =
            "org.infernalstudios.questlog.client.gui.screen.QuestlogScreen";
    /** Static {@code mostRecentNotificationQuest} field — populated when a quest is triggered
     *  or completed, cleared by {@code QuestlogScreen.init()}. The badge ("!" icon) on the
     *  native button is shown when this is non-null, OR when any quest is completed-but-
     *  not-rewarded. We mirror both checks on the tab. */
    public static final String QUEST_LOG_CLIENT_EVENTS =
            "org.infernalstudios.questlog.QuestlogClientEvents";
    /** Static {@code getLocal()} returns the per-player {@code QuestManager}; we use it to
     *  scan for completed-but-unrewarded quests when there's no recent notification. */
    public static final String QUEST_LOG_CLIENT =
            "org.infernalstudios.questlog.QuestlogClient";

    // -- Mapwright (NeoForge 1.21.1) -------------------------------------
    // Fullscreen plain {@link net.minecraft.client.gui.screens.Screen} subclass opened by the
    // mod's own keybind ({@code IKeyMappings.Normal.OPEN_MAP}) via
    // {@code Minecraft.setScreen(new MapScreen(playerPosition))} after setting
    // {@code MapwrightClient.targetPanningPosition} to the same position.
    // MapScreen.render() calls super.render() so tabs added as children draw without needing
    // the manual-renderables list in {@code ClientNeoForgeEvents.onScreenRenderPost}; the
    // stamp-bag overlay + tool-cursor drawn after super.render() float above the tab row but
    // only intrude in the bottom/right of the screen where the tab bar isn't.
    /** {@code Screen} subclass — constructor takes a single {@code org.joml.Vector2d openingPos}.
     *  Reads {@code MapwrightClient.targetPanningPosition} on construction, so both must be set
     *  in the same open call (see {@code InputListener.tick}). */
    public static final String MAPWRIGHT_SCREEN =
            "wawa.mapwright.map.MapScreen";
    /** Public static field holder. We need {@code targetPanningPosition} (public static
     *  {@code org.joml.Vector2d}) — MapScreen's constructor lerps from {@code openingPos}
     *  toward this value, so writing both to the player's position keeps the map centered on
     *  open (matching the keybind's no-scoping / no-pins path). */
    public static final String MAPWRIGHT_CLIENT =
            "wawa.mapwright.MapwrightClient";

    // -- Field Guide (evanbones, NeoForge 1.21.1) -------------------------
    // Plain {@link net.minecraft.client.gui.screens.Screen} subclasses (NOT container
    // screens). All three extend {@code BookScreen}; their {@code render()} blits the
    // 300x200 book texture and then calls {@code super.render()} so tabs added as
    // children render naturally — no entry in {@code ClientNeoForgeEvents.onScreenRenderPost}
    // needed. The mod adds its own ImageButton to the inventory via {@code InventoryScreenMixin};
    // we suppress that mixin via {@link vodmordia.modtabs.mixin.FieldGuideInventoryScreenMixin}
    // so the tab is the only inventory entry point.
    /** Category landing page — opened by the no-arg constructor, same path the mod's
     *  inventory button uses when {@code defaultScreen != "last_opened_screen"} or no
     *  prior book screen exists. */
    public static final String FIELD_GUIDE_CATEGORY_SCREEN =
            "com.evandev.fieldguide.client.gui.screens.FieldGuideCategoryScreen";
    /** Per-entry detail page. Player navigates here from the category screen; we register
     *  it so cycling skips the tab when the player is reading an entry. */
    public static final String FIELD_GUIDE_ENTRY_SCREEN =
            "com.evandev.fieldguide.client.gui.screens.FieldGuideEntryScreen";
    /** Intro "journal" page shown for the {@code intro} category. {@code init()} on
     *  FieldGuideCategoryScreen redirects to this when the selected category id ends in
     *  {@code /intro}, so the player can end up here on first open. */
    public static final String FIELD_GUIDE_JOURNAL_SCREEN =
            "com.evandev.fieldguide.client.gui.screens.FieldGuideJournalScreen";
    /** {@code BookScreen.lastOpenedScreen} — public static field set whenever any
     *  BookScreen's {@code init()} runs. When {@code ClientConfig.defaultScreen ==
     *  "last_opened_screen"} the inventory button opens this instead of constructing a
     *  fresh category screen; the tab mirrors that branch. */
    public static final String FIELD_GUIDE_BOOK_SCREEN =
            "com.evandev.fieldguide.client.gui.screens.BookScreen";
    /** Mod's ClientConfig holder — {@code defaultScreen} field is a String key
     *  ("last_opened_screen" / "current_biome" / category id / ""). We read it via the
     *  no-arg {@code get()} static so a player who configured the mod to remember the
     *  last screen gets the same behavior from the tab as from the native button. */
    public static final String FIELD_GUIDE_CLIENT_CONFIG =
            "com.evandev.fieldguide.config.ClientConfig";
    /** {@code BiologyDictionaryItem.createBook()}-equivalent — we read {@code FIELD_GUIDE}
     *  off {@code ModItems} via {@code Supplier.get()} so the tab icon matches the
     *  in-game item. Falls back to vanilla {@code KNOWLEDGE_BOOK} if reflection fails. */
    public static final String FIELD_GUIDE_MOD_ITEMS =
            "com.evandev.fieldguide.item.ModItems";

    // -- Modonomicon (klikli-dev, NeoForge 1.21.1) ------------------------
    // Patchouli-style guidebook framework. Each book registers its own ModonomiconItem
    // subclass and stores the book id in a data component; right-click on the item opens
    // the book via {@code BookGuiManager.openBook(BookAddress.defaultFor(book))}. The mod
    // doesn't add an inventory button — books are item-driven — so no suppression mixin
    // is needed. Parent screens override {@code render()} and manually iterate
    // {@code this.renderables} (NOT super.render), but tabs added via
    // {@code addRenderableWidget} land in that same list so they DO draw without needing
    // a {@code ClientNeoForgeEvents.onScreenRenderPost} entry.
    /** Base item class for any Modonomicon book. Has public static {@code getBook(ItemStack)}
     *  returning {@code Book}, and {@code getBookId(ItemStack)} returning the
     *  {@code ResourceLocation}. */
    public static final String MODONOMICON_ITEM =
            "com.klikli_dev.modonomicon.item.ModonomiconItem";
    /** {@code BookGuiManager} — public static {@code get()} returns the singleton;
     *  {@code openBook(BookAddress)} is the entry point used by both the keybind and
     *  {@link #MODONOMICON_ITEM}'s right-click. */
    public static final String MODONOMICON_BOOK_GUI_MANAGER =
            "com.klikli_dev.modonomicon.client.gui.BookGuiManager";
    /** Address-locator record for a book open request. Static {@code defaultFor(Book)}
     *  constructs the canonical address used by item-right-click. The class lives under
     *  {@code client.gui.book} (NOT under {@code book} where most other book types sit). */
    public static final String MODONOMICON_BOOK_ADDRESS =
            "com.klikli_dev.modonomicon.client.gui.book.BookAddress";
    /** Parent screen for books with {@code displayMode == NODE}. Active screen
     *  ({@code Minecraft.screen}) for node-mode books — category / entry / search /
     *  bookmarks screens render through it but are NOT set as the active screen. */
    public static final String MODONOMICON_BOOK_PARENT_NODE_SCREEN =
            "com.klikli_dev.modonomicon.client.gui.book.node.BookParentNodeScreen";
    /** Parent screen for books with {@code displayMode == INDEX}. Active screen for
     *  index-mode books; sub-screens (category index, entries) are pushed as GUI layers
     *  via {@code ClientServices.GUI.pushGuiLayer}, so the active screen stays this. */
    public static final String MODONOMICON_BOOK_PARENT_INDEX_SCREEN =
            "com.klikli_dev.modonomicon.client.gui.book.index.BookParentIndexScreen";
    /** Shown when {@code BookDataManager.get().getBook(id)} returns null — typically a
     *  data load failure. We register it so the tab follows the player into the error
     *  state rather than vanishing on a transient failure. */
    public static final String MODONOMICON_BOOK_ERROR_SCREEN =
            "com.klikli_dev.modonomicon.client.gui.book.BookErrorScreen";
}
