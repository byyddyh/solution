package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSMeas;
import cn.byyddyh.dataModel.GNSSRaw;
import cn.byyddyh.utils.GNSSThresholds;
import cn.byyddyh.utils.GpsConstants;

import java.math.BigDecimal;
import java.util.*;

/**
 * 处理原始测量值，计算伪距
 */
public class PseudorangeProcess {

    private final static long WEEKNANOS = 604800000000000L;
    private static double[] tRxSeconds;
    private static double[] tTxSeconds;

    public static GNSSMeas processGnssMeas(GNSSRaw gnssRaw) {
        GNSSMeas gnssMeas = new GNSSMeas();

        // 首先筛选有效值，以便对有效数据进行滚动检查等
        gnssRaw = filterValid(gnssRaw);

        // 1毫秒内的任何东西都被认为是相同的历元
        double[] rxMillis = new double[gnssRaw.allRxMillis.size()];
        for (int i = 0; i < gnssRaw.allRxMillis.size(); i++) {
            rxMillis[i] = gnssRaw.allRxMillis.get(i) * 0.001;
            BigDecimal decimal = BigDecimal.valueOf(rxMillis[i]);
            if (gnssMeas.FctSeconds.size() == 0 || gnssMeas.FctSeconds.get(gnssMeas.FctSeconds.size() - 1).compareTo(decimal) != 0) {
                gnssMeas.FctSeconds.add(decimal);
            }
        }

        // N 执行时间
        int N = gnssMeas.FctSeconds.size();
        System.out.println("\t\tN:" + N);

        // M 卫星数量
        TreeSet<Integer> Svid = new TreeSet<>();
        for (int i = 0; i < gnssRaw.Svid.size(); i++) {
            Svid.add(gnssRaw.Svid.get(i).intValue());
        }
        gnssMeas.Svid.addAll(Svid);
        System.out.println("Svid: " + Svid);
        int M = Svid.size();
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        int index = 0;
        for (Integer id :Svid) {
            hashMap.put(id, index++);
        }

        // GPS周数
        int[] weekNumbers = new int[gnssRaw.FullBiasNanos.size()];
        for (int i = 0; i < gnssRaw.FullBiasNanos.size(); i++) {
            weekNumbers[i] = (int) (-gnssRaw.FullBiasNanos.get(i) / 1000000000.0 / GpsConstants.WEEKSEC);
        }

        // 如果Tow state~=1，那么断言，因为gnssRaw.FullBiasNanos（1）可能是错误的
        long state = gnssRaw.State.get(0);
        if (!((state & 1) > 0 && (state & 8) > 0)) {
            throw new Error("gnssRaw.State(1) must have bits 0 and 3 true before calling ProcessGnssMeas");
        }

        // 计算相对于周初的测量时间,在一周的铸造时间之前减去大长（即1980年的时间）作为双倍
        // 使用gnssRaw.FullBiasNanos(1)计算TrxNano，以便TrxNano包括自第一个历元起的rx时钟漂移
        long weekNumberNanos;
        long tRxNanos;

        tRxSeconds = new double[weekNumbers.length];
        tTxSeconds = new double[weekNumbers.length];
        for (int i = 0; i < weekNumbers.length; i++) {
            weekNumberNanos = weekNumbers[i] * WEEKNANOS;
            tRxNanos = gnssRaw.TimeNanos.get(i) - gnssRaw.FullBiasNanos.get(0) - weekNumberNanos;

            if (tRxNanos < 0) {
                throw new Error("tRxNanos should be >= 0");
            }

            // 减去分数偏移TimeOffsetNanos和BiasNanos
            tRxSeconds[i] = (tRxNanos - gnssRaw.TimeOffsetNanos.get(i) - gnssRaw.BiasNanos.get(i)) / 1000000000.0;
            tTxSeconds[i] = gnssRaw.ReceivedSvTimeNanos.get(i) / 1000000000.0;
        }

        gnssMeas.HwDscDelS = new ArrayList<>(Collections.nCopies(N, 0));

        // 检查tRxSeconds中的周滚动
        double[] prSeconds = checkGpsWeekRoller();

        // 用米计算伪距
        double[] PrM = new double[prSeconds.length];
        double[] PrSigmaM = new double[prSeconds.length];
        for (int i = 0; i < prSeconds.length; i++) {
            PrM[i] = prSeconds[i] * GpsConstants.LIGHTSPEED;
            PrSigmaM[i] = gnssRaw.ReceivedSvTimeUncertaintyNanos.get(i) * 0.000000001 * GpsConstants.LIGHTSPEED;
        }

        List<Double> PrrMps = gnssRaw.PseudorangeRateMetersPerSecond;
        List<Double> PrrSigmaMps = gnssRaw.PseudorangeRateUncertaintyMetersPerSecond;
        List<Double> AdrM = gnssRaw.AccumulatedDeltaRangeMeters;
        List<Double> AdrSigmaM = gnssRaw.AccumulatedDeltaRangeUncertaintyMeters;
        List<Double> AdrState = gnssRaw.AccumulatedDeltaRangeState;
        List<Double> Cn0DbHz = gnssRaw.Cn0DbHz;

        // 向量打包
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < N; i++) {
            // get index of measurements within 1ms of this time tag
            for (; endIndex < gnssRaw.allRxMillis.size(); endIndex++) {
                if (Math.abs(gnssMeas.FctSeconds.get(i).doubleValue() * 1000 - gnssRaw.allRxMillis.get(endIndex)) >= 1) {
                    break;
                }
            }

            BigDecimal[] gtRxSeconds = new BigDecimal[M];
            BigDecimal[] gtTxSeconds = new BigDecimal[M];
            BigDecimal[] gPrM = new BigDecimal[M];
            Double[] gPrSigmaM = new Double[M];
            Double[] gPrrMps = new Double[M];
            Double[] gPrrSigmaMps = new Double[M];
            Double[] gAdrM = new Double[M];
            Double[] gAdrSigmaM = new Double[M];
            Double[] gAdrState = new Double[M];
            Double[] gCn0DbHz = new Double[M];
            int countIndex = startIndex;
            while(endIndex - startIndex > 0) {
                int k = hashMap.get(gnssRaw.Svid.get(startIndex).intValue());
                gtRxSeconds[k] = BigDecimal.valueOf(tRxSeconds[startIndex]);
                gtTxSeconds[k] = BigDecimal.valueOf(tTxSeconds[startIndex]);
                gPrM[k] = BigDecimal.valueOf(PrM[startIndex]);
                gPrSigmaM[k] = PrSigmaM[startIndex];
                gPrrMps[k] = PrrMps.get(startIndex);
                gPrrSigmaMps[k] = PrrSigmaMps.get(startIndex);
                gAdrM[k] = AdrM.get(startIndex);
                gAdrSigmaM[k] = AdrSigmaM.get(startIndex);
                gAdrState[k] = AdrState.get(startIndex);
                gCn0DbHz[k] = Cn0DbHz.get(startIndex++);
            }

            gnssMeas.tRxSeconds.add(gtRxSeconds);
            gnssMeas.tTxSeconds.add(gtTxSeconds);
            gnssMeas.PrM.add(gPrM);
            gnssMeas.PrSigmaM.add(gPrSigmaM);
            gnssMeas.PrrMps.add(gPrrMps);
            gnssMeas.PrrSigmaMps.add(gPrrSigmaMps);
            gnssMeas.AdrM.add(gAdrM);
            gnssMeas.AdrSigmaM.add(gAdrSigmaM);
            gnssMeas.AdrState.add(gAdrState);
            gnssMeas.Cn0DbHz.add(gCn0DbHz);

            // save the hw clock discontinuity count for this epoch
            gnssMeas.ClkDCount.add(gnssRaw.HardwareClockDiscontinuityCount.get(countIndex).intValue());

            if (!Objects.equals(gnssRaw.HardwareClockDiscontinuityCount.get(countIndex), gnssRaw.HardwareClockDiscontinuityCount.get(endIndex - 1))) {
                throw new Error("HardwareClockDiscontinuityCount changed within the same epoch");
            }
        }

        // GetDelPr
        getDelPr(gnssMeas);
        return gnssMeas;
    }

    /**
     * utility function for ProcessGnssMeas,
     * remove fields corresponding to measurements that are invalid
     */
    private static GNSSRaw filterValid(GNSSRaw raw) {
        GNSSRaw gnssRaw = new GNSSRaw();

        for (int i = 0; i < raw.FullBiasNanos.size(); i++) {
            if (!(raw.ReceivedSvTimeUncertaintyNanos.get(i) > GNSSThresholds.MAXTOWUNCNS
                    || raw.PseudorangeRateUncertaintyMetersPerSecond.get(i) > GNSSThresholds.MAXPRRUNCMPS)) {
                gnssRaw.ElapsedRealtimeMillis.add(raw.ElapsedRealtimeMillis.get(i));
                gnssRaw.TimeNanos.add(raw.TimeNanos.get(i));
                gnssRaw.FullBiasNanos.add(raw.FullBiasNanos.get(i));
                gnssRaw.BiasNanos.add(raw.BiasNanos.get(i));
                gnssRaw.BiasUncertaintyNanos.add(raw.BiasUncertaintyNanos.get(i));
                gnssRaw.DriftNanosPerSecond.add(raw.DriftNanosPerSecond.get(i));
                gnssRaw.DriftUncertaintyNanosPerSecond.add(raw.DriftUncertaintyNanosPerSecond.get(i));
                gnssRaw.HardwareClockDiscontinuityCount.add(raw.HardwareClockDiscontinuityCount.get(i));
                gnssRaw.Svid.add(raw.Svid.get(i));
                gnssRaw.TimeOffsetNanos.add(raw.TimeOffsetNanos.get(i));
                gnssRaw.State.add(raw.State.get(i));
                gnssRaw.ReceivedSvTimeNanos.add(raw.ReceivedSvTimeNanos.get(i));
                gnssRaw.ReceivedSvTimeUncertaintyNanos.add(raw.ReceivedSvTimeUncertaintyNanos.get(i));
                gnssRaw.Cn0DbHz.add(raw.Cn0DbHz.get(i));
                gnssRaw.PseudorangeRateMetersPerSecond.add(raw.PseudorangeRateMetersPerSecond.get(i));
                gnssRaw.PseudorangeRateUncertaintyMetersPerSecond.add(raw.PseudorangeRateUncertaintyMetersPerSecond.get(i));
                gnssRaw.AccumulatedDeltaRangeState.add(raw.AccumulatedDeltaRangeState.get(i));
                gnssRaw.AccumulatedDeltaRangeMeters.add(raw.AccumulatedDeltaRangeMeters.get(i));
                gnssRaw.AccumulatedDeltaRangeUncertaintyMeters.add(raw.AccumulatedDeltaRangeUncertaintyMeters.get(i));
                gnssRaw.CarrierCycles.add(raw.CarrierCycles.get(i));
                gnssRaw.MultipathIndicator.add(raw.MultipathIndicator.get(i));
                gnssRaw.ConstellationType.add(raw.ConstellationType.get(i));
                gnssRaw.allRxMillis.add(raw.allRxMillis.get(i));
            }
        }

        return gnssRaw;
    }

    /**
     * 检查tRxSeconds中的周滚动
     */
    private static double[] checkGpsWeekRoller() {
        double[] prSeconds = new double[tRxSeconds.length];
        for (int i = 0; i < tRxSeconds.length; i++) {
            prSeconds[i] = tRxSeconds[i] - tTxSeconds[i];
            if (prSeconds[i] > GpsConstants.WEEKSEC * 0.5) {
                System.out.println("WARNING: week rollover detected in time tags. Adjusting ...");

                // TODO 需要修正
                double delS = Math.round(prSeconds[i] / GpsConstants.WEEKSEC) * GpsConstants.WEEKSEC;
                prSeconds[i] = prSeconds[i] - delS;
                if (prSeconds[i] > 10) {
                    throw new Error("Failed to correct week rollover");
                } else {
                    tRxSeconds[i] = tRxSeconds[i] - delS;
                    System.out.println("Corrected week rollover");
                }
            }
        }

        return prSeconds;
    }

    /**
     * utility function for ProcessGnssMeas, compute DelPr. gnssMeas.DelPrM = NxM, change in pr while clock is continuous
     */
    private static void getDelPr(GNSSMeas gnssMeas) {
        int N = gnssMeas.FctSeconds.size();
        int M = gnssMeas.Svid.size();

        // clock discontinuity
        List<Boolean> bClockDis = new ArrayList<>();
        bClockDis.add(false);
        for (int i = 0; i < gnssMeas.ClkDCount.size() - 1; i++) {
            bClockDis.add(gnssMeas.ClkDCount.get(i+1) - gnssMeas.ClkDCount.get(i) != 0);
        }

        // initialize first epoch to zero (by definition), rest to NaN
        Double[][] delPrM = new Double[N][M];
        for (int i = 0; i < M; i++) {
            delPrM[0][i] = (double) 0;
        }

        for (int j = 0; j < M; j++) {
            int i0 = 0;
            for (int i = 1; i < N; i++) {
                if (bClockDis.get(i) || gnssMeas.PrM.get(i0)[j] == null) {
                    i0 = i;
                }
                if (bClockDis.get(i)) {
                    delPrM[i][j] = null;
                } else {
                    if (gnssMeas.PrM.get(i)[j] == null) {
                        delPrM[i][j] = null;
                    } else {
                        delPrM[i][j] = gnssMeas.PrM.get(i)[j].subtract(gnssMeas.PrM.get(i0)[j]).doubleValue();
                    }
                }
            }
        }

        gnssMeas.DelPrM.addAll(Arrays.asList(delPrM).subList(0, N));
    }


    public static void main(String[] args) {
        System.out.println(Math.round(2.4));
        System.out.println(Math.round(2.6));
        System.out.println(Math.round(-2.4));
        System.out.println(Math.round(-2.6));

        System.out.println(PseudorangeProcess.WEEKNANOS);

        // state = 16399
        int state = 16399;
        System.out.println(state & 1);
        System.out.println(state & 8);
        System.out.println();

        System.out.println(Arrays.toString(new Double[10]));
    }
}
