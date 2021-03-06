package nars.op.mental;

import nars.NAR;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class FullInternalExperience extends InternalExperience {

    public FullInternalExperience(NAR n) {
        super(n);
    }


    @Override
    public boolean isFull() {
        return true;
    }

}