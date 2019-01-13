

package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.collectors.Collector;
import com.shatteredpixel.shatteredpixeldungeon.items.collectors.PowderHorn;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.ammo.Bullet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class BulletGun extends Gun<Bullet> {

    {
        canEquip=true;
        needEquip=true;

        backfire=true;
        knockback=true;
        disarm=true;

        DEBUFF_RATE=1f/9f;
        CONSUMABLE_COUNT=1;
        MISSLOAD_CHANCE=.1f;
        DISARM_CHANCE=1f/3f;

        load.fill(defAmmoClass());
        if (!(this instanceof GunslingerPistol)) {
            attachManager.random();
        }
    }

    @Override
    public Class<? extends Collector> collector() {
        return PowderHorn.class;
    }

    @Override
    public Class<? extends Bullet> defAmmoClass() {
        return Bullet.class;
    }

    @Override
    public Class<? extends Buff>[] buffs() {
        return super.buffs();
    }

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc");
        desc+="\n\n";
        if (levelKnown) {
            desc += Messages.get(BulletGun.class, "stats_desc", tier, minGun(), maxGun(), min(), max(), STRReq());
        } else {
            desc += Messages.get(BulletGun.class, "stats_desc_ui", tier, minGun(0), maxGun(0), min(0), max(0), STRReq(0));
        }
        desc += "\n" + Messages.get(BulletGun.class, "req_desc", attachManager.consumablesCount(), 1);
        desc += "\n\n" + Messages.get(BulletGun.class, "attachments");
        for (String s : attachManager.names()){
            desc += "\n" + Messages.get(BulletGun.class, "attach",s);
        }
//        desc+=(broken()?"\n"+Messages.get(Item.class,"brokendesc"):"");
        return desc;
    }
}
