package cn.byyddyh.dataProcess;

public class DataFilter {

    private static final Integer INDEX_FIRST = 1;
    private static final Integer INDEX_TWO = 8;

    public static boolean nanosCheck(double d) {
        return d != 0;
    }

    public static boolean ConstellationTypeCheck(int i) {
        return i == 1;
    }

    public static boolean stateCheck(Integer d) {
        return ((d & INDEX_FIRST) & (d & INDEX_TWO)) != 0;
    }
}
