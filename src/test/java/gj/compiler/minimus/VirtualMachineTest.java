package gj.compiler.minimus;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class VirtualMachineTest {

    private String readTestFile(String filename) throws IOException, URISyntaxException {
        File file = new File(Objects.requireNonNull(getClass().getResource(filename)).toURI());
        try (FileInputStream inputStream = new FileInputStream(file)){
            return new String(inputStream.readAllBytes());
        }
    }

    @Test
    public void testMachine() throws IOException, URISyntaxException {
        String program = readTestFile("testcase1");
        VirtualMachine vm = new VirtualMachine();
        vm.run(program);

    }

}
