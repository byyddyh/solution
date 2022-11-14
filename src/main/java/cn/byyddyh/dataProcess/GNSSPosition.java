package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSGpsEph;
import cn.byyddyh.dataModel.GNSSMeas;
import cn.byyddyh.dataModel.GpsPvt;
import cn.byyddyh.utils.GNSSThresholds;
import cn.byyddyh.utils.GpsConstants;
import cn.byyddyh.utils.MathUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 从gnssMeas计算PVT(位置、速度和时间(UTC时间)。)
 */
public class GNSSPosition {

    private static HashMap<Integer, List<Integer>> gpsEphToSvid = new HashMap<>();

    // index of columns
    private static final int jWk = 0, jSec = 1, jSv = 2, jPr = 3, jPrSig = 4, jPrr = 5, jPrrSig = 6;

    /**
     * initial state: [center of the Earth, bc=0, velocities = 0]'
     */
    private static double[] xo = new double[8];

    public static void gpsWlsPvt(GNSSMeas gnssMeas, GNSSGpsEph allGpsEph) {
        int N = gnssMeas.FctSeconds.size();
        long[] weekNum = new long[N];
        for (int i = 0; i < N; i++) {
            weekNum[i] = gnssMeas.FctSeconds.get(i).longValue() / GpsConstants.WEEKSEC;
        }

        // 这里的周展期待定检查（在ProcessGnssMeas中进行了检查，但该功能应该是独立的，因此我们应该再次检查，并在必要时将tRxSeconds调整为+-一周）
        // 顺便问一下，为什么不随身携带fct，而不用担心周数的麻烦以及相关周展期问题？
        // A、 因为当你把fct放进一个double时，你不能得到超过1000ns（1微秒）的预测。这将在距离残差计算中造成约800m/s*1us（卫星距离速率*时间误差）~1mm的误差。
        // 那么呢？好吧，如果你用载波相位开始处理，这些误差可能会累积。
        GpsPvt gpsPvt = new GpsPvt();

        // 确定Svid和index之间的关系
        loadMap(allGpsEph);

        gpsPvt.FctSeconds = gnssMeas.FctSeconds;

        for (int i = 0; i < N; i++) {
            List<Integer> svid = new ArrayList<>();

            // 根据 gnssMeas.PrM 选出有效的svid
            for (int j = 0; j < gnssMeas.PrM.get(i).length; j++) {
                if (gnssMeas.PrM.get(i)[j] != null) {
                    svid.add(gnssMeas.Svid.get(j));
                }
            }

            // 找到最接近最新鲜的星历数据
            GNSSGpsEph gpsEph = closestGpsEph(allGpsEph, svid, gnssMeas.FctSeconds.get(i));
            System.out.println(gpsEph);
            // svid for which we have ephemeris
            List<Integer> idList = gpsEph.PRN;
            int size = idList.size();
            if (size < 4) {
                // skip to next epoch
                continue;
            }

            gpsPvt.numSvs.add(size);

            /* WLS PVT */
            // for those svIds with valid ephemeris, pack prs matrix for WlsNav
            List<List<Double>> prs = new ArrayList<>();
            HashMap<Integer, Integer> idToSvidIndexMap = new HashMap<>();
            for (int k = 0; k < svid.size(); ++k) {
                idToSvidIndexMap.put(svid.get(k), k);
            }

            // 封装prs
            for (Integer is : idList) {
                Integer index = idToSvidIndexMap.get(is);
                List<Double> data = new ArrayList<>();
                data.add((double) weekNum[i]);
                data.add(gnssMeas.tRxSeconds.get(i)[index].doubleValue());
                data.add(Double.valueOf(is));
                data.add(gnssMeas.PrM.get(i)[index].doubleValue());
                data.add(gnssMeas.PrSigmaM.get(i)[index]);
                data.add(gnssMeas.PrrMps.get(i)[index]);
                data.add(gnssMeas.PrrSigmaMps.get(i)[index]);
                prs.add(data);
            }

            System.out.println(prs);

            // initialize speed to zero
            for (int j = 4; j < 6; j++) {
                xo[j] = 0;
            }

            // compute WLS solution
            wlsPvt(prs, gpsEph);
        }
    }

    private static void loadMap(GNSSGpsEph allGpsEph) {
        for (int i = 0; i < allGpsEph.PRN.size(); i++) {
            List<Integer> indexList = gpsEphToSvid.computeIfAbsent(allGpsEph.PRN.get(i), k -> new ArrayList<>());
            indexList.add(i);
        }
    }

    /**
     * find ephemeris in a GPS ephemeris structure allGpsEph for all svIds listed
     * return gpsEph = unique ephemeris for svIds, with fctToe closest to fctSeconds
     */
    private static GNSSGpsEph closestGpsEph(GNSSGpsEph allGpsEph, List<Integer> svIds, BigDecimal fctSeconds) {
        GNSSGpsEph gpsEph = new GNSSGpsEph();

        for (int i = 0; i < svIds.size(); i++) {
            List<Integer> indexList = gpsEphToSvid.get(svIds.get(i));
            if (indexList.size() > 0) {
                double minVal = Double.MAX_VALUE;
                int minIndex = 0;
                Integer fitInterval = 0;
                // 遍历 indexList
                for (int j = 0; j < indexList.size(); j++) {
                    Integer index = indexList.get(j);
                    // find Toe within fit interval, set fit interval
                    Integer fitIntervalHours = allGpsEph.Fit_interval.get(index);

                    // Rinex says "Zero if not known", so adjust for zeros
                    if (fitIntervalHours == 0) {
                        fitIntervalHours = 4;
                    }

                    // full cycle time of ephemeris Toe
                    long fctToe = allGpsEph.GPS_Week.get(index) * GpsConstants.WEEKSEC + allGpsEph.Toe.get(index);

                    // find freshest Toe
                    double min = Math.abs(fctToe - fctSeconds.doubleValue());
                    if (minVal > min) {
                        fitInterval = fitIntervalHours;
                        minVal = min;
                        minIndex = j;
                    }
                }

                // 更新gpsEph
                if (minVal < (fitInterval / 2.0) * 3600) {
                    gpsEph.PRN.add(allGpsEph.PRN.get(indexList.get(minIndex)));
                    gpsEph.Toc.add(allGpsEph.Toc.get(indexList.get(minIndex)));
                    gpsEph.af0.add(allGpsEph.af0.get(indexList.get(minIndex)));
                    gpsEph.af1.add(allGpsEph.af1.get(indexList.get(minIndex)));
                    gpsEph.af2.add(allGpsEph.af2.get(indexList.get(minIndex)));
                    gpsEph.IODE.add(allGpsEph.IODE.get(indexList.get(minIndex)));
                    gpsEph.Crs.add(allGpsEph.Crs.get(indexList.get(minIndex)));
                    gpsEph.Delta_n.add(allGpsEph.Delta_n.get(indexList.get(minIndex)));
                    gpsEph.M0.add(allGpsEph.M0.get(indexList.get(minIndex)));
                    gpsEph.Cuc.add(allGpsEph.Cuc.get(indexList.get(minIndex)));
                    gpsEph.e.add(allGpsEph.e.get(indexList.get(minIndex)));
                    gpsEph.Cus.add(allGpsEph.Cus.get(indexList.get(minIndex)));
                    gpsEph.Asqrt.add(allGpsEph.Asqrt.get(indexList.get(minIndex)));
                    gpsEph.Toe.add(allGpsEph.Toe.get(indexList.get(minIndex)));
                    gpsEph.Cic.add(allGpsEph.Cic.get(indexList.get(minIndex)));
                    gpsEph.OMEGA.add(allGpsEph.OMEGA.get(indexList.get(minIndex)));
                    gpsEph.Cis.add(allGpsEph.Cis.get(indexList.get(minIndex)));
                    gpsEph.i0.add(allGpsEph.i0.get(indexList.get(minIndex)));
                    gpsEph.Crc.add(allGpsEph.Crc.get(indexList.get(minIndex)));
                    gpsEph.omega.add(allGpsEph.omega.get(indexList.get(minIndex)));
                    gpsEph.OMEGA_DOT.add(allGpsEph.OMEGA_DOT.get(indexList.get(minIndex)));
                    gpsEph.IDOT.add(allGpsEph.IDOT.get(indexList.get(minIndex)));
                    gpsEph.codeL2.add(allGpsEph.codeL2.get(indexList.get(minIndex)));
                    gpsEph.GPS_Week.add(allGpsEph.GPS_Week.get(indexList.get(minIndex)));
                    gpsEph.L2Pdata.add(allGpsEph.L2Pdata.get(indexList.get(minIndex)));
                    gpsEph.accuracy.add(allGpsEph.accuracy.get(indexList.get(minIndex)));
                    gpsEph.health.add(allGpsEph.health.get(indexList.get(minIndex)));
                    gpsEph.TGD.add(allGpsEph.TGD.get(indexList.get(minIndex)));
                    gpsEph.IODC.add(allGpsEph.IODC.get(indexList.get(minIndex)));
                    gpsEph.ttx.add(allGpsEph.ttx.get(indexList.get(minIndex)));
                    gpsEph.Fit_interval.add(allGpsEph.Fit_interval.get(indexList.get(minIndex)));
                } else {
                    System.out.println("No valid ephemeris found for svId " + svIds.get(i));
                }
            }
        }

        return gpsEph;
    }

    /**
     * calculate a weighted least squares PVT solution, xHat given pseudoranges, pr rates, and initial state
     */
    private static void wlsPvt(List<List<Double>> prs, GNSSGpsEph gpsEph) {
        if (!checkInputs(prs, gpsEph)) {
            throw new Error("inputs not right size, or not properly aligned with each other");
        }

        List<Double> ttxSeconds = new ArrayList<>();
        List<Double> ttxWeek = new ArrayList<>();
        for (int i = 0; i < prs.size(); i++) {
            // week of tx. Note - we could get a rollover, when ttx_sv goes negative, and it is handled in GpsEph2Pvt, where we work with fct
            // 注意：当ttx_sv变为负值时，我们可能会发生滚动，它在GpsEph2Pvt中处理，我们在这里使用fct
            ttxWeek.add(prs.get(i).get(jWk));
            // ttx by sv clock, this is accurate satellite time of tx, because we use actual pseudo-ranges
            // 这是精确的tx卫星时间，因为我们在这里使用实际的伪距离，而不是校正的距离。
            ttxSeconds.add(prs.get(i).get(jSec) - prs.get(i).get(jPr) / GpsConstants.LIGHTSPEED);
        }
        // 写下伪距离的等式，以看到rx时钟误差精确抵消，从而获得精确的GPS时间：我们从sv时间中减去卫星时钟误差，如下所述：
        double[] dtsvS = GNSSGpsEph.gpsEph2Dtsv(gpsEph, ttxSeconds);

        // 从sv时间中减去dtsv得到真实的gps时间
        List<Double> ttx = new ArrayList<>();
        for (int i = 0; i < dtsvS.length; i++) {
            ttx.add(ttxSeconds.get(i) - dtsvS[i]);
        }

        // calculate satellite position at ttx
        XyzNode xyzNode = gpsEph2Pvt(gpsEph, ttxWeek, ttx);
        List<Double[]> svXyzTtx = xyzNode.xyzM;
        List<Double> dtsv = xyzNode.dtsvS;
        List<Double[]> svXyzDot = xyzNode.vMps;
        List<Double> dtsvDot = xyzNode.dtsvSDot;

        Double[][] svXyzTrx = new Double[svXyzTtx.size()][svXyzTtx.get(0).length];
        for (int i = 0; i < svXyzTtx.size(); i++) {
            System.arraycopy(svXyzTtx.get(i), 0, svXyzTrx[i], 0, svXyzTtx.get(i).length);
        }

        // Compute weights
        Double[][] Wpr = MathUtils.generateDiag(1.0, prs, jPrSig);
        Double[][] Wrr = MathUtils.generateDiag(1.0, prs, jPrrSig);

        // iterate on this next part till change in pos & line of sight vectors converge
        Double[] xHat = new Double[4];
        Double[] dx = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        int whileCount = 0, maxWhileCount = 100;

        // we expect the while loop to converge in < 10 iterations, even with initial
        // position on other side of the Earth (see Stanford course AA272C "Intro to GPS")
        double bc = xo[3];
        while (MathUtils.norm(dx) > GNSSThresholds.MAXDELPOSFORNAVM) {
            whileCount++;
            if (whileCount >= maxWhileCount) {
                throw new Error("while loop did not converge after " + whileCount + " iterations");
            }

            for (int i = 0; i < gpsEph.Toe.size(); i++) {
                // calculate tflight from, bc and dtsv
                double dtflight = (prs.get(i).get(jPr) - bc) / GpsConstants.LIGHTSPEED + dtsv.get(i);
                // Use of bc: bc>0 <=> pr too big <=> tflight too big.
                //        i.e. trx = trxu - bc/GpsConstants.LIGHTSPEED
                // Use of dtsv: dtsv>0 <=> pr too small <=> tflight too small.
                //        i.e ttx = ttxsv - dtsv
                MathUtils.flightTimeCorrection(svXyzTtx.get(i), dtflight);
            }
        }
    }

    /**
     * Calculate sv coordinates, in ECEF frame, sv clock bias, and sv velocity
     * 计算ECEF帧中的sv坐标、sv时钟偏差和sv速度
     */
    private static XyzNode gpsEph2Pvt(GNSSGpsEph gpsEph, List<Double> ttxWeek, List<Double> ttx) {
        XyzNode xyzNode = gpsEph2Xyz(gpsEph, ttxWeek, ttx);
        xyzNode.vMps = new ArrayList<>();
        xyzNode.dtsvSDot = new ArrayList<>();

        // compute velocity from delta position & dtsvS at (t+0.5) - (t-0.5)
        // This is better than differentiating, because both the orbital and relativity
        // terms have nonlinearities that are not easily differentiable
        // 根据（t+0.5）-（t-0.5）处的增量位置和dtsvS计算速度，这比微分要好，因为轨道项和相对论项都具有不易微分的非线性

        // time + 0.5 seconds
        List<Double> t1 = new ArrayList<>();
        List<Double> t2 = new ArrayList<>();
        for (int i = 0; i < ttx.size(); i++) {
            t1.add(ttx.get(i) + 0.5);
            t2.add(ttx.get(i) - 0.5);
        }
        XyzNode xyzNodePlus = gpsEph2Xyz(gpsEph, ttxWeek, t1);

        // time - 0.5 seconds
        XyzNode xyzNodeMinus = gpsEph2Xyz(gpsEph, ttxWeek, t2);

        System.out.println("=============");
        for (int i = 0; i < xyzNodePlus.xyzM.size(); i++) {
            Double[] xyz = new Double[3];
            for (int j = 0; j < 3; j++) {
                xyz[j] = xyzNodePlus.xyzM.get(i)[j] - xyzNodeMinus.xyzM.get(i)[j];
            }

            xyzNode.vMps.add(xyz);
            xyzNode.dtsvSDot.add(xyzNodePlus.dtsvS.get(i) - xyzNodeMinus.dtsvS.get(i));
        }
        System.out.println("=============");
        return xyzNode;
    }

    static class XyzNode {
        List<Double> dtsvS;

        List<Double[]> xyzM;

        List<Double[]> vMps;

        List<Double> dtsvSDot;
    }

    /**
     * Calculate sv coordinates, in ECEF frame, at ttx = gpsTime
     */
    private static XyzNode gpsEph2Xyz(GNSSGpsEph gpsEph, List<Double> ttxWeek, List<Double> ttx) {
        int p = gpsEph.TGD.size();
        if (ttxWeek.size() != p || ttx.size() != p) {
            throw new Error("gpsTime must be px2 [gpsWeek, gpsSec], where p =length(gpsEph)");
        }

        List<Double> MK = new ArrayList<>();
        double[] A = new double[gpsEph.Fit_interval.size()];
        double[] tk = new double[gpsEph.Fit_interval.size()];
        for (int i = 0; i < gpsEph.Fit_interval.size(); i++) {
            // set fitIntervalSeconds
            double fitIntervalHours = gpsEph.Fit_interval.get(i);
            if (fitIntervalHours == 0) {
                fitIntervalHours = 2;
            }
            double fitIntervalSeconds = fitIntervalHours * 3600;

            // Calculate dependent variables -------------------------------------------------
            // Time since time of applicability accounting for weeks and therefore rollovers
            // subtract weeks first, to avoid precision errors:
            tk[i] = (ttxWeek.get(i) - gpsEph.GPS_Week.get(i)) * GpsConstants.WEEKSEC + (ttx.get(i) - gpsEph.Toe.get(i));
            if (Math.abs(tk[i]) > fitIntervalSeconds) {
                System.out.println("WARNING in GpsEph2Xyz.m, times outside fit interval");
            }

            // semi-major axis of orbit
            A[i] = Math.pow(gpsEph.Asqrt.get(i).doubleValue(), 2);
            // Computed mean motion (rad/sec)
            double no = Math.sqrt(GpsConstants.mu / Math.pow(A[i], 3));

            // Corrected Mean Motion
            double n = no + gpsEph.Delta_n.get(i).doubleValue();
            double h = Math.sqrt(A[i] * (1 - Math.pow(gpsEph.e.get(i), 2)) * GpsConstants.mu);

            // Mean Anomaly
            MK.add(gpsEph.M0.get(i) + n * tk[i]);
        }

        // Solve Kepler's equation for eccentric anomaly
        Double[] Ek = MathUtils.Kepler(MK, gpsEph.e);

        // Calculate satellite clock bias (See ICD-GPS-200 20.3.3.3.3.1)
        // subtract weeks first, to avoid precision errors:
        List<Double> dtsvS = new ArrayList<>();
        List<Double[]> xyzM = new ArrayList<>();
        for (int i = 0; i < ttx.size(); i++) {
            double dt = (ttxWeek.get(i) - gpsEph.GPS_Week.get(i)) * GpsConstants.WEEKSEC + (ttx.get(i) - gpsEph.Toc.get(i));

            // Calculate satellite clock bias
            double sin_EK = Math.sin(Ek[i]);
            double cos_EK = Math.cos(Ek[i]);
            dtsvS.add( gpsEph.af0.get(i).doubleValue() + gpsEph.af1.get(i).doubleValue() * dt + gpsEph.af2.get(i).doubleValue() * dt * dt
                    + GpsConstants.FREL * gpsEph.e.get(i) * gpsEph.Asqrt.get(i).doubleValue() * sin_EK - gpsEph.TGD.get(i).doubleValue() );

            // true anomaly
            double vk = Math.atan2(Math.sqrt(1 - Math.pow(gpsEph.e.get(i), 2)) * sin_EK / (1 - gpsEph.e.get(i) * cos_EK),
                    (cos_EK - gpsEph.e.get(i)) / (1 - gpsEph.e.get(i) * cos_EK));
            // Argument of latitude
            double Phik = vk + gpsEph.omega.get(i);

            double sin_2Phik = Math.sin(2 * Phik);
            double cos_2Phik = Math.cos(2 * Phik);
            // The next three terms are the second harmonic perturbations
            double duk = gpsEph.Cus.get(i).doubleValue() * sin_2Phik + gpsEph.Cuc.get(i).doubleValue() * cos_2Phik;         // Argument of latitude correction
            double drk = gpsEph.Crc.get(i) * cos_2Phik + gpsEph.Crs.get(i) * sin_2Phik;                                     // Radius Correction
            double dik = gpsEph.Cic.get(i).doubleValue() * cos_2Phik + gpsEph.Cis.get(i).doubleValue() * sin_2Phik;         // Correction to Inclination

            double uk = Phik + duk;                                                                                 // Corrected argument of latitude
            double rk = A[i] * ((1 - Math.pow(gpsEph.e.get(i), 2)) / (1 + gpsEph.e.get(i) * Math.cos(vk))) + drk;                // Corrected radius
            double ik = gpsEph.i0.get(i) + gpsEph.IDOT.get(i).doubleValue() * tk[i] + dik;                          // Corrected inclination

            double sin_uk = Math.sin(uk);
            double cos_uk = Math.cos(uk);
            double xkp = rk * cos_uk;           // Position in orbital plane
            double ykp = rk * sin_uk;

            // Wk = corrected longitude of ascending node
            double Wk = gpsEph.OMEGA.get(i) + (gpsEph.OMEGA_DOT.get(i).doubleValue() - GpsConstants.WE) * tk[i] - GpsConstants.WE * gpsEph.Toe.get(i);

            // for dtflight, see FlightTimeCorrection.m
            double sin_Wk = Math.sin(Wk);
            double cos_Wk = Math.cos(Wk);
            double sin_ik = Math.sin(ik);
            double cos_ik = Math.cos(ik);

            Double[] xyz = new Double[]{
                    xkp * cos_Wk - ykp * cos_ik * sin_Wk,
                    xkp * sin_Wk + ykp * cos_ik * cos_Wk,
                    ykp * sin_ik
            };
            xyzM.add(xyz);
        }

        XyzNode node = new XyzNode();
        node.dtsvS = dtsvS;
        node.xyzM = xyzM;
        return node;
    }

    /**
     * utility function for WlsPvt
     */
    private static boolean checkInputs(List<List<Double>> prs, GNSSGpsEph gpsEph) {
        if (prs.size() != gpsEph.PRN.size()) {
            return false;
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < prs.size(); i++) {
            max = Math.max(max, prs.get(i).get(jSec));
            min = Math.min(min, prs.get(i).get(jSec));

            if (prs.get(i).get(jSv).intValue() != gpsEph.PRN.get(i)) {
                return false;
            }

            if (prs.get(i).size() != 7) {
                return false;
            }
        }

        return !(max - min > 0);
    }

    public static void main(String[] args) {
        System.out.println(Math.atan2(4, -3));

        Double[] dx = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        System.out.println(MathUtils.norm(new Double[]{-2.0, -2.0}));
        System.out.println(MathUtils.norm(dx) > GNSSThresholds.MAXDELPOSFORNAVM);
    }
}
