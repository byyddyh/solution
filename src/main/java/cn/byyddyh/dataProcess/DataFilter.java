package cn.byyddyh.dataProcess;

public class DataFilter {

    private static final Integer INDEX_FIRST = 1;
    private static final Integer INDEX_TWO = 8;

    public boolean nanosCheck(long d) {
        return d != 0;
    }

    public boolean ConstellationTypeCheck(long i) {
        return i == 1;
    }

    public boolean stateCheck(long d) {
        return (((d & INDEX_FIRST) != 0) && ((d & INDEX_TWO) != 0));
    }
}
