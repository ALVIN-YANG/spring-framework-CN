// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可，除非法律要求或书面同意，否则不得使用此文件。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非根据法律规定或书面同意，否则在许可证下分发的软件按“原样”提供，
* 不提供任何明示或暗示的保证或条件，包括但不限于适销性、特定用途适用性和非侵权性。
* 请参阅许可证，了解具体管理许可权限和限制的条款。*/
package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * 用于管理注入元数据的内部类。
 *
 * <p>不推荐在应用程序中直接使用。
 *
 * <p>由以下类使用：
 * - {@link AutowiredAnnotationBeanPostProcessor}
 * - {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor}
 * - {@link org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor}
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class InjectionMetadata {

    /**
     * 一个空的 {@code InjectionMetadata} 实例，包含无操作（no-op）回调。
     * @since 5.2
     */
    public static final InjectionMetadata EMPTY = new InjectionMetadata(Object.class, Collections.emptyList()) {

        @Override
        protected boolean needsRefresh(Class<?> clazz) {
            return false;
        }

        @Override
        public void checkConfigMembers(RootBeanDefinition beanDefinition) {
        }

        @Override
        public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) {
        }

        @Override
        public void clear(@Nullable PropertyValues pvs) {
        }
    };

    private final Class<?> targetClass;

    private final Collection<InjectedElement> injectedElements;

    @Nullable
    private volatile Set<InjectedElement> checkedElements;

    /**
     * 创建一个新的 {@code InjectionMetadata} 实例。
     * <p>建议使用 {@link #forElements} 来复用 {@link #EMPTY} 实例，以防没有元素的情况。
     * @param targetClass 目标类
     * @param elements 相关的注入元素
     * @see #forElements
     */
    public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
        this.targetClass = targetClass;
        this.injectedElements = elements;
    }

    /**
     * 返回要注入的 {@link 注入元素 elements}。
     * @return 要注入的元素
     */
    public Collection<InjectedElement> getInjectedElements() {
        return Collections.unmodifiableCollection(this.injectedElements);
    }

    /**
     * 返回根据指定的 {@link PropertyValues} 要注入的 {@link InjectedElement 元素}。如果某个属性已经定义在一个 {@link InjectedElement 注入元素} 上，则该属性会被排除。
     * @param pvs 需要考虑的属性值
     * @return 要注入的元素
     * @since 6.0.10
     */
    public Collection<InjectedElement> getInjectedElements(@Nullable PropertyValues pvs) {
        return this.injectedElements.stream().filter(candidate -> candidate.shouldInject(pvs)).toList();
    }

    /**
     * 判断此元数据实例是否需要刷新。
     * @param clazz 当前目标类
     * @return 返回 {@code true} 表示需要刷新，否则返回 {@code false}
     * @since 5.2.4
     */
    protected boolean needsRefresh(Class<?> clazz) {
        return (this.targetClass != clazz);
    }

    public void checkConfigMembers(RootBeanDefinition beanDefinition) {
        if (this.injectedElements.isEmpty()) {
            this.checkedElements = Collections.emptySet();
        } else {
            Set<InjectedElement> checkedElements = new LinkedHashSet<>((this.injectedElements.size() * 4 / 3) + 1);
            for (InjectedElement element : this.injectedElements) {
                Member member = element.getMember();
                if (!beanDefinition.isExternallyManagedConfigMember(member)) {
                    beanDefinition.registerExternallyManagedConfigMember(member);
                    checkedElements.add(element);
                }
            }
            this.checkedElements = checkedElements;
        }
    }

    public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> checkedElements = this.checkedElements;
        Collection<InjectedElement> elementsToIterate = (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                element.inject(target, beanName, pvs);
            }
        }
    }

    /**
     * 清除包含元素中的属性跳过。
     * @since 3.2.13
     */
    public void clear(@Nullable PropertyValues pvs) {
        Collection<InjectedElement> checkedElements = this.checkedElements;
        Collection<InjectedElement> elementsToIterate = (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                element.clearPropertySkipping(pvs);
            }
        }
    }

    /**
     * 返回一个可能的空元素对应的 {@code InjectionMetadata} 实例。
     * @param elements 要注入的元素（可能为空）
     * @param clazz 目标类
     * @return 一个新的 {@link #InjectionMetadata(Class, Collection)} 实例
     * @since 5.2
     */
    public static InjectionMetadata forElements(Collection<InjectedElement> elements, Class<?> clazz) {
        return (elements.isEmpty() ? new InjectionMetadata(clazz, Collections.emptyList()) : new InjectionMetadata(clazz, elements));
    }

    /**
     * 检查给定的注入元数据是否需要刷新。
     * @param metadata 已存在的元数据实例
     * @param clazz 当前目标类
     * @return 返回 {@code true} 表示需要刷新，否则返回 {@code false}
     * @see #needsRefresh(Class)
     */
    public static boolean needsRefresh(@Nullable InjectionMetadata metadata, Class<?> clazz) {
        return (metadata == null || metadata.needsRefresh(clazz));
    }

    /**
     * 单个注入的元素。
     */
    public abstract static class InjectedElement {

        protected final Member member;

        protected final boolean isField;

        @Nullable
        protected final PropertyDescriptor pd;

        @Nullable
        protected volatile Boolean skip;

        protected InjectedElement(Member member, @Nullable PropertyDescriptor pd) {
            this.member = member;
            this.isField = (member instanceof Field);
            this.pd = pd;
        }

        public final Member getMember() {
            return this.member;
        }

        protected final Class<?> getResourceType() {
            if (this.isField) {
                return ((Field) this.member).getType();
            } else if (this.pd != null) {
                return this.pd.getPropertyType();
            } else {
                return ((Method) this.member).getParameterTypes()[0];
            }
        }

        protected final void checkResourceType(Class<?> resourceType) {
            if (this.isField) {
                Class<?> fieldType = ((Field) this.member).getType();
                if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
                    throw new IllegalStateException("Specified field type [" + fieldType + "] is incompatible with resource type [" + resourceType.getName() + "]");
                }
            } else {
                Class<?> paramType = (this.pd != null ? this.pd.getPropertyType() : ((Method) this.member).getParameterTypes()[0]);
                if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
                    throw new IllegalStateException("Specified parameter type [" + paramType + "] is incompatible with resource type [" + resourceType.getName() + "]");
                }
            }
        }

        /**
         * 是否应注入属性值。
         * @param pvs 要检查的属性值
         * @return 是否应注入属性值
         * @since 6.0.10
         */
        protected boolean shouldInject(@Nullable PropertyValues pvs) {
            if (this.isField) {
                return true;
            }
            return !checkPropertySkipping(pvs);
        }

        /**
         * 这一个或{@link #getResourceToInject}需要被重写。
         */
        protected void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs) throws Throwable {
            if (!shouldInject(pvs)) {
                return;
            }
            if (this.isField) {
                Field field = (Field) this.member;
                ReflectionUtils.makeAccessible(field);
                field.set(target, getResourceToInject(target, requestingBeanName));
            } else {
                try {
                    Method method = (Method) this.member;
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(target, getResourceToInject(target, requestingBeanName));
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }

        /**
         * 检查此注入器的属性是否因为已经指定了显式的属性值而需要跳过。同时，将受影响的属性标记为已处理，以便其他处理器忽略它。
         */
        protected boolean checkPropertySkipping(@Nullable PropertyValues pvs) {
            Boolean skip = this.skip;
            if (skip != null) {
                return skip;
            }
            if (pvs == null) {
                this.skip = false;
                return false;
            }
            synchronized (pvs) {
                skip = this.skip;
                if (skip != null) {
                    return skip;
                }
                if (this.pd != null) {
                    if (pvs.contains(this.pd.getName())) {
                        // 在Bean定义中显式提供的值。
                        this.skip = true;
                        return true;
                    } else if (pvs instanceof MutablePropertyValues mpvs) {
                        mpvs.registerProcessedProperty(this.pd.getName());
                    }
                }
                this.skip = false;
                return false;
            }
        }

        /**
         * 清除此元素的属性跳过。
         * @since 3.2.13
         */
        protected void clearPropertySkipping(@Nullable PropertyValues pvs) {
            if (pvs == null) {
                return;
            }
            synchronized (pvs) {
                if (Boolean.FALSE.equals(this.skip) && this.pd != null && pvs instanceof MutablePropertyValues mpvs) {
                    mpvs.clearProcessedProperty(this.pd.getName());
                }
            }
        }

        /**
         * 需要重写此方法或 {@link #inject} 方法之一。
         */
        @Nullable
        protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
            return null;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            return (this == other || (other instanceof InjectedElement that && this.member.equals(that.member)));
        }

        @Override
        public int hashCode() {
            return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " for " + this.member;
        }
    }
}
