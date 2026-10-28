package dev.sjinrui.collectionbook.common.client;

import dev.sjinrui.collectionbook.common.CollectionBookMod;
import dev.sjinrui.collectionbook.common.category.CollectionCategory;
import dev.sjinrui.collectionbook.common.data.CollectionEntry;
import dev.sjinrui.collectionbook.common.network.CollectionNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收集书手册 GUI（全部逻辑放在 common，客户端运行）。
 *
 * <p>分类页签：物品 / 生物 / 食物 / 生物群系；网格展示已收集条目；
 * 新收集金色高亮 + 角标；顶部“新 N”确认；按获取时间排序循环。
 */
public class CollectionScreen extends Screen {
    private static final int PANEL_W = 312;
    private static final int PANEL_H = 224;
    private static final int CELL_W = 36;
    private static final int CELL_H = 42;
    private static final int COLS = 8;

    private static final int COLLECTED_BG = 0x5538C86A;
    private static final int UNCOLLECTED_BG = 0x55444444;
    private static final int NEW_BORDER = 0xFFFFD23F;
    private static final int NEW_BG = 0x66FFD23F;
    private static final int PANEL_BG = 0xE8171A22;
    private static final int TAB_SELECTED = 0xFF3E6A78;
    private static final int TOOLBAR_BG = 0x443E6A78;
    private static final int TEXT = 0xFFE7EDF0;
    private static final int MUTED = 0xFF9DB7C0;

    private enum DisplayFilter {
        ALL("collection_book.filter.all"),
        COLLECTED("collection_book.filter.collected"),
        UNCOLLECTED("collection_book.filter.uncollected");

        final String key;
        DisplayFilter(String key) { this.key = key; }
        DisplayFilter next() {
            DisplayFilter[] all = DisplayFilter.values();
            return all[(ordinal() + 1) % all.length];
        }
    }

    private record Cell(String id, Component name, ItemStack icon, CollectionEntry entry) {}

    private CollectionCategory tab = CollectionCategory.ITEM;
    private DisplayFilter filter = DisplayFilter.ALL;
    private CollectionSortMode sort = CollectionSortMode.DEFAULT;
    private final List<Cell> cells = new ArrayList<>();
    private double scroll = 0;
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd");

    public CollectionScreen() {
        super(Component.translatable("collection_book.title"));
    }

    @Override
    protected void init() {
        rebuild();
    }

    private int panelLeft() { return (this.width - PANEL_W) / 2; }
    private int panelTop() { return (this.height - PANEL_H) / 2; }

    private Map<String, CollectionEntry> collectedMap(CollectionCategory category) {
        Map<String, CollectionEntry> map = new HashMap<>();
        for (CollectionEntry e : CollectionClientData.all()) {
            if (e.getCategory() == category) {
                map.put(e.getResourceId(), e);
            }
        }
        return map;
    }

    private void rebuild() {
        cells.clear();
        Map<String, CollectionEntry> collected = collectedMap(tab);
        List<Cell> built = new ArrayList<>();

        switch (tab) {
            case ITEM -> collectItems(built, collected, false);
            case FOOD -> collectItems(built, collected, true);
            case MOB -> collectMobs(built, collected);
            case BIOME -> collectBiomes(built, collected);
        }

        for (Cell c : built) {
            boolean isCollected = c.entry() != null;
            if (filter == DisplayFilter.COLLECTED && !isCollected) continue;
            // 未收集视图里，未确认的新条目临时可见
            if (filter == DisplayFilter.UNCOLLECTED && (isCollected && !c.entry().isNew())) continue;
            cells.add(c);
        }

        Comparator<Cell> cmp = switch (sort) {
            case NEWEST_FIRST -> Comparator.comparingLong((Cell c) -> c.entry() == null ? Long.MAX_VALUE : c.entry().getFirstCollected()).reversed();
            case OLDEST_FIRST -> Comparator.comparingLong((Cell c) -> c.entry() == null ? Long.MAX_VALUE : c.entry().getFirstCollected());
            default -> Comparator.comparingInt(c -> 0);
        };
        cells.sort(cmp);
    }

    private void collectItems(List<Cell> out, Map<String, CollectionEntry> collected, boolean foodOnly) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) continue;
            if (foodOnly && !item.isEdible()) continue;
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            String id = key.toString();
            ItemStack stack = new ItemStack(item);
            out.add(new Cell(id, stack.getHoverName(), stack, collected.get(id)));
        }
    }

    private void collectMobs(List<Cell> out, Map<String, CollectionEntry> collected) {
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> e : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            ResourceLocation id = e.getKey().location();
            ItemStack icon = spawnEggIcon(id);
            out.add(new Cell(id.toString(), e.getValue().getDescription(), icon, collected.get(id.toString())));
        }
    }

    private ItemStack spawnEggIcon(ResourceLocation entityKey) {
        ResourceLocation eggKey = new ResourceLocation(entityKey.getNamespace(), entityKey.getPath() + "_spawn_egg");
        Item egg = BuiltInRegistries.ITEM.get(eggKey);
        if (egg != Items.AIR) {
            return new ItemStack(egg);
        }
        return new ItemStack(Items.BOOK);
    }

    private void collectBiomes(List<Cell> out, Map<String, CollectionEntry> collected) {
        if (Minecraft.getInstance().getConnection() == null) return;
        Registry<Biome> reg = Minecraft.getInstance().getConnection().registryAccess()
                .registryOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> key : reg.registryKeySet()) {
            String id = key.location().toString();
            out.add(new Cell(id, Component.literal(key.location().getPath()), new ItemStack(Items.BOOK), collected.get(id)));
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int left = panelLeft();
        int top = panelTop();

        g.fill(left, top, left + PANEL_W, top + PANEL_H, PANEL_BG);
        g.drawString(this.font, this.title, left + 8, top + 6, 0xFFEAF9FF, false);

        // 页签
        int tabW = 72;
        for (int i = 0; i < CollectionCategory.values().length; i++) {
            CollectionCategory c = CollectionCategory.values()[i];
            int tx = left + 8 + i * (tabW + 2);
            boolean selected = c == tab;
            g.fill(tx, top + 18, tx + tabW, top + 32, selected ? TAB_SELECTED : TOOLBAR_BG);
            g.drawString(this.font, Component.translatable(c.translationKey), tx + 4, top + 21, selected ? 0xFFFFFFFF : MUTED, false);
        }

        // 工具栏：排序 / 筛选 / 新N确认
        int ty = top + 38;
        drawToolbarButton(g, left + 8, ty, 96, sortLabel());
        drawToolbarButton(g, left + 108, ty, 96, filterLabel());
        int newCount = CollectionClientData.countNew();
        drawToolbarButton(g, left + 208, ty, 96, Component.translatable("collection_book.ack", newCount));

        // 网格
        int gridTop = top + 56;
        int gridBottom = top + PANEL_H - 8;
        int rowsVisible = (gridBottom - gridTop) / CELL_H;
        int maxScroll = Math.max(0, (cells.size() + COLS - 1) / COLS - rowsVisible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        int startRow = (int) scroll;
        int hoveredIndex = -1;
        for (int r = 0; r < rowsVisible; r++) {
            int rowIndex = startRow + r;
            for (int col = 0; col < COLS; col++) {
                int index = rowIndex * COLS + col;
                if (index >= cells.size()) break;
                Cell cell = cells.get(index);
                int cx = left + 10 + col * (CELL_W + 1);
                int cy = gridTop + r * CELL_H;

                boolean collectedCell = cell.entry() != null;
                boolean isNew = collectedCell && cell.entry().isNew();
                g.fill(cx, cy, cx + CELL_W - 2, cy + CELL_H - 2, isNew ? NEW_BG : (collectedCell ? COLLECTED_BG : UNCOLLECTED_BG));
                if (isNew) {
                    g.fill(cx, cy, cx + CELL_W - 2, cy + 1, NEW_BORDER);
                    g.fill(cx, cy + CELL_H - 3, cx + CELL_W - 2, cy + CELL_H - 2, NEW_BORDER);
                }

                g.renderItem(cell.icon(), cx + 8, cy + 2);

                String name = truncate(cell.name().getString(), 5);
                g.drawString(this.font, name, cx + 2, cy + 22, collectedCell ? TEXT : MUTED, false);
                if (collectedCell) {
                    String time = isNew ? "新" : dateFmt.format(new Date(cell.entry().getFirstCollected()));
                    g.drawString(this.font, time, cx + 2, cy + 32, isNew ? NEW_BORDER : MUTED, false);
                }

                if (mouseX >= cx && mouseX < cx + CELL_W - 2 && mouseY >= cy && mouseY < cy + CELL_H - 2) {
                    hoveredIndex = index;
                }
            }
        }

        if (maxScroll > 0) {
            g.fill(left + PANEL_W - 6, gridTop, left + PANEL_W - 3, gridBottom, 0x55284F5B);
            int thumbH = Math.max(12, (gridBottom - gridTop) / (maxScroll + 1));
            int thumbY = gridTop + (int) ((gridBottom - gridTop - thumbH) * (scroll / (double) maxScroll));
            g.fill(left + PANEL_W - 6, thumbY, left + PANEL_W - 3, thumbY + thumbH, 0xFF78D8FF);
        }

        if (hoveredIndex >= 0) {
            Cell cell = cells.get(hoveredIndex);
            g.renderTooltip(this.font, cell.name(), mouseX, mouseY);
        }
    }

    private Component sortLabel() {
        return Component.translatable("collection_book.sort.btn", Component.translatable(sort.translationKey));
    }

    private Component filterLabel() {
        return Component.translatable("collection_book.filter.btn", Component.translatable(filter.key));
    }

    private void drawToolbarButton(GuiGraphics g, int x, int y, int w, Component label) {
        g.fill(x, y, x + w, y + 14, TOOLBAR_BG);
        g.drawString(this.font, label, x + 3, y + 3, TEXT, false);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int left = panelLeft();
        int top = panelTop();

        int tabW = 72;
        for (int i = 0; i < CollectionCategory.values().length; i++) {
            int tx = left + 8 + i * (tabW + 2);
            if (in(mouseX, mouseY, tx, top + 18, tx + tabW, top + 32)) {
                tab = CollectionCategory.values()[i];
                scroll = 0;
                rebuild();
                return true;
            }
        }

        int ty = top + 38;
        if (in(mouseX, mouseY, left + 8, ty, left + 104, ty + 14)) {
            sort = sort.next(); rebuild(); return true;
        }
        if (in(mouseX, mouseY, left + 108, ty, left + 204, ty + 14)) {
            filter = filter.next(); rebuild(); return true;
        }
        if (in(mouseX, mouseY, left + 208, ty, left + 304, ty + 14)) {
            CollectionNetwork.sendAckToServer();
            CollectionClientData.clearNewFlags();
            rebuild();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean in(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= delta;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
