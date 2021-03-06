package nars.op.scheme;


import nars.NAR;
import nars.nal.AbstractNALTester;
import nars.util.meter.TestNAR;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class EvalSchemeTest extends AbstractNALTester {

    public EvalSchemeTest(Supplier<NAR> build) {
        super(build);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Iterable configurations() {
        return AbstractNALTester.nars(8, false);
    }


    @Ignore
    @Test
    public void testCAR() {

        TestNAR tester = test();
        tester.nar.input("scheme((car, (quote, (*, 2, 3))), #x)!");

        assertTrue("test impl unfinished", false);
        //tester.requires.add(new OutputContainsCondition(tester.nar, "<2 --> (/, scheme, (car, (quote, (2, 3))), _, SELF)>. :|: %1.00;0.99%", 1));

        tester.run(4);

    }

//    //----
//    @Test @Ignore
//    public void testDynamicBrainfuckProxy() throws Exception {
//
//        NAR n = new NAR(new Default().clock(new HardRealtimeClock(false)) );
//
//        //TextOutput.out(n);
//
//        BrainfuckMachine bf= new NALObjects(n).build("scm", BrainfuckMachine.class);
//
//        bf.execute("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.");
//
//        n.frame(6500);
//
//    }

/*
    @Test @Ignore
    public void testDynamicSchemeProxy() throws Exception {

        NAR n = new NAR(new Default().clock(new HardRealtimeClock(false)) );

        //TextOutput.out(n);

        SchemeClosure env = new NALObjects(n).build("scm", SchemeClosure.class);

        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";

        env.eval(input);

        List<Expression> result = env.eval("(factorial 3)");

        assertThat(result.get(0), is(number(6)));

        n.frame(50);

        n.frame(1660);
        }
*/

//
//
//        new Thread( () -> { Repl.repl(System.in, System.out, e); } ).start();
//
//        while (true) {
//            n.frame(10);
//            Thread.sleep(500);
//        }
//


//        Class derivedClass = new NALObject().add(new TestHandler()).connect(TestClass.class, n);
//
//        System.out.println(derivedClass);
//
//        Object x = derivedClass.newInstance();
//
//        System.out.println(x);
//
//        ((TestClass)x).callable();


}
