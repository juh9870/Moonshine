package com.shatteredpixel.shatteredpixeldungeon.items.weapon.types;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

public interface Shield {
    default int defenseFactor( Char owner ){
        return 0;
    }

    default float offhandModifier( Char owner ) {
        return 0.5f;
    }
}
