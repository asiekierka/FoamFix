package pl.asie.foamfix.ghostbuster;

public class GhostBusterDefinition {
	public final String obfMethodName;
	public final String deobfMethodName;
	public final int accessPos;
	public final int posPos;
	public final int radius;

	public GhostBusterDefinition(String obfMethodName, String deobfMethodName, int accessPos, int posPos, int radius) {
		this.obfMethodName = obfMethodName;
		this.deobfMethodName = deobfMethodName;
		this.accessPos = accessPos;
		this.posPos = posPos;
		this.radius = radius;
	}

	public static GhostBusterDefinition updateTick(int radius) {
		return new GhostBusterDefinition("func_180650_b", "updateTick", 1, 2, radius);
	}

	public static GhostBusterDefinition neighborChanged() {
		return new GhostBusterDefinition("func_189540_a", "neighborChanged", 2, 3, 1);
	}
}
