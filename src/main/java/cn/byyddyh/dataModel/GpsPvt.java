package cn.byyddyh.dataModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GpsPvt {
    public List<BigDecimal> FctSeconds;                         /*time vector, same as gnssMeas.FctSeconds*/
    public List<BigDecimal[]> allLlaDegDegM;                    /*matrix, (i,:) = [lat (deg), lon (deg), alt (m)]*/
    public List<BigDecimal[]> sigmaLLaM;                        /*standard deviation of [lat,lon,alt] (m)*/
    public List<BigDecimal> allBcMeters;                        /*common bias computed with llaDegDegM*/
    public List<BigDecimal[]> allVelMps;                        /*(i,:) = velocity in NED coords*/
    public List<BigDecimal[]> sigmaVelMps;                      /*standard deviation of velocity (m/s)*/
    public List<BigDecimal> allBcDotMps;                        /*common freq bias computed with velocity*/
    public List<Integer> numSvs;                             /*number of satellites used in corresponding llaDegDegM*/
    public List<BigDecimal> hdop;                               /*hdop of corresponding fix*/

    public GpsPvt() {
        FctSeconds = new ArrayList<>();
        allLlaDegDegM = new ArrayList<>();
        sigmaLLaM = new ArrayList<>();
        allBcMeters = new ArrayList<>();
        allVelMps = new ArrayList<>();
        sigmaVelMps = new ArrayList<>();
        allBcDotMps = new ArrayList<>();
        numSvs = new ArrayList<>();
        hdop = new ArrayList<>();
    }

    public GpsPvt(List<BigDecimal> fctSeconds, List<BigDecimal[]> allLlaDegDegM, List<BigDecimal[]> sigmaLLaM,
                  List<BigDecimal> allBcMeters, List<BigDecimal[]> allVelMps, List<BigDecimal[]> sigmaVelMps,
                  List<BigDecimal> allBcDotMps, List<Integer> numSvs, List<BigDecimal> hdop) {
        FctSeconds = fctSeconds;
        this.allLlaDegDegM = allLlaDegDegM;
        this.sigmaLLaM = sigmaLLaM;
        this.allBcMeters = allBcMeters;
        this.allVelMps = allVelMps;
        this.sigmaVelMps = sigmaVelMps;
        this.allBcDotMps = allBcDotMps;
        this.numSvs = numSvs;
        this.hdop = hdop;
    }

    @Override
    public String toString() {
        return "GpsPvt{" +
                "FctSeconds=" + FctSeconds +
                ", allLlaDegDegM=" + allLlaDegDegM +
                ", sigmaLLaM=" + sigmaLLaM +
                ", allBcMeters=" + allBcMeters +
                ", allVelMps=" + allVelMps +
                ", sigmaVelMps=" + sigmaVelMps +
                ", allBcDotMps=" + allBcDotMps +
                ", numSvs=" + numSvs +
                ", hdop=" + hdop +
                '}';
    }
}
