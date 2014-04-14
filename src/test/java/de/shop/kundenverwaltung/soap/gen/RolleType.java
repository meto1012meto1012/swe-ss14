
package de.shop.kundenverwaltung.soap.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für rolleType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="rolleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ADMIN"/>
 *     &lt;enumeration value="MITARBEITER"/>
 *     &lt;enumeration value="ABTEILUNGSLEITER"/>
 *     &lt;enumeration value="KUNDE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "rolleType")
@XmlEnum
public enum RolleType {

    ADMIN,
    MITARBEITER,
    ABTEILUNGSLEITER,
    KUNDE;

    public String value() {
        return name();
    }

    public static RolleType fromValue(String v) {
        return valueOf(v);
    }

}
