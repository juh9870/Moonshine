

package com.shatteredpixel.shatteredpixeldungeon.items.guns.ammo;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.Ammo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class Bullet extends Item implements Ammo {

    {
        stackable=true;
    }

    public Bullet(){
    }

    public Bullet(int quanity){
        this();
        quantity(quanity);
    }

	@Override
	public void onMiss(int cell) {
		Dungeon.level.drop(this, cell).sprite.drop();
	}

	@Override
    public int image() {
        switch (quantity){
            case 1:
                image=ItemSpriteSheet.BULLET1;
                break;
            case 2:
                image=ItemSpriteSheet.BULLET2;
                break;
            case 3:
                image=ItemSpriteSheet.BULLET3;
                break;
            case 4:
                image=ItemSpriteSheet.BULLET4;
                break;
            default:
                image=ItemSpriteSheet.BULLETBAG;
        }
        return super.image();
    }

    @Override
    public int throwImage() {
        return image=ItemSpriteSheet.BULLETFLYING;
    }

    @Override
    public Item random() {
        return random(.7f);
    }
    public Item random(float num) {
        quantity(Random.IntRange((int)(30*num),(int)(40*num)));
        return this;
    }

    @Override
    public int price() {
        return 4*quantity();
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public int throwPos(Hero user, int dst) {
        return super.throwPos(user, dst);
    }

    @Override
    public Class<? extends Buff>[] buffs() {
        return new Class[]{
                Bleeding.class,
                Cripple.class
        };
    }

    @Override
    public Float[] buffsChances() {
        return new Float[]{
                1f,
                1f
        };
    }

    @Override
    public Float[] buffsDuration() {
        return new Float[]{
                -1f,
                Cripple.DURATION
        };
    }
}
