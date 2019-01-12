package com.shatteredpixel.shatteredpixeldungeon.actors.hero.belongings;

import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Storeable;

public class WeaponHolder implements Bundlable {
    @Storeable
    public KindOfWeapon right;
    @Storeable
    public KindOfWeapon left;
    @Storeable
    private int curSlot=0;
    @Storeable
    public float maxWeight = 1.0f;

    public int getCurSlot(){
        if (!anyEquiped())return curSlot;
        if (get(curSlot)==null){
            curSlot=1-curSlot;
        }
        return curSlot;
    }

    public KindOfWeapon currentWeapon(){
		return get(getCurSlot());
    }

    public boolean anyEquiped(){
        return right!=null||left!=null;
    }

    public boolean switchActive(){
    	if (freeSlot()!=-1)return false;
    	curSlot=1-curSlot;
    	return true;
	}

    public KindOfWeapon offhandWeapon(){
        return get(1-getCurSlot());
    }

    public boolean canEquip(KindOfWeapon wep){
        return canEquip(wep,right==null?0:1);
    }

    public int getSlot(KindOfWeapon wep){
        return wep==right?0:wep==left?1:-1;
    }

    public int freeSlot(){
        return right==null?0:left==null?1:-1;
    }

    public boolean canEquip(KindOfWeapon wep, int slot){
        if (slot==0)return left==null||maxWeight-left.weight>=wep.weight;
        else if (slot==1)return right==null||maxWeight-right.weight>=wep.weight;

        throw new IllegalArgumentException("slot can be either 0 or 1");
    }

    public boolean isEquipped(KindOfWeapon wep){
        return right==wep||left==wep;
    }

    public KindOfWeapon get(int slot){
        if (slot!=0&&slot!=1)throw new IllegalArgumentException("slot can be either 0 or 1");
        return slot==0?right:left;
    }

    public void set(KindOfWeapon wep, int slot){
        if (slot==0)right=wep;
        else if (slot==1)left=wep;
        else throw new IllegalArgumentException("slot can be either 0 or 1");
    }
}
