package com.shatteredpixel.shatteredpixeldungeon.items.wands.tripple;

import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.watabou.utils.Callback;

public class CircularWand extends TrippleEffectWand {


	private class NormalEffect extends WandEffect {

		@Override
		public void onZap(Ballistica target) {

		}

		@Override
		protected void fx(Ballistica bolt, Callback callback) {
			MagicMissile.boltFromChar(curUser.sprite.parent,MagicMissile.BEACON,curUser.sprite,bolt.collisionPos,callback);
		}
	}
}
