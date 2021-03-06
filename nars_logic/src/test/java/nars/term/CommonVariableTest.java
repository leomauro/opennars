package nars.term;

import nars.Narsese;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


/**
 * Created by me on 9/9/15.
 */
public class CommonVariableTest {

    Narsese p = Narsese.the();

    @Test
    public void commonVariableTest1() {
        assertEquals("%1%2",
                CommonVariable.make(
                        p.term("%1"),
                        p.term("%2")).toString());

        //reverse order
        assertEquals("%1%2",
                CommonVariable.make(
                        p.term("%2"),
                        p.term("%1")).toString());
    }

    @Test
    public void commonVariableTest2() {
        //different lengths
        assertEquals("%2%00",
                CommonVariable.make(
                        p.term("%00"),
                        p.term("%2")).toString());
        //different lengths
        assertEquals("%2%00",
                CommonVariable.make(
                        p.term("%2"),
                        p.term("%00")).toString());

    }

    @Test
    public void commonVariableInstancing() {
        //different lengths

        CommonVariable ca = CommonVariable.make(
                p.term("%1"),
                p.term("%2"));
        CommonVariable cb = CommonVariable.make(
                p.term("%2"),
                p.term("%1"));

        assertEquals(ca, cb);
        Assert.assertTrue("efficient re-use of common variable of name length=1", ca == cb);
    }
}