package dev.sevennv.client.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Access to the per-block break delay used by Fast Break. */
@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
    @Accessor("destroyDelay")
    int sevennv$getDestroyDelay();

    @Accessor("destroyDelay")
    void sevennv$setDestroyDelay(int delay);
}
