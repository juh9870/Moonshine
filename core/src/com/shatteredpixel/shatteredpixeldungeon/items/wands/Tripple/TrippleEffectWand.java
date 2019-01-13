package com.shatteredpixel.shatteredpixeldungeon.items.wands.Tripple;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Storeable;
import static java.lang.Math.pow;

public abstract class TrippleEffectWand extends Wand {
    private static final float exponent = 2.53f; //log(33)/log(4)

    public WandEffect firstEffect;
    public WandEffect secondEffect;
    public WandEffect neutralEffect;

    @Storeable
    public int consistentCasts = 0; //x>0 first effect
                                    //x<0 second effect
    public WandEffect curEffect;

    //see https://www.desmos.com/calculator/ugkiezcxob
    protected float[] effectsChances(){
        int n = consistentCasts;
        float a = (float)Math.min(Math.max(0,.33-(pow(Math.max(0,n),exponent)*.01)+(pow(Math.max(0,-n),exponent)*.01)),0.66);
        return new float[]{
                a,      //first effect
                .66f-a, //second effect
                .33f    //neutral effect
        };
    }

    protected void randomizeEffect(){
        int effect = Random.chances(effectsChances());
        if (effect==0)consistentCasts=Math.max(0,consistentCasts)+1;
        else consistentCasts-=Math.signum(consistentCasts);
        curEffect = new WandEffect[]{
                firstEffect,
                secondEffect,
                neutralEffect
        }[effect];
    }

    private static final String CURRENT_EFFECT = "cureffect";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        if (curEffect==firstEffect)bundle.put(CURRENT_EFFECT,0);
        else if (curEffect==secondEffect)bundle.put(CURRENT_EFFECT,1);
        else bundle.put(CURRENT_EFFECT,2);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        switch (bundle.getInt(CURRENT_EFFECT)){
            case 0:
                curEffect=firstEffect;
                break;
            case 1:
                curEffect=secondEffect;
                break;
            default:
                curEffect=neutralEffect;
        }
    }

    @Override
    protected void onZap(Ballistica attack) {
        //Do nothing. Effects zaps instead.
    }
    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        // TODO: do something on hit
    }

    public abstract class WandEffect {
        public abstract void onZap(Ballistica target);
        protected void fx( Ballistica bolt, Callback callback ) {
            MagicMissile.boltFromChar( curUser.sprite.parent,
                    MagicMissile.MAGIC_MISSILE,
                    curUser.sprite,
                    bolt.collisionPos,
                    callback);
            Sample.INSTANCE.play( Assets.SND_ZAP );
        }
        public int chargesPerCast(){
            return 1;
        }
        public int collisionProperties = Ballistica.MAGIC_BOLT;
    }

    public abstract class DamageWandEffect extends WandEffect implements Hero.Doom {
        public int min(){
            return min(level());
        }

        public abstract int min(int lvl);

        public int max(){
            return max(level());
        }

        public abstract int max(int lvl);

        public int damageRoll(){
            return Random.NormalIntRange(min(), max());
        }

        public int damageRoll(int lvl){
            return Random.NormalIntRange(min(lvl), max(lvl));
        }

        @Override
        public void onDeath() {
            Dungeon.fail( getClass() );
            GLog.n( Messages.get(this, "ondeath") );
        }
    }

    public static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                final TrippleEffectWand curWand;
                if (curItem instanceof TrippleEffectWand) {
                    curWand = (TrippleEffectWand) Wand.curItem;
                } else {
                    return;
                }

                WandEffect curEffect=curWand.curEffect;

                final Ballistica shot = new Ballistica( curUser.pos, target, curEffect.collisionProperties);
                int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));

                if (curWand.curCharges >= (curWand.cursed ? 1 : curEffect.chargesPerCast())) {

                    curUser.busy();

                    if (curWand.cursed){
                        CursedWand.cursedZap(curWand, curUser, new Ballistica( curUser.pos, target, Ballistica.MAGIC_BOLT));
                        if (!curWand.cursedKnown){
                            curWand.cursedKnown = true;
                            GLog.n(Messages.get(Wand.class, "curse_discover", curWand.name()));
                        }
                    } else {
                        curEffect.fx(shot, new Callback() {
                            public void call() {
                                curEffect.onZap(shot);
                                curWand.randomizeEffect();
                                curWand.wandUsed();
                            }
                        });
                    }

                    Invisibility.dispel();

                } else {

                    GLog.w( Messages.get(Wand.class, "fizzles") );

                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };
}