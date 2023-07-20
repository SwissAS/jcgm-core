package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=0, Element=21
 * @author diaa (daviddiana11)
 * @version $Id$
 * @since Jul 26, 2022
 */
public class BeginApplicationStructure extends Command {
    
    private final String applicationStructureIdentifier;
    private final String applicationStructureType;
    private final boolean inheritanceFlag;
    
    public BeginApplicationStructure(int ec, int eid, int l, DataInput in, CGM cgm) throws IOException {
        super(ec, eid, l, in, cgm);

        // application structure identifier (SF)
        this.applicationStructureIdentifier = makeString();
        
        // application structure type (SF)
        this.applicationStructureType = makeString();
        
        // inheritance flag (one of: statelist, application structure) (E)
        this.inheritanceFlag = makeEnum() >= 1;
        
        assert this.currentArg == this.args.length;
    }

    @Override
    public void paint(CGMDisplay d) {
        d.setWithinApplicationStructureBody(true);
    }

    @Override
    public String toString() {
        return "Unsupported - BeginApplicationStructure [" +
                "applicationStructureIdentifier='" + this.applicationStructureIdentifier + '\'' +
                ", applicationStructureType='" + this.applicationStructureType + '\'' +
                ", inheritanceFlag=" + this.inheritanceFlag +
                ']';
    }
}
