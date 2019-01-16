package com.shatteredpixel.shatteredpixeldungeon.items.wands.tripple;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SpikyRoots;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class FluidWand extends TrippleEffectWand {
	{
		firstEffect=new RootEffect();
		secondEffect = new GasEffect();
		neutralEffect=new NormalEffect();
		image=ItemSpriteSheet.WAND_REGROWTH;
		randomizeEffect();

		collisionProperties=Ballistica.STOP_TERRAIN | Ballistica.STOP_TARGET | Ballistica.PIERCE_LOS_BLOCKING;
	}

	private class RootEffect extends NormalEffect {
		@Override
		protected void affectCell(int cell) {
			super.affectCell(cell);

			Char ch = Actor.findChar(cell);
			if (ch!=null &&
					(imbue!=1 || ch.alignment==Char.Alignment.ENEMY)
					) {
				int strength = 3 + level();
				if (imbue == 1) strength *= 1.5f;
				GameScene.add(Blob.seed(cell, strength, SpikyRoots.class));
			}
		}
	}
	private class GasEffect extends NormalEffect {
		@Override
		protected void affectCell(int cell) {
			super.affectCell(cell);

			int strength = (5 + level());
			if (imbue==2)strength*=1.5f;

			GameScene.add( Blob.seed( cell, strength, ToxicGas.class ) );
		}
	}

	private class NormalEffect extends WandEffect {

		int completedNum = 3;
		final int maxSnakeLength = 3;
		final int snakesNum = 3;
		HashSet<Integer> affectedCells;

		@Override
		public void onZap(Ballistica target) {
			affectedCells = new HashSet<>();
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
						affectedCells.add(cell);
					} else break;
				}

				moveSnake(snake,0);
			}
			affectCell(target.collisionPos);
		}

		private void moveSnake(ArrayList<Integer> snake, int index){
			if (index<snake.size()-1) {
				MagicMissile.boltFromCell(
						MagicMissile.FOLIAGE,
						snake.get(index),
						snake.get(index+1),
						100,
						() ->{
							affectCell(snake.get(index+1));
							moveSnake(snake, index + 1);
						}
				);

			} else {
				completedNum++;
				if (completedNum>=snakesNum){
					wandUsed();
					completedNum=0;
				}
			}
		}

		protected void affectCell(int cell){
			int map = Dungeon.level.map[cell];
			if (map == Terrain.EMPTY ||
					map == Terrain.EMBERS ||
					map == Terrain.GRASS ||
					map == Terrain.EMPTY_DECO) {
				Level.set( cell, Actor.findChar(cell)==null?Terrain.HIGH_GRASS:Terrain.GRASS );
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

			//Snakes can't move diagonally
			for (int i=0; i<PathFinder.NEIGHBOURS4.length;i++){
				int cell = c+PathFinder.NEIGHBOURS4[i];

				if (Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&!snake.contains(cell)){
					Char ch = Actor.findChar(cell);

					//Cells with enemies have extreme priority
					if (ch!=null && ch.alignment==Char.Alignment.ENEMY)steps[i]=4096;
					//Try to get as far as possible from origin
					else if (PathFinder.distance[cell]>PathFinder.distance[c])steps[i]=2;
					//We still have chance to go closer to origin
					else steps[i]=1;

					//Only go there if have no other options
					if (affectedCells.contains(cell))steps[i]=0.01f;
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
