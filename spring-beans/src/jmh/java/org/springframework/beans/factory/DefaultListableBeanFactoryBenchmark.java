// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 许可协议（以下简称“许可协议”）授权；
* 除非遵守许可协议，否则不得使用此文件。
* 您可以在以下链接获取许可协议副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可协议了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.LifecycleBean;
import org.springframework.beans.testfixture.beans.TestBean;

/**
 * 测试从 {@link DefaultListableBeanFactory} 中检索各种类型 bean 的基准
 *
 * @author Brian Clozel
 */
@BenchmarkMode(Mode.Throughput)
public class DefaultListableBeanFactoryBenchmark {

    public static class Shared {

        public DefaultListableBeanFactory beanFactory;
    }

    @State(Scope.Benchmark)
    public static class PrototypeCreationState extends Shared {

        @Param({ "simple", "dependencyCheck", "constructor", "constructorArgument", "properties", "resolvedProperties" })
        public String mode;

        @Setup
        public void setup() {
            this.beanFactory = new DefaultListableBeanFactory();
            RootBeanDefinition rbd = new RootBeanDefinition(TestBean.class);
            switch(this.mode) {
                case "simple":
                    break;
                case "dependencyCheck":
                    rbd = new RootBeanDefinition(LifecycleBean.class);
                    rbd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
                    this.beanFactory.addBeanPostProcessor(new LifecycleBean.PostProcessor());
                    break;
                case "constructor":
                    rbd.getConstructorArgumentValues().addGenericArgumentValue("juergen");
                    rbd.getConstructorArgumentValues().addGenericArgumentValue("99");
                    break;
                case "constructorArgument":
                    rbd.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("spouse"));
                    this.beanFactory.registerBeanDefinition("test", rbd);
                    this.beanFactory.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class));
                    break;
                case "properties":
                    rbd.getPropertyValues().add("name", "juergen");
                    rbd.getPropertyValues().add("age", "99");
                    break;
                case "resolvedProperties":
                    rbd.getPropertyValues().add("spouse", new RuntimeBeanReference("spouse"));
                    this.beanFactory.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class));
                    break;
            }
            rbd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            this.beanFactory.registerBeanDefinition("test", rbd);
            this.beanFactory.freezeConfiguration();
        }
    }

    @Benchmark
    public Object prototypeCreation(PrototypeCreationState state) {
        return state.beanFactory.getBean("test");
    }

    @State(Scope.Benchmark)
    public static class SingletonLookupState extends Shared {

        @Setup
        public void setup() {
            this.beanFactory = new DefaultListableBeanFactory();
            this.beanFactory.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
            this.beanFactory.freezeConfiguration();
        }
    }

    @Benchmark
    public Object singletLookup(SingletonLookupState state) {
        return state.beanFactory.getBean("test");
    }

    @Benchmark
    public Object singletLookupByType(SingletonLookupState state) {
        return state.beanFactory.getBean(TestBean.class);
    }

    @State(Scope.Benchmark)
    public static class SingletonLookupManyBeansState extends Shared {

        @Setup
        public void setup() {
            this.beanFactory = new DefaultListableBeanFactory();
            this.beanFactory.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
            for (int i = 0; i < 1000; i++) {
                this.beanFactory.registerBeanDefinition("a" + i, new RootBeanDefinition(A.class));
            }
            this.beanFactory.freezeConfiguration();
        }
    }

    // 参见 SPR-6870
    @Benchmark
    public Object singletLookupByTypeManyBeans(SingletonLookupState state) {
        return state.beanFactory.getBean(B.class);
    }

    static class A {
    }

    static class B {
    }
}
