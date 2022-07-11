package online.githuboy.jrebel.mybatisplus.cbp;

import org.zeroturnaround.bundled.javassist.CannotCompileException;
import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.expr.ExprEditor;
import org.zeroturnaround.bundled.javassist.expr.MethodCall;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * Process MybatisConfiguration$StrictMap class.
 *
 * @author suchu
 */
public class StrictMapCBP extends JavassistClassBytecodeProcessor {

    private boolean proceed = false;

    public StrictMapCBP() {
    }

    @Override
    public void process(ClassPool classPool, ClassLoader classLoader, CtClass ctClass) throws Exception {
        if (!proceed) {
            ctClass.getDeclaredMethod("put").instrument(new ExprEditor() {
                public void edit(MethodCall m) throws CannotCompileException {
                    if ("containsKey".equals(m.getMethodName())) {
                        m.replace("{  if (" + Constants.SqlMapReloaderClass + ".isReloading())    $_ = false;  else    $_ = $proceed($$);}");
                    } else if ("get".equals(m.getMethodName())) {
                        m.replace("{  $_ = $proceed($$);  if (" + Constants.SqlMapReloaderClass + ".isReloading()       && !($_ instanceof com.baomidou.mybatisplus.core.MybatisConfiguration$StrictMap$Ambiguity))    $_ = null;}");
                    }
                }
            });
            proceed = true;
        }
    }
}