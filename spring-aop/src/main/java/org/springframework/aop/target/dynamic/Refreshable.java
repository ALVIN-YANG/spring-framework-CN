// 翻译完成 glm-4-flash
/*版权所有 2002-2012 原作者或原作者。

根据Apache License，版本2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.target.dynamic;

/**
 * 接口，由动态目标对象实现，
 * 支持重新加载，并可选择轮询以获取更新。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public interface Refreshable {

    /**
     * 刷新底层的目标对象。
     */
    void refresh();

    /**
     * 返回自启动以来实际刷新的次数。
     */
    long getRefreshCount();

    /**
     * 返回实际刷新发生的最后时间（作为时间戳）。
     */
    long getLastRefreshTime();
}
