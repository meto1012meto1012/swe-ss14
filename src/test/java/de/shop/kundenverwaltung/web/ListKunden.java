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

package de.shop.kundenverwaltung.web;

import com.google.common.base.Strings;
import de.shop.util.AbstractPage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ListKunden extends AbstractPage {
	static final String TABELLE_ID = "kundenTabelle";
	

	//=========================================================================
	// Suchformular
	//=========================================================================
	
	/**
	 * <pre>
	 *  JSF:
	 *    <form jsf:id="form" jsf:prependId="false">
	 *       <r:autocomplete id="nachname" ...>
	 * HTML:
	 *    <div id="nachname" ...>
	 *       <input id="nachnameInput" type="text" ... />
	 * </pre>
	 */
	@FindBy(id = "nachnameInput")
	private WebElement nachnameFeld;
	
	/**
	 * <pre>
	 * RichFaces
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      ...
	 *       <r:commandButton id="sucheButton" ...>
	 * HTML:
	 *   <input id="sucheButton" type="submit" ...>
	 * </pre>
	 */
	@FindBy(id = "sucheButton")
	private WebElement sucheButton;
	

	//=========================================================================
	// Tabelle mit gefundenen Kunden
	//=========================================================================

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <r:dataTable id="kundenTabelle" ...>
	 * HTML:
	 *   <table id="kundenTabelle" ...>
	 * </pre>
	 */
	@FindBy(id = TABELLE_ID)
	private WebElement kundenTabelle;
	
	/**
	 * <pre>
	 *    <form jsf:id="form" jsf:prependId="false">
	 *       <r:dataTable id="kundenTabelle" ...>
	 * HTML:
	 * <table id="">
	 *   <tbody>
	 *     <tr>
	 *       <td class="rf-dt-nd-c">...
	 * <pre>
	 */
	@FindBys({
		@FindBy(id = TABELLE_ID),
		@FindBy(className = "rf-dt-nd-c")
		//@FindBy(xpath = "tbody/tr/td")
	})
	private WebElement fehlermeldung;
	
	
	
	public WebElement getNachnameFeld() {
		return nachnameFeld;
	}

	public ListKunden suchen(String prefix, String nachname) {
		if (nachname == null) {
			throw new IllegalArgumentException("nachname ist null");
		}
		
		nachnameFeld.clear();    // evtl. Vorbelegung loeschen

		if (!Strings.isNullOrEmpty(prefix)) {
			nachnameFeld.sendKeys(prefix);
			waitAjax().until().element(body.findElement(className("ui-autocomplete"))).is().visible();
			
			// RichFaces
			// <form jsf:id="..." jsf:prependId="false">
			//    <r:autocomplete id="nachname" ...>
			// JSF:
			//    <ul id="..." class="ui-autocomplete ..." ...>
			//       <li class="ui-menu-item" role="presentation">
			//          <a id="ui-id-7" class="ui-corner-all" tabindex="-1">
			//             Alpha
			body.findElement(className("ui-autocomplete"))
			    .findElements(className("ui-menu-item"))
			    .stream()
			    .map(elem -> elem.findElement(tagName("a")))
			    .filter(elem -> nachname.equals(elem.getText()))
			    .findFirst()
			    .get()
			    .click();
		}
		else {
			nachnameFeld.sendKeys(nachname);
		}

		sucheButton.click();
		
		return this;
	}
	
	public WebElement getKundenTabelle() {
		return kundenTabelle;
	}

	/**
	 * <pre>
	 * RichFaces:
	 *   <form jsf:id="form" jsf:prependId="false">
	 *      <r:dataTable id="kundenTabelle" ...>
	 *         <r:column id = "nachnameSpalte" ...>
	 * HTML:
	 *   <table id="kundenTabelle" ...>
	 *      <tbody ...>
	 *         <tr ...>
	 *            <td id="kundenTabelle:1:nachnameSpalte" ...>
	 * </pre>
	 * @param spalteId Nummer der Spalte beginnend mit 0
	 * @return Liste der Eintraege in der Spalte
	 */
	public List<WebElement> getSpalte(String spalteId) {
		final List<WebElement> spalte = new ArrayList<>();
		
		for (int zeilennr = 0; ; zeilennr++) {
			final String zelleId = TABELLE_ID + ":" + zeilennr + ":" + spalteId;
			WebElement zelle;
			
			try {
				zelle = kundenTabelle.findElement(id(zelleId));
			}
			catch (NoSuchElementException e) {
				break;
			}
			spalte.add(zelle);
		}
		
		return spalte;
	}
	
	public ViewKundePopup clickNachname(Long kundeId, WebDriver browser) {
		final int zeilennr = getZeilennr(kundeId);  // Zaehlung ab 0
		
		// Richtige Zeile gefunden: Nachname fuer Popup anklicken
		//    <form id="form" jsf:prependId="false">
		//       <r:dataTable id="kundenTabelle" ...>
		//          <r:column id="nachnameSpalte">
		//             <input jsf:id="nachnamePopup" type="submit" ...>
		// HTML:
		// <a id="kundenTabelle:...:nachnamePopup">
		final String idLink = TABELLE_ID + ":" + zeilennr + ":nachnamePopup";
		final WebElement link = kundenTabelle.findElement(id(idLink));
		link.click();
		
		final ViewKundePopup kundePopup = PageFactory.initElements(browser, ViewKundePopup.class);
		kundePopup.init(browser, zeilennr);
		
		return kundePopup;
	}
	
	private int getZeilennr(Long kundeId) {
		int zeilennr = 0;
		for (;;) {
			// Zeile mit der passenden Kunde-ID ermitteln: Zaehlung ab 0
			//    <form jsf:id="form" jsf:prependId="false">
			//       <r:dataTable id="kundenTabelle" ...>
			//          <r:column id="idSpalte">
			// HTML:
			// <td id="kundenTabelle:...:idSpalte">
			final String idZelle = TABELLE_ID + ":" + zeilennr + ":idSpalte";
			WebElement zelle;
			try {
				zelle = kundenTabelle.findElement(id(idZelle));
			}
			catch (NoSuchElementException e) {
				if (zeilennr == 0) {
					zeilennr = -1;
				}
				break;   // Es gibt keine weiteren Zeilen mehr
			}
			
			final String kundeIdStr = zelle.getText();
			final long tmpKundeId = Long.parseLong(kundeIdStr);
			if (tmpKundeId == kundeId.longValue()) {
				break;
			}
			
			zeilennr++;
		}
		
		return zeilennr;
	}
	
	public void clickUpdateButton(Long kundeId) {
		final int zeilennr = getZeilennr(kundeId);

		// Richtige Zeile gefunden: Edit-Button anklicken
		//    <form jsf:id="form" jsf:prependId="false">
		//       <r:dataTable id="kundenTabelle" ...>
		//          <r:column id="editSpalte">
		//             <input jsf:id="editButton"
		//                <image jsf:id="editIcon" jsf:name="..." jsf:library="..."
		// HTML:
		// <input id="kundenTabelle:...:editButton" type="image" ...>
		final String idButton = TABELLE_ID + ":" + zeilennr + ":editIcon";
		kundenTabelle.findElement(id(idButton))
		             .click();
		sleep(1);
	}

	public WebElement getFehlermeldung() {
		return fehlermeldung;
	}		
}
