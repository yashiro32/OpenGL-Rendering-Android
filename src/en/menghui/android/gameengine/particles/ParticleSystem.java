package en.menghui.android.gameengine.particles;

import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;

public class ParticleSystem {
	private float pps;
	private float speed;
	private float gravityComplient;
	private float lifeLength;
	
	private ParticleTexture texture;
	
	public ParticleSystem(ParticleTexture texture, float pps, float speed, float gravityComplient, float lifeLength) {
		this.pps = pps;
		this.speed = speed;
		this.gravityComplient = gravityComplient;
		this.lifeLength = lifeLength;
		this.texture = texture;
	}
	
	public void generateParticles(float[] systemCenter) {
		float delta = MasterRenderer.getFrameTimeSeconds();
		float particlesToCreate = pps * delta;
		int count = (int) Math.floor(particlesToCreate);
		float partialParticle = particlesToCreate % 1;
		for (int i = 0; i < count; i++) {
			emitParticle(systemCenter);
		}
		if (Math.random() < partialParticle) {
			emitParticle(systemCenter);
		}
	}
	
	private void emitParticle(float[] center) {
		float dirX = (float) Math.random() * 2f - 1f;
		float dirZ = (float) Math.random() * 2f - 1f;
		float[] velocity = {dirX, 1, dirZ};
		velocity = Maths.normaliseArray(velocity);
		velocity = Maths.scaleArray(velocity, speed);
		new Particle(texture, center, velocity, gravityComplient, lifeLength, 0, 1);
	}
}
