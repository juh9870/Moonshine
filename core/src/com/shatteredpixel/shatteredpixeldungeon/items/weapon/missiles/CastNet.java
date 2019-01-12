package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Tied;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class CastNet extends MissileWeapon {
	{
		image=ItemSpriteSheet.WEAPON_HOLDER;
		tier=4;
	}

	@Override
	public void recalculateAmmo() {
		int newMaxAmmo = 1+(int)((Math.sqrt(8 * Math.max(level(),0) + 1) - 1)/2*.65f);
		ammo+=newMaxAmmo-maxAmmo;
		maxAmmo=newMaxAmmo;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (throwing)Buff.affect(defender,Tied.class,Math.max(Random.Int((int)Tied.DURATION*(level()/2)),2));
		return super.proc(attacker, defender, damage);
	}

	@Override
	public int[] rangedBounds(int lvl) {
		return new int[]{
				0,
				(int)(tier+tier*lvl*.75f)
		};
	}
}
