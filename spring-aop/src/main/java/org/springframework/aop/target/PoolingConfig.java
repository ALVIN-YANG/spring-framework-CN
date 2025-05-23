// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者。
 
根据Apache License，版本2.0（“许可证”），除非法律要求或书面同意，否则您不得使用此文件，除非遵守许可证。
您可以在以下链接处获得许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target;

/**
 * 用于池化目标源的配置接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface PoolingConfig {

    /**
     * 返回池的最大大小。
     */
    int getMaxSize();

    /**
     * 返回池中活动对象的数量。
     * @throws UnsupportedOperationException 如果池不支持此操作则抛出异常
     */
    int getActiveCount() throws UnsupportedOperationException;

    /**
     * 返回池中空闲对象的数量。
     * @throws UnsupportedOperationException 如果该池不支持此操作，则抛出此异常
     */
    int getIdleCount() throws UnsupportedOperationException;
}
