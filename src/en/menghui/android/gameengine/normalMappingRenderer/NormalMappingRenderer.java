package en.menghui.android.gameengine.normalMappingRenderer;

import java.util.List;
import java.util.Map;

import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NormalMappingRenderer {
	/* private NormalMappingShader shader;
	
	public NormalMappingRenderer(Matrix4f projectionMatrix) {
		this.shader = new NormalMappingShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix.getArray());
		shader.connectTextureUnits();
		shader.stop();
	}
	
	public void render(Map<TexturedModel, List<Entity>> entities, float[] clipPlane, List<Light> lights, Camera camera) {
		shader.start();
		prepare(clipPlane, lights, camera);
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for (Entity entity : batch) {
				prepareInstance(entity);
				GLES30.glDrawElements(GLES30.GL_TRIANGLES, model.getRawModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
			}
			unbindTextureModel();
		}
		shader.stop();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
		GLES30.glEnableVertexAttribArray(2);
		GLES30.glEnableVertexAttribArray(3);
		ModelTexture texture = model.getTexture();
		shader.loadNumberOfRows(texture.getNumberOfRows());
		if (texture.isHasTransparency()) {
			MasterRenderer.disableCulling();
		}
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getNormalMap());
	}
	
	private void unbindTextureModel() {
		MasterRenderer.enableCulling();
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glDisableVertexAttribArray(2);
		GLES30.glDisableVertexAttribArray(3);
		GLES30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix.getArray());
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}
	
	private void prepare(float[] clipPlane, List<Light> lights, Camera camera) {
		shader.loadClipPlane(clipPlane);
		// Need to be public variables in MasterRenderer.
		shader.loadSkyColour(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		
		shader.loadLights(lights, viewMatrix);
		shader.loadViewMatrix(viewMatrix.getArray());
	} */
	
	private NormalMappingShader shader;
	 
    public NormalMappingRenderer(Matrix4f projectionMatrix) {
        this.shader = new NormalMappingShader();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix.getArray());
        shader.connectTextureUnits();
        shader.stop();
    }
 
    public void render(Map<TexturedModel, List<Entity>> entities, float[] clipPlane, List<Light> lights, Camera camera) {
        shader.start();
        prepare(clipPlane, lights, camera);
        for (TexturedModel model : entities.keySet()) {
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            for (Entity entity : batch) {
                prepareInstance(entity);
                GLES30.glDrawElements(GLES30.GL_TRIANGLES, model.getRawModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
            }
            unbindTexturedModel();
        }
        shader.stop();
    }
     
    public void cleanUp(){
        shader.cleanUp();
    }
 
    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        GLES30.glBindVertexArray(rawModel.getVaoID());
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glEnableVertexAttribArray(2);
        GLES30.glEnableVertexAttribArray(3);
        ModelTexture texture = model.getTexture();
        shader.loadNumberOfRows(texture.getNumberOfRows());
        if (texture.isHasTransparency()) {
            MasterRenderer.disableCulling();
        }
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getNormalMap());
		
		shader.loadUseSpecularMap(texture.hasSpecularMap());
		if (texture.hasSpecularMap()) {
			GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.getSpecularMap());
		}
    }
 
    private void unbindTexturedModel() {
        MasterRenderer.enableCulling();
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
        GLES30.glDisableVertexAttribArray(2);
        GLES30.glDisableVertexAttribArray(3);
        GLES30.glBindVertexArray(0);
    }
 
    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix.getArray());
        shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
    }
 
    private void prepare(float[] clipPlane, List<Light> lights, Camera camera) {
        shader.loadClipPlane(clipPlane);
        //need to be public variables in MasterRenderer
        shader.loadSkyColour(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        
        shader.loadLights(lights, viewMatrix);
        shader.loadViewMatrix(viewMatrix.getArray());
    }
    
    
}
