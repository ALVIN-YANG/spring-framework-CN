// 翻译完成 glm-4-flash
/*版权所有 2002-2020 原作者或作者。
 
根据Apache License, Version 2.0 ("许可证")进行许可；
除非根据法律规定或书面同意，否则不得使用此文件，除非遵守许可证。
您可以在以下地址获取许可证副本：
 
https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按"原样"分发，
不提供任何形式的保证或条件，无论是明示的还是暗示的。
有关许可权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.config;

import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.util.StringUtils;

/**
 * 代表一个方面的 {@link ParseState} 条目。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectEntry implements ParseState.Entry {

    private final String id;

    private final String ref;

    /**
     * 创建一个新的 {@code AspectEntry} 实例。
     * @param id 面向元素的唯一标识符
     * @param ref 由该面向元素引用的 bean 名称
     */
    public AspectEntry(String id, String ref) {
        this.id = id;
        this.ref = ref;
    }

    @Override
    public String toString() {
        return "Aspect: " + (StringUtils.hasLength(this.id) ? "id='" + this.id + "'" : "ref='" + this.ref + "'");
    }
}
