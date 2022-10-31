package org.example;

public class TestWait {
    public static void main(String[] args) {
        App in = new App();

        new Thread(() -> {
            synchronized (in) {
                System.out.println("a.start....." + System.currentTimeMillis());
                try {
                    in.wait(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("a.end....." + System.currentTimeMillis());
            }
        }, "a").start();

        new Thread(() -> {
            synchronized (in) {
                System.out.println("b.start....." + System.currentTimeMillis());
                try {
                    in.wait(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("b.end....." + System.currentTimeMillis());
            }
        }, "b").start();
    }
}
