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
package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import com.shatteredpixel.shatteredpixeldungeon.effects.Fireball;
import com.shatteredpixel.shatteredpixeldungeon.input.GameAction;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStartGame;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.*;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.Random;

public class TitleScene extends PixelScene {
	
	@Override
	public void create() {
		
		super.create();

		Music.INSTANCE.play( Assets.THEME, true );

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;
		
		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );

		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		add( title );

		float topRegion = Math.max(95f, h*0.45f);

		title.x = (w - title.width()) / 2f;
		if (SPDSettings.landscape())
			title.y = (topRegion - title.height()) / 2f;
		else
			title.y = 16 + (topRegion - title.height() - 16) / 2f;

		align(title);

		float yRaise = 2;
		float yMotion = 2;
		float timeCycle = 2;
		float xDistance = 0;

		Image cloudLeft = new Image(BannerSprites.get( BannerSprites.Type.CLOUD_LEFT)){
			float initY=-1;
			int arg = Random.Int(60);
			@Override
			public void update() {
				super.update();
				if (initY==-1)initY=y;
				y=(float)(initY+yMotion*Math.sin((Math.PI*arg)/(60*timeCycle)));
				arg++;
			}
		};
		add(cloudLeft);
		cloudLeft.x=title.x-15-xDistance;
		cloudLeft.y=title.y+title.height()-cloudLeft.height()+6+yRaise;

		Image cloudRight = new Image(BannerSprites.get( BannerSprites.Type.CLOUD_RIGHT)){
			float initY=-1;
			int arg = Random.Int(60);
			@Override
			public void update() {
				super.update();
				if (initY==-1)initY=y;
				y=(float)(initY+yMotion*Math.sin((Math.PI*arg)/(60*timeCycle)));
				arg++;
			}
		};
		add(cloudRight);
		cloudRight.x=title.x+title.width()-cloudRight.width()+15+xDistance;
		cloudRight.y=title.y+title.height()-cloudRight.height()+8+yRaise;

//		placeTorch(title.x + 22, title.y + 46);
//		placeTorch(title.x + title.width - 22, title.y + 46);

		DashboardItem btnBadges = new DashboardItem( Messages.get(this, "badges"), 3 ) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( BadgesScene.class );
			}
		};
		add(btnBadges);

		DashboardItem btnAbout = new DashboardItem( Messages.get(this, "about"), 1 ) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( AboutScene.class );
			}
		};
		add( btnAbout );

		DashboardItem btnPlay = new DashboardItem( Messages.get(this, "play"), 0 ) {
			@Override
			protected void onClick() {
				if (GamesInProgress.checkAll().size() == 0){
					TitleScene.this.add( new WndStartGame(1) );
				} else {
					ShatteredPixelDungeon.switchNoFade( StartScene.class );
				}
			}
		};
		add( btnPlay );

		DashboardItem btnRankings = new DashboardItem( Messages.get(this, "rankings"), 2 ) {
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.switchNoFade( RankingsScene.class );
			}
		};
		add( btnRankings );

		if (SPDSettings.landscape()) {
			btnRankings     .setPos( w / 2 - btnRankings.width(), topRegion );
			btnBadges       .setPos( w / 2, topRegion );
			btnPlay         .setPos( btnRankings.left() - btnPlay.width(), topRegion );
			btnAbout        .setPos( btnBadges.right(), topRegion );
		} else {
			btnPlay.setPos( w / 2 - btnPlay.width(), topRegion );
			btnRankings.setPos( w / 2, btnPlay.top() );
			btnBadges.setPos( w / 2 - btnBadges.width(), btnPlay.top() + DashboardItem.SIZE );
			btnAbout.setPos( w / 2, btnBadges.top() );
		}

		BitmapText version = new BitmapText( "v " + Game.version + "", pixelFont);
		version.measure();
		version.hardlight( 0xCCCCCC );
		version.x = w - version.width();
		version.y = h - version.height();
		add( version );

		Button changes = new ChangesButton();
		changes.setPos( w-changes.width(), h - version.height() - changes.height());
		add( changes );
		
		int pos = 0;

		PrefsButton btnPrefs = new PrefsButton();
		btnPrefs.setRect( pos, 0, 16, 16 );
		add( btnPrefs );

		pos += btnPrefs.width();

		LanguageButton btnLang = new LanguageButton();
		btnLang.setRect(pos, 0, 14, 16);
		add( btnLang );

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( w - btnExit.width(), 0 );
		add( btnExit );

		UpdateNotification updInfo = new UpdateNotification();
		updInfo.setPos(0, h-updInfo.height());
		add(updInfo);

		fadeIn();
	}

	private void placeTorch( float x, float y ) {
		Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}
	
	private static class DashboardItem extends Button<GameAction> {
		
		public static final float SIZE	= 48;
		
		private static final int IMAGE_SIZE	= 32;
		
		private Image image;
		private RenderedText label;

		public DashboardItem( String text, int index ) {
			super();

			image.frame( image.texture.uvRect( index * IMAGE_SIZE, 0, (index + 1) * IMAGE_SIZE, IMAGE_SIZE ) );
			this.label.text( text );

			setSize( SIZE, SIZE );
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			image = new Image( Assets.DASHBOARD );
			add( image );

			label = renderText( 9 );
			add( label );
		}

		@Override
		protected void layout() {
			super.layout();

			image.x = x + (width - image.width()) / 2;
			image.y = y;
			align(image);

			label.x = x + (width - label.width()) / 2;
			label.y = image.y + image.height() +2;
			align(label);
		}
		
		@Override
		protected void onTouchDown() {
			image.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.SND_CLICK, 1, 1, 0.8f );
		}
		
		@Override
		protected void onTouchUp() {
			image.resetColor();
		}
	}
}
