package online.githuboy.jrebel.mybatisplus.cbp;

import org.zeroturnaround.bundled.javassist.*;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
import org.zeroturnaround.javarebel.integration.util.JavassistUtil;

/**
 * @author zeroturnaround
 * @author suchu
 * @since 2019/5/9 18:02
 */
public class MybatisConfigurationCBP extends JavassistClassBytecodeProcessor {
    private static final String LOGGER = (LoggerFactory.class.getName() + ".getInstance().productPrefix(\"MyBatisPlus\")");

    @Override
    public void process(ClassPool cp, ClassLoader cl, CtClass ctClass) throws Exception {
        cp.importPackage("java.util.Map");
        cp.importPackage("java.util.ArrayList");
        cp.importPackage("java.util.HashMap");
        cp.importPackage(Constants.JrConfigurationClass);
        cp.importPackage(Constants.SqlMapReloaderClass);
        cp.importPackage(Constants.JrInterceptorChain);
        cp.importPackage("org.apache.ibatis.plugin.Interceptor");
        ctClass.addInterface(cp.get(Constants.JrConfigurationClass));
        ctClass.addField(new CtField(cp.get(Constants.SqlMapReloaderClass), "reloader", ctClass));
        ctClass.addField(CtField.make("private ArrayList __nonXmlInterceptors = new ArrayList();", ctClass));
        overrideAddInterceptor(ctClass);
        overrideIsResourceLoaded(ctClass);
        CtConstructor[] constructors = ctClass.getConstructors();
        for (CtConstructor constructor : constructors) {
            if (constructor.callsSuper()) {
                constructor.insertAfter("reloader = new " + Constants.SqlMapReloaderClass + "($0);");
            }
        }
        //rewrite addMappedStatement
        ctClass.getDeclaredMethod("addMappedStatement").setBody("super.addMappedStatement($1);");
        ctClass.getDeclaredMethod("addInterceptor").insertBefore("if (!reloader.isInsideConf() && !reloader.isReloading()) {  " + LOGGER + ".info(\"Memorizing non-xml interceptor: {}\", $1);  __nonXmlInterceptors.add($1);}");
        ctClass.addMethod(CtNewMethod.make("public " + Constants.SqlMapReloaderClass + " getReloader() {  return reloader;}", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void reinit() {  loadedResources.clear();  ((" + Constants.JrInterceptorChain + ")  interceptorChain).jrClear();}", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void jrRebuildStatements() {" + (JavassistUtil.hasDeclaredMethod(ctClass, "buildAllStatements") ? "  buildAllStatements();" : "") + "}", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void afterReloading() {  " + LOGGER + ".info(\"XML configuration were reloaded, restoring memorized interceptors\");  for (java.util.Iterator iter =  __nonXmlInterceptors.iterator(); iter.hasNext(); ) {     Interceptor interceptor = (Interceptor) iter.next();     " + LOGGER + ".info(\"re-adding interceptor: {}\", interceptor);     interceptorChain.addInterceptor(interceptor);  }}", ctClass));
        ctClass.getDeclaredMethod("isResourceLoaded").insertAfter("if (reloader.shouldReload($1)) {  loadedResources.remove($1);  $_ = false;}");
        if (!JavassistUtil.hasDeclaredMethod(ctClass, "getSqlFragments")) {
            ctClass.addMethod(CtNewMethod.make("public Map getSqlFragments() {  return new HashMap();}", ctClass));
        }
    }

    /**
     * Override the `addInterceptor`
     * @param ctClass CtClass
     */
    private void overrideAddInterceptor( CtClass ctClass) throws CannotCompileException {
        ctClass.addMethod(CtNewMethod.make("public void addInterceptor(org.apache.ibatis.plugin.Interceptor interceptor){ super.addInterceptor(interceptor);}", ctClass));
    }

    /**
     * Override the `IsResourceLoaded`
     * @param ctClass CtClass
     */
    private void overrideIsResourceLoaded( CtClass ctClass) throws CannotCompileException {
        ctClass.addMethod(CtNewMethod.make("public boolean isResourceLoaded(String resource){return super.isResourceLoaded(resource);}", ctClass));
    }
}
