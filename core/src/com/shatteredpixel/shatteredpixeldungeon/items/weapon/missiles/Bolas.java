package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Bolas extends MissileWeapon {
	{
		image=ItemSpriteSheet.BOLAS;
		tier=3;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (throwing&&Random.Float()<.75)Buff.affect(defender,Cripple.class,(Math.max(Random.Int(damage),2)));
		return super.proc(attacker, defender, damage);
	}

	@Override
	public int[] rangedBounds(int lvl) {
		return new int[]{
				//min
				(tier-1)+level()/2,

				//max
				(int)( (1.5f * (tier + 1)) +
						(lvl * tier * 0.75f))
		};
	}
}
