package online.githuboy.jrebel.mybatisplus.cbp;

import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.NotFoundException;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * MybatisMapperProxy class hook
 *
 * @author suchu
 * @author zeroturnaround
 * @since 2019/05/01 09:19
 */
public class MybatisMapperProxyCBP extends JavassistClassBytecodeProcessor {
    public MybatisMapperProxyCBP() {
    }

    public void process(ClassPool cp, ClassLoader cl, CtClass ctClass) throws Exception {
        this.disableMethodCache(ctClass);
    }

    private void disableMethodCache(CtClass ctClass) throws Exception {
        try {
            ctClass.getDeclaredMethod("cachedMapperMethod").insertBefore("{  this.methodCache.clear(); }");
        } catch (NotFoundException var3) {
        }
        //Compatible with mybatis-plus 3.4.0+ https://github.com/baomidou/mybatis-plus/commit/a6ae6ab4e91b126e1212d7041f6bd8365e5ab286
        try {
            ctClass.getDeclaredMethod("cachedInvoker").insertBefore("{  this.methodCache.clear(); }");
        } catch (NotFoundException var3) {
        }

    }
}