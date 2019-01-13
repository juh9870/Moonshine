package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.watabou.utils.Bundle;

public class LeveledBuff extends Buff {

	public void set( float level ) {
		this.level = Math.max(this.level, level);
	}

	public void extend( float duration ) {
		this.level += duration;
	}


	protected float level;

	private static final String LEVEL	= "level";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );

	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
	}
}
