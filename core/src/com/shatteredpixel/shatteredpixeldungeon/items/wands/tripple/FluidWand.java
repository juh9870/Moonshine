package com.shatteredpixel.shatteredpixeldungeon.items.wands.tripple;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Regrowth;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.misc.EmptyItem;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.watabou.utils.Callback;
import com.watabou.utils.GameArrays;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class FluidWand extends TrippleEffectWand {
	{
		firstEffect=secondEffect=neutralEffect=new NormalEffect();
		image=ItemSpriteSheet.WAND_REGROWTH;
		randomizeEffect();
	}

	private class NormalEffect extends WandEffect {

		int completedNum = 0;
		final int maxSnakeLength = 4;
		final int snakesNum = 3;

		@Override
		public void onZap(Ballistica target) {


			PathFinder.buildDistanceMap(target.collisionPos,Dungeon.level.passable,maxSnakeLength);
			ArrayList<Integer>[] snakes = new ArrayList[snakesNum];

			for (int i=0;i<snakes.length;i++){
				ArrayList<Integer> snake = snakes[i]=new ArrayList<>();
				int c = target.collisionPos;
				snake.add(c);
				for (int j=0;j<maxSnakeLength;j++){
					Integer cell = step(snake);
					if (cell!=null){
						snake.add(cell);
					} else break;
				}
			}

			for (ArrayList<Integer> snake : snakes){
				moveSnake(snake,0);
			}
			affectCell(target.collisionPos);
		}

		private void moveSnake(ArrayList<Integer> snake, int index){
			if (index<snake.size()-1) {
				((MissileSprite) curUser.sprite.parent.recycle(MissileSprite.class)).
						reset(snake.get(index),
								snake.get(index + 1),
								new EmptyItem(),
								() ->{
									affectCell(snake.get(index+1));
									moveSnake(snake, index + 1);
								});
			} else {
				completedNum++;
				if (completedNum>=snakesNum){
					wandUsed();
					completedNum=0;
				}
			}
		}

		protected void affectCell(int cell){
//			GameScene.add( Blob.seed( cell, 10, Regrowth.class ) );
			int map = Dungeon.level.map[cell];
			if (map == Terrain.EMPTY ||
					map == Terrain.EMBERS ||
					map == Terrain.EMPTY_DECO) {
				Level.set( cell, Terrain.GRASS );
				GameScene.updateMap(cell);
			}
			CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC,10);
		}

		private Integer step(ArrayList<Integer> snake){
			float[] steps = new float[]{
					0,
					0,
					0,
					0,
			};

			boolean foundCell = false;

			int c = snake.get(snake.size()-1);
			for (int i=0; i<PathFinder.NEIGHBOURS4.length;i++){
				int cell = c+PathFinder.NEIGHBOURS4[i];

				if (Dungeon.level.passable[cell]&&!snake.contains(cell)){
					if (PathFinder.distance[cell]>PathFinder.distance[c])steps[i]=4;
					else steps[i]=1;
					foundCell=true;
				}
			}

			if (foundCell){
				return c+PathFinder.NEIGHBOURS4[Random.chances(steps)];
			}
			return null;
		}

		@Override
		protected void fx(Ballistica bolt, Callback callback) {
			MagicMissile.boltFromChar(curUser.sprite.parent,
					MagicMissile.FOLIAGE,
					curUser.sprite,
					bolt.collisionPos,
					callback);
		}
	}
}
