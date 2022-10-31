package org.example;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;
import test.Class1;

public class Main {

    public static void main(String[] args) {
        // write your code here
        Object[] result = null;
        Class1 test = null;
        try {
            test = new Class1();
            result = test.test(3, 5);
            System.out.println(result[0]);
            System.out.println(result[1]);
            System.out.println(result[2]);
        } catch (MWException e) {
            e.printStackTrace();
        } finally {
            MWArray.disposeArray(result);
        }
    }

}

