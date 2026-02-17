package dlindustries.vigillant.system.module.modules.render;

import dlindustries.vigillant.system.module.Category;
import dlindustries.vigillant.system.module.Module;
import dlindustries.vigillant.system.module.modules.client.ClickGUI;
import dlindustries.vigillant.system.module.setting.BooleanSetting;
import dlindustries.vigillant.system.module.setting.NumberSetting;
import dlindustries.vigillant.system.utils.EncryptedString;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

import java.awt.*;

public final class SuperVision extends Module {
    private final NumberSetting range = new NumberSetting(EncryptedString.of("Range"), 8, 256, 80, 1)
            .setDescription(EncryptedString.of("Maximum target distance"));
    private final NumberSetting fillAlpha = new NumberSetting(EncryptedString.of("Fill Alpha"), 0, 255, 80, 1)
            .setDescription(EncryptedString.of("Fill opacity"));
    private final NumberSetting outlineAlpha = new NumberSetting(EncryptedString.of("Outline Alpha"), 0, 255, 220, 1)
            .setDescription(EncryptedString.of("Outline opacity"));
    private final BooleanSetting solid = new BooleanSetting(EncryptedString.of("Solid"), true)
            .setDescription(EncryptedString.of("Draw filled boxes"));

    private boolean worldRenderHookRegistered;

    public SuperVision() {
        super(EncryptedString.of("Player ESP"),
                EncryptedString.of("Renders players through walls"),
                -1,
                Category.RENDER);
        addSettings(range, fillAlpha, outlineAlpha, solid);
        registerWorldRenderHook();
    }

    private void registerWorldRenderHook() {
        if (worldRenderHookRegistered) return;
        worldRenderHookRegistered = true;
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        WorldRenderer worldRenderer = context.worldRenderer();
        GameRenderer gameRenderer = context.gameRenderer();
        if (worldRenderer == null || gameRenderer == null) return;
        Camera camera = gameRenderer.getCamera();
        if (camera == null) return;

        float tickDelta = mc.getRenderTickCounter().getDynamicDeltaTicks();
        double rangeSq = range.getValue() * range.getValue();

        int fillColor = rgba(fillAlpha.getValueInt()).getRGB();
        int outlineColor = rgba(outlineAlpha.getValueInt()).brighter().getRGB();
        DrawStyle style = solid.getValue()
                ? DrawStyle.filledAndStroked(outlineColor, 1.5f, fillColor)
                : DrawStyle.stroked(outlineColor, 1.5f);

        try (var ignored = worldRenderer.startDrawingGizmos()) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
                if (mc.player.squaredDistanceTo(player) > rangeSq) continue;

                Box worldBox = getInterpolatedBox(player, tickDelta);
                GizmoDrawing.box(worldBox, style).ignoreOcclusion();
            }
        }
    }

    private static Box getInterpolatedBox(Entity entity, float tickDelta) {
        Vec3d currentPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
        Vec3d lerpedPos = entity.getLerpedPos(tickDelta);
        Vec3d interpOffset = lerpedPos.subtract(currentPos);
        return entity.getBoundingBox().offset(interpOffset.x, interpOffset.y, interpOffset.z);
    }

    private Color rgba(int alpha) {
        return new Color(
                ClickGUI.red.getValueInt(),
                ClickGUI.green.getValueInt(),
                ClickGUI.blue.getValueInt(),
                alpha
        );
    }
}
