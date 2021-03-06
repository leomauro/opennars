package nars;

import nars.util.data.Util;
import net.openhft.affinity.AffinityLock;

import java.util.logging.Logger;

/**
 * self managed set of processes which run a NAR
 * as a loop at a certain frequency.
 */
public class NARLoop implements Runnable {

    final static Logger logger = Logger.getLogger(NARLoop.class.getSimpleName());

    public final NAR nar;

    private final Thread thread;

    /**
     * sleep mode delay time
     */
    static final long sleepTimeMS = 250;
    private final boolean cpuCoreReserve;


    public volatile int cyclesPerFrame = 1;
    volatile int periodMS = 1000;
    private volatile boolean stopped = false;
    //private boolean running;

    //TODO make this into a SimpleIntegerProperty also


    @Override
    public String toString() {
        return nar.toString() + ":loop@" + getFrequency() + "Hz";
    }

    //in Hz / fps
    public double getFrequency() {
        return 1000.0 / periodMS;
    }

    public int getPeriodMS() {
        return periodMS;
    }


    public NARLoop(NAR n, int initialPeriod) {
        this(n, initialPeriod, false);
    }

    /**
     *
     * @param n
     * @param initialPeriod
     * @param reserveCPUCore whether to acquire a thread affinity lock on a CPU core, which will improve performance if a dedicated core can be assigned
     */
    public NARLoop(NAR n, int initialPeriod, boolean reserveCPUCore) {


        this.nar = n;
        this.cpuCoreReserve = reserveCPUCore;

        n.the("loop", this);

        setPeriodMS(initialPeriod);

        this.thread = new Thread(this);
        thread.start();
        logger.info(() -> (this + " started thread " + thread + " with priority=" + thread.getPriority()) );

    }


    public final boolean setPeriodMS(final int period) {
        int prevPeriod = getPeriodMS();

        if (prevPeriod == period) return false;

        this.periodMS = period;

        //thread priority control
        if (thread != null) {
            int pri = thread.getPriority();
            final int fullThrottlePri = Thread.MIN_PRIORITY;

            final int targetPri;
            if (periodMS == 0) {
                targetPri = fullThrottlePri;
            } else {
                targetPri = Thread.NORM_PRIORITY;
            }

            if (targetPri != pri)
                thread.setPriority(fullThrottlePri);

            thread.interrupt();

        }


        return true;
    }

    public void stop() {
        logger.info(() -> (this + " stop requested") );
        stopped = true;
    }

    public void waitForTermination() throws InterruptedException {
        stop();
        thread.join();
    }

    @Override
    final public void run() {




        AffinityLock al;
        if (cpuCoreReserve) {
            al = AffinityLock.acquireLock();
            if (al.isAllocated() && al.isBound()) {
                logger.info(thread + " running exclusively on CPU " +  al.cpuId());
            }
        }
        else {
            al = null;
        }

        try {
            final NAR nar = this.nar;

            try {
                while (!stopped) {

                    final int periodMS = this.periodMS;

                    if (periodMS < 0) {
                        //        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            //e.printStackTrace();
//        }

                        Util.pause(sleepTimeMS);
                        continue;
                    }


                    final long start = System.currentTimeMillis();

                    if (!nar.running.get()) {
                        nar.frame(cyclesPerFrame);
                    } else {
                        //wait until nar is free
                    }


                    final long frameTimeMS = System.currentTimeMillis() - start;


                    throttle(periodMS, frameTimeMS);

                }
            } catch (Exception e) {
                nar.memory.eventError.emit(e);
                stopped = true;
            }
        } finally {
            if (al!=null)
                al.release();
        }

        logger.info(() -> (this + " stopped") );
    }

    protected static long throttle(long minFramePeriodMS, long frameTimeMS) {
        double remainingTime = (minFramePeriodMS - frameTimeMS) / 1.0E3;

        if (remainingTime > 0) {

            //        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            //e.printStackTrace();
//        }

            Util.pause(minFramePeriodMS);

        } else if (remainingTime < 0) {

            Thread.yield();

            //System.err.println(Thread.currentThread() + " loop lag: " + remainingTime + "ms too slow");

            //minFramePeriodMS++;
            //; incresing frame period to " + minFramePeriodMS + "ms");
        }
        return minFramePeriodMS;
    }


    public final void pause() {
        setPeriodMS(-1);
    }

//    //TODO not well tested
//    public void setRunning(boolean r) {
//        this.running = r;
//    }
//
//    public boolean isRunning() {
//        return running;
//    }
}
