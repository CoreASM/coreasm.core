package myplugins.secondplugin;

import java.util.List;

import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.stdplugins.number.NumberElement;
import org.coreasm.engine.stdplugins.string.StringElement;

/** 
 * A CoreASM function to calculate a hash on String values
 */
public class HashValueFunction extends FunctionElement {

    @Override
    public Element getValue(List<Element> args) {
        Element result = Element.UNDEF;
        
        // if there is only one argument passed to this function
        if (args.size() == 1) { 
            Element argument = args.get(0);
            
            // if this argument is a StringElement
            if (argument instanceof StringElement) {
                result = new NumberElement(argument.toString().hashCode());
            }
            
        }
        
        return result;
    }

}

