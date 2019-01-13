

package com.shatteredpixel.shatteredpixeldungeon.items.misc;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.collectors.PowderHorn;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gunpowder extends Item {
    {
        image = ItemSpriteSheet.GUNPOWDER;

        stackable = true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean doPickUp(Hero hero) {
        PowderHorn ph = hero.belongings.getItem(PowderHorn.class);
        if (ph!=null){
            ph.collect(this);
        } else {
            ArrayList<Integer> cells = new ArrayList<Integer>();
            int pos = hero.pos;
            for (int c : PathFinder.NEIGHBOURS8) {
                if ((Dungeon.level.passable[c+pos]||Dungeon.level.avoid[c+pos])&&!Dungeon.level.pit[c+pos]){
                    cells.add(c+pos);
                }
            }
            if (cells.size()>0){
                Dungeon.level.drop(this, Random.element(cells)).sprite.drop();
				hero.spendAndNext( TIME_TO_PICK_UP );
				return true;
            }
            return false;
        }


        Sample.INSTANCE.play( Assets.SND_DEWDROP );
        hero.spendAndNext( TIME_TO_PICK_UP );

        return true;
    }
}
