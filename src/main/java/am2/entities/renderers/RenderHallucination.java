package am2.entities.renderers;

import am2.entities.EntityHallucination;
import am2.entities.EntitySpecificHallucinations;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderHallucination extends RenderLiving {

    private final ResourceLocation texture;

    public RenderHallucination(ModelBase model, ResourceLocation rec) {
        super(model, 0.5F);
        this.texture = rec;
        super.shadowSize = 0.0F;
    }

    public void renderLivingHallucination(EntityHallucination HallucinationEntity, double par2, double par4, double par6, float par8, float par9) {
        if(Minecraft.getMinecraft().thePlayer.getCommandSenderName().equals(HallucinationEntity.getTargetName()))
            super.doRender(HallucinationEntity, par2, par4, par6, par8, par9);
    }

    public void doRender(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6, float par8, float par9) {
        this.renderLivingHallucination((EntityHallucination)par1EntityLivingBase, par2, par4, par6, par8, par9);
    }

    protected ResourceLocation getEntityTexture(Entity par1Entity) {
        return this.texture;
    }

    protected void preRenderHallucination(EntityHallucination HallucinationEntity, float par2) {
        if (HallucinationEntity instanceof EntitySpecificHallucinations.EntityHallucinationWitherSkeleton) GL11.glScalef(2f, 2f, 2f);
        super.preRenderCallback(HallucinationEntity, par2);
    }

    public void doRender(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderLivingHallucination((EntityHallucination)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2) {
        this.preRenderHallucination((EntityHallucination)par1EntityLivingBase, par2);
    }

    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderLivingHallucination((EntityHallucination)par1Entity, par2, par4, par6, par8, par9);
    }
}
