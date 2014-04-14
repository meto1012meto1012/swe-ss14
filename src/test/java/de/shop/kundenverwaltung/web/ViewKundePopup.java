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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.openqa.selenium.By.id;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
public class ViewKundePopup {
	private WebDriver browser;
	private int zeilennr;

	public void init(WebDriver browser, int zeilennr) {
		this.browser = browser;
		this.zeilennr = zeilennr;
	}
	
	public String getNachname() {
		// RichFaces:
		//   <form jsf:id="form" jsf:prependId="false">
		//      <r:dataTable id="kundenTabelle" ...>
		//         <r:column id="nachnameSpalte">
		//            <r:popupPanel id="popup" ...>
		//               <r:tabPanel id="tabPanel" ...>
		//                  <r:tab id="stammdatenTab">
		//                     <h:panelGrid id="kundeGrid"
		//                        <h:outputText id="nachname" ...>
		// HTML:
		//   <table id="kundenTabelle" ...>
		//      <tbody>
		//         <tr>
		//            <td id="kundenTabelle:...:nachnameSpalte"
		//               <div id="kundenTabelle:...:popup_container"
		//                  <div id="kundenTabelle:...:tabPanel"
		//                     <div id="kundenTabelle:...:stammdatenTab"
		//                        <table id="kundenTabelle:...:kundeGrid" ...>
		//                           <span id="kundenTabelle:...:nachname">
		final String nachnameId = ListKunden.TABELLE_ID + ":" + zeilennr + ":nachname";
		final String nachname = browser.findElement(id(nachnameId)).getText();
		return nachname;
	}
	
	public ViewKundePopup clickBestellungenTab() {
		// RichFaces:
		//   <form id="form" jsf:prependId="false">
		//      <r:dataTable id="kundenTabelle" ...>
		//         <r:column id="nachnameSpalte">
		//            <r:popupPanel id="popup" ...>
		//               <r:tabPanel id="tabPanel" ...>
		//                  <r:tab id="stammdatenTab">
		//                  <r:tab id="bestellungenTab">
		//                     <f:facet name="header">
		//                        <img jsf:id="bestellungenGif"
		// HTML:
		//   <table id="kundenTabelle" ...>
		//      <tbody>
		//         <tr>
		//            <td id="kundenTabelle:...:nachnameSpalte"
		//               <div id="kundenTabelle:...:popup_container"
		//                  <div id="kundenTabelle:...:tabPanel"
		//                     <div id="kundenTabelle:...:bestellungenTab:header:active"
		//                        <img id="bestellungenGif"
		final String bestellungenTabId = ListKunden.TABELLE_ID + ":" + zeilennr + ":bestellungenGif";
		browser.findElement(id(bestellungenTabId))
		      .click();
		return this;
	}
	
	public ViewKundePopup clickAlleBestellungen() {
		// RichFaces:
		//   <form jsf:id="form" jsf:prependId="false">
		//      <r:dataTable id="kundenTabelle" ...>
		//         <r:column id="nachnameSpalte">
		//            <r:popupPanel id="popup" ...>
		//               <r:tabPanel id="tabPanel" ...>
		//                  <r:tab id="stammdatenTab">
		//                  <r:tab id="bestellungenTab">
		//                     <r:dataTable id="bestellungenTabelle" ...>
		//                        <r:column id="togglerSpalte" colspan="3">
		//                           <r:collapsibleSubTableToggler id="subTableToggler"
		// HTML:
		//   <table id="kundenTabelle" ...>
		//      <tbody>
		//         <tr>
		//            <td id="kundenTabelle:...:nachnameSpalte"
		//               <div id="kundenTabelle:...:popup_container"
		//                  <div id="kundenTabelle:...:tabPanel"
		//                     <div id="kundenTabelle:...:stammdatenTab"
		//                     <div id="kundenTabelle:...:bestellungenTab"
		//                        <div id="kundenTabelle:...:bestellungenTab:content"
		//                           <table id="kundenTabelle:...:bestellungenTabelle" ...>
		//                              <span id="kundenTabelle:...:bestellungenTabelle:...:subTableToggler" ...>
		//                                 <span id="kundenTabelle:...:bestellungenTabelle:...:subTableToggler:collapsed" ...>
		final String bestellungenTabelleId = ListKunden.TABELLE_ID + ":" + zeilennr + ":bestellungenTabelle";
		final WebElement bestellungenTabelle = browser.findElement(id(bestellungenTabelleId));
		int zeilennrBestellung = 0;
		for (;;) {
			WebElement bestellungToggler;
			try {
				bestellungToggler = bestellungenTabelle.findElement(id(bestellungenTabelleId
						                                               + ":"
						                                               + zeilennrBestellung
						                                               + ":subTableToggler:collapsed"));
			}
			catch (NoSuchElementException e) {
				break;
			}
			bestellungToggler.click();
		
			// RichFaces:
			//   <form jsf:id="form" jsf:prependId="false">
			//      <r:dataTable id="kundenTabelle" ...>
			//         <r:column id="nachnameSpalte">
			//            <r:popupPanel id="popup" ...>
			//               <r:tabPanel id="tabPanel" ...>
			//                  <r:tab id="stammdatenTab">
			//                  <r:tab id="bestellungenTab">
			//                     <r:dataTable id="bestellungenTabelle" ...>
			//                        <r:column id="togglerSpalte" ...>
			//                           <r:collapsibleSubTableToggler id="subTableToggler"
			//                        <r:collapsibleSubTable id="positionenSubtable"
			//                            <r:column id="artikelIdSpalteSub">
			// HTML:
			//   <table id="kundenTabelle" ...>
			//      <tbody>
			//         <tr>
			//            <td id="kundenTabelle:...:nachnameSpalte"
			//               <div id="kundenTabelle:...:popup_container"
			//                  <div id="kundenTabelle:...:tabPanel"
			//                     <div id="kundenTabelle:...:stammdatenTab"
			//                     <div id="kundenTabelle:...:bestellungenTab"
			//                        <div id="kundenTabelle:...:bestellungenTab:content"
			//                           <table id="kundenTabelle:...:bestellungenTabelle" ...>
			//                              <tbody
			//                                 <tr
			//                                    <td id="kundenTabelle:...:bestellungenTabelle:...:positionenSubtable:0:artikelIdSpalteSub"
			bestellungenTabelle.findElement(id(bestellungenTabelleId
                                               + ":"
                                               + zeilennrBestellung
                                               + ":positionenSubtable:0:artikelIdSpalteSub"));
	        
	        zeilennrBestellung++;
		}
		if (zeilennrBestellung == 0) {
			throw new RuntimeException("Keine Bestellung vorhanden");
		}

		return this;
	}
	
	public void close() {
		// RichFaces:
		//   <form jsf:id="form" jsf:prependId="false">
		//      <r:dataTable id="kundenTabelle" ...>
		//         <r:column id="nachnameSpalte">
		//            <r:popupPanel id="popup" ...>
		//               <f:facet name="controls">
		//                  <h:outputLink id="hideControl" ...>
		//                     <h:graphicImage id="hideIcon" ...>
		// HTML:
		//   <table id="kundenTabelle" ...>
		//      <tbody>
		//         <tr>
		//            <td id="kundenTabelle:...:nachnameSpalte"
		//               <div id="kundenTabelle:...:popup_container"
		//                  <div id="kundenTabelle:...:popup_header_controls"
		//                     <a id="kundenTabelle:...:hideControl"
		//                        <img id="kundenTabelle:...:hideIcon">
		final String closeId = ListKunden.TABELLE_ID + ":" + zeilennr + ":hideIcon";
		browser.findElement(id(closeId))
		       .click();
	}
}

