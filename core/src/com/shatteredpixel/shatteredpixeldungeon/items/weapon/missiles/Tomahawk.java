package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Tomahawk extends MissileWeapon {
    {
        image = ItemSpriteSheet.THROWING_KNIFE;
        tier=5;
    }

	@Override
    public float stickChance() {
        return 0.2f;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (throwing&&Random.Float()<.1f)Buff.affect(defender,Bleeding.class).set(Math.max(Random.Int(damage)/2,1));
        return super.proc(attacker, defender, damage);
    }
}
