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
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import com.watabou.utils.Storeable;

abstract public class KindOfWeapon extends EquipableItem {
	
	protected static final float TIME_TO_EQUIP = 1f;

	@Storeable
	public float weight = 0.5f;

	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings.weapon.isEquipped(this);
	}
	@Override
	public boolean doEquip( Hero hero ){
		if (hero.belongings.weapon.right!=null&&hero.belongings.weapon.left!=null) {
			KindOfWeapon w0 = this;
			KindOfWeapon w1 = hero.belongings.weapon.right;
			KindOfWeapon w2 = hero.belongings.weapon.left;
			GameScene.show(
					 new WndOptions(Messages.get(KindOfWeapon.class, "unequip_title"),
							Messages.get(KindOfWeapon.class, "unequip_message"),
							Messages.titleCase(w1.toString()),
							Messages.titleCase(w2.toString())) {

						@Override
						protected void onSelect(int index) {

							KindOfWeapon equipped = (index == 0 ? w1 : w2);
							if (!hero.belongings.weapon.canEquip(w0,index)){
								GLog.n( Messages.get(KindOfWeapon.class, "big",Messages.titleCase(w0.name()),Messages.titleCase((index == 0 ? w2 : w1).name())));
								return;
							}
							//temporarily give 1 extra backpack spot to support swapping with a full inventory
							hero.belongings.backpack.size++;
							if (equipped.doUnequip(hero, true, false)) {
								//fully re-execute rather than just call doEquip as we want to preserve quickslot
								execute(hero, AC_EQUIP);
							}
							hero.belongings.backpack.size--;
						}
					});

			return false;
		} else {
			if (hero.belongings.weapon.canEquip(this)) {
				return doEquip(hero, hero.belongings.weapon.freeSlot());
			} else {
				GLog.n( Messages.get(KindOfWeapon.class, "big",Messages.titleCase(this.name()),Messages.titleCase(hero.belongings.weapon.currentWeapon().name())));
				return false;
			}

		}
	}

	public boolean doEquip( Hero hero, int slot ) {

		if (slot==-1)return false;
		detachAll( hero.belongings.backpack );
		
		if (hero.belongings.weapon.canEquip(this,slot)&&(hero.belongings.weapon.get(slot) == null || hero.belongings.weapon.get(slot).doUnequip( hero, true ))) {
			
			hero.belongings.weapon.set(this,slot);
			activate( hero );

			updateQuickslot();
			
			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(KindOfWeapon.class, "equip_cursed") );
			}
			
			hero.spendAndNext( TIME_TO_EQUIP );
			return true;
			
		} else {
			
			collect( hero.belongings.backpack );
			return false;
		}
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (!hero.belongings.weapon.isEquipped(this))return false;
		if (super.doUnequip( hero, collect, single )) {
			if (hero.belongings.weapon.right==this){
				hero.belongings.weapon.right=null;
			} else {
				hero.belongings.weapon.left=null;
			}
			return true;

		} else {

			return false;

		}
	}

	public int min(){
		return min(level());
	}

	public int max(){
		return max(level());
	}

	abstract public int min(int lvl);
	abstract public int max(int lvl);

	public int damageRoll( Char owner ) {
		return Random.NormalIntRange( min(), max() );
	}
	
	public float accuracyFactor( Char owner ) {
		return 1f;
	}
	
	public float speedFactor( Char owner ) {
		return 1f;
	}

	public int reachFactor( Char owner ){
		return 1;
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		return damage;
	}
	
}
