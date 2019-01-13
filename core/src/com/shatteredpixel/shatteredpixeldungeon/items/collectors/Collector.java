

package com.shatteredpixel.shatteredpixeldungeon.items.collectors;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public abstract class Collector<T extends Item> extends Item {

    private static final String TXT_STATUS	= "%d/%d";

    protected int volume = 0;

    private static final String VOLUME	= "volume";

    public String notEnough(){
        return Messages.get(this,"not_enough");
    }

    public abstract int maxVolume();

    public int volume(){
        return volume;
    }

    public boolean spend(int count){
        if (volume()>=count){
            volume-=count;
            return true;
        }
        return false;
    }

    public void add(int quanity){
        volume=Math.min(maxVolume(),volume+quanity);
    }

    public Collector<T> setQuanity(int quanity){
        volume=Math.min(quanity,maxVolume());
        return this;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( VOLUME, volume );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        volume	= bundle.getInt( VOLUME );
    }


    public void empty() {volume = 0; updateQuickslot();}

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    public boolean isFull() {
        return volume >= maxVolume();
    }

    public void fill() {
        volume = maxVolume();
        updateQuickslot();
    }

    @Override
    public String status() {
        return Messages.format( TXT_STATUS, volume, maxVolume() );
    }
    public void collect(T itm ) {

        if (!isFull())
            GLog.i( Messages.get(this, "collected") );
        add(itm.quantity());
        if (isFull()) {
            volume = maxVolume();
            GLog.p( Messages.get(this, "full") );
        }

        updateQuickslot();
    }
}
