package com.shatteredpixel.shatteredpixeldungeon.items.wands.Tripple;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class ThermalWand extends TrippleEffectWand {

    {
        firstEffect     =new FireEffect();
        secondEffect    =new FrostEffect();
        neutralEffect   =new NeutralEffect();
        randomizeEffect();
        image=ItemSpriteSheet.WAND_HOLDER;
    }

    @Override
    protected int initialCharges() {
        return 500;
    }

    private class FireEffect extends WandEffect {

        @Override
        public void onZap(Ballistica target) {
            for (int c : PathFinder.NEIGHBOURS9){
                int cell = target.collisionPos+c;
                Char targ;
                GameScene.add(Blob.seed(cell,2,Fire.class));
                CellEmitter.get(cell).burst(FlameParticle.FACTORY,5);
            }
            neutralEffect.onZap(target);
        }
        @Override
        protected void fx(Ballistica bolt, Callback callback) {
            MagicMissile.boltFromChar(curUser.sprite.parent,
                    MagicMissile.FIRE,
                    curUser.sprite,
                    bolt.collisionPos,
                    callback);
            Sample.INSTANCE.play(Assets.SND_ZAP);
        }
    }
    private class FrostEffect extends WandEffect {
        @Override
        public void onZap(Ballistica target) {
            for (int c : PathFinder.NEIGHBOURS9){
                int cell = target.collisionPos+c;
                GameScene.add(Blob.seed(cell,4,Freezing.class));
                CellEmitter.get(cell).start(SnowParticle.FACTORY,.1f,6);
            }
            neutralEffect.onZap(target);
        }
        @Override
        protected void fx(Ballistica bolt, Callback callback) {
            MagicMissile.boltFromChar(curUser.sprite.parent,
                    MagicMissile.FROST,
                    curUser.sprite,
                    bolt.collisionPos,
                    callback);
            Sample.INSTANCE.play(Assets.SND_ZAP);
        }
    }
    private class NeutralEffect extends DamageWandEffect {

        public int min(int lvl){
            return 1+lvl;
        }

        public int max(int lvl){
            return 5+3*lvl;
        }

        @Override
        protected void fx(Ballistica bolt, Callback callback) {
            MagicMissile.boltFromChar( curUser.sprite.parent,
                    MagicMissile.FIRE,
                    curUser.sprite,
                    bolt.collisionPos,
                    callback);
            MagicMissile.boltFromChar(curUser.sprite.parent,
                    MagicMissile.SMOKE,
                    curUser.sprite,
                    bolt.collisionPos,
                    ()->{});
            MagicMissile.boltFromChar(curUser.sprite.parent,
                    MagicMissile.MAGIC_MISSILE,
                    curUser.sprite,
                    bolt.collisionPos,
                    ()->{});
            Sample.INSTANCE.play( Assets.SND_ZAP );
        }

        @Override
        public void onZap(Ballistica target) {
            for (int c : PathFinder.NEIGHBOURS9){
                int cell = target.collisionPos+c;
                Char targ;
                if ((targ=Actor.findChar(cell))!=null){
                    targ.damage(damageRoll(),this);
                    processSoulMark(targ, chargesPerCast());
                }
            }
            BlastWave.blast(target.collisionPos);
        }
    }

    public static class BlastWave extends Image {

        private static final float TIME_TO_FADE = 0.2f;

        private float time;

        public BlastWave(){
            super(Effects.get(Effects.Type.RIPPLE));
            origin.set(width / 2, height / 2);
        }

        public void reset(int pos) {
            revive();

            x = (pos % Dungeon.level.width()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - width) / 2;
            y = (pos / Dungeon.level.width()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - height) / 2;

            time = TIME_TO_FADE;
        }

        @Override
        public void update() {
            super.update();

            if ((time -= Game.elapsed) <= 0) {
                kill();
            } else {
                float p = time / TIME_TO_FADE;
                alpha(p);
                scale.y = scale.x = (1-p)*3;
            }
        }

        public static void blast(int pos) {
            Group parent = Dungeon.hero.sprite.parent;
            WandOfBlastWave.BlastWave b = (WandOfBlastWave.BlastWave) parent.recycle(WandOfBlastWave.BlastWave.class);
            parent.bringToFront(b);
            b.reset(pos);
        }

    }
}
