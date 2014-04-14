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

package de.shop.artikelverwaltung.web;

import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class SelectArtikel extends AbstractPage {
	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="selectForm" jsf:prependId="false">
	 *      <h:panelGrid id="selectGrid"
	 *         <r:select id="artikelBezeichnung" ...>
	 *            <c:forEach ...>
	 *               <f:selectItem id="artikelItem#4711" ...>
	 * HTML:
	 *   <form id="selectForm" ...>
	 *      <table id="selectGrid">
	 *         <span id="artikelBezeichnungButton" ...>
	 *           <span class="rf-sel-btn-arrow"></span>
	 * </pre>
	 */
	@FindBys({
		@FindBy(id = "artikelBezeichnungButton"),
		@FindBy(className = "rf-sel-btn-arrow")
	})
	private WebElement selectArrow;

	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="selectForm" jsf:prependId="false">
	 *      ...
	 *       <input jsf:id="selectButton" type="submit" ...>
	 * HTML:
	 *   <form id="selectForm" ...>
	 *      <input id="selectButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "selectButton")
	private WebElement auswaehlenButton;
	
	
	public WebElement getSelectArrow() {
		return selectArrow;
	}
	
	public void select(int zeilennr) {
		selectArrow.click();
		
		// RichFaces:
		//   <form jsf:id="selectForm" jsf:prependId="false">
		//      <h:panelGrid id="selectGrid"
		//         <r:select id="artikelBezeichnung" ...>
		//            <c:forEach ...>
		//               <f:selectItem id="artikelItem4711" ...>
		// HTML:
		//   <form id="selectForm" ...>
		//      <table id="selectGrid">
		//         <div id="artikelBezeichnung"
		//            <div id="artikelBezeichnungList"
		//               <div id="artikelBezeichnungItems"
		//                  <div id="artikelBezeichnungItem0"
		//                     Tisch 'Oval'
		waitGui().until().element(body.findElement(id("artikelBezeichnungList"))).is().visible();
		body.findElement(id("artikelBezeichnungItem" + zeilennr)).click();
		auswaehlenButton.click();
	}
}
