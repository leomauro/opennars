package nars.nal.meta;

import com.google.common.collect.Sets;
import nars.nal.Deriver;
import nars.nal.TaskRule;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 8/15/15.
 */
public class RuleDerivationGraphTest {


    @Test
    public void testRuleStatistics() {
        //SimpleDeriver d = new SimpleDeriver(Deriver.standard);

        List<TaskRule> R = Deriver.standard;
        int registeredRules = R.size();


        Frequency f = new Frequency();
        for (TaskRule t : R)
            f.addValue(t);
        Iterator<Map.Entry<Comparable<?>, Long>> ii = f.entrySetIterator();
        while (ii.hasNext()) {
            Map.Entry<Comparable<?>, Long> e = ii.next();
            if (e.getValue() > 1) {
                System.err.println("duplicate: " + e);
            }
        }
        System.out.println("total: " + f.getSumFreq() + ", unique=" + f.getUniqueCount());

        HashSet<TaskRule> setRules = Sets.newHashSet(R);

        assertEquals("no duplicates", registeredRules, setRules.size());

        Set<PreCondition> preconds = new HashSet();
        int totalPrecond = 0;
        for (TaskRule t : R) {
            for (PreCondition p : t.postPreconditions) {
                totalPrecond++;
                preconds.add(p);
            }
        }
        System.out.println("total precondtions = " + totalPrecond + ", unique=" + preconds.size());

        //preconds.forEach(p -> System.out.println(p));


        //Set<TaskBeliefPair> ks = d.ruleIndex.keySet();
//        System.out.println("Patterns: keys=" + ks.size() + ", values=" + d.ruleIndex.size());
//        for (TaskBeliefPair pp : ks) {
//            System.out.println(pp + " x " + d.ruleIndex.get(pp).size());
//
//        }


    }



    @Test public void testRuleTrie() {
        RuleTrie x = new RuleTrie(Deriver.standard);
        x.printSummary();
    }

    @Test public void testPostconditionSingletons() {
//        System.out.println(PostCondition.postconditions.size() + " unique postconditions " + PostCondition.totalPostconditionsRequested);
//        for (PostCondition p : PostCondition.postconditions.values()) {
//            System.out.println(p);
//        }

    }

//    @Test
//    public void testDerivationComparator() {
//
//        NARComparator c = new NARComparator(
//                new Default(),
//                new Default()
//        ) {
//
//
//        };
//        c.input("<x --> y>.\n<y --> z>.\n");
//
//
//
//        int cycles = 64;
//        for (int i = 0; i < cycles; i++) {
//            if (!c.areEqual()) {
//
//                /*System.out.println("\ncycle: " + c.time());
//                c.printTasks("Original:", c.a);
//                c.printTasks("Rules:", c.b);*/
//
////                System.out.println(c.getAMinusB());
////                System.out.println(c.getBMinusA());
//            }
//            c.frame(1);
//        }
//
//        System.out.println("\nDifference: " + c.time());
//        System.out.println("Original - Rules:\n" + c.getAMinusB());
//        System.out.println("Rules - Original:\n" + c.getBMinusA());
//
//    }
}
