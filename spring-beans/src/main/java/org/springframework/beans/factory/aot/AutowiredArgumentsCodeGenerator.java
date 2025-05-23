// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License 2.0（以下简称“许可证”）许可，您可能无法使用此文件，除非符合许可证规定。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途的适用性或不侵犯第三方权利。
* 请参阅许可证了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 自动注入参数代码生成器。
 *
 * <p>生成的代码形式为：`args.get(0), args.get(1)` 或
 * `args.get(0, String.class), args.get(1, Integer.class)`
 *
 * <p>简单形式仅在目标方法或构造函数唯一时使用。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public class AutowiredArgumentsCodeGenerator {

    private final Class<?> target;

    private final Executable executable;

    public AutowiredArgumentsCodeGenerator(Class<?> target, Executable executable) {
        this.target = target;
        this.executable = executable;
    }

    public CodeBlock generateCode(Class<?>[] parameterTypes) {
        return generateCode(parameterTypes, 0, "args");
    }

    public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex) {
        return generateCode(parameterTypes, startIndex, "args");
    }

    public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex, String variableName) {
        Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
        Assert.notNull(variableName, "'variableName' must not be null");
        boolean ambiguous = isAmbiguous();
        CodeBlock.Builder code = CodeBlock.builder();
        for (int i = startIndex; i < parameterTypes.length; i++) {
            code.add((i != startIndex) ? ", " : "");
            if (!ambiguous) {
                code.add("$L.get($L)", variableName, i - startIndex);
            } else {
                code.add("$L.get($L, $T.class)", variableName, i - startIndex, parameterTypes[i]);
            }
        }
        return code.build();
    }

    private boolean isAmbiguous() {
        if (this.executable instanceof Constructor<?> constructor) {
            return Arrays.stream(this.target.getDeclaredConstructors()).filter(Predicate.not(constructor::equals)).anyMatch(this::hasSameParameterCount);
        }
        if (this.executable instanceof Method method) {
            return Arrays.stream(ReflectionUtils.getAllDeclaredMethods(this.target)).filter(Predicate.not(method::equals)).filter(candidate -> candidate.getName().equals(method.getName())).anyMatch(this::hasSameParameterCount);
        }
        return true;
    }

    private boolean hasSameParameterCount(Executable executable) {
        return this.executable.getParameterCount() == executable.getParameterCount();
    }
}
