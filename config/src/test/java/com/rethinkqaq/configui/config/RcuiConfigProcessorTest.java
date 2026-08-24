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

package com.rethinkqaq.configui.config;

import com.rethinkqaq.configui.config.processor.RcuiConfigProcessor;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RcuiConfigProcessorTest {
    @Test
    void generatesReadableWrapperForAnnotatedModel() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        Path root = Files.createTempDirectory("rcui-processor");
        Path source = root.resolve("example/ExampleConfigModel.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
            package example;
            @com.rethinkqaq.configui.config.annotation.RcuiConfig(id=\"example\", wrapperName=\"ExampleConfig\")
            public class ExampleConfigModel {
                @com.rethinkqaq.configui.config.annotation.Setting(section=\"general\", min=0, max=100, step=5)
                public int amount = 25;
                @com.rethinkqaq.configui.config.annotation.Setting
                public boolean enabled = true;
            }
            """);
        Path generated = root.resolve("generated");
        Path classes = root.resolve("classes");
        Files.createDirectories(generated);
        Files.createDirectories(classes);
        var fileManager = compiler.getStandardFileManager(null, null, null);
        var units = fileManager.getJavaFileObjects(source.toFile());
        List<String> options = List.of(
            "-processor", RcuiConfigProcessor.class.getName(),
            "-classpath", System.getProperty("java.class.path"),
            "-s", generated.toString(), "-d", classes.toString()
        );
        boolean success = compiler.getTask(null, fileManager, null, options, null, units).call();
        fileManager.close();
        assertTrue(success);
        String wrapper = Files.readString(generated.resolve("example/ExampleConfig.java"));
        assertTrue(wrapper.contains("enabledBinding"));
        assertTrue(wrapper.contains("ConfigValidators.numeric(0.0, 100.0, 5.0)"));
        assertTrue(Files.exists(classes.resolve("example/ExampleConfig.class")));
    }
}
