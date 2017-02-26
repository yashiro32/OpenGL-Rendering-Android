package en.menghui.android.gameengine.entityRenderer;

import java.util.List;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.environmentMapRenderer.CubeMap;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class EntityRenderer {
	private StaticShader shader;
    private CubeMap environmentMap;
    
    public EntityRenderer(float[] projectionMatrix, CubeMap environmentMap) {
        this.environmentMap = environmentMap;
        shader = new StaticShader();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }
    
    public void render(List<Entity> entities, Camera camera) {
        shader.start();
        shader.loadViewMatrix(camera);
        bindEnvironmentMap();
        for (Entity entity : entities) {
            TexturedModel model = entity.getModel();
            bindModelVao(model);
            loadModelMatrix(entity);
            bindTexture(model);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, model.getRawModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
            unbindVao();
        }
        shader.stop();
    }
 
    public void cleanUp() {
        shader.cleanUp();
    }
    
    private void bindEnvironmentMap(){
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, environmentMap.getTexture());
    }
    
    private void bindModelVao(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        GLES30.glBindVertexArray(rawModel.getVaoID());
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glEnableVertexAttribArray(2);
    }
 
    private void unbindVao() {
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
        GLES30.glDisableVertexAttribArray(2);
        GLES30.glBindVertexArray(0);
    }
    
    private void bindTexture(TexturedModel model) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
    }
    
    private void loadModelMatrix(Entity entity) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), 0, entity.getRotY(), 0, entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix.getArray());
    }
}
