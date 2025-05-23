// 翻译完成 glm-4-flash
/*版权所有 2002-2016 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”），除非适用法律要求或书面同意，否则不得使用此文件，除非符合许可证。
您可以在以下链接处获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.aopalliance.intercept;

import javax.annotation.Nonnull;

/**
 * 此接口表示程序中的调用。
 *
 * <p>调用是一个连接点，可以被拦截器拦截。
 *
 * @author Rod Johnson
 */
public interface Invocation extends Joinpoint {

    /**
     * 获取参数作为数组对象。
     * 可以修改该数组中的元素值来改变参数。
     * @return 调用的参数
     */
    @Nonnull
    Object[] getArguments();
}
