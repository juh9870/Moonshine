
package com.shatteredpixel.shatteredpixeldungeon.items.guns;


import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

public interface Ammo {

    default int onHit(Char c, int dmg){
    	onMiss(c.pos);
    	return dmg;
	};

    default void onMiss(int cell){

	};

    default void onWall(int cell){
		onMiss(cell);
	};

    default float pierceMod(){
    	return 1;
	};

    default float minDmgMod(){
    	return 1;
	};

    default float maxDmgMod(){
    	return 1;
	};

    default float armorPiercingRate(){
    	return 1;
	};

    default Class<? extends Buff>[] buffs(){
    	return new Class[0];
	};

    default Float[] buffsChances(){
    	return new Float[0];
	};

    default Float[] buffsDuration(){
    	return new Float[0];
	};
}
