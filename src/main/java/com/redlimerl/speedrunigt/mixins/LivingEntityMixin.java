package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow protected boolean dead;

    @Shadow protected PlayerEntity attackingPlayer;

    public LivingEntityMixin(World world) {
        super(world);
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8
     */

    @Inject(at = @At("HEAD"), method = "onKilled")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.dead || timer.getStatus() == TimerStatus.NONE || this.attackingPlayer == null) return;

        //Kill All Bosses
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (Objects.equals(EntityType.getEntityName(this), "EnderDragon")) {
                timer.updateMoreData(0, 1);
            }
            if (Objects.equals(EntityType.getEntityName(this), "WitherBoss")) {//Wither
                timer.updateMoreData(1, 1);
                RunCategories.checkAllBossesCompleted();
            }
            if (Objects.equals(EntityType.getEntityName(this), "ElderGuardian")) { //Elder Guardian
                timer.updateMoreData(2, 1);
                RunCategories.checkAllBossesCompleted();
            }
        }

        //Kill Wither
        if (timer.getCategory() == RunCategories.KILL_WITHER && Objects.equals(EntityType.getEntityName(this), "WitherBoss")) {
            InGameTimer.complete();
        }

        //Kill Elder Guardian
        if (timer.getCategory() == RunCategories.KILL_ELDER_GUARDIAN && Objects.equals(EntityType.getEntityName(this), "ElderGuardian")) {
            InGameTimer.complete();
        }

        // For Timelines
        if (timer.getCategory() == RunCategories.KILL_ALL_BOSSES) {
            if (Objects.equals(EntityType.getEntityName(this), "WitherBoss")) timer.tryInsertNewTimeline("kill_wither");
            if (Objects.equals(EntityType.getEntityName(this), "ElderGuardian")) timer.tryInsertNewTimeline("kill_elder_guardian");
            if (Objects.equals(EntityType.getEntityName(this), "EnderDragon")) timer.tryInsertNewTimeline("kill_ender_dragon");
        }
        if (timer.getCategory() == RunCategories.ANY && Objects.equals(EntityType.getEntityName(this), "Blaze"))
            timer.tryInsertNewTimeline("killed_blaze");
    }
}
