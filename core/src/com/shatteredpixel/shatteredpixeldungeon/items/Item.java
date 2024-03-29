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

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameArrays;
import com.watabou.utils.Storeable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item implements Bundlable {

	protected static final String TXT_TO_STRING_LVL		= "%s %+d";
	protected static final String TXT_TO_STRING_X		= "%s x%d";
	
	protected static final float TIME_TO_THROW		= 1.0f;
	protected static final float TIME_TO_PICK_UP	= 1.0f;
	protected static final float TIME_TO_DROP		= 0.5f;
	
	public static final String AC_DROP		= "DROP";
	public static final String AC_THROW		= "THROW";

	@Storeable
	public String defaultAction;
	@Storeable
	public boolean usesTargeting;
	
	protected String name = Messages.get(this, "name");
	@Storeable
	public int image = 0;
	
	public boolean stackable = false;
	@Storeable
	protected int quantity = 1;
	private int level = 0;
	@Storeable
	public int tier = 0;

	public boolean levelKnown = false;
	
	public boolean cursed;
	public boolean cursedKnown;

	public boolean isPlaceholder(){
		return false;
	}

	// Unique items persist through revival
	public boolean unique = false;

	// whether an item can be included in heroes remains
	public boolean bones = false;

	@Storeable
	public String uuid = java.util.UUID.randomUUID().toString();
	
	private static Comparator<Item> itemComparator = new Comparator<Item>() {
		@Override
		public int compare( Item lhs, Item rhs ) {
			return Generator.Category.order( lhs ) - Generator.Category.order( rhs );
		}
	};
	
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<String>();
		actions.add( AC_DROP );
		actions.add( AC_THROW );
		return actions;
	}
	
	public boolean doPickUp( Hero hero ) {
		if (collect( hero.belongings.backpack )) {
			
			GameScene.pickUp( this, hero.pos );
			Sample.INSTANCE.play( Assets.SND_ITEM );
			hero.spendAndNext( TIME_TO_PICK_UP );
			return true;
			
		} else {
			return false;
		}
	}
	
	public void doDrop( Hero hero ) {
		if (isPlaceholder())return;
		hero.spendAndNext( TIME_TO_DROP );
		Dungeon.level.drop( detachAll( hero.belongings.backpack ), hero.pos ).sprite.drop( hero.pos );
	}

	//resets an item's properties, to ensure consistency between runs
	public void reset(){
		//resets the name incase the language has changed.
		name = Messages.get(this, "name");
	}

	public void doThrow( Hero hero ) {
		GameScene.aimHelper=true;
		curBallistica=Ballistica.PROJECTILE;
		GameScene.selectCell( thrower );
	}
	
	public void execute( Hero hero, String action ) {
		
		curUser = hero;
		curItem = this;

		Combo combo = hero.buff(Combo.class);
		if (combo != null) combo.detach();

		switch (action) {
			case AC_DROP:

				doDrop(hero);

				break;
			case AC_THROW:

				doThrow(hero);

				break;
		}
	}
	
	public void execute( Hero hero ) {
		execute( hero, defaultAction );
	}
	
	protected void onThrow( int cell ) {
		Heap heap = Dungeon.level.drop( this, cell );
		if (!heap.isEmpty()) {
			heap.sprite.drop( cell );
		}
	}
	
	//takes two items and merges them (if possible)
	public Item merge( Item other ){
		if (isSimilar( other )){
			quantity += other.quantity;
			other.quantity = 0;
		}
		return this;
	}

	public boolean collect( Bag container ) {
		return collect(container,true);
	}
	public boolean collect( Bag container, boolean stack ) {

		ArrayList<Item> items = container.items;
		ArrayList<Item> equips=null;
		if (Dungeon.hero!=null){
			equips=Dungeon.hero.belongings.equips();
		}
		
		if (items.contains( this )) {
			return true;
		}
		
		for (Item item:GameArrays.merge(items,equips)) {
			if (item instanceof Bag && ((Bag)item).grab( this )) {
				return collect( (Bag)item );
			}
		}
		
		if (stackable&&stack) {
			for (Item item:GameArrays.merge(items,equips)) {
				if (isSimilar( item )) {
					item.merge( this );
					item.updateQuickslot();
					return true;
				}
			}
		}
		
		if (items.size() < container.size) {

			if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
				Badges.validateItemLevelAquired( this );
			}
			
			items.add( this );
			Dungeon.quickslot.replacePlaceholder(this);
			updateQuickslot();
			Collections.sort( items, itemComparator );
			return true;
			
		} else {
			
			GLog.n( Messages.get(Item.class, "pack_full", name()) );
			return false;
			
		}
	}
	
	public boolean collect() {
		return collect( Dungeon.hero.belongings.backpack );
	}

	//returns a new item if the split was sucessful and there are now 2 items, otherwise null
	public Item split( int amount ){
		if (amount <= 0 || amount >= quantity()) {
			return null;
		} else {
			try {

				Item split = clone();
				split.quantity(amount);
				quantity -= amount;

				return split;
			} catch (Exception e){
				ShatteredPixelDungeon.reportException(e);
				return null;
			}
		}
	}

	public final Item detach( Bag container ) {

		if (quantity <= 0) {

			return null;

		} else
		if (quantity == 1) {

			if (stackable && !(this instanceof MissileWeapon)){
				Dungeon.quickslot.convertToPlaceholder(this);
			}
			return detachAll( container );

		} else {


			Item detached = split(1);
			updateQuickslot();

			if (detached != null) detached.onDetach( );
			return detached;
		}
	}
	
	public final Item detachAll( Bag container ) {
		Dungeon.quickslot.clearItem( this );
		updateQuickslot();

		for (Item item : container.items) {
			if (item == this) {
				container.items.remove(this);
				item.onDetach();
				return this;
			} else if (item instanceof Bag) {
				Bag bag = (Bag)item;
				if (bag.contains( this )) {
					return detachAll( bag );
				}
			}
		}
		
		return this;
	}
	
	public boolean isSimilar( Item item ) {
		return item!=null && getClass() == item.getClass();
	}

	protected void onDetach(){}

	public int level(){
		return level;
	}

	public void level( int value ){
		level = value;

		updateQuickslot();
	}
	
	public Item upgrade() {
		
		this.level++;

		updateQuickslot();
		
		return this;
	}
	
	final public Item upgrade( int n ) {
		for (int i=0; i < n; i++) {
			upgrade();
		}
		
		return this;
	}
	
	public Item degrade() {
		
		this.level--;
		
		return this;
	}
	
	final public Item degrade( int n ) {
		for (int i=0; i < n; i++) {
			degrade();
		}
		
		return this;
	}
	
	public int visiblyUpgraded() {
		return levelKnown ? level : 0;
	}
	
	public boolean visiblyCursed() {
		return cursed && cursedKnown;
	}
	
	public boolean isUpgradable() {
		return true;
	}
	
	public boolean isIdentified() {
		return levelKnown && cursedKnown;
	}
	
	public boolean isEquipped( Hero hero ) {
		return false;
	}
	
	public Item identify() {
		
		levelKnown = true;
		cursedKnown = true;
		
		if (Dungeon.hero != null && Dungeon.hero.isAlive()) {
			Catalog.setSeen(getClass());
		}
		
		return this;
	}
	
	public static void evoke( Hero hero ) {
		hero.sprite.emitter().burst( Speck.factory( Speck.EVOKE ), 5 );
	}
	
	@Override
	public String toString() {

		String name = name();

		if (visiblyUpgraded() != 0)
			name = Messages.format( TXT_TO_STRING_LVL, name, visiblyUpgraded()  );

		if (quantity > 1)
			name = Messages.format( TXT_TO_STRING_X, name, quantity );

		return name;

	}
	
	public String name() {
		return name;
	}

	public boolean active(){
		return true;
	}
	
	public final String trueName() {
		return name;
	}
	
	public int image() {
		return image;
	}

	public int throwImage(){
		return image();
	}
	
	public ItemSprite.Glowing glowing() {
		return null;
	}

	public Emitter emitter() { return null; }
	
	public String info() {
		return desc();
	}
	
	public String desc() {
		return Messages.get(this, "desc");
	}
	
	public int quantity() {
		return quantity;
	}
	
	public Item quantity( int value ) {
		quantity = value;
		return this;
	}
	
	public int price() {
		return 0;
	}
	
	public static Item virtual( Class<? extends Item> cl ) {
		try {
			
			Item item = (Item)ClassReflection.newInstance(cl);
			item.quantity = 0;
			return item;
			
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}
	
	public Item random() {
		return this;
	}
	
	public String status() {
		return quantity != 1 ? Integer.toString( quantity ) : null;
	}
	
	public void updateQuickslot() {
			QuickSlotButton.refresh();
	}
	
	private static final String QUANTITY		= "quantity";
	private static final String LEVEL			= "level";
	private static final String LEVEL_KNOWN		= "levelKnown";
	private static final String CURSED			= "cursed";
	private static final String CURSED_KNOWN	= "cursedKnown";
	private static final String QUICKSLOT		= "quickslotpos";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		Bundlable.super.storeInBundle(bundle);
		bundle.put( LEVEL, level );
		bundle.put( LEVEL_KNOWN, levelKnown );
		bundle.put( CURSED, cursed );
		bundle.put( CURSED_KNOWN, cursedKnown );
		if (Dungeon.quickslot.contains(this)) {
			bundle.put( QUICKSLOT, Dungeon.quickslot.getSlot(this) );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		Bundlable.super.restoreFromBundle(bundle);
		levelKnown	= bundle.getBoolean( LEVEL_KNOWN );
		cursedKnown	= bundle.getBoolean( CURSED_KNOWN );
		
		int level = bundle.getInt( LEVEL );
		if (level > 0) {
			upgrade( level );
		} else if (level < 0) {
			degrade( -level );
		}
		
		cursed	= bundle.getBoolean( CURSED );

		//only want to populate slot on first load.
		if (Dungeon.hero == null) {
			if (bundle.contains(QUICKSLOT)) {
				Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this);
			}
		}
	}

	public int throwPos( Hero user, int dst){
		return new Ballistica(user.pos,dst,Ballistica.PROJECTILE,SPDSettings.preciseAim()).collisionPos;
	}
	
	public void cast( final Hero user, final int dst ) {
		
		final int cell = throwPos( user, dst );
		user.sprite.zap( cell );
		user.busy();

		Sample.INSTANCE.play( Assets.SND_MISS, 0.6f, 0.6f, 1.5f );

		Char enemy = Actor.findChar( cell );
		QuickSlotButton.target(enemy);

		final float delay = castDelay(user, dst);

		if (enemy != null) {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							enemy.sprite,
							this,
							() -> {
								Item.this.detach(user.belongings.backpack).onThrow(cell);
								user.spendAndNext(delay);
							});
		} else {
			((MissileSprite) user.sprite.parent.recycle(MissileSprite.class)).
					reset(user.sprite,
							cell,
							this,
							() -> {
								Item.this.detach(user.belongings.backpack).onThrow(cell);
								user.spendAndNext(delay);
							});
		}
	}

	@Override
	public Item clone() {
		try {
			Bundle b = new Bundle();
			storeInBundle(b);
			Item itm = ClassReflection.newInstance(getClass());
			itm.restoreFromBundle(b);
			return itm;
		} catch (ReflectionException e){
			return null;
		}
	}

	public float castDelay(Char user, int dst ){
		return TIME_TO_THROW;
	}

	protected static Hero curUser		 = null;
	protected static Item curItem		 = null;
	public 	  static int  curBallistica	 = Ballistica.PROJECTILE;
	protected static CellSelector.Listener thrower = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				curItem.cast( curUser, target );
			}
		}
		@Override
		public String prompt() {
			return Messages.get(Item.class, "prompt");
		}
	};
}
