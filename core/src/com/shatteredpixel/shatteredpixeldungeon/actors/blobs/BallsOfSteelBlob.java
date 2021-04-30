package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.BallsOfSteel;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.watabou.noosa.Camera;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Storeable;

import java.util.*;

public class BallsOfSteelBlob extends Blob {


	private static final int MAX_BOLT_DIST = 8;

	protected ArrayList<Lightning.Arc> arcs;

	@Override
	protected void evolve() {

		Set<Integer> AOE = new HashSet<>();

		int pos;
		Char ch;
		for (int i = area.left; i < area.right; i++){
			for (int j = area.top; j < area.bottom; j++){
				pos = i + j*Dungeon.level.width();

				if (Dungeon.level.insideMap(pos)) {
					if (cur[pos] > 0) {

						Heap h = Dungeon.level.heaps.get(pos);

						boolean found = false;

						if (h!=null && h.items != null) {
							for (Item itm : h.items) {
								if (itm instanceof BallsOfSteel) {
									found = true;
								}
							}
						}
						if (found){
							off[pos] = cur[pos];
							volume += off[pos];

							AOE.add(pos);
						} else {
							cur[pos]=0;
						}
					}
				}
			}
		}

		if (AOE.size()<2)return;

		int lightnings = AOE.size()-1;
		int iters = 0;

		System.out.println("Arcs: "+lightnings);

		arcs = new ArrayList<>();
		Set strikeCells = new HashSet();

		int from = Random.element(AOE);
		AOE.remove(from);
		int to;
		while (lightnings>0 && iters++<1000){
			to = Random.element(AOE);
			Ballistica bolt = new Ballistica(from,to,Ballistica.STOP_TERRAIN|Ballistica.STOP_TARGET|Ballistica.PIERCE_LOS_BLOCKING,true);
			if (bolt.collisionPos!=to)continue;
			if (bolt.dist>MAX_BOLT_DIST)continue;

			lightnings--;

			if (Dungeon.level.heroFOV[from]||Dungeon.level.heroFOV[to]) {
				Char ch1;
				if ((ch = Actor.findChar(from)) != null) {
					if ((ch1 = Actor.findChar(to)) != null) {
						arcs.add(new Lightning.Arc(ch.sprite.center(), ch1.sprite.center()));
					} else {
						arcs.add(new Lightning.Arc(ch.sprite.center(), to));
					}
				} else {
					if ((ch1 = Actor.findChar(to)) != null) {
						arcs.add(new Lightning.Arc(from, ch1.sprite.center()));
					} else {
						arcs.add(new Lightning.Arc(from, to));
					}
				}
			}

			List<Integer> targs = bolt.subPath(0,bolt.dist);
			for (int i=0; i<targs.size(); i++){
				int cell = targs.get(i);
				strikeCells.add(cell);
				if (i!=0&&i!=targs.size()-1)
					for (int c : PathFinder.NEIGHBOURS4) {
						strikeCells.add(cell+c);
					}
			}
			AOE.remove(to);
			if (Random.Boolean())
				from=to;

		}
		if (!arcs.isEmpty())
			CellEmitter.get(from).parent.add(new Lightning(arcs, () -> strikeCells.forEach(o -> strikeCell((int)o))));
	}

	protected void strikeCell(int cell){
		Char ch = Actor.findChar(cell);
		if (ch!=null && !ch.isImmune(getClass())){
			float dmg = 2 * Random.Float(.8f, 1.2f);
			if (Dungeon.level.water[cell]&&!ch.flying)dmg*=1.5;
			ch.damage((int)Math.floor(dmg),this);

			if (ch == Dungeon.hero) Camera.main.shake( 2, 0.3f );
			ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
			ch.sprite.flash();
		}
	}
}
