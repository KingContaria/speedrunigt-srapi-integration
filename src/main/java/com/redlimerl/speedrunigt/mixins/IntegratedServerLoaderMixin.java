package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {

    @Inject(at = @At("HEAD"), method = "start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/server/DataPackContents;Lnet/minecraft/util/registry/DynamicRegistryManager$Immutable;Lnet/minecraft/world/SaveProperties;)V")
    public void onCreate(LevelStorage.Session session, DataPackContents dataPackContents, DynamicRegistryManager.Immutable dynamicRegistryManager, SaveProperties saveProperties, CallbackInfo ci) {
        RunCategory category = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY);
        if (category.isAutoStart()) {
            InGameTimer.start(session.getDirectoryName(), RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
            InGameTimer.getInstance().setDefaultGameMode(saveProperties.getLevelInfo().getGameMode().getId());
            InGameTimer.getInstance().setCheatAvailable(saveProperties.getLevelInfo().areCommandsAllowed());
            InGameTimer.getInstance().checkDifficulty(saveProperties.getDifficulty());
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        InGameTimerUtils.CAN_DISCONNECT = false;
    }

    @Inject(at = @At("HEAD"), method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V")
    public void onWorldOpen(Screen parent, String levelName, boolean safeMode, boolean canShowBackupPrompt, CallbackInfo ci) {
        try {
            boolean loaded = InGameTimer.load(levelName);
            if (!loaded) InGameTimer.end();
        } catch (Exception e) {
            InGameTimer.end();
            SpeedRunIGT.error("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        InGameTimerUtils.CAN_DISCONNECT = false;
    }
}
