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

    private String applicationStructureAttributeType;
    private StructuredDataRecord sdr;
    
    public ApplicationStructureAttribute(int ec, int eid, int l, DataInput in, CGM cgm) throws IOException {
        super(ec, eid, l, in, cgm);
        try {
            // application structure attribute type (SF)
            this.applicationStructureAttributeType = makeString();
            
            // data record (SDR)
            this.sdr = makeSDR();
        } catch (Exception e) {
            // do nothing
        }
        
        assert this.currentArg == this.args.length;
    }
    
    public String getApplicationStructureAttributeType() {
        return this.applicationStructureAttributeType;
    }
    
    public StructuredDataRecord getSdr() {
        return this.sdr;
    }

    @Override
    public String toString() {
        return "Unsupported - ApplicationStructureAttribute [" +
                "applicationStructureAttributeType='" + this.applicationStructureAttributeType + '\'' +
                ", sdr=" + this.sdr +
                ']';
    }
}
