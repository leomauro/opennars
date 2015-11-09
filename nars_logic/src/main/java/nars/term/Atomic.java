package nars.term;

import nars.Op;
import nars.term.transform.TermVisitor;
import nars.util.data.Util;
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Base class for terms which contain no subterms
 */
public abstract class Atomic implements Term, Byted, Externalizable {

    protected byte[] data;

    transient int hash;

    Atomic(String id) {
        this(Utf8.toUtf8(id));
    }

    protected Atomic(byte[] id) {
        setBytes(id);
    }

    Atomic() {

    }


    @Override
    public abstract Op op();

    @Override
    public abstract int structure();

    @Override
    public void append(final Appendable w, final boolean pretty) throws IOException {
        Utf8.fromUtf8ToAppendable(bytes(), w);
    }

    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        StringBuilder sb = new StringBuilder();
        Utf8.fromUtf8ToStringBuilder(bytes(), sb);
        return sb;
    }

    @Override
    public final boolean isNormalized() {
        return true;
    }

    @Override
    public String toString() {
        return Utf8.fromUtf8toString(bytes());
    }

    @Override
    public final boolean equals(final Object x) {
        if (this == x) return true;
        Term ax = (Term)x;
        return (ax.op() == op()) && Byted.equals(this, (Atomic)ax);
    }


    @Override
    public final int hashCode() {
        final int h = this.hash;
        if (h == 0) {
            throw new RuntimeException("should have hashed");
//            rehash();
//            return this.hash;
        }
        return h;
    }

    /**
     * @param that The Term to be compared with the current Term
     */
    @Override
    public final int compareTo(final Object that) {
        if (that==this) return 0;

        Term t = (Term)that;
        int d = op().compareTo(t.op());
        if (d!=0) return d;

        //if the op is the same, it will be a subclass of atom
        //which should have an ordering determined by its byte[]
        return Byted.compare(this, (Atomic)that);
    }

    @Override
    public final int getByteLen() {
        return bytes().length;
    }


    /**
     * Atoms are singular, so it is useless to clone them
     */
    @Override
    public final Term clone() {
        return this;
    }

    @Override
    public final Term cloneDeep() {
        return this;
    }

    @Override
    public final void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);
    }

    @Override
    public final String toString(boolean pretty) {
        return toString();
    }

    @Override public abstract boolean hasVar();

    @Override public abstract int vars();

    @Override public abstract boolean hasVarIndep();

    @Override public abstract boolean hasVarDep();

    @Override public abstract boolean hasVarQuery();

    @Override
    public abstract int complexity();

    @Override
    public final int length() {
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
        //return 0;
    }

    @Override public final int volume() { return 1; }

    public final boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    public final void rehash() {
        /** do nothing */
    }

    @Override final public byte[] bytes() {
        return data;
    }

    @Override
    public final void setBytes(final byte[] id) {
        if (id!=this.data) {
            this.data = id;
            int newHash = Util.ELFHashNonZero(id, op().ordinal());
            if (newHash  == 0) newHash  = 1;
            this.hash = newHash;
        }
    }

    @Override public final boolean impossibleToMatch(int possibleSubtermStructure) {
        /*
        for atomic terms, there will be only one
        bit set in this (for the operator). if it does not equal
        the parameter, then the structure can not match.
        */
        return possibleSubtermStructure != 0 &&
            structure()!=possibleSubtermStructure;
    }

    /** atomic terms contain nothing */
    @Override public final boolean containsTerm(Term target) {
        return false;
    }

    /** atomic terms contain nothing */
    @Override public final boolean containsTermRecursively(Term target) {
        return false;
    }

    @Override
    public abstract int varIndep();

    @Override
    public abstract int varDep();

    @Override
    public abstract int varQuery();


    @Override
    public final Atomic normalized() {
        return this;
    }

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException {
        byte[] name = bytes();
        //out.writeByte((byte)op().ordinal());
        out.writeShort(name.length);
        out.write(bytes());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int nameLen = in.readShort();
        byte[] name = new byte[nameLen];

        in.read(name);

        setBytes(name);
    }
}
