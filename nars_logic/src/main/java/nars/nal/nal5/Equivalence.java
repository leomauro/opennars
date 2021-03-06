/*
 * Equivalence.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal5;

import nars.Op;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Tense;
import nars.term.Statement;
import nars.term.Term;
import nars.term.TermSet;
import nars.term.TermVector;

/**
 * A Statement about an Equivalence relation.
 */
public class Equivalence extends Statement {

    @Deprecated private final int temporalOrder; //TODO use subclasses

    /**
     * Constructor with partial values, called by make
     *
     */
    private Equivalence(Term subject, Term predicate, int order) {
        super( (order!=Tense.ORDER_FORWARD) ?
            new TermSet() : new TermVector()
        );

        if ((order == Tense.ORDER_BACKWARD) ||
                (order == Tense.ORDER_INVALID)) {
            throw new RuntimeException("Invalid temporal order=" + order + "; args=" + subject + " , " + predicate);
        }

        temporalOrder = order;
        
        init(subject, predicate);
    }


    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate, int temporalOrder) {
        if (subject.equals(predicate))
            return subject;                
        return make(subject, predicate, temporalOrder);        
    }

    public static Term makeTerm(Term subject, Term predicate) {
        return makeTerm(subject, predicate, Tense.ORDER_NONE);
    }


    /**
     * Try to make a new compound from two term. Called by the logic
     * rules.
     *
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Term make(Term subject, Term predicate) {  // to be extended to check if subject is Conjunction
        return make(subject, predicate, Tense.ORDER_NONE);
    }

    public static Term make(Term subject, Term predicate, int temporalOrder) {  // to be extended to check if subject is Conjunction

        if ((subject instanceof Implication) || (subject instanceof Equivalence)
                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
            return null;
        }

        if (invalidStatement(subject, predicate)) {
            return null;
        }

        //swap terms for commutivity, or to reverse a backward order
        final boolean reverse;
        if (temporalOrder == Tense.ORDER_BACKWARD) {
            temporalOrder = Tense.ORDER_FORWARD;
            reverse = true;
        }
        else if (temporalOrder != Tense.ORDER_FORWARD)
            reverse = subject.compareTo(predicate) > 0;
        else
            reverse = false;

        if (reverse) {
            //swap
            Term interm = subject;
            subject = predicate;
            predicate = interm;
        }

        return new Equivalence(subject, predicate, temporalOrder);
    }

    /**
     * Get the operate of the term.
     *
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        switch (temporalOrder) {
            case Tense.ORDER_FORWARD:
                return Op.EQUIVALENCE_AFTER;
            case Tense.ORDER_CONCURRENT:
                return Op.EQUIVALENCE_WHEN;
        }
        return Op.EQUIVALENCE;
    }

    /**
     * Check if the compound is commutative.
     *
     * @return true for commutative
     */
    @Override
    public final boolean isCommutative() {
        return (temporalOrder != Tense.ORDER_FORWARD);
    }

    @Override
    public final int getTemporalOrder() {
        return temporalOrder;
    }
}
