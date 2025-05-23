// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或作者们。

根据Apache许可证版本2.0（“许可证”），除非法律要求或书面同意，否则您不得使用此文件，除非遵守许可证。
您可以在以下网址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 提供描述介绍所需信息的接口。
 *
 * <p>{@link IntroductionAdvisor IntroductionAdvisors}必须实现此接口。如果一个实现了{@link org.aopalliance.aop.Advice}，
 * 则可以使用它作为介绍而无需使用{@link IntroductionAdvisor}。在这种情况下，建议是自描述的，
 * 不仅提供了必要的功能，而且还描述了它所引入的接口。
 *
 * @author Rod Johnson
 * @since 1.1.1
 */
public interface IntroductionInfo {

    /**
     * 返回由这个Advisor或Advice引入的附加接口。
     * @return 引入的接口
     */
    Class<?>[] getInterfaces();
}
