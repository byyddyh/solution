package cn.byyddyh.dataModel;

import java.util.ArrayList;
import java.util.List;

public class GNSSRaw {
    private List<Double> ElapsedRealtimeMillis;
    private List<Integer> TimeNanos;
    private List<Double> LeapSecond;
    private List<Double> TimeUncertaintyNanos;
    private List<Integer> FullBiasNanos;
    private List<Double> BiasNanos;
    private List<Double> BiasUncertaintyNanos;
    private List<Double> DriftNanosPerSecond;
    private List<Double> DriftUncertaintyNanosPerSecond;
    private List<Double> HardwareClockDiscontinuityCount;
    private List<Double> Svid;
    private List<Double> TimeOffsetNanos;
    private List<Double> State;
    private List<Integer> ReceivedSvTimeNanos;
    private List<Integer> ReceivedSvTimeUncertaintyNanos;
    private List<Double> Cn0DbHz;
    private List<Double> PseudorangeRateMetersPerSecond;
    private List<Double> PseudorangeRateUncertaintyMetersPerSecond;
    private List<Double> AccumulatedDeltaRangeState;
    private List<Double> AccumulatedDeltaRangeMeters;
    private List<Double> AccumulatedDeltaRangeUncertaintyMeters;
    private List<Double> CarrierFrequencyHz;
    private List<Integer> CarrierCycles;
    private List<Double> MultipathIndicator;
    private List<Double> ConstellationType;
    private List<Double> AgcDb;
    private List<Integer> allRxMillis;

    public GNSSRaw() {
        ElapsedRealtimeMillis = new ArrayList<>();
        TimeNanos = new ArrayList<>();
        LeapSecond = new ArrayList<>();
        TimeUncertaintyNanos = new ArrayList<>();
        FullBiasNanos = new ArrayList<>();
        BiasNanos = new ArrayList<>();
        BiasUncertaintyNanos = new ArrayList<>();
        DriftNanosPerSecond = new ArrayList<>();
        DriftUncertaintyNanosPerSecond = new ArrayList<>();
        HardwareClockDiscontinuityCount = new ArrayList<>();
        Svid = new ArrayList<>();
        TimeOffsetNanos = new ArrayList<>();
        State = new ArrayList<>();
        ReceivedSvTimeNanos = new ArrayList<>();
        ReceivedSvTimeUncertaintyNanos = new ArrayList<>();
        Cn0DbHz = new ArrayList<>();
        PseudorangeRateMetersPerSecond = new ArrayList<>();
        PseudorangeRateUncertaintyMetersPerSecond = new ArrayList<>();
        AccumulatedDeltaRangeState = new ArrayList<>();
        AccumulatedDeltaRangeMeters = new ArrayList<>();
        AccumulatedDeltaRangeUncertaintyMeters = new ArrayList<>();
        CarrierFrequencyHz = new ArrayList<>();
        CarrierCycles = new ArrayList<>();
        MultipathIndicator = new ArrayList<>();
        ConstellationType = new ArrayList<>();
        AgcDb = new ArrayList<>();
        allRxMillis = new ArrayList<>();
    }

    public GNSSRaw(List<Double> elapsedRealtimeMillis, List<Integer> timeNanos, List<Double> leapSecond,
                   List<Double> timeUncertaintyNanos, List<Integer> fullBiasNanos, List<Double> biasNanos,
                   List<Double> biasUncertaintyNanos, List<Double> driftNanosPerSecond, List<Double> driftUncertaintyNanosPerSecond,
                   List<Double> hardwareClockDiscontinuityCount, List<Double> svid, List<Double> timeOffsetNanos, List<Double> state,
                   List<Integer> receivedSvTimeNanos, List<Integer> receivedSvTimeUncertaintyNanos, List<Double> cn0DbHz,
                   List<Double> pseudorangeRateMetersPerSecond, List<Double> pseudorangeRateUncertaintyMetersPerSecond, List<Double> accumulatedDeltaRangeState,
                   List<Double> accumulatedDeltaRangeMeters, List<Double> accumulatedDeltaRangeUncertaintyMeters, List<Double> carrierFrequencyHz,
                   List<Integer> carrierCycles, List<Double> multipathIndicator, List<Double> constellationType, List<Double> agcDb, List<Integer> allRxMillis) {
        this.ElapsedRealtimeMillis = elapsedRealtimeMillis;
        this.TimeNanos = timeNanos;
        this.LeapSecond = leapSecond;
        this.TimeUncertaintyNanos = timeUncertaintyNanos;
        this.FullBiasNanos = fullBiasNanos;
        this.BiasNanos = biasNanos;
        this.BiasUncertaintyNanos = biasUncertaintyNanos;
        this.DriftNanosPerSecond = driftNanosPerSecond;
        this.DriftUncertaintyNanosPerSecond = driftUncertaintyNanosPerSecond;
        this.HardwareClockDiscontinuityCount = hardwareClockDiscontinuityCount;
        this.Svid = svid;
        this.TimeOffsetNanos = timeOffsetNanos;
        this.State = state;
        this.ReceivedSvTimeNanos = receivedSvTimeNanos;
        this.ReceivedSvTimeUncertaintyNanos = receivedSvTimeUncertaintyNanos;
        this.Cn0DbHz = cn0DbHz;
        this.PseudorangeRateMetersPerSecond = pseudorangeRateMetersPerSecond;
        this.PseudorangeRateUncertaintyMetersPerSecond = pseudorangeRateUncertaintyMetersPerSecond;
        this.AccumulatedDeltaRangeState = accumulatedDeltaRangeState;
        this.AccumulatedDeltaRangeMeters = accumulatedDeltaRangeMeters;
        this.AccumulatedDeltaRangeUncertaintyMeters = accumulatedDeltaRangeUncertaintyMeters;
        this.CarrierFrequencyHz = carrierFrequencyHz;
        this.CarrierCycles = carrierCycles;
        this.MultipathIndicator = multipathIndicator;
        this.ConstellationType = constellationType;
        this.AgcDb = agcDb;
        this.allRxMillis = allRxMillis;
    }
}
