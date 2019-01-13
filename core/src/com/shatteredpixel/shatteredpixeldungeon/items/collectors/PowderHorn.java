

package com.shatteredpixel.shatteredpixeldungeon.items.collectors;

import com.shatteredpixel.shatteredpixeldungeon.items.misc.Gunpowder;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class PowderHorn extends Collector<Gunpowder> {

    {
        image = ItemSpriteSheet.POWDERHORN;
    }

    @Override
    public int maxVolume() {
        return 300;
    }
}
