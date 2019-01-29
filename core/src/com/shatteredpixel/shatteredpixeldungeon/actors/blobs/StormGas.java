/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Camera;
import com.watabou.utils.Random;
import com.watabou.utils.Storeable;

import java.util.*;

public class StormGas extends Blob {

	private static final int MAX_BOLT_DIST = 8;

	@Storeable
	protected int strength = 0;
	protected ArrayList<Lightning.Arc> arcs;


	@Override
	protected void evolve() {
		super.evolve();

		HashMap<Integer,Float> AOE = new HashMap<>();

		if (volume<=0){
			strength=0;
			return;
		}

		int pos;
		Char ch;
		for (int i = area.left; i < area.right; i++){
			for (int j = area.top; j < area.bottom; j++){
				pos = i + j*Dungeon.level.width();
				if (cur[pos] > 0) {
					float weight = 1;
					if ((ch=Actor.findChar(pos))!=null&&ch!=Dungeon.hero)weight*=4096;
					if (Dungeon.level.water[pos])weight*=4;
					AOE.put(pos,weight);
				}
			}
		}

		if (AOE.size()<2)return;

		// http://juh9870.cf/rnd/?code=return%20Math.random()*Math.random()*2%1;
		int lightnings = Math.min(Random.roundWeighted((float)((1-Random.Float()*Random.Float()*3%1)*Math.sqrt(AOE.size()))),AOE.size()-1);
		int iters = 0;

		if (lightnings<=0)return;

		System.out.println("Arcs: "+lightnings);

		arcs = new ArrayList<>();
		Set strikeCells = new HashSet();

		int from = Random.chances(AOE);
		AOE.remove(from);
		int to;
		while (lightnings>0 && iters++<1000){
			to = Random.chances(AOE);
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
			for (int cell : targs){
				strikeCells.add(cell);
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
			float dmg = strength * Random.Float(.8f, 1.2f);
			if (Dungeon.level.water[cell]&&!ch.flying)dmg*=1.5;
			ch.damage((int)Math.floor(dmg),this);

			if (ch == Dungeon.hero) Camera.main.shake( 2, 0.3f );
			ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
			ch.sprite.flash();
		}
	}

	public void setStrength(int strength) {
		this.strength = Math.max(strength,this.strength);
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );

		emitter.pour( Speck.factory( Speck.STORMGAS ), 0.2f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}