// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可协议”）授权；
* 除非遵守许可协议，否则您不得使用此文件。
* 您可以在以下链接获取许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议，了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import static org.springframework.core.testfixture.io.ResourceTestUtils.qualifiedResource;

/**
 * 测试在并发方式下创建原型Bean的基准测试。
 * 此基准测试需要在运行特定基准测试时，在命令行上自定义工作线程数 {@code -t <int>}，以利用并发性。
 *
 * @author Brian Clozel
 */
@BenchmarkMode(Mode.Throughput)
public class ConcurrentBeanFactoryBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        public DefaultListableBeanFactory factory;

        @Setup
        public void setup() {
            this.factory = new DefaultListableBeanFactory();
            new XmlBeanDefinitionReader(this.factory).loadBeanDefinitions(qualifiedResource(ConcurrentBeanFactoryBenchmark.class, "context.xml"));
            this.factory.addPropertyEditorRegistrar(registry -> registry.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), false)));
        }
    }

    @Benchmark
    public void concurrentBeanCreation(BenchmarkState state, Blackhole bh) {
        bh.consume(state.factory.getBean("bean1"));
        bh.consume(state.factory.getBean("bean2"));
    }

    public static class ConcurrentBean {

        private Date date;

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
