package hk.siggi.bukkit.chestshop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
	
	private ReflectionUtil() {
	}
	
	public static Method getMethod(Class clazz, String methodName, Class... params) throws NoSuchMethodException {
		NoSuchMethodException nsme = null;
		while (true) {
			try {
				return clazz.getDeclaredMethod(methodName, params);
			} catch (NoSuchMethodException e) {
				if (nsme == null) {
					nsme = e;
				}
				if (clazz.equals(Object.class)) {
					throw nsme;
				} else {
					clazz = clazz.getSuperclass();
				}
			} catch (SecurityException e) {
				throw e;
			}
		}
	}
	
	public static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		NoSuchFieldException nsfe = null;
		while (true) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				if (nsfe == null) {nsfe = e;}
				if (clazz.equals(Object.class)) {throw nsfe;} else {clazz=clazz.getSuperclass();}
			} catch (SecurityException e) {
				throw e;
			}
		}
	}
	
	public static void setInt(Object obj, String fieldName, int value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field field = getField(obj.getClass(), fieldName);
		field.setAccessible(true);
		field.setInt(obj, value);
	}
}
