/*
 * Rethink Config UI Lib
 * Copyright (C) 2026 RethinkQAQ
 *
 * This file is part of Rethink Config UI Lib.
 *
 * Rethink Config UI Lib is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 3 of the License.
 *
 * Rethink Config UI Lib is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Rethink Config UI Lib. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rethinkqaq.configui.config.processor;

import com.rethinkqaq.configui.config.ConfigCodec;
import com.rethinkqaq.configui.config.annotation.RcuiConfig;
import com.rethinkqaq.configui.config.annotation.Setting;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("com.rethinkqaq.configui.config.annotation.RcuiConfig")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class RcuiConfigProcessor extends AbstractProcessor {
    private Types types;
    private Elements elements;
    private Filer filer;
    private final Set<String> generated = new java.util.HashSet<>();

    @Override
    public synchronized void init(javax.annotation.processing.ProcessingEnvironment environment) {
        super.init(environment);
        types = environment.getTypeUtils();
        elements = environment.getElementUtils();
        filer = environment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) return false;
        for (Element element : roundEnvironment.getElementsAnnotatedWith(RcuiConfig.class)) {
            if (!(element instanceof TypeElement model)) continue;
            try { generate(model); }
            catch (GenerationException exception) { error(model, exception.getMessage()); }
            catch (IOException exception) { error(model, "Could not generate configuration wrapper: " + exception.getMessage()); }
        }
        return false;
    }

    private void generate(TypeElement model) throws IOException, GenerationException {
        if (model.getKind() != ElementKind.CLASS || model.getModifiers().contains(Modifier.ABSTRACT)) throw new GenerationException("@RcuiConfig can only annotate a concrete class");
        if (!model.getModifiers().contains(Modifier.PUBLIC)) throw new GenerationException("Configuration model must be public");
        if (ElementFilter.constructorsIn(model.getEnclosedElements()).stream().noneMatch(constructor ->
            constructor.getModifiers().contains(Modifier.PUBLIC) && constructor.getParameters().isEmpty())) {
            throw new GenerationException("Configuration model must have a public no-argument constructor");
        }
        RcuiConfig config = model.getAnnotation(RcuiConfig.class);
        String id = config.id();
        requireId(id, "configuration id");
        String file = config.file().isBlank() ? id + ".yaml" : config.file();
        if (java.nio.file.Paths.get(file).isAbsolute() || file.contains("..")) throw new GenerationException("Configuration file must be a relative path without '..': " + file);
        String wrapperName = config.wrapperName().isBlank() ? model.getSimpleName() + "Config" : config.wrapperName();
        requireJavaName(wrapperName);
        String packageName = elements.getPackageOf(model).getQualifiedName().toString();
        String qualifiedWrapper = packageName.isBlank() ? wrapperName : packageName + "." + wrapperName;
        if (!generated.add(qualifiedWrapper)) return;

        List<FieldInfo> fields = new ArrayList<>();
        Map<String, FieldInfo> paths = new LinkedHashMap<>();
        for (VariableElement field : ElementFilter.fieldsIn(model.getEnclosedElements())) {
            Setting setting = field.getAnnotation(Setting.class);
            if (setting == null) continue;
            if (!field.getModifiers().contains(Modifier.PUBLIC) || field.getModifiers().contains(Modifier.STATIC) || field.getModifiers().contains(Modifier.FINAL)) {
                throw new GenerationException("@Setting field must be public, non-static and non-final: " + field.getSimpleName());
            }
            String section = setting.section().isBlank() ? "general" : setting.section();
            String key = setting.key().isBlank() ? field.getSimpleName().toString() : setting.key();
            requireId(key, "setting key");
            requireId(section, "setting section");
            String path = section + "." + key;
            if (paths.containsKey(path)) throw new GenerationException("Duplicate setting path: " + path);
            validateBounds(field, setting);
            FieldInfo info = new FieldInfo(field, setting, section, path, key, codecExpression(field, setting), valueType(field.asType()), validatorExpression(field, setting));
            fields.add(info);
            paths.put(path, info);
        }

        List<String> migrations = migrationExpressions(config);
        writeWrapper(model, id, file, wrapperName, packageName, config.schemaVersion(), fields, migrations);
    }

    private void writeWrapper(TypeElement model, String id, String file, String wrapperName, String packageName,
                              int schemaVersion, List<FieldInfo> fields, List<String> migrations) throws IOException {
        String qualifiedModel = model.getQualifiedName().toString();
        JavaFileObject source = filer.createSourceFile(packageName.isBlank() ? wrapperName : packageName + "." + wrapperName, model);
        try (Writer out = source.openWriter()) {
            if (!packageName.isBlank()) out.append("package ").append(packageName).append(";\n\n");
            out.append("@javax.annotation.processing.Generated(\"com.rethinkqaq.configui.config.processor.RcuiConfigProcessor\")\n");
            out.append("public final class ").append(wrapperName).append(" implements java.lang.AutoCloseable {\n");
            out.append("    private final ").append(qualifiedModel).append(" model;\n");
            out.append("    private final com.rethinkqaq.configui.config.ConfigSpec spec;\n");
            out.append("    private final com.rethinkqaq.configui.config.YamlConfigStore store;\n");
            for (FieldInfo field : fields) out.append("    private final com.rethinkqaq.configui.config.ConfigValue<").append(field.valueType).append("> ").append(field.name()).append(";\n");
            out.append("\n    private ").append(wrapperName).append("(").append(qualifiedModel).append(" model, java.nio.file.Path path) throws java.io.IOException {\n");
            out.append("        this.model = java.util.Objects.requireNonNull(model, \"model\");\n");
            for (FieldInfo field : fields) {
                out.append("        this.").append(field.name()).append(" = com.rethinkqaq.configui.config.ConfigValue.generated(\"").append(field.path).append("\", \"")
                    .append(field.section).append("\", \"").append(field.key).append("\", \"")
                    .append(escape(field.title())).append("\", \"").append(escape(field.setting.description())).append("\", \"")
                    .append(escape(field.constraints())).append("\", model.").append(field.name()).append(", ")
                    .append(field.codecExpression).append(", ").append(field.validatorExpression).append(", () -> model.").append(field.name())
                    .append(", value -> model.").append(field.name()).append(" = value);\n");
            }
            out.append("        this.spec = com.rethinkqaq.configui.config.ConfigSpec.generated(\"").append(id).append("\", ").append(Integer.toString(schemaVersion)).append(", ");
            out.append(sectionExpression(fields)).append(", ");
            out.append(valueListExpression(fields)).append(", ");
            out.append(migrationListExpression(migrations)).append(");\n");
            out.append("        this.store = com.rethinkqaq.configui.config.YamlConfigStore.open(spec, path);\n");
            out.append("        this.store.load();\n    }\n\n");
            out.append("    public static ").append(wrapperName).append(" createAndLoad(java.nio.file.Path configDirectory) throws java.io.IOException {\n");
            out.append("        java.nio.file.Path path = java.util.Objects.requireNonNull(configDirectory, \"configDirectory\").resolve(\"").append(escape(file)).append("\");\n");
            out.append("        return new ").append(wrapperName).append("(new ").append(qualifiedModel).append("(), path);\n    }\n");
            out.append("    public ").append(qualifiedModel).append(" model() { return model; }\n");
            out.append("    public com.rethinkqaq.configui.config.ConfigSpec spec() { return spec; }\n");
            for (FieldInfo field : fields) {
                out.append("    public com.rethinkqaq.configui.config.ConfigValue<").append(field.valueType).append("> ").append(field.name()).append("() { return ").append(field.name()).append("; }\n");
                out.append("    public com.rethinkqaq.configui.core.UiBinding<").append(field.valueType).append("> ").append(field.name()).append("Binding() { return ").append(field.name()).append(".binding(); }\n");
            }
            out.append("    public void save() throws java.io.IOException { store.save(); }\n");
            out.append("    public void flush() throws java.io.IOException { store.flush(); }\n");
            out.append("    public void reload() throws java.io.IOException { store.load(); }\n");
            out.append("    public void reset() { spec.values().values().forEach(value -> resetValue(value)); }\n");
            out.append("    @SuppressWarnings({\"rawtypes\", \"unchecked\"}) private static void resetValue(com.rethinkqaq.configui.config.ConfigValue value) { value.reset(); }\n");
            out.append("    @Override public void close() throws java.io.IOException { store.close(); }\n");
            out.append("}\n");
        }
    }

    private String sectionExpression(List<FieldInfo> fields) {
        Map<String, String> sections = new LinkedHashMap<>();
        for (FieldInfo field : fields) sections.putIfAbsent(field.section, titleCase(field.section));
        if (sections.isEmpty()) return "java.util.List.of()";
        StringBuilder result = new StringBuilder("java.util.List.of(");
        boolean first = true;
        for (Map.Entry<String, String> section : sections.entrySet()) {
            if (!first) result.append(", ");
            first = false;
            result.append("new com.rethinkqaq.configui.config.ConfigSpec.Section(\"").append(section.getKey()).append("\", \"").append(section.getValue()).append("\", \"\")");
        }
        return result.append(')').toString();
    }

    private String valueListExpression(List<FieldInfo> fields) {
        StringBuilder result = new StringBuilder("java.util.List.of(");
        for (int i = 0; i < fields.size(); i++) { if (i > 0) result.append(", "); result.append("this.").append(fields.get(i).name()); }
        return result.append(')').toString();
    }

    private String migrationListExpression(List<String> migrations) {
        if (migrations.isEmpty()) return "java.util.List.of()";
        return "java.util.List.of(" + String.join(", ", migrations) + ")";
    }

    private List<String> migrationExpressions(RcuiConfig config) throws GenerationException {
        try { config.migrations(); return List.of(); }
        catch (MirroredTypesException exception) {
            List<String> result = new ArrayList<>();
            for (TypeMirror type : exception.getTypeMirrors()) result.add("new " + type + "()");
            return result;
        }
    }

    private String codecExpression(VariableElement field, Setting setting) throws GenerationException {
        String explicitCodec = explicitCodecType(field, setting);
        if (explicitCodec != null && !explicitCodec.equals(ConfigCodec.None.class.getCanonicalName())) {
            validateCodec(explicitCodec);
            return "new " + explicitCodec + "()";
        }
        TypeMirror type = field.asType();
        return builtinCodec(type);
    }

    private String builtinCodec(TypeMirror type) throws GenerationException {
        return switch (type.getKind()) {
            case BOOLEAN -> "com.rethinkqaq.configui.config.ConfigCodecs.BOOLEAN";
            case INT -> "com.rethinkqaq.configui.config.ConfigCodecs.INTEGER";
            case LONG -> "com.rethinkqaq.configui.config.ConfigCodecs.LONG";
            case FLOAT -> "com.rethinkqaq.configui.config.ConfigCodecs.FLOAT";
            case DOUBLE -> "com.rethinkqaq.configui.config.ConfigCodecs.DOUBLE";
            case DECLARED -> declaredCodec((DeclaredType) type);
            default -> throw new GenerationException("Unsupported @Setting type: " + type);
        };
    }

    private String declaredCodec(DeclaredType type) throws GenerationException {
        String name = type.asElement().toString();
        if (name.equals("java.lang.Boolean")) return "com.rethinkqaq.configui.config.ConfigCodecs.BOOLEAN";
        if (name.equals("java.lang.Integer")) return "com.rethinkqaq.configui.config.ConfigCodecs.INTEGER";
        if (name.equals("java.lang.Long")) return "com.rethinkqaq.configui.config.ConfigCodecs.LONG";
        if (name.equals("java.lang.Float")) return "com.rethinkqaq.configui.config.ConfigCodecs.FLOAT";
        if (name.equals("java.lang.Double")) return "com.rethinkqaq.configui.config.ConfigCodecs.DOUBLE";
        if (name.equals("java.lang.String")) return "com.rethinkqaq.configui.config.ConfigCodecs.STRING";
        if (name.equals("java.util.List")) {
            if (type.getTypeArguments().size() != 1) throw new GenerationException("List setting must declare one element type");
            return "com.rethinkqaq.configui.config.ConfigCodecs.listOf(" + builtinCodec(type.getTypeArguments().get(0)) + ")";
        }
        if (type.asElement().getKind() == ElementKind.ENUM) return "com.rethinkqaq.configui.config.ConfigCodecs.enumCodec(" + type + ".class)";
        throw new GenerationException("A custom Codec is required for @Setting type: " + type);
    }

    private String explicitCodecType(VariableElement field, Setting setting) throws GenerationException {
        try { setting.codec(); return ConfigCodec.None.class.getCanonicalName(); }
        catch (MirroredTypeException exception) { return exception.getTypeMirror().toString(); }
    }

    private void validateCodec(String codecName) throws GenerationException {
        TypeElement codec = elements.getTypeElement(codecName);
        TypeElement base = elements.getTypeElement(ConfigCodec.class.getCanonicalName());
        if (codec == null || base == null || !types.isAssignable(types.erasure(codec.asType()), types.erasure(base.asType()))) {
            throw new GenerationException("Codec must implement ConfigCodec: " + codecName);
        }
        if (codec.getModifiers().contains(Modifier.ABSTRACT)
            || ElementFilter.constructorsIn(codec.getEnclosedElements()).stream().noneMatch(constructor ->
                constructor.getModifiers().contains(Modifier.PUBLIC) && constructor.getParameters().isEmpty())) {
            throw new GenerationException("Codec must have a public no-argument constructor: " + codecName);
        }
    }

    private String valueType(TypeMirror type) {
        return switch (type.getKind()) {
            case BOOLEAN -> "java.lang.Boolean";
            case INT -> "java.lang.Integer";
            case LONG -> "java.lang.Long";
            case FLOAT -> "java.lang.Float";
            case DOUBLE -> "java.lang.Double";
            default -> type.toString();
        };
    }

    private String validatorExpression(VariableElement field, Setting setting) throws GenerationException {
        if (setting.step() <= 0.0 && setting.min() == -Double.MAX_VALUE && setting.max() == Double.MAX_VALUE) return "java.util.List.of()";
        if (!isNumber(field.asType())) throw new GenerationException("min, max and step are only valid for numeric @Setting fields");
        double minimum = setting.min() == -Double.MAX_VALUE ? -Double.MAX_VALUE : setting.min();
        double maximum = setting.max() == Double.MAX_VALUE ? Double.MAX_VALUE : setting.max();
        double step = setting.step();
        return "java.util.List.of(com.rethinkqaq.configui.config.ConfigValidators.numeric(" + numberLiteral(minimum) + ", " + numberLiteral(maximum) + ", " + numberLiteral(step) + "))";
    }

    private String constraints(FieldInfo field) {
        Setting setting = field.setting;
        if (setting.step() <= 0.0 && setting.min() == -Double.MAX_VALUE && setting.max() == Double.MAX_VALUE) return "";
        return "range " + numberLiteral(setting.min()) + ".." + numberLiteral(setting.max()) + ", step " + numberLiteral(setting.step());
    }

    private void validateBounds(VariableElement field, Setting setting) throws GenerationException {
        boolean any = setting.step() != 0.0 || setting.min() != -Double.MAX_VALUE || setting.max() != Double.MAX_VALUE;
        if (!any) return;
        if (!isNumber(field.asType())) throw new GenerationException("min, max and step are only valid for numeric @Setting fields");
        if (setting.min() > setting.max() || setting.step() < 0.0 || !Double.isFinite(setting.min()) || !Double.isFinite(setting.max()) || !Double.isFinite(setting.step())) throw new GenerationException("Invalid numeric range or step");
    }

    private boolean isNumber(TypeMirror type) {
        if (switch (type.getKind()) { case INT, LONG, FLOAT, DOUBLE -> true; default -> false; }) return true;
        if (type.getKind() != TypeKind.DECLARED) return false;
        String name = type.toString();
        return name.equals("java.lang.Integer") || name.equals("java.lang.Long")
            || name.equals("java.lang.Float") || name.equals("java.lang.Double");
    }

    private String numberLiteral(double value) { return Double.toString(value); }
    private String titleCase(String value) {
        if (value.isEmpty()) return value;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (i > 0 && Character.isUpperCase(character) && Character.isLowerCase(value.charAt(i - 1))) result.append(' ');
            if (character == '_' || character == '-') result.append(' ');
            else if (i == 0 || value.charAt(i - 1) == '_' || value.charAt(i - 1) == '-') result.append(Character.toUpperCase(character));
            else result.append(character);
        }
        return result.toString();
    }
    private String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
    private void requireId(String value, String label) throws GenerationException { if (value == null || !value.matches("[A-Za-z0-9_-]+")) throw new GenerationException("Invalid " + label + ": " + value); }
    private void requireJavaName(String value) throws GenerationException { if (!value.matches("[A-Za-z_$][A-Za-z0-9_$]*")) throw new GenerationException("Invalid generated wrapper name: " + value); }
    private void error(Element element, String message) { processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element); }

    private final class FieldInfo {
        private final VariableElement field;
        private final Setting setting;
        private final String section;
        private final String path;
        private final String key;
        private final String codecExpression;
        private final String valueType;
        private final String validatorExpression;

        private FieldInfo(VariableElement field, Setting setting, String section, String path, String key, String codecExpression,
                          String valueType, String validatorExpression) {
            this.field = field; this.setting = setting; this.section = section; this.path = path; this.key = key;
            this.codecExpression = codecExpression; this.valueType = valueType; this.validatorExpression = validatorExpression;
        }
        private String name() { return field.getSimpleName().toString(); }
        private String title() { return setting.title().isBlank() ? titleCase(name()) : setting.title(); }
        private String constraints() { return RcuiConfigProcessor.this.constraints(this); }
    }

    private static final class GenerationException extends Exception {
        private GenerationException(String message) { super(message); }
    }
}
