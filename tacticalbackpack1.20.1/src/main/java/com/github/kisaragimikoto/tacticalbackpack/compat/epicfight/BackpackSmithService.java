package com.github.kisaragimikoto.tacticalbackpack.compat.epicfight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.Map;

/** Uses vanilla enchant NBT so it survives Epic Fight updates and also works when Epic Fight is absent. */
public final class BackpackSmithService {
    public static boolean epicFightLoaded() { return ModList.get().isLoaded("epicfight"); }
    public static boolean setLevel(ItemStack stack, ResourceLocation enchantmentId, int level, int hardCap) {
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(enchantmentId);
        if (enchantment == null || stack.isEmpty() || level < 0 || level > hardCap) return false;
        Map<Enchantment,Integer> map = new HashMap<>(net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(stack));
        if (level == 0) map.remove(enchantment); else map.put(enchantment, level);
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(map, stack);
        return true;
    }
    public static int upgrade(ItemStack stack, ResourceLocation id, int amount, int hardCap) {
        Enchantment e=ForgeRegistries.ENCHANTMENTS.getValue(id); if(e==null) return -1;
        int current=net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(e,stack);
        int target=Math.max(0,Math.min(hardCap,current+amount));
        return setLevel(stack,id,target,hardCap)?target:-1;
    }
    private BackpackSmithService() {}
}
