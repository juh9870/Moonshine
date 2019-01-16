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
package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.noosa.NinePatch;

public class Chrome {

	public enum  Type {
		TOAST,
		TOAST_TR,
		WINDOW,
		BUTTON,
		TAG,
		GEM,
		SCROLL,
		TAB_SET,
		TAB_SELECTED,
		TAB_UNSELECTED
	}

	public static String[] chromeStyles = new String[]{
			"default",
			"transparent",
			"eyekiller",
			"coffee",
			"cold I",
			"cold II",
			"bubblegum",
			"mossy copper",
			"candy",
			"flame",
			"turtle",
			"stone",
			"warm & cold",
			"chocolate",
			"blazing",
			"enchanted",
			"acid",
			"cold III",
	};

	public static NinePatch get( Type type ) {

		int x=SPDSettings.chromeStyle()%4;
		int y=SPDSettings.chromeStyle()/4;

		int dx=x*128;
		int dy=y*64;

		String Asset = Assets.CHROME;
		switch (type) {
		case WINDOW:
			return new NinePatch( Asset, 0+dx, 0+dy, 20, 20, 6 );
		case TOAST:
			return new NinePatch( Asset, 22+dx, 0+dy, 18, 18, 5 );
		case TOAST_TR:
			return new NinePatch( Asset, 40+dx, 0+dy, 18, 18, 5 );
		case BUTTON:
			return new NinePatch( Asset, 58+dx, 0+dy, 6, 6, 2 );
		case TAG:
			return new NinePatch( Asset, 22+dx, 18+dy, 16, 14, 3 );
		case GEM:
			return new NinePatch( Asset, 0+dx, 32+dy, 32, 32, 13 );
		case SCROLL:
			return new NinePatch( Asset, 32+dx, 32+dy, 32, 32, 5, 11, 5, 11 );
		case TAB_SET:
			return new NinePatch( Asset, 64+dx, 0+dy, 20, 20, 6 );
		case TAB_SELECTED:
			return new NinePatch( Asset, 65+dx, 22+dy, 8, 13, 3, 7, 3, 5 );
		case TAB_UNSELECTED:
			return new NinePatch( Asset, 75+dx, 22+dy, 8, 13, 3, 7, 3, 5 );
		default:
			return null;
		}
	}
}
