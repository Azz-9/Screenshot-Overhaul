package me.Azz_9.screenshot_utilities.mixin;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

	// Disable using item in PhotoMode
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit, CallbackInfoReturnable<InteractionResult> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	// Disable interact in PhotoMode
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void onInteract(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (entity.equals(MINECRAFT.player) || PhotoMode.isEnabled()) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	// Disable attacking self in PhotoMode
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void onAttack(Player player, Entity entity, CallbackInfo ci) {
		if (entity.equals(MINECRAFT.player)) {
			ci.cancel();
		}
	}
}
