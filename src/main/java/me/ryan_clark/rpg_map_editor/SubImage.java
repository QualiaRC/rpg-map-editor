package me.ryan_clark.rpg_map_editor;

public class SubImage {
	public final double x;
	public final double y;
	
	public SubImage(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static final SubImage getSprite(int spriteId) {
		int x = spriteId % 10;
		int y = spriteId / 10;
		return new SubImage(x * 50, y * 50);
	}
}