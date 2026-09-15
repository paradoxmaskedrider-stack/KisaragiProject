package com.github.kisaragimikoto.tacticalbackpack.compat.epicfight;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Backpack smith: sneak-right-click a weaponsmith/armorer with a weapon to raise its first enchantment. */
@Mod.EventBusSubscriber
public final class BackpackSmithNpcHandler {
 @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e) {
  if (e.getLevel().isClientSide || !(e.getTarget() instanceof Villager v)) return;
  if (v.getVillagerData().getProfession()!=VillagerProfession.WEAPONSMITH && v.getVillagerData().getProfession()!=VillagerProfession.ARMORER && v.getVillagerData().getProfession()!=VillagerProfession.TOOLSMITH) return;
  if (!e.getEntity().isShiftKeyDown()) return;
  ItemStack held=e.getEntity().getItemInHand(e.getHand()); if(held.isEmpty()) return;
  var ench=net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(held);
  if(ench.isEmpty()) { e.getEntity().displayClientMessage(Component.literal("鍛冶師: 先にエンチャントを付けてください"),true); return; }
  var first=ench.keySet().iterator().next(); ResourceLocation id=net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(first); if(id==null)return;
  int next=BackpackSmithService.upgrade(held,id,1,255);
  e.getEntity().displayClientMessage(Component.literal("鍛冶師: "+id+" を Lv."+next+" に強化"),true); e.setCanceled(true);
 }
 private BackpackSmithNpcHandler(){}
}
