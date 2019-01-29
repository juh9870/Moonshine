package com.shatteredpixel.shatteredpixeldungeon.items.wands.tripple;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
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
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class ThermalWand extends TrippleEffectWand {

	{
		firstEffect = new FireEffect();
		secondEffect = new FrostEffect();
		neutralEffect = new NeutralEffect();
		randomizeEffect();
		image = ItemSpriteSheet.WAND_FIREBOLT;
	}

	private static final int aoeBallisticaParams = Ballistica.STOP_TARGET|Ballistica.STOP_TERRAIN|Ballistica.PIERCE_LOS_BLOCKING;

	private class FireEffect extends WandEffect {

		@Override
		public void onZap(Ballistica target) {

			boolean augmented = imbue == 1;
			int[] aoe = augmented ? PathFinder.NEIGHBOURS25 : PathFinder.NEIGHBOURS9;

			HashSet<Integer> validCells = new HashSet<>();

			for (int c : aoe) {
				int cell = target.collisionPos + c;

				if (new Ballistica(target.collisionPos,cell, aoeBallisticaParams,true).collisionPos!=cell)continue;

				if (!Dungeon.level.flamable[cell] &&
						(Dungeon.level.solid[cell] || Dungeon.level.pit[cell])) continue;

				Char ch = Actor.findChar(cell);
				if (ch==curUser)continue;
				if (ch!=null){
					GameScene.add(Blob.seed(cell, 1, Fire.class));
					processSoulMark(ch, chargesPerCast());
					CellEmitter.get(cell).burst(FlameParticle.FACTORY, 2);
				}
				validCells.add(cell);
			}

			int burnCellsNum = (int) Math.ceil(validCells.size() * 0.4f);
			for (int i = 0; i < burnCellsNum; i++) {
				int cell = Random.element(validCells);
				validCells.remove(cell);
				GameScene.add(Blob.seed(cell, augmented ? 2 : 4, Fire.class));
				CellEmitter.get(cell).burst(FlameParticle.FACTORY, 5);
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

			boolean augmented = imbue == 2;
			int[] aoe = augmented ? PathFinder.NEIGHBOURS25 : PathFinder.NEIGHBOURS9;

			ArrayList<Integer> validCells = new ArrayList<>();

			for (int c : aoe) {
				int cell = target.collisionPos + c;
				if (new Ballistica(target.collisionPos,cell, aoeBallisticaParams,true).collisionPos!=cell)continue;
				if (Actor.findChar(cell)==curUser)continue;
				if (!Dungeon.level.solid[cell] && !Dungeon.level.pit[cell]) validCells.add(cell);
			}
			for (int cell : validCells) {
				Heap heap = Dungeon.level.heaps.get(cell);
				if (heap != null) {
					heap.freeze();
				}

				float duration = level()+2;

				if (augmented)duration*=1.333;

				Char ch = Actor.findChar(cell);
				if (ch != null){
					if (ch.buff(Frost.class) != null || ch.buff(Chill.class) != null){
						return; //do nothing, can't affect a frozen target
					} else {
						ch.sprite.burst( 0xFF99CCFF, level() / 2 + 2 );
					}

					processSoulMark(ch, chargesPerCast());

					if (ch.isAlive()){
						if (Dungeon.level.water[ch.pos])
							Buff.prolong(ch, Chill.class, 2+duration);
						else
							Buff.prolong(ch, Chill.class, duration);
					}
				}
				GameScene.add(Blob.seed(cell,(int)duration,Freezing.class));
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

		public int min(int lvl) {
			return 1 + lvl;
		}

		public int max(int lvl) {
			return 5 + 3 * lvl;
		}

		@Override
		protected void fx(Ballistica bolt, Callback callback) {
			MagicMissile.boltFromChar(curUser.sprite.parent,
					MagicMissile.FIRE,
					curUser.sprite,
					bolt.collisionPos,
					callback);
			MagicMissile.boltFromChar(curUser.sprite.parent,
					MagicMissile.SMOKE,
					curUser.sprite,
					bolt.collisionPos,
					() -> {
					});
			MagicMissile.boltFromChar(curUser.sprite.parent,
					MagicMissile.MAGIC_MISSILE,
					curUser.sprite,
					bolt.collisionPos,
					() -> {
					});
			Sample.INSTANCE.play(Assets.SND_ZAP);
		}

		@Override
		public void onZap(Ballistica target) {
			for (int c : PathFinder.NEIGHBOURS9) {
				int cell = target.collisionPos + c;
				Dungeon.level.press(cell,null,true);
				if (cell==curUser.pos)continue;
				Char targ;
				if ((targ = Actor.findChar(cell)) != null) {
					targ.damage(damageRoll(), this);
					processSoulMark(targ, chargesPerCast());
				}
			}
			BlastWave.blast(target.collisionPos);
			wandUsed();
		}
	}

	public static class BlastWave extends Image {

		private static final float TIME_TO_FADE = 0.2f;

		private float time;

		public BlastWave() {
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
				scale.y = scale.x = (1 - p) * 3;
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
