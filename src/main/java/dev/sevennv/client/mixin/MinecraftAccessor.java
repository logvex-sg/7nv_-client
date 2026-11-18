package dev.sevennv.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read/write access to the private right-click cooldown used by Fast Place. */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("rightClickDelay")
    int sevennv$getRightClickDelay();

    @Accessor("rightClickDelay")
    void sevennv$setRightClickDelay(int delay);
}
