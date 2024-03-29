/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2017 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.GameSettings;

public class SPDSettings extends GameSettings {

	//Version info
	
	public static final String KEY_VERSION      = "version";
	
	public static void version( int value)  {
		put( KEY_VERSION, value );
	}
	
	public static int version() {
		return getInt( KEY_VERSION, 0 );
	}
	
	//Graphics
	
	public static final String KEY_FULLSCREEN	= "fullscreen";
	public static final String KEY_LANDSCAPE	= "landscape";
	public static final String KEY_POWER_SAVER 	= "power_saver";
	public static final String KEY_SCALE		= "scale";
	public static final String KEY_ZOOM			= "zoom";
	public static final String KEY_BRIGHTNESS	= "brightness";
	public static final String KEY_GRID 	    = "visual_grid";

	public static final String KEY_WINDOW_FULLSCREEN	= "windowFullscreen";
	public static final String KEY_WINDOW_WIDTH			= "windowWidth";
	public static final String KEY_WINDOW_HEIGHT		= "windowHeight";

	public static final int DEFAULT_WINDOW_WIDTH = 480;
	public static final int DEFAULT_WINDOW_HEIGHT = 800;

	public static void fullscreen( boolean value ) {
		if (value) {
			put(KEY_WINDOW_FULLSCREEN, true);

			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			int w = getInt(KEY_WINDOW_WIDTH, DEFAULT_WINDOW_WIDTH);
			int h = getInt(KEY_WINDOW_HEIGHT, DEFAULT_WINDOW_HEIGHT);
			put(KEY_WINDOW_FULLSCREEN, false);
			Gdx.graphics.setWindowedMode(w, h);
		}
	}
	
	public static boolean fullscreen() {
		return getBoolean(KEY_WINDOW_FULLSCREEN, Gdx.graphics.isFullscreen());
	}
	
	public static void landscape( boolean value ){
		put( KEY_LANDSCAPE, value );
	}
	
	public static boolean landscape() {
		if (!SharedLibraryLoader.isAndroid)
			return Game.width > Game.height;
		else return getBoolean(KEY_LANDSCAPE,true);
	}

	/*public static void powerSaver( boolean value ){
		put( KEY_POWER_SAVER, value );
		((ShatteredPixelDungeon)ShatteredPixelDungeon.instance).updateDisplaySize();
	}

	public static boolean powerSaver(){
		return getBoolean( KEY_POWER_SAVER, false );
	}*/
	
	public static void scale( int value ) {
		put( KEY_SCALE, value );
	}
	
	public static int scale() {
		return getInt( KEY_SCALE, 0 );
	}
	
	public static void zoom( int value ) {
		put( KEY_ZOOM, value );
	}
	
	public static int zoom() {
		return getInt( KEY_ZOOM, 0 );
	}
	
	public static void brightness( int value ) {
		put( KEY_BRIGHTNESS, value );
		GameScene.updateFog();
	}
	
	public static int brightness() {
		return getInt( KEY_BRIGHTNESS, 0, -2, 2 );
	}
	
	public static void visualGrid( int value ){
		put( KEY_GRID, value );
		GameScene.updateMap();
	}
	
	public static int visualGrid() {
		return getInt( KEY_GRID, 0, -1, 3 );
	}
	
	//Interface
	
	public static final String KEY_QUICKSLOTS	= "quickslots";
	public static final String KEY_FLIPTOOLBAR	= "flipped_ui";
	public static final String KEY_FLIPTAGS 	= "flip_tags";
	public static final String KEY_BARMODE		= "toolbar_mode";
	public static final String KEY_AIMTYPE		= "aimtype";
	public static final String KEY_CHROME		= "chrome";

	public static void quickSlots( int value ){ put( KEY_QUICKSLOTS, value ); }

	public static int quickSlots(){ return getInt( KEY_QUICKSLOTS, 4, 0, 4); }

	public static void flipToolbar( boolean value) {
		put(KEY_FLIPTOOLBAR, value );
	}

	public static boolean flipToolbar(){ return getBoolean(KEY_FLIPTOOLBAR, false); }

	public static void flipTags( boolean value) {
		put(KEY_FLIPTAGS, value );
	}

	public static boolean flipTags(){ return getBoolean(KEY_FLIPTAGS, false); }


	public static void chromeStyle(int value){
		put(KEY_CHROME,value);
	}

	public static int chromeStyle(){
		return getInt(KEY_CHROME,0)%Chrome.chromeStyles.length;
	}

	public static void aimType(int value){
		put(KEY_AIMTYPE,value);
		preciseAim = value==2;
		aimhelper = value==1;
	}

	public static int aimType(){
		return getInt(KEY_AIMTYPE,0);
	}

	private static Boolean preciseAim;
	public static boolean preciseAim(){
		if (preciseAim==null)
			preciseAim=getInt(KEY_AIMTYPE, 0)==2;
		return preciseAim;
	}

	private static Boolean aimhelper;

	public static boolean aimhelper(){
		if (aimhelper==null)
			aimhelper=getInt(KEY_AIMTYPE, 0)==1;
		return aimhelper;
	}
	
	public static void toolbarMode( String value ) {
		put( KEY_BARMODE, value );
	}
	
	public static String toolbarMode() {
		return getString(KEY_BARMODE, !SPDSettings.landscape() ? "SPLIT" : "GROUP");
	}
	
	//Game State
	
	public static final String KEY_LAST_CLASS	= "last_class";
	public static final String KEY_CHALLENGES	= "challenges";
	public static final String KEY_INTRO		= "intro";
	
	public static void intro( boolean value ) {
		put( KEY_INTRO, value );
	}
	
	public static boolean intro() {
		return getBoolean( KEY_INTRO, true );
	}
	
	public static void lastClass( int value ) {
		put( KEY_LAST_CLASS, value );
	}
	
	public static int lastClass() {
		return getInt( KEY_LAST_CLASS, 0, 0, 3 );
	}
	
	public static void challenges( int value ) {
		put( KEY_CHALLENGES, value );
	}
	
	public static int challenges() {
		return getInt( KEY_CHALLENGES, 0, 0, Challenges.MAX_VALUE );
	}
	
	//Audio
	
	public static final String KEY_MUSIC		= "music";
	public static final String KEY_MUSIC_VOL    = "music_vol";
	public static final String KEY_SOUND_FX		= "soundfx";
	public static final String KEY_SFX_VOL      = "sfx_vol";
	
	public static void music( boolean value ) {
		Music.INSTANCE.enable( value );
		put( KEY_MUSIC, value );
	}
	
	public static boolean music() {
		return getBoolean( KEY_MUSIC, true );
	}
	
	public static void musicVol( int value ){
		Music.INSTANCE.volume(value/10f);
		put( KEY_MUSIC_VOL, value );
	}
	
	public static int musicVol(){
		return getInt( KEY_MUSIC_VOL, 10, 0, 10 );
	}
	
	public static void soundFx( boolean value ) {
		Sample.INSTANCE.enable( value );
		put( KEY_SOUND_FX, value );
	}
	
	public static boolean soundFx() {
		return getBoolean( KEY_SOUND_FX, true );
	}
	
	public static void SFXVol( int value ) {
		Sample.INSTANCE.volume(value/10f);
		put( KEY_SFX_VOL, value );
	}
	
	public static int SFXVol() {
		return getInt( KEY_SFX_VOL, 10, 0, 10 );
	}
	
	//Languages and Font
	
	public static final String KEY_LANG         = "language";
	public static final String KEY_SYSTEMFONT	= "system_font";
	
	public static void language(Languages lang) {
		put( KEY_LANG, lang.code());
	}
	
	public static Languages language() {
		//multi-language does not currently work
		return Languages.ENGLISH;
	}
	
	public static void systemFont(boolean value){
		put(KEY_SYSTEMFONT, value);
		if (!value) {
			RenderedText.setFont("pixelfont.ttf");
		} else {
			RenderedText.setFont( null );
		}
	}
	
	public static boolean systemFont(){
		return getBoolean(KEY_SYSTEMFONT,
				(language() == Languages.KOREAN || language() == Languages.CHINESE));
	}

	public static boolean debug = false;
	
}
