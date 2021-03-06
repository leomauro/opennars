package nars.term;

import nars.Narsese;
import nars.Op;
import nars.nal.nal1.Negation;
import nars.term.transform.Substitution;
import nars.util.data.Util;
import nars.util.utf8.Utf8;

import java.util.Map;

public class Atom extends ImmutableAtom  {

    final static byte[] NullName = new byte[0];
    public static final Term Null = new MutableAtomic(NullName) {
        @Override
        public String toString() {
            return "NULL";
        }

        @Override
        public boolean hasVar() {
            return false;
        }

        @Override
        public int vars() {
            return 0;
        }

        @Override
        public boolean hasVarIndep() {
            return false;
        }

        @Override
        public boolean hasVarDep() {
            return false;
        }

        @Override
        public boolean hasVarQuery() {
            return false;
        }

        @Override
        public Term substituted(Map<Term, Term> subs) {
            return null;
        }

        @Override
        public final Term substituted(Substitution s) {
            return null;
        }

        @Override
        public int complexity() {
            return 0;
        }

        @Override
        public int varIndep() {
            return 0;
        }

        @Override
        public int varDep() {
            return 0;
        }

        @Override
        public int varQuery() {
            return 0;
        }

        @Override
        public Op op() {
            return Op.NONE;
        }

        @Override
        public int structure() {
            return 0;
        }


    };

    final static Atom[] digits = new Atom[10];
    //private static final Map<String,Atom> atoms = Global.newHashMap();


    public static int hash(byte[] id, int ordinal) {
        return Util.ELFHashNonZero(id, Util.PRIME1 * (1+ordinal));
        //return Util.WildPlasserHashNonZero(id, (1+ordinal));
    }

    public Atom(byte[] n) {
        super(n, Atom.hash(n, Op.ATOM.ordinal() ));
    }

    public Atom(String n) {
        this(Utf8.toUtf8(n));
    }



//    /**
//     * Constructor with a given name
//     *
//     * @param id A String as the name of the Term
//     */
//    public Atom(final String id) {
//        super(id);
//    }
//
//    public Atom(final byte[] id) {
//        super(id);
//    }

    //    /**
//     * Default constructor that build an internal Term
//     */
//    @Deprecated protected Atom() {
//    }

    /** Creates a quote-escaped term from a string. Useful for an atomic term that is meant to contain a message as its name */
    public static Atom quote(String t) {
        return Atom.the('"' + t + '"');
    }

    /** determines if the string is invalid as an unquoted term according to the characters present */
    public static boolean quoteNecessary(CharSequence t) {
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
//            if (Character.isWhitespace(c)) return true;
            if (!Narsese.isValidAtomChar(c))
                return true;
//            if ((!Character.isDigit(c)) && (!Character.isAlphabetic(c))) return true;
        }
        return false;
    }

//    /** interns the atomic term given a name, storing it in the static symbol table */
//    public final static Atom theCached(final String name) {
//        return atoms.computeIfAbsent(name, AtomInterner);
//    }

    public final static Atom the(final String name, boolean quoteIfNecessary) {
        if (quoteIfNecessary && quoteNecessary(name))
            return quote(name);

        return the(name);
    }

    public final static Term the(Term x) {
        return x;
    }

    public final static Atom the(Number o) {

        if (o instanceof Byte) return the(o.intValue());
        if (o instanceof Short) return the(o.intValue());
        if (o instanceof Integer) return the(o.intValue());

        if (o instanceof Long) return the(o.toString());

        if ((o instanceof Float) || (o instanceof Double)) return the(o.floatValue());

        return the(o.toString(), true);
    }

    /** gets the atomic term of an integer */
    public final static Atom the(final int i) {
        return the(i, 10);
    }

    /** gets the atomic term of an integer, with specific radix (up to 36) */
    public final static Atom the(final int i, int radix) {
        //fast lookup for single digits
        if ((i >= 0) && (i <= 9)) {
            Atom a = digits[i];
            if (a == null)
                a = digits[i] = the(Integer.toString(i, radix));
            return a;
        }
        return the(Integer.toString(i, radix));
    }

    public final static Atom the(final float v) {
        if (Util.equal( (float)Math.floor(v), v, Float.MIN_VALUE*2 )) {
            //close enough to be an int, so it doesnt need to be quoted
            return the((int)v);
        }
        return the(Float.toString(v));
    }

    /** gets the atomic term given a name */
    public final static Atom the(final String name) {
//        int olen = name.length();
//        switch (olen) {
//            case 0:
//                throw new RuntimeException("empty atom name: " + name);
//
////            //re-use short term names
////            case 1:
////            case 2:
////                return theCached(name);
//
//            default:
//                if (olen > Short.MAX_VALUE/2)
//                    throw new RuntimeException("atom name too long");

                return Atom.the(Utf8.toUtf8(name));
      //  }
    }

    public final static Atom the(byte[] id) {
        return new Atom(id);
    }


    public final static Atom the(byte c) {
        return Atom.the(new byte[] { c });
    }

    public static String unquote(final Term s) {
        return toUnquoted(s.toString());
    }

    public static String toUnquoted(String x) {
        if (x.startsWith("\"") && x.endsWith("\"")) {
            return x.substring(1, x.length() - 1);
        }
        return x;
    }

    /*
    // similar to String.intern()
    public final static Atom the(final String name) {
        if (name.length() <= 2)
            return theCached(name);
        return new Atom(name);
    }
    */

    public static Term the(final Object o) {

        if (o instanceof Term) return ((Term)o);
        else if (o instanceof String)
            return the((String)o);
        else if (o instanceof Number)
            return the((Number)o);
        return null;
    }

    public static Negation notThe(String untrue) {
        return (Negation) Negation.make(Atom.the(untrue));
    }

    @Override
    public final Op op() {
        return Op.ATOM;
    }

    @Override
    public final int structure() {
        return 1 << Op.ATOM.ordinal();
    }

    @Override
    public final Term substituted(Map<Term, Term> subs) {
        return this;
    }

    @Override
    public final Term substituted(Substitution s) {
        return this;
    }

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override public final boolean hasVar() { return false;    }

    @Override public final int vars() { return 0;    }

    @Override public final boolean hasVarIndep() { return false;    }

    @Override public final boolean hasVarDep() { return false;    }

    @Override public final boolean hasVarQuery() { return false;    }

    public final String toStringUnquoted() {
        return toUnquoted(toString());
    }

    @Override
    public final int complexity() {
        return 1;
    }

    @Override
    public final int varIndep() {
        return 0;
    }

    @Override
    public final int varDep() {
        return 0;
    }

    @Override
    public final int varQuery() {
        return 0;
    }


    /** performs a thorough check of the validity of a term (by cloneDeep it) to see if it's valid */
//    public static boolean valid(final Term content) {
//
//        return true;
////        try {
////            Term cloned = content.cloneDeep();
////            return cloned!=null;
////        }
////        catch (Throwable e) {
////            if (Global.DEBUG && Global.DEBUG_INVALID_SENTENCES) {
////                System.err.println("INVALID TERM: " + content);
////                e.printStackTrace();
////            }
////            return false;
////        }
////
//    }
}


//    @Override
//    public boolean hasVar(Op type) {
//        return false;
//    }

//    /**
//     * Equal terms have identical name, though not necessarily the same
//     * reference.
//     *
//     * @return Whether the two Terms are equal
//     * @param that The Term to be compared with the current Term
//     */
//    @Override
//    public boolean equals(final Object that) {
//        if (this == that) return true;
//        if (!(that instanceof Atom)) return false;
//        final Atom t = (Atom)that;
//        return equalID(t);
//
////        if (equalsType(t) && equalsName(t)) {
////            t.name = name; //share
////            return true;
////        }
////        return false;
//    }


//    public final void recurseSubtermsContainingVariables(final TermVisitor v) {
//        recurseSubtermsContainingVariables(v, null);
//    }

//    /**
//     * Recursively check if a compound contains a term
//     *
//     * @param target The term to be searched
//     * @return Whether the two have the same content
//     */
//    @Override public final void recurseSubtermsContainingVariables(final TermVisitor v, Term parent) {
//        //TODO move to Variable subclass and leave this empty here
//        if (hasVar())
//            v.visit(this, parent);
//    }

//    final public byte byt(int n) {
//        return data[n];
//    }

//    final byte byt0() {
//        return data[0];
//    }

//    @Override
//    public final boolean equalsOrContainsTermRecursively(final Term target) {
//        return equals(target);
//   }

