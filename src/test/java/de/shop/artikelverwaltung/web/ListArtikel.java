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

import com.google.common.base.Strings;
import de.shop.util.AbstractPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ListArtikel extends AbstractPage {
	private static final String ARTIKEL_TABELLE = "artikelTabelle";
	
	//=========================================================================
	// Suchformular
	//=========================================================================

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <h:panelGrid id="sucheGrid" ...>
	 *         <input jsf:id="bezeichnung" ...>
	 * HTML:
	 *   ...
	 *      <table id="sucheGrid">
	 *         <tbody>
	 *            <tr>
	 *               <td>
	 *                  <input id="bezeichnung" type="text" ...>
	 * </pre>
	 */
	@FindBy(id = "bezeichnung")
	private WebElement bezeichnungFeld;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <r:commandButton id="findButton" ...>
	 * HTML:
	 *   <input id="findButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "findButton")
	private WebElement sucheButton;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <r:dataTable id="artikelTabelle" ...>
	 * HTML:
	 *   <form id="form"
	 *      <table id="artikelTabelle"
	 * </pre>
	 */
	@FindBy(id = ARTIKEL_TABELLE)
	private WebElement artikelTabelle;

	
	public WebElement getBezeichnungFeld() {
		return bezeichnungFeld;
	}
	
	public ListArtikel suchen(String bezeichnung) {
		bezeichnungFeld.clear();    // evtl. Vorbelegung loeschen
		if (!Strings.isNullOrEmpty(bezeichnung)) {
			bezeichnungFeld.sendKeys(bezeichnung);
		}
		sucheButton.click();
		return this;
	}
	
	public WebElement getArtikelTabelle() {
		return artikelTabelle;
	}
	
	public void inDenWarenkorb(int zeilennr) {
		// <r:dataTable id="artikelTabelle" ...>
		//    <r:column id="buttonSpalte" ...>
		//       <h:commandLink id="warenkorbButton" ...>
		//          <h:graphicImage id="warenkorbIcon" ...>
		// <table id="artikelTabelle"
		//    <tbody id="artikelTabelle:tb"
		//       <tr id="artikelTabelle:0"
		//          <td id="artikelTabelle:0:buttonSpalte"
		//             <a id="artikelTabelle:0:warenkorbButton"
		//                <img id="artikelTabelle:0:warenkorbIcon"
		final WebElement wkIcon = artikelTabelle.findElement(id(ARTIKEL_TABELLE + ":" + zeilennr
				                                                + ":warenkorbIcon"));
		wkIcon.click();
		// TODO warum 2x auf das Warenkorb-Icon klicken?
		wkIcon.click();
	}
}
