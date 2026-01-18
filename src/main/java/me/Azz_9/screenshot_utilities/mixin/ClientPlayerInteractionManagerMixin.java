package me.Azz_9.screenshot_utilities.mixin;

import me.Azz_9.screenshot_utilities.client.photoMode.PhotoMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		if (PhotoMode.isEnabled()) {
			cir.setReturnValue(ActionResult.PASS);
		}
	}

	@Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
	private void onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (entity.equals(CLIENT.player) || PhotoMode.isEnabled()) {
			cir.setReturnValue(ActionResult.PASS);
		}
	}

	@Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
	private void onInteractEntityAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (entity.equals(CLIENT.player) || PhotoMode.isEnabled()) {
			cir.setReturnValue(ActionResult.PASS);
		}
	}

	@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
	private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (target.equals(CLIENT.player)) {
			ci.cancel();
		}
	}
}
