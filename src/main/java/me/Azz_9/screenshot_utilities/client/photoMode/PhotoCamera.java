package me.Azz_9.screenshot_utilities.client.photoMode;

import static me.Azz_9.screenshot_utilities.client.Screenshot_utilitiesClient.MINECRAFT;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class PhotoCamera extends AbstractClientPlayer {

	public static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));
	private static final float ROLL_SPEED_WITH_CTRL = 1.0f;
	private static final float ROLL_SPEED_WITHOUT_CTRL = 5.0f;
	public float roll = 0.0f;
	public float prevRoll = 0.0f;
	private double velocity = 1.0f;

	public ClientInput input;
	public float yBob;
	public float xBob;
	public float yBobO;
	public float xBobO;

	public PhotoCamera(
			@NonNull ClientLevel level,
			@NonNull GameProfile gameProfile
	) {
		super(level, gameProfile);
		setId(-500);
		setPose(Pose.SWIMMING);
		getAbilities().flying = true;
		noPhysics = true;
		setInvisible(true);
		input = new KeyboardInput(MINECRAFT.options);

		if (MINECRAFT.player != null) {
			absSnapTo(MINECRAFT.player.getX(), getSwimmingY(MINECRAFT.player), MINECRAFT.player.getZ(), MINECRAFT.player.getYRot(), MINECRAFT.player.getXRot());
		}
	}

	@Override
	public void tick() {
		input.tick();
		doMotion();
		super.tick();
	}

	private static double getSwimmingY(@NonNull Entity entity) {
		if (entity.getPose() == Pose.SWIMMING) {
			return entity.getY();
		}
		return entity.getY() - entity.getEyeHeight(Pose.SWIMMING) + entity.getEyeHeight(entity.getPose());
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

	@Override
	protected void doWaterSplashEffect() {
	}

	public void doMotion() {
		getAbilities().setFlyingSpeed(0);

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

		getAbilities().flying = true;
		setOnGround(false);
	}

	@Override
	public float getViewXRot(float a) {
		return this.getXRot();
	}

	@Override
	public float getViewYRot(float a) {
		return this.getYRot();
	}

	@Override
	public boolean isEffectiveAi() {
		return true;
	}

	@Override
	public boolean canSimulateMovement() {
		return true;
	}

	@Override
	protected void applyInput() {
		Vec2 input = this.input.getMoveVector();
		if (input.lengthSquared() != 0.0F) {
			input = input.scale(0.98F);
		}

		this.xxa = input.x;
		this.zza = input.y;
		this.jumping = this.input.keyPresses.jump();
		this.setSprinting((this.input.keyPresses.sprint() && this.input.keyPresses.forward()) || (this.input.keyPresses.forward() && this.isSprinting()));
		this.yBobO = this.yBob;
		this.xBobO = this.xBob;
		this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5F;
		this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5F;
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

	public float getRoll(float tickDelta) {
		return Mth.rotLerp(tickDelta, prevRoll, roll);
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
}
