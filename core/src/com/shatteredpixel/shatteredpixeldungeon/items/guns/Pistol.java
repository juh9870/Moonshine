

package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Pistol extends BulletGun {
    {
        image = ItemSpriteSheet.PISTOL;
        tier=2;

        CONSUMABLE_COUNT = 2;
    }

    @Override
    public int maxLoadSize() {
        return 2;
    }
}
