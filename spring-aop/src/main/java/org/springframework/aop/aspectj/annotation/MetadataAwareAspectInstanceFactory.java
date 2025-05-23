// 翻译完成 glm-4-flash
/*版权所有 2002-2023 原作者或作者们。
 
根据Apache许可证版本2.0（以下简称“许可证”）许可，除非法律要求或书面同意，否则您不得使用此文件。您可以在以下地址获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非根据适用法律或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。有关权限和限制的具体语言，请参阅许可证。*/
package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.lang.Nullable;

/**
 * 这是 {@link org.springframework.aop.aspectj.AspectInstanceFactory} 的子接口，
 * 它返回与 AspectJ 注解类关联的 {@link AspectMetadata}。
 *
 * @author Rod Johnson
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjType
 */
public interface MetadataAwareAspectInstanceFactory extends AspectInstanceFactory {

    /**
     * 获取此工厂方面（aspect）的 AspectJ AspectMetadata。
     * @return 面方面（aspect）的元数据
     */
    AspectMetadata getAspectMetadata();

    /**
     * 获取此工厂可能获取的最佳创建互斥锁。
     * @return 互斥锁对象（可能为 {@code null} 表示不需要使用互斥锁）
     * @since 4.3
     */
    @Nullable
    Object getAspectCreationMutex();
}
