package javax0.geci.toolsgen;

import javax0.geci.engine.Geci;
import javax0.geci.api.Source;
import javax0.geci.tools.AbstractJavaGenerator;
import javax0.geci.tools.CompoundParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javax0.geci.api.Source.maven;

public class CopyGenerator extends AbstractJavaGenerator {

    @Override
    public void process(Source source, Class<?> klass, CompoundParams global) throws Exception {
        final var src = source.open();
        final var dstFile = global.get("copyTo");
        final var dst = source.newSource(dstFile).open();
        dst.write("// DO NOT EDIT THIS FILE. THIS WAS GENERATED.");
        dst.write(
            src.getContent().replaceAll("Field", "Method")
                .replaceAll("field", "method"));
    }

    @Override
    public String mnemonic() {
        return "copyClass";
    }

    @Test
    void generateCopies() throws Exception {
        Assertions.assertFalse(
            new Geci().source(maven("..").module("javageci-tools").mainSource()).register( new CopyGenerator()).generate()
            , Geci.FAILED
        );
    }
}
