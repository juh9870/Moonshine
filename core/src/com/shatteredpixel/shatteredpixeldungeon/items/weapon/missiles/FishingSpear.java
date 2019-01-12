package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class FishingSpear extends MissileWeapon {
	{
		image=ItemSpriteSheet.FISHING_SPEAR;
		tier=4;
	}

	@Override
	public float stickChance() {
		return 0.25f;
	}

	@Override
	public float unStickChance() {
		return .1f;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (throwing&&Random.Float()<.1f)Buff.affect(defender,Weakness.class,Math.max(Random.Int(damage)/1.5f,1));
		return super.proc(attacker, defender, damage);
	}
}
