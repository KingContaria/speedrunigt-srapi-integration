package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionScreenMixin {

    @Inject(method = "method_32632", remap = false, at = @At("TAIL"))
    public void onChangeDifficulty(CyclingButtonWidget<?> button, Difficulty difficulty, CallbackInfo ci) {
        InGameTimerUtils.CHANGED_OPTIONS.add(button);
    }
}
