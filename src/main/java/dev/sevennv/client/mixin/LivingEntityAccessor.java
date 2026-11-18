package dev.sevennv.client.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read/write access to the per-jump cooldown used by No Jump Delay. */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("noJumpDelay")
    int sevennv$getNoJumpDelay();

    @Accessor("noJumpDelay")
    void sevennv$setNoJumpDelay(int delay);
}
