package am2.spell.components;

import am2.AMCore;
import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffList;
import am2.items.ItemsCommonProxy;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesMagicka;

import java.util.EnumSet;
import java.util.Random;

public class ConcentrateTime implements ISpellComponent {

        @Override
        public boolean applyEffectBlock(ItemStack stack, World world, int blockx, int blocky, int blockz, int blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
            Block block = world.getBlock(blockx, blocky, blockz);
            if (block == Blocks.air){
                return false;
            }
            if (block == Blocks.bedrock) { // global. The disable switch is in the method being called
                ExtendedProperties ep = ExtendedProperties.For(caster);
                if (SpellUtils.instance.casterHasMana(caster, 200000)) { // above normal max. mana, but there are multiple possible ways to achieve it.
                    ep.deductMana(200000);
//                    MinecraftServer server = MinecraftServer.getServer();
//                    if((server != null) && (server.getConfigurationManager() != null)) { // server side, or client side singleplayer
//                        MysteriumPatchesFixesMagicka.changeTickrate((int) (20 * ((SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 1) * 1.5)));
//                        int duration = (int)(SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, caster, world, 0, SpellModifiers.DURATION) / 2);
//                        duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
//                        MysteriumPatchesFixesMagicka.countdownToChangeBack = duration; // if the server is reset, this will reset anyway, so there's no issues with not storing this in nbt
//                    }  // 1.5, 3, 4.5 times slower
                }
                return false;
            }
            int duration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, caster, world, 0, SpellModifiers.DURATION) * 2;
            duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
            ExtendedProperties ep = ExtendedProperties.For(caster);
            if (ep != null) {
                ep.addToExtraVariables("accelerated_fast_tile_" + blockx + "_" + blocky + "_" + blockz + "_" + world.provider.dimensionId + "_" + (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 2), String.valueOf(duration));
            }
            return true;
        }

        @Override
        public boolean applyEffectEntity(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase caster, Entity target){
            if (!(target instanceof EntityLivingBase)){
                return true;
            }
            EntityLivingBase targetBase = (EntityLivingBase) target;
            ExtendedProperties ep = ExtendedProperties.For(targetBase);
            int duration = SpellUtils.instance.getModifiedInt_Mul(BuffList.default_buff_duration, stack, caster, target, world, 0, SpellModifiers.DURATION) * 2;
            duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
            if (ep != null) {
                ep.addToExtraVariables("accelerated_fast_entity_" + (SpellUtils.instance.countModifiers(SpellModifiers.BUFF_POWER, stack, 0) + 2) + "_" + targetBase.getUniqueID().toString(), String.valueOf(duration));
            }
            return true;
        }

        @Override
        public float manaCost(net.minecraft.entity.EntityLivingBase caster){
            return 20;
        }

        @Override
        public float burnout(net.minecraft.entity.EntityLivingBase caster){
            return 10;
        }

        @Override
        public ItemStack[] reagents(net.minecraft.entity.EntityLivingBase caster){
            return null;
        }

        @Override
        public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random
        rand, int colorModifier){
            AMParticle particle = (AMParticle) AMCore.proxy.particleManager.spawn(world, "sparkle", x, y, z);
            if (particle != null){
                particle.AddParticleController(new ParticleOrbitEntity(particle, caster, 0.1f, 1, false).SetTargetDistance(rand.nextDouble() + 0.5));
                particle.setMaxAge(25 + rand.nextInt(10));
                if (colorModifier > -1){
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }

        @Override
        public EnumSet<Affinity> getAffinity(){
            return EnumSet.of(Affinity.NONE);
        }

        @Override
        public int getID(){
            return 95;
        }

        @Override
        public Object[] getRecipeItems(){
            return new Object[]{
                    new ItemStack(ItemsCommonProxy.rune, 1, ItemsCommonProxy.rune.META_YELLOW),
                    new ItemStack(ItemsCommonProxy.rune, 1, ItemsCommonProxy.rune.META_CYAN),
                    new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_TEMPORALCLUSTER),
                    new ItemStack(ItemsCommonProxy.itemOre, 1, ItemsCommonProxy.itemOre.META_SUNSTONEFRAGMENT),
                    Items.clock,
            };
        }

        @Override
        public float getAffinityShift(Affinity affinity){
            return 0;
        }
}
