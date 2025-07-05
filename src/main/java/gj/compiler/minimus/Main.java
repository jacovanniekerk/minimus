package gj.compiler.minimus;

/**
 * Minimus (most basic), Minimus++ (slightly more advanced)
 *
 * Using standard memory model for vm.
 *
 * (see <a href="https://www.geeksforgeeks.org/c/memory-layout-of-c-program/">memory-layout-of-c-program</a>)
 * <pre>>
 * +--------------------------+ High address (0xffff)
 * | Stack (grows downward)   |
 * +--------------------------+
 * |                          |
 * | Free space...            |
 * |                          |
 * +--------------------------|
 * | Heap (grows upward)      |
 * +--------------------------+
 * | Uninitialised data (bss) |
 * + -------------------------+
 * | Initialised data         |
 * +--------------------------+
 * | Text (code segment)      |
 * +--------------------------+ Low address (0x0000)
 * </pre>
 *
 */
public class Main {

    public static void test(String a, int... b) {
        System.out.println(a);
        for (int bb : b) System.out.println("  " + bb);
    }

    public static void main(String[] args) {
        System.out.println("Minimus 1.0"); test("hello");
    }
}
