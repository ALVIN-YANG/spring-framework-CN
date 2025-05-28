// 翻译完成 glm-4-flash
/** 版权所有 2002-2020 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 对Bean定义中的 {@code equals()} 和 {@code hashCode()} 方法进行的单元测试。
 *
 * @author Rob Harrop
 * @author Sam Brannen
 */
@SuppressWarnings("serial")
public class DefinitionMetadataEqualsHashCodeTests {

    @Test
    public void rootBeanDefinition() {
        RootBeanDefinition master = new RootBeanDefinition(TestBean.class);
        RootBeanDefinition equal = new RootBeanDefinition(TestBean.class);
        RootBeanDefinition notEqual = new RootBeanDefinition(String.class);
        RootBeanDefinition subclass = new RootBeanDefinition(TestBean.class) {
        };
        setBaseProperties(master);
        setBaseProperties(equal);
        setBaseProperties(notEqual);
        setBaseProperties(subclass);
        assertEqualsAndHashCodeContracts(master, equal, notEqual, subclass);
    }

    /**
     * 自3.2.8版本以来
     * 参考：<a href="https://jira.spring.io/browse/SPR-11420">SPR-11420</a>
     */
    @Test
    public void rootBeanDefinitionAndMethodOverridesWithDifferentOverloadedValues() {
        RootBeanDefinition master = new RootBeanDefinition(TestBean.class);
        RootBeanDefinition equal = new RootBeanDefinition(TestBean.class);
        setBaseProperties(master);
        setBaseProperties(equal);
        // 模拟 AbstractBeanDefinition.validate() 方法，该方法将委托给
        // AbstractBeanDefinition.prepareMethodOverrides(): 准备方法覆盖。
        master.getMethodOverrides().getOverrides().iterator().next().setOverloaded(false);
        // 但不要模拟验证'equal'对象。因此，一个方法
        // 在 'equal' 方法中重写将会被标记为重载，但相应的
        // 在'master'分支中重写将不会生效。但是...Bean定义仍然应该
        // 视为相等。
        assertThat(equal).as("Should be equal").isEqualTo(master);
        assertThat(equal.hashCode()).as("Hash code for equal instances must match").isEqualTo(master.hashCode());
    }

    @Test
    public void childBeanDefinition() {
        ChildBeanDefinition master = new ChildBeanDefinition("foo");
        ChildBeanDefinition equal = new ChildBeanDefinition("foo");
        ChildBeanDefinition notEqual = new ChildBeanDefinition("bar");
        ChildBeanDefinition subclass = new ChildBeanDefinition("foo") {
        };
        setBaseProperties(master);
        setBaseProperties(equal);
        setBaseProperties(notEqual);
        setBaseProperties(subclass);
        assertEqualsAndHashCodeContracts(master, equal, notEqual, subclass);
    }

    @Test
    public void runtimeBeanReference() {
        RuntimeBeanReference master = new RuntimeBeanReference("name");
        RuntimeBeanReference equal = new RuntimeBeanReference("name");
        RuntimeBeanReference notEqual = new RuntimeBeanReference("someOtherName");
        RuntimeBeanReference subclass = new RuntimeBeanReference("name") {
        };
        assertEqualsAndHashCodeContracts(master, equal, notEqual, subclass);
    }

    private void setBaseProperties(AbstractBeanDefinition definition) {
        definition.setAbstract(true);
        definition.setAttribute("foo", "bar");
        definition.setAutowireCandidate(false);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        // definition.getConstructorArgumentValues().addGenericArgumentValue("foo");```java获取定义的构造函数参数值的集合，并向其中添加一个泛型参数值"foo"。```
        definition.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
        definition.setDependsOn(new String[] { "foo", "bar" });
        definition.setDestroyMethodName("destroy");
        definition.setEnforceDestroyMethod(false);
        definition.setEnforceInitMethod(true);
        definition.setFactoryBeanName("factoryBean");
        definition.setFactoryMethodName("factoryMethod");
        definition.setInitMethodName("init");
        definition.setLazyInit(true);
        definition.getMethodOverrides().addOverride(new LookupOverride("foo", "bar"));
        definition.getMethodOverrides().addOverride(new ReplaceOverride("foo", "bar"));
        definition.getPropertyValues().add("foo", "bar");
        definition.setResourceDescription("desc");
        definition.setRole(BeanDefinition.ROLE_APPLICATION);
        definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        definition.setSource("foo");
    }

    private void assertEqualsAndHashCodeContracts(Object master, Object equal, Object notEqual, Object subclass) {
        assertThat(equal).as("Should be equal").isEqualTo(master);
        assertThat(equal.hashCode()).as("Hash code for equal instances should match").isEqualTo(master.hashCode());
        assertThat(notEqual).as("Should not be equal").isNotEqualTo(master);
        assertThat(notEqual.hashCode()).as("Hash code for non-equal instances should not match").isNotEqualTo(master.hashCode());
        assertThat(subclass).as("Subclass should be equal").isEqualTo(master);
        assertThat(subclass.hashCode()).as("Hash code for subclass should match").isEqualTo(master.hashCode());
    }
}
