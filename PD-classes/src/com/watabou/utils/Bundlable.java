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

package com.watabou.utils;


import java.lang.reflect.Field;

public interface Bundlable {

	default void storeInBundle(Bundle bundle ){
		Class c = getClass();
		do {
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				if (f.getAnnotation(Storeable.class) != null) {
					try {
						f.setAccessible(true);
						Object o = f.get(this);
						if (o==null)continue;
						if (o instanceof Integer) {
							bundle.put(f.getName(), (Integer) o);
						} else if (o instanceof Float) {
							bundle.put(f.getName(), (Float) o);
						} else if (o instanceof Boolean) {
							bundle.put(f.getName(), (Boolean) o);
						} else if (o instanceof Long) {
							bundle.put(f.getName(), (Long) o);
						} else if (o instanceof String) {
							bundle.put(f.getName(), (String) o);
						} else if (o instanceof Class) {
							bundle.put(f.getName(), (Class) o);
						} else if (o instanceof Enum) {
							bundle.put(f.getName(), (Enum<?>) o);
						} else if (o instanceof Bundle) {
							bundle.put(f.getName(), (Bundle) o);
						} else if (o instanceof Bundlable || Bundlable.class.isAssignableFrom(f.getType())) {
							bundle.put(f.getName(), (Bundlable) o);
						} else if (o instanceof int[]) {
							bundle.put(f.getName(), (int[]) o);
						} else if (o instanceof float[]) {
							bundle.put(f.getName(), (float[]) o);
						} else if (o instanceof boolean[]) {
							bundle.put(f.getName(), (boolean[]) o);
						} else if (o instanceof String[]) {
							bundle.put(f.getName(), (String[]) o);
						} else if (o instanceof Class[]) {
							bundle.put(f.getName(), (Class[]) o);
						} else {
							throw new UnsupportedOperationException("Automatic storing of " + o.getClass() + " is not supported.");
						}
					} catch (IllegalAccessException ignored) {
					}
				}
			}
		} while ((c=c.getSuperclass())!=null);
	}

	default void restoreFromBundle(Bundle bundle ){
		Class c = getClass();
		do {
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				Storeable ann;
				if ((ann = f.getAnnotation(Storeable.class)) != null) {
					try {
						f.setAccessible(true);
						Object value;

						Class cl = f.getType();
						if (!bundle.contains(f.getName())&&ann.ignoreNull()){
							continue;
						}
						try {
							if (int.class.isAssignableFrom(cl)) {
								value = bundle.getInt(f.getName());
							} else if (float.class.isAssignableFrom(cl)) {
								value = bundle.getFloat(f.getName());
							} else if (boolean.class.isAssignableFrom(cl)) {
								value = bundle.getBoolean(f.getName());
							} else if (long.class.isAssignableFrom(cl)) {
								value = bundle.getLong(f.getName());
							} else if (String.class.isAssignableFrom(cl)) {
								value = bundle.getString(f.getName());
							} else if (Class.class.isAssignableFrom(cl)) {
								value = bundle.getClass(f.getName());
							} else if (Enum.class.isAssignableFrom(cl)) {
								value = bundle.getEnum(f.getName(), ann.enumClass());
							} else if (Bundle.class.isAssignableFrom(cl)) {
								value = bundle.getBundle(f.getName());
							} else if (Bundlable.class.isAssignableFrom(cl)) {
								value = bundle.get(f.getName());
							} else if (int[].class.isAssignableFrom(cl)) {
								value = bundle.getIntArray(f.getName());
							} else if (float[].class.isAssignableFrom(cl)) {
								value = bundle.getFloatArray(f.getName());
							} else if (boolean[].class.isAssignableFrom(cl)) {
								value = bundle.getBooleanArray(f.getName());
							} else if (String[].class.isAssignableFrom(cl)) {
								value = bundle.getStringArray(f.getName());
							} else if (Class[].class.isAssignableFrom(cl)) {
								value = bundle.getClassArray(f.getName());
							} else {
								throw new UnsupportedOperationException("Automatic restoring of " + cl + " is not supported.");
							}
						} catch (NullPointerException e) {
							if (ann.ignoreExceptions()) {
								value = null;
							} else {
								throw e;
							}
						}

						f.set(this, value);
					} catch (IllegalAccessException ignored) {}
				}
			}
		} while ((c=c.getSuperclass())!=null);
	}
	
}
