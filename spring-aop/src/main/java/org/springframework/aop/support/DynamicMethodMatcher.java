// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者们。

根据Apache许可证版本2.0（以下简称“许可证”）授权；
除非根据法律规定或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下链接处获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按照“原样”分发，
不提供任何明示或暗示的保证或条件，无论是关于其适用性、无侵权行为或特定目的的适用性。
有关许可的特定语言、权限和限制，请参阅许可证。*/
package org.springframework.aop.support;

import java.lang.reflect.Method;
import org.springframework.aop.MethodMatcher;

/**
 * 用于动态方法匹配器的便捷抽象超类，
 * 该类在运行时关注参数。
 *
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

    @Override
    public final boolean isRuntime() {
        return true;
    }

    /**
     * 可以重写此方法以添加动态匹配的预条件。此实现始终返回true。
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return true;
    }
}
