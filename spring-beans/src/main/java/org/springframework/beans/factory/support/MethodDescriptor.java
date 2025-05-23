// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能不得使用此文件除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、适用于特定目的和不侵犯知识产权。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.support;

import org.springframework.util.ClassUtils;

/**
 * 用于描述一个 {@link java.lang.reflect.Method 方法} 的描述符，该描述符包含方法的声明类、方法名和参数类型。
 *
 * @param declaringClass 方法的声明类
 * @param methodName 方法名
 * @param parameterTypes 方法接受的参数类型
 * @author Sam Brannen
 * @since 6.0.11
 */
record MethodDescriptor(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {

    /**
     * 为提供的bean类和方法名创建一个{@link MethodDescriptor}。
     * <p>提供的{@code methodName}可以是{@linkplain Method#getName() 简单方法名}或一个
     * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method) 完整方法名}。
     * <p>如果方法名是完整的，此实用程序将解析方法名及其声明类，然后尝试使用提供的{@code beanClass}的{@link ClassLoader}来加载方法的声明类。否则，返回的描述符将引用提供的{@code beanClass}和{@code methodName}。
     * @param beanName 工厂中的bean名称（用于调试目的）
     * @param beanClass bean类
     * @param methodName 方法名称
     * @return 一个新的{@code MethodDescriptor}；永远不会为{@code null}
     */
    static MethodDescriptor create(String beanName, Class<?> beanClass, String methodName) {
        try {
            Class<?> declaringClass = beanClass;
            String methodNameToUse = methodName;
            // 如果需要，解析完全限定的方法名。
            int indexOfDot = methodName.lastIndexOf('.');
            if (indexOfDot > 0) {
                String className = methodName.substring(0, indexOfDot);
                methodNameToUse = methodName.substring(indexOfDot + 1);
                if (!beanClass.getName().equals(className)) {
                    declaringClass = ClassUtils.forName(className, beanClass.getClassLoader());
                }
            }
            return new MethodDescriptor(declaringClass, methodNameToUse);
        } catch (Exception | LinkageError ex) {
            throw new BeanDefinitionValidationException("Could not create MethodDescriptor for method '%s' on bean with name '%s': %s".formatted(methodName, beanName, ex.getMessage()));
        }
    }
}
