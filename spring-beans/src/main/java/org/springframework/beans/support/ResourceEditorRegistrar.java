// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache 许可证 2.0 版（"许可证"），您可以使用此文件，但须遵守许可证规定。
* 您可以在以下地址获得许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件按照"现状"提供，
* 不提供任何形式的明示或暗示保证，包括但不限于适销性或特定用途适用性。
* 请参阅许可证了解管理许可权和限制的具体语言。*/
package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import org.xml.sax.InputSource;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputSourceEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.PathEditor;
import org.springframework.beans.propertyeditors.ReaderEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 *  实现了 PropertyEditorRegistrar 接口，用于向给定的
 *  {@link org.springframework.beans.PropertyEditorRegistry}
 * （通常是用于在 {@link org.springframework.context.ApplicationContext} 中创建 bean 的
 *  {@link org.springframework.beans.BeanWrapper}）填充资源编辑器。
 *  由
 *  {@link org.springframework.context.support.AbstractApplicationContext}
 *  使用。
 *
 *  @author Juergen Hoeller
 *  @author Chris Beams
 *  @since 2.0
 */
public class ResourceEditorRegistrar implements PropertyEditorRegistrar {

    private final PropertyResolver propertyResolver;

    private final ResourceLoader resourceLoader;

    /**
     * 为给定的 {@link ResourceLoader} 和 {@link PropertyResolver} 创建一个新的 ResourceEditorRegistrar。
     * @param resourceLoader 用于创建编辑器的 ResourceLoader（或 ResourcePatternResolver）（通常是一个 ApplicationContext）
     * @param propertyResolver PropertyResolver（通常是一个 Environment）
     * @see org.springframework.core.env.Environment
     * @see org.springframework.core.io.support.ResourcePatternResolver
     * @see org.springframework.context.ApplicationContext
     */
    public ResourceEditorRegistrar(ResourceLoader resourceLoader, PropertyResolver propertyResolver) {
        this.resourceLoader = resourceLoader;
        this.propertyResolver = propertyResolver;
    }

    /**
     * 将以下资源编辑器填充到给定的 {@code registry} 中：
     * ResourceEditor, InputStreamEditor, InputSourceEditor, FileEditor, URLEditor,
     * URIEditor, ClassEditor, ClassArrayEditor.
     * <p>如果此注册器已配置了 {@link ResourcePatternResolver}，
     * 将注册一个 ResourceArrayPropertyEditor。
     * @see org.springframework.core.io.ResourceEditor
     * @see org.springframework.beans.propertyeditors.InputStreamEditor
     * @see org.springframework.beans.propertyeditors.InputSourceEditor
     * @see org.springframework.beans.propertyeditors.FileEditor
     * @see org.springframework.beans.propertyeditors.URLEditor
     * @see org.springframework.beans.propertyeditors.URIEditor
     * @see org.springframework.beans.propertyeditors.ClassEditor
     * @see org.springframework.beans.propertyeditors.ClassArrayEditor
     * @see org.springframework.core.io.support.ResourceArrayPropertyEditor
     */
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
        doRegisterEditor(registry, Resource.class, baseEditor);
        doRegisterEditor(registry, ContextResource.class, baseEditor);
        doRegisterEditor(registry, WritableResource.class, baseEditor);
        doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
        doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
        doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
        doRegisterEditor(registry, Path.class, new PathEditor(baseEditor));
        doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
        doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));
        ClassLoader classLoader = this.resourceLoader.getClassLoader();
        doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
        doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
        doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));
        if (this.resourceLoader instanceof ResourcePatternResolver resourcePatternResolver) {
            doRegisterEditor(registry, Resource[].class, new ResourceArrayPropertyEditor(resourcePatternResolver, this.propertyResolver));
        }
    }

    /**
     * 如果可能，覆盖默认编辑器（因为这正是我们在这里想要做的）；
     * 否则，注册为自定义编辑器。
     */
    private void doRegisterEditor(PropertyEditorRegistry registry, Class<?> requiredType, PropertyEditor editor) {
        if (registry instanceof PropertyEditorRegistrySupport registrySupport) {
            registrySupport.overrideDefaultEditor(requiredType, editor);
        } else {
            registry.registerCustomEditor(requiredType, editor);
        }
    }
}
