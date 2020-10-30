package xyz.mizhoux.aop.aspect.processor.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import xyz.mizhoux.aop.aspect.anno.MethodAspectAnno;
import xyz.mizhoux.aop.aspect.processor.MethodAspectProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 抽象方法切面处理器，提供两个功能的默认实现：<br/>
 * （1）方法不匹配时记录日志<br/>
 * （2）目标方法抛出异常时记录日志
 *
 * @author 之叶
 * @date   2019/08/28
 */
public abstract class AbstractMethodAspectProcessor<R> implements MethodAspectProcessor<R> {

    @Override
    public void onMismatch(ProceedingJoinPoint point) {
        Logger logger = getLogger(point);
        String logTag = getLogTag(point);

        // 获得方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获得方法
        Method method = signature.getMethod();
        // 获得方法的 @MethodAspectAnno 注解
        MethodAspectAnno anno = method.getAnnotation(MethodAspectAnno.class);
        // 获得方法切面处理器的 Class
        Class<? extends MethodAspectProcessor> processorType = anno.value();

        String processorName = processorType.getSimpleName();

        // 如果是接口或者抽象类
        if (processorType.isInterface() || Modifier.isAbstract(processorType.getModifiers())) {
            logger.warn("{} 需要指定具体的切面处理器，因为 {} 是接口或者抽象类", logTag, processorName);
            return;
        }

        logger.warn("{} 不是 {} 可以处理的方法，或者 {} 在 Spring 容器中不存在", logTag, processorName, processorName);
    }

    @Override
    public void onThrow(ProceedingJoinPoint point, Throwable e) {
        Logger logger = getLogger(point);
        String logTag = getLogTag(point);

        logger.error("{} 执行时出错", logTag, e);
    }

    /**
     * 获得被代理类的 Logger
     *
     * @param point 连接点
     * @return 被代理类的 Logger
     */
    protected Logger getLogger(ProceedingJoinPoint point) {
        Object target = point.getTarget();

        return LoggerFactory.getLogger(target.getClass());
    }

    /**
     * LogTag = 类名.方法名
     *
     * @param point 连接点
     * @return 目标类名.执行方法名
     */
    protected String getLogTag(ProceedingJoinPoint point) {
        Object target = point.getTarget();
        String className = target.getClass().getSimpleName();

        MethodSignature signature = (MethodSignature) point.getSignature();
        String methodName = signature.getName();

        return className + "." + methodName;
    }

    /**
     * 获得目标方法的返回类型
     *
     * @param point 连接点
     * @return 目标方法的返回类型
     */
    protected Class getReturnType(ProceedingJoinPoint point) {
        // 获得方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 获得方法
        Method method = signature.getMethod();

        return method.getReturnType();
    }


    private void explain(ProceedingJoinPoint point){
        //获取被代理的对象
        Object target = point.getTarget();
        //拦截的方法名称
        String methodName = point.getSignature().getName();
        //拦截的方法参数
        Object[] args = point.getArgs();
        //拦截的放参数类型
        Class[] parameterTypes = ((MethodSignature)point.getSignature()).getMethod().getParameterTypes();
        Method m = null;
        try {
           //通过反射获得拦截的method
            m = target.getClass().getMethod(methodName, parameterTypes);
            //如果是桥则要获得实际拦截的method
            if(m.isBridge()){
                for(int i = 0; i < args.length; i++){
                   /* //获得泛型类型
                    Class genClazz = GenericsUtils.getSuperClassGenricType(target.getClass());
                     //根据实际参数类型替换parameterType中的类型
                    if(args[i].getClass().isAssignableFrom(genClazz)){
                        parameterTypes[i] = genClazz;
                    }*/
                }
                //获得parameterType参数类型的方法
                m = target.getClass().getMethod(methodName, parameterTypes);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
