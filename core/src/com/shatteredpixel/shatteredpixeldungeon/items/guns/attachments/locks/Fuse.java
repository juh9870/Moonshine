

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.locks;

import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;

public class Fuse extends Lock {
    @Override
    public float missloadChance() {
        return 0.5f;
    }

    @Override
    public int consumablesMod() {
        return 1;
    }
}
