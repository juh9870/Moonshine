package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.BallsOfSteelBlob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BallsOfSteel extends MissileWeapon {
	{
		maxAmmo=ammo=4;
		image=ItemSpriteSheet.BULLET1;
	}

	@Override
	public void recalculateAmmo() {
	}

	@Override
	public int[] rangedBounds(int lvl) {
		return new int[]{
				//min
				0,
				//max
				0
		};
	}

	@Override
	protected void rangedLand(int cell) {
		GameScene.add(Blob.seed(cell,100,BallsOfSteelBlob.class));
		super.rangedLand(cell);
	}
}
