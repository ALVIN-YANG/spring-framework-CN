// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非符合许可证规定，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体的管理权限和限制。*/
package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ReflectionUtils;

/**
 * {@link MethodInvoker} 的子类，尝试通过一个 {@link TypeConverter} 将给定的参数转换为实际目标方法的参数。
 *
 * <p>支持灵活的参数转换，特别是用于调用特定重载方法的情况。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.BeanWrapperImpl#convertIfNecessary
 */
public class ArgumentConvertingMethodInvoker extends MethodInvoker {

    @Nullable
    private TypeConverter typeConverter;

    private boolean useDefaultConverter = true;

    /**
     * 设置用于参数类型转换的TypeConverter。
     * <p>默认使用的是{@link org.springframework.beans.SimpleTypeConverter}。
     * 可以通过任何TypeConverter实现来覆盖默认设置，通常是预先配置的SimpleTypeConverter或BeanWrapperImpl实例。
     * @see org.springframework.beans.SimpleTypeConverter
     * @see org.springframework.beans.BeanWrapperImpl
     */
    public void setTypeConverter(@Nullable TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
        this.useDefaultConverter = (typeConverter == null);
    }

    /**
     * 返回用于参数类型转换的 TypeConverter。
     * <p>如果需要直接访问底层的 PropertyEditor，可以将此对象转换为 {@link org.springframework.beans.PropertyEditorRegistry}（前提是当前的 TypeConverter 实际上实现了 PropertyEditorRegistry 接口）。
     */
    @Nullable
    public TypeConverter getTypeConverter() {
        if (this.typeConverter == null && this.useDefaultConverter) {
            this.typeConverter = getDefaultTypeConverter();
        }
        return this.typeConverter;
    }

    /**
     * 获取此方法调用器的默认TypeConverter。
     * <p>当未指定显式TypeConverter时调用。
     * 默认实现构建一个
     * {@link org.springframework.beans.SimpleTypeConverter}。
     * 可在子类中覆盖。
     */
    protected TypeConverter getDefaultTypeConverter() {
        return new SimpleTypeConverter();
    }

    /**
     * 为给定类型的所有属性注册指定的自定义属性编辑器。
     * <p>通常与默认的
     * {@link org.springframework.beans.SimpleTypeConverter} 结合使用；也可以与任何实现 PropertyEditorRegistry 接口的 TypeConverter 一起使用。
     * @param requiredType 属性的类型
     * @param propertyEditor 要注册的编辑器
     * @see #setTypeConverter
     * @see org.springframework.beans.PropertyEditorRegistry#registerCustomEditor
     */
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        TypeConverter converter = getTypeConverter();
        if (!(converter instanceof PropertyEditorRegistry registry)) {
            throw new IllegalStateException("TypeConverter does not implement PropertyEditorRegistry interface: " + converter);
        }
        registry.registerCustomEditor(requiredType, propertyEditor);
    }

    /**
     * 此实现寻找具有匹配参数类型的方法。
     * @see #doFindMatchingMethod
     */
    @Override
    @Nullable
    protected Method findMatchingMethod() {
        Method matchingMethod = super.findMatchingMethod();
        // 第二次遍历：查找可以将参数转换为参数类型的函数。
        if (matchingMethod == null) {
            // 将参数数组解释为单个方法参数。
            matchingMethod = doFindMatchingMethod(getArguments());
        }
        if (matchingMethod == null) {
            // 将参数数组解释为单个方法参数的数组类型。
            matchingMethod = doFindMatchingMethod(new Object[] { getArguments() });
        }
        return matchingMethod;
    }

    /**
     * 实际上找到一个与匹配参数类型的方法，即每个参数值都可以赋值给相应的参数类型。
     * @param arguments 要与方法参数匹配的参数值
     * @return 一个匹配的方法，如果没有匹配的方法则返回 {@code null}
     */
    @Nullable
    protected Method doFindMatchingMethod(Object[] arguments) {
        TypeConverter converter = getTypeConverter();
        if (converter != null) {
            String targetMethod = getTargetMethod();
            Method matchingMethod = null;
            int argCount = arguments.length;
            Class<?> targetClass = getTargetClass();
            Assert.state(targetClass != null, "No target class set");
            Method[] candidates = ReflectionUtils.getAllDeclaredMethods(targetClass);
            int minTypeDiffWeight = Integer.MAX_VALUE;
            Object[] argumentsToUse = null;
            for (Method candidate : candidates) {
                if (candidate.getName().equals(targetMethod)) {
                    // 检查被检查的方法是否有正确的参数数量。
                    int parameterCount = candidate.getParameterCount();
                    if (parameterCount == argCount) {
                        Class<?>[] paramTypes = candidate.getParameterTypes();
                        Object[] convertedArguments = new Object[argCount];
                        boolean match = true;
                        for (int j = 0; j < argCount && match; j++) {
                            // 验证提供的参数是否可以赋值给方法参数。
                            try {
                                convertedArguments[j] = converter.convertIfNecessary(arguments[j], paramTypes[j]);
                            } catch (TypeMismatchException ex) {
                                // 忽略 -> 简单地不匹配。
                                match = false;
                            }
                        }
                        if (match) {
                            int typeDiffWeight = getTypeDifferenceWeight(paramTypes, convertedArguments);
                            if (typeDiffWeight < minTypeDiffWeight) {
                                minTypeDiffWeight = typeDiffWeight;
                                matchingMethod = candidate;
                                argumentsToUse = convertedArguments;
                            }
                        }
                    }
                }
            }
            if (matchingMethod != null) {
                setArguments(argumentsToUse);
                return matchingMethod;
            }
        }
        return null;
    }
}
