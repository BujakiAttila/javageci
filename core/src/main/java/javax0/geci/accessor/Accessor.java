package javax0.geci.accessor;

import javax0.geci.api.Segment;
import javax0.geci.api.Source;
import javax0.geci.tools.AbstractGenerator;
import javax0.geci.tools.CompoundParams;
import javax0.geci.tools.Tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Accessor extends AbstractGenerator {
    private static final int PACKAGE = Modifier.PROTECTED | Modifier.PRIVATE | Modifier.PUBLIC;

    public String mnemonic() {
        return "accessor";
    }

    public void process(Source source, Class<?> klass, CompoundParams global) throws Exception {
        var accessMask = mask(global.get("include"));
        var gid = global.get("id");
        source.init(gid);
        final var fields = klass.getDeclaredFields();
        for (final var field : fields) {
            var local = Tools.getParameters(field, mnemonic());
            var isFinal = Modifier.isFinal(field.getModifiers());
            var params = new CompoundParams(local, global);
            if (params.isNot("exclude")) {
                var name = nameAsString(field);
                var capName = cap(name);
                var fieldType = Tools.typeAsString(field);
                var access = params.get("access", "public");
                var only = params.get("only");
                if (matchMask(field, accessMask)) {
                    var id = getId(field, params);
                    source.init(id);
                    try (var segment = source.open(id)) {
                        if (!isFinal && !only.equals("setter")) {
                            writeSetter(name, capName, fieldType, access, segment);
                        }
                        if (!"getter".equals(only)) {
                            writeGetter(name, capName, fieldType, access, segment);
                        }
                    }
                }
            }
        }
    }

    private void writeGetter(String name, String ucName, String type, String access, Segment segment) {
        segment.write_r(access + " " + type + " get" + ucName + "(){");
        segment.write("return " + name + ";");
        segment.write_l("}");
        segment.newline();
    }

    private void writeSetter(String name, String ucName, String type, String access, Segment segment) {
        segment.write_r(access + " void set" + ucName + "(" +
                type + " " + name + "){");
        segment.write("this." + name + " = " + name + ";");
        segment.write_l("}");
        segment.newline();
    }

    private boolean matchMask(Field field, int mask) {
        int modifiers = field.getModifiers();
        if ((mask & Modifier.STRICT) != 0 && (modifiers & PACKAGE) == 0) {
            return true;
        }
        return (modifiers & mask) != 0;
    }

    private int mask(String includes) {
        int modMask = 0;
        if (includes == null) {
            modMask = Modifier.PRIVATE;
        } else {
            for (var exclude : includes.split(",")) {
                if (exclude.trim().equals("private")) {
                    modMask |= Modifier.PRIVATE;
                }
                if (exclude.trim().equals("public")) {
                    modMask |= Modifier.PUBLIC;
                }
                if (exclude.trim().equals("protected")) {
                    modMask |= Modifier.PROTECTED;
                }
                if (exclude.trim().equals("static")) {
                    modMask |= Modifier.STATIC;
                }
                if (exclude.trim().equals("package")) {
                    modMask |= Modifier.STRICT;//reuse the bit
                }
            }
        }
        return modMask;
    }

    private String cap(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String nameAsString(Field field) {
        return field.getName();
    }

    private String getId(Field field, CompoundParams fieldParams) {
        var id = fieldParams.get("id");
        if (id == null) {
            throw new RuntimeException("accessor field " + field + " has no segment id");
        }
        return id;
    }
}
