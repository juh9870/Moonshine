

package com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.locks;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;
import com.watabou.utils.Random;

public class Lock extends Attachment {

    static Class<? extends Lock>[] attachments = new Class[]{
            FlintLock.class,
            WheelLock.class,
            Fuse.class
    };

    static float[] chances = new float[]{
            1f,
            1f,
            .75f
    };

    public static Lock random(){
        try {
            return ClassReflection.newInstance(attachments[Random.chances(chances)]);
        } catch (ReflectionException e){
            return null;
        }
    }
}
