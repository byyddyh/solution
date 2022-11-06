package cn.byyddyh.dataModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Iono {
    public List<String> alpha;
    public List<String> beta;

    public Iono() {
        alpha = new ArrayList<>();
        beta = new ArrayList<>();
    }

    public Iono(List<String> alpha, List<String> beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public String toString() {
        return "Iono{" +
                "alpha=" + alpha +
                ", beta=" + beta +
                '}';
    }
}
