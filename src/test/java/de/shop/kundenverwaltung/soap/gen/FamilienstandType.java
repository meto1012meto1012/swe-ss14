
package de.shop.kundenverwaltung.soap.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für familienstandType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="familienstandType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LEDIG"/>
 *     &lt;enumeration value="VERHEIRATET"/>
 *     &lt;enumeration value="GESCHIEDEN"/>
 *     &lt;enumeration value="VERWITWET"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "familienstandType")
@XmlEnum
public enum FamilienstandType {

    LEDIG,
    VERHEIRATET,
    GESCHIEDEN,
    VERWITWET;

    public String value() {
        return name();
    }

    public static FamilienstandType fromValue(String v) {
        return valueOf(v);
    }

}
