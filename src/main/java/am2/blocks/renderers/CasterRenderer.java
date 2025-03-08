package am2.blocks.renderers;

import am2.blocks.BlockCasterRune;
import am2.blocks.tileentities.TileEntityBlockCaster;
import am2.models.modelBlockCaster;
import am2.texture.ResourceManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CasterRenderer extends TileEntitySpecialRenderer {
   private ResourceLocation rLoc = new ResourceLocation("arsmagica2", ResourceManager.getCustomBlockTexturePath("blockCaster.png"));
   private modelBlockCaster model = new modelBlockCaster();

   public void renderAModelAt(TileEntityBlockCaster tile, double d, double d1, double d2, float f) {
      int i = 2;
      int y = 0;
      if (tile.getWorldObj() != null) {
         i = tile.getBlockMetadata() & 3;
         y = (tile.getBlockMetadata() & 12) >> 2;
      }

      int j = (i + 1) * 90;
      int n = 0;
      if (y == 1) {
         n = 90;
      } else if (y == 2) {
         n = -90;
      }

      this.bindTexture(this.rLoc);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F);
      GL11.glRotatef((float)j, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, -1.0F, 0.0F);
      GL11.glRotatef((float)n, 0.0F, 0.0F, 1.0F);
      GL11.glTranslatef(0.0F, 1.0F, 0.0F);
      GL11.glScalef(1.0F, -1.0F, -1.0F);
      this.model.renderModel(tile.getRotation(), 0.0625F);
      GL11.glPopMatrix();
   }

   public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
      if (!(tileentity.getBlockType() instanceof BlockCasterRune)) {
         this.renderAModelAt((TileEntityBlockCaster)tileentity, d, d1, d2, f);
      }
   }
}
