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

package de.shop.util.web;

import de.shop.util.interceptor.Log;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@SessionScoped
@Named
@Log
public class Skin implements Serializable {
	private static final long serialVersionUID = -7795677531657784822L;
	
	private static final String BLUESKY_STRING = "blueSky";
	private static final String DEEPMARINE_STRING = "deepMarine";
	private static final String EMARALDTOWN_STRING = "emeraldTown";
	private static final String JAPANCHERRY_STRING = "japanCherry";
	private static final String RUBY_STRING = "ruby";
	private static final String WINE_STRING = "wine";
	private static final SkinType DEFAULT = SkinType.BLUESKY;
	
	private enum SkinType {
		BLUESKY(BLUESKY_STRING),
		DEEPMARINE(DEEPMARINE_STRING),
		EMARALDTOWN(EMARALDTOWN_STRING),
		JAPANCHERRY(JAPANCHERRY_STRING),
		RUBY(RUBY_STRING),
		WINE(WINE_STRING);
		
		private final String value;

		private SkinType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		public static SkinType build(String value) {
			switch (value) {
				case BLUESKY_STRING:
					return BLUESKY;
					
				case DEEPMARINE_STRING:
					return DEEPMARINE;
					
				case EMARALDTOWN_STRING:
					return EMARALDTOWN;
					
				case JAPANCHERRY_STRING:
					return JAPANCHERRY;
					
				case RUBY_STRING:
					return RUBY;
					
				case WINE_STRING:
					return WINE;
					
				default:
					return DEFAULT;
			}
		}
	}
	
	private SkinType value = DEFAULT;
	
	public String getValue() {
		return value.getValue();
	}
	
	public void setValue(String value) {
		this.value = SkinType.build(value);
	}
	
	public void change(String skinStr) {
		final SkinType newSkin = SkinType.build(skinStr);
		if (newSkin.equals(value)) {
			return;
		}
		
		value = newSkin;
		FacesContext.getCurrentInstance().renderResponse();
	}
}
