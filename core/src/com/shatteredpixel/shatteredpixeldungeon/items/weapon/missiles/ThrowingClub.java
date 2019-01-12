package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class ThrowingClub extends MissileWeapon {
    {
        image = ItemSpriteSheet.WEAPON_HOLDER;
        tier=3;
    }



    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (throwing&&Random.Float()<.1f)Buff.affect(defender,Vertigo.class,(Math.max(Random.Int(damage)/2,1)));
        return super.proc(attacker, defender, damage);
    }
}
