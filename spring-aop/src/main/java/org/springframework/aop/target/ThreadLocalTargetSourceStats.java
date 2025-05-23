// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache License，版本2.0（“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

/**
 * 用于ThreadLocal TargetSource的统计信息。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface ThreadLocalTargetSourceStats {

    /**
     * 返回客户端调用次数。
     */
    int getInvocationCount();

    /**
     * 返回由线程绑定对象满足的命中次数。
     */
    int getHitCount();

    /**
     * 返回已创建的线程绑定对象的数量。
     */
    int getObjectCount();
}
