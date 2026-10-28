package dev.sjinrui.collectionbook.forge;

import dev.sjinrui.collectionbook.common.CollectionBookMod;
import dev.sjinrui.collectionbook.common.registry.CollectionBookItems;
import dev.sjinrui.collectionbook.common.registry.CollectionBookTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge 注册表事件：注册收集书物品与创造模式物品栏。
 */
public class RegistrationHandler {

    @SubscribeEvent
    public void onRegister(RegisterEvent event) {
        event.register(Registries.ITEM, helper ->
                helper.register(new ResourceLocation(CollectionBookMod.MOD_ID, CollectionBookItems.BOOK_ID),
                        CollectionBookItems.COLLECTION_BOOK));

        event.register(Registries.CREATIVE_MODE_TAB, helper ->
                helper.register(new ResourceLocation(CollectionBookMod.MOD_ID, CollectionBookTabs.TAB_ID),
                        CollectionBookTabs.MAIN));
    }
}
