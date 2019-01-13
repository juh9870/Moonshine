

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.locks;

import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;

public class FlintLock extends Lock {

    @Override
    public float missloadChance() {
        return 1.25f;
    }

    @Override
    public float reloadTimeMod() {
        return .8f;
    }
}
