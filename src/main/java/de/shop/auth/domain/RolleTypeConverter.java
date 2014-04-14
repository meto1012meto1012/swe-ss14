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

package de.shop.auth.domain;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Converter(autoApply = true)
@FacesConverter(forClass = RolleType.class)
public class RolleTypeConverter implements AttributeConverter<RolleType, String>, javax.faces.convert.Converter {
	@Override
	public String convertToDatabaseColumn(RolleType rolleType) {
		if (rolleType == null) {
			return null;
		}
		return rolleType.getValue();
	}

	@Override
	public RolleType convertToEntityAttribute(String value) {
		return RolleType.build(value);
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent comp, Object obj) {
		if (obj == null) {
			return "";
		}
		
		return RolleType.class
				        .cast(obj)
				        .getValue();
	}
	
	@Override
	public Object getAsObject(FacesContext ctx, UIComponent comp, String str) {
		return RolleType.build(str);
	}
}
