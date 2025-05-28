// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0 ("许可协议") 许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下地址获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的质量保证或适用性保证；
* 请参阅许可协议以了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

/**
 * 对在Bean中使用{@link AbstractPropertyAccessor}的基准测试。
 *
 * @author Brian Clozel
 */
@BenchmarkMode(Mode.Throughput)
public class AbstractPropertyAccessorBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param({ "DirectFieldAccessor", "BeanWrapper" })
        public String accessor;

        @Param({ "none", "stringTrimmer", "numberOnPath", "numberOnNestedPath", "numberOnType" })
        public String customEditor;

        public int[] input;

        public PrimitiveArrayBean target;

        public AbstractPropertyAccessor propertyAccessor;

        @Setup
        public void setup() {
            this.target = new PrimitiveArrayBean();
            this.input = new int[1024];
            if (this.accessor.equals("DirectFieldAccessor")) {
                this.propertyAccessor = new DirectFieldAccessor(this.target);
            } else {
                this.propertyAccessor = new BeanWrapperImpl(this.target);
            }
            switch(this.customEditor) {
                case "stringTrimmer" ->
                    this.propertyAccessor.registerCustomEditor(String.class, new StringTrimmerEditor(false));
                case "numberOnPath" ->
                    this.propertyAccessor.registerCustomEditor(int.class, "array.somePath", new CustomNumberEditor(Integer.class, false));
                case "numberOnNestedPath" ->
                    this.propertyAccessor.registerCustomEditor(int.class, "array[0].somePath", new CustomNumberEditor(Integer.class, false));
                case "numberOnType" ->
                    this.propertyAccessor.registerCustomEditor(int.class, new CustomNumberEditor(Integer.class, false));
            }
        }
    }

    @Benchmark
    public PrimitiveArrayBean setPropertyValue(BenchmarkState state) {
        state.propertyAccessor.setPropertyValue("array", state.input);
        return state.target;
    }

    @SuppressWarnings("unused")
    private static class PrimitiveArrayBean {

        private int[] array;

        public int[] getArray() {
            return this.array;
        }

        public void setArray(int[] array) {
            this.array = array;
        }
    }
}
