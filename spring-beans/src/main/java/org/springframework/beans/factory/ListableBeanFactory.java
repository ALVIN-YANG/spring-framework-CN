// 翻译完成 glm-4-flash
/** 版权所有 2002-2022 原作者或作者。
*
* 根据 Apache License, Version 2.0 ("许可证") 许可使用，除非符合许可证规定，否则不得使用此文件。
* 您可以在以下链接获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或书面同意，否则在许可证下分发的软件按照“原样”提供，不提供任何明示或暗示的保证或条件。
* 请参阅许可证了解具体管理许可权限和限制的语言。*/
package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 *  扩展了 {@link BeanFactory} 接口，由可以枚举其所有bean实例的bean工厂实现，而不是像客户端请求的那样逐个尝试通过名称查找bean。预加载所有bean定义（如基于XML的工厂）的BeanFactory实现可以采用此接口。
 *
 * <p>如果这是一个 {@link HierarchicalBeanFactory}，则返回值将 <i>不会</i> 考虑任何BeanFactory层次结构，而仅与当前工厂中定义的bean相关。请使用 {@link BeanFactoryUtils} 辅助类来考虑祖先工厂中的bean。
 *
 * <p>此接口中的方法将仅尊重此工厂的bean定义。它们将忽略通过其他方式（如通过 {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} 的 {@code registerSingleton} 方法）注册的任何单例bean，但除了 {@code getBeanNamesForType} 和 {@code getBeansOfType}，它们也会检查这样的手动注册的单例bean。当然，BeanFactory的 {@code getBean} 允许透明地访问这样的特殊bean。然而，在典型场景中，所有bean都由外部bean定义，所以大多数应用程序不必担心这种区分。
 *
 * <p><b>注意：</b>除了 {@code getBeanDefinitionCount} 和 {@code containsBeanDefinition} 之外，此接口中的方法不是为频繁调用而设计的。实现可能较慢。
 *
 * @since 2001年4月16日
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

    /**
     * 检查此 Bean 工厂是否包含具有给定名称的 Bean 定义。
     * <p>不考虑此工厂可能参与的任何层次结构，
     * 并且忽略通过非 Bean 定义方式注册的任何单例 Bean。
     * @param beanName 要查找的 Bean 名称
     * @return 如果此 Bean 工厂包含具有给定名称的 Bean 定义，则返回 true
     * @see #containsBean
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 返回工厂中定义的豆子数量。
     * <p>不考虑此工厂可能参与的任何层次结构，
     * 并且忽略通过非豆子定义方式注册的任何单例豆子。
     * @return 工厂中定义的豆子数量
     */
    int getBeanDefinitionCount();

    /**
     * 返回在此工厂中定义的所有bean的名称。
     * <p>不考虑此工厂可能参与的任何层次结构，
     * 并且忽略通过除bean定义之外的其他方式注册的单例bean。
     * @return 在此工厂中定义的所有bean的名称，
     * 或者如果没有定义任何bean，则返回一个空数组
     */
    String[] getBeanDefinitionNames();

    /**
     * 返回指定bean的提供者，允许在需要时按需检索实例，包括可用性和唯一性选项。
     * @param requiredType bean必须匹配的类型；可以是一个接口或超类
     * @param allowEagerInit 是否允许基于流的访问初始化<i>延迟初始化的单例</i>和<i>由FactoryBeans创建的对象</i>（或通过有“factory-bean”引用的工厂方法）以进行类型检查
     * @return 相应的提供者句柄
     * @since 5.3
     * @see #getBeanProvider(ResolvableType, boolean)
     * @see #getBeanProvider(Class)
     * @see #getBeansOfType(Class, boolean, boolean)
     * @see #getBeanNamesForType(Class, boolean, boolean)
     */
    <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit);

    /**
     * 返回指定bean的提供者，允许按需延迟检索实例，包括可用性和唯一性选项。
     * @param requiredType bean必须匹配的类型；可以是泛型类型声明。
     * 注意，这里不支持集合类型，与反射注入点不同。对于以编程方式检索与特定类型匹配的bean列表，请在此处指定实际的bean类型作为参数，并随后使用 {@link ObjectProvider#orderedStream()} 或其懒加载流式处理/迭代选项。
     * @param allowEagerInit 是否允许基于流的访问初始化 <i>延迟初始化的单例</i> 和 <i>由FactoryBeans</i>（或带有“factory-bean”引用的工厂方法）创建的对象</i> 以进行类型检查
     * @return 对应的提供者句柄
     * @since 5.3
     * @see #getBeanProvider(ResolvableType)
     * @see ObjectProvider#iterator()
     * @see ObjectProvider#stream()
     * @see ObjectProvider#orderedStream()
     * @see #getBeanNamesForType(ResolvableType, boolean, boolean)
     */
    <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit);

    /**
     * 返回与给定类型（包括子类）匹配的bean名称（包括由FactoryBean创建的对象），根据bean定义或FactoryBean的`getObjectType`值来判断。
     * <p><b>注意：此方法仅内省顶级bean。</b>它<i>不会</i>检查可能匹配指定类型的嵌套bean。
     * <p>考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，将针对原始FactoryBean进行类型匹配。
     * <p>不考虑此工厂可能参与的任何继承层次。使用BeanFactoryUtils的`beanNamesForTypeIncludingAncestors`方法来包括祖先工厂中的bean。
     * <p>注意：<i>不会</i>忽略除bean定义之外其他方式注册的单例bean。
     * <p>此版本的`getBeanNamesForType`匹配所有类型的bean，无论是单例、原型还是FactoryBean。在大多数实现中，结果将与`getBeanNamesForType(type, true, true)`相同。
     * <p>此方法返回的bean名称应尽可能按后端配置中定义的顺序返回。
     * @param type 要匹配的泛型类型类或接口
     * @return 匹配给定对象类型（包括子类）的bean名称（或由FactoryBean创建的对象）的名称，如果没有找到则返回空数组
     * @since 4.2
     * @see #isTypeMatch(String, ResolvableType)
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType)
     */
    String[] getBeanNamesForType(ResolvableType type);

    /**
     * 返回与给定类型（包括子类）匹配的bean名称（包括FactoryBean的getObjectType值），
     * 通过bean定义或FactoryBean的情况下进行判断。
     * <p><b>注意：此方法仅对顶级bean进行内省。</b>它不会检查可能匹配指定类型的嵌套bean。
     * <p>如果设置了"allowEagerInit"标志，则考虑由FactoryBean创建的对象，
     * 这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，则将直接匹配FactoryBean本身。
     * 如果"allowEagerInit"未设置，则仅检查原始FactoryBean（这不需要初始化每个FactoryBean）。
     * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的{@code beanNamesForTypeIncludingAncestors}
     * 来包括祖先工厂中的bean。
     * <p>注意：不会忽略除bean定义以外的其他方式注册的单例bean。
     * <p>此方法返回的bean名称应始终尽可能按照后端配置中的定义顺序返回。
     * @param type 要匹配的泛型类型类或接口
     * @param includeNonSingletons 是否包括原型或作用域bean，而不仅仅是单例（也适用于FactoryBean）
     * @param allowEagerInit 是否初始化<i>懒加载单例</i>和<i>由FactoryBean创建的对象</i>（或由具有"factory-bean"引用的工厂方法）
     * 以进行类型检查。请注意，FactoryBean需要被预先初始化以确定其类型：因此请注意，将此标志传递为"true"
     * 将初始化FactoryBean和"factory-bean"引用。
     * @return 匹配给定对象类型（包括子类）的bean名称（或由FactoryBean创建的对象），
     * 如果没有匹配项，则返回空数组
     * @since 5.2
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType, boolean, boolean)
     */
    String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

    /**
     * 返回与给定类型（包括子类）匹配的bean名称（包括FactoryBean的getObjectType值），
     * 无论从bean定义还是FactoryBean的情况下。
     * <p><b>注意：此方法仅 introspects 顶级bean。</b> 它 <i>不会</i> 检查可能匹配指定类型的嵌套bean。
     * <p>考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，
     * 将会对FactoryBean本身进行类型匹配。
     * <p>不考虑此工厂可能参与的任何继承层次。使用BeanFactoryUtils的{@code beanNamesForTypeIncludingAncestors}
     * 包括祖先工厂中的bean。
     * <p>注意：<i>不会</i> 忽略通过除bean定义之外的其他方式注册的单例bean。
     * <p>此版本的{@code getBeanNamesForType}匹配所有类型的bean，无论是单例、原型还是FactoryBean。
     * 在大多数实现中，结果将与{@code getBeanNamesForType(type, true, true)}相同。
     * <p>此方法返回的bean名称应尽可能按照后端配置中定义的顺序返回。
     * @param type 要匹配的类或接口，或为所有bean名称提供{@code null}
     * @return 匹配给定对象类型（包括子类）的bean（或由FactoryBean创建的对象）的名称，
     * 或如果没有任何匹配项，则返回空数组
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    String[] getBeanNamesForType(@Nullable Class<?> type);

    /**
     * 返回匹配给定类型的 Bean 名称（包括子类），判断依据是 Bean 定义或 FactoryBean 的情况下 `getObjectType` 的值。
     * <p><b>注意：此方法仅 introspects 顶级 Bean。</b> 它不会检查可能匹配指定类型的嵌套 Bean。
     * <p>如果设置了 "allowEagerInit" 标志，将考虑由 FactoryBean 创建的对象，这意味着 FactoryBean 将被初始化。如果由 FactoryBean 创建的对象不匹配，将使用原始的 FactoryBean 本身与类型进行匹配。如果没有设置 "allowEagerInit"，则只检查原始 FactoryBean（这不需要初始化每个 FactoryBean）。
     * <p>不考虑此工厂可能参与的任何层次结构。使用 BeanFactoryUtils 的 `beanNamesForTypeIncludingAncestors` 方法来包括祖先工厂中的 Bean。
     * <p>注意：不会忽略通过其他方式（而非 Bean 定义）注册的单例 Bean。
     * <p>此方法返回的 Bean 名称应尽可能按后端配置中定义的顺序返回，即 Bean 名称的 <i>定义顺序</i>。
     * @param type 要匹配的类或接口，或为 {@code null} 返回所有 Bean 名称
     * @param includeNonSingletons 是否包括原型或作用域 Bean，以及仅包括单例（也适用于 FactoryBean）
     * @param allowEagerInit 是否初始化 <i>懒加载单例</i> 和 <i>由 FactoryBean 创建的对象</i>（或通过具有 "factory-bean" 引用的工厂方法）以进行类型检查。请注意，FactoryBean 需要急切初始化以确定其类型：因此请注意，传入 "true" 将初始化 FactoryBean 和 "factory-bean" 引用。
     * @return 匹配给定对象类型（包括子类）的 Bean 名称（或由 FactoryBean 创建的对象）的列表，如果没有找到则返回空数组
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
     */
    String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

    /**
     * 返回与给定对象类型（包括子类）匹配的Bean实例，判断依据可以是Bean定义或者FactoryBean的`getObjectType`值。
     * <p><b>注意：此方法仅内省顶级Bean。</b>它不会检查可能匹配指定类型的嵌套Bean。
     * <p>考虑由FactoryBean创建的对象，这意味着FactoryBean将被初始化。如果FactoryBean创建的对象不匹配，将使用原始的FactoryBean与类型进行匹配。
     * <p>不考虑此工厂可能参与的任何层次结构。使用BeanFactoryUtils的`beansOfTypeIncludingAncestors`包括祖先工厂中的Bean。
     * <p>注意：不会忽略通过除Bean定义以外的其他方式注册的单例Bean。
     * <p>此版本的`getBeansOfType`匹配所有类型的Bean，无论是单例、原型还是FactoryBean。在大多数实现中，结果将与`getBeansOfType(type, true, true)`相同。
     * <p>此方法返回的Map应始终尽可能返回在后端配置中按定义顺序排列的Bean名称和相应的Bean实例。
     * @param type 要匹配的类或接口，或`null`以匹配所有具体Bean
     * @return 一个Map，包含匹配的Bean，键是Bean名称，值是对应的Bean实例
     * @throws BeansException 如果无法创建Bean
     * @since 1.1.2
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

    /**
     * 返回与给定对象类型（包括子类）匹配的 Bean 实例，根据 Bean 定义或 FactoryBean 的 `getObjectType` 方法值进行判断。
     * <p><b>注意：此方法仅 introspects 顶级 Bean。</b> 它 <i>不会</i> 检查可能匹配指定类型的嵌套 Bean。
     * <p>如果设置了 "allowEagerInit" 标志，则考虑由 FactoryBean 创建的对象，这意味着 FactoryBean 将被初始化。如果 FactoryBean 创建的对象不匹配，则将直接匹配原始的 FactoryBean。如果 "allowEagerInit" 未设置，则仅检查原始 FactoryBean（这不需要初始化每个 FactoryBean）。
     * <p>不考虑此工厂可能参与的任何层次结构。使用 BeanFactoryUtils 的 `beansOfTypeIncludingAncestors` 方法来包括祖先工厂中的 Bean。
     * <p>注意：<i>不会</i> 忽略通过除 Bean 定义之外的其他方式注册的单例 Bean。
     * <p>此方法返回的 Map 应始终返回 Bean 名称和对应的 Bean 实例，尽可能按后端配置中的定义顺序。
     * @param type 要匹配的类或接口，或 `null` 以匹配所有具体 Bean
     * @param includeNonSingletons 是否包括原型或作用域 Bean，或仅包括单例（也适用于 FactoryBean）
     * @param allowEagerInit 是否初始化 <i>懒加载单例</i> 和由 FactoryBean（或带有 "factory-bean" 引用的工厂方法）创建的对象以进行类型检查。请注意，FactoryBean 需要被尽早初始化以确定其类型：因此请注意，将此标志设置为 "true" 将初始化 FactoryBean 和 "factory-bean" 引用。
     * @return 一个包含匹配 Bean 的 Map，其中包含 Bean 名称作为键和相应的 Bean 实例作为值
     * @throws BeansException 如果无法创建 Bean
     * @see FactoryBean#getObjectType
     * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
     */
    <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException;

    /**
     * 查找所有被指定 {@link Annotation} 类型注解的 bean 名称，但不创建相应的 bean 实例。
     * <p>注意，此方法考虑由 FactoryBeans 创建的对象，这意味着 FactoryBeans 将会被初始化以确定它们的对象类型。
     * @param annotationType 要查找的注解类型（在指定 bean 的类、接口或工厂方法级别）
     * @return 所有匹配的 bean 名称
     * @since 4.0
     * @see #getBeansWithAnnotation(Class)
     * @see #findAnnotationOnBean(String, Class)
     */
    String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

    /**
     * 查找所有使用提供的 {@link Annotation} 类型注解的 bean，
     * 返回一个包含 bean 名称与对应 bean 实例的 Map。
     * <p>注意，此方法考虑由 FactoryBeans 创建的对象，这意味着
     * FactoryBeans 将会被初始化以确定它们的对象类型。
     * @param annotationType 要查找的注解类型
     * （在指定 bean 的类、接口或工厂方法级别）
     * @return 一个包含匹配的 bean 的 Map，其中包含 bean 名称作为
     * 键，相应的 bean 实例作为值
     * @throws BeansException 如果无法创建 bean
     * @since 3.0
     * @see #findAnnotationOnBean(String, Class)
     * @see #findAnnotationOnBean(String, Class, boolean)
     * @see #findAllAnnotationsOnBean(String, Class, boolean)
     */
    Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

    /**
     * 在指定的bean上查找类型为{@link Annotation}的注解，如果给定类本身没有找到注解，则会遍历其接口和超类，同时也会检查bean的工厂方法（如果有）。
     * @param beanName 要查找注解的bean的名称
     * @param annotationType 要查找的注解类型（在指定bean的类、接口或工厂方法级别）
     * @return 如果找到，则返回给定类型的注解，否则返回{@code null}
     * @throws NoSuchBeanDefinitionException 如果没有具有给定名称的bean
     * @since 3.0
     * @see #findAnnotationOnBean(String, Class, boolean)
     * @see #findAllAnnotationsOnBean(String, Class, boolean)
     * @see #getBeanNamesForAnnotation(Class)
     * @see #getBeansWithAnnotation(Class)
     * @see #getType(String)
     */
    @Nullable
    <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException;

    /**
     * 在指定的 bean 上查找类型为 {@code annotationType} 的 {@link Annotation}，
     * 如果在给定的类本身上找不到任何注解，将遍历其接口和超类，同时检查 bean 的工厂方法（如果有的话）。
     * @param beanName 要查找注解的 bean 的名称
     * @param annotationType 要查找的注解类型（在指定 bean 的类、接口或工厂方法级别）
     * @param allowFactoryBeanInit 是否允许仅为了确定其对象类型而初始化 {@code FactoryBean}
     * @return 如果找到，则返回给定类型的注解，否则返回 {@code null}
     * @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的 bean
     * @since 5.3.14
     * @see #findAnnotationOnBean(String, Class)
     * @see #findAllAnnotationsOnBean(String, Class, boolean)
     * @see #getBeanNamesForAnnotation(Class)
     * @see #getBeansWithAnnotation(Class)
     * @see #getType(String, boolean)
     */
    @Nullable
    <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

    /**
     * 查找指定bean上的所有 {@link Annotation} 实例，如果给定类本身没有找到任何注解，则遍历其接口和超类，以及检查bean的工厂方法（如果有）。
     * @param beanName 要查找注解的bean的名称
     * @param annotationType 要查找的注解类型（在指定bean的类、接口或工厂方法级别）
     * @param allowFactoryBeanInit 是否允许仅为了确定其对象类型而初始化 {@code FactoryBean}
     * @return 找到的给定类型的注解集合（可能为空）
     * @throws NoSuchBeanDefinitionException 如果没有找到具有给定名称的bean
     * @since 6.0
     * @see #getBeanNamesForAnnotation(Class)
     * @see #findAnnotationOnBean(String, Class, boolean)
     * @see #getType(String, boolean)
     */
    <A extends Annotation> Set<A> findAllAnnotationsOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;
}
