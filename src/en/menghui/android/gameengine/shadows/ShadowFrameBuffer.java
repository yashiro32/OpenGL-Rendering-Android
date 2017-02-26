package en.menghui.android.gameengine.shadows;

import java.nio.ByteBuffer;

import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;

/**
 * The frame buffer for the shadow pass. This class sets up the depth texture
 * which can be rendered to during the shadow render pass, producing a shadow
 * map.
 * 
 * @author Meng Hui
 *
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ShadowFrameBuffer {
	private final int WIDTH;
	private final int HEIGHT;
	private int[] fbo = new int[1];
	private int[] shadowMap = new int[1];
	
	/**
	 * Initialises the frame buffer and shadow map of a certain size.
	 * 
	 * @param width - the width of the shadow map in pixels.
	 * 
	 * @param height - the height of the shadow map in pixels.
	 */
	protected ShadowFrameBuffer(int width, int height) {
		this.WIDTH = width;
		this.HEIGHT = height;
		initialiseFrameBuffer();
	}
	
	/**
	 * Deletes the frame buffer and shadow map texture when the game closes.
	 */
	protected void cleanUp() {
		GLES30.glDeleteFramebuffers(1, fbo, 0);
		GLES30.glDeleteTextures(1, shadowMap, 0);
	}
	
	/**
	 * Binds the frame buffer, setting it as the current render target. 
	 */
	protected void bindFrameBuffer() {
		bindFrameBuffer(fbo[0], WIDTH, HEIGHT);
	}
	
	/**
	 * Unbinds the frame buffer, setting the default frame buffer as the current
	 * render target.
	 */
	protected void unbindFrameBuffer() {
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glViewport(0, 0, MasterRenderer.mWidth, MasterRenderer.mHeight);
	}
	
	/**
	 * @return The ID of the shadow map texture.
	 */
	protected int getShadowMap() {
		return shadowMap[0];
	}
	
	/**
	 * Creates the frame buffer and adds its depth attachment texture.
	 */
	private void initialiseFrameBuffer() {
		fbo = createFrameBuffer();
		shadowMap = createDepthBufferAttachment(WIDTH, HEIGHT);
		unbindFrameBuffer();
	}
	
	/**
	 * Binds the frame buffer as the current render target.
	 * 
	 * @param frameBuffer - the frame buffer.
	 * 
	 * @param width - the width of the frame buffer.
	 * 
	 * @param height - the height of the frame buffer.
	 */
	private static void bindFrameBuffer(int frameBuffer, int width, int height) {
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
		GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, frameBuffer);
		GLES30.glViewport(0, 0, width, height);
	}
	
	/**
	 * Creates a frame buffer and binds it so that attachments can be added to
	 * it. The draw buffer is set to none, indicating that there's no colour
	 * buffer to be rendered to.
	 * 
	 * @return The newly created frame buffer's ID. 
	 */
	private static int[] createFrameBuffer() {
		int[] frameBuffer = new int[1];
		GLES30.glGenFramebuffers(1, frameBuffer, 0);
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
		int[] targets = {GLES30.GL_NONE};
		GLES30.glDrawBuffers(1, targets, 0);
		
		return frameBuffer;
	}
	
	/**
	 * Creates a depth buffer texture attachment.
	 * 
	 * @param width - the width of the texture.
	 * 
	 * @param height - the height of the texture.
	 * 
	 * @return The ID of the depth texture. 
	 */
	private static int[] createDepthBufferAttachment(int width, int height) {
		int[] texture = new int[1];
		GLES30.glGenTextures(1, texture, 0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
		// GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT16, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_FLOAT, (ByteBuffer) null);
		GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT32F, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_FLOAT, (ByteBuffer) null);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, texture[0], 0);
        
        return texture;
	}
}
