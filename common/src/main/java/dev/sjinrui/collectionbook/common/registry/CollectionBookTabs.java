package dev.sjinrui.collectionbook.common.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * 公共侧创造模式物品栏。
 */
public final class CollectionBookTabs {
    public static final String TAB_ID = "main";

    public static final CreativeModeTab MAIN = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.collection_book"))
            .icon(() -> new ItemStack(CollectionBookItems.COLLECTION_BOOK))
            .displayItems((params, output) -> output.accept(CollectionBookItems.COLLECTION_BOOK))
            .build();

    private CollectionBookTabs() {}
}
