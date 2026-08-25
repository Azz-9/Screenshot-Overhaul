package me.Azz_9.screenshot_overhaul.client.texture;

import static me.Azz_9.screenshot_overhaul.CommonClass.MINECRAFT;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import me.Azz_9.screenshot_overhaul.client.panorama.PanoramaHolder;

// MC_COPY Class partially copied from net.minecraft.client.renderer.CubeMap
public class PanoramaCubeMap implements AutoCloseable {

	private static final float Z_NEAR = 0.05F;
	private static final float Z_FAR = 10.0F;
	private static final float FOV = 85.0F;

	private final @NonNull Identifier location;
	private final @NonNull GpuBuffer vertexBuffer;
	private final @NonNull ProjectionMatrixBuffer projectionMatrixBuffer;
	private boolean textureReady = false;

	private @Nullable TextureTarget offscreenTarget;

	public PanoramaCubeMap(final @NonNull Identifier location) {
		this.location = location;
		this.vertexBuffer = initializeVertices();
		this.projectionMatrixBuffer = new ProjectionMatrixBuffer("panorama_widget");
	}

	/**
	 * Charge les images et enregistre la DynamicCubeMapTexture.
	 * Doit être appelé sur le main thread après le chargement async des images.
	 */
	public void uploadTexture(@NonNull NativeImage[] images) {
		RenderSystem.assertOnRenderThread();
		PanoramaHolder.register(images, location);
		this.textureReady = true;
	}

	/**
	 * Rend la cubemap dans une zone précise du main render target.
	 *
	 * @param width, height  Zone en pixels écran (coordonnées GUI)
	 * @param rotX, rotY           Rotation en degrés
	 */
	public void renderToArea(int width, int height, float rotX, float rotY) {
		if (!textureReady) return;

		RenderSystem.assertOnRenderThread();

		double scale = MINECRAFT.getWindow().getGuiScale();
		int physicalWidth = (int)(width * scale);
		int physicalHeight = (int)(height * scale);

		if (offscreenTarget == null
				|| offscreenTarget.width != physicalWidth
				|| offscreenTarget.height != physicalHeight) {
			if (offscreenTarget != null) offscreenTarget.destroyBuffers();
			offscreenTarget = new TextureTarget("Panorama Widget Offscreen", physicalWidth, physicalHeight, true);
		}

		// Projection adaptée aux dimensions de la zone, pas de la fenêtre
		Matrix4f projectionMatrix = new Matrix4f().perspective(
				(float) Math.toRadians(FOV),
				(float) width / height,
				Z_NEAR,
				Z_FAR
		);

		if (offscreenTarget.getColorTextureView() == null) return;

		RenderSystem.backupProjectionMatrix();
		RenderSystem.setProjectionMatrix(
				projectionMatrixBuffer.getBuffer(projectionMatrix),
				ProjectionType.PERSPECTIVE
		);

		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(36);

		Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.rotationX((float) Math.PI);
		modelViewStack.rotateX(rotX * (float) (Math.PI / 180.0));
		modelViewStack.rotateY(rotY * (float) (Math.PI / 180.0));

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
				new Matrix4f(modelViewStack),
				new Vector4f(1f, 1f, 1f, 1f),
				new Vector3f(),
				new Matrix4f()
		);
		modelViewStack.popMatrix();

		try (RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(
						() -> "Panorama Widget",
						offscreenTarget.getColorTextureView(),
						OptionalInt.of(0xFF000000), // clear noir
						offscreenTarget.getDepthTextureView(),
						OptionalDouble.of(1.0)
				)) {

			renderPass.setPipeline(RenderPipelines.PANORAMA);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setVertexBuffer(0, vertexBuffer);
			renderPass.setIndexBuffer(indexBuffer, indices.type());
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			AbstractTexture texture = MINECRAFT.getTextureManager().getTexture(this.location);
			renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
			renderPass.drawIndexed(0, 0, 36, 1);
		}

		RenderSystem.restoreProjectionMatrix();
	}

	@Override
	public void close() {
		this.vertexBuffer.close();
		this.projectionMatrixBuffer.close();
		if (offscreenTarget != null) offscreenTarget.destroyBuffers();
		MINECRAFT.getTextureManager().release(this.location);
		this.textureReady = false;
	}

	private static @NonNull GpuBuffer initializeVertices() {
		GpuBuffer gpuBuffer;
		try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 4 * 6)) {
			BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
			bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F);
			bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(-1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, 1.0F);
			bufferBuilder.addVertex(1.0F, -1.0F, -1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, 1.0F);
			bufferBuilder.addVertex(-1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, -1.0F);
			bufferBuilder.addVertex(1.0F, 1.0F, 1.0F);

			try (MeshData meshData = bufferBuilder.buildOrThrow()) {
				gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Cube map vertex buffer", 32, meshData.vertexBuffer());
			}
		}

		return gpuBuffer;
	}

	public @Nullable TextureTarget getOffscreenTarget() {
		return offscreenTarget;
	}
}
