//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.02.11 at 11:48:30 PM CET 
//


package net.ivoa.xml.votable.v1;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="boolean"/>
 *     &lt;enumeration value="bit"/>
 *     &lt;enumeration value="unsignedByte"/>
 *     &lt;enumeration value="short"/>
 *     &lt;enumeration value="int"/>
 *     &lt;enumeration value="long"/>
 *     &lt;enumeration value="char"/>
 *     &lt;enumeration value="unicodeChar"/>
 *     &lt;enumeration value="float"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="floatComplex"/>
 *     &lt;enumeration value="doubleComplex"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "dataType")
@XmlEnum
public enum DataType implements Serializable {

    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),
    @XmlEnumValue("bit")
    BIT("bit"),
    @XmlEnumValue("unsignedByte")
    UNSIGNED_BYTE("unsignedByte"),
    @XmlEnumValue("short")
    SHORT("short"),
    @XmlEnumValue("int")
    INT("int"),
    @XmlEnumValue("long")
    LONG("long"),
    @XmlEnumValue("char")
    CHAR("char"),
    @XmlEnumValue("unicodeChar")
    UNICODE_CHAR("unicodeChar"),
    @XmlEnumValue("float")
    FLOAT("float"),
    @XmlEnumValue("double")
    DOUBLE("double"),
    @XmlEnumValue("floatComplex")
    FLOAT_COMPLEX("floatComplex"),
    @XmlEnumValue("doubleComplex")
    DOUBLE_COMPLEX("doubleComplex");
    private final String value;

    DataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataType fromValue(String v) {
        for (DataType c: DataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
