// 翻译完成 glm-4-flash
/*版权所有 2002-2018 原作者或作者们。

根据Apache License，版本2.0（以下简称“许可证”）授权；
除非遵守许可证，否则您不得使用此文件。
您可以在以下链接获取许可证副本：
https://www.apache.org/licenses/LICENSE-2.0

除非法律要求或书面同意，否则在许可证下分发的软件
按照“原样”分发，不提供任何明示或暗示的保证或条件。
有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInfo;
import org.springframework.util.ClassUtils;

/**
 * 支持实现 {@link org.springframework.aop.IntroductionInfo} 的类。
 *
 * <p>允许子类方便地添加给定对象的所有接口，并抑制不应添加的接口。同时允许查询所有引入的接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

    protected final Set<Class<?>> publishedInterfaces = new LinkedHashSet<>();

    private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<>(32);

    /**
     * 抑制指定的接口，该接口可能由于代理实现而自动检测到。
     * 调用此方法以排除内部接口在代理级别上的可见性。
     * <p>如果代理没有实现该接口，则不执行任何操作。
     * @param ifc 要抑制的接口
     */
    public void suppressInterface(Class<?> ifc) {
        this.publishedInterfaces.remove(ifc);
    }

    @Override
    public Class<?>[] getInterfaces() {
        return ClassUtils.toClassArray(this.publishedInterfaces);
    }

    /**
     * 检查指定的接口是否为已发布的介绍接口。
     * @param ifc 要检查的接口
     * @return 该接口是否属于此介绍的一部分
     */
    public boolean implementsInterface(Class<?> ifc) {
        for (Class<?> pubIfc : this.publishedInterfaces) {
            if (ifc.isInterface() && ifc.isAssignableFrom(pubIfc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在代理级别发布给定代理实现的所有接口。
     * @param delegate 代理对象
     */
    protected void implementInterfacesOnObject(Object delegate) {
        this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesAsSet(delegate));
    }

    /**
     * 这个方法是否在引入的接口上？
     * @param mi 方法调用
     * @return 调用的方法是否在引入的接口上
     */
    protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
        Boolean rememberedResult = this.rememberedMethods.get(mi.getMethod());
        if (rememberedResult != null) {
            return rememberedResult;
        } else {
            // 解出它并缓存它。
            boolean result = implementsInterface(mi.getMethod().getDeclaringClass());
            this.rememberedMethods.put(mi.getMethod(), result);
            return result;
        }
    }

    // 您提供的代码注释内容为空，请提供具体的 Java 代码注释内容，以便我能为您进行翻译。
    // 序列化支持
    // 由于您只提供了代码注释部分的分隔符“---------------------------------------------------------------------”，并没有提供实际的代码注释内容，因此我无法进行翻译。请提供具体的代码注释内容，我将根据内容进行翻译。
    /**
     * 此方法仅实现以恢复记录器。
     * 我们没有将记录器声明为静态的，因为这意味着子类将使用此类的日志类别。
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 依赖于默认的序列化机制；只需在反序列化后初始化状态即可。
        ois.defaultReadObject();
        // 初始化 transient 字段。
        this.rememberedMethods = new ConcurrentHashMap<>(32);
    }
}
