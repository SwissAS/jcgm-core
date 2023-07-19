package net.sf.jcgm.core;

import java.io.DataInput;
import java.io.IOException;

/**
 * Class=4, Element=6
 * @author diaa (daviddiana11)
 * @version $Id$
 * @since Dec 16, 2021
 */
public class AppendText extends Command {

    private final String string;
    private final boolean finalNotFinal;
    
    /** 
     * {@code true} if the cmd has already been executed i.e. the text appended to the previous {@link TextCommand};<br>
     * {@code false} otherwise
     */
    private boolean executed = false;

    public AppendText(int ec, int eid, int l, DataInput in, CGM cgm) throws IOException {
        super(ec, eid, l, in, cgm);

        this.finalNotFinal = makeEnum() >= 1;
        this.string = makeString();
    }

    @Override
    public void paint(CGMDisplay d) {
        if (this.executed) {
            // just ignore this cmd if it has already been executed - useful if commands are cached
            return;
        }

        this.executed = true;
        
        String decodedString = d.useSymbolEncoding() ? SymbolDecoder.decode(this.string) : this.string;

        d.appendText(decodedString);
        if (!this.finalNotFinal) {
            // if there is still text to append
            return;
        }

        TextCommand textCommand = d.getTextCommand();
        if (textCommand == null) {
            throw new IllegalArgumentException("cannot append and paint a string if there is no previous TextCommand");
        }
        
        textCommand.setStringComplete(true);
        textCommand.paint(d);
    }

    @Override
    public String toString() {
        return "AppendText" + " String [" + this.string + "]";
    }
}
