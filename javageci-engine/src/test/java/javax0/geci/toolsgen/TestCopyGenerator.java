package javax0.geci.toolsgen;

import javax0.geci.api.Source;
import javax0.geci.engine.Geci;
import javax0.geci.tools.AbstractFieldsGenerator;
import javax0.geci.tools.AbstractJavaGenerator;
import javax0.geci.tools.CompoundParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax0.geci.api.Source.maven;

/**
 * A simple sample code generator that copies the {@link AbstractFieldsGenerator} and
 * {@link javax0.geci.tools.AbstractFilteredFieldsGenerator} to the same name but replacing all fields to be methods.
 */
public class TestCopyGenerator extends AbstractJavaGenerator {

    @Override
    public void process(Source source, Class<?> klass, CompoundParams global) {
        final var dstFile = global.get("copyTo");
        final var dst = source.newSource(dstFile).open();
        dst.write("// DO NOT EDIT THIS FILE. THIS WAS GENERATED by TestCopyGenerator.java");
        dst.write(
            String.join("\n", source.getLines())
                .replaceAll("Field", "Method")
                .replaceAll("field", "method")
                // we have to remove the annotation otherwise the next run would copy the content of the files to
                // themselves and they would grow infinitely and also they would not compile.
                .replaceAll("@Geci\\(\"copyClass.*?\"\\)", "")
                .replaceAll("import\\s+javax0\\.geci\\.annotations\\.Geci;","")
        );
    }

    @Override
    public String mnemonic() {
        return "copyClass";
    }

    @Test
    void generateCopies() throws Exception {
        Geci geci = new Geci();
        Assertions.assertFalse(
            geci
                .source(maven("..").module("javageci-tools").mainSource())
                .only("FieldsGenerator.java$")
                .register(new TestCopyGenerator())
                .generate()
            , geci.failed()
        );
    }
}
