// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者。
 
根据Apache License，版本2.0（以下简称“许可证”）授权；
除非符合许可证规定，否则不得使用此文件。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的明示或暗示保证，包括但不限于对适销性、适用性和非侵权性的保证。
有关许可的具体语言和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.Serializable;
import org.springframework.aop.ClassFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 简单的 ClassFilter 实现，该实现允许类（以及可选的子类）通过。
 *
 * @author Rod Johnson
 * @author Sam Brannen
 */
@SuppressWarnings("serial")
public class RootClassFilter implements ClassFilter, Serializable {

    private final Class<?> clazz;

    public RootClassFilter(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        this.clazz = clazz;
    }

    @Override
    public boolean matches(Class<?> candidate) {
        return this.clazz.isAssignableFrom(candidate);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other || (other instanceof RootClassFilter that && this.clazz.equals(that.clazz)));
    }

    @Override
    public int hashCode() {
        return this.clazz.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + this.clazz.getName();
    }
}
