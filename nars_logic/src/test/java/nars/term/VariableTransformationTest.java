package nars.term;

import nars.NAR;
import nars.nal.nal5.Conjunction;
import nars.nar.Default;
import nars.task.Task;
import nars.term.transform.TermVisitor;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 5/13/15.
 */
public class VariableTransformationTest {

    @Test public void testTransformVariables() {
        NAR nar = new Default();
        Compound c = nar.term("<$a --> x>");
        Compound d = Compound.transformIndependentToDependentVariables(c).normalized();
        assertTrue(c!=d);
        assertEquals(d, nar.term("<#1 --> x>"));
    }

    @Test
    public void testDestructiveNormalization() {
        String t = "<$x --> y>";
        String n = "<$1 --> y>";
        NAR nar = new Default();
        Term x = nar.term(t);
        assertEquals(n, x.toString());
        assertTrue("immediate construction of a term from a string should automatically be normalized", x.isNormalized());

    }

    @Test
    public void testCombinations() {


        String same = "(&&,<$1 --> x>,<$1 --> y>)";
        String different = "(&&,<$1 --> x>,<$2 --> y>)";
        combine("<$1 --> x>", true, "<$1 --> y>", true, same);
        combine("<$1 --> x>", false, "<$1 --> y>", true, different);
        combine("<$1 --> x>", true, "<$1 --> y>", false, different);
        combine("<$1 --> x>", false, "<$1 --> y>", false, different);

    }

    static Term scope(Term x, boolean s) {

        x.recurseTerms(new TermVisitor() {

            boolean changed = false;

            @Override
            public void visit(Term t, Term superterm) {
                if (t instanceof Compound && t.hasVar()) {

                    Compound ct = ((Compound)t);
                    for (int i = 0; i < t.length(); i++) {
                        Term x = ct.term[i];
                        if (x instanceof Variable) {
                            Variable nv = ((Variable) x).clone(s);
                            if (nv!=ct.term[i]) changed = true;
                            ct.term[i] = nv;

                        }
                    }

                    if (changed)
                        ct.invalidate();
                }
            }
        });

        return x;
    }

    public void combine(String a, boolean scopedA, String b, boolean scopedB, String expect) {
        NAR n = new Default();
        Term ta = scope(n.term(a), scopedA);
        Term tb = scope(n.term(b), scopedB);
        Term c = Conjunction.make(ta, tb).normalized();

        Term e = n.term(expect).normalized();
        Term d = e.normalized();
        assertNotNull(e);
        assertEquals(a + " (" + scopedA + ")  +    "   + b + " (" + scopedB + ")", d, c);
        assertEquals(a + " (" + scopedA + ")  +    "   + b + " (" + scopedB + ")", e, c);
    }

    @Test public void varNormTestIndVar() {
        //<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>. gets changed to this: <<($1, $4) --> bigger> ==> <($2, $1) --> smaller>>. after input

        NAR n = new Default();

        String t = "<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>";

        Term term = n.term(t);
        Task task = n.task(t + ".");
        //n.input("<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>.");

        System.out.println(t);
        System.out.println(term);
        System.out.println(task);

        task = task.normalized();
        System.out.println(task);

        Task t2 = n.inputTask(t + ".");
        System.out.println(t2);

        //TextOutput.out(n);
        n.frame(10);

    }
}

