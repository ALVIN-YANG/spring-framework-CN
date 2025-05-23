// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.util.Arrays;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.ClassUtils;

/**
 * 用于支持 {@link ResolvableType} 的内部代码生成器。
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 6.0
 */
final class ResolvableTypeCodeGenerator {

    private ResolvableTypeCodeGenerator() {
    }

    public static CodeBlock generateCode(ResolvableType resolvableType) {
        return generateCode(resolvableType, false);
    }

    private static CodeBlock generateCode(ResolvableType resolvableType, boolean allowClassResult) {
        if (ResolvableType.NONE.equals(resolvableType)) {
            return CodeBlock.of("$T.NONE", ResolvableType.class);
        }
        Class<?> type = ClassUtils.getUserClass(resolvableType.toClass());
        if (resolvableType.hasGenerics() && !resolvableType.hasUnresolvableGenerics()) {
            return generateCodeWithGenerics(resolvableType, type);
        }
        if (allowClassResult) {
            return CodeBlock.of("$T.class", type);
        }
        return CodeBlock.of("$T.forClass($T.class)", ResolvableType.class, type);
    }

    private static CodeBlock generateCodeWithGenerics(ResolvableType target, Class<?> type) {
        ResolvableType[] generics = target.getGenerics();
        boolean hasNoNestedGenerics = Arrays.stream(generics).noneMatch(ResolvableType::hasGenerics);
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("$T.forClassWithGenerics($T.class", ResolvableType.class, type);
        for (ResolvableType generic : generics) {
            code.add(", $L", generateCode(generic, hasNoNestedGenerics));
        }
        code.add(")");
        return code.build();
    }
}
