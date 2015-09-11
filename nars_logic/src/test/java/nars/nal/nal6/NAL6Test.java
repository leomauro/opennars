package nars.nal.nal6;

import nars.NAR;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.nar.experimental.Equalized;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by me on 8/19/15.
 */
public class NAL6Test extends JavaNALTest {

        private final NAR seed;

        public NAL6Test(NAR b) { super(b); this.seed = b; }

        @Parameterized.Parameters(name= "{0}")
        public static Collection configurations() {
            return Arrays.asList(new Object[][]{
                    {new Default()},
//                    {new DefaultDeep()},
                    {new Equalized().nal(6) },
                    {new Equalized() },
//                    {new Default().setInternalExperience(null) },
                    {new Default().nal(6)},
//                    {new Classic().setInternalExperience(null) },

                    //{new Solid(1, 128, 1, 1, 1, 2).level(6)}


            });
        }



    @Test
    public void variable_unification1() throws InvalidInputException {
        tester.believe("<<$x --> bird> ==> <$x --> flyer>>").en("If something is a bird, then it is a flyer.");
        tester.believe("<<$y --> bird> ==> <$y --> flyer>>",0.00f,0.70f).en("If something is a bird, then it is not a flyer.");
        tester.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> flyer>>",0.79f,0.92f).en("If something is a bird, then usually, it is a flyer.");
        tester.run();
    }


    @Test
    public void variable_unification2() throws InvalidInputException {
        tester.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is a animal.");
        tester.believe("<<$y --> robin> ==> <$y --> bird>>").en("If something is a robin, then it is a bird.");
        tester.mustBelieve(100,"<<$1 --> robin> ==> <$1 --> animal>>",1.00f,0.81f).en("If something is a robin, then it is a animal.");
        tester.mustBelieve(100,"<<$1 --> animal> ==> <$1 --> robin>>",1.00f,0.45f).en(" I guess that if something is a animal, then it is a robin.");
        tester.run();
    }


    @Test
    public void variable_unification3() throws InvalidInputException {
        tester.believe("<<$x --> swan> ==> <$x --> bird>>",1.00f,0.80f).en("If something is a swan, then it is a bird.");
        tester.believe("<<$y --> swan> ==> <$y --> swimmer>>",0.80f,0.9f).en("If something is a swan, then it is a swimmer.");
        tester.mustBelieve(100,"<<$1 --> swan> ==> (||,<$1 --> bird>,<$1 --> swimmer>)>",1.00f,0.72f).en("I believe that if something is a swan, then it is a bird or a swimmer.");
        tester.mustBelieve(100,"<<$1 --> swan> ==> (&&,<$1 --> bird>,<$1 --> swimmer>)>",0.80f,0.72f).en("I believe that if something is a swan, then usually, it is both a bird and a swimmer.");
        tester.mustBelieve(100,"<<$1 --> swimmer> ==> <$1 --> bird>>",1.00f,0.37f).en("I guess if something is a swimmer, then it is a bird.");
        tester.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> swimmer>>",0.80f,0.42f).en("I guess if something is a bird, then it is a swimmer.");
        tester.mustBelieve(100,"<<$1 --> bird> <=> <$1 --> swimmer>>",0.80f,0.42f).en("I guess something is a bird, if and only if it is a swimmer.");
        tester.run();
    }


    @Test
    public void variable_unification4() throws InvalidInputException {
        tester.believe("<<bird --> $x> ==> <robin --> $x>>").en("What can be said about bird can also be said about robin.");
        tester.believe("<<swimmer --> $y> ==> <robin --> $y>>",0.70f,0.90f).en("What can be said about swimmer usually can also be said about robin.");
        tester.mustBelieve(100,"<(&&,<bird --> $1>,<swimmer --> $1>) ==> <robin --> $1>>",1.00f,0.81f).en("What can be said about bird and swimmer can also be said about robin.");
        tester.mustBelieve(100,"<(||,<bird --> $1>,<swimmer --> $1>) ==> <robin --> $1>>",0.70f,0.81f).en("What can be said about bird or swimmer can also be said about robin.");
        tester.mustBelieve(100,"<<bird --> $1> ==> <swimmer --> $1>>",1.00f,0.36f).en("I guess what can be said about bird can also be said about swimmer.");
        tester.mustBelieve(100,"<<swimmer --> $1> ==> <bird --> $1>>",0.70f,0.45f).en("I guess what can be said about swimmer can also be said about bird.");
        tester.mustBelieve(100,"<<bird --> $1> <=> <swimmer --> $1>>",0.70f,0.45f).en("I guess bird and swimmer share most properties.");
        tester.run();
    }


    @Test
    public void variable_unification5() throws InvalidInputException {
        tester.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>) ==> <$x --> bird>>").en("If something can fly and chirp, then it is a bird.");
        tester.believe("<<$y --> [withWings]> ==> <$y --> flyer>>").en("If something has wings, then it can fly.");
        tester.mustBelieve(100,"<(&&,<$1 --> [chirping]>,<$1 --> [withWings]>) ==> <$1 --> bird>>",1.00f,0.81f).en("If something can chirp and has wings, then it is a bird.");
        tester.run();
    }


    @Test
    public void variable_unification6() throws InvalidInputException {
        tester.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>, <(*, $x, worms) --> food>) ==> <$x --> bird>>").en("If something can fly, chirp, and eats worms, then it is a bird.");
        tester.believe("<(&&,<$y --> [chirping]>,<$y --> [withWings]>) ==> <$y --> bird>>").en("If something can chirp and has wings, then it is a bird.");
        tester.mustBelieve(100,"<(&&,<$1 --> flyer>,<(*,$1,worms) --> food>) ==> <$1 --> [withWings]>>",1.00f,0.45f).en("If something can fly and eats worms, then I guess it has wings.");
        tester.mustBelieve(100,"<<$1 --> [withWings]> ==> (&&,<$1 --> flyer>,<(*,$1,worms) --> food>)>",1.00f,0.45f).en("I guess if something has wings, then it can fly and eats worms.");
        tester.run();
    }


    @Test
    public void variable_unification7() throws InvalidInputException {
        tester.believe("<(&&,<$x --> flyer>,<(*,$x,worms) --> food>) ==> <$x --> bird>>").en("If something can fly and eats worms, then it is a bird.");
        tester.believe("<<$y --> flyer> ==> <$y --> [withWings]>>").en("If something can fly, then it has wings.");
        tester.mustBelieve(100,"<(&&,<$1 --> [withWings]>,<worms --> (/,food,$1,_)>) ==> <$1 --> bird>>",1.00f,0.45f).en("If something has wings and eats worms, then I guess it is a bird.");
        tester.run();
    }


    @Test
    public void variable_elimination() throws InvalidInputException {
        tester.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is an animal.");
        tester.believe("<robin --> bird>").en("A robin is a bird.");
        tester.mustBelieve(100,"<robin --> animal>",1.00f,0.81f).en("A robin is an animal.");
        tester.run();
    }


    @Test
    public void variable_elimination2() throws InvalidInputException {
        tester.believe("<<$x --> bird> ==> <$x --> animal>>").en("If something is a bird, then it is an animal.");
        tester.believe("<tiger --> animal>").en("A tiger is an animal.");
        tester.mustBelieve(100, "<tiger --> bird>", 1.00f,0.45f).en("I guess that a tiger is a bird.");
        tester.run();
    }


    @Test
    public void variable_elimination3() throws InvalidInputException {
        tester.believe("<<$x --> animal> <=> <$x --> bird>>").en("Something is a animal if and only if it is a bird.");
        tester.believe("<robin --> bird>").en("A robin is a bird.");
        tester.mustBelieve(100,"<robin --> animal>",1.00f,0.81f).en("A robin is a animal.");
        tester.run();
    }


    @Test
    public void variable_elimination4() throws InvalidInputException {
        tester.believe("(&&,<#x --> bird>,<#x --> swimmer>)").en("Some bird can swim.");
        tester.believe("<swan --> bird>",0.90f,0.9f).en("Swan is a type of bird.");
        tester.mustBelieve(100,"<swan --> swimmer>",0.90f,0.43f).en("I guess swan can swim.");
        tester.run();
    }


    @Test
    public void variable_elimination5() throws InvalidInputException {
        tester.believe("<{Tweety} --> [withWings]>").en("Tweety has wings.");
        tester.believe("<(&&,<$x --> [chirping]>,<$x --> [withWings]>) ==> <$x --> bird>>").en("If something can chirp and has wings, then it is a bird.");
        tester.mustBelieve(100,"<<{Tweety} --> [chirping]> ==> <{Tweety} --> bird>>",1.00f,0.81f).en("If Tweety can chirp, then it is a bird.");
        tester.run();
    }


    @Test
    public void variable_elimination6() throws InvalidInputException {
        tester.believe("<(&&,<$x --> flyer>,<$x --> [chirping]>, <(*, $x, worms) --> food>) ==> <$x --> bird>>").en("If something can fly, chirp, and eats worms, then it is a bird.");
        tester.believe("<{Tweety} --> flyer>").en("Tweety can fly.");
        tester.mustBelieve(100,"<(&&,<{Tweety} --> [chirping]>,<(*,{Tweety},worms) --> food>) ==> <{Tweety} --> bird>>",1.00f,0.81f).en("If Tweety can chirp and eats worms, then it is a bird.");
        tester.run();
    }


    @Test
    public void multiple_variable_elimination() throws InvalidInputException {
        tester.believe("<(&&,<$x --> key>,<$y --> lock>) ==> <$y --> (/,open,$x,_)>>").en("Every lock can be opened by every key.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>",1.00f,0.81f).en("Lock-1 can be opened by every key.");
        tester.run();
    }


    @Test
    public void multiple_variable_elimination2() throws InvalidInputException {
        tester.believe("<<$x --> lock> ==> (&&,<#y --> key>,<$x --> (/,open,#y,_)>)>").en("Every lock can be opened by some key.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"(&&,<#1 --> key>,<{lock1} --> (/,open,#1,_)>)",1.00f,0.81f).en("Some key can open Lock-1.");
        tester.run();
    }


    @Test
    public void multiple_variable_elimination3() throws InvalidInputException {
        tester.believe("(&&,<#x --> lock>,<<$y --> key> ==> <#x --> (/,open,$y,_)>>)").en("There is a lock that can be opened by every key.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"<<$1 --> key> ==> <{lock1} --> (/,open,$1,_)>>",1.00f,0.43f).en("I guess Lock-1 can be opened by every key.");
        tester.run();
    }


    @Test
    public void multiple_variable_elimination4() throws InvalidInputException {
        tester.believe("(&&,<#x --> (/,open,#y,_)>,<#x --> lock>,<#y --> key>)").en("There is a key that can open some lock.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"(&&,<#1 --> key>,<(*,#1,{lock1}) --> open>)",1.00f,0.43f).en("I guess there is a key that can open Lock-1.");
        tester.run();
    }


    @Test
    public void variable_introduction() throws InvalidInputException {
        tester.believe("<swan --> bird>").en("A swan is a bird.");
        tester.believe("<swan --> swimmer>",0.80f,0.9f).en("A swan is usually a swimmer.");
        tester.mustBelieve(100,"<<$1 --> bird> ==> <$1 --> swimmer>>",0.80f,0.45f).en("I guess a bird is usually a swimmer.");
        tester.mustBelieve(100,"<<$1 --> swimmer> ==> <$1 --> bird>>",1.00f,0.39f).en("I guess a swimmer is a bird.");
        tester.mustBelieve(100,"<<$1 --> swimmer> <=> <$1 --> bird>>",0.80f,0.45f).en("I guess a bird is usually a swimmer, and the other way around.");
        tester.mustBelieve(100,"(&&, <#1 --> swimmer>, <#1 --> bird>)",0.80f,0.81f).en("Some bird can swim.");
        tester.run();
    }


    @Test
    public void variable_introduction2() throws InvalidInputException {
        tester.believe("<gull --> swimmer>").en("A gull is a swimmer.");
        tester.believe("<swan --> swimmer>",0.80f,0.9f).en("Usually, a swan is a swimmer.");
        tester.mustBelieve(100,"<<gull --> $1> ==> <swan --> $1>>",0.80f,0.45f).en("I guess what can be said about gull usually can also be said about swan.");
        tester.mustBelieve(100,"<<swan --> $1> ==> <gull --> $1>>",1.00f,0.39f).en("I guess what can be said about swan can also be said about gull.");
        tester.mustBelieve(100,"<<gull --> $1> <=> <swan --> $1>>",0.80f,0.45f).en("I guess gull and swan share most properties.");
        tester.mustBelieve(100,"(&&,<gull --> #1>,<swan --> #1>)",0.80f,0.81f).en("Gull and swan have some common property.");
        tester.run();
    }


    @Test
    public void variables_introduction() throws InvalidInputException {
        tester.believe("<{key1} --> (/,open,_,{lock1})>").en("Key-1 opens Lock-1.");
        tester.believe("<{key1} --> key>").en("Key-1 is a key.");
        tester.mustBelieve(100, "<<$1 --> key> ==> <$1 --> (/,open,_,{lock1})>>", 1.00f,0.45f).en("I guess every key can open Lock-1.");
        tester.mustBelieve(100,"(&&,<#1 --> (/,open,_,{lock1})>,<#1 --> key>)",1.00f, 0.81f).en("Some key can open Lock-1.");
        tester.run();
    }


    @Test
    public void multiple_variables_introduction() throws InvalidInputException {
        tester.believe("<<$x --> key> ==> <{lock1} --> (/,open,$x,_)>>").en("Lock-1 can be opened by every key.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)",1.00f,0.81f).en("There is a lock that can be opened by every key.");
        tester.mustBelieve(100,"<(&&,<$1 --> key>,<$2 --> lock>) ==> <$2 --> (/,open,$1,_)>>",1.00f,0.45f).en("I guess every lock can be opened by every key.");
        tester.run();
    }


    @Test
    public void multiple_variables_introduction2() throws InvalidInputException {
        tester.believe("(&&,<#x --> key>,<{lock1} --> (/,open,#x,_)>)").en("Lock-1 can be opened by some key.");
        tester.believe("<{lock1} --> lock>").en("Lock-1 is a lock.");
        tester.mustBelieve(100,"(&&,<#1 --> key>,<#2 --> lock>,<#2 --> (/,open,#1,_)>)",1.00f,0.81f).en("There is a key that can open some lock.");
        tester.mustBelieve(100,"<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>",1.00f,0.45f).en("I guess every lock can be opened by some key.");
        tester.run();
    }


    @Test
    public void second_level_variable_unification() throws InvalidInputException {
        tester.believe("(&&,<#1 --> lock>,<<$2 --> key> ==> <#1 --> (/,open,$2,_)>>)",1.00f,0.90f).en("there is a lock which is opened by all keys");
        tester.believe("<{key1} --> key>",1.00f,0.90f).en("key1 is a key");
        tester.mustBelieve(100,"(&&,<#1 --> lock>,<#1 --> (/,open,{key1},_)>)",1.00f,0.81f).en("there is a lock which is opened by key1");
        tester.run();
    }


    @Test
    public void second_level_variable_unification2() throws InvalidInputException {
        tester.believe("<<$1 --> lock> ==> (&&,<#2 --> key>,<$1 --> (/,open,#2,_)>)>",1.00f,0.90f).en("all locks are opened by some key");
        tester.believe("<{key1} --> key>",1.00f,0.90f).en("key1 is a key");
        tester.mustBelieve(100,"<<$1 --> lock> ==> <$1 --> (/,open,{key1},_)>>",1.00f,0.43f).en("maybe all locks are opened by key1");
        tester.run();
    }


    @Test
    public void second_variable_introduction_induction() throws InvalidInputException {
        tester.believe("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>").en("if something opens lock1, it is a key");
        tester.believe("<lock1 --> lock>").en("lock1 is a key");
        tester.mustBelieve(100,"<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.45f).en("there is a lock with the property that when opened by something, this something is a key (induction)");
        tester.run();
    }


    @Test
    public void variable_elimination_deduction() throws InvalidInputException {
        tester.believe("<lock1 --> lock>",1.00f,0.90f).en("lock1 is a lock");
        tester.believe("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.90f).en("there is a lock with the property that when opened by something, this something is a key");
        tester.mustBelieve(100,"<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>",1.00f,0.43f).en("whatever opens lock1 is a key");
        tester.run();
    }


    @Test
    public void abduction_with_variable_elimination_abduction() throws InvalidInputException {
        tester.believe("<<lock1 --> (/,open,$1,_)> ==> <$1 --> key>>",1.00f,0.90f).en("whatever opens lock1 is a key");
        tester.believe("<(&&,<#1 --> lock>,<#1 --> (/,open,$2,_)>) ==> <$2 --> key>>",1.00f,0.90f).en("there is a lock with the property that when opened by something, this something is a key");
        tester.mustBelieve(100,"<lock1 --> lock>",1.00f,0.45f).en("lock1 is a lock");
        tester.run();
    }




    @Test
    public void recursionSmall() throws InvalidInputException {
        /*
        <0 --> num>. %1.00;0.90% {0 : 1}

        <<$1 --> num> ==> <($1) --> num>>. %1.00;0.90% {0 : 2}

        <(((0))) --> num>?  {0 : 3}

        1200

        ''outputMustContain('<(0) --> num>.')
        ''outputMustContain('<((0)) --> num>.')
        ''outputMustContain('<(((0))) --> num>.')
        ''outputMustContain('<(((0))) --> num>. %1.00;0.26%')
        */

        //TextOutput.out(nar);


        long time = /*seed instanceof Solid ? 100 :*/ 2500;

        float minConf = 0.66f;
        tester.believe("<0 --> num>", 1.0f, 0.9f);
        tester.believe("<<$1 --> num> ==> <($1) --> num>>", 1.0f, 0.9f);
        tester.ask("<(((0))) --> num>");
        tester.mustBelieve(time, "<(0) --> num>", 1.0f, 1.0f, 0.81f, 1.0f);
        tester.mustBelieve(time, "<((0)) --> num>", 1.0f, 1.0f, 0.73f, 1.0f);
        tester.mustBelieve(time, "<(((0))) --> num>", 1.0f, 1.0f, minConf, 1.0f);
        tester.run();

    }

}
