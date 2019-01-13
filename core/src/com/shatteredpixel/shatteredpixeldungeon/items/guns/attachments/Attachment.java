

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.Ammo;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.Gun;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Attachment implements Bundlable {

    public float acuMod(){
        return 1;
    }

    public float reloadTimeMod(){
        return 1;
    }

    public float meleeDlyMod(){
        return 1;
    }

    public float meleeMinDmgMod(){
        return 1;
    }

    public float meleeMaxDmgMod(){
        return 1;
    }

    public float knockMod(){
        return 1;
    }

    public float minDmgMod(){
        return 1;
    }

    public float maxDmgMod(){
        return 1;
    }

    public float pierceMod(){
        return 1;
    }

    public float missfireChance(){
        return 1;
    }

    public float missloadChance(){
        return 1;
    }

    public int consumablesMod() {
        return 0;
    }

    public float enemyArmorMod(){
        return 1;
    }

    public<T extends Ammo> int attackProc(Char enemy, int damage, Gun g, T ammo){
        return damage;
    }

    public String name(){
        return Messages.titleCase(Messages.get(this,"name"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
    }
}
