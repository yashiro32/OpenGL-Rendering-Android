package en.menghui.android.gameengine.particles;

import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Player;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;

public class Particle {
	private float[] position;
	private float[] velocity;
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private float scale;
	
	private ParticleTexture texture;
	
	private float[] texOffset1 = new float[2];
	private float[] texOffset2 = new float[2];
	private float blend;
	
	private float elapsedTime = 0;
	private float distance;
	
	public Particle(ParticleTexture texture, float[] position, float[] velocity, float gravityEffect, float lifeLength, float rotation, float scale) {
		this.texture = texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		
		ParticleMaster.addParticle(this);
	}
	
	public float getDistance() {
		return distance;
	}
	
	public float[] getTexOffset1() {
		return texOffset1;
	}
	
	public float[] getTexOffset2() {
		return texOffset2;
	}
	
	public float getBlend() {
		return blend;
	}
	
	public ParticleTexture getTexture() {
		return texture;
	}

	public float[] getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}
	
	protected boolean update(Camera camera) {
		velocity[1] += Player.GRAVITY * gravityEffect * MasterRenderer.getFrameTimeSeconds();
		float[] change = velocity.clone();
		change = Maths.scaleArray(change, MasterRenderer.getFrameTimeSeconds());
		position = Maths.addArrays(change, position);
		distance = Maths.arrayLengthSquared(Maths.subArrays(camera.getPosition(), position));
		updateTextureCoordInfo();
		elapsedTime += MasterRenderer.getFrameTimeSeconds();
		return elapsedTime < lifeLength;
	}
	
	private void updateTextureCoordInfo() {
		float lifeFactor = elapsedTime / lifeLength;
		int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
		float atlasProgression = lifeFactor * stageCount;
		int index1 = (int) Math.floor(atlasProgression);
		int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
		this.blend = atlasProgression % 1;
		setTextureOffset(texOffset1, index1);
		setTextureOffset(texOffset2, index2);
	}
	
	private void setTextureOffset(float[] offset, int index) {
		int column = index % texture.getNumberOfRows();
		int row = index / texture.getNumberOfRows();
		offset[0] = (float) column / texture.getNumberOfRows();
		offset[1] = (float) row / texture.getNumberOfRows();
	}
	
	
}
