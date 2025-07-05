package gj.compiler.minimus;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimus Virtual Machine
 * </p>
 * A very simple mini-vm, with the follow characteristics:
 * </p>
 * <pre>
 * 10 general use registers (program counter, stack pointer, flags, etc. are
 * all simulated):
 *    regs: r0...r9
 *   flags: | -- | -- | ge | gt | le | lt | ne | eq |
 * Commands (opp/source/target): 3 bytes each
 *   PUSH   [ri]                            ; push ri onto stack
 *   POP    [ri]                            ; pop value from stack into ri
 *   LOAD   [ri], [rj]                      ; memory(ri) -> rj
 *   LOADI  [c], [rj]                       ; memory(c) -> rj
 *   STORE  [ri], [rj]                      ; ri -> memory(rj)
 *   STOREI [c], [rj]                       ; c -> memory(rj)
 *   MOV    [ri], [rj]                      ; ri -> rj
 *   MOVI   [c], [rj]                       ; c -> rj
 *   ADD, SUB, MUL, DIV [ri], [rj]          ; ri opp rj -> rj
 *   ADDI, SUBI, MULI, DIVI [c], [rj]       ; c opp rj -> rj
 *   CMP [ri], [rj]                         ; compare ri with rj, sets flags
 *   CMPI [ri], [c]                         ; compare ri with c, sets flags
 *   JMP, JE, JL, JLE, JG, JGE [ri]         ; ri -> pc (based on flag)
 *   SYSCALL
 *   HALT
 *   NOP
 * </pre>
 * </p>
 * Example:
 * <pre>
 * .data        ; starts storing at 0x1000 upwards
 * 1
 * 2
 * .code        ; starts storing from 0x0000 upwards
 * LOADI r1, 4096
 * LOADI r2, 4097
 * :loop
 * ADD r2, r1   ; Add r2 to r1 (r1 = r1 + r2)
 * MOVI 1, r0   ; "1" is the system call for print
 * SYSCALL      ; call the OS
 * CMP r1, 1000 ; compare r1 with a 1000
 * JLE :loop
 * HALT
 * </pre>
 */
public class VirtualMachine {

    private static final int MEM_SIZE = 0xffff;

    private int[] memory = new int[MEM_SIZE];

    public VirtualMachine(String assembly){

    }

    // extract section
    private List<String> extractSection(String section) { return null; }


    // convert opcode and args into 3-byte string
    private int[] decode(String instruction, int ip) {
        return null;
    }

    // load program
    private void load() {

        // get data section
        // for each value
        //      load into memory

        // set instruction pointer

        // get code section
        // for each instruction
        //      decode instruction

    }


}
  