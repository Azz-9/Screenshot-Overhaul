package me.Azz_9.screenshot_utilities.client.photoMode;

import me.Azz_9.screenshot_utilities.api.network.ClientPlayNetworkHandlerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.CLIENT;

public class PhotoCamera extends ClientPlayerEntity {

	public static final double DIAGONAL_MULTIPLIER = MathHelper.sin((float) Math.toRadians(45));
	private static final float ROLL_SPEED_WITH_CTRL = 1.0f;
	private static final float ROLL_SPEED_WITHOUT_CTRL = 5.0f;
	public float roll = 0.0f;
	public float prevRoll = 0.0f;
	private double speed = 1.0f;

	public PhotoCamera(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, PlayerInput lastPlayerInput, boolean lastSprinting) {
		super(client, world, networkHandler, stats, recipeBook, lastPlayerInput, lastSprinting);
		setId(-500);
		setPose(EntityPose.SWIMMING);
		((ClientPlayNetworkHandlerAccessor) networkHandler).screenshotUtilities$setLoaded(true); // Otherwise input is frozen
		getAbilities().flying = true;
		noClip = true;
		setInvisible(true);
		input = new KeyboardInput(CLIENT.options);

		if (CLIENT.player != null) {
			refreshPositionAndAngles(CLIENT.player.getX(), getSwimmingY(CLIENT.player), CLIENT.player.getZ(), CLIENT.player.getYaw(), CLIENT.player.getPitch());
		}
	}

	private static double getSwimmingY(Entity entity) {
		if (entity.getPose() == EntityPose.SWIMMING) {
			return entity.getY();
		}
		return entity.getY() - entity.getEyeHeight(EntityPose.SWIMMING) + entity.getEyeHeight(entity.getPose());
	}

	public float getRoll(float tickDelta) {
		return MathHelper.lerpAngleDegrees(tickDelta, prevRoll, roll);
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void spawn() {
		((ClientWorld) getEntityWorld()).addEntity(this);
	}

	public void despawn() {
		((ClientWorld) getEntityWorld()).removeEntity(getId(), RemovalReason.DISCARDED);
	}

	// Prevents collision with solid entities (shulkers, boats)
	@Override
	public boolean collidesWith(Entity other) {
		return false;
	}

	// Ensures that the PhotoCamera is always in the swimming pose.
	@Override
	public void setPose(EntityPose pose) {
		super.setPose(EntityPose.SWIMMING);
	}

	// Prevents slow down due to being in swimming pose.
	@Override
	public boolean shouldSlowDown() {
		return false;
	}

	@Override
	protected boolean updateWaterSubmersionState() {
		this.isSubmergedInWater = this.isSubmergedIn(FluidTags.WATER);
		return this.isSubmergedInWater;
	}

	@Override
	public void tickMovement() {
		getAbilities().setFlySpeed(0);
		this.doMotion();
		super.tickMovement();
		getAbilities().flying = true;
		setOnGround(false);
	}

	public void doMotion() {
		double speed = getSpeed();

		float yaw = getYaw();
		double velocityX = 0.0;
		double velocityY = 0.0;
		double velocityZ = 0.0;

		Vec3d forward = Vec3d.fromPolar(0, yaw);
		Vec3d side = Vec3d.fromPolar(0, yaw + 90);

		this.input.tick();
		speed = speed * (this.isSprinting() ? 1.5 : 1.0);

		boolean straight = false;
		if (this.input.playerInput.forward()) {
			velocityX += forward.x * speed;
			velocityZ += forward.z * speed;
			straight = true;
		}
		if (this.input.playerInput.backward()) {
			velocityX -= forward.x * speed;
			velocityZ -= forward.z * speed;
			straight = true;
		}

		boolean strafing = false;
		if (this.input.playerInput.right()) {
			velocityZ += side.z * speed;
			velocityX += side.x * speed;
			strafing = true;
		}
		if (this.input.playerInput.left()) {
			velocityZ -= side.z * speed;
			velocityX -= side.x * speed;
			strafing = true;
		}

		if (straight && strafing) {
			velocityX *= DIAGONAL_MULTIPLIER;
			velocityZ *= DIAGONAL_MULTIPLIER;
		}

		if (this.input.playerInput.jump()) {
			velocityY += speed;
		}
		if (this.input.playerInput.sneak()) {
			velocityY -= speed;
		}

		this.setVelocity(velocityX, velocityY, velocityZ);
	}

	private float getRollSpeed() {
		return CLIENT.isCtrlPressed() ? ROLL_SPEED_WITH_CTRL : ROLL_SPEED_WITHOUT_CTRL;
	}

	public void rollLeft() {
		this.roll -= getRollSpeed();
	}

	public void rollRight() {
		this.roll += getRollSpeed();
	}
}
