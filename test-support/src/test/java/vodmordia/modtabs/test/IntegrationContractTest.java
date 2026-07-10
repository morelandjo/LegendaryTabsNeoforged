package vodmordia.modtabs.test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies reflection and mixin assumptions without loading optional mod classes. */
final class IntegrationContractTest {
    private static final Pattern ENUM_ID = Pattern.compile("^[ \\t]*[A-Z][A-Z0-9_]*\\(\\\"([^\\\"]+)\\\"", Pattern.MULTILINE);

    record Contract(String mode, String modId, String className, String member, String descriptor, String note) {
        String displayName() {
            return modId + (className.isBlank() ? " (documented)" : " -> " + className + (member.isBlank() ? "" : "#" + member));
        }
    }

    private static List<Contract> contracts() throws Exception {
        Path manifest = Path.of(System.getProperty("modtabs.contracts"));
        List<Contract> result = new ArrayList<>();
        for (String raw : Files.readAllLines(manifest)) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = raw.split("\\|", -1);
            assertEquals(6, fields.length, "Contract rows must have six pipe-delimited fields: " + raw);
            result.add(new Contract(fields[0].strip(), fields[1].strip(), fields[2].strip(),
                    fields[3].strip(), fields[4].strip(), fields[5].strip()));
        }
        return result;
    }

    @Test
    void everyIntegrationIsRepresentedInTheManifest() throws Exception {
        String source = Files.readString(Path.of(System.getProperty("modtabs.integrationEnum")));
        Matcher matcher = ENUM_ID.matcher(source);
        Set<String> declared = new HashSet<>();
        while (matcher.find()) declared.add(matcher.group(1));

        Set<String> covered = new HashSet<>();
        for (Contract contract : contracts()) covered.add(contract.modId);
        assertEquals(declared, covered,
                "Update integration-contracts.txt whenever ModIntegration changes. " +
                "Use REQUIRED for dependencies present in this test target or DEFERRED with a reason.");
    }

    @TestFactory
    Stream<DynamicTest> contractsMatchResolvedModBytecode() throws Exception {
        return contracts().stream().map(contract -> DynamicTest.dynamicTest(contract.displayName(), () -> verify(contract)));
    }

    private static void verify(Contract contract) throws Exception {
        assertTrue(contract.mode.equals("REQUIRED") || contract.mode.equals("DEFERRED"), "Unknown mode " + contract.mode);
        if (contract.mode.equals("DEFERRED")) {
            assertFalse(contract.note.isBlank(), "Deferred contracts must explain why they cannot yet be checked");
            return;
        }
        assertFalse(contract.className.isBlank(), "Required contracts need a class");
        String resource = contract.className.replace('.', '/') + ".class";
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, "Missing integration class " + contract.className + " on the resolved compile classpath");
            if (contract.member.isBlank()) return;
            MemberFinder finder = new MemberFinder(contract.member, contract.descriptor);
            new ClassReader(input).accept(finder, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            assertTrue(finder.found, "Missing member " + contract.className + "#" + contract.member +
                    (contract.descriptor.isBlank() ? "" : " " + contract.descriptor));
        }
    }

    private static final class MemberFinder extends ClassVisitor {
        private final String name;
        private final String descriptor;
        private boolean found;

        private MemberFinder(String name, String descriptor) {
            super(Opcodes.ASM9);
            this.name = name;
            this.descriptor = descriptor;
        }

        private boolean matches(String candidateName, String candidateDescriptor) {
            return name.equals(candidateName) && (descriptor.isBlank() || descriptor.equals(candidateDescriptor));
        }

        @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            if (matches(name, descriptor)) found = true;
            return null;
        }

        @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (matches(name, descriptor)) found = true;
            return null;
        }
    }
}
