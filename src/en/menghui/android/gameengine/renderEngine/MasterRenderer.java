package en.menghui.android.gameengine.renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.R;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.entities.Player;
import en.menghui.android.gameengine.fontMeshCreator.FontType;
import en.menghui.android.gameengine.fontMeshCreator.GUIText;
import en.menghui.android.gameengine.fontRendering.TextMaster;
import en.menghui.android.gameengine.guis.GuiRenderer;
import en.menghui.android.gameengine.guis.GuiTexture;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.normalMappingObjConverter.NormalMappedObjLoader;
import en.menghui.android.gameengine.normalMappingRenderer.NormalMappingRenderer;
import en.menghui.android.gameengine.particles.ComplexParticleSystem;
import en.menghui.android.gameengine.particles.Particle;
import en.menghui.android.gameengine.particles.ParticleMaster;
import en.menghui.android.gameengine.particles.ParticleSystem;
import en.menghui.android.gameengine.particles.ParticleTexture;
import en.menghui.android.gameengine.postProcessing.Fbo;
import en.menghui.android.gameengine.postProcessing.PostProcessing;
import en.menghui.android.gameengine.shaders.StaticShader;
import en.menghui.android.gameengine.shaders.TerrainShader;
import en.menghui.android.gameengine.shadows.ShadowMapMasterRenderer;
import en.menghui.android.gameengine.skybox.SkyboxRenderer;
import en.menghui.android.gameengine.terrains.Terrain;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.textures.TerrainTexture;
import en.menghui.android.gameengine.textures.TerrainTexturePack;
import en.menghui.android.gameengine.toolbox.Maths;
import en.menghui.android.gameengine.toolbox.MousePicker;
import en.menghui.android.gameengine.water.WaterFrameBuffers;
import en.menghui.android.gameengine.water.WaterRenderer;
import en.menghui.android.gameengine.water.WaterShader;
import en.menghui.android.gameengine.water.WaterTile;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MasterRenderer implements GLSurfaceView.Renderer {
	private Context context;
	
	private StaticShader shader;
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader;
	private GuiRenderer guiRenderer;
	private SkyboxRenderer skyboxRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;
	
	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();
	private List<Entity> entityList = new ArrayList<Entity>();
	private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<TexturedModel, List<Entity>>();
	private List<Entity> normalMapListEntities = new ArrayList<Entity>();
	
	private Loader loader;
	private RawModel model;
	private TexturedModel staticModel;
	
	private Entity entity;
	
	private Player player;
	
	public static int mWidth;
	public static int mHeight;
	
	private Matrix4f projectionMatrix;
	
	public static final float FOV = 70;
	public static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;
	
	public static final float RED = 0.5444f;
	public static final float GREEN = 0.62f;
	public static final float BLUE = 0.69f;
	public static final float ALPHA = 1;
	
	private Camera camera;
	
	private List<Light> lights = new ArrayList<Light>();
	private Light light;
	
	private Terrain terrain;
	private Terrain terrain2;
	
	private NormalMappingRenderer normalMapRenderer;
	
	private static long lastFrameTime;
	private static float delta;
	
	private List<GuiTexture> guis;
	
	private MousePicker picker;
	
	private WaterShader waterShader;
	private WaterRenderer waterRenderer;
	private List<WaterTile> waters;
	
	private WaterFrameBuffers buffers;
	
	private ComplexParticleSystem system;
	
	private Fbo fbo;
	private Fbo outputFbo, outputFbo2;
	
	public MasterRenderer(Context context) {
		this.context = context;
		
		// renderer = new GLRenderer(context);
	}
	
	public static void enableCulling() {
		GLES30.glEnable(GLES30.GL_CULL_FACE);
		GLES30.glCullFace(GLES30.GL_BACK);
	}
	
	public static void disableCulling() {
		GLES30.glDisable(GLES30.GL_CULL_FACE);
	}
	
	public void prepare() {
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		// Clear the color buffer
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
		GLES30.glClearColor(RED,  GREEN,  BLUE, ALPHA);
		// Bind the shadow map.
		GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, getShadowMapTexture());
	}
	
	public void render(List<Light> lights, Camera camera, float[] clipPlane) {
	    prepare();
		shader.start();
		shader.loadClipPlane(clipPlane);
		shader.loadSkyColour(RED, GREEN, BLUE);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		
		normalMapRenderer.render(normalMapEntities, clipPlane, lights, camera);
		
		terrainShader.start();
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadSkyColour(RED, GREEN, BLUE);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainShader.loadMapSize(shadowMapRenderer.getShadowMapSize());
		terrainRenderer.render(terrains, shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainShader.stop();
		
		skyboxRenderer.render(camera, RED, GREEN, BLUE);
		
		terrains.clear();
		entities.clear();
		normalMapEntities.clear();
	}
	
	public void renderScene(List<Entity> entities, List<Entity> normalEntities, List<Terrain> terrains, List<Light> lights, Camera camera, float[] clipPlane) {
		/* for (Terrain terrain : terrains) {
			processTerrain(terrain);
		} */
		processTerrain(terrain);
		processTerrain(terrain2);
		for (Entity entity : entities) {
			processEntity(entity);
		}
		/* for (Entity entity : normalEntities) {
			processNormalMapEntity(entity);
		} */
		render(lights, camera, clipPlane);
	}
	
	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processNormalMapEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = normalMapEntities.get(entityModel);
		
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}
	
	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
		normalMapRenderer.cleanUp();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		lastFrameTime = getCurrentTime(); // Set the lastFrameTime variable.
		
		enableCulling();
		
		loader = new Loader(this.context);
		
		shader = new StaticShader();
		
		terrainShader = new TerrainShader();
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture(R.drawable.grassy2));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture(R.drawable.mud));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture(R.drawable.grass_flowers));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture(R.drawable.path));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture(R.drawable.blend_map));
		
		terrain = new Terrain(0, 1, loader, texturePack, blendMap, R.drawable.heightmap, context);
		terrain2 = new Terrain(1 , 1, loader, texturePack, blendMap, R.drawable.heightmap, context);
		terrains.add(terrain);
		terrains.add(terrain2);
		
		// model = loader.loadToVAO(vertices, textureCoords, indices);
		model = OBJLoader.loadObjModel(R.raw.tree, loader, context);
		ModelTexture texture = new ModelTexture(loader.loadTexture(R.drawable.tree));
		staticModel = new TexturedModel(model, texture);
		// texture.setShineDamper(10);
		// texture.setReflectivity(1);
		
		TexturedModel rocks = new TexturedModel(OBJLoader.loadObjModel(R.raw.rocks, loader, context), new ModelTexture(loader.loadTexture(R.drawable.rocks)));
		
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture(R.drawable.fern));
		fernTextureAtlas.setNumberOfRows(2);
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel(R.raw.fern, loader, context), new ModelTexture(loader.loadTexture(R.drawable.fern)));
		fern.getTexture().setHasTransparency(true);
		
		TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel(R.raw.pine, loader, context), new ModelTexture(loader.loadTexture(R.drawable.pine)));
        bobble.getTexture().setHasTransparency(true);
		
		Random random = new Random();
		for (int i = 0; i < 1; i++) {
			float x = random.nextFloat()*800 - 400;
			float z = random.nextFloat() * -600;
			float y = terrain.getHeightOfTerrain(x, z);
			float[] position = {x, y, z};
			// float[] position = {0, 0, -25};
			entityList.add(new Entity(staticModel, position, 0, 0, 0, 3));
			x = random.nextFloat()*800 - 400;
			z = random.nextFloat() * -600;
			y = terrain.getHeightOfTerrain(x, z);
			float[] position2 = {x, y, z};
			entityList.add(new Entity(bobble, position2, 0, 0, 0, 1));
			x = random.nextFloat()*800 - 400;
			z = random.nextFloat() * -600;
			y = terrain.getHeightOfTerrain(x, z);
			float[] position3 = {x, y, z};
			entityList.add(new Entity(fern, random.nextInt(4), position3, 0, 0, 0, 0.6f));
			x = random.nextFloat()*800 - 400;
			z = random.nextFloat() * -600;
			y = terrain.getHeightOfTerrain(x, z);
			float[] position4 = {x, y, z};
			entityList.add(new Entity(rocks, position4, 0, 0, 0, 1));
		}
		
		float[] lightPos = {100, 100, 100};
		// float[] lightPos = {10, 10, -10};
		float[] lightColor = {1.3f, 1.3f, 1.3f};
		light = new Light(lightPos, lightColor);
		lights.add(light);
		float[] lightPos2 = {185, 10, -293};
		float[] lightColor2 = {2, 0, 0};
		float[] atten2 = {1, 0.01f, 0.002f};
		Light light2 = new Light(lightPos2, lightColor2, atten2);
		// lights.add(light2);
		float[] lightPos3 = {370, 17, -300};
		float[] lightColor3 = {0, 2, 2};
		float[] atten3 = {1, 0.01f, 0.002f};
		Light light3 = new Light(lightPos3, lightColor3, atten3);
		// lights.add(light3);
		float[] lightPos4 = {293, 7, -305};
		float[] lightColor4 = {2, 2, 0};
		float[] atten4 = {1, 0.01f, 0.002f};
		Light light4 = new Light(lightPos4, lightColor4, atten4);
		// lights.add(light4);
		
		/* float[] posi1 = {185, -4.7f, -293};
		entityList.add(new Entity(lamp, posi1, 0, 0, 0, 1));
		float[] posi2 = {370, 4.2f, -300};
		entityList.add(new Entity(lamp, posi2, 0, 0, 0, 1));
		float[] posi3 = {293, -6.8f, -305};
		entityList.add(new Entity(lamp, posi3, 0, 0, 0, 1)); */
		
		RawModel bunnyModel = OBJLoader.loadObjModel(R.raw.person, loader, context);
		TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(loader.loadTexture(R.drawable.player_texture)));
		float[] position = {75, 5, -75};
		player = new Player(stanfordBunny, position, 0, 100, 0, 0.6f);
		
		camera = new Camera(player);
		
		guis = new ArrayList<GuiTexture>();
		/* float[] pos = {0.5f, 0.5f};
		float[] scale = {0.25f, 0.25f};
		GuiTexture gui = new GuiTexture(loader.loadTexture(R.drawable.socuwan), pos, scale);
		float[] pos2 = {0.30f, 0.58f};
		float[] scale2 = {0.4f, 0.4f};
		GuiTexture gui2 = new GuiTexture(loader.loadTexture(R.drawable.thinmatrix), pos2, scale2);
		guis.add(gui);
		guis.add(gui2); */
		
		guiRenderer = new GuiRenderer(loader);
		
		buffers = new WaterFrameBuffers();
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ(R.raw.barrel, loader, context), new ModelTexture(loader.loadTexture(R.drawable.barrel)));
		barrelModel.getTexture().setNormalMap(loader.loadTexture(R.drawable.barrel_normal));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		TexturedModel crateModel = new TexturedModel(NormalMappedObjLoader.loadOBJ(R.raw.crate, loader, context), new ModelTexture(loader.loadTexture(R.drawable.crate)));
        crateModel.getTexture().setNormalMap(loader.loadTexture(R.drawable.crate_normal));
        crateModel.getTexture().setShineDamper(10);
        crateModel.getTexture().setReflectivity(0.5f);
         
        TexturedModel boulderModel = new TexturedModel(NormalMappedObjLoader.loadOBJ(R.raw.boulder, loader, context), new ModelTexture(loader.loadTexture(R.drawable.boulder)));
        boulderModel.getTexture().setNormalMap(loader.loadTexture(R.drawable.boulder_normal));
        boulderModel.getTexture().setShineDamper(10);
        boulderModel.getTexture().setReflectivity(0.5f);
        
        TexturedModel cherryModel = new TexturedModel(OBJLoader.loadObjModel(R.raw.cherry, loader, context), new ModelTexture(loader.loadTexture(R.drawable.cherry)));
		cherryModel.getTexture().setHasTransparency(true);
		cherryModel.getTexture().setShineDamper(10);
		cherryModel.getTexture().setReflectivity(0.5f);
		cherryModel.getTexture().setSpecularMap(loader.loadTexture(R.drawable.cherry_s));
		float[] posCherry = {75, 10, -75};
		entityList.add(new Entity(cherryModel, posCherry, 0, 0, 0, 1));
        
		float[] pos1 = {75, 10, -75};
		float[] pos2 = {85, 10, -75};
		float[] pos3 = {65, 10, -75};
		normalMapListEntities.add(new Entity(barrelModel, pos1, 0, 0, 0, 1f));
		normalMapListEntities.add(new Entity(boulderModel, pos2, 0, 0, 0, 1f));
		normalMapListEntities.add(new Entity(crateModel, pos3, 0, 0, 0, 0.04f));
		
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture(R.drawable.particle_atlas), 4);
		system = new ComplexParticleSystem(particleTexture, 40, 10, 0.1f, 1, 1.6f);
		system.randomizeRotation();
		system.setLifeError(0.1f);
		system.setSpeedError(0.4f);
		system.setScaleError(0.8f);
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.mWidth = width;
		this.mHeight = height;
		
		GLES30.glViewport(0, 0, mWidth, mHeight);
		
		createProjectionMatrix();
		/* shader.start();
		shader.loadProjectionMatrix(projectionMatrix.getArray());
		shader.stop(); */
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		
		// picker = new MousePicker(camera, this.projectionMatrix);
		
		waterShader = new WaterShader();
		waterRenderer = new WaterRenderer(loader, waterShader, projectionMatrix, buffers);
		waters = new ArrayList<WaterTile>();
		waters.add(new WaterTile(75, -75, 0));
		
		normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
		
		TextMaster.init(loader);
		FontType font = new FontType(loader.loadTexture(R.drawable.candara), R.raw.candara, context);
		float[] pos4 = {0.0f, 0.4f};
		GUIText text = new GUIText("This is a test text!", 3, font, pos4, 1f, true);
		text.setColour(0.1f, 0.1f, 0.1f);
		float[] color = {0, 0.5f, 0};
		text.setOutlineColour(color);
		
		ParticleMaster.init(loader, projectionMatrix);
		
		this.shadowMapRenderer = new ShadowMapMasterRenderer(camera);
		/* float[] pos = {0.5f, 0.5f};
		float[] scale = {0.5f, 0.5f};
		GuiTexture shadowMap = new GuiTexture(getShadowMaptexture(), pos, scale);
		guis.add(shadowMap); */
		
		// fbo = new Fbo(mWidth, mHeight, Fbo.DEPTH_RENDER_BUFFER);
		fbo = new Fbo(mWidth, mHeight);
		outputFbo = new Fbo(mWidth, mHeight, Fbo.DEPTH_TEXTURE);
		outputFbo2 = new Fbo(mWidth, mHeight, Fbo.DEPTH_TEXTURE);
		PostProcessing.init(loader);
		
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		float[] plane = {0, 1, 0, -waters.get(0).getHeight() + 1f};
		float[] plane2 = {0, -1, 0, waters.get(0).getHeight()};
		float[] plane3 = {0, -1, 0, 100000};
		
		player.move(terrain);
		// camera.move("A", 0.2f);
		camera.move("W", 0.2f);
		
		// picker.update();
		// System.out.println("" + picker.getCurrentRay());
		
		system.generateParticles(player.getPosition());
		float[] vec = {500, 10, -300};
		system.generateParticles(vec);
		ParticleMaster.update(camera);
		
		renderShadowMap(entityList, lights.get(0));
		
		buffers.bindReflectionFrameBuffer();
		float distance = 2 * (camera.getPosition()[1] - waters.get(0).getHeight());
		camera.getPosition()[1] -= distance;
		camera.invertPitch();
		
		processEntity(player);
		renderScene(entityList, normalMapListEntities, terrains, lights, camera, plane);
		
		camera.getPosition()[1] += distance;
		camera.invertPitch();
		
		buffers.bindRefractionFrameBuffer();
		
		processEntity(player);
		renderScene(entityList, normalMapListEntities, terrains, lights, camera, plane);
		
		buffers.unbindCurrentFrameBuffer();
		
		
		fbo.bindFrameBuffer();
		
		processEntity(player);
		
		/* processTerrain(terrain);
		processTerrain(terrain2);
		
		for (Entity entity : entityList) {
			processEntity(entity);
		}
		
		render(lights, camera, plane3); */
		
		renderScene(entityList, normalMapListEntities, terrains, lights, camera, plane);
		waterRenderer.render(waters, camera, lights.get(0));
		ParticleMaster.renderParticles(camera);
		fbo.unbindFrameBuffer();
		fbo.resolveToFbo(GLES30.GL_COLOR_ATTACHMENT0, outputFbo);
		fbo.resolveToFbo(GLES30.GL_COLOR_ATTACHMENT1, outputFbo2);
		PostProcessing.doPostProcessing(outputFbo.getColourTexture(), outputFbo2.getColourTexture());
		
		guiRenderer.render(guis);
		
		// TextMaster.render();
		
		updateFrameTime();
	}
	
	private void createProjectionMatrix() {
		/* float aspectRatio = (float) this.mWidth / (float) this.mHeight;
		float yScale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float xScale = yScale / aspectRatio;
		float frustumLength = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.set(0, 0, xScale);
		projectionMatrix.set(1, 1, yScale);
		projectionMatrix.set(2, 2, -((FAR_PLANE - NEAR_PLANE) / frustumLength));
		projectionMatrix.set(2, 3, -1);
		projectionMatrix.set(3, 2, -((2 * NEAR_PLANE * FAR_PLANE) / frustumLength));
		projectionMatrix.set(3, 3, 0); */
		
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) this.mWidth / (float) this.mHeight;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.set(0, 0, x_scale);
		projectionMatrix.set(1, 1, y_scale);
		projectionMatrix.set(2, 2, -((FAR_PLANE + NEAR_PLANE) / frustum_length));
		projectionMatrix.set(2, 3, -1);
		projectionMatrix.set(3, 2, -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length));
		projectionMatrix.set(3, 3, 0);
	}
	
	private void updateFrameTime() {
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = getCurrentTime();
	}
	
	public static float getFrameTimeSeconds() {
		return delta;
	}
	
	private static long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	public void renderShadowMap(List<Entity> entityList, Light sun) {
		for (Entity entity : entityList) {
			processEntity(entity);
		}
		shadowMapRenderer.render(entities, sun);
		entities.clear();
	}
	
	public int getShadowMapTexture() {
		return shadowMapRenderer.getShadowMap(); 
	}
}
