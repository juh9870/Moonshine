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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

import java.util.ArrayList;

public final class YAPDBallistica {

	public ArrayList<Integer> trace = new ArrayList<>();
	public int collisionPos;
	public int distance;

    private int stepA;
    private int stepB;
    private int dA;
    private int dB;

    private boolean hit;

    public YAPDBallistica(int from, int to, boolean goThrough, boolean hitChars){
    	collisionPos=cast(from,to,goThrough,hitChars);
	}

    public int cast( int from, int to, boolean goThrough, boolean hitChars ) {

        int w = Dungeon.level.width();

        int x0 = from % w;
        int x1 = to % w;
        int y0 = from / w;
        int y1 = to / w;

        int dx = x1 - x0;
        int dy = y1 - y0;

        int stepX = dx > 0 ? +1 : -1;
        int stepY = dy > 0 ? +1 : -1;

        dx = Math.abs( dx );
        dy = Math.abs( dy );

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

        int cell = calc( from, to, hitChars, goThrough, dA / 2 );

        if ( !hit ) {

            for (int err = 0; err <= dA; err++) {
                int calc = calc( from, to, hitChars, goThrough, err );
                if ( hit ) {
                    cell = calc;
                    break;
                }
            }
        }

        return cell;

    }

    private int calc( int from, int to, boolean hitChars, boolean goThrough, int err ) {

        hit = false;
        distance = 0;
        trace.clear();
        trace.add(from);

        int cell = from;

        while ( !hit || goThrough ) {

            cell += stepA;

            err += dB;

            if ( err >= dA ) {
                err = err - dA;
                cell = cell + stepB;
            }

            if ( cell == to ) {
                hit = true;
            }

            distance++;
            trace.add( cell );
//            trace[ distance + 1 ] = 0;

			if ( hitChars && Actor.findChar( cell ) != null ){
				return trace.get( distance );
			}
			if( Dungeon.level.map[cell]==Terrain.DOOR){
				return trace.get( distance );
			}
            if ( Dungeon.level.solid[ cell ] ) {
				do {
					distance--;
				} while (Dungeon.level.solid[trace.get( distance )]
						&& Dungeon.level.map[trace.get( distance )]!=Terrain.DOOR);
                return trace.get( distance );
            }

            /*// basically if current cell is not a wall
            if ( Level.passable[ cell ] || Level.illusory[ cell ] || Level.avoid[ cell ] ) {

                distance++;
                trace[ distance ] = cell;
                trace[ distance + 1 ] = 0;

                // doors are also solid yet do not count as walls
                if ( Level.solid[ cell ] || ( hitChars && Actor.findChar( cell ) != null ) ) {
                    return trace[ distance ];
                }

            } else {

                // we need to keep the next cell for beam reflection logic
                trace[ distance + 1 ] = cell;
                return trace[ distance ];

            } */
        }

        // we need to reset the next cell for beam reflection logic
//        trace[ distance + 1 ] = 0;

        return to;
    }

}
