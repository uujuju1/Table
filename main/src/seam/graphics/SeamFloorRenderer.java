package seam.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

public class SeamFloorRenderer implements Disposable {
	public FetchBatch reference;

	private final FloorBatch rendererBatch = new FloorBatch();
	private final FloatSeq vertices = new FloatSeq();
	private final ShortSeq indices = new ShortSeq();
	private final Mat projection = new Mat();
	private final Shader shader;

	private Mesh[] layers;

	private boolean fillerDrawCalls;

	public SeamFloorRenderer() {
		shader = new Shader(
			"""
				attribute vec4 a_position;
				attribute vec4 a_color;
				attribute vec2 a_texCoord0;
				
				uniform mat4 u_proj;
				uniform mat4 u_trans;
				varying vec4 v_color;
				varying vec2 v_texCoords;
				
				void main(){
				  v_color = a_color;
				  v_color.a = v_color.a * (255.0/254.0);
				  v_texCoords = a_texCoord0;
				  gl_Position = u_proj * u_trans * a_position;
				}
			""",
			"""
        varying vec4 v_color;
        varying vec2 v_texCoords;
        uniform sampler2D u_texture;

        void main(){
          gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
        }
			"""
		);
		Events.on(EventType.DisposeEvent.class, e -> dispose());
	}

	public void buildLayer(CacheLayer layer, Seq<Tile> tiles) {
		vertices.clear();
		indices.clear();

		Batch old = Core.batch;
		Core.batch = rendererBatch;
		tiles.each(tile -> {
			tile.floor().drawBase(tile);

			if (tile.overlay() != Blocks.air) tile.overlay().drawBase(tile);
		});
		fillerDrawCalls = true;
		tiles.each(tile -> {
			tile.floor().drawBase(tile);

			if (tile.overlay() != Blocks.air) tile.overlay().drawBase(tile);
		});
		fillerDrawCalls = false;
		Core.batch = old;

		Mesh layerMesh = layers[layer.id] = new Mesh(true, vertices.size, indices.size, VertexAttribute.position, VertexAttribute.color, VertexAttribute.texCoords);
		layerMesh.getVerticesBuffer().limit(vertices.size);
		layerMesh.getVerticesBuffer().position(0);
		layerMesh.getIndicesBuffer().limit(indices.size);
		layerMesh.getIndicesBuffer().position(0);
		layerMesh.setVertices(vertices.toArray());
		layerMesh.setIndices(indices.toArray());
	}

	public void buildLayers() {
		ObjectMap<CacheLayer, Seq<Tile>> layers = new ObjectMap<>();

		Vars.world.tiles.eachTile(tile -> {
			Floor floor = tile.floor();

			layers.get(floor.cacheLayer, Seq::new).add(tile);
		});

		layers.each(this::buildLayer);
	}

	@Override
	public void dispose() {
		if (layers != null) {
			for (Mesh mesh : layers) {
				if (mesh != null) mesh.dispose();
			}
		}
	}

	public void drawFloor() {
		if (layers != null) {
			shader.bind();
			shader.setUniformMatrix4(
				"u_proj",
				projection.set(Core.camera.mat)
			);
			shader.setUniformMatrix4(
				"u_trans",
				projection.idt().translate(reference.x, reference.y).rotate(reference.rotation).scale(reference.scaleX, reference.scaleY)
			);
			Core.atlas.find("grass1").texture.bind(0);
			Gl.enable(Gl.blend);
			for (Mesh mesh : layers) {
				if (mesh != null) {
					mesh.render(shader, Gl.triangles, 0, mesh.getMaxVertices() / 5);
				}
			}
		}
	}

	// do not even try to dispose this, cause this class can be un-disposed
	@Override
	public boolean isDisposed() {
		if (layers != null) {
			for (Mesh mesh : layers) {
				if (mesh != null && mesh.isDisposed()) return true;
			}
		}
		return false;
	}

	public void reload() {
		if (layers != null) {
			for (Mesh mesh : layers) {
				if (mesh != null) mesh.dispose();
			}
		}

		layers = new Mesh[CacheLayer.all.length];

		buildLayers();
	}

	class FloorBatch extends Batch {
		@Override
		protected void draw(Texture texture, float[] spriteVertices, int offset, int count) {
			for (int i = 0; i < spriteVertices.length / 6; i++) {
				vertices.add(spriteVertices[i], spriteVertices[i + 1]);
				vertices.add(fillerDrawCalls ? Color.clearFloatBits : spriteVertices[i + 2]);
				vertices.add(spriteVertices[i + 3], spriteVertices[i + 4]);
			}

			int baseVertex = vertices.size / 5;

			for (int i = 2; i < spriteVertices.length / 6; i++) {
				indices.add((short) (baseVertex + i - 1), (short) (baseVertex + i), (short) baseVertex);
			}
		}

		@Override
		protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation) {
			float u = region.u;
			float v = region.v2;
			float u2 = region.u2;
			float v2 = region.v;

			float color = fillerDrawCalls ? Color.clearFloatBits : this.colorPacked;

			int baseVertex = vertices.size / 5;

			indices.add((short) baseVertex, (short) (baseVertex + 1), (short) (baseVertex + 2));
			indices.add((short) (baseVertex + 2), (short) (baseVertex + 3), (short) baseVertex);

			if(!Mathf.zero(rotation)){
				//bottom left and top right corner points relative to origin
				float worldOriginX = x + originX;
				float worldOriginY = y + originY;
				float fx = -originX;
				float fy = -originY;
				float fx2 = width - originX;
				float fy2 = height - originY;

				// rotate
				float cos = Mathf.cosDeg(rotation);
				float sin = Mathf.sinDeg(rotation);

				float x1 = cos * fx - sin * fy + worldOriginX;
				float y1 = sin * fx + cos * fy + worldOriginY;
				float x2 = cos * fx - sin * fy2 + worldOriginX;
				float y2 = sin * fx + cos * fy2 + worldOriginY;
				float x3 = cos * fx2 - sin * fy2 + worldOriginX;
				float y3 = sin * fx2 + cos * fy2 + worldOriginY;
				float x4 = x1 + (x3 - x2);
				float y4 = y3 - (y2 - y1);

				vertices.add(x1, y1);
				vertices.add(color);
				vertices.add(u, v);

				vertices.add(x2, y2);
				vertices.add(color);
				vertices.add(u, v2);

				vertices.add(x3, y3);
				vertices.add(color);
				vertices.add(u2, v2);

				vertices.add(x4, y4);
				vertices.add(color);
				vertices.add(u2, v);
			}else{
				float fx2 = x + width;
				float fy2 = y + height;

				vertices.add(x, y);
				vertices.add(color);
				vertices.add(u, v);

				vertices.add(x, fy2);
				vertices.add(color);
				vertices.add(u, v2);

				vertices.add(fx2, fy2);
				vertices.add(color);
				vertices.add(u2, v2);

				vertices.add(fx2, y);
				vertices.add(color);
				vertices.add(u2, v);
			}
		}

		@Override
		protected void flush() {

		}
	}
}
