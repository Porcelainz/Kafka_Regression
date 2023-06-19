package main;
public class HeapSize {
    public static void main(String[] args) {
        // Get the current JVM heap size
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("Current JVM Heap Size: " + heapSize + " bytes");

        // Get the maximum JVM heap size
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        System.out.println("Maximum JVM Heap Size: " + maxHeapSize + " bytes");
    }
}