package am2.spell.components;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.buffs.BuffList;
import am2.items.ItemEssence;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import am2.particles.ParticleOrbitPoint;
import am2.playerextensions.ExtendedProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;

public class Banish implements ISpellComponent {

    @Override
    public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
        return false;
    }

    @Override
    public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
        if (world.isRemote || !(target instanceof EntityLivingBase)) return true;

        ExtendedProperties casterP = ExtendedProperties.For(caster);
        if (casterP.hasExtraVariable("karma")) {
            if (casterP.getExtraVariable("karma").equalsIgnoreCase("bad")) {
                if (((EntityLivingBase) target).isPotionActive(BuffList.astralDistortion.id)) {
                    if (target instanceof EntityPlayer)
                        ((EntityPlayer) target).addChatMessage(new ChatComponentText("The distortion around you prevents you from teleporting"));
                    return true;
                }
                ((EntityLivingBase) target).setPositionAndUpdate(target.posX, -50, target.posZ);
            } else if (caster instanceof EntityPlayer) {
                ((EntityPlayer) caster).addChatMessage(new ChatComponentText("Evil world-bending forces refuse to budge, as you have not broken inner harmony."));
                return true;
            }
        } else if (caster instanceof EntityPlayer) {
            ((EntityPlayer) caster).addChatMessage(new ChatComponentText("Evil world-bending forces refuse to budge, as you have not broken inner harmony."));
            return true;
        }
        return true;
    }

    @Override
    public float manaCost(EntityLivingBase caster){
        return 1000;
    }

    @Override
    public float burnout(EntityLivingBase caster){
        return ArsMagicaApi.getBurnoutFromMana(manaCost(caster));
    }

    @Override
    public ItemStack[] reagents(EntityLivingBase caster){
        return new ItemStack[]{new ItemStack(ItemsCommonProxy.essence, 1, ItemsCommonProxy.essence.META_ENDER)};
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
        for (int i = 0; i < 100; ++i){
            AMParticle particle = (AMParticle)AMCore.proxy.particleManager.spawn(world, "arcane", x, y - 1, z);
            if (particle != null){
                particle.addRandomOffset(1, 1, 1);
                if (rand.nextBoolean())
                    particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f, 1, false).SetTargetDistance(rand.nextDouble() + 0.5));
                else
                    particle.AddParticleController(new ParticleOrbitPoint(particle, x, y, z, 1, false).SetOrbitSpeed(0.1f).SetTargetDistance(rand.nextDouble() + 0.5));
                particle.setMaxAge(25 + rand.nextInt(10));
                if (colorModifier > -1){
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public EnumSet<Affinity> getAffinity(){
        return EnumSet.of(Affinity.ENDER);
    }

    @Override
    public int getID(){
        return 93;
    }

    @Override
    public Object[] getRecipeItems(){
        return new Object[]{
                new ItemStack(ItemsCommonProxy.rune, 1, ItemsCommonProxy.rune.META_BLACK),
                new ItemStack(ItemsCommonProxy.itemOre, 1, ItemEssence.META_NIGHTMARE),
                new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_CELESTIALFISH),
                Items.compass,
                Items.ender_pearl
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity){
        return 0.5f;
    }
}
