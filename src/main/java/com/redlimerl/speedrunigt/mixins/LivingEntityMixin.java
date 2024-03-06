package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MobEntity.class)
public abstract class LivingEntityMixin extends Entity {

    // LivingEntity#dead
    @Shadow protected boolean field_3304;

    // LivingEntity#attackingPlayer
    @Shadow protected PlayerEntity field_3331;

    public LivingEntityMixin(World world) {
        super(world);
    }
    /**
     * @author Void_X_Walker
     * @reason Backported to 1.8
     */

    @Inject(at = @At("HEAD"), method = "dropInventory")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        @NotNull InGameTimer timer = InGameTimer.getInstance();

        if (this.removed || this.field_3304 || timer.getStatus() == TimerStatus.NONE) return;

        // For Timelines
        if (Objects.equals(EntityType.getEntityName(this), "WitherBoss") && this.field_3331 != null) timer.tryInsertNewTimeline("kill_wither");
        if (Objects.equals(EntityType.getEntityName(this), "ElderGuardian") && this.field_3331 != null) timer.tryInsertNewTimeline("kill_elder_guardian");
        if (Objects.equals(EntityType.getEntityName(this), "EnderDragon")) timer.tryInsertNewTimeline("kill_ender_dragon");
        if (Objects.equals(EntityType.getEntityName(this), "Blaze")) timer.tryInsertNewTimeline("killed_blaze");

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

        if (Objects.equals(EntityType.getEntityName(this), "EnderDragon") && !this.world.isClient) {
            InGameTimerUtils.IS_KILLED_ENDER_DRAGON = true;
        }
    }
}
