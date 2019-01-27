package me.ryan_clark.rpg_map_editor;

import java.io.Serializable;

public class Tile implements Serializable {

	private static final long serialVersionUID = 3740910742090842301L;

	public static enum CollisionType {
		NONE, SOLID, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, NULL
	}
	
	public final int spriteId;
	public final CollisionType collisionId;
	
	public Tile(int spriteId, CollisionType collisionId) {
		this.spriteId = spriteId;
		this.collisionId = collisionId;
	}
	
	public Tile(Tile t) {
		this.spriteId = t.spriteId;
		this.collisionId = t.collisionId;
	}
	
	public static CollisionType collisionTypeFromId(int id) {
		switch(id) {
			case 0:
				return CollisionType.NONE;
			case 1:
				return CollisionType.SOLID;
			case 2:
				return CollisionType.TOP_LEFT;
			case 3:
				return CollisionType.TOP_RIGHT;
			case 4:
				return CollisionType.BOTTOM_LEFT;
			case 5:
				return CollisionType.BOTTOM_RIGHT;
			default:
				return CollisionType.NULL;
		}
	}
	
	public static int collisionIdFromType(CollisionType type) {
		switch(type) {
			case NONE:
				return 0;
			case SOLID:
				return 1;
			case TOP_LEFT:
				return 2;
			case TOP_RIGHT:
				return 3;
			case BOTTOM_LEFT:
				return 4;
			case BOTTOM_RIGHT:
				return 5;
			default:
				return -1;
		}
	}
}
