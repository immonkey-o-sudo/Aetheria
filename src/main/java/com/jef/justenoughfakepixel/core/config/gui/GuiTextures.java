package com.jef.justenoughfakepixel.core.config.gui;

import net.minecraft.util.ResourceLocation;

/**
 * Central registry for every static {@link ResourceLocation} used by JEF's GUI / rendering code.
 * <p>
 * Rules:
 * <ul>
 *   <li>Static, unchanging texture paths belong here as {@code public static final} constants.</li>
 *   <li>Paths that are built at runtime from data (cape IDs, extra-icon names, …) must stay
 *       where the data lives – do <em>not</em> move them here.</li>
 *   <li>Minecraft / vanilla sound-event {@code ResourceLocation}s are not textures; leave them
 *       inline at the call-site.</li>
 * </ul>
 */
public final class GuiTextures {

    private GuiTextures() {}

    // -------------------------------------------------------------------------
    // Social / about icons
    // -------------------------------------------------------------------------
    public static final ResourceLocation DISCORD = new ResourceLocation("justenoughfakepixel:discord.png");
    public static final ResourceLocation GITHUB  = new ResourceLocation("justenoughfakepixel:github.png");

    // -------------------------------------------------------------------------
    // Generic buttons
    // -------------------------------------------------------------------------
    public static final ResourceLocation button_tex   = new ResourceLocation("justenoughfakepixel:button.png");
    public static final ResourceLocation button_white = new ResourceLocation("justenoughfakepixel:button_white.png");

    // -------------------------------------------------------------------------
    // Config GUI chrome
    // -------------------------------------------------------------------------
    public static final ResourceLocation BAR    = new ResourceLocation("justenoughfakepixel:core/bar.png");
    public static final ResourceLocation OFF    = new ResourceLocation("justenoughfakepixel:core/toggle_off.png");
    public static final ResourceLocation ONE    = new ResourceLocation("justenoughfakepixel:core/toggle_1.png");
    public static final ResourceLocation TWO    = new ResourceLocation("justenoughfakepixel:core/toggle_2.png");
    public static final ResourceLocation THREE  = new ResourceLocation("justenoughfakepixel:core/toggle_3.png");
    public static final ResourceLocation ON     = new ResourceLocation("justenoughfakepixel:core/toggle_on.png");
    public static final ResourceLocation DELETE = new ResourceLocation("justenoughfakepixel:core/delete.png");
    public static final ResourceLocation RESET  = new ResourceLocation("justenoughfakepixel:core/reset.png");

    // Config search icon
    public static final ResourceLocation SEARCH_ICON = new ResourceLocation("justenoughfakepixel:search.png");

    // -------------------------------------------------------------------------
    // Slider
    // -------------------------------------------------------------------------
    public static final ResourceLocation slider_off_cap     = new ResourceLocation("justenoughfakepixel:core/slider/slider_off_cap.png");
    public static final ResourceLocation slider_off_notch   = new ResourceLocation("justenoughfakepixel:core/slider/slider_off_notch.png");
    public static final ResourceLocation slider_off_segment = new ResourceLocation("justenoughfakepixel:core/slider/slider_off_segment.png");
    public static final ResourceLocation slider_on_cap      = new ResourceLocation("justenoughfakepixel:core/slider/slider_on_cap.png");
    public static final ResourceLocation slider_on_notch    = new ResourceLocation("justenoughfakepixel:core/slider/slider_on_notch.png");
    public static final ResourceLocation slider_on_segment  = new ResourceLocation("justenoughfakepixel:core/slider/slider_on_segment.png");
    public static final ResourceLocation slider_button_new  = new ResourceLocation("justenoughfakepixel:core/slider/slider_button.png");

    // -------------------------------------------------------------------------
    // Colour picker (GuiElementColour)
    // -------------------------------------------------------------------------
    public static final ResourceLocation colour_selector_dot         = new ResourceLocation("justenoughfakepixel:core/colour_selector_dot.png");
    public static final ResourceLocation colour_selector_bar         = new ResourceLocation("justenoughfakepixel:core/colour_selector_bar.png");
    public static final ResourceLocation colour_selector_bar_alpha   = new ResourceLocation("justenoughfakepixel:core/colour_selector_bar_alpha.png");
    public static final ResourceLocation colour_selector_chroma      = new ResourceLocation("justenoughfakepixel:core/colour_selector_chroma.png");
    public static final ResourceLocation colourPickerLocation        = new ResourceLocation("mbcore:dynamic/colourpicker");
    public static final ResourceLocation colourPickerBarValueLocation   = new ResourceLocation("mbcore:dynamic/colourpickervalue");
    public static final ResourceLocation colourPickerBarOpacityLocation = new ResourceLocation("mbcore:dynamic/colourpickeropacity");

    // -------------------------------------------------------------------------
    // Search bar (RenderUtils)
    // -------------------------------------------------------------------------
    public static final ResourceLocation SEARCH_BAR_TEX      = new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar.png");
    public static final ResourceLocation SEARCH_BAR_TEX_GOLD = new ResourceLocation("justenoughfakepixel", "textures/gui/search_bar_gold.png");

    // -------------------------------------------------------------------------
    // Inventory buttons editor
    // -------------------------------------------------------------------------
    public static final ResourceLocation INVENTORY_TEX = new ResourceLocation("minecraft:textures/gui/container/inventory.png");
    public static final ResourceLocation INV_EDITOR_TEX = new ResourceLocation("justenoughfakepixel", "invbuttons/editor.png");
    public static final ResourceLocation INV_PRESETS_JSON  = new ResourceLocation("justenoughfakepixel", "invbuttons/presets.json");
    public static final ResourceLocation INV_EXTRA_ICONS_JSON = new ResourceLocation("justenoughfakepixel", "invbuttons/extraicons.json");

    // -------------------------------------------------------------------------
    // Dungeon room data
    // -------------------------------------------------------------------------
    public static final ResourceLocation DUNGEON_ROOMS_JSON = new ResourceLocation("justenoughfakepixel", "dungeonrooms/dungeonrooms.json");

    // -------------------------------------------------------------------------
    // Case-opening GUI
    // -------------------------------------------------------------------------
    public static final ResourceLocation CASE_FADE_SIDE  = new ResourceLocation("justenoughfakepixel", "textures/dungeons/caseopening/gui/fade_side.png");
    public static final ResourceLocation CASE_BLUR_SHADER = new ResourceLocation("justenoughfakepixel", "shaders/post/blur.json");

    // -------------------------------------------------------------------------
    // Capes UI
    // -------------------------------------------------------------------------
    public static final ResourceLocation CAPES_UI = new ResourceLocation("justenoughfakepixel", "textures/gui/capesUI.png");

    // -------------------------------------------------------------------------
    // Storage overlay – indexed by style (0 … STORAGE_STYLE_COUNT-1)
    // -------------------------------------------------------------------------
    public static final int STORAGE_STYLE_COUNT = 5;

    private static final ResourceLocation[] STORAGE_BG_TEXTURES   = new ResourceLocation[STORAGE_STYLE_COUNT];
    private static final ResourceLocation[] STORAGE_SLOT_TEXTURES  = new ResourceLocation[STORAGE_STYLE_COUNT];

    static {
        for (int i = 0; i < STORAGE_STYLE_COUNT; i++) {
            STORAGE_BG_TEXTURES[i]   = new ResourceLocation("justenoughfakepixel", "textures/gui/containers/style" + i + "_bg.png");
            STORAGE_SLOT_TEXTURES[i] = new ResourceLocation("justenoughfakepixel", "textures/gui/containers/style" + i + "_slot.png");
        }
    }

    /** Returns the background texture for the given storage overlay style index. */
    public static ResourceLocation storageBackground(int style) {
        return STORAGE_BG_TEXTURES[Math.max(0, Math.min(style, STORAGE_STYLE_COUNT - 1))];
    }

    /** Returns the slot texture for the given storage overlay style index. */
    public static ResourceLocation storageSlot(int style) {
        return STORAGE_SLOT_TEXTURES[Math.max(0, Math.min(style, STORAGE_STYLE_COUNT - 1))];
    }
}
