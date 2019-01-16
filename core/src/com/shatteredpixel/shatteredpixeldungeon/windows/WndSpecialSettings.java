package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.SimpleButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndSpecialSettings extends Window {

	private static final int WIDTH		    = 112;
	private static final int HEIGHT         = 138;
	private static final int SLIDER_HEIGHT	= 24;
	private static final int BTN_HEIGHT	    = 18;
	private static final int GAP_TINY 		= 2;
	private static final int GAP_SML 		= 6;
	private static final int GAP_LRG 		= 18;


	public WndSpecialSettings(){
		super();

		String[] chromes = Chrome.chromeStyles;

		SimpleButton btnLeft = new SimpleButton(Icons.BTN_LEFT.get()){
			@Override
			protected void onClick() {

				int id = (SPDSettings.chromeStyle()-1)%chromes.length;
				if (id<0)id=chromes.length+id;

				SPDSettings.chromeStyle(id);

				chromeChanged();
			}
		};
		SimpleButton btnRight = new SimpleButton(Icons.BTN_RIGHT.get()){
			@Override
			protected void onClick() {
				int id = (SPDSettings.chromeStyle()+1)%chromes.length;
				if (id<0)id=chromes.length-id;

				SPDSettings.chromeStyle(id);

				chromeChanged();
			}
		};
		RedButton text = new RedButton(Messages.titleCase(chromes[SPDSettings.chromeStyle()]));

		btnLeft.setPos(0,0);
		add(btnLeft);

		text.setRect(btnLeft.right()+GAP_TINY,0,WIDTH-btnLeft.width()-btnRight.width()-GAP_TINY*2,btnLeft.height());
		add(text);

		btnRight.setPos(text.right()+GAP_TINY,0);
		add(btnRight);

		resize(WIDTH, HEIGHT);
	}

	private void chromeChanged(){
		parent.add(new WndSpecialSettings());
		hide();
	}
}
