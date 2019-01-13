

package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

public interface RangedAttacker {
    boolean canShoot(Hero user, Char enemy);

    void doShoot(Hero user, Char target);
}
