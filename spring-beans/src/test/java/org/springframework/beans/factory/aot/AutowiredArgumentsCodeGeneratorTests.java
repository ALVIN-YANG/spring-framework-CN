// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”）许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是“按原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体规定许可和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试 {@link AutowiredArgumentsCodeGenerator}。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
class AutowiredArgumentsCodeGeneratorTests {

    @Test
    void generateCodeWhenNoArguments() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "zero");
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("");
    }

    @Test
    void generatedCodeWhenSingleArgument() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "one", String.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("args.get(0)");
    }

    @Test
    void generateCodeWhenMultipleArguments() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "three", String.class, Integer.class, Boolean.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("args.get(0), args.get(1), args.get(2)");
    }

    @Test
    void generateCodeWhenMultipleArgumentsWithOffset() {
        Constructor<?> constructor = Outer.Nested.class.getDeclaredConstructors()[0];
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(Outer.Nested.class, constructor);
        assertThat(generator.generateCode(constructor.getParameterTypes(), 1)).hasToString("args.get(0), args.get(1)");
    }

    @Test
    void generateCodeWhenAmbiguousConstructor() throws Exception {
        Constructor<?> constructor = AmbiguousConstructors.class.getDeclaredConstructor(String.class, Integer.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(AmbiguousConstructors.class, constructor);
        assertThat(generator.generateCode(constructor.getParameterTypes())).hasToString("args.get(0, java.lang.String.class), args.get(1, java.lang.Integer.class)");
    }

    @Test
    void generateCodeWhenUnambiguousConstructor() throws Exception {
        Constructor<?> constructor = UnambiguousConstructors.class.getDeclaredConstructor(String.class, Integer.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousConstructors.class, constructor);
        assertThat(generator.generateCode(constructor.getParameterTypes())).hasToString("args.get(0), args.get(1)");
    }

    @Test
    void generateCodeWhenAmbiguousMethod() {
        Method method = ReflectionUtils.findMethod(AmbiguousMethods.class, "two", String.class, Integer.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(AmbiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("args.get(0, java.lang.String.class), args.get(1, java.lang.Integer.class)");
    }

    @Test
    void generateCodeWhenAmbiguousSubclassMethod() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "two", String.class, Integer.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(AmbiguousSubclassMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("args.get(0, java.lang.String.class), args.get(1, java.lang.Integer.class)");
    }

    @Test
    void generateCodeWhenUnambiguousMethod() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "two", String.class, Integer.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes())).hasToString("args.get(0), args.get(1)");
    }

    @Test
    void generateCodeWithCustomArgVariable() {
        Method method = ReflectionUtils.findMethod(UnambiguousMethods.class, "one", String.class);
        AutowiredArgumentsCodeGenerator generator = new AutowiredArgumentsCodeGenerator(UnambiguousMethods.class, method);
        assertThat(generator.generateCode(method.getParameterTypes(), 0, "objs")).hasToString("objs.get(0)");
    }

    static class Outer {

        class Nested {

            Nested(String a, Integer b) {
            }
        }
    }

    static class UnambiguousMethods {

        void zero() {
        }

        void one(String a) {
        }

        void two(String a, Integer b) {
        }

        void three(String a, Integer b, Boolean c) {
        }
    }

    static class AmbiguousMethods {

        void two(String a, Integer b) {
        }

        void two(Integer b, String a) {
        }
    }

    static class AmbiguousSubclassMethods extends UnambiguousMethods {

        void two(Integer a, String b) {
        }
    }

    static class UnambiguousConstructors {

        UnambiguousConstructors() {
        }

        UnambiguousConstructors(String a) {
        }

        UnambiguousConstructors(String a, Integer b) {
        }
    }

    static class AmbiguousConstructors {

        AmbiguousConstructors(String a, Integer b) {
        }

        AmbiguousConstructors(Integer b, String a) {
        }
    }
}
