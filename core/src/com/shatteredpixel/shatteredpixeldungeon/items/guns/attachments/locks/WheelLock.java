

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.locks;

import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;

public class WheelLock extends Lock {

    @Override
    public float missloadChance() {
        return .75f;
    }

    @Override
    public float reloadTimeMod() {
        return 1.2f;
    }
}
