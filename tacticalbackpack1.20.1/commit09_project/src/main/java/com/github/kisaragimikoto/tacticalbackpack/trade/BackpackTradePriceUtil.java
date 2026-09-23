package com.github.kisaragimikoto.tacticalbackpack.trade;
import net.minecraft.world.item.*;
public final class BackpackTradePriceUtil { private BackpackTradePriceUtil(){} public static int getPrice(Item item){ ItemStack s=new ItemStack(item); if(s.isEmpty()) return 9999; int p=switch(s.getRarity()){case COMMON->8;case UNCOMMON->24;case RARE->64;case EPIC->128;}; if(s.isDamageableItem())p+=32; if(s.getMaxStackSize()==1)p+=16; return Math.min(p,9999); } }
