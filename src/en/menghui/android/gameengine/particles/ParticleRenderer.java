package en.menghui.android.gameengine.particles;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ParticleRenderer {
	 private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	 private static final int MAX_INSTANCES = 10000;
	 private static final int INSTANCE_DATA_LENGTH = 21;
	 
	 private static final FloatBuffer buffer = Loader.createFloatBuffer(MAX_INSTANCES * INSTANCE_DATA_LENGTH);
	 
	 private RawModel quad;
	 private ParticleShader shader;
	 
	 private Loader loader;
	 private int vbo;
	 private int pointer = 0; 
	 
	 protected ParticleRenderer(Loader loader, Matrix4f projectionMatrix) {
		 this.loader = loader;
		 this.vbo = loader.createEmptyVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
		 quad = loader.loadToVAO(VERTICES, 2);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
		 loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
		 shader = new ParticleShader();
		 shader.start();
		 shader.loadProjectionMatrix(projectionMatrix.getArray());
		 shader.stop();
	 }
	 
	 protected void render(Map<ParticleTexture, List<Particle>> particles, Camera camera) {
		 Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		 prepare();
		 for (ParticleTexture texture : particles.keySet()) {
			 bindTexture(texture);
			 List<Particle> particleList = particles.get(texture);
			 pointer = 0;
			 float[] vboData = new float[particleList.size() * INSTANCE_DATA_LENGTH];
			 // Bind texture
			 for (Particle particle : particleList) {
				 updateModelViewMatrix(particle.getPosition(), particle.getRotation(), particle.getScale(), viewMatrix, vboData);
				 updateTexCoordInfo(particle, vboData);
				 // shader.loadTextureCoordInfo(particle.getTexOffset1(), particle.getTexOffset2(), texture.getNumberOfRows(), particle.getBlend());
				 // GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
			 }
			 loader.updateVbo(vbo, vboData, buffer);
			 GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleList.size());
		 }
		 finishRendering();
	 }
	 
	 //The code below is for the updateModelViewMatrix() method
     //modelMatrix.m00 = viewMatrix.m00;
     //modelMatrix.m01 = viewMatrix.m10;
     //modelMatrix.m02 = viewMatrix.m20;
     //modelMatrix.m10 = viewMatrix.m01;
     //modelMatrix.m11 = viewMatrix.m11;
     //modelMatrix.m12 = viewMatrix.m21;
     //modelMatrix.m20 = viewMatrix.m02;
     //modelMatrix.m21 = viewMatrix.m12;
     //modelMatrix.m22 = viewMatrix.m22;
 
     protected void cleanUp(){
    	 shader.cleanUp();
     }
     
     private void updateTexCoordInfo(Particle particle, float[] data) {
    	 data[pointer++] = particle.getTexOffset1()[0];
    	 data[pointer++] = particle.getTexOffset1()[1];
    	 data[pointer++] = particle.getTexOffset2()[0];
    	 data[pointer++] = particle.getTexOffset2()[1];
    	 data[pointer++] = particle.getBlend();
     }
     
     private void bindTexture(ParticleTexture texture) {
    	 if (texture.usesAdditiveBlending()) {
			 GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE);
		 } else {
			 GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
		 }
		 GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		 GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.getTextureID());
		 shader.loadNumberOfRows(texture.getNumberOfRows());
     }
     
     private void updateModelViewMatrix(float[] position, float rotation, float scale, Matrix4f viewMatrix, float[] vboData) {
    	 Matrix4f modelMatrix = new Matrix4f();
    	 modelMatrix.translate(position[0], position[1], position[2]);
    	 modelMatrix.set(0, 0, viewMatrix.get(0, 0));
    	 modelMatrix.set(0, 1, viewMatrix.get(1, 0));
    	 modelMatrix.set(0, 2, viewMatrix.get(2, 0));
    	 modelMatrix.set(1, 0, viewMatrix.get(0, 1));
    	 modelMatrix.set(1, 1, viewMatrix.get(1, 1));
    	 modelMatrix.set(1, 2, viewMatrix.get(2, 1));
    	 modelMatrix.set(2, 0, viewMatrix.get(0, 2));
    	 modelMatrix.set(2, 1, viewMatrix.get(1, 2));
    	 modelMatrix.set(2, 2, viewMatrix.get(2, 2));
    	 // modelMatrix.rotate(rotation, 0, 0, 1);
    	 // modelMatrix.scale(scale, scale, scale);
    	 Matrix4f modelViewMatrix = new Matrix4f();
    	 modelViewMatrix.loadMultiply(viewMatrix, modelMatrix); // Matrix must be m00 = 1, m01 = 0, m02 
    	 modelViewMatrix.rotate(rotation, 0, 0, 1);
    	 modelViewMatrix.scale(scale, scale, scale);
    	 /* System.out.println("m00: " + modelViewMatrix.get(0, 0));
    	 System.out.println("m01: " + modelViewMatrix.get(0, 1));
    	 System.out.println("m02: " + modelViewMatrix.get(0, 2));
    	 System.out.println("m10: " + modelViewMatrix.get(1, 0));
    	 System.out.println("m11: " + modelViewMatrix.get(1, 1));
    	 System.out.println("m12: " + modelViewMatrix.get(1, 2));
    	 System.out.println("m20: " + modelViewMatrix.get(2, 0));
    	 System.out.println("m21: " + modelViewMatrix.get(2, 1));
    	 System.out.println("m22: " + modelViewMatrix.get(2, 2)); */
    	 // shader.loadModelViewMatrix(modelViewMatrix.getArray());
    	 storeMatrixData(modelViewMatrix, vboData);
     }
     
     private void storeMatrixData(Matrix4f matrix, float[] vboData) {
    	 vboData[pointer++] = matrix.get(0, 0);
    	 vboData[pointer++] = matrix.get(0, 1);
    	 vboData[pointer++] = matrix.get(0, 2);
    	 vboData[pointer++] = matrix.get(0, 3);
    	 vboData[pointer++] = matrix.get(1, 0);
    	 vboData[pointer++] = matrix.get(1, 1);
    	 vboData[pointer++] = matrix.get(1, 2);
    	 vboData[pointer++] = matrix.get(1, 3);
    	 vboData[pointer++] = matrix.get(2, 0);
    	 vboData[pointer++] = matrix.get(2, 1);
    	 vboData[pointer++] = matrix.get(2, 2);
    	 vboData[pointer++] = matrix.get(2, 3);
    	 vboData[pointer++] = matrix.get(3, 0);
    	 vboData[pointer++] = matrix.get(3, 1);
    	 vboData[pointer++] = matrix.get(3, 2);
    	 vboData[pointer++] = matrix.get(3, 3);
     }
     
     private void prepare(){
    	 shader.start();
    	 GLES30.glBindVertexArray(quad.getVaoID());
    	 GLES30.glEnableVertexAttribArray(0);
    	 GLES30.glEnableVertexAttribArray(1);
    	 GLES30.glEnableVertexAttribArray(2);
    	 GLES30.glEnableVertexAttribArray(3);
    	 GLES30.glEnableVertexAttribArray(4);
    	 GLES30.glEnableVertexAttribArray(5);
    	 GLES30.glEnableVertexAttribArray(6);
    	 GLES30.glEnable(GLES30.GL_BLEND);
    	 GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    	 GLES30.glDepthMask(false);
     }
     
     private void finishRendering(){
    	 GLES30.glDepthMask(true);
    	 GLES30.glDisable(GLES30.GL_BLEND);
    	 GLES30.glDisableVertexAttribArray(0);
    	 GLES30.glDisableVertexAttribArray(1);
    	 GLES30.glDisableVertexAttribArray(2);
    	 GLES30.glDisableVertexAttribArray(3);
    	 GLES30.glDisableVertexAttribArray(4);
    	 GLES30.glDisableVertexAttribArray(5);
    	 GLES30.glDisableVertexAttribArray(6);
    	 GLES30.glBindVertexArray(0);
    	 shader.stop();
     }
     
     
}
