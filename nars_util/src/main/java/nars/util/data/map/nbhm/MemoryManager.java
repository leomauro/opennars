//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nars.util.data.map.nbhm;

import sun.misc.Cleaner;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;


public abstract class MemoryManager {
    static final long MEM_MAX = Runtime.getRuntime().maxMemory();
    static final MemoryManager.HeapUsageMonitor HEAP_USAGE_MONITOR = new MemoryManager.HeapUsageMonitor();
    static long MEM_CRITICAL;
    private static volatile boolean CAN_ALLOC;
    private static volatile boolean MEM_LOW_CRITICAL;
    private static Object _lock;
    //private static final Histo myHisto;
    static final AtomicLong MEM_ALLOC;
    static final AtomicLong _taskMem;
    private static Object _taskMemLock;

    public MemoryManager() {
    }

    static void setMemGood() {
        if(!CAN_ALLOC) {
            Object var0 = _lock;
            synchronized(_lock) {
                CAN_ALLOC = true;
                _lock.notifyAll();
            }

            //Log.warn(new Object[]{"Continuing after swapping"});
        }
    }

    static void setMemLow() {
        if(CAN_ALLOC) {
            Object var0 = _lock;
            synchronized(_lock) {
                CAN_ALLOC = false;
            }

            //Log.warn(new Object[]{"Pausing to swap to disk; more memory may help"});
        }
    }

    static boolean canAlloc() {
        return CAN_ALLOC;
    }

//    static void set_goals(String msg, boolean oom) {
//
//    set_goals(msg, oom, 0L);
//    }

//    static void set_goals(String msg, boolean oom, long bytes) {
//        long heapUsed = Cleaner.HEAP_USED_AT_LAST_GC;
//        long timeGC = Cleaner.TIME_AT_LAST_GC;
//        long freeHeap = MEM_MAX - heapUsed;
//
//        assert freeHeap >= 0L : "I am really confused about the heap usage; MEM_MAX=" + MEM_MAX + " heapUsed=" + heapUsed;
//
//        long cacheUsage = myHisto.histo(false)._cached;
//        long pojoUsedGC = Math.max(heapUsed - cacheUsage, 0L);
//        long d = MEM_CRITICAL;
//        long p = pojoUsedGC;
//        long age = System.currentTimeMillis() - timeGC;
//
//        for(age = Math.min(age, 600000L); (age -= 5000L) > 0L; p -= p >> 3) {
//            ;
//        }
//
//        d -= 2L * p - bytes;
//        d = Math.max(d, MEM_MAX >> 3);
//        if(Cleaner.DESIRED != -1L) {
//            Cleaner.DESIRED = d;
//        }
//
//        String m = "";
//        if(cacheUsage > Cleaner.DESIRED) {
//            m = CAN_ALLOC?"Blocking!  ":"blocked:   ";
//            if(oom) {
//                setMemLow();
//            }
//
//            Cleaner.kick_store_cleaner();
//        } else {
//            setMemGood();
//            if(oom) {
//                m = "Unblock allocations; cache emptied but memory is low: ";
//                Log.warn(new Object[]{m + " OOM but cache is emptied:  MEM_MAX = " + PrettyPrint.bytes(MEM_MAX) + ", DESIRED_CACHE = " + PrettyPrint.bytes(d) + ", CACHE = " + PrettyPrint.bytes(cacheUsage) + ", POJO = " + PrettyPrint.bytes(p) + ", this request bytes = " + PrettyPrint.bytes(bytes)});
//            } else {
//                m = "MemGood:   ";
//            }
//        }
//
//        String s = m + msg + ", HEAP_LAST_GC=" + (heapUsed >> 20) + "M, KV=" + (cacheUsage >> 20) + "M, POJO=" + (pojoUsedGC >> 20) + "M, free=" + (freeHeap >> 20) + "M, MAX=" + (MEM_MAX >> 20) + "M, DESIRED=" + (Cleaner.DESIRED >> 20) + "M" + (oom?" OOM!":" NO-OOM");
//        if(CAN_ALLOC) {
//            Log.trace(new Object[]{s});
//        } else {
//            System.err.println(s);
//        }
//
//    }

    static Object malloc(int elems, long bytes, int type, Object orig, int from) {
        return malloc(elems, bytes, type, orig, from, false);
    }

    static Object malloc(int elems, long bytes, int type, Object orig, int from, boolean force) {
        while(true) {
            if(!MEM_LOW_CRITICAL && !force && !CAN_ALLOC && bytes > 256L  ) {
                    //&& !(Thread.currentThread() instanceof Cleaner)) {
                Object e = _lock;
                synchronized(_lock) {
                    try {
                        _lock.wait(3000L);
                    } catch (InterruptedException var10) {
                        ;
                    }
                }
            }

            MEM_ALLOC.addAndGet(bytes);

            try {
                switch(type) {
                case -9:
                    return Arrays.copyOfRange((double[])((double[])orig), from, elems);
                case -8:
                    return Arrays.copyOfRange((long[])((long[])orig), from, elems);
                case -7:
                case -6:
                case -5:
                case -3:
                case -2:
                case 3:
                case 6:
                case 7:
                default:
                    throw new RuntimeException("fail"); //H2O.fail();
                case -4:
                    return Arrays.copyOfRange((int[])((int[])orig), from, elems);
                case -1:
                    return Arrays.copyOfRange((byte[])((byte[])orig), from, elems);
                case 0:
                    return new boolean[elems];
                case 1:
                    return new byte[elems];
                case 2:
                    return new short[elems];
                case 4:
                    return new int[elems];
                case 5:
                    return new float[elems];
                case 8:
                    return new long[elems];
                case 9:
                    return new double[elems];
                case 10:
                    return new Object[elems];
                }
            } catch (OutOfMemoryError var12) {
                /*
                if(Cleaner.isDiskFull()) {
                    UDPRebooted.suicide(T.oom, H2O.SELF);
                }

                set_goals("OOM", true, bytes);
                */
                throw new RuntimeException(var12);
            }
        }
    }

    public static byte[] malloc1(int size) {
        return malloc1(size, false);
    }

    public static byte[] malloc1(int size, boolean force) {
        return (byte[])((byte[])malloc(size, (long)(size * 1), 1, (Object)null, 0, force));
    }

    public static short[] malloc2(int size) {
        return (short[])((short[])malloc(size, (long)(size * 2), 2, (Object)null, 0));
    }

    public static int[] malloc4(int size) {
        return (int[])((int[])malloc(size, (long)(size * 4), 4, (Object)null, 0));
    }

    public static long[] malloc8(int size) {
        return (long[])((long[])malloc(size, (long)(size * 8), 8, (Object)null, 0));
    }

    public static float[] malloc4f(int size) {
        return (float[])((float[])malloc(size, (long)(size * 4), 5, (Object)null, 0));
    }

    public static double[] malloc8d(int size) {
        return (double[])((double[])malloc(size, (long)(size * 8), 9, (Object)null, 0));
    }

    public static boolean[] mallocZ(int size) {
        return (boolean[])((boolean[])malloc(size, (long)size, 0, (Object)null, 0));
    }

    public static Object[] mallocObj(int size) {
        return (Object[])((Object[])malloc(size, (long)(size * 8), 10, (Object)null, 0, false));
    }

    public static byte[] arrayCopyOfRange(byte[] orig, int from, int sz) {
        return (byte[])((byte[])malloc(sz, (long)(sz - from), -1, orig, from));
    }

    public static int[] arrayCopyOfRange(int[] orig, int from, int sz) {
        return (int[])((int[])malloc(sz, (long)((sz - from) * 4), -4, orig, from));
    }

    public static long[] arrayCopyOfRange(long[] orig, int from, int sz) {
        return (long[])((long[])malloc(sz, (long)((sz - from) * 8), -8, orig, from));
    }

    public static double[] arrayCopyOfRange(double[] orig, int from, int sz) {
        return (double[])((double[])malloc(sz, (long)((sz - from) * 8), -9, orig, from));
    }

    public static byte[] arrayCopyOf(byte[] orig, int sz) {
        return arrayCopyOfRange((byte[])orig, 0, sz);
    }

    public static int[] arrayCopyOf(int[] orig, int sz) {
        return arrayCopyOfRange((int[])orig, 0, sz);
    }

    public static long[] arrayCopyOf(long[] orig, int sz) {
        return arrayCopyOfRange((long[])orig, 0, sz);
    }

    public static double[] arrayCopyOf(double[] orig, int sz) {
        return arrayCopyOfRange((double[])orig, 0, sz);
    }

    static boolean tryReserveTaskMem(long m) {
        if(!CAN_ALLOC) {
            return false;
        } else if(m == 0L) {
            return true;
        } else {
            assert m >= 0L : "m < 0: " + m;

            long current = _taskMem.addAndGet(-m);
            if(current < 0L) {
                _taskMem.addAndGet(m);
                return false;
            } else {
                return true;
            }
        }
    }

    /*
    static void reserveTaskMem(long m) {
        final long bytes = m;

        while(!tryReserveTaskMem(bytes)) {
            try {
                ForkJoinPool.managedBlock(new ManagedBlocker() {
                    public boolean isReleasable() {
                        return MemoryManager._taskMem.get() >= bytes;
                    }

                    public boolean block() throws InterruptedException {
                        synchronized(MemoryManager._taskMemLock) {
                            try {
                                MemoryManager._taskMemLock.wait();
                            } catch (InterruptedException var4) {
                                ;
                            }
                        }

                        return this.isReleasable();
                    }
                });
            } catch (InterruptedException var5) {
                Log.throwErr(var5);
            }
        }

    }
    */

    static void freeTaskMem(long m) {
        if(m != 0L) {
            _taskMem.addAndGet(m);
            Object var2 = _taskMemLock;
            synchronized(_taskMemLock) {
                _taskMemLock.notifyAll();
            }
        }
    }

    static {
        MEM_CRITICAL = HEAP_USAGE_MONITOR._gc_callback;
        CAN_ALLOC = true;
        MEM_LOW_CRITICAL = false;
        _lock = new Object();
        //myHisto = new Histo();
        MEM_ALLOC = new AtomicLong();
        _taskMem = new AtomicLong(MEM_MAX - (MEM_MAX >> 2));
        _taskMemLock = new Object();
    }

    private static class HeapUsageMonitor implements NotificationListener {
        MemoryMXBean _allMemBean = ManagementFactory.getMemoryMXBean();
        MemoryPoolMXBean _oldGenBean;
        long _gc_callback;

        HeapUsageMonitor() {
            int c = 0;
            Iterator i$ = ManagementFactory.getMemoryPoolMXBeans().iterator();

            while(true) {
                MemoryPoolMXBean m;
                do {
                    do {
                        do {
                            if(!i$.hasNext()) {
                                assert c == 1;

                                return;
                            }

                            m = (MemoryPoolMXBean)i$.next();
                        } while(m.getType() != MemoryType.HEAP);
                    } while(!m.isCollectionUsageThresholdSupported());
                } while(!m.isUsageThresholdSupported());

                this._oldGenBean = m;
                this._gc_callback = MemoryManager.MEM_MAX;

                while(true) {
                    try {
                        m.setCollectionUsageThreshold(this._gc_callback);
                    } catch (IllegalArgumentException var5) {
                        this._gc_callback -= this._gc_callback >> 3;
                        continue;
                    }

                    NotificationEmitter emitter = (NotificationEmitter)this._allMemBean;
                    emitter.addNotificationListener(this, (NotificationFilter)null, m);
                    ++c;
                    break;
                }
            }
        }

        public void handleNotification(Notification notification, Object handback) {
        }
    }
}
