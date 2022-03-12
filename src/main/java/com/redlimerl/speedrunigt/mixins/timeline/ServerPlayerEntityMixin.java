package com.redlimerl.speedrunigt.mixins.timeline;

import com.mojang.authlib.GameProfile;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.running.RunCategories;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    private ServerWorld beforeWorld = null;
    private Vec3d lastPortalPos = null;

    @Inject(method = "moveToWorld", at = @At("HEAD"))
    public void onChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        beforeWorld = this.getServerWorld();
        lastPortalPos = this.getPos();
        InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = !InGameTimer.getInstance().isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY;
    }

    @Inject(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerChangeDimension(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER))
    public void onChangedDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        RegistryKey<World> oldRegistryKey = beforeWorld.getRegistryKey();
        RegistryKey<World> newRegistryKey = world.getRegistryKey();

        InGameTimer timer = InGameTimer.getInstance();
        if (timer.getStatus() != TimerStatus.NONE && !timer.isCoop() && InGameTimer.getInstance().getCategory() == RunCategories.ANY) {
            if (oldRegistryKey == World.OVERWORLD && newRegistryKey == World.NETHER) {
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.NETHER, this.getPos().add(0, 0, 0), lastPortalPos.add(0, 0, 0));
            }

            if (oldRegistryKey == World.NETHER && newRegistryKey == World.OVERWORLD) {
                if (InGameTimerUtils.isBlindTraveled(lastPortalPos)) {
                    InGameTimer.getInstance().tryInsertNewTimeline("nether_travel");
                }
                InGameTimerUtils.IS_CAN_WAIT_WORLD_LOAD = InGameTimerUtils.isLoadableBlind(World.OVERWORLD, lastPortalPos.add(0, 0, 0), this.getPos().add(0, 0, 0));
            }
        }
    }
}