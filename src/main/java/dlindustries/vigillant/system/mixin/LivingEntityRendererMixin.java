package dlindustries.vigillant.system.mixin;

import dlindustries.vigillant.system.module.modules.render.NameTags;
import dlindustries.vigillant.system.system;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @ModifyVariable(
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private double adjustDistance(double distanceSq, LivingEntity entity) {
        NameTags nameTags = system.INSTANCE.getModuleManager().getModule(NameTags.class);
        if (nameTags != null && nameTags.isEnabled() && nameTags.isUnlimitedRange()) {
            return 1.0; // Fake close distance
        }
        return distanceSq;
    }

    @Inject(
            method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void forcePlayerNametags(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        NameTags nameTags = system.INSTANCE.getModuleManager().getModule(NameTags.class);
        if (nameTags != null && nameTags.isEnabled() && nameTags.shouldForcePlayerNametags()) {
            cir.setReturnValue(true);
        }
    }
}
