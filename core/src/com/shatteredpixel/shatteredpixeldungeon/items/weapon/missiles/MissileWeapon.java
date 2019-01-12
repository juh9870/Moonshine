package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Storeable;

abstract public class MissileWeapon extends MeleeWeapon {

    public static final String AC_YELL		= "YELL";

    {
        stackable=true;

        weight=0.3f;
        defaultAction = AC_YELL;
        usesTargeting = false;
    }

    @Override
    public int STRReq(int lvl){
        lvl = Math.max(0, lvl);
        //strength req decreases at +1,+3,+6,+10,etc.
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    public float stickChance(){
        return 0;
    }
    public float unStickChance(){
        return 0.1f;
    }

    @Override
    public int image() {
        return super.image();
    }

    @Override
    public int max(int lvl) {
        return throwing?rangedBounds(lvl)[1]:meleeBounds(lvl)[1];
    }

    @Override
    public int min(int lvl) {
        return throwing?rangedBounds(lvl)[0]:meleeBounds(lvl)[0];
    }

    public int[] rangedBounds(int lvl){
        return new int[]{
                //min
                super.min(lvl),

                //max
                (int)(2.5f*(tier+1)) +
                        lvl*tier
        };
    }

    public int[] meleeBounds(int lvl){
        return new int[]{
                //min
                tier,

                //max
                tier+(tier*lvl)/2
        };
    }

    @Storeable
    public int ammo = 2, maxAmmo = 2;

    public boolean throwing = false;

    public void recalculateAmmo(){
        int newMaxAmmo = 2+(int)(Math.sqrt(8 * Math.max(level(),0) + 1) - 1)/2;
        ammo+=newMaxAmmo-maxAmmo;
        maxAmmo=newMaxAmmo;
        updateQuickslot();
    }

    @Override
    public void level(int value) {
        super.level(value);
        recalculateAmmo();
    }

    @Override
    public Item upgrade() {
        super.upgrade();
        recalculateAmmo();
        return this;
    }

    @Override
    public Item degrade() {
        super.degrade();
        recalculateAmmo();
        return this;
    }


    @Override
    public void cast(Hero user, int dst) {
        if (!user.belongings.weapon.isEquipped(this)){
            super.cast(user,dst);
            return;
        }

        final MissileWeapon tw = detachOne();

        if (tw==null) {
			return;
		}

        final int cell = throwPos( user, dst );
        user.sprite.zap( cell );
        user.busy();

        Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

        Char enemy = Actor.findChar( cell );
        QuickSlotButton.target(enemy);

        final float delay = castDelay(user, dst);

        if (enemy != null) {
            ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                    reset(user.sprite,
                            enemy.sprite,
                            this,
                            () -> {
                                tw.onThrow(cell);
                            });
        } else {
            ((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
                    reset(user.sprite,
                            cell,
                            this,
                            () -> {
                                tw.onThrow(cell);
                            });
        }
    }

    @Override
    public boolean isSimilar(Item item) {
        return super.isSimilar(item) && ((MissileWeapon)item).uuid.equals(uuid);
    }

    @Override
    public Item merge(Item other) {
        if (isSimilar(other)){
            MissileWeapon to = (MissileWeapon)other;
                ammo+=to.ammo;
                to.ammo=0;
                level(Math.max(level(),to.level()));
        }
        return this;
    }

    private MissileWeapon detachOne(){
        if (ammo <=0){
            GLog.n(Messages.get(this,"noammo"));
            return null;
        }
        ammo--;
        MissileWeapon w = (MissileWeapon)clone();
        w.ammo=1;
        w.uuid = uuid;
        updateQuickslot();

        return w;
    }

    @Override
    public boolean isPlaceholder() {
        return ammo<=0;
//        return false;
    }

    @Override
    public String status() {
        return ammo+"/"+maxAmmo;
    }

    protected void rangedMiss(int cell ) {
        super.onThrow(cell);
        curUser.spendAndNext(castDelay(curUser,cell));
    }

    protected void rangedHit( Char enemy, int cell ){
        if (enemy.isAlive()&&Random.Float()<stickChance()){
            PinCushion p = Buff.affect(enemy, PinCushion.class);
            if (p.target == enemy){
                p.stick(this);
            }
        } else {
            super.onThrow(cell);
        }
        curUser.spendAndNext(castDelay(curUser,cell));
    }

    @Override
    public boolean doEquip(Hero hero) {
        if(super.doEquip(hero)){
            defaultAction=AC_THROW;
            usesTargeting=true;
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if(super.doUnequip(hero, collect, single)){
            defaultAction=AC_YELL;
            usesTargeting=false;
            return true;
        }
        return false;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_YELL)){
            GLog.n(Messages.get(EquipableItem.class,"needequip"));
        }
    }

    @Override
    protected void onThrow( int cell ) {
        Char enemy = Actor.findChar( cell );
        if (enemy == null || enemy == curUser) {
            super.onThrow( cell );
            curUser.spendAndNext(castDelay(curUser,cell));
        } else {
            throwing=true;
            if (!curUser.shoot( enemy, this )) {
                rangedMiss( cell );
            } else {
                rangedHit( enemy, cell );
            }
            throwing=false;
        }
    }
}
