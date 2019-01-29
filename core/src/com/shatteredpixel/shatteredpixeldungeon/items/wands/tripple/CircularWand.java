package com.shatteredpixel.shatteredpixeldungeon.items.wands.tripple;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.DewGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CheckedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class CircularWand extends TrippleEffectWand {

	//Light damage source for resistances
	public static class Light {}

	{
		neutralEffect=new NormalEffect();
		firstEffect=new LightEffect();
		secondEffect=new StormEffect();

		image=ItemSpriteSheet.WAND_FROST;
		randomizeEffect();

		collisionProperties=Ballistica.STOP_TERRAIN | Ballistica.STOP_TARGET | Ballistica.PIERCE_LOS_BLOCKING;
	}

	private class StormEffect extends WandEffect {

		@Override
		public void onZap(Ballistica target) {

			GameScene.add(Blob.seed(target.collisionPos, 50 + 10 * level(), DewGas.class));
			((DewGas) Dungeon.level.blobs.get(DewGas.class)).storm(target.collisionPos);
			StormGas sg = (StormGas) Dungeon.level.blobs.get(StormGas.class);
			int strength = level()+1;
			if (imbue==2)strength*=1.333f;
			sg.setStrength(strength);
			wandUsed();

		}
	}

	private class LightEffect extends DamageWandEffect {

		private static final int DIST = 8;
		@Override
		public void onZap(Ballistica target) {
			boolean[] FOV = new boolean[Dungeon.level.length()];
			Point c = Dungeon.level.cellToPoint(target.collisionPos);
			ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, DIST);

			int sX = Math.max(0, c.x - DIST);
			int eX = Math.min(Dungeon.level.width()-1, c.x + DIST);

			int sY = Math.max(0, c.y - DIST);
			int eY = Math.min(Dungeon.level.height()-1, c.y + DIST);

			boolean noticed = false;

			float noticeChance = .5f;
			if (imbue==1)noticeChance=.75f;

			for (int y = sY; y <= eY; y++){
				int curr = y*Dungeon.level.width() + sX;
				for ( int x = sX; x <= eX; x++){

					if (FOV[curr]){
						Dungeon.level.mapped[curr] = true;

						if (Dungeon.level.secret[curr] && Random.Float()<noticeChance) {
							Dungeon.level.discover(curr);

							if (Dungeon.level.heroFOV[curr]) {
								GameScene.discoverTile(curr, Dungeon.level.map[curr]);
								ScrollOfMagicMapping.discover(curr);
								noticed = true;
							}
						}

						Char ch = Actor.findChar(curr);
						if (ch!=null&&(ch.properties().contains(Char.Property.UNDEAD)||ch.properties().contains(Char.Property.DEMONIC))){
							ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10+level() );
							Sample.INSTANCE.play(Assets.SND_BURNING);

							float dmg = (int)(Random.NormalIntRange(min(),max())*1.333f);
							if (imbue==1)dmg*=1.333f;
							ch.damage(Math.round(dmg), Light.class);
						}
						CellEmitter.get( curr ).start( ShaftParticle.FACTORY, 0.5f, 1 );
					}
					curr++;
				}
			}

			if (noticed) {
				Sample.INSTANCE.play( Assets.SND_SECRET );
			}

			Sample.INSTANCE.play( Assets.SND_TELEPORT );
			GameScene.updateFog();

			neutralEffect.onZap(target);
		}


		@Override
		protected void fx(Ballistica bolt, Callback callback) {
			MagicMissile.boltFromChar(curUser.sprite.parent,MagicMissile.RAINBOW,curUser.sprite,bolt.collisionPos,callback);
		}

		public int min(int lvl) {
			return 1 + lvl;
		}

		public int max(int lvl) {
			return 5 + 3 * lvl;
		}
	}

	private class NormalEffect extends WandEffect {

		@Override
		public void onZap(Ballistica target) {
			GameScene.add(Blob.seed(target.collisionPos, 50 + 10 * level(), DewGas.class));
			wandUsed();
		}

		@Override
		protected void fx(Ballistica bolt, Callback callback) {
			MagicMissile.boltFromChar(curUser.sprite.parent,MagicMissile.BEACON,curUser.sprite,bolt.collisionPos,callback);
		}
	}
}
