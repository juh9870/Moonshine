package com.shatteredpixel.shatteredpixeldungeon.items.guns;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.collectors.Collector;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.Attachment;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.barrels.Barrel;
import com.shatteredpixel.shatteredpixeldungeon.items.guns.attachments.locks.Lock;
import com.shatteredpixel.shatteredpixeldungeon.items.misc.EmptyItem;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.HighGrass;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ballistica.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class Gun<T extends Item & Ammo> extends Weapon implements RangedAttacker {

	private static final int USAGES_TO_KNOW = 10;

	public static final String AC_SHOT = "SHOT";

	public static final String AC_RELOAD = "RELOAD";

	{
		usesTargeting = true;
		defaultAction = AC_SHOT;
	}

	protected Class<? extends T> ammoClass = defAmmoClass();

	//Gun traits variables, modify in subclasses if you want to
	public boolean knockback = false;
	public boolean backfire = false;
	public boolean disarm = false;
	public boolean chooseAmmo = false;
	public boolean preciseShot = false;
	public boolean breakDoors = false;
	public boolean pierceDoors = false;
	public boolean breakGrass = false;
	public boolean pierceGrass = true;

	public boolean needEquip = false;
	public boolean canEquip = false;

	//Default gun stats, modify in subclasses if you want to
	protected float TIME_TO_RELOAD = 1f;
	protected float PIERCE_CHANCE = 0.0f;     //Base pierce chance
	protected float PIERCE_DECAY = 1.0f;     //Multiplier for pierce chance after each pierce
	protected float PIERCE_DMG_REDUCE = 1.0f;     //Multiplier for damage after each pierce
	protected float ENEMY_ARMOR_MOD = 1.0f;     //Modifier for enemy dr roll
	protected float ACCURACY_MOD = 1.0f;     //Multiplier for attackRoll
	protected float MISSFIRE_CHANCE = 0.0f;
	protected float MISSLOAD_CHANCE = 0.0f;
	protected float MISSLOAD_CURSE = 0.0f;
	protected float DISARM_CHANCE = 0.0f;
	protected float BACKFIRE_CHANCE = 0.0f;
	protected float KNOCKBACK_CHANCE = 0.0f;
	protected int KNOCKBACK_POWER = 2;        //Knockback power
	protected float DEBUFF_RATE = 0.0f;
	protected int DEBUFF_TRIES = 1;
	protected int CONSUMABLE_COUNT = 0;

	protected int AIMHELPER_BALLISTICA = Ballistica.PIERCE_LOS_BLOCKING | Ballistica.STOP_TERRAIN;


	public Class<? extends Collector> collector() {
		return null;
	}

	public abstract Class<? extends T> defAmmoClass();

	//OldGun logic variables, DON'T set them to null!
	public Load<T> load = new Load<>(maxLoadSize());
	public AttachManager attachManager = new AttachManager();

	//Identification variables
	public int usagesToKnow = USAGES_TO_KNOW;
	public boolean chargeknown = false;

	//<Melee weapons>

	@Override
	public int STRReq(int lvl) {
		lvl = Math.max(0, lvl);
		//strength req decreases at +1,+3,+6,+10,etc.
		return Math.max((8 + tier * 2) - (int) (Math.sqrt(8 * lvl + 1) - 1) / 2, 0);
	}

	@Override
	public int min(int lvl) {
		return (int) ((
				tier +
						lvl
		) * attachManager.meleeMinDmgMod());
	}

	@Override
	public int max(int lvl) {
		return (int) ((
				4 * (tier) +
						lvl * (tier)
		) * attachManager.meleeMaxDmgMod());
	}

	@Override
	public float speedFactor(Char owner) {
		return super.speedFactor(owner) * attachManager.meleeDlyMod();
	}

	//</Melee weapons>

	public int maxLoadSize() {
		return 1;
	}

	protected int flatMinGun(int lvl) {
		return tier + lvl;
	}

	protected int flatMaxGun(int lvl) {
		return 4 * (tier + 1) + lvl * (tier + 1);
	}

	public int minGun(int lvl) {
		return Math.min((int) (flatMinGun(lvl) * attachManager.minDmgMod()), maxGun(lvl));
	}

	public int maxGun(int lvl) {
		return (int) (flatMaxGun(lvl) * attachManager.maxDmgMod());
	}

	public int minGun() {
		return minGun(level());
	}

	public int maxGun() {
		return maxGun(level());
	}

	@Override
	public Item identify() {
		super.identify();
		chargeknown = true;
		return this;
	}
	//todo: bring it back when i get implement Amnesia challenge
//    @Override
//    public Item unIdentify(boolean curse) {
//        super.unIdentify(curse);
//        chargeknown=false;
//        return this;
//    }

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SHOT);
		actions.add(AC_RELOAD);
		if (!canEquip) {
			actions.remove(EquipableItem.AC_EQUIP);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (hero.ready) {
			if (!needEquip || isEquipped(hero)) {
				if (action.equals(AC_SHOT)) {
					if (!shot(hero)) {
						action = AC_RELOAD;
					}
				}

				if (action.equals(AC_RELOAD)) {
					QuickSlotButton.cancel();
					reload(hero);
				}

			} else {
				GLog.w(Messages.get(this, "not_equipped"));
				QuickSlotButton.cancel();
			}
		}
	}

	public void reload(Hero hero) {
		float spend = 0;
		if (load.needReload()) {
			if (chooseAmmo) {
				selectAmmo(hero);
				return;
			}
			spend = onReload(hero);
		}
		if (spend != 0) {
			hero.sprite.operate(hero.pos);
		} else {
			hero.next();
		}
	}

	public float onReload(Hero hero) {
		T ammo = hero.belongings.getItem(ammoClass);
		if (ammo != null) {
			if (attachManager.consumablesCount() > 0 && collector() != null) {
				Collector collector = hero.belongings.getItem(collector());
				if (collector == null || !collector.spend(attachManager.consumablesCount())) {
					GLog.w(collector.notEnough());
					return 0;
				}
			}
			T ammo_ = (T) ammo.clone().quantity(1);
			if (attachManager.missloadChance() <= 0 && Random.Float() < attachManager.missloadChance()) {
				Dungeon.level.drop(ammo_, hero.pos);
			} else {
				load.tryAdd(ammo_);
			}
			ammo.detach(hero.belongings.backpack);
			return attachManager.reloadTime();
		}
		return 0;
	}

	public boolean selectAmmo(final Hero hero) {
		WndBag.searchClass = ammoClass;
		GameScene.show(new WndBag(Dungeon.hero.belongings.backpack, item -> {
			ammoClass = (Class<? extends T>) item.getClass();
			hero.spendAndNext(onReload(hero));
		}, WndBag.Mode.CLASS, Messages.get(this, "ammo_choose")));
		return true;
	}

	public boolean shot(Hero hero) {
		if (load.hasAmmo()) {
			curBallistica=AIMHELPER_BALLISTICA;
			GameScene.selectCell(shooter);
			return true;
		}
		return false;
	}

	public ShotInfo buildShotPath(Hero hero, int target, T ammo) {
		ShotInfo si = new ShotInfo();

		Ballistica shot = new Ballistica(hero.pos, target, AIMHELPER_BALLISTICA, SPDSettings.preciseAim());

		si.path = new ArrayList<>();
		si.targets = new HashMap<>();
		si.destroyedDoors = new ArrayList<>();
		si.grassTrample = new ArrayList<>();
		si.wallHit = true;
		si.landed = false;
		List<Integer> path = shot.subPath(1, shot.dist);

		float pierce = 0;

		for (int cell : path) {
			si.path.add(cell);
			Char c = Char.findChar(cell);
			if (c != null) {
				si.targets.put(cell, (float) Math.pow(pierceDmgReduce(), pierce));
				if (Random.Float() < attachManager.pierceChanceMod() * ammo.pierceMod() * Math.pow(PIERCE_DECAY, pierce)) {
					pierce++;
				} else {
					si.wallHit = false;
					si.landed = false;
					si.targ = cell;
					break;
				}
			}
			if (Dungeon.level.map[cell] == Terrain.HIGH_GRASS) {
				if (breakGrass) si.grassTrample.add(cell);
				if (!pierceGrass) {
					if (breakGrass) {
						si.targ = cell;
					} else {
						if (si.path.size() > 1) {
							si.path.remove(si.path.size() - 1);
							si.targ = si.path.get(si.path.size() - 1);
						} else {
							si.targ = -1;
						}
					}
					break;
				}

			}
			if (Dungeon.level.map[cell] == Terrain.DOOR) {
				if (breakDoors) si.destroyedDoors.add(cell);
				if (!pierceDoors) {
					if (breakDoors) {
						si.targ = cell;
					} else {
						if (si.path.size() > 1) {
							si.path.remove(si.path.size() - 1);
							si.targ = si.path.get(si.path.size() - 1);
						} else {
							si.targ = -1;
						}
					}
					break;
				}
			} else if (Dungeon.level.solid[cell]) {
				if (si.path.size() > 1) {
					si.path.remove(si.path.size() - 1);
					si.targ = si.path.get(si.path.size() - 1);
					si.landed = false;
				} else {
					si.targ = -1;
				}
				break;
			}
			if (preciseShot && target == cell) {
				si.landed = true;
				si.targ = cell;
				break;
			}
			si.targ = cell;
		}

		return si;
	}

	public void fx(int from, int to, Callback callback, Item proto) {
		((MissileSprite) CellEmitter.get(from).parent.recycle(MissileSprite.class)).
				reset(from, to, proto, callback);
	}

	private boolean delay = false;

	protected CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			((Gun) curItem).doShoot(Dungeon.hero, target);
		}

		@Override
		public String prompt() {
			return Messages.get(Gun.class, "prompt");
		}
	};

	public void doShoot(final Hero hero, Integer target) {
		if (target != null) {

			final T ammo;
			if (load.hasAmmo())
				ammo = load.pop(0);
			else {
				reload(hero);
				return;
			}

			if (target == hero.pos) {
				GLog.i(Messages.get(this, "self_target"));
				return;
			}

			target = beforeUse(hero, target, ammo);

			if (target == null) {
				afterUse(hero, target);
				return;
			}

			final ShotInfo shot = buildShotPath(hero, target, ammo);

			if (shot.targ == -1) {
				GLog.i(Messages.get(this, "self_target"));
				hero.next();
				return;
			}

			load.remove(0);

			final int endPath = shot.targ;

			if (Actor.findChar(target) != null) {
				QuickSlotButton.target(Actor.findChar(target));
			}

			Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, .4f);
			if (hero.sprite != null)
				hero.sprite.zap(target);

			if (shot.wallHit || shot.landed) {
				fx(hero.pos, endPath, new Callback() {
					@Override
					public void call() {
						onShot(hero, endPath, 1, ammo, true);
					}
				}, new EmptyItem());
			}

			for (int cc : shot.targets.keySet()) {
				final int c = cc;
				fx(hero.pos, c, new Callback() {
					@Override
					public void call() {
						onShot(hero, c, shot.targets.get(c), ammo, false);
					}
				}, new EmptyItem());
			}

			for (int c : shot.grassTrample) {
				final int cell = c;
				fx(hero.pos, cell, new Callback() {
					@Override
					public void call() {
						HighGrass.trample(Dungeon.level, cell, null);
					}
				}, new EmptyItem());
			}
			for (int c : shot.destroyedDoors) {
				final int cell = c;
				fx(hero.pos, cell, new Callback() {
					@Override
					public void call() {
						Dungeon.level.map[cell] = Terrain.EMBERS;
						GameScene.updateMap(cell);
					}
				}, new EmptyItem());
			}

			fx(hero.pos, endPath, new Callback() {
				@Override
				public void call() {
					afterUse(hero, endPath);
				}
			}, ammo);
		}
	}

	public Integer beforeUse(final Hero hero, int targ, T ammo) {
		hero.busy();

		Invisibility.dispel();

		boolean ret = false;

		if (knockback && ((hero.STR() < STRReq() && Random.Float() < KNOCKBACK_CHANCE) || (cursed && Random.Float() < KNOCKBACK_CHANCE * 2))) {
			int oppositeHero = hero.pos + (hero.pos - targ);
			Ballistica trajectory = new Ballistica(hero.pos, oppositeHero, Ballistica.MAGIC_BOLT);
			WandOfBlastWave.throwChar(hero, trajectory, (int) (KNOCKBACK_POWER * attachManager.knockMod()), true, new Callback() {
				@Override
				public void call() {
					hero.next();
				}
			});
			targ = new int[]{targ, targ + Random.element((Integer[]) GameArrays.wrap(PathFinder.NEIGHBOURS9))}[Random.chances(new float[]{2, 1})];
			ret = true;
			delay = true;
		}

		if (hero.STR() < STRReq() && disarm && !cursed && Random.Float() < DISARM_CHANCE) {
			if (hero.belongings.weapon.isEquipped(this)) doUnequip(hero, false);
			else detach(hero.belongings.backpack);
			Dungeon.level.drop(this, hero.pos);
			targ += Random.element((Integer[]) GameArrays.wrap(PathFinder.NEIGHBOURS9));
			ret = true;
		}

		if (ret) return targ;

		if (cursed && Random.Float() < BACKFIRE_CHANCE) {
			onShot(hero, hero.pos, 1, ammo, false);
			return null;
		}

		if (Random.Float() < attachManager.missfireChance()) {
			Dungeon.level.drop(ammo, hero.pos);
			return null;
		}

		return targ;
	}

	public void afterUse(Hero hero, Integer target) {
		if (!delay)
			hero.next();
		if (!isIdentified()) {
			usagesToKnow--;
			if (usagesToKnow <= 0) {
				identify();
				GLog.w(Messages.get(Gun.class, "identify", name()));
			}
		}
		updateQuickslot();
	}

	public void onShot(Hero hero, int cell, float dmgMod, T ammo, boolean wallhit) {
		Char c = Actor.findChar(cell);
		if (c != null) {
			onHit(hero, c, dmgMod, ammo);
		} else {
			if (wallhit)
				ammo.onWall(cell);
			else
				ammo.onMiss(cell);
		}
	}

	public void onHit(Hero hero, Char enemy, float dmgMod, T ammo) {

		boolean visibleFight = Dungeon.level.heroFOV[enemy.pos];
		if (hit(hero, enemy)) {

			int dmg = gunDamageRoll(ammo);
			dmg -= enemy.drRoll() * attachManager.enemyArmorMod() * ammo.armorPiercingRate();
			dmg = attachManager.attackProc(enemy, dmg, this, ammo);
			dmg = shotProc(enemy, dmg, this, ammo);
			dmg = enemy.defenseProc(curUser, dmg);

			enemy.damage(dmg, ammo);
			if (visibleFight) {
				Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));

				if (enemy.sprite != null && hero.sprite != null) {
					enemy.sprite.bloodBurstA(hero.sprite.center(), dmg);
					enemy.sprite.flash();
				}
				if (!enemy.isAlive()) {
//					hero.onKill(enemy);
				}
			}

		} else {
			if (visibleFight) {
				String defense = enemy.defenseVerb();
				enemy.sprite.showStatus(CharSprite.NEUTRAL, defense);
			}
			ammo.onMiss(enemy.pos);
		}

	}

	public int gunDamageRoll(T ammo) {
		return Random.NormalIntRange((int) (minGun() * ammo.minDmgMod()), (int) (maxGun() * ammo.maxDmgMod()));
	}

	public int shotProc(Char enemy, int damage, Gun gun, T ammo) {
		Class<? extends Buff>[] buffs = GameArrays.concat(buffs(), ammo.buffs());
		Float[] buffsChances = GameArrays.concat(buffsChances(), ammo.buffsChances());
		Float[] buffsDuration = GameArrays.concat(buffsDuration(), ammo.buffsDuration());
		for (int i = 0; i < DEBUFF_TRIES; i++) {
			if (Random.Float() < DEBUFF_RATE) {
				int j = Random.chances((float[]) GameArrays.simplify(buffsChances));
				Class<? extends Buff> b = buffs[j];
				if (FlavourBuff.class.isAssignableFrom(b) && buffsDuration[j] != null)
					Buff.affect(enemy, (Class<? extends FlavourBuff>) b, buffsDuration[j] != -1 ? buffsDuration[j] : Math.max(2, Random.NormalIntRange(1, damage)));
				else if (LeveledBuff.class.isAssignableFrom(b)) {
					Buff.affect(enemy, (Class<? extends LeveledBuff>) b).set(buffsDuration[j] != -1 ? buffsDuration[j] : Math.max(2, Random.NormalIntRange(1, damage)));
				} else
					Buff.affect(enemy, b);
			}
		}
		return damage;
	}

	public Class<? extends Buff>[] buffs() {
		return new Class[0];
	}

	public Float[] buffsChances() {
		return new Float[0];
	}

	public Float[] buffsDuration() {
		return new Float[0];
	}

	@Override
	public boolean canShoot(Hero user, Char enemy) {
		return new Ballistica(user.pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}

	@Override
	public void doShoot(Hero user, Char target) {
		curItem = this;
		curUser = user;
		doShoot(user, target.pos);
	}

	@Override
	public String status() {
		if (levelKnown) {
			return (chargeknown ? load.size() : "?") + "/" + load.capacity;
		} else {
			return "?/?";
		}
	}


	public boolean hit(Hero attacker, Char defender) {
		float mod = (float) Math.pow(0.8f, Math.max(STRReq() - attacker.STR(), 0));
		mod *= attachManager.acuMod();

		float acuRoll = Random.Float(attacker.attackSkill(defender) * mod);
		float defRoll = Random.Float(defender.defenseSkill(attacker));
		if (attacker.buff(Bless.class) != null) acuRoll *= 1.20f;
		if (defender.buff(Bless.class) != null) defRoll *= 1.20f;

		//todo: uncomment when i implement Drunk debuff.
//        Drunk dr = attacker.buff(Drunk.class);
//        if (dr!=null){
//            acuRoll*=dr.acumod();
//        }

		return acuRoll >= defRoll;
	}

	private static final String CHARGEKNOWN = "chargeknown";
	private static final String LOAD = "load";
	private static final String ATTACHMENTS = "attachmanager";
	private static final String AMMO_CLASS = "ammo_class";
	private static final String UNFAMILIRIARITY = "unfamiliarity";
	private static final String AMMO_LOAD = "ammo_load";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(UNFAMILIRIARITY, usagesToKnow);
		bundle.put(CHARGEKNOWN, chargeknown);
		bundle.put(LOAD, load);
		attachManager.storeInBundle(bundle);
		bundle.put(AMMO_CLASS, ammoClass);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		usagesToKnow = bundle.getInt(UNFAMILIRIARITY);

		if (bundle.contains(AMMO_LOAD)) {
			Class[] ammoLoad = bundle.getClassArray(AMMO_LOAD);

			load = new Load<>(ammoLoad.length);
			attachManager = new AttachManager();
			ammoClass = defAmmoClass();
			chargeknown = isIdentified();

			for (int i = 0; i < ammoLoad.length; i++) {
				try {
					load.tryAdd(ClassReflection.newInstance(ammoClass));
				} catch (ReflectionException ignored) {

				}
			}
		} else {
			chargeknown = bundle.getBoolean(CHARGEKNOWN);
			load = (Load<T>) bundle.get(LOAD);
			attachManager.restoreFromBundle(bundle);
			ammoClass = bundle.getClass(AMMO_CLASS);
		}
	}


	public float pierceDmgReduce() {
		return PIERCE_DMG_REDUCE;
	}

	public static class ShotInfo {
		public ArrayList<Integer> path;
		public HashMap<Integer, Float> targets;
		public ArrayList<Integer> destroyedDoors;
		public ArrayList<Integer> grassTrample;
		public int targ;
		public boolean wallHit;
		public boolean landed;
	}

	public class AttachManager implements Bundlable {

		public Attachment barrel;
		public Attachment lock;
		public Attachment butt;
		public Attachment aim;

		public void random() {
			do {
				barrel = Barrel.random();
				lock = Lock.random();
			} while (consumablesCount() <= 0);
		}

		public float acuMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.acuMod();
			if (lock != null)
				a *= lock.acuMod();
			if (butt != null)
				a *= butt.acuMod();
			if (aim != null)
				a *= aim.acuMod();
			return a * ACCURACY_MOD;
		}

		public float reloadTime() {
			float a = 1;
			if (barrel != null)
				a *= barrel.reloadTimeMod();
			if (lock != null)
				a *= lock.reloadTimeMod();
			if (butt != null)
				a *= butt.reloadTimeMod();
			if (aim != null)
				a *= aim.reloadTimeMod();
			return a * TIME_TO_RELOAD;
		}

		public float meleeDlyMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.meleeDlyMod();
			if (lock != null)
				a *= lock.meleeDlyMod();
			if (butt != null)
				a *= butt.meleeDlyMod();
			if (aim != null)
				a *= aim.meleeDlyMod();
			return a;
		}

		public float meleeMinDmgMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.meleeMinDmgMod();
			if (lock != null)
				a *= lock.meleeMinDmgMod();
			if (butt != null)
				a *= butt.meleeMinDmgMod();
			if (aim != null)
				a *= aim.meleeMinDmgMod();
			return a;
		}

		public float meleeMaxDmgMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.meleeMaxDmgMod();
			if (lock != null)
				a *= lock.meleeMaxDmgMod();
			if (butt != null)
				a *= butt.meleeMaxDmgMod();
			if (aim != null)
				a *= aim.meleeMaxDmgMod();
			return a;
		}

		public float knockMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.knockMod();
			if (lock != null)
				a *= lock.knockMod();
			if (butt != null)
				a *= butt.knockMod();
			if (aim != null)
				a *= aim.knockMod();
			return a;
		}

		public float minDmgMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.minDmgMod();
			if (lock != null)
				a *= lock.minDmgMod();
			if (butt != null)
				a *= butt.minDmgMod();
			if (aim != null)
				a *= aim.minDmgMod();
			return a;
		}

		public float maxDmgMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.maxDmgMod();
			if (lock != null)
				a *= lock.maxDmgMod();
			if (butt != null)
				a *= butt.maxDmgMod();
			if (aim != null)
				a *= aim.maxDmgMod();
			return a;
		}

		public float pierceChanceMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.pierceMod();
			if (lock != null)
				a *= lock.pierceMod();
			if (butt != null)
				a *= butt.pierceMod();
			if (aim != null)
				a *= aim.pierceMod();
			return a * PIERCE_CHANCE;
		}

		public float enemyArmorMod() {
			float a = 1;
			if (barrel != null)
				a *= barrel.enemyArmorMod();
			if (lock != null)
				a *= lock.enemyArmorMod();
			if (butt != null)
				a *= butt.enemyArmorMod();
			if (aim != null)
				a *= aim.enemyArmorMod();
			return a * ENEMY_ARMOR_MOD;
		}

		public float missfireChance() {
			float a = 1;
			if (barrel != null)
				a *= barrel.missfireChance();
			if (lock != null)
				a *= lock.missfireChance();
			if (butt != null)
				a *= butt.missfireChance();
			if (aim != null)
				a *= aim.missfireChance();
			return a * MISSFIRE_CHANCE;
		}

		public float missloadChance() {
			float a = 1;
			if (barrel != null)
				a *= barrel.missloadChance();
			if (lock != null)
				a *= lock.missloadChance();
			if (butt != null)
				a *= butt.missloadChance();
			if (aim != null)
				a *= aim.missloadChance();
			return a * MISSLOAD_CHANCE;
		}

		public int consumablesCount() {
			int a = 0;
			if (barrel != null)
				a += barrel.consumablesMod();
			if (lock != null)
				a += lock.consumablesMod();
			if (butt != null)
				a += butt.consumablesMod();
			if (aim != null)
				a += aim.consumablesMod();
			return a + CONSUMABLE_COUNT;
		}

		public int attackProc(Char enemy, int damage, Gun g, T ammo) {

			if (barrel != null)
				damage = barrel.attackProc(enemy, damage, g, ammo);
			if (lock != null)
				damage = lock.attackProc(enemy, damage, g, ammo);
			if (butt != null)
				damage = butt.attackProc(enemy, damage, g, ammo);
			if (aim != null)
				damage = aim.attackProc(enemy, damage, g, ammo);
			return damage;
		}

		public String[] names() {
			String[] ret = new String[0];
			if (barrel != null) {
				ret = GameArrays.push(ret, barrel.name());
			}
			if (lock != null) {
				ret = GameArrays.push(ret, lock.name());
			}
			if (butt != null) {
				ret = GameArrays.push(ret, butt.name());
			}
			if (aim != null) {
				ret = GameArrays.push(ret, aim.name());
			}
			return ret;
		}

		public static final String BARREL = "attach.barrel";
		public static final String LOCK = "attach.lock";
		public static final String BUTT = "attach.butt";
		public static final String AIM = "attach.aim";

		@Override
		public void restoreFromBundle(Bundle bundle) {
			barrel = (Attachment) bundle.get(BARREL);
			lock = (Attachment) bundle.get(LOCK);
			butt = (Attachment) bundle.get(BUTT);
			aim = (Attachment) bundle.get(AIM);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put(BARREL, barrel);
			bundle.put(LOCK, lock);
			bundle.put(BUTT, butt);
			bundle.put(AIM, aim);
		}
	}

	public static class Load<T extends Ammo & Bundlable> implements Bundlable {

		private ArrayList<T> load = new ArrayList<>();

		private int capacity;

		public Load() {
			//Don't use that
		}

		public Load(int capacity) {
			this.capacity = capacity;
			load = new ArrayList<>();
		}

		public boolean tryAdd(T ammo) {
			if (load.size() < capacity) {
				add(ammo);
				return true;
			}
			return false;
		}

		public boolean tryAddToFront(T ammo) {
			if (load.size() < capacity) {
				addToFront(ammo);
				return true;
			}
			return false;
		}

		public T pull(int id) {
			T a = pop(id);
			remove(id);
			return a;
		}

		public Load<T> addToFront(T ammo) {
			load.add(0, ammo);
			return this;
		}

		public Load<T> add(T ammo) {
			load.add(ammo);
			return this;
		}

		public T pop(int id) {
			return load.get(id);
		}

		public Load<T> remove(int id) {
			load.remove(id);
			return this;
		}

		public boolean hasAmmo() {
			return load.size() > 0;
		}

		public boolean needReload() {
			return load.size() < capacity;
		}

		public int size() {
			return load.size();
		}

		public void fill(Class<? extends T> item) {
			clear();
			try {
				while (tryAdd(ClassReflection.newInstance(item))) {

				}
			} catch (ReflectionException ignored) {
			}
		}

		public void clear() {
			load = new ArrayList<>(capacity);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			load = new ArrayList<>();
			Collection<Bundlable> cb = bundle.getCollection("load");
			for (int i = 0; i < cb.size(); i++) load.add((T) (((ArrayList<Bundlable>) cb).get(i)));
			capacity = bundle.getInt("capacity");
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			bundle.put("load", load);
			bundle.put("capacity", capacity);
		}
	}
}
