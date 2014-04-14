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
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Named;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Named
@SessionScoped
@Log
public class Sprache implements Serializable {
	private static final long serialVersionUID = 1986565724093259408L;
	
	@Produces
	@Named
	@Client
	private Locale locale;

	
	@PostConstruct
	private void postConstruct() {
		locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
	}

	public void change(String localeStr) {
		final Locale newLocale = new Locale(localeStr);
		if (newLocale.equals(locale)) {
			return;
		}
		
		locale = newLocale;
		
		final FacesContext ctx = FacesContext.getCurrentInstance();
		ctx.getViewRoot().setLocale(locale);
		ctx.renderResponse();
	}
	
//    @Produces
//    @Faces
//    public Locale getLocale() {
//		final FacesContext ctx = FacesContext.getCurrentInstance();
//    	final UIViewRoot viewRoot = ctx.getViewRoot();
//        return viewRoot != null
//        	   ? viewRoot.getLocale()
//        	   : ctx.getApplication().getViewHandler().calculateLocale(ctx);
//    }
	
	@Override
	public String toString() {
		return "Sprache [locale=" + locale + "]";
	}
}
