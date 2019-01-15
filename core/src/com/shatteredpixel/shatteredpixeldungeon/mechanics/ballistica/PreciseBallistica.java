package com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;

class PreciseBallistica extends Ballistica {

	private int stepA;
	private int stepB;
	private int dA;
	private int dB;

	private boolean hit;

	public PreciseBallistica(int from, int to, int params) {
		sourcePos = from;
		collisionPos = cast(from, to, (params & STOP_TARGET) > 0, (params & STOP_CHARS) > 0, (params & STOP_TERRAIN) > 0, (params & PIERCE_LOS_BLOCKING) > 0, (params & PIERCE_DOORS) > 0);
	}

	private int cast(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean pierceLOS, boolean pierceDoors) {

		int w = Dungeon.level.width();

		int x0 = from % w;
		int x1 = to % w;
		int y0 = from / w;
		int y1 = to / w;

		int dx = x1 - x0;
		int dy = y1 - y0;

		int stepX = dx > 0 ? +1 : -1;
		int stepY = dy > 0 ? +1 : -1;

		dx = Math.abs(dx);
		dy = Math.abs(dy);

		if (dx > dy) {

			stepA = stepX;
			stepB = stepY * w;
			dA = dx;
			dB = dy;

		} else {

			stepA = stepY * w;
			stepB = stepX;
			dA = dy;
			dB = dx;

		}

		int cell = calc(from, to, stopTarget, stopChars , stopTerrain, pierceLOS, pierceDoors, dA / 2);

		if (!hit) {

			for (int err = 0; err <= dA; err++) {
				int calc = calc(from, to, stopTarget, stopChars, stopTerrain, pierceLOS, pierceDoors, err);
				if (hit) {
					cell = calc;
					break;
				}
			}
		}

		return cell;

	}

	private int calc(int from, int to, boolean stopTarget, boolean stopChars, boolean stopTerrain, boolean pierceLOS, boolean pierceDoors, int err) {

		hit = false;
		dist = 0;
		path.clear();
		path.add(from);

		boolean collided = false;

		int cell = from;

		while (Dungeon.level.insideMap(cell)) {

			cell += stepA;

			err += dB;

			if (err >= dA) {
				err = err - dA;
				cell = cell + stepB;
			}


			path.add(cell);

			if (!collided) {

				dist++;

				if (cell == to) {
					hit = true;
					if (stopTarget) {
						collided = true;
					}
				}

				if (stopTerrain && !Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) {
					do {
						dist--;
					} while (!Dungeon.level.passable[path.get(dist)] && !Dungeon.level.avoid[path.get(dist)]);
					collided = true;
				} else if (!pierceLOS && Dungeon.level.losBlocking[cell]) {
					collided = true;
				} else if (stopChars && Actor.findChar(cell) != null) {
					collided = true;
				} else if (!pierceDoors && Dungeon.level.map[cell] == Terrain.DOOR) {
					collided = true;
				}
			}
		}

		return path.get(dist);
	}
}
