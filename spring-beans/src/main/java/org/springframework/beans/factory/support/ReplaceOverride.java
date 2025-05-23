// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0 ("许可证") 许可使用；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 扩展了 {@link MethodOverride}，表示由 IoC 容器对方法的任意重写。
 *
 * <p>任何非最终方法都可以被重写，无论其参数和返回类型如何。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class ReplaceOverride extends MethodOverride {

    private final String methodReplacerBeanName;

    private final List<String> typeIdentifiers = new ArrayList<>();

    /**
     * 构建一个新的 ReplaceOverride。
     * @param methodName 要重写的方法的名称
     * @param methodReplacerBeanName 用于替换方法的 bean 名称
     */
    public ReplaceOverride(String methodName, String methodReplacerBeanName) {
        super(methodName);
        Assert.notNull(methodReplacerBeanName, "Method replacer bean name must not be null");
        this.methodReplacerBeanName = methodReplacerBeanName;
    }

    /**
     * 返回实现 MethodReplacer 的 Bean 的名称。
     */
    public String getMethodReplacerBeanName() {
        return this.methodReplacerBeanName;
    }

    /**
     * 添加一个类字符串片段，例如 "Exception"
     * 或 "java.lang.Exc"，以标识参数类型。
     * @param identifier 完全限定类名的一个子字符串
     */
    public void addTypeIdentifier(String identifier) {
        this.typeIdentifiers.add(identifier);
    }

    @Override
    public boolean matches(Method method) {
        if (!method.getName().equals(getMethodName())) {
            return false;
        }
        if (!isOverloaded()) {
            // 未重载：无需担心参数类型匹配...
            return true;
        }
        // 如果我们到达这里，我们需要坚持精确的参数匹配...
        if (this.typeIdentifiers.size() != method.getParameterCount()) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < this.typeIdentifiers.size(); i++) {
            String identifier = this.typeIdentifiers.get(i);
            if (!parameterTypes[i].getName().contains(identifier)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (other instanceof ReplaceOverride that && super.equals(other) && ObjectUtils.nullSafeEquals(this.methodReplacerBeanName, that.methodReplacerBeanName) && ObjectUtils.nullSafeEquals(this.typeIdentifiers, that.typeIdentifiers));
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.methodReplacerBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.typeIdentifiers);
        return hashCode;
    }

    @Override
    public String toString() {
        return "Replace override for method '" + getMethodName() + "'";
    }
}
