/*
 * Copyright (C) 2013 Juergen Zimmermann, Hochschule Karlsruhe
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.shop.util.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import static java.util.logging.Level.FINER;


/**
 * Interceptor zur Protokollierung von public-Methoden der CDI-faehigen Beans und der EJBs.
 * Sowohl der Methodenaufruf als auch der Rueckgabewert werden mit Level FINER protokolliert.
 * Exceptions werden nicht protokolliert, um den Stacktrace zu erhalten.
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Interceptor
@Log
@Dependent    // FIXME LogInterceptor braucht @Dependent https://issues.jboss.org/browse/WELD-1540
public class LogInterceptor implements Serializable {
	private static final long serialVersionUID = 6225006198548883927L;
	
	private static final String COUNT = "=Anzahl:";
	private static final int MAX_ELEM = 4;  // bei Collections wird ab 5 Elementen nur die Anzahl ausgegeben
	
	private static final int CHAR_POS_AFTER_SET = 3; // getX...
	private static final int CHAR_POS_AFTER_IS = 2; // isX...
	private static final int CHAR_POS_AFTER_GET = 3; // setX...
	
	@AroundConstruct
	public void logConstructor(InvocationContext ctx) throws Exception {
		final Class<?> clazz = ctx.getConstructor().getDeclaringClass();
		final Logger logger = Logger.getLogger(clazz.getName());

		if (logger.isLoggable(FINER)) {
			if (clazz.getAnnotation(Stateless.class) != null) {
				logger.finer("Stateless EJB wurde erzeugt");				
			}
			else if (clazz.getAnnotation(Stateful.class) != null) {
				logger.finer("Stateful EJB wurde erzeugt");
			}
			else {
				logger.finer("CDI-faehiges Bean wurde erzeugt");
			}
		}
		
		ctx.proceed();
	}
	
	@AroundInvoke
	public Object log(InvocationContext ctx) throws Exception {
		final Logger logger = Logger.getLogger(ctx.getTarget().getClass().getName());

		if (!logger.isLoggable(FINER)) {
			return ctx.proceed();
		}

		final Method method = ctx.getMethod();
		final String methodName = method.getName();

		// KEINE Protokollierung von get-, set-, is-Methoden sowie toString(), equals() und hashCode()
		if ((methodName.startsWith("get")) && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_GET))) {
			return ctx.proceed();
		}
		else if ((methodName.startsWith("set")) && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_SET))) {
			return ctx.proceed();
		}
		else if ((methodName.startsWith("is")) && Character.isUpperCase(methodName.charAt(CHAR_POS_AFTER_IS))) {
			return ctx.proceed();
		}
		else if ("toString".equals(methodName) || "equals".equals(methodName) || "hashCode".equals(methodName)) {
			return ctx.proceed();
		}
		
		final Object[] params = ctx.getParameters();
		final Parameter[] paramReps = method.getParameters();

		// Methodenaufruf protokollieren
		final StringBuilder sb = new StringBuilder();
		if (params != null) {
			final int anzahlParams = params.length;
			sb.append(": ");
			for (int i = 0; i < anzahlParams; i++) {
				if (params[i] == null) {
					sb.append(paramReps[i].getName());
					sb.append("=null");
				}
				else {
					final String paramStr = toString(params[i], paramReps[i].getName());
					sb.append(paramStr);
				}
				sb.append(", ");
			}
			final int laenge = sb.length();
			sb.delete(laenge - 2, laenge - 1);
		}
		logger.finer(methodName + " BEGINN" + sb);
		
		// Eigentlicher Methodenaufruf
		final Object result = ctx.proceed();

		// Ende der eigentlichen Methode protokollieren
		if (result == null) {
			// Methode vom Typ void oder Rueckgabewert null
			logger.finer(methodName + " ENDE");
		}
		else {
			logger.finer(methodName + " ENDE: " + toString(result, "result"));
		}
		
		return result;
	}
	
	/**
	 * Collection oder Array oder Objekt in einen String konvertieren
	 */
	private static String toString(Object obj, String paramName) {
		if (obj instanceof Collection<?>) {
			// Collection: Elemente bei kleiner Anzahl ausgeben; sonst nur die Anzahl
			final Collection<?> coll = (Collection<?>) obj;
			final int anzahl = coll.size();
			if (anzahl > MAX_ELEM) {
				return paramName + COUNT + coll.size();
			}

			return paramName + "=" + coll.toString();
		}
		
		if (obj.getClass().isArray()) {
			// Array in String konvertieren: Element fuer Element
			return arrayToString(obj, paramName);
		}

		// Objekt, aber keine Collection und kein Array
		return paramName + "=" + obj.toString();
	}
	
	/**
	 * Array in einen String konvertieren
	 */
	private static String arrayToString(Object obj, String paramName) {
		final Class<?> componentClass = obj.getClass().getComponentType();

		if (!componentClass.isPrimitive()) {
			// Array von Objekten
			final Object[] arr = (Object[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}
		
		// Array von primitiven Werten: byte, short, int, long, float, double, boolean, char
		
		if ("byte".equals(componentClass.getName())) {
			final byte[] arr = (byte[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}

		if ("short".equals(componentClass.getName())) {
			final short[] arr = (short[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}
		
		if ("int".equals(componentClass.getName())) {
			final int[] arr = (int[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}
		
		if ("long".equals(componentClass.getName())) {
			final long[] arr = (long[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}
		
		if ("float".equals(componentClass.getName())) {
			final float[] arr = (float[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}
		
		if ("double".equals(componentClass.getName())) {
			final double[] arr = (double[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}

		if ("boolean".equals(componentClass.getName())) {
			final boolean[] arr = (boolean[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}

		if ("char".equals(componentClass.getName())) {
			final char[] arr = (char[]) obj;
			if (arr.length > MAX_ELEM) {
				return paramName + COUNT + arr.length;
			}
			return paramName + "=" + Arrays.toString(arr);
		}

		return paramName + "=<<UNKNOWN PRIMITIVE ARRAY>>";
	}
}
