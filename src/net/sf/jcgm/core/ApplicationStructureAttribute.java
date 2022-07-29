package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=9, Element=1
 * 
 * @author diaa (daviddiana11)
 * @version $Id$
 * @since Jul 26, 2022
 */
public class ApplicationStructureAttribute extends Command {

    private final String applicationStructureAttributeType;
    private final StructuredDataRecord sdr;
    
    public ApplicationStructureAttribute(int ec, int eid, int l, DataInput in) throws IOException {
        super(ec, eid, l, in);
        
        // application structure attribute type (SF)
        this.applicationStructureAttributeType = makeString();
        
        // data record (SDR)
        this.sdr = makeSDR();
        
        assert this.currentArg == this.args.length;
    }

    @Override
    public String toString() {
        return "Unsupported - ApplicationStructureAttribute [" +
                "applicationStructureAttributeType='" + this.applicationStructureAttributeType + '\'' +
                ", sdr=" + this.sdr +
                ']';
    }
}
