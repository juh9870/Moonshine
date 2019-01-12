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
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Shuriken extends MissileWeapon {

	{
		image = ItemSpriteSheet.SHURIKEN;
		tier=2;
	}

	@Override
	public float speedFactor(Char owner) {
		if (owner instanceof Hero && ((Hero) owner).justMoved)  return 0;
		else                                                    return super.speedFactor(owner);
	}

	@Override
	public void recalculateAmmo() {
		int newMaxAmmo = 4+(int)(Math.sqrt(8 * Math.max(level(),0) + 1) - 1);
		ammo+=newMaxAmmo-maxAmmo;
		maxAmmo=newMaxAmmo;
	}

	@Override
	public float stickChance() {
		return .35f;
	}

	@Override
	public int[] rangedBounds(int lvl) {
		return new int[]{
				//min
				(tier-1)+level()/2,

				//max
				(int)( (1.5f * (tier + 1)) +	//4.5 base, down from 7.5
				 	   (lvl * tier * 0.75f))	//+1.5 per level, down from +2
		};
	}
}
