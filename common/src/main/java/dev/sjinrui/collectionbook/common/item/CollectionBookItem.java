package dev.sjinrui.collectionbook.common.item;

import dev.sjinrui.collectionbook.common.platform.CollectionPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * “收集书”物品：右键在客户端打开手册 GUI。
 */
public class CollectionBookItem extends Item {
    public CollectionBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            CollectionPlatform.openBookScreen();
        }
        return InteractionResultHolder.success(stack);
    }
}
