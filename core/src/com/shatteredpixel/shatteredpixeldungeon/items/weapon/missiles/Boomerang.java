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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;

import java.util.ArrayList;

public class Boomerang extends MissileWeapon {

	{
		image = ItemSpriteSheet.BOOMERANG;

		unique = true;
		bones = false;

		ammo=maxAmmo=1;

	}

	public void recalculateAmmo(){
		//Boomerang always have 1 ammo
	}

	@Override
	public float stickChance() {
		return 0;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		if (!isEquipped(hero)) actions.add(AC_EQUIP);
		return actions;
	}

	@Override
	public int min(int lvl) {
		return  1 +
				lvl;
	}

	@Override
	public int max(int lvl) {
		return  6 +     //half the base damage of a tier-1 weapon
				2 * lvl;//scales the same as a tier 1 weapon
	}

	@Override
	public String status() {
		return null;
	}

	@Override
	public int STRReq(int lvl) {
		lvl = Math.max(0, lvl);
		//strength req decreases at +1,+3,+6,+10,etc.
		return 9 - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}

	@Override
	public void rangedHit( Char enemy, int cell ) {
		circleBack(cell, curUser);
	}
	
	@Override
	protected void rangedMiss( int cell ) {
		circleBack( cell, curUser );
	}

	@Override
	protected void rangedLand(int cell) {
		Mob m = new Rat();
		m.pos = cell;
		m.state=m.SLEEPING;
		Buff.affect(m,Paralysis.class,10);
		GameScene.add(m);
		circleBack( cell, curUser );
	}

	private void circleBack(int from, Hero owner ) {

		((MissileSprite)curUser.sprite.parent.recycle( MissileSprite.class )).
				reset( from, owner.sprite, curItem, null );
		if (!collect()) {
			Dungeon.level.drop( this, owner.pos ).sprite.drop();
		}
		curUser.spendAndNext(castDelay(curUser,from));
	}
	
	@Override
	public String desc() {
		String info = super.desc();
		switch (augment) {
			case SPEED:
				info += "\n\n" + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += "\n\n" + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		return info;
	}
}
