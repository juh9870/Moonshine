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
package com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

import java.util.ArrayList;
import java.util.List;



public class Ballistica {
	//parameters to specify the colliding cell
	public static final int STOP_TARGET = 1;        //ballistica will stop at the target cell
	public static final int STOP_CHARS = 2;        //ballistica will stop on first char hit
	public static final int STOP_TERRAIN = 4;        //ballistica will stop on terrain

	public static final int PIERCE_LOS_BLOCKING = 8;//ballistica won't stop on LOS blocking
	public static final int PIERCE_DOORS = 16;        //ballistica won't stop on Doors

	public static final int PROJECTILE = STOP_TARGET | STOP_CHARS | STOP_TERRAIN;

	public static final int MAGIC_BOLT = STOP_CHARS | STOP_TERRAIN;

	public static final int WONT_STOP = PIERCE_DOORS | PIERCE_LOS_BLOCKING;


	//note that the path is the FULL path of the projectile, including tiles after collision.
	//make sure to generate a subPath for the common case of going source to collision.
	public ArrayList<Integer> path = new ArrayList<>();
	public Integer sourcePos = null;
	public Integer collisionPos = null;
	public Integer dist = 0;


	protected Ballistica(){}

	public Ballistica(int from, int to, int params){
		this(from,to,params,false);
	}

	public Ballistica(int from, int to, int params, boolean precise){
		Ballistica b;
		if (precise){
			b = new PreciseBallistica(from,to,params);
		} else {
			b = new DefaultBallistica(from,to,params);
		}
		this.path = b.path;
		this.sourcePos = b.sourcePos;
		this.collisionPos = b.collisionPos;
		this.dist = b.dist;
	}


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
