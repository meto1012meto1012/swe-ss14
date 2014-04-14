
package de.shop.kundenverwaltung.soap.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hobbyType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="hobbyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SPORT"/>
 *     &lt;enumeration value="LESEN"/>
 *     &lt;enumeration value="REISEN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "hobbyType")
@XmlEnum
public enum HobbyType {

    SPORT,
    LESEN,
    REISEN;

    public String value() {
        return name();
    }

    public static HobbyType fromValue(String v) {
        return valueOf(v);
    }

}
