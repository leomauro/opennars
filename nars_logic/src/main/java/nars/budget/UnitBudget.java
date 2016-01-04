/*
 * BudgetValue.java
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
package nars.budget;

import nars.nal.nal7.Tense;
import nars.truth.Truth;
import nars.util.data.Util;

import static nars.nal.UtilityFunctions.and;
import static nars.nal.UtilityFunctions.or;

/**
 * A triple of priority (current), durability (decay), and quality (long-term average).
 *
 * Mutable, unit-scaled (1.0 max) budget value
 *
 */
public class UnitBudget implements Budget {

//    public static final Procedure2<Budget,Budget> average =
//        //(Serializable & Procedure2<Budget, Budget>) Budget::mergeAverage;
//        (Serializable & Procedure2<Budget, Budget>) Budget::mergeAverageLERP;
//    public static final Procedure2<Budget,Budget> plus =
//        (Serializable & Procedure2<Budget, Budget>) Budget::mergePlus;
//    public static final Procedure2<Budget,Budget> max =
//        (Serializable & Procedure2<Budget, Budget>) Budget::mergeMax;
//


    //common instance for a 'deleted budget'.  TODO use a wrapper class to make it unmodifiable
    public static final Budget deleted = new UnitBudget();
    static {  deleted.delete(); }

    //common instance for a 'zero budget'.  TODO use a wrapper class to make it unmodifiable
    public static final Budget zero = new UnitBudget();
    static {  zero.zero();    }

    /**
     * The relative share of time resource to be allocated
     */
    private float priority;

    /**
     * The percent of priority to be kept in a constant period; All priority
     * values "decay" over time, though at different rates. Each item is given a
     * "durability" factor in (0, 1) to specify the percentage of priority level
     * left after each reevaluation
     */
    private float durability;

    /**
     * The overall (context-independent) evaluation
     */
    private float quality;


    /**
     * time at which this budget was last forgotten, for calculating accurate memory decay rates
     */
    protected long lastForgetTime = Tense.TIMELESS;

//    public Budget newDefaultBudget(Memory m, char punctuation, Truth truth) {
//        Budget b = new Budget();
//        m.applyDefaultBudget(b, punctuation, truth);
//        return b;
//    }
//
//    public Budget(final float p, char punctuation, Truth qualityFromTruth) {
//        this(p,
//                punctuation == Symbols.JUDGMENT ? Global.DEFAULT_JUDGMENT_DURABILITY :
//                        (punctuation == Symbols.QUESTION ? Global.DEFAULT_QUESTION_DURABILITY :
//                                (punctuation == Symbols.GOAL ? Global.DEFAULT_GOAL_DURABILITY :
//                                        Global.DEFAULT_QUEST_DURABILITY)),
//                qualityFromTruth);
//    }

    public UnitBudget(float p, float d, Truth qualityFromTruth) {
        this(p, d, qualityFromTruth !=
                null ? BudgetFunctions.truthToQuality(qualityFromTruth) : 1.0f);
    }


    /**
     * Constructor with initialization
     *
     * @param p Initial priority
     * @param d Initial durability
     * @param q Initial quality
     */
    public UnitBudget(float p, float d, float q) {
        setPriority(p);
        setDurability(d);
        setQuality(q);
    }


    /**
     * begins with 0.0f for all components
     */
    public UnitBudget() {
    }

    public UnitBudget(Budget v) {
        this(v, false);
    }

    /**
     * Cloning constructor
     *
     * @param v Budget value to be cloned
     */
    public UnitBudget(Budget v, boolean copyLastForgetTime) {
        this();
        if (v != null) {
            budget(v);
            if (!copyLastForgetTime)
                setLastForgetTime(-1);
        }
    }

    /*
    public static void ensureBetweenZeroAndOne(final float v) {
        String err = null;
        if (Float.isNaN(v)) err = ("value is NaN");
        else if (v > 1.0f)  err = ("value > 1.0: " + v);
        else if (v < 0.0f)  err = ("value < 1.0: " + v);

        if (err!=null)
            throw new RuntimeException(err);
    }*/


//    /**
//     * creates a new Budget instance, should be avoided if possible
//     */
//    final public static Budget budgetIfAboveThreshold(final float budgetThreshold, final float pri, final float dur, final float qua) {
//        if (aveGeoNotLessThan(budgetThreshold, pri, dur, qua))
//            return new Budget(pri, dur, qua);
//        return null;
//    }



    public static float summarySum(Iterable<? extends Budgeted> dd) {
        float f = 0;
        for (Budgeted x : dd)
            f += x.getBudget().summary();
        return f;
    }


    /**
     * Cloning method
     * TODO give this a less amgiuous name to avoid conflict with subclasses that have clone methods
     */
    @Override
    public final Budget clone() {
        return new UnitBudget(this, true);
    }


//    public final Budget accumulateLerp(final float addPriority, final float otherDurability, final float otherQuality) {
//        final float currentPriority = getPriority();
//
//        /** influence factor determining LERP amount of blending durability and quality */
//        final float f = 1 - Math.abs(currentPriority - clamp(addPriority+currentPriority));
//
//        return set(
//                currentPriority + addPriority,
//                lerp(getDurability(), otherDurability, f),
//                lerp(getQuality(), otherQuality, f)
//                //max(getDurability(), otherDurability),
//                //max(getQuality(), otherQuality)
//        );
//    }



    /**
     * Get priority value
     *
     * @return The current priority
     */
    @Override
    public final float getPriority() {
        return priority;
    }

    /**
     * Change priority value
     *
     * @param p The new priority
     * @return whether the operation had any effect
     */
    @Override
    public final void setPriority(float p) {
        if (Budget.isDeleted(p)) {
            throw new RuntimeException("NaN priority");
        }
        priority = Util.clamp(p);
    }

    @Override
    public void deleteBudget() {
        this.priority = Float.NaN;
    }

    /**
     * Get durability value
     *
     * @return The current durability
     */
    @Override
    public final float getDurability() {
        return durability;
    }

    /**
     * Change durability value
     *
     * @param d The new durability
     */
    @Override
    public final void setDurability(float d) {
        durability = Util.clamp(d);
//        if (this.durability < 0.5) {
//            System.err.println("low durability");
//        }
    }



//    /**
//     * returns true if this budget is greater in all quantities than another budget,
//     * used to prevent a merge that would have no consequence
//     * NOT TESTED
//     * @param other
//     * @return
//     */
//    public boolean greaterThan(final BudgetValue other) {
//        return (getPriority() - other.getPriority() > Parameters.BUDGET_THRESHOLD) &&
//                (getDurability()- other.getDurability()> Parameters.BUDGET_THRESHOLD) &&
//                (getQuality() - other.getQuality() > Parameters.BUDGET_THRESHOLD);
//    }

    /**
     * Get quality value
     *
     * @return The current quality
     */
    @Override
    public final float getQuality() {
        return quality;
    }

    /**
     * Change quality value
     *
     * @param q The new quality
     */
    @Override
    public final void setQuality(float q) {
        quality = Util.clamp(q);
    }

//    public float summary(float additionalPriority) {
//        return aveGeo(Math.min(1.0f, priority + additionalPriority), durability, quality);
//    }

    /**
     * Increase quality value by a percentage of the remaining range
     *
     * @param v The increasing percent
     */
    public void orQuality(float v) {
        quality = or(quality, v);
    }

    /**
     * Decrease quality value by a percentage of the remaining range
     *
     * @param v The decreasing percent
     */
    public void andQuality(float v) {
        quality = and(quality, v);
    }

//    /**
//     * Merge one BudgetValue into another
//     *
//     * @param that The other Budget
//     * @return whether the merge had any effect
//     */
//    @Override
//    public void merge(final Prioritized that) {
//        setPriority(mean(getPriority(), that.getPriority()));
//    }




//    /**
//     * applies a merge only if the changes would be significant
//     * (the difference in value equal to or exceeding the budget epsilon parameter)
//     *
//     * @return whether change occurred
//     */
//    public boolean mergeIfChanges(Budget target, float budgetEpsilon) {
//        if (this == target) return false;
//
//        final float p = mean(getPriority(), target.getPriority());
//        final float d = mean(getDurability(), target.getDurability());
//        final float q = mean(getQuality(), target.getQuality());
//
//        return setIfChanges(p, d, q, budgetEpsilon);
//    }



//    /* Whether budget is above threshold, with the involvement of additional priority (saved previously, or boosting)
//     * @param additionalPriority saved credit to contribute to possibly push it over threshold
//     */
//    public boolean aboveThreshold(float additionalPriority) {
//        return (summary(additionalPriority) >= Global.BUDGET_THRESHOLD);
//    }


//    /**
//     * Whether budget is above threshold, with the involvement of additional priority (saved previously, or boosting)
//     * @param additionalPriority
//     * @return NaN if neither aboveThreshold, nor aboveThreshold with additional priority; 0 if no additional priority necessary to make above threshold, > 0 if that amount of the additional priority was "spent" to cause it to go above threshold
//     */
//    public float aboveThreshold(float additionalPriority) {
//        float s = summary();
//        if (s >= Parameters.BUDGET_THRESHOLD)
//            return 0;
//        if (summary(additionalPriority) >= Parameters.BUDGET_EPSILON) {
//            //calculate how much was necessary
//
//            float dT = Parameters.BUDGET_THRESHOLD - s; //difference between how much needed
//
//            //TODO solve for additional:
//            //  newSummary - s = dT
//            //  ((priority+additional)*(duration)*(quality))^(1/3) - s = dT;
//
//            float used = 0;
//        }
//        return Float.NaN;
//    }



    public boolean equals(Object that) {
        throw new RuntimeException("N/A");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("N/A");
        //this will be relatively slow if used in a hash collection
        //TODO if hashes are needed, it should use a similar hashing method as Truth values does
        //return Objects.hash(getPriority(), getDurability(), getQuality());
    }


    /**
     * Fully display the BudgetValue
     *
     * @return String representation of the value
     */
    @Override
    public String toString() {
        return getBudgetString();
    }



    /**
     * linear interpolate the priority value to another value
     * https://en.wikipedia.org/wiki/Linear_interpolation
     */
    /*public void lerpPriority(final float targetValue, final float momentum) {
        if (momentum == 1.0) 
            return;
        else if (momentum == 0) 
            setPriority(targetValue);
        else
            setPriority( (getPriority() * momentum) + ((1f - momentum) * targetValue) );
    }*/



    @Override
    public final long setLastForgetTime(long currentTime) {

        long period = lastForgetTime == Tense.TIMELESS ? 0 : currentTime - lastForgetTime;

        lastForgetTime = currentTime;
        return period;
    }

//    public Budget budget(final float p, final float d, final float q) {
//        setPriority(p);
//        setDurability(d);
//        setQuality(q);
//        return this;
//    }

    @Override
    public long getLastForgetTime() {
        return lastForgetTime;
    }

//    /**
//     * fast version which avoids bounds checking, safe to use if getting values from an existing Budget instance
//     */
//    protected final AbstractBudget budgetDirect(float p, float d, float q) {
//
//        if (isDeleted(p)) {
//            throw new RuntimeException("source budget invalid");
//        }
//
//        priority = p;
//        durability = d;
//        quality = q;
//        return this;
//    }






//    @Override
//    public float receive(float amount) {
//        float maxReceivable = 1.0f - getPriority();
//
//        float received = Math.min(amount, maxReceivable);
//        addPriority(received);
//
//        return amount - received;
//    }

//    /**
//     * modifies the budget if any of the components are signifiantly different
//     * returns whether the budget was changed
//     */
//    protected boolean setIfChanges(final float p, final float d, final float q, float budgetEpsilon) {
//        float dp = abs(getPriority() - p);
//        float dd = abs(getDurability() - d);
//        float dq = abs(getQuality() - q);
//
//        if (dp < budgetEpsilon && dd < budgetEpsilon && dq < budgetEpsilon)
//            return false;
//
//        set(p, d, q);
//        return true;
//    }

    @Override
    public void mulPriority(float factor) {
        setPriority(getPriority() * factor);
    }

    public void mulDurability(float factor) {
        setDurability(getDurability() * factor);
    }




//    public static void requireNotDeleted(float pri) {
//        if (isDeleted(pri)) {
//            throw new RuntimeException("NaN priority");
//        }
//
//    }
//
//    public static void requireNotDeleted(Task q) {
//        requireNotDeleted(q.getPriority());
//    }

}
