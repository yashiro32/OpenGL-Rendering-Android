package en.menghui.android.gameengine.renderEngine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import en.menghui.android.gameengine.R;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.shaders.StaticShader;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GLRenderer implements GLSurfaceView.Renderer {
	private Context context; 
	
	private Loader loader;
	private RawModel model;
	private TexturedModel staticModel;
	
	private StaticShader shader;
	
	private Entity entity;
	
	private int mWidth;
	private int mHeight;
	
	private Matrix4f projectionMatrix;
	
	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	private Camera camera;
	
	private List<Light> lights = new ArrayList<Light>();
	private Light light;
	
	public GLRenderer(Context context) {
		this.context = context;
	}
	
	public void prepare() {
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		// Clear the color buffer
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
		GLES30.glClearColor(0, 0.3f, 0.0f, 1);
	}
	
	public void render(Entity entity, StaticShader shader) {
		TexturedModel model = entity.getModel();
		RawModel rawModel = model.getRawModel();
		
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
		GLES30.glEnableVertexAttribArray(2);
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix.getArray());
		
		ModelTexture texture = model.getTexture();
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,  model.getTexture().getID());
		GLES30.glDrawElements(GLES30.GL_TRIANGLES, rawModel.getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glDisableVertexAttribArray(2);
		GLES30.glBindVertexArray(0);
	}
	
	public void render(Map<TexturedModel, List<Entity>> entities) {
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for (Entity entity : batch) {
				prepareInstance(entity);
				GLES30.glDrawElements(GLES30.GL_TRIANGLES, model.getRawModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
		GLES30.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
	}
	
	private void unbindTexturedModel() {
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glDisableVertexAttribArray(2);
		GLES30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix.getArray());
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES30.glEnable(GLES30.GL_CULL_FACE);
		GLES30.glCullFace(GLES30.GL_BACK);
		
		loader = new Loader(this.context);
		
		shader = new StaticShader();
		
		// model = loader.loadToVAO(vertices, textureCoords, indices);
		model = OBJLoader.loadObjModel(R.raw.dragon, loader, context);
		ModelTexture texture = new ModelTexture(loader.loadTexture(R.drawable.white));
	    staticModel = new TexturedModel(model, texture);
	    texture.setShineDamper(10);
	    texture.setReflectivity(1);
		
	    float[] position = {0, 0, -25};
		entity = new Entity(staticModel, position, 0, 0, 0, 1);
		
		float[] lightPos = {0, 0, -20};
		float[] lightColor = {1, 1, 1};
		light = new Light(lightPos, lightColor);
		lights.add(light);
		
		camera = new Camera(null);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.mWidth = width;
		this.mHeight = height;
		
		GLES30.glViewport(0, 0, mWidth, mHeight);
		
		createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix.getArray());
		shader.stop();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// entity.increasePosition(0, 0, -0.1f);
		entity.increaseRotation(0, 1, 0);
		// camera.move("A");
		prepare();
		shader.start();
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		render(entity, shader);
		shader.stop();
	}
	
	private void createProjectionMatrix() {
		float aspectRatio = (float) this.mWidth / (float) this.mHeight;
		float yScale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float xScale = yScale / aspectRatio;
		float frustumLength = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.set(0, 0, xScale);
		projectionMatrix.set(1, 1, yScale);
		projectionMatrix.set(2, 2, -((FAR_PLANE - NEAR_PLANE) / frustumLength));
		projectionMatrix.set(2, 3, -1);
		projectionMatrix.set(3, 2, -((2 * NEAR_PLANE * FAR_PLANE) / frustumLength));
		projectionMatrix.set(3, 3, 0);
	}
	
	
}
