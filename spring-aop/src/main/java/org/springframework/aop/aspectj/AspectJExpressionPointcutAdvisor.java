// 翻译完成 glm-4-flash
/*版权所有 2002-2017 原作者或作者。
 
根据Apache License, Version 2.0（“许可证”），除非根据法律要求或书面同意，否则您不得使用此文件，除非符合许可证。
您可以在以下链接获取许可证副本：
 
      https://www.apache.org/licenses/LICENSE-2.0
 
除非法律要求或书面同意，否则在许可证下分发的软件按“原样”分发，不提供任何明示或暗示的保证或条件。
有关许可证的具体语言规定权限和限制，请参阅许可证。*/
package org.springframework.aop.aspectj;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.lang.Nullable;

/**
 * Spring AOP 顾问，可用于任何 AspectJ 切入点表达式。
 *
 * @author Rob Harrop
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJExpressionPointcutAdvisor extends AbstractGenericPointcutAdvisor implements BeanFactoryAware {

    private final AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();

    public void setExpression(@Nullable String expression) {
        this.pointcut.setExpression(expression);
    }

    @Nullable
    public String getExpression() {
        return this.pointcut.getExpression();
    }

    public void setLocation(@Nullable String location) {
        this.pointcut.setLocation(location);
    }

    @Nullable
    public String getLocation() {
        return this.pointcut.getLocation();
    }

    public void setParameterNames(String... names) {
        this.pointcut.setParameterNames(names);
    }

    public void setParameterTypes(Class<?>... types) {
        this.pointcut.setParameterTypes(types);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.pointcut.setBeanFactory(beanFactory);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
