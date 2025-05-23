// 翻译完成 glm-4-flash
/*版权所有 2002-2012，原作者或作者。

根据Apache License，版本2.0（“许可证”）；除非遵守许可证，否则不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，
不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop;

/**
 * 用于执行一个或多个 AOP <b>引入</b> 的顾问的超接口。
 *
 * 此接口不能直接实现；子接口必须提供实现引入的顾问类型。
 *
 * 引入是通过 AOP 通知实现附加接口（由目标未实现）。
 *
 * @author Rod Johnson
 * @since 04.04.2003
 * @see IntroductionInterceptor
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

    /**
     * 返回确定此介绍应应用于哪些目标类的过滤器。
     * <p>这代表了切点的类部分。请注意，方法匹配对介绍没有意义。
     * @return 类过滤器
     */
    ClassFilter getClassFilter();

    /**
     * 建议的接口能否通过引入建议来实现？
     * 在添加 IntroductionAdvisor 之前调用。
     * 如果建议的接口不能由引入建议来实现，则抛出 IllegalArgumentException 异常
     */
    void validateInterfaces() throws IllegalArgumentException;
}
