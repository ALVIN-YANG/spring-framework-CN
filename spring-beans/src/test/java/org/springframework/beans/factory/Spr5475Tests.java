// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可协议") 进行许可；
* 除非符合许可协议，否则不得使用此文件。
* 您可以在以下链接处获得许可协议的副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可协议下分发的软件
* 是按“现状”提供的，不提供任何明示或暗示的保证或条件。
* 请参阅许可协议以了解具体管理许可和限制的语言。*/
package org.springframework.beans.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * SPR-5475 揭示了这样一个事实：当错误地调用工厂方法时，显示的错误信息对用户来说不具有指导性，反而具有误导性。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 */
public class Spr5475Tests {

    @Test
    public void noArgFactoryMethodInvokedWithOneArg() {
        assertExceptionMessageForMisconfiguredFactoryMethod(rootBeanDefinition(Foo.class).setFactoryMethod("noArgFactory").addConstructorArgValue("bogusArg").getBeanDefinition(), "Error creating bean with name 'foo': No matching factory method found on class " + "[org.springframework.beans.factory.Spr5475Tests$Foo]: factory method 'noArgFactory(String)'. " + "Check that a method with the specified name and arguments exists and that it is static.");
    }

    @Test
    public void noArgFactoryMethodInvokedWithTwoArgs() {
        assertExceptionMessageForMisconfiguredFactoryMethod(rootBeanDefinition(Foo.class).setFactoryMethod("noArgFactory").addConstructorArgValue("bogusArg1").addConstructorArgValue("bogusArg2".getBytes()).getBeanDefinition(), "Error creating bean with name 'foo': No matching factory method found on class " + "[org.springframework.beans.factory.Spr5475Tests$Foo]: factory method 'noArgFactory(String,byte[])'. " + "Check that a method with the specified name and arguments exists and that it is static.");
    }

    @Test
    public void noArgFactoryMethodInvokedWithTwoArgsAndTypesSpecified() {
        RootBeanDefinition def = new RootBeanDefinition(Foo.class);
        def.setFactoryMethodName("noArgFactory");
        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        cav.addIndexedArgumentValue(0, "bogusArg1", CharSequence.class.getName());
        cav.addIndexedArgumentValue(1, "bogusArg2".getBytes());
        def.setConstructorArgumentValues(cav);
        assertExceptionMessageForMisconfiguredFactoryMethod(def, "Error creating bean with name 'foo': No matching factory method found on class " + "[org.springframework.beans.factory.Spr5475Tests$Foo]: factory method 'noArgFactory(CharSequence,byte[])'. " + "Check that a method with the specified name and arguments exists and that it is static.");
    }

    private void assertExceptionMessageForMisconfiguredFactoryMethod(BeanDefinition bd, String expectedMessage) {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("foo", bd);
        assertThatExceptionOfType(BeanCreationException.class).isThrownBy(factory::preInstantiateSingletons).withMessageContaining(expectedMessage);
    }

    @Test
    public void singleArgFactoryMethodInvokedWithNoArgs() {
        // 调用一个接受参数的工厂方法，如果没有提供任何参数，则会抛出异常，这与其他情况不同。
        // 这里调用了一个无参数的工厂方法时传入了参数。添加这个测试只是为了记录这种差异。
        assertExceptionMessageForMisconfiguredFactoryMethod(rootBeanDefinition(Foo.class).setFactoryMethod("singleArgFactory").getBeanDefinition(), "Error creating bean with name 'foo': " + "Unsatisfied dependency expressed through method 'singleArgFactory' parameter 0: " + "Ambiguous argument values for parameter of type [java.lang.String] - " + "did you specify the correct bean references as arguments?");
    }

    static class Foo {

        static Foo noArgFactory() {
            return new Foo();
        }

        static Foo singleArgFactory(String arg) {
            return new Foo();
        }
    }
}
