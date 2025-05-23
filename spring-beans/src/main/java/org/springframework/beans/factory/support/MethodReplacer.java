// 翻译完成 glm-4-flash
/** 版权所有 2002-2021 原作者或作者们。
*
* 根据 Apache 许可证 2.0 版（以下简称“许可证”），除非法律要求或书面同意，否则您不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“现状”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证以了解具体规定许可权限和限制的内容。*/
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * 接口由可以重写 IoC（控制反转）管理对象上任何方法的类实现：这是 <b>方法注入</b> 形式的依赖注入。
 *
 * <p>此类方法可能是（但不必是）抽象的，在这种情况下，容器将创建一个具体的子类以实例化。
 *
 * @author Rod Johnson
 * @since 1.1
 */
public interface MethodReplacer {

    /**
     * 重新实现给定的方法。
     * @param obj 我们要重新实现方法的实例
     * @param method 要重新实现的方法
     * @param args 方法的参数
     * @return 方法的返回值
     */
    Object reimplement(Object obj, Method method, Object[] args) throws Throwable;
}
