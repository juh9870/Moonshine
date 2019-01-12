package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Random;

public class Runes {

    public static final int RUNES_COUNT = 80;

    private static TextureFilm runes;
    static {
        runes=new TextureFilm(Assets.RUNES,6,8);
    }

    public static Image get(int id){
        Image rune = new Image(Assets.RUNES);
        rune.frame(runes.get(id));
        return rune;
    }

    public static int random(){
        return Random.Int(RUNES_COUNT);
    }
}