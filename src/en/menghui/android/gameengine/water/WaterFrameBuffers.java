package en.menghui.android.gameengine.water;

import java.nio.ByteBuffer;

import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WaterFrameBuffers {
	protected static final int REFLECTION_WIDTH = 70;
	private static final int REFLECTION_HEIGHT = 120;
	
	protected static final int REFRACTION_WIDTH = 70;
	private static final int REFRACTION_HEIGHT = 120;
	
	private int[] reflectionFrameBuffer = new int[1];
	private int[] reflectionTexture = new int[1];
	private int[] reflectionDepthBuffer = new int[1];
	
	private int[] refractionFrameBuffer = new int[1];
	private int[] refractionTexture = new int[1];
	private int[] refractionDepthTexture = new int[1];
	
	public WaterFrameBuffers() { // Call when loading the game;
		initialiseReflectionFrameBuffer();
		initialiseRefractionFrameBuffer();
	}
	
	public void cleanUp() { // Call when closing the game.
		GLES30.glDeleteFramebuffers(1, reflectionFrameBuffer, 0);
		GLES30.glDeleteTextures(1, reflectionTexture, 0);
		GLES30.glDeleteRenderbuffers(1, reflectionDepthBuffer, 0);
		GLES30.glDeleteFramebuffers(1, refractionFrameBuffer, 0);
		GLES30.glDeleteTextures(1, refractionTexture, 0);
		GLES30.glDeleteTextures(1, refractionDepthTexture, 0);
	}
	
	public void bindReflectionFrameBuffer() { // Call before rendering to this FBO.
		bindFrameBuffer(reflectionFrameBuffer[0], REFLECTION_WIDTH, REFLECTION_HEIGHT);
	}
	
	public void bindRefractionFrameBuffer() { // Call before rendering to this FBO.
		bindFrameBuffer(refractionFrameBuffer[0], REFRACTION_WIDTH, REFRACTION_HEIGHT);
	}
	
	public void unbindCurrentFrameBuffer() { // Call to switch to default frame buffer.
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glViewport(0, 0, MasterRenderer.mWidth, MasterRenderer.mHeight);
	}
	
	public int getReflectionTexture() { // Get the resulting texture.
		return reflectionTexture[0];
	}
	
	public int getRefractionTexture() { // Get the resulting texture.
		return refractionTexture[0];
	}
	
	public int getRefractionDepthTexture() { // Get the resulting depth texture.
		return refractionDepthTexture[0];
	}
	
	private void initialiseReflectionFrameBuffer() {
		reflectionFrameBuffer = createFrameBuffer();
		reflectionTexture = createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
		reflectionDepthBuffer = createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}
	
	private void initialiseRefractionFrameBuffer() {
		refractionFrameBuffer = createFrameBuffer();
		refractionTexture = createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
		refractionDepthTexture = createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}
	
	private void bindFrameBuffer(int frameBuffer, int width, int height) {
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0); // To make sure the texture isn't bound.
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer);
		GLES30.glViewport(0, 0, width, height);
	}
	
	private int[] createFrameBuffer() {
		int[] frameBuffer = new int[1];
		GLES30.glGenFramebuffers(1, frameBuffer, 0);
		// Generate name for frame buffer.
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
		// Create the framebuffer.
		int[] targets = {GLES30.GL_COLOR_ATTACHMENT0};
		GLES30.glDrawBuffers(1, targets, 0);
		// Indicate that we will always render to color attachment 0.
		return frameBuffer;
	}
	
	private int[] createTextureAttachment(int width, int height) {
		int[] texture = new int[1];
		GLES30.glGenTextures(1, texture, 0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
		GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB, width, height, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, texture[0], 0);
		return texture;
	}
	
	private int[] createDepthTextureAttachment(int width, int height) {
		int[] texture = new int[1];
		GLES30.glGenTextures(1, texture, 0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
		GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT32F, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_FLOAT, (ByteBuffer) null);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, texture[0], 0);
		return texture;
	}
	
	private int[] createDepthBufferAttachment(int width, int height) {
		int[] depthBuffer = new int[1];
		GLES30.glGenRenderbuffers(1, depthBuffer, 0);
		GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBuffer[0]);
		GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, width, height);
		GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthBuffer[0]);
		return depthBuffer; 
	}
}
