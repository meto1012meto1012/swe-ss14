
package de.shop.kundenverwaltung.soap.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für geschlechtType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="geschlechtType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MAENNLICH"/>
 *     &lt;enumeration value="WEIBLICH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "geschlechtType")
@XmlEnum
public enum GeschlechtType {

    MAENNLICH,
    WEIBLICH;

    public String value() {
        return name();
    }

    public static GeschlechtType fromValue(String v) {
        return valueOf(v);
    }

}
