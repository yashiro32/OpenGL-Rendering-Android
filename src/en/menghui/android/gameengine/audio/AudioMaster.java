package en.menghui.android.gameengine.audio;

import java.util.ArrayList;
import java.util.List;

public class AudioMaster {
	private static List<Integer> buffers = new ArrayList<Integer>();
	
	public static void init() {
		
	}
	
	public static int loadSound(String file) {
		int buffer = -1;
		buffers.add(buffer);
		
		return buffer;
	}
	
	public static void cleanUp() {
		
	}
}
