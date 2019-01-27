package me.ryan_clark.rpg_map_editor;

public class Action {
	public final Tile oldTile;
	public  final Tile newTile;
	public final int posX;
	public final int posY;
	
	public Action(Tile oldTile, Tile newTile, int posX, int posY) {
		this.oldTile = oldTile;
		this.newTile = newTile;
		this.posX = posX;
		this.posY = posY;
	}
	
	
}
