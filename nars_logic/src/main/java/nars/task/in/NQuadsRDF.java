package nars.task.in;

import nars.$;
import nars.NAR;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.task.FluentTask;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Stamp;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by me on 6/4/15.
 */
abstract public class NQuadsRDF {


    private final static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    //private static String parentTagName = null;

    //private final NAR nar;

    //final float beliefConfidence;
    //private boolean includeDataType = false;

//    public NQuadsRDF(NAR n, float beliefConfidence) throws Exception {
//        this.nar = n;
//        this.beliefConfidence = beliefConfidence;
//    }

    //input(new FileInputStream(nqLoc));

//
//    final static Pattern nQuads = Pattern.compile(
//            //"((?:\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?:@\\w+(?:-\\w+)?|\\^\\^<[^>]+>)?)|<[^>]+>|\\_\\:\\w+|\\.)"
//            "(<[^\\s]+>|_:(?:[A-Za-z][A-Za-z0-9\\-_]*))\\s+(<[^\\s]+>)\\s+(<[^\\s]+>|_:(?:[A-Za-z][A-Za-z0-9\\-_]*)|\\\"(?:(?:\\\"|[^\"])*)\\\"(?:@(?:[a-z]+[\\-A-Za-z0-9]*)|\\^\\^<(?:[^>]+)>)?)\\s+(<[^\\s]+>).*"
//    );

    public static void input(NAR nar, String input) throws Exception {
        NxParser p  = new NxParser();
        p.parse(Collections.singleton(input));
        input(nar, p);
    }

    public static void input(NAR nar, InputStream input) throws Exception {
        //try {
            NxParser p = new NxParser();
            p.parse(input);
            input(nar, p);
        //}
//        catch (Exception pe) {
//            //try turtle parser
//            TurtleParser p = new TurtleParser();
//            p.parse(new InputStreamReader(input), URI.create("_"));
//            input(nar, p);
//        }
    }

    public static void input(NAR nar, Iterable<Node[]> nxp) throws Exception {
        input(nar, StreamSupport.stream(nxp.spliterator(), false));
    }

    public static void input(NAR nar, Stream<Node[]> nxp) throws Exception {

        nar.input(
            nxp.map( (Node[] nx) -> {
                if (nx.length >= 3) {
                    return input(
                            nar,
                            resource(nx[0]),
                            resource(nx[1]),
                            resource(nx[2])
                    );
                }
                return null;
            } ).filter(x -> x!=null)
        );



    }

//    public static void input(NAR nar, File input) throws Exception {
//        input(nar, new Scanner(input));
//    }



    //TODO interpret Node subclasses in special ways, possibly returning Compounds not only Atom's
    public static Atom resource(Node n) {
        String s = n.getLabel();
        //if (s.startsWith("<") && s.endsWith(">")) {
            //s = s.substring(1, s.length() - 1);

            if (s.contains("#")) {
                String[] a = s.split("#");
                String p = a[0]; //TODO handle the namespace
                s = a[1];
            }
            else {
                String[] a = s.split("/");
                if (a.length == 0) return null;
                s = a[a.length - 1];
            }

            if (s.isEmpty()) return null;

            return Atom.the(s, true);
        //}
        //else
          //  return null;
    }


    static public Term atom(String uri) {
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash!=-1)
            uri = uri.substring(lastSlash + 1);

        switch(uri) {
            case "owl#Thing": uri = "thing"; break;
        }

        return Atom.the(uri);
    }

//
//    //TODO make this abstract for inserting the fact in different ways
//    protected void inputClass(Term clas) {
//        inputClassBelief(clas);
//    }

//    private void inputClassBelief(Term clas) {
//        nar.believe(isAClass(clas));
//    }

//    public static Term isAClass(Term clas) {
//        return Instance.make(clas, owlClass);
//    }

    public static final Atom owlClass = Atom.the("Class");
    static final Atom parentOf = Atom.the("parentOf");
    static final Atom type = Atom.the("type");
    static final Atom subClassOf = Atom.the("subClassOf");
    static final Atom subPropertyOf = Atom.the("subPropertyOf");
    static final Atom equivalentClass = Atom.the("equivalentClass");
    static final Atom equivalentProperty = Atom.the("equivalentProperty");
    static final Atom inverseOf = Atom.the("inverseOf");
    static final Atom disjointWith = Atom.the("disjointWith");
    static final Atom domain = Atom.the("domain");
    static final Atom range = Atom.the("range");
    static final Atom sameAs = Atom.the("sameAs");
    static final Atom dataTypeProperty = Atom.the("DatatypeProperty");

    /**
     * Saves the relation into the database. Both entities must exist if the
     * relation is to be saved. Takes care of updating relation_types as well.
     *
     */
    public static Task input(final NAR nar,
                             final Atom subject,
                             final Atom predicate, final Term object) {

        //http://www.w3.org/TR/owl-ref/

        Compound belief = null;

        if (predicate.equals(parentOf) || predicate.equals(type)
                ||predicate.equals(subClassOf)||predicate.equals(subPropertyOf)) {

            if (object.equals(owlClass)) {
                return null;
            }

            //if (!includeDataType) {
                if (object.equals(dataTypeProperty)) {
                    return null;
                }
            //}

            belief = (Inheritance.make(subject, object));

        }
        else if (predicate.equals(equivalentClass)) {
            belief = Task.taskable(Equivalence.make(subject, object));
        }
        else if (predicate.equals(sameAs)) {
            belief = Task.taskable(Similarity.make(subject, object));
            //belief = (Equivalence.make(subject, object));
        }
        else if (predicate.equals(domain)) {
            // PROPERTY domain CLASS
            //<PROPERTY($subj, $obj) ==> <$subj {-- CLASS>>.

            Term a = $.$("<{(#subj,#obj)} --> [" + subject + "]>");
            Term b = $.$("<{#subj} --> [" +  object + "]>");
            belief = $.impl(a,b);
        }
        else if (predicate.equals(range)) {
            // PROPERTY range CLASS
            //<PROPERTY($subj, $obj) ==> <$obj {-- CLASS>>.

            Term a = $.$("<{(#subj,#obj)} --> [" + subject + "]>");
            Term b = $.$("<{#obj} --> [" +  object + "]>");
            belief = $.impl(a,b);

//            belief = nar.term(
//                    //"<" + subject + "($subj,$obj) ==> <$obj {-- " + object + ">>"
//                    "(" + subject + "($subj,$obj) && <$obj {-- " + object + ">)"
//            );

        }
        else if (predicate.equals(equivalentProperty)) {
            belief = Task.taskable(Equivalence.make(subject, object));
        }
        else if (predicate.equals(inverseOf)) {

            //TODO: PREDSUBJ(#subj, #obj) <=> PREDOBJ(#obj, #subj)
        }
        else if (predicate.equals(disjointWith)) {
            //System.out.println(subject + " " + predicate + " " + object);
            belief = Task.taskable(
                Negation.make(Similarity.make(subject, object))
            );
        }
        else {
            //System.out.println(subject + " " + predicate + " " + object);
            if (subject!=null && object!=null && predicate!=null) {
                belief = Inheritance.make(
                        Product.make(subject, object),
                        predicate
                );
            }
        }

        if (belief!=null) {
            return new FluentTask().term(belief).
                    belief().truth(1f,0.9f)
                    .time(nar.time(),
                    Stamp.ETERNAL //TODO Tense parameter
                    );
        }

        return null;
    }



    // ======== String manipulation methods ========
    /**
     * Format the XML tag. Takes as input the QName of the tag, and formats it
     * to a namespace:tagname format.
     *
     * @param qname the QName for the tag.
     * @return the formatted QName for the tag.
     */
    private String formatTag(QName qname) {
        String prefix = qname.getPrefix();
        String suffix = qname.getLocalPart();

        suffix = suffix.replace("http://dbpedia.org/ontology/", "");

        if (prefix == null || prefix.length() == 0) {
            return suffix;
        } else {
            return prefix + ":" + suffix;
        }
    }

    /**
     * Split up Uppercase Camelcased names (like Java classnames or C++ variable
     * names) into English phrases by splitting wherever there is a transition
     * from lowercase to uppercase.
     *
     * @param name the input camel cased name.
     * @return the "english" name.
     */
    private String getEnglishName(String name) {
        StringBuilder englishNameBuilder = new StringBuilder();
        char[] namechars = name.toCharArray();
        for (int i = 0; i < namechars.length; i++) {
            if (i > 0 && Character.isUpperCase(namechars[i])
                    && Character.isLowerCase(namechars[i - 1])) {
                englishNameBuilder.append(' ');
            }
            englishNameBuilder.append(namechars[i]);
        }
        return englishNameBuilder.toString();
    }

}

