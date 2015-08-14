package nars.meta;

import nars.Global;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Interval;
import nars.process.ConceptProcess;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.*;
import nars.truth.Truth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by me on 7/31/15.
 */
public class PostCondition //since there can be multiple tasks derived per rule
{

    private final Term term;
    private final Term[] modifiers;

    public final TruthFunction truth;
    public final DesireFunction desire;
    boolean single_premise = false;
    boolean negation = false;

    /* high-speed adaptive RETE-like precondition filtering:

            sort all unique preconditions by a hueristic value:

                # of applications (across all appearances in rules) divided by an estimated computational cost,
                since some preconditions are less expensive to test than others.
                this value represents the discriminatory power to computational cost ratio
                of the precondition, and the higher the value, the earlier this condition
                should be tested to eliminate the most possibilities as soon as possible.

            the remaining preconditions to test in each iteration only need to be those
            which will discriminate the remaining eligible rules, and eliminating
            these sooner will require less necessary precondition tests.

            if no tests remain, the process terminates without any derivation.  this
            is the ideal result becaues a brute-force approach (as originally implemented here)
            requires the slower traversal of all rules, regardless.

     */

    public PostCondition(Term term, Term... modifiers) {
        this.term = term;

        @Deprecated List<Term> otherModifiers = new ArrayList();
        TruthFunction truthFunc = null;
        DesireFunction desireFunc = null;

        for (final Term m : modifiers) {
            if (!(m instanceof Inheritance)) {
                System.err.println("Unknown postcondition format: " + m );
                continue;
            }

            Inheritance i = (Inheritance) m;
            Term type = i.getPredicate();
            Term which = i.getSubject();
            String swhich = which.toString();

            if (swhich.equals("Negation")) {
                negation = true;
            }

            //TODO compare by Atom, and avoid generating switch String (UTF8 will be slightly more efficient than UTF16)
            if (swhich.equals("Negation") || swhich.equals("Conversion") || swhich.equals("Contraposition") || swhich.equals("Identity")) {
                single_premise = true;
            }

            if (type instanceof Atom) {
                final String typeStr = type.toString();
                switch (typeStr) {

                    case "Truth":
                        TruthFunction tm = TruthFunction.get(which);
                        if (tm != null) {
                            if (truthFunc!=null) //only allow one
                                throw new RuntimeException("truthFunc " + truthFunc + " already specified; attempting to set to " + tm);
                            truthFunc = tm;
                        } else {
                            throw new RuntimeException("unknown TruthFunction " + which);
                        }
                        break;

                    case "Desire":
                        DesireFunction dm = DesireFunction.get(which);
                        if (dm != null) {
                            if (desireFunc!=null) //only allow one
                                throw new RuntimeException("desireFunc " + desireFunc + " already specified; attempting to set to " + dm);
                            desireFunc = dm;
                        } else {
                            throw new RuntimeException("unknown TruthFunction " + which);
                        }
                        break;

                    case "Order":
                        break;

                    default:
                        System.err.println("Unknown postcondition: " + type + ":" + which );
                        otherModifiers.add(m);
                        break;
                }
            }


        }


        this.truth = truthFunc;
        this.desire = desireFunc;

        this.modifiers = otherModifiers.toArray(new Term[otherModifiers.size()]);
    }

    public boolean apply(Term[] preconditions, Task task, Sentence belief, Term beliefterm, NAL nal) {
        if (task == null)
            throw new RuntimeException("null task");

        final Truth T = task.truth;
        final Truth B = belief == null ? null : belief.truth;

        Truth truth = null;
        Truth desire = null;
        boolean single_premise = this.single_premise;

        if (negation && task.truth.getFrequency() >= 0.5) { //its negation, it needs this additional information to be useful
            return false;
        }

        if (!single_premise && belief == null) {  //at this point single_premise is already decided, if its double premise and belief is null, we can stop already here
            return false;
        }

        if (this.truth != null) {
            truth = this.truth.get(T, B);
        }

        if (truth == null && task.isJudgment()) {
            System.err.println("truth rule not specified, deriving nothing: \n" + this);
            return false; //not specified!!
        }

        if (desire == null && task.isGoal()) {
            System.out.println("desire rule not specified, deriving nothing: \n" + this);
            return false; //not specified!!
        }

        //now match the rule with the task term <- should probably happen earlier ^^
        final Map<Term, Term> assign = Global.newHashMap();
        final Map<Term, Term> precondsubs = Global.newHashMap();
        final Map<Term, Term> waste = Global.newHashMap();

        Term derive = term; //first entry is term
        int occurence_shift = 0;

        //TODO checking the precondition again for every postcondition misses the point, but is easily fixable (needs to be moved down to Rule)
        if (single_premise) { //only match precondition pattern with task //SINGLE_PREMISE MAY BE OBSOLETE; BUT DONT DELETE YET

            //match first rule pattern with task
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                return false;

            //match second rule pattern with beliefterm (belief may be null because belief might not exist, but termlink matters here)
            if (!negation && !Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[1], beliefterm, assign, waste, nal.memory.random))
                return false;

            //now we have to apply this to the derive term
            derive = derive.substituted(assign);

        } else {

            //match first rule pattern with task
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[0], task.getTerm(), assign, waste, nal.memory.random))
                return false;

            //match second rule pattern with belief
            if (!Variables.findSubstitute(Symbols.VAR_PATTERN, preconditions[1], belief.getTerm(), assign, waste, nal.memory.random))
                return false;

            //also check if the preconditions are met
            for (int i = 2; i < preconditions.length; i++) {
                Inheritance predicate = (Inheritance) preconditions[i];
                Term predicate_name = predicate.getPredicate();
                Term[] args = ((Product) (((SetExt) predicate.getSubject()).term(0))).terms();
                //ok apply substitution to both elements in args

                final Term arg1 = args[0].substituted(assign);

                final Term arg2;
                //arg2 is optional
                if (args.length > 1 && args[1]!=null)
                    arg2 = args[1].substituted(assign);
                else
                    arg2 = null;

                final String predicateNameStr = predicate_name.toString().replace("^","");

                switch (predicateNameStr) {
                    case "not_equal":
                        if (arg1.equals(arg2))
                            return false; //not_equal
                        break;
                    case "event":
                        if(arg1.equals(task.getTerm()) && task.getOccurrenceTime() == Stamp.ETERNAL) {
                            return false;
                        }
                        if(arg2 == null) {
                            return false;
                        }
                        if(arg2.equals(beliefterm) && (belief == null || belief.getOccurrenceTime() == Stamp.ETERNAL)) {
                            return false;
                        }
                        break;
                    case "negative":
                        if(arg1.equals(task.getTerm()) && task.truth.getFrequency() >= 0.5) {
                            return false;
                        }
                        else
                        if(arg2.equals(beliefterm) && (belief == null || belief.truth.getFrequency() >= 0.5)) {
                            return false;
                        }
                        single_premise = true;
                        break;
                    case "no_common_subterm":

                        //TODO this will only compare the first level of subterms
                        //for recursive, we will need a stronger test
                        //but we should decide if recursive is actually necessary
                        //and create alternate noCommonSubterm and noCommonRecursiveSubterm
                        //preconditions to be entirely clear

                        if ((arg1 instanceof Compound) && (arg2 instanceof Compound))
                            if (Terms.shareAnySubTerms((Compound)arg1, (Compound)arg2))
                                return false;
                        break;
                    case "measure_time":
                        {
                            if(belief == null) {
                                return false;
                            }
                            if(task.isEternal() || belief.isEternal()) {
                                return false;
                            }

                            long time1 = 0, time2 = 0;
                            if (arg1.equals(task.getTerm())) {
                                time1 = task.getOccurrenceTime();
                                time2 = belief.getOccurrenceTime();
                            }
                            if (arg1.equals(belief.getTerm())) {
                                time1 = task.getOccurrenceTime();
                                time2 = belief.getOccurrenceTime();
                            }
                            long time = time1 - time2;
                            if (time < 0) {
                                return false;
                            }
                            assign.put(args[2], CyclesInterval.make(time, nal.memory)); // I:=+8 for example
                        }
                        break;
                    case "after":
                        {
                            if(belief == null) {
                                return false;
                            }
                            if(task.isEternal() || belief.isEternal()) {
                                return false;
                            }
                            long time1 = 0, time2 = 0;
                            if (arg1.equals(task.getTerm())) {
                                if(!task.after(belief,nal.memory.getParam().duration.get()))
                                    return false;
                            }
                            if (arg1.equals(belief.getTerm())) {
                                if(!belief.after(task,nal.memory.getParam().duration.get()))
                                    return false;
                            }
                        }
                        break;
                    case "not_implication_or_equivalence":
                    {
                        if (arg1.equals(task.getTerm())) {
                            if(task.getTerm() instanceof Implication || task.getTerm() instanceof Equivalence) {
                                return false;
                            }
                        }
                        if (belief != null && arg1.equals(belief.getTerm())) {
                            if(belief.getTerm() instanceof Implication || belief.getTerm() instanceof Equivalence) {
                                return false;
                            }
                        }
                    }
                    break;
                    case "concurrent":
                        {
                            if(belief == null) {
                                return false;
                            }
                            if(task.isEternal() || belief.isEternal()) {
                                return false;
                            }
                            long time1 = 0, time2 = 0;
                            if (arg1.equals(task.getTerm())) {
                                if(!(!task.after(belief,nal.memory.getParam().duration.get()) && !belief.after(task,nal.memory.getParam().duration.get())))
                                    return false;
                            }
                            if (arg1.equals(belief.getTerm())) {
                                if(!(!task.after(belief,nal.memory.getParam().duration.get()) && !belief.after(task,nal.memory.getParam().duration.get())))
                                    return false;
                            }
                        }
                        break;
                    case "substitute":
                        {
                            Term M = args[1]; //this one got substituted, but with what?
                            Term with = assign.get(M); //with what assign assigned it to (the match between the rule and the premises)
                            //args[0] now encodes a variable which we want to replace with what M was assigned to
                            //(relevant for variable elimination rules)
                            precondsubs.put(args[0],with);
                        }
                        break;
                    case "shift_occurrence_forward":
                        {
                            occurence_shift += timeOffsetForward(arg1,nal);
                            occurence_shift += timeOffsetForward(arg2,nal);
                        }
                        break;
                    case "shift_occurrence_backward":
                        {
                            occurence_shift -= timeOffsetForward(arg1,nal);
                            occurence_shift -= timeOffsetForward(arg2,nal);
                        }
                        break;
                }
            }

            //now we have to apply this to the derive term
            derive = derive.substituted(assign); //at first M -> #1 for example (rule match), then #1 -> test (var elimination)
            if(!precondsubs.isEmpty()) {
                derive = derive.substituted(precondsubs);
            }
        }

        if (derive!=null && derive.toString().contains("%")) {
            System.err.println("Reactor leak - Pattern variable detected in output");
        }


        //TODO also allow substituted evaluation on output side (used by 2 rules I think)

        Budget budget = BudgetFunctions.compoundForward(truth, derive, nal);


        //TODO on occurenceDerive, for example consider ((&/,<a --> b>,+8) =/> (c --> k)), (a --> b) |- (c --> k)
        // or ((a --> b) =/> (c --> k)), (a --> b) |- (c --> k) where the order makes a difference,
        //a difference in occuring, not a difference in matching
        //CALCULATE OCCURENCE TIME HERE AND SET DERIVED TASK OCCURENCE TIME ACCORDINGLY!

        boolean allowOverlap = false; //to be refined

        if (derive instanceof Compound) {
            if (!single_premise) {

                TaskSeed<Compound> t = nal.newDoublePremise(task, belief, allowOverlap);
                if (t != null) {
                    t.term((Compound) derive).punctuation(task.punctuation)
                            .truth(truth).budget(budget);

                    if(t.getOccurrenceTime() != Stamp.ETERNAL) {
                        t.occurr(t.getOccurrenceTime()+occurence_shift);
                    }

                    //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

                    if (t != null)
                        nal.deriveDouble(t);
                }

            } else {

                TaskSeed<Compound> t = nal.newSinglePremise(task, allowOverlap);
                if (t != null) {
                    t.term((Compound) derive).punctuation(task.punctuation)
                            .truth(truth).budget(budget);

                    if(t.getOccurrenceTime() != Stamp.ETERNAL) {
                        t.occurr(t.getOccurrenceTime()+occurence_shift);
                    }

                    //TODO ANTICIPATE IF IN FUTURE AND Event:Anticipate is given

                    if (t != null)
                        nal.deriveSingle(t);
                }
            }
        }
        return true;
    }

    public long timeOffsetForward(Term arg, NAL nal) {
        if(arg instanceof Interval) {
            return ((Interval)arg).cycles(nal.memory.param.duration);
        }
        int duration = nal.memory.param.duration.get();
        if(arg.toString().equals("\"=/>\"")) {
            return duration;
        }
        if(arg.toString().equals("\"=\\>\"")) {
            return -duration;
        }
        return 0;
    }

    @Override
    public String toString() {
        return term + "(" + Arrays.toString(modifiers) + ")";
    }

}
