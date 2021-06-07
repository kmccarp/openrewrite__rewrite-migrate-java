/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.migrate.lang;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Incubating;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

@Incubating(since = "7.7.0")
public class MigrateClassLoaderDefineClass extends Recipe {
    private static final MethodMatcher DEFINE_CLASS_MATCHER = new MethodMatcher("java.lang.ClassLoader defineClass(byte[], int, int)");

    @Override
    public String getDisplayName() {
        return "Migrates deprecated method java.lang.ClassLoader.defineClass(byte, int, int)";
    }

    @Override
    public String getDescription() {
        return "Replaces the java.lang.ClassLoader deprecated method defineClass(byte, int, int) with defineClass(String, byte, int, int).";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            final JavaTemplate template = template("(null, #{any(byte[])}, #{any(int)}, #{any(int)})")
                    .build();

            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = super.visitMethodInvocation(method, ctx);

                if (DEFINE_CLASS_MATCHER.matches(m) && m.getArguments().size() == 3) {
                    m = method.withTemplate(template,
                            m.getCoordinates().replaceArguments(),
                            m.getArguments().get(0),
                            m.getArguments().get(1),
                            m.getArguments().get(2));
                }
                return m;
            }
        };
    }
}