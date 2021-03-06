package me.earth.phobos.features.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Ranges extends Module {
  private final Setting<Boolean> hitSpheres = register(new Setting("HitSpheres", Boolean.valueOf(false)));
  
  private final Setting<Boolean> circle = register(new Setting("Circle", Boolean.valueOf(true)));
  
  private final Setting<Boolean> ownSphere = register(new Setting("OwnSphere", Boolean.valueOf(false), v -> ((Boolean)this.hitSpheres.getValue()).booleanValue()));
  
  private final Setting<Boolean> raytrace = register(new Setting("RayTrace", Boolean.valueOf(false), v -> ((Boolean)this.circle.getValue()).booleanValue()));
  
  private final Setting<Float> lineWidth = register(new Setting("LineWidth", Float.valueOf(1.5F), Float.valueOf(0.1F), Float.valueOf(5.0F)));
  
  private final Setting<Double> radius = register(new Setting("Radius", Double.valueOf(4.5D), Double.valueOf(0.1D), Double.valueOf(8.0D)));
  
  public Ranges() {
    super("Ranges", "Draws a circle around the player.", Module.Category.RENDER, false, false, false);
  }
  
  public void onUpdate() {}
  
  public void onRender3D(Render3DEvent event) {
    if (((Boolean)this.circle.getValue()).booleanValue()) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.enableDepth();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      RenderManager renderManager = mc.getRenderManager();
      float hue = (float)(System.currentTimeMillis() % 7200L) / 7200.0F;
      Color color = new Color(Color.HSBtoRGB(hue, 1.0F, 1.0F));
      ArrayList<Vec3d> hVectors = new ArrayList<>();
      double x = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * event.getPartialTicks() - renderManager.renderPosX;
      double y = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * event.getPartialTicks() - renderManager.renderPosY;
      double z = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * event.getPartialTicks() - renderManager.renderPosZ;
      GL11.glLineWidth(((Float)this.lineWidth.getValue()).floatValue());
      GL11.glBegin(1);
      for (int i = 0; i <= 360; i++) {
        Vec3d vec = new Vec3d(x + Math.sin(i * Math.PI / 180.0D) * ((Double)this.radius.getValue()).doubleValue(), y + 0.1D, z + Math.cos(i * Math.PI / 180.0D) * ((Double)this.radius.getValue()).doubleValue());
        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), vec, false, false, true);
        if (result != null && ((Boolean)this.raytrace.getValue()).booleanValue()) {
          Phobos.LOGGER.info("raytrace was not null");
          hVectors.add(result.hitVec);
        } else {
          hVectors.add(vec);
        } 
      } 
      for (int j = 0; j < hVectors.size() - 1; j++) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        GL11.glVertex3d(((Vec3d)hVectors.get(j)).x, ((Vec3d)hVectors.get(j)).y, ((Vec3d)hVectors.get(j)).z);
        GL11.glVertex3d(((Vec3d)hVectors.get(j + 1)).x, ((Vec3d)hVectors.get(j + 1)).y, ((Vec3d)hVectors.get(j + 1)).z);
        color = new Color(Color.HSBtoRGB(hue += 0.0027777778F, 1.0F, 1.0F));
      } 
      GL11.glEnd();
      GlStateManager.resetColor();
      GlStateManager.disableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
    } 
    if (((Boolean)this.hitSpheres.getValue()).booleanValue())
      for (EntityPlayer player : mc.world.playerEntities) {
        if (player == null || (player.equals(mc.player) && !((Boolean)this.ownSphere.getValue()).booleanValue()))
          continue; 
        Vec3d interpolated = EntityUtil.interpolateEntity((Entity)player, event.getPartialTicks());
        if (Phobos.friendManager.isFriend(player.getName())) {
          GL11.glColor4f(0.15F, 0.15F, 1.0F, 1.0F);
        } else if (mc.player.getDistance((Entity)player) >= 64.0F) {
          GL11.glColor4f(0.0F, 1.0F, 0.0F, 1.0F);
        } else {
          GL11.glColor4f(1.0F, mc.player.getDistance((Entity)player) / 150.0F, 0.0F, 1.0F);
        } 
        RenderUtil.drawSphere(interpolated.x, interpolated.y, interpolated.z, ((Double)this.radius.getValue()).floatValue(), 20, 15);
      }  
  }
}
