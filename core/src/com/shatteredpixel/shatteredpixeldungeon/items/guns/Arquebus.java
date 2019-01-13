

package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Arquebus extends BulletGun {
    {
        image = ItemSpriteSheet.ARQUEBUS;
        tier=4;
        PIERCE_CHANCE=.75f;
        PIERCE_DECAY=1/3f;
        PIERCE_DMG_REDUCE=.75f;

        CONSUMABLE_COUNT=4;
    }
}
