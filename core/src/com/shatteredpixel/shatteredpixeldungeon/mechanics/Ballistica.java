/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Yet Another Pixel Dungeon
 * Copyright (C) 2015-2016 Considered Hamster
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
package com.shatteredpixel.shatteredpixeldungeon.mechanics;


import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.ArrayList;
import java.util.List;

public final class Ballistica {

	public ArrayList<Integer> path = new ArrayList<>();
	public Integer sourcePos = null;
	public Integer collisionPos = null;
	public int dist;

	private int stepA;
	private int stepB;
	private int dA;
	private int dB;

	private boolean hit;

	//parameters to specify the colliding cell
	public static final int STOP_TARGET = 1; 		//ballistica will stop at the target cell
	public static final int STOP_CHARS = 2; 		//ballistica will stop on first char hit
	public static final int STOP_TERRAIN = 4; 		//ballistica will stop on terrain

	public static final int PIERCE_LOS_BLOCKING = 8;//ballistica won't stop on LOS blocking
	public static final int PIERCE_DOORS = 16; 		//ballistica won't stop on Doors

	public static final int PROJECTILE = STOP_TARGET | STOP_CHARS | STOP_TERRAIN ;

	public static final int MAGIC_BOLT = STOP_CHARS | STOP_TERRAIN;

	public static final int WONT_STOP = PIERCE_DOORS | PIERCE_LOS_BLOCKING;

	public Ballistica(int from, int to, int params){
		sourcePos = from;
		collisionPos=cast(from, to, (params & STOP_TARGET) > 0, (params & STOP_CHARS) > 0, (params & STOP_TERRAIN) > 0, (params & PIERCE_LOS_BLOCKING) > 0, (params & PIERCE_DOORS) > 0);
	}

	private int cast(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean pierceLOS, boolean pierceDoors) {

		int w = Dungeon.level.width();

		int x0 = from % w;
		int x1 = to % w;
		int y0 = from / w;
		int y1 = to / w;

		int dx = x1 - x0;
		int dy = y1 - y0;

		int stepX = dx > 0 ? +1 : -1;
		int stepY = dy > 0 ? +1 : -1;

		dx = Math.abs(dx);
		dy = Math.abs(dy);

		if (dx > dy) {

			stepA = stepX;
			stepB = stepY * w;
			dA = dx;
			dB = dy;

		} else {

			stepA = stepY * w;
			stepB = stepX;
			dA = dy;
			dB = dx;

		}

		int cell = calc(from, to, stopChars, stopTarget, stopTerrain, pierceLOS, pierceDoors, dA / 2);

		if (!hit) {

			for (int err = 0; err <= dA; err++) {
				int calc = calc(from, to, stopChars, stopTerrain, stopTarget, pierceLOS, pierceDoors, err);
				if (hit) {
					cell = calc;
					break;
				}
			}
		}

		return cell;

	}

	private int calc(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean pierceLOS, boolean pierceDoors, int err) {

		hit = false;
		dist = 0;
		path.clear();
		path.add(from);

		boolean collided = false;

		int cell = from;

		while ( Dungeon.level.insideMap(cell) ) {

			cell += stepA;

			err += dB;

			if ( err >= dA ) {
				err = err - dA;
				cell = cell + stepB;
			}



			path.add( cell );

			if (!collided) {

				dist++;

				if ( cell == to ) {
					hit = true;
					if (stopTarget){
						collided=true;
					}
				}

				if (stopTerrain && !Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) {
					do {
						dist--;
					} while (!Dungeon.level.passable[path.get(dist)] && !Dungeon.level.avoid[path.get(dist)]);
					collided = true;
				} else if (!pierceLOS && Dungeon.level.losBlocking[cell]){
					collided = true;
				} else if (stopChars && Actor.findChar(cell) != null) {
					collided = true;
				} else if (!pierceDoors&&Dungeon.level.map[cell] == Terrain.DOOR) {
					collided = true;
				}
			}
		}

		return path.get(dist);
	}

	//returns a segment of the path from start to end, inclusive.
	//if there is an error, returns an empty arraylist instead.
	public List<Integer> subPath(int start, int end){
		try {
			end = Math.min( end, path.size()-1);
			return path.subList(start, end+1);
		} catch (Exception e){
			ShatteredPixelDungeon.reportException(e);
			return new ArrayList<>();
		}
	}
}
