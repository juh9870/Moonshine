

package com.shatteredpixel.shatteredpixeldungeon.items.guns;


import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Blunderbuss extends BulletGun {
    {
        image = ItemSpriteSheet.BLUNDERBUSS;
        tier=3;

        ENEMY_ARMOR_MOD=3f;
        TIME_TO_RELOAD=1.5f;
        DEBUFF_RATE=1f/3f;
        DEBUFF_TRIES=3;

        CONSUMABLE_COUNT=3;

        breakDoors=true;
        breakGrass=true;
    }

    @Override
    public int maxLoadSize() {
        return 2;
    }

    @Override
    public int flatMaxGun(int lvl) {
        return (int)(4*(tier)+lvl*(tier+.5));
    }
}
