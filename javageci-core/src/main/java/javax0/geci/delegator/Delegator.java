package javax0.geci.delegator;

import javax0.geci.annotations.Generated;
import javax0.geci.api.Segment;
import javax0.geci.api.Source;
import javax0.geci.core.annotations.AnnotationBuilder;
import javax0.geci.tools.AbstractFilteredFieldsGenerator;
import javax0.geci.tools.CompoundParams;
import javax0.geci.tools.GeciReflectionTools;
import javax0.geci.tools.MethodTool;
import javax0.geci.tools.reflection.Selector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AnnotationBuilder
public class Delegator extends AbstractFilteredFieldsGenerator {
    private static class Config {
        /**
         * - Config
         *
         * * `{{configVariableName}}` can be used to define the annotation
         * to mark the methods that are generated by the generator. When
         * the generator runs it looks up the methods of the class or
         * interface of the field upon which it has to delegate and then
         * it tries to find it in the code. If the method is not in the
         * currently compiled version of the class it has to be created.
         * If it is there and it has the `Generated` annotation then it
         * has to be regenerated. If the code is the same then the
         * framework will take care of not overwriting the code with
         * something that is identical. However, if the method is there
         * already but it is NOT annotated with the annotation
         * `Generated` then it means the method was created manually by
         * the programmer. In this case it must not be generated.
         *
         *   The already available `Generated` annotations have compile
         * retention and they are not available when the test runs for
         * reflective access. Thus there is a need for a new `Generated`
         * annotation. If the program already has one then it can be
         * used instead of the one provided by the Geci framework. It
         * can be configured in the builder of the generator.
         */
        private Class<? extends Annotation> generatedAnnotation = Generated.class;

        /**
         *-
         *
         * * `{{configVariableName}}` can filter out some of the fields from being
         * delegated. The default is `{{configDefaultValue}}` a.k.a. all fields that
         * are not static are candidate for delegation. If there are
         * many fields in a class and only a few need delegation then
         * the simplest strategy is to set this parameter to `false` on
         * the class annotation and `true` on each individual field that
         * needs delegation.
         */
        private String filter = "!static";

        /**
         * -
         *
         * * `{{configVariableName}}` is a filter expression that
         * selects the methods that are to be delegated. Because the
         * methods themselves may not be annotated and configured
         * because they may happen to be in a class, the delegated class
         * for which the source code is not there (e.g.: Map in the JDK)
         * the only way to select the methods that are needed to be
         * delegated are filter expressions. The configuration parameter
         * `filter` is already used to filter the methods that need
         * delegation, thus the methods in the delegated class or
         * interface should use a different configuration parameter:
         * `{{configVariableName}}`. The default value for this
         * parameter is `{{configDefaultValue}}`.
         *
         *
         */
        private String methods = "public & !static";
    }

    @Override
    public void process(Source source, Class<?> klass, CompoundParams params, Field field, Segment segment) {
        final var name = field.getName();
        final var local = localConfig(params);
        final List<Method> methods = Arrays.stream(GeciReflectionTools.getDeclaredMethodsSorted(field.getType()))
                .filter(Selector.compile(local.methods)::match)
                .collect(Collectors.toList());
        for (final var method : methods) {
            if (!manuallyCoded(klass, method)) {
                writeGenerated(segment, config.generatedAnnotation);
                segment.write_r(MethodTool.with(method).signature() + " {")
                        .write((isVoid(method) ? "" : "return ") + name + "." + MethodTool.with(method).call() + ";")
                        .write_l("}")
                        .newline();
            }
        }
    }

    private static boolean isVoid(Method method) {
        return "void".equals(method.getReturnType().getName());
    }

    private boolean manuallyCoded(Class<?> klass, Method method) {
        try {
            var localMethod = klass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return localMethod.getDeclaredAnnotation(config.generatedAnnotation) == null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    protected String defaultFilterExpression() {
        return config.filter;
    }

    //<editor-fold id="configBuilder" configurableMnemonic="delegator">
    private String configuredMnemonic = "delegator";

    @Override
    public String mnemonic(){
        return configuredMnemonic;
    }

    private final Config config = new Config();
    public static Delegator.Builder builder() {
        return new Delegator().new Builder();
    }

    private static final java.util.Set<String> implementedKeys = java.util.Set.of(
        "filter",
        "methods",
        "id"
    );

    @Override
    public java.util.Set<String> implementedKeys() {
        return implementedKeys;
    }
    public class Builder {
        public Builder filter(String filter) {
            config.filter = filter;
            return this;
        }

        public Builder generatedAnnotation(Class<? extends java.lang.annotation.Annotation> generatedAnnotation) {
            config.generatedAnnotation = generatedAnnotation;
            return this;
        }

        public Builder methods(String methods) {
            config.methods = methods;
            return this;
        }

        public Builder mnemonic(String mnemonic) {
            configuredMnemonic = mnemonic;
            return this;
        }

        public Delegator build() {
            return Delegator.this;
        }
    }
    private Config localConfig(CompoundParams params){
        final var local = new Config();
        local.filter = params.get("filter",config.filter);
        local.generatedAnnotation = config.generatedAnnotation;
        local.methods = params.get("methods",config.methods);
        return local;
    }
    //</editor-fold>
}
