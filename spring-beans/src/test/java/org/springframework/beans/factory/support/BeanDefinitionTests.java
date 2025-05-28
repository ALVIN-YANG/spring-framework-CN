// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证，
* 无论是否明确声明或暗示。有关许可的特定语言管理权限和限制，
* 请参阅许可证。*/
package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.testfixture.beans.TestBean;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @作者 Juergen Hoeller
 */
public class BeanDefinitionTests {

    @Test
    public void beanDefinitionEquality() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.setAbstract(true);
        bd.setLazyInit(true);
        bd.setScope("request");
        RootBeanDefinition otherBd = new RootBeanDefinition(TestBean.class);
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.setAbstract(true);
        otherBd.setLazyInit(true);
        otherBd.setScope("request");
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
    }

    @Test
    public void beanDefinitionEqualityWithPropertyValues() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.getPropertyValues().add("name", "myName");
        bd.getPropertyValues().add("age", "99");
        RootBeanDefinition otherBd = new RootBeanDefinition(TestBean.class);
        otherBd.getPropertyValues().add("name", "myName");
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getPropertyValues().add("age", "11");
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getPropertyValues().add("age", "99");
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
    }

    @Test
    public void beanDefinitionEqualityWithConstructorArguments() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.getConstructorArgumentValues().addGenericArgumentValue("test");
        bd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5);
        RootBeanDefinition otherBd = new RootBeanDefinition(TestBean.class);
        otherBd.getConstructorArgumentValues().addGenericArgumentValue("test");
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getConstructorArgumentValues().addIndexedArgumentValue(1, 9);
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5);
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
    }

    @Test
    public void beanDefinitionEqualityWithTypedConstructorArguments() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.getConstructorArgumentValues().addGenericArgumentValue("test", "int");
        bd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5, "long");
        RootBeanDefinition otherBd = new RootBeanDefinition(TestBean.class);
        otherBd.getConstructorArgumentValues().addGenericArgumentValue("test", "int");
        otherBd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5);
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5, "int");
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5, "long");
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
    }

    @Test
    public void genericBeanDefinitionEquality() {
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setParentName("parent");
        bd.setScope("request");
        bd.setAbstract(true);
        bd.setLazyInit(true);
        GenericBeanDefinition otherBd = new GenericBeanDefinition();
        otherBd.setScope("request");
        otherBd.setAbstract(true);
        otherBd.setLazyInit(true);
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.setParentName("parent");
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
        bd.getPropertyValues();
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
        bd.getConstructorArgumentValues();
        assertThat(bd.equals(otherBd)).isTrue();
        assertThat(otherBd.equals(bd)).isTrue();
        assertThat(bd.hashCode()).isEqualTo(otherBd.hashCode());
    }

    @Test
    public void beanDefinitionHolderEquality() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.setAbstract(true);
        bd.setLazyInit(true);
        bd.setScope("request");
        BeanDefinitionHolder holder = new BeanDefinitionHolder(bd, "bd");
        RootBeanDefinition otherBd = new RootBeanDefinition(TestBean.class);
        assertThat(!bd.equals(otherBd)).isTrue();
        assertThat(!otherBd.equals(bd)).isTrue();
        otherBd.setAbstract(true);
        otherBd.setLazyInit(true);
        otherBd.setScope("request");
        BeanDefinitionHolder otherHolder = new BeanDefinitionHolder(bd, "bd");
        assertThat(holder.equals(otherHolder)).isTrue();
        assertThat(otherHolder.equals(holder)).isTrue();
        assertThat(holder.hashCode()).isEqualTo(otherHolder.hashCode());
    }

    @Test
    public void beanDefinitionMerging() {
        RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
        bd.getConstructorArgumentValues().addGenericArgumentValue("test");
        bd.getConstructorArgumentValues().addIndexedArgumentValue(1, 5);
        bd.getPropertyValues().add("name", "myName");
        bd.getPropertyValues().add("age", "99");
        bd.setQualifiedElement(getClass());
        GenericBeanDefinition childBd = new GenericBeanDefinition();
        childBd.setParentName("bd");
        RootBeanDefinition mergedBd = new RootBeanDefinition(bd);
        mergedBd.overrideFrom(childBd);
        assertThat(mergedBd.getConstructorArgumentValues().getArgumentCount()).isEqualTo(2);
        assertThat(mergedBd.getPropertyValues()).hasSize(2);
        assertThat(mergedBd).isEqualTo(bd);
        mergedBd.getConstructorArgumentValues().getArgumentValue(1, null).setValue(9);
        assertThat(bd.getConstructorArgumentValues().getArgumentValue(1, null).getValue()).isEqualTo(5);
        assertThat(bd.getQualifiedElement()).isEqualTo(getClass());
    }
}
