package net.sf.jcgm.core;

/**
 * Element class 9: Application Structure Descriptor
 *
 * @author diaa (daviddiana11)
 * @version $Id$
 * @since Jul 26, 2022
 */
public enum ApplicationStructureDescriptorElement {

    UNUSED_0(0),
    APPLICATION_STRUCTURE_ATTRIBUTE(1),
    ;

    private final int elementCode;

    ApplicationStructureDescriptorElement(int elementCode) {
        this.elementCode = elementCode;
    }

    public static ApplicationStructureDescriptorElement getElement(int ec) {
        if (ec < 0 || ec >= values().length)
            throw new ArrayIndexOutOfBoundsException(ec);

        return values()[ec];
    }

    @Override
    public String toString() {
        return name().concat("(").concat(String.valueOf(this.elementCode)).concat(")");
    }
}
