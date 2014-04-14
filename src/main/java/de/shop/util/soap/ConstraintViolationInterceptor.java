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

package de.shop.util.soap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Path.Node;

import static javax.validation.ElementKind.RETURN_VALUE;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ConstraintViolationInterceptor implements Serializable {
	private static final long serialVersionUID = -5185419395102871261L;

	@AroundInvoke
	public Object log(InvocationContext ctx) throws Exception {
		Object result = null;
		try {
			result = ctx.proceed();
		}
		catch (ConstraintViolationException e) {
			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			
			// Rueckgabewert null oder leere Liste? d.h. NOT_FOUND?
			if (violations.size() > 1) {
				throw new RuntimeException(e);
			}

			final ConstraintViolation<?> violation = violations.iterator().next();
			
			// Ende des Validation-Pfades suchen
			final Iterator<Path.Node> pathIterator = violation.getPropertyPath().iterator();
			Node node = null;
			while (pathIterator.hasNext()) {
				node = pathIterator.next();
			}
			
			// Verletzung des Rueckgabewertes?
			if (node != null && node.getKind() == RETURN_VALUE) {
				final Object invalidValue = violation.getInvalidValue();
				// null oder leere Liste?
				if (invalidValue == null || (invalidValue instanceof List && ((List<?>) invalidValue).isEmpty())) {
					return invalidValue;
				}
				throw new RuntimeException(e);
			}
		}
		return result;
	}
}
