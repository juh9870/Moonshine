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
	public Integer dist = 0;

	private int stepA;
	private int stepB;
	private int dA;
	private int dB;

	private boolean hit;

	//parameters to specify the colliding cell
	public static final int STOP_TARGET = 1; //ballistica will stop at the target cell
	public static final int STOP_CHARS = 2; //ballistica will stop on first char hit
	public static final int STOP_TERRAIN = 4; //ballistica will stop on terrain
	public static final int STOP_LOS_BLOCKING = 8; //ballistica will stop on LOS blocking
	public static final int PIERCE_DOORS = 16; //ballistica won't stop on Doors

	public static final int PROJECTILE = STOP_TARGET | STOP_CHARS | STOP_TERRAIN | STOP_LOS_BLOCKING;

	public static final int MAGIC_BOLT = STOP_CHARS | STOP_TERRAIN | STOP_LOS_BLOCKING;

	public static final int WONT_STOP = 0;

	public Ballistica(int from, int to, int params) {
		sourcePos = from;
		cast(from, to, (params & STOP_TARGET) > 0, (params & STOP_CHARS) > 0, (params & STOP_TERRAIN) > 0, (params & STOP_LOS_BLOCKING) > 0, (params & PIERCE_DOORS) > 0);
		if (collisionPos != null)
			dist = path.indexOf(collisionPos);
		else
			collisionPos = path.get(dist = path.size() - 1);
	}

	private int cast(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean stopLOS, boolean pierceDoors) {

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

		int cell = calc(from, to, stopChars, stopTarget, stopTerrain, stopLOS, pierceDoors, dA / 2);

		if (!hit) {

			for (int err = 0; err <= dA; err++) {
				int calc = calc(from, to, stopChars, stopTerrain, stopTarget, stopLOS, pierceDoors, err);
				if (hit) {
					cell = calc;
					break;
				}
			}
		}

		return cell;

	}

	private int calc(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean stopLOS, boolean pierceDoors, int err) {

		hit = false;
		dist = 0;
		path.add(from);

		int cell = from;

		while (Dungeon.level.insideMap(cell)) {

			if (stopTerrain && cell != sourcePos && Dungeon.level.solid[cell] && !(pierceDoors && Dungeon.level.map[cell] == Terrain.DOOR)) {
				collide(path.get(path.size() - 1));
			}

			path.add(cell);
//            path[ distance + 1 ] = 0;

			if (cell == to && collisionPos==null) {
				hit = true;
				if (stopTarget) {
					collide(cell);
				}
			}
			if (stopChars && cell != sourcePos && Actor.findChar(cell) != null) {
				collide(cell);
			}
			if ((stopLOS && cell != sourcePos && Dungeon.level.losBlocking[cell]) && !(pierceDoors && Dungeon.level.map[cell] == Terrain.DOOR)) {
				collide(cell);
			}

			err += dB;

			if (err >= dA) {
				err = err - dA;
				cell = cell + stepB;
			}
			cell += stepA;
			dist++;

            /*// basically if current cell is not a wall
            if ( Level.passable[ cell ] || Level.illusory[ cell ] || Level.avoid[ cell ] ) {

                distance++;
                path[ distance ] = cell;
                path[ distance + 1 ] = 0;

                // doors are also solid yet do not count as walls
                if ( Level.solid[ cell ] || ( hitChars && Actor.findChar( cell ) != null ) ) {
                    return path[ distance ];
                }

            } else {

                // we need to keep the next cell for beam reflection logic
                path[ distance + 1 ] = cell;
                return path[ distance ];

            } */
		}

		// we need to reset the next cell for beam reflection logic
//        path[ distance + 1 ] = 0;

		return to;
	}

	//we only want to record the first position collision occurs at.
	private void collide(int cell){
		if (collisionPos == null)
			collisionPos = cell;
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
