package online.githuboy.jrebel.mybatisplus.cbp;

import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.CtField;
import org.zeroturnaround.bundled.javassist.CtMethod;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * MybatisMapperProxyFactory class hook<br>
 * <br>
 * <p>
 * Optimize {@link MybatisMapperProxy} to clear all method caches when Mapper reloads,
 * thus avoiding the need for  {@link MybatisMapperProxy} to {@link MybatisMapperProxyCBP#disableMethodCache clean} up the method cache every time the {@link MybatisMapperProxy#invoke} method is executed<br>
 * </p>
 * <br>
 * <b>Known issues</b>
 * <ol>
 *     <li>Probabilistic {@link StackOverflowError} is thrown when mapper is modified and reloaded,it only needs to be re-requested.</li>
 * </ol>
 *
 * @author suchu
 * @since 2022/07/19
 */
public class MybatisMapperProxyFactoryCBP extends JavassistClassBytecodeProcessor {
    @Override
    public void process(ClassPool classPool, ClassLoader classLoader, CtClass ctClass) throws Exception {
        ctClass.addField(CtField.make("private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory.class);", ctClass));
        CtMethod newInstanceMethod = ctClass.getDeclaredMethod("newInstance", new CtClass[]{classPool.get("org.apache.ibatis.session.SqlSession")});
        newInstanceMethod.insertBefore(" { " +
                " if(!this.methodCache.isEmpty()){\n" +
                "     logger.info(\"JRebel: clean MybatisMapperProxy:{} method cache\",mapperInterface.getName());\n" +
                "     java.util.Iterator iterator = this.methodCache.entrySet().iterator();\n" +
                "        while (iterator.hasNext()){\n" +
                "            java.util.Map.Entry next =(java.util.Map.Entry) iterator.next();\n" +
                "            java.lang.reflect.Method method =(java.lang.reflect.Method) next.getKey();\n" +
                "            if(mapperInterface.equals(method.getDeclaringClass())){\n" +
                "                logger.info(\"\\t method -> {}\",method.toString());\n" +
                "                iterator.remove();\n" +
                "            }\n" +
                "        }\n" +
                "}\n" +
                "}\n");
    }

}
