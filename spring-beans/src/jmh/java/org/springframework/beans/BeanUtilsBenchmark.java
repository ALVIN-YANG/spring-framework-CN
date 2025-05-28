// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的条款。*/
package org.springframework.beans;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BeanUtilsBenchmark {

    private Constructor<TestClass1> noArgConstructor;

    private Constructor<TestClass2> constructor;

    @Setup
    public void setUp() throws NoSuchMethodException {
        this.noArgConstructor = TestClass1.class.getDeclaredConstructor();
        this.constructor = TestClass2.class.getDeclaredConstructor(int.class, String.class);
    }

    @Benchmark
    public Object emptyConstructor() {
        return BeanUtils.instantiateClass(this.noArgConstructor);
    }

    @Benchmark
    public Object nonEmptyConstructor() {
        return BeanUtils.instantiateClass(this.constructor, 1, "str");
    }

    static class TestClass1 {
    }

    @SuppressWarnings("unused")
    static class TestClass2 {

        private final int value1;

        private final String value2;

        TestClass2(int value1, String value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
    }
}
