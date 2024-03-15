package com.redlimerl.speedrunigt.mixins.retime;

import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { Screen.class, SettingsScreen.class })
public class OptionButtonWidgetMixin {

    @Inject(method = "buttonClicked", at = @At("RETURN"))
    public void onClickOption(ButtonWidget button, CallbackInfo ci) {
        if (button instanceof OptionButtonWidget) {
            InGameTimerUtils.CHANGED_OPTIONS.add(((OptionButtonWidget) button).method_1088());
        }
    }
}
