

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.barrels;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;
import com.watabou.utils.Random;

public class Barrel extends Attachment {

    static Class<? extends Barrel>[] attachments = new Class[]{
            LongBarrel.class,
            ShortBarrel.class,
            BalancedBarrel.class
    };

    static float[] chances = new float[]{
            1f,
            1f,
            .75f
    };

    public static Barrel random(){
        try {
            return ClassReflection.newInstance(attachments[Random.chances(chances)]);
        } catch (ReflectionException e){
            return null;
        }
    }
}
