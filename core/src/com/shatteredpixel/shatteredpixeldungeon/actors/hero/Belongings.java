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
package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.belongings.WeaponHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	public static final int BACKPACK_SIZE	= 20;
	
	private Hero owner;
	
	public Bag backpack;

	public WeaponHolder weapon = null;
	public Armor armor = null;
	public KindofMisc misc1 = null;
	public KindofMisc misc2 = null;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Bag() {{
			name = Messages.get(Bag.class, "name");
			size = BACKPACK_SIZE;
		}};
		backpack.owner = owner;
		weapon = new WeaponHolder();
//		weapon.owner=owner;
	}
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String MISC1       = "misc1";
	private static final String MISC2       = "misc2";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( MISC1, misc1);
		bundle.put( MISC2, misc2);
	}
	
	public void restoreFromBundle( Bundle bundle ) {

		//moving keys to Notes, for pre-0.6.1 saves
		if (bundle.contains("ironKeys")) {
			int[] ironKeys = bundle.getIntArray( "ironKeys" );
			for (int i = 0; i < ironKeys.length; i++){
				if (ironKeys[i] > 0){
					Notes.add((Key) new IronKey(i).quantity(ironKeys[i]));
				}
			}
		}
		
		if (bundle.contains("specialKeys")) {
			int[] specialKeys = bundle.getIntArray( "specialKeys" );
			for (int i = 0; i < specialKeys.length; i++){
				if (specialKeys[i] > 0){
					if (i % 5 == 0){
						Notes.add((Key) new SkeletonKey(i).quantity(specialKeys[i]));
					} else {
						Notes.add((Key) new GoldenKey(i).quantity(specialKeys[i]));
					}
				}
			}
		}
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (WeaponHolder) bundle.get(WEAPON);
		if (weapon.right != null) {
			weapon.right.activate(owner);
		}
		if (weapon.left != null) {
			weapon.left.activate(owner);
		}
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor != null){
			armor.activate( owner );
		}
		
		misc1 = (KindofMisc)bundle.get(MISC1);
		if (misc1 != null) {
			misc1.activate( owner );
		}
		
		misc2 = (KindofMisc)bundle.get(MISC2);
		if (misc2 != null) {
			misc2.activate( owner );
		}
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			info.armorTier = ((Armor)bundle.get( ARMOR )).tier;
		} else {
			info.armorTier = 0;
		}
	}

	public ArrayList<Item> equips(){
		return new ArrayList<>(Arrays.asList(
				weapon.right,
				weapon.left,
				armor,
				misc1,
				misc2
		));
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				return (T)item;
			}
		}
		
		return null;
	}
	
	public Item getSimilar( Item similar ){
		
		for (Item item : this) {
			if (item.isSimilar(similar)) {
				return item;
			}
		}
		
		return null;
	}
	
	public void identify() {
		for (Item item : this) {
			item.identify();
		}
	}
	
	public void observe() {
		if (weapon.right != null) {
			weapon.right.identify();
			Badges.validateItemLevelAquired( weapon.right );
		}
		if (weapon.left != null) {
			weapon.left.identify();
			Badges.validateItemLevelAquired( weapon.left );
		}
		if (armor != null) {
			armor.identify();
			Badges.validateItemLevelAquired( armor );
		}
		if (misc1 != null) {
			misc1.identify();
			Badges.validateItemLevelAquired(misc1);
		}
		if (misc2 != null) {
			misc2.identify();
			Badges.validateItemLevelAquired(misc2);
		}
		for (Item item : backpack) {
			item.cursedKnown = true;
		}
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor, weapon.left, weapon.right, misc1, misc2);
	}
	
	public Item randomUnequipped() {
		return Random.element( backpack.items );
	}
	
	public void resurrect( int depth ) {

		for (Item item : backpack.items.toArray( new Item[0])) {
			if (item instanceof Key) {
				if (((Key)item).depth == depth) {
					item.detachAll( backpack );
				}
			} else if (item.unique) {
				item.detachAll(backpack);
				//you keep the bag itself, not its contents.
				if (item instanceof Bag){
					((Bag)item).resurrect();
				}
				item.collect();
			} else if (!item.isEquipped( owner )) {
				item.detachAll( backpack );
			}
		}

		if (weapon.right != null) {
			weapon.right.cursed = false;
			weapon.right.activate( owner );
		}

		if (weapon.left != null) {
			weapon.left.cursed = false;
			weapon.left.activate( owner );
		}

		if (armor != null) {
			armor.cursed = false;
			armor.activate( owner );
		}
		
		if (misc1 != null) {
			misc1.cursed = false;
			misc1.activate( owner );
		}
		if (misc2 != null) {
			misc2.cursed = false;
			misc2.activate( owner );
		}
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	public boolean removeExactItem(Item itm){
		for (Iterator<Item> iterator = iterator(); iterator.hasNext();){
			if (iterator.next()==itm){
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	public boolean removeUUIDItem(Item itm, boolean placeholderMode){
		for (Iterator<Item> iterator = iterator(); iterator.hasNext();){
			Item cur = iterator.next();
			if (cur.uuid.equals(itm.uuid) && (cur.isPlaceholder() || !placeholderMode)){
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon.right, weapon.left, armor, misc1, misc2};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon.right = null;
				break;
			case 1:
				equipped[0] = weapon.left = null;
				break;
			case 2:
				equipped[1] = armor = null;
				break;
			case 3:
				equipped[2] = misc1 = null;
				break;
			case 4:
				equipped[3] = misc2 = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
