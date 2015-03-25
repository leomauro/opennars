package ca.nengo.test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import nars.build.Default;
import nars.core.NAR;
import nars.io.narsese.NarseseParser;
import nars.logic.entity.*;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import sun.tracing.NullProviderFactory;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * autocompletion and structural editing of narsese
 */

/*
now, to try displaing this. Would be nice to keep this core independent of nengo stuff, but java lacks
 multiple inheritance, what now?
 */

public class Lang {

    //i need @BuildParseTree, but grappa wont play with this class, because its not a top level class?
    @BuildParseTree
    public class Parser extends NarseseParser {
        protected Parser(){super();}
    };

    public NAR nar = new NAR(new Default());
    public NarseseParser p;// = Parser.newParser(nar);
    int debugIndent = 0;

    private void debug(String s)
    {
        for (int i = 0; i < debugIndent; i++)
            System.out.print(" ");
        System.out.println(s);
    }


    public class Widget{
        public Node node;
        public Widget(Node n){
            this.node = n;
            debug(" new " + this);
        }
        public void print(){
            debug(""+this);
        }

        public Widget node2widget(Node n) {
            Object v = n.getValue();
            if (v == null)
                return null;
            Type t = v.getClass();
            debug(" " + n.getMatcher().getLabel() + "  " + t);
            if (t == Term.class)
                return new Word(n);
            else if (t == Float.class)
                return new Number(n);
            else if (t == String.class)
                return null;
            else if (t == Character.class)
                return null;
            else if (n.getMatcher().getLabel() == "s")
                return null;
            else if (n.getMatcher().getLabel() == "EOI")
                return null;
            else if (n.getMatcher().getLabel() == "zeroOrMore")
                return new ListWidget(n);
            else if (n.getMatcher().getLabel() == "oneOrMore")
                return new ListWidget(n);
            else if (n.getMatcher().getLabel() == "sequence")
                return children2one(n);
            else if (n.getMatcher().getLabel() == "firstOf")
                return children2one(n);
            else
                return new Syntaxed(n);
        }

        public Widget children2one(Node n)
        {
            ArrayList<Widget> items = children2list(n);
            if(items.size()==1)
                return items.get(0);
            else if(items.size()==0)
                return null;
            else
                throw new RuntimeException("ewwww " + n + ", " + items.size() + ", " +n.getChildren());
        }

        public ArrayList<Widget> children2list(Node n) {
            ArrayList<Widget> items = new ArrayList<Widget>();
            debugIndent++;
            for (Object o : n.getChildren()) {

                Node i = (Node) o;
                debug(" doing " + i);
                Widget w = (Widget) node2widget(i);
                debug(" done " + i);
                debug(" continuing with " + this);
                if (w != null)
                    items.add(w);
            }
            debugIndent--;
            return items;
        }


    };
    public class CompoundWidget extends Widget{
        public ArrayList<Widget> items;// = new ArrayList<Widget>();

        public CompoundWidget(Node n){
            super(n);
            items = children2list(n);
        }
        public void print(){
            debug(""+this+" with items: ");
            debugIndent++;
            for (Widget w:items)
                w.print();

            debugIndent--;
        }

    }

    public class ListWidget extends CompoundWidget{
        public ListWidget(Node n){
            super(n);
        }
    };

    public class Syntaxed extends CompoundWidget{
        public Syntaxed(Node n){
            super(n);
        }
    };
    public class Word extends Widget{
        public Word(Node n){super(n);}
    };
    public class Number extends Widget{
        public Number(Node n){super(n);}
        public float getValue(){
            return (float)node.getValue();
        }
    };


    //ELEMENTS OF A GRAMMAR RULE (OF ONE CHOICE), BESIDES STRING:
    /*
    private static class Sym {
        public static enum Param {LIST_ELEM_TYPE, SRSTR};
        public static class Params extends HashMap<Param, Object>{};
        public static Params newParams(){
            return new Params();
        }

        Map<Param, Object> params; // like the type of items of a List or min/max items
        Type type;
        public Sym(Class type, String name, Map<Param, Object> params){
            this.type = type;
        }
    };

    private Object some(Object sym){
        Sym.Params params = Sym.newParams();
        params.put(Sym.Param.LIST_ELEM_TYPE, sym);
        return new Sym(ListWidget.class, "", params);

    }
    */


    public Lang(){
        p = Parboiled.createParser(NarseseParser.class);
        p.memory = nar.memory;
    }
        /*
    //private class Sequence extends ImmutableList<Object> {}; // can contain String, Sym,
    private class Choices extends ArrayList<ImmutableList<Object>> {};
    private class Grammar extends HashMap<Object, Choices> {};
    private Grammar g;
    public Lang(){
        g = new Grammar();
        g.put(Task.class, new Choices());
        g.get(Task.class).add(l().add(
                BudgetValue.class).add(
                Sentence.class).build());
    }
    private ImmutableList.Builder<Object> l(){
        return new ImmutableList.Builder<Object>();
    }
        */



    public static void main(String[] args) {
        String input;
        //input = "<<(*,$a,$b,$c) --> Nadd> ==> <(*,$c,$a) --> NbiggerOrEqual>>.";
        //input = "<{light} --> [on]>.";
        //input = "(--,<goal --> reached>).";
        //input = "<(*,{tom},{sky}) --> likes>.";
        input = "<neutralization --> reaction>. <neutralization --> reaction>?";

        Lang l = new Lang();
        l.text2widget(input);
    }

    public void text2widget(String text)
    {

        ParseRunner rpr = new RecoveringParseRunner(p.Input());
        ParsingResult r = rpr.run(text);
        /*r.getValueStack().iterator().forEachRemaining(x -> {
            System.out.println("  " + x.getClass() + ' ' + x)});*/





        System.out.println("valid? " + (r.matched && (r.parseErrors.isEmpty())) );
        r.getValueStack().iterator().forEachRemaining(x -> System.out.println("  " + x.getClass() + ' ' + x));

        for (Object e : r.parseErrors) {
            if (e instanceof InvalidInputError) {
                InvalidInputError iie = (InvalidInputError) e;
                System.err.println(e);
                if (iie.getErrorMessage()!=null)
                    System.err.println(iie.getErrorMessage());
                for (MatcherPath m : iie.getFailedMatchers()) {
                    System.err.println("  ?-> " + m);
                }
                System.err.println(" at: " + iie.getStartIndex() + " to " + iie.getEndIndex());
            }
            else {
                System.err.println(e);
            }

        }

        System.out.println(printNodeTree(r));







        Node root = r.parseTreeRoot;
        //Object x = root.getValue();
        //System.out.println("  " + x.getClass() + ' ' + x);
        System.out.println();
        System.out.println(" " + root);
        System.out.println();
        Widget w = new ListWidget((Node)root.getChildren().get(1));
        System.out.println();
        System.out.println(" " + root);
        System.out.println();
        w.print();
    }
}
// PS.: fix the parser, but try to avoid hacky ways ;)