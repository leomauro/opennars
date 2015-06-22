package nars.rover.run;

import automenta.vivisect.Video;
import nars.Global;
import nars.NAR;
import nars.gui.NARSwing;
import nars.model.impl.Default;
import nars.rover.RoverEngine;
import nars.rover.robot.RoverModel;

import javax.swing.*;

/**
 * Created by me on 6/20/15.
 */
public class SomeRovers {

    static {
        Video.themeInvert();
    }

    public static void main(String[] args) {
        Global.DEBUG = false;
        Global.EXIT_ON_EXCEPTION = true;


        float fps = 60;
        boolean cpanels = false;

        final RoverEngine game = new RoverEngine();


        int rovers = 3;

        for (int i = 0; i < rovers; i++)  {

            NAR nar;
            nar = new NAR(new Default().simulationTime().
                    setActiveConcepts(768).
                    setNovelTaskBagSize(48).setTermLinkBagSize(24));
            nar.param.inputsMaxPerCycle.set(100);
            nar.param.conceptsFiredPerCycle.set(128);
            nar.setCyclesPerFrame(3);
            nar.param.shortTermMemoryHistory.set(3);
            nar.param.temporalRelationsMax.set(3);

            (nar.param).outputVolume.set(3);
            (nar.param).duration.set(7);
            //nar.param.budgetThreshold.set(0.02);
            //nar.param.confidenceThreshold.set(0.02);
            (nar.param).conceptForgetDurations.set(15f);
            (nar.param).taskLinkForgetDurations.set(10f);
            (nar.param).termLinkForgetDurations.set(10f);
            (nar.param).novelTaskForgetDurations.set(10f);



            game.add(new RoverModel("r" + i, nar, game));

            if (cpanels) {
                SwingUtilities.invokeLater(() -> {
                    new NARSwing(nar, false);
                });
            }
        }

        game.run(fps);
    }
}
