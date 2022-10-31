package org.example;

public class ThreadTest {
    private static volatile int flag = 1;
    private static final Object object = new Object();

    public static void main(String[] args){
        new Thread(() -> {
            synchronized (object) {
                while (flag != 1) {
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("a");
                flag = 2;
                object.notifyAll();
            }
        }, "a").start();

        new Thread(() -> {
            synchronized (object) {
                while (flag != 2) {
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("b");
                flag = 3;
                object.notifyAll();
            }
        }, "b").start();

        new Thread(() -> {
            synchronized (object) {
                while (flag != 3) {
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println("c");
                flag = 1;
                object.notifyAll();
            }
        }, "c").start();
    }
}
