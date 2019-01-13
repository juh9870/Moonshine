

package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Fusil extends BulletGun {
    {
        image = ItemSpriteSheet.FUSIL;
        tier = 5;

        ACCURACY_MOD=1.5f;

        CONSUMABLE_COUNT=3;
    }

    @Override
    public int maxLoadSize() {
        return 2;
    }
}
