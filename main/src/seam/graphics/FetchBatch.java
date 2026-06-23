package seam.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import seam.runtime.*;

/**
 * Fetches draw calls so that each draw call can have a certain transformation applied to it.
 */
public class FetchBatch extends Batch {
	private Batch destBatch;

	public float x, y, rotation, scaleX = 1f, scaleY = 1f;

	private static final Vec2 temp = new Vec2(), temp2 = new Vec2();

	public void begin() {
		destBatch = Core.batch;
		Core.batch = this;
	}

	public void checkDrawing() {
		if (destBatch == null || Core.batch == destBatch || Core.batch != this) throw new RuntimeException("This batch is not prepared for drawing");
	}

	@Override
	protected void draw(Runnable request) {
		wrap(() -> Draw.draw(Draw.z(), request));
	}

	@Override
	protected void discard() {
		wrap(Draw::discard);
	}

	@Override
	protected void draw(Texture texture, float[] spriteVertices, int offset, int count) {
		wrap(() -> {
			float[] vertices = new float[spriteVertices.length];
			System.arraycopy(spriteVertices, 0, vertices, 0, vertices.length);

			float centerx = 0, centery = 0;

			for (int i = 0; i < vertices.length / 6; i++) {
				centerx += vertices[i * 6] / (vertices.length / 6);
				centery += vertices[i * 6 + 1] / (vertices.length / 6);
			}

			for (int i = 0; i < vertices.length / 6; i++) {
				temp.set(vertices[i * 6], vertices[i * 6 + 1]).sub(centerx, centery).rotate(rotation).add(centerx, centery).scl(scaleX, scaleY).rotate(rotation);

				vertices[i * 6] = temp.x + x;
				vertices[i * 6 + 1] = temp.y + y;
			}

			Draw.vert(texture, vertices, offset, count);
		});
	}

	@Override
	protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation) {
		wrap(() -> {
			float endX = Angles.trnsx(this.rotation, x + width / 2f, y + height / 2f) * scaleX;
			float endY = Angles.trnsy(this.rotation, x + width / 2f, y + height / 2f) * scaleY;
			Draw.rect(region, endX + this.x, endY + this.y, width * scaleX, width * scaleY, originX, originY, rotation + this.rotation);
		});
	}

	public void end() {
		Core.batch = destBatch;
		destBatch = null;
	}

	@Override
	protected void flush() {
		wrap(Draw::flush);
	}

	@Override
	protected Blending getBlending() {
		checkDrawing();

		Core.batch = destBatch;
		Blending blend = Draw.getBlend();
		Core.batch = this;
		return blend;
	}

	@Override
	protected float getPackedColor() {
		checkDrawing();

		Core.batch = destBatch;
		float col = Draw.getColorPacked();
		Core.batch = this;
		return col;
	}

	@Override
	protected float getPackedMixColor() {
		checkDrawing();

		Core.batch = destBatch;
		float col = Draw.getMixColorPacked();
		Core.batch = this;
		return col;
	}

	@Override
	protected Mat getProjection() {
		checkDrawing();

		Core.batch = destBatch;
		Mat proj = Draw.proj();
		Core.batch = this;
		return proj;
	}

	@Override
	protected Shader getShader() {
		checkDrawing();

		Core.batch = destBatch;
		Shader shader = Draw.getShader();
		Core.batch = this;
		return shader;
	}

	@Override
	protected Mat getTransform() {
		checkDrawing();

		Core.batch = destBatch;
		Mat proj = Draw.trans();
		Core.batch = this;
		return proj;
	}

	@Override
	protected void setBlending(Blending blending) {
		wrap(() -> Draw.blend(blending));
	}

	@Override
	protected void setPackedColor(float packedColor) {
		wrap(() -> Draw.color(packedColor));
	}

	@Override
	protected void setPackedMixColor(float packedColor) {
		wrap(() -> Draw.mixcol(packedColor));
	}

	@Override
	protected void setProjection(Mat projection) {
		wrap(() -> Draw.proj(projection));
	}

	@Override
	protected void setShader(Shader shader) {
		wrap(() -> Draw.shader(shader));
	}

	@Override
	protected void setShader(Shader shader, boolean apply) {
		wrap(() -> Draw.shader(shader, apply));
	}

	@Override
	protected void setSort(boolean sort) {
		throw new RuntimeException("Cannot change sorting with this batch");
	}

	@Override
	protected void setTransform(Mat transform) {
		wrap(() -> Draw.trans(transform));
	}

	private void wrap(Runnable run) {
		checkDrawing();

		Core.batch = destBatch;
		run.run();
		Core.batch = this;
	}

	@Override
	protected void z(float z) {
		wrap(() -> Draw.z(z));
	}
}
