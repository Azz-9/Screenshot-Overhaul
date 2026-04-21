package me.Azz_9.screenshot_utilities.client.photoMode;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatAbilities;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NonNull;

import me.Azz_9.screenshot_utilities.accessors.network.ClientPacketListenerAccessor;

@Environment(EnvType.CLIENT)
public class PhotoCamera extends LocalPlayer {

	public static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));
	private static final float ROLL_SPEED_WITH_CTRL = 1.0f;
	private static final float ROLL_SPEED_WITHOUT_CTRL = 5.0f;
	public float roll = 0.0f;
	public float prevRoll = 0.0f;
	private double velocity = 1.0f;

	public PhotoCamera(
			@NonNull Minecraft client,
			@NonNull ClientLevel world,
			@NonNull ClientPacketListener networkHandler,
			@NonNull StatsCounter stats,
			@NonNull ClientRecipeBook recipeBook,
			@NonNull Input lastPlayerInput,
			boolean lastSprinting,
			ChatAbilities chatAbilities
	) {
		super(client, world, networkHandler, stats, recipeBook, lastPlayerInput, lastSprinting, chatAbilities);
		setId(-500);
		setPose(Pose.SWIMMING);
		((ClientPacketListenerAccessor) networkHandler).screenshotUtilities$setClientLoaded(true); // Otherwise input is frozen
		getAbilities().flying = true;
		noPhysics = true;
		setInvisible(true);
		input = new KeyboardInput(MINECRAFT.options);

		if (MINECRAFT.player != null) {
			absSnapTo(MINECRAFT.player.getX(), getSwimmingY(MINECRAFT.player), MINECRAFT.player.getZ(), MINECRAFT.player.getYRot(), MINECRAFT.player.getXRot());
		}
	}

	private static double getSwimmingY(@NonNull Entity entity) {
		if (entity.getPose() == Pose.SWIMMING) {
			return entity.getY();
		}
		return entity.getY() - entity.getEyeHeight(Pose.SWIMMING) + entity.getEyeHeight(entity.getPose());
	}

	public float getRoll(float tickDelta) {
		return Mth.rotLerp(tickDelta, prevRoll, roll);
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public void spawn() {
		((ClientLevel) level()).addEntity(this);
	}

	public void despawn() {
		((ClientLevel) level()).removeEntity(getId(), RemovalReason.DISCARDED);
	}

	// Prevents collision with solid entities (shulkers, boats)

	@Override
	public boolean canCollideWith(@NonNull Entity entity) {
		return false;
	}

	// Ensures that the PhotoCamera is always in the swimming pose.
	@Override
	public void setPose(@NonNull Pose pose) {
		super.setPose(Pose.SWIMMING);
	}

	// Prevents slow down due to being in swimming pose.

	@Override
	public boolean isMovingSlowly() {
		return false;
	}

	@Override
	protected boolean updateIsUnderwater() {
		this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
		return this.wasUnderwater;
	}

	@Override
	public void aiStep() {
		getAbilities().setFlyingSpeed(0);
		this.doMotion();
		super.aiStep();
		getAbilities().flying = true;
		setOnGround(false);
	}

	public void doMotion() {
		double speed = getVelocity();

		float yaw = getYRot();
		double velocityX = 0.0;
		double velocityY = 0.0;
		double velocityZ = 0.0;

		Vec3 forward = Vec3.directionFromRotation(0, yaw);
		Vec3 side = Vec3.directionFromRotation(0, yaw + 90);

		this.input.tick();
		speed = speed * (this.isSprinting() ? 1.5 : 1.0);

		boolean straight = false;
		if (this.input.keyPresses.forward()) {
			velocityX += forward.x * speed;
			velocityZ += forward.z * speed;
			straight = true;
		}
		if (this.input.keyPresses.backward()) {
			velocityX -= forward.x * speed;
			velocityZ -= forward.z * speed;
			straight = true;
		}

		boolean strafing = false;
		if (this.input.keyPresses.right()) {
			velocityZ += side.z * speed;
			velocityX += side.x * speed;
			strafing = true;
		}
		if (this.input.keyPresses.left()) {
			velocityZ -= side.z * speed;
			velocityX -= side.x * speed;
			strafing = true;
		}

		if (straight && strafing) {
			velocityX *= DIAGONAL_MULTIPLIER;
			velocityZ *= DIAGONAL_MULTIPLIER;
		}

		if (this.input.keyPresses.jump()) {
			velocityY += speed;
		}
		if (this.input.keyPresses.shift()) {
			velocityY -= speed;
		}

		this.setDeltaMovement(velocityX, velocityY, velocityZ);
	}

	private float getRollSpeed() {
		return MINECRAFT.hasControlDown() ? ROLL_SPEED_WITH_CTRL : ROLL_SPEED_WITHOUT_CTRL;
	}

	public void rollLeft() {
		this.roll -= getRollSpeed();
	}

	public void rollRight() {
		this.roll += getRollSpeed();
	}
}
