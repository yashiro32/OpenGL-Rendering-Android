package en.menghui.android.gameengine.postProcessing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Fbo {
	public static final int NONE = 0;
	public static final int DEPTH_TEXTURE = 1;
	public static final int DEPTH_RENDER_BUFFER = 2;
	
	private final int width;
	private final int height;
	
	private int[] frameBuffer = new int[1];
	
	private boolean multisampleAndMultiTarget = false;
	
	private int[] colourTexture = new int[1];
	private int[] depthTexture = new int[1];
	
	private int[] depthBuffer = new int[1];
	private int[] colourBuffer = new int[1];
	private int[] colourBuffer2 = new int[1];
	
	/**
	 * Creates an FBO of a specified width and height, with the desired type of
	 * depth buffer attachment.
	 * 
	 * @param width - the width of the FBO.
	 * 
	 * @param height - the height of the FBO.
	 * 
	 * @param depthBufferType - an int indicating the type of depth buffer attachment that
	 * this FBO should use. 
	 */
	public Fbo(int width, int height, int depthBufferType) {
		this.width = width;
		this.height = height;
		initialiseFrameBuffer(depthBufferType);
	}
	
	public Fbo(int width, int height) {
		this.width = width;
		this.height = height;
		this.multisampleAndMultiTarget = true;
		initialiseFrameBuffer(DEPTH_RENDER_BUFFER);
	}
	
	/**
	 * Deletes the frame buffer and its attachments when the game closes.
	 */
	public void cleanUp() {
		GLES30.glDeleteFramebuffers(1, frameBuffer, 0);
		GLES30.glDeleteTextures(1, colourTexture, 0);
		GLES30.glDeleteTextures(1, depthTexture, 0);
		GLES30.glDeleteRenderbuffers(1, depthBuffer, 0);
		GLES30.glDeleteRenderbuffers(1, colourBuffer, 0);
		GLES30.glDeleteRenderbuffers(1, colourBuffer2, 0);
	}
	
	/**
	 * Binds the frame buffer, setting it as the current render target. Anything
	 * rendered after this will be rendered to this FBO, and not to the screen.
	 */
	public void bindFrameBuffer() {
		GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, frameBuffer[0]);
		GLES30.glViewport(0, 0, width, height);
	}
	
	/**
	 * Unbinds the frame buffer, setting the default frame buffer as the current
	 * render target. Anything rendered after this will be rendered to the
	 * screen, and not this FBO.
	 */
	public void unbindFrameBuffer() {
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glViewport(0, 0, MasterRenderer.mWidth, MasterRenderer.mHeight);
	}
	
	/**
	 * Binds the current FBO to be read from (not used in tutorial 43).
	 */
	public void bindToRead() {
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
		GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, frameBuffer[0]);
		GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
	}
	
	/**
	 * @return The ID of the texture containing the colour buffer of the FBO.
	 */
	public int getColourTexture() {
		return colourTexture[0];
	}
	
	/**
	 * @return The texture containing the FBOs depth buffer.
	 */
	public int getDepthTexture() {
		return depthTexture[0];
	}
	
	public void resolveToFbo(int readBuffer, Fbo outputFbo) {
		GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, outputFbo.frameBuffer[0]);
		GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, this.frameBuffer[0]);
		GLES30.glReadBuffer(readBuffer);
		GLES30.glBlitFramebuffer(0, 0, width, height, 0, 0, outputFbo.width, outputFbo.height, GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT, GLES30.GL_NEAREST);
		this.unbindFrameBuffer();
	}
	
	public void resolveToScreen() {
		GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
		GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, this.frameBuffer[0]);
		int[] targets = {GLES30.GL_BACK};
		GLES30.glDrawBuffers(1, targets, 0);
		GLES30.glBlitFramebuffer(0, 0, width, height, 0, 0, MasterRenderer.mWidth, MasterRenderer.mHeight, GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_NEAREST);
		this.unbindFrameBuffer();
	}
	
	/**
	 * Creates the FBO along with a colour buffer texture attachment, and
	 * possibly a depth buffer.
	 * 
	 * @param type - the type of depth buffer attachment to be attached to the FBO.
	 */
	private void initialiseFrameBuffer(int type) {
		createFrameBuffer();
		if (multisampleAndMultiTarget) {
			colourBuffer = createMultisampleColourAttachment(GLES30.GL_COLOR_ATTACHMENT0);
			colourBuffer2 = createMultisampleColourAttachment(GLES30.GL_COLOR_ATTACHMENT1);
		} else {
			createTextureAttachment();
		}
		if (type == DEPTH_RENDER_BUFFER) {
			createDepthBufferAttachment();
		} else if (type == DEPTH_TEXTURE) {
			createDepthTextureAttachment();
		}
		unbindFrameBuffer();
	}
	
	/**
	 * Creates a new frame buffer object and sets the buffer to which drawing
	 * will occur - colour attachment 0. This is the attachment where the colour
	 * buffer texture is.
	 */
	private void createFrameBuffer() {
		GLES30.glGenFramebuffers(1, frameBuffer, 0);
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);
		
		// int[] targets = {GLES30.GL_COLOR_ATTACHMENT0};
		// GLES30.glDrawBuffers(1, targets, 0);
		determineDrawBuffers();
	}
	
	private void determineDrawBuffers() {
		ByteBuffer bb = ByteBuffer.allocateDirect(2*4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer drawBuffers = bb.asIntBuffer();
		drawBuffers.put(GLES30.GL_COLOR_ATTACHMENT0);
		if (this.multisampleAndMultiTarget) {
			drawBuffers.put(GLES30.GL_COLOR_ATTACHMENT1);
		}
		drawBuffers.flip();
		
		GLES30.glDrawBuffers(2, drawBuffers);
	}
	
	/**
	 * Creates a texture and sets it as the colour buffer attachment for this FBO.
	 */
	private void createTextureAttachment() {
		GLES30.glGenTextures(1, colourTexture, 0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colourTexture[0]);
		GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, colourTexture[0], 0);
	}
	
	/**
	 * Adds a depth buffer to the FBO in the form of a texture, which can later be sampled.
	 */
	private void createDepthTextureAttachment() {
		GLES30.glGenTextures(1, depthTexture, 0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture[0]);
		GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT32F, width, height, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_FLOAT, (ByteBuffer) null);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
		GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, depthTexture[0], 0);
	}
	
	private int[] createMultisampleColourAttachment(int attachment) {
		int[] colourBuffer = new int[1];
		GLES30.glGenRenderbuffers(1, colourBuffer, 0);
		GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, colourBuffer[0]);
		GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, 4, GLES30.GL_RGBA8, width, height);
		GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, attachment, GLES30.GL_RENDERBUFFER, colourBuffer[0]);
		
		return colourBuffer;
	}
	
	/**
	 * Adds a depth buffer to the FBO in the form of a render buffer. This can't
	 * be used for sampling in the shaders.
	 */
	private void createDepthBufferAttachment() {
		GLES30.glGenRenderbuffers(1, depthBuffer, 0);
		GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, depthBuffer[0]);
		if (!multisampleAndMultiTarget) {
			GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT32F, width, height);
		} else {
			GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, 4, GLES30.GL_DEPTH_COMPONENT32F, width, height);
		}
		GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, depthBuffer[0]);
	}
	
}
