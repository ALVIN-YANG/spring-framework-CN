// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者。
 
根据Apache许可证2.0版本（以下简称“许可证”）许可；除非符合许可证规定，否则不得使用此文件。
您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何形式的明示或暗示保证。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.framework.adapter;

/**
 * 单例用于发布一个共享的 DefaultAdvisorAdapterRegistry 实例。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @see DefaultAdvisorAdapterRegistry
 */
public final class GlobalAdvisorAdapterRegistry {

    private GlobalAdvisorAdapterRegistry() {
    }

    /**
     * 记录单个实例，以便我们可以将其返回给请求它的类。
     */
    private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

    /**
     * 返回单例的 {@link DefaultAdvisorAdapterRegistry} 实例。
     */
    public static AdvisorAdapterRegistry getInstance() {
        return instance;
    }

    /**
     * /**
     *  重置单例 {@link DefaultAdvisorAdapterRegistry}，移除任何已注册的
     *  {@link AdvisorAdapterRegistry#registerAdvisorAdapter(AdvisorAdapter) 插件适配器}。
     * /
     */
    static void reset() {
        instance = new DefaultAdvisorAdapterRegistry();
    }
}
