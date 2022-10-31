package org.example;

public class Solution {

    static class A {

    }

    static class B extends A {

    }

    static class C extends A {

    }

    public static void main(String[] args) {
        A b = new B();
        if (b instanceof B) {
            System.out.println("b is B class");
        } else {
            System.out.println("b is not B class");
        }

        A c = new C();
        if (c instanceof B) {
            System.out.println("c is B class");
        } else {
            System.out.println("c is not B class");
        }
    }
}
