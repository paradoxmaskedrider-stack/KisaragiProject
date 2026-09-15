package com.github.kisaragimikoto.tacticalbackpack.compat.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;

public class CuriosBackpackCurio {

    public boolean canEquip(String identifier, LivingEntity livingEntity) {
        return true;
    }

    public boolean canUnequip(String identifier, LivingEntity livingEntity) {
        return true;
    }

    public void onEquip(String identifier, LivingEntity livingEntity) {
        // 背中装備時処理
    }

    public void onUnequip(String identifier, LivingEntity livingEntity) {
        // 解除時処理
    }

    public boolean canEquipFromUse(ItemStack stack) {
        return true;
    }
}