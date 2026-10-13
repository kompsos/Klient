package com.kompsos.mixin;

import com.kompsos.Klient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {
    @Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
    public void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        Klient.COMMAND_BLOCK_MATRIX_SYSTEM.blockChanged(pos, newState);
    }
}