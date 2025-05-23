// 翻译完成 glm-4-flash
/** 版权所有 2002-2023 原作者或作者。
*
* 根据 Apache License 2.0（“许可证”）许可；
* 除非遵守许可证，否则不得使用此文件。
* 您可以在以下地址获取许可证副本：
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* 除非适用法律要求或经书面同意，否则在许可证下分发的软件
* 是按“原样”分发的，不提供任何形式的明示或暗示保证。
* 请参阅许可证了解具体管理权限和限制的条款。*/
package org.springframework.beans;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import org.xml.sax.InputSource;
import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CharsetEditor;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CurrencyEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputSourceEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PathEditor;
import org.springframework.beans.propertyeditors.PatternEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.ReaderEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.TimeZoneEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.beans.propertyeditors.ZoneIdEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * 对 {@link PropertyEditorRegistry} 接口的基类实现。
 * 提供默认编辑器和自定义编辑器的管理。
 * 主要作为 {@link BeanWrapperImpl} 的基类。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @since 1.2.6
 * @see java.beans.PropertyEditorManager
 * @see java.beans.PropertyEditorSupport#setAsText
 * @see java.beans.PropertyEditorSupport#setValue
 */
public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

    @Nullable
    private ConversionService conversionService;

    private boolean defaultEditorsActive = false;

    private boolean configValueEditorsActive = false;

    @Nullable
    private Map<Class<?>, PropertyEditor> defaultEditors;

    @Nullable
    private Map<Class<?>, PropertyEditor> overriddenDefaultEditors;

    @Nullable
    private Map<Class<?>, PropertyEditor> customEditors;

    @Nullable
    private Map<String, CustomEditorHolder> customEditorsForPath;

    @Nullable
    private Map<Class<?>, PropertyEditor> customEditorCache;

    /**
     * 指定一个用于转换属性值的 {@link ConversionService}，作为替代 JavaBeans 属性编辑器的方法。
     */
    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * 如果存在，则返回相关的ConversionService。
     */
    @Nullable
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释内容，我将为您进行中英文双语翻译。
    // 默认编辑器的管理
    // 由于您没有提供具体的Java代码注释内容，我无法进行翻译。请提供需要翻译的代码注释，我将为您翻译成中文。
    /**
     * 激活此注册实例的默认编辑器，
     * 允许在需要时延迟注册默认编辑器。
     */
    protected void registerDefaultEditors() {
        this.defaultEditorsActive = true;
    }

    /**
     * 激活仅用于配置目的的配置值编辑器，例如 {@link org.springframework.beans.propertyeditors.StringArrayPropertyEditor}。
     * <p>这些编辑器默认未注册，仅仅是因为它们在数据绑定方面通常不合适。当然，在任何情况下，您都可以通过 {@link #registerCustomEditor} 单独注册它们。
     */
    public void useConfigValueEditors() {
        this.configValueEditorsActive = true;
    }

    /**
     * 使用给定的属性编辑器覆盖指定类型的默认编辑器。
     * <p>请注意，这与注册自定义编辑器不同，因为编辑器在语义上仍然是一个默认编辑器。ConversionService 将覆盖这样的默认编辑器，而自定义编辑器通常覆盖 ConversionService。
     * @param requiredType 属性的类型
     * @param propertyEditor 要注册的编辑器
     * @see #registerCustomEditor(Class, PropertyEditor)
     */
    public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        if (this.overriddenDefaultEditors == null) {
            this.overriddenDefaultEditors = new HashMap<>();
        }
        this.overriddenDefaultEditors.put(requiredType, propertyEditor);
    }

    /**
     * 获取给定属性类型的默认编辑器（如果存在）。
     * <p>如果默认编辑器处于活动状态，则将其惰性地注册。
     * @param requiredType 属性的类型
     * @return 默认编辑器，如果未找到则返回 {@code null}
     * @see #registerDefaultEditors
     */
    @Nullable
    public PropertyEditor getDefaultEditor(Class<?> requiredType) {
        if (!this.defaultEditorsActive) {
            return null;
        }
        if (this.overriddenDefaultEditors != null) {
            PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
            if (editor != null) {
                return editor;
            }
        }
        if (this.defaultEditors == null) {
            createDefaultEditors();
        }
        return this.defaultEditors.get(requiredType);
    }

    /**
     * 实际上注册此注册实例的默认编辑器。
     */
    private void createDefaultEditors() {
        this.defaultEditors = new HashMap<>(64);
        // 简单的编辑器，没有参数化功能。
        // JDK 不包含任何这些目标类型的默认编辑器。
        this.defaultEditors.put(Charset.class, new CharsetEditor());
        this.defaultEditors.put(Class.class, new ClassEditor());
        this.defaultEditors.put(Class[].class, new ClassArrayEditor());
        this.defaultEditors.put(Currency.class, new CurrencyEditor());
        this.defaultEditors.put(File.class, new FileEditor());
        this.defaultEditors.put(InputStream.class, new InputStreamEditor());
        this.defaultEditors.put(InputSource.class, new InputSourceEditor());
        this.defaultEditors.put(Locale.class, new LocaleEditor());
        this.defaultEditors.put(Path.class, new PathEditor());
        this.defaultEditors.put(Pattern.class, new PatternEditor());
        this.defaultEditors.put(Properties.class, new PropertiesEditor());
        this.defaultEditors.put(Reader.class, new ReaderEditor());
        this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
        this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
        this.defaultEditors.put(URI.class, new URIEditor());
        this.defaultEditors.put(URL.class, new URLEditor());
        this.defaultEditors.put(UUID.class, new UUIDEditor());
        this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());
        // 集合编辑器的默认实例。
        // 可以通过注册自定义实例作为自定义编辑器来覆盖。
        this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
        this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
        this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
        this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
        this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));
        // 默认的原始数组编辑器。
        this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
        this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());
        // JDK 不包含 char 的默认编辑器！
        this.defaultEditors.put(char.class, new CharacterEditor(false));
        this.defaultEditors.put(Character.class, new CharacterEditor(true));
        // Spring的CustomBooleanEditor接受的标志值比JDK默认编辑器更多。
        this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
        this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));
        // JDK 不包含为数字包装类型提供默认编辑器！
        // 覆盖 JDK 原生数字编辑器，使用我们自己的 CustomNumberEditor。
        this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
        this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
        this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
        this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
        this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
        this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
        this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
        this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
        this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
        this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
        this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
        this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
        this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));
        // 仅当明确请求时才注册配置值编辑器。
        if (this.configValueEditorsActive) {
            StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
            this.defaultEditors.put(String[].class, sae);
            this.defaultEditors.put(short[].class, sae);
            this.defaultEditors.put(int[].class, sae);
            this.defaultEditors.put(long[].class, sae);
        }
    }

    /**
     * 将此实例中注册的默认编辑器复制到指定的目标注册表中。
     * @param target 要复制到的目标注册表
     */
    protected void copyDefaultEditorsTo(PropertyEditorRegistrySupport target) {
        target.defaultEditorsActive = this.defaultEditorsActive;
        target.configValueEditorsActive = this.configValueEditorsActive;
        target.defaultEditors = this.defaultEditors;
        target.overriddenDefaultEditors = this.overriddenDefaultEditors;
    }

    // 很抱歉，您只提供了代码的分割线“---------------------------------------------------------------------”，没有提供实际的代码注释内容。请提供具体的代码注释内容，以便我能够为您进行翻译。
    // 自定义编辑器的管理
    // 由于您没有提供具体的 Java 代码注释内容，我无法进行翻译。请提供需要翻译的英文代码注释，我将为您进行准确的翻译。
    @Override
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        registerCustomEditor(requiredType, null, propertyEditor);
    }

    @Override
    public void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor) {
        if (requiredType == null && propertyPath == null) {
            throw new IllegalArgumentException("Either requiredType or propertyPath is required");
        }
        if (propertyPath != null) {
            if (this.customEditorsForPath == null) {
                this.customEditorsForPath = new LinkedHashMap<>(16);
            }
            this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
        } else {
            if (this.customEditors == null) {
                this.customEditors = new LinkedHashMap<>(16);
            }
            this.customEditors.put(requiredType, propertyEditor);
            this.customEditorCache = null;
        }
    }

    @Override
    @Nullable
    public PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath) {
        Class<?> requiredTypeToUse = requiredType;
        if (propertyPath != null) {
            if (this.customEditorsForPath != null) {
                // 首先检查特定属性的编辑器。
                PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
                if (editor == null) {
                    List<String> strippedPaths = new ArrayList<>();
                    addStrippedPropertyPaths(strippedPaths, "", propertyPath);
                    for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null; ) {
                        String strippedPath = it.next();
                        editor = getCustomEditor(strippedPath, requiredType);
                    }
                }
                if (editor != null) {
                    return editor;
                }
            }
            if (requiredType == null) {
                requiredTypeToUse = getPropertyType(propertyPath);
            }
        }
        // 没有特定属性的编辑器 -> 检查特定类型的编辑器。
        return getCustomEditor(requiredTypeToUse);
    }

    /**
     * 判断此注册表中是否包含指定数组/集合元素的定制编辑器。
     * @param elementType 元素的目标类型
     * (如果不知道，则可以为 {@code null})
     * @param propertyPath 属性路径（通常是数组/集合的路径；
     * 如果不知道，则可以为 {@code null})
     * @return 是否找到了匹配的定制编辑器
     */
    public boolean hasCustomEditorForElement(@Nullable Class<?> elementType, @Nullable String propertyPath) {
        if (propertyPath != null && this.customEditorsForPath != null) {
            for (Map.Entry<String, CustomEditorHolder> entry : this.customEditorsForPath.entrySet()) {
                if (PropertyAccessorUtils.matchesProperty(entry.getKey(), propertyPath) && entry.getValue().getPropertyEditor(elementType) != null) {
                    return true;
                }
            }
        }
        // 没有特定属性编辑器 -> 检查特定类型编辑器。
        return (elementType != null && this.customEditors != null && this.customEditors.containsKey(elementType));
    }

    /**
     * 确定给定属性路径的属性类型。
     * <p>当没有指定所需类型时，由 {@link #findCustomEditor} 调用，
     * 以便能够在仅给定属性路径的情况下找到特定类型的编辑器。
     * <p>默认实现始终返回 {@code null}。
     * BeanWrapperImpl 通过实现 BeanWrapper 接口中定义的标准的 {@code getPropertyType}
     * 方法来覆盖此方法。
     * @param propertyPath 要确定类型的属性路径
     * @return 属性的类型，或者如果无法确定则返回 {@code null}
     * @see BeanWrapper#getPropertyType(String)
     */
    @Nullable
    protected Class<?> getPropertyType(String propertyPath) {
        return null;
    }

    /**
     * 获取为给定属性已注册的自定义编辑器。
     * @param propertyName 要查找的属性路径
     * @param requiredType 要查找的类型
     * @return 自定义编辑器，如果没有为该属性指定则返回 {@code null}
     */
    @Nullable
    private PropertyEditor getCustomEditor(String propertyName, @Nullable Class<?> requiredType) {
        CustomEditorHolder holder = (this.customEditorsForPath != null ? this.customEditorsForPath.get(propertyName) : null);
        return (holder != null ? holder.getPropertyEditor(requiredType) : null);
    }

    /**
     * 获取给定类型的自定义编辑器。如果找不到直接匹配，
     * 尝试获取超类的自定义编辑器（无论如何，它都能通过
     * `getAsText` 方法将值渲染为字符串）。
     * @param requiredType 要查找的类型
     * @return 自定义编辑器，或者对于此类型如果没有找到则为 {@code null}
     * @see java.beans.PropertyEditor#getAsText()
     */
    @Nullable
    private PropertyEditor getCustomEditor(@Nullable Class<?> requiredType) {
        if (requiredType == null || this.customEditors == null) {
            return null;
        }
        // 直接检查注册的编辑器是否为该类型。
        PropertyEditor editor = this.customEditors.get(requiredType);
        if (editor == null) {
            // 检查缓存编辑器是否已注册用于超类或接口的类型。
            if (this.customEditorCache != null) {
                editor = this.customEditorCache.get(requiredType);
            }
            if (editor == null) {
                // 查找超类或接口的编辑器。
                for (Map.Entry<Class<?>, PropertyEditor> entry : this.customEditors.entrySet()) {
                    Class<?> key = entry.getKey();
                    if (key.isAssignableFrom(requiredType)) {
                        editor = entry.getValue();
                        // 搜索类型缓存编辑器，以避免开销
                        // 重复的可赋值检查。
                        if (this.customEditorCache == null) {
                            this.customEditorCache = new HashMap<>();
                        }
                        this.customEditorCache.put(requiredType, editor);
                        if (editor != null) {
                            break;
                        }
                    }
                }
            }
        }
        return editor;
    }

    /**
     * 从已注册的自定义编辑器中猜测指定属性的属性类型（前提是它们为特定类型注册过）。
     * @param propertyName 属性的名称
     * @return 属性类型，或者在无法确定时返回 {@code null}
     */
    @Nullable
    protected Class<?> guessPropertyTypeFromEditors(String propertyName) {
        if (this.customEditorsForPath != null) {
            CustomEditorHolder editorHolder = this.customEditorsForPath.get(propertyName);
            if (editorHolder == null) {
                List<String> strippedPaths = new ArrayList<>();
                addStrippedPropertyPaths(strippedPaths, "", propertyName);
                for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editorHolder == null; ) {
                    String strippedName = it.next();
                    editorHolder = this.customEditorsForPath.get(strippedName);
                }
            }
            if (editorHolder != null) {
                return editorHolder.getRegisteredType();
            }
        }
        return null;
    }

    /**
     * 将此实例中注册的自定义编辑器复制到指定的目标注册表中。
     * @param target 要复制的目标注册表
     * @param nestedProperty 目标注册表的嵌套属性路径，如果有的话。
     * 如果此参数非空，则仅复制注册在嵌套属性路径下的编辑器。如果此参数为空，则复制所有编辑器。
     */
    protected void copyCustomEditorsTo(PropertyEditorRegistry target, @Nullable String nestedProperty) {
        String actualPropertyName = (nestedProperty != null ? PropertyAccessorUtils.getPropertyName(nestedProperty) : null);
        if (this.customEditors != null) {
            this.customEditors.forEach(target::registerCustomEditor);
        }
        if (this.customEditorsForPath != null) {
            this.customEditorsForPath.forEach((editorPath, editorHolder) -> {
                if (nestedProperty != null) {
                    int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(editorPath);
                    if (pos != -1) {
                        String editorNestedProperty = editorPath.substring(0, pos);
                        String editorNestedPath = editorPath.substring(pos + 1);
                        if (editorNestedProperty.equals(nestedProperty) || editorNestedProperty.equals(actualPropertyName)) {
                            target.registerCustomEditor(editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
                        }
                    }
                } else {
                    target.registerCustomEditor(editorHolder.getRegisteredType(), editorPath, editorHolder.getPropertyEditor());
                }
            });
        }
    }

    /**
     * 添加所有变体的去重键和/或索引的属性路径。
     * 以嵌套路径递归调用自身。
     * @param strippedPaths 要添加到其中的结果列表
     * @param nestedPath 当前嵌套路径
     * @param propertyPath 要检查去重键/索引的属性路径
     */
    private void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {
        int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
        if (startIndex != -1) {
            int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
            if (endIndex != -1) {
                String prefix = propertyPath.substring(0, startIndex);
                String key = propertyPath.substring(startIndex, endIndex + 1);
                String suffix = propertyPath.substring(endIndex + 1);
                // 删除第一个键。
                strippedPaths.add(nestedPath + prefix + suffix);
                // 搜索用于剥离的进一步键，同时剥离第一个键。
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
                // 搜索要剥离的更多键，但第一个键除外。
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
            }
        }
    }

    /**
     * 用于存储具有属性名称的已注册自定义编辑器的持有者。
     * 保留 PropertyEditor 本身以及它注册的类型。
     */
    private static final class CustomEditorHolder {

        private final PropertyEditor propertyEditor;

        @Nullable
        private final Class<?> registeredType;

        private CustomEditorHolder(PropertyEditor propertyEditor, @Nullable Class<?> registeredType) {
            this.propertyEditor = propertyEditor;
            this.registeredType = registeredType;
        }

        private PropertyEditor getPropertyEditor() {
            return this.propertyEditor;
        }

        @Nullable
        private Class<?> getRegisteredType() {
            return this.registeredType;
        }

        @Nullable
        private PropertyEditor getPropertyEditor(@Nullable Class<?> requiredType) {
            // 特殊情况：如果没有指定所需类型，这种情况通常只发生在
            // 集合元素，或所需类型无法分配给已注册类型。
            // 这通常只发生在类型为 Object 的泛型属性上 -
            // 如果未注册用于集合或数组类型的 PropertyEditor，则返回 PropertyEditor。
            // （如果未注册为集合或数组，则假定其意图为）
            // for 元素。
            if (this.registeredType == null || (requiredType != null && (ClassUtils.isAssignable(this.registeredType, requiredType) || ClassUtils.isAssignable(requiredType, this.registeredType))) || (requiredType == null && (!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
                return this.propertyEditor;
            } else {
                return null;
            }
        }
    }
}
