package online.githuboy.jrebel.mybatisplus.cbp;

import org.zeroturnaround.bundled.javassist.*;
import org.zeroturnaround.bundled.javassist.expr.ExprEditor;
import org.zeroturnaround.bundled.javassist.expr.MethodCall;
import org.zeroturnaround.bundled.javassist.expr.NewExpr;
import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * MybatisMapperAnnotationBuilder class hook
 * @author zeroturnaround
 * @author suchu
 * @since 2019/5/9 19:42
 */
public class MybatisMapperAnnotationBuilderCBP extends JavassistClassBytecodeProcessor {
    class LoadXmlResourceHook extends ExprEditor {
        LoadXmlResourceHook() {
        }

        public void edit(NewExpr e) throws CannotCompileException {
            String className = e.getClassName();
            if ("org.apache.ibatis.builder.xml.XMLMapperBuilder".equals(className) ||
                    "com.baomidou.mybatisplus.core.MybatisXMLMapperBuilder".equals(className)) {
                e.replace("{  $_ = $proceed($$);  if ($2 instanceof JrConfiguration) {    SqlMapReloader reloader = ((JrConfiguration) $2).getReloader();    if (reloader != null) {      reloader.addMapping(Resources.getResourceURL($3), $3);    }  }}");
            }
        }

        public void edit(MethodCall m) throws CannotCompileException {
            if ("getResourceAsStream".equals(m.getMethodName()) && "org.apache.ibatis.io.Resources".equals(m.getClassName())) {
                m.replace("{  try {    $_ = $proceed($$);  } catch (java.io.IOException ioe) {    if (this.configuration instanceof " + Constants.JrConfigurationClass + ") {      SqlMapReloader reloader = ((JrConfiguration) this.configuration).getReloader();      if (reloader != null && reloader.mappingsLoadedFromSameLocation()) {        String _urlString = reloader.buildUrlBasedOnFirstMapping(type);        String _path = reloader.buildPathBasedOnFirstMapping(type);        java.io.InputStream _in = null;        try {          _in = Resources.getUrlAsStream(_urlString);        } catch (java.io.IOException ignore) {}        if (_in != null) {          try {            XMLMapperBuilder _builder = new XMLMapperBuilder(_in, this.configuration, _path, this.configuration.getSqlFragments());            _builder.parse();          } catch (Exception e) {            throw new RuntimeException(\"Failed to parse mapping resource: '\" + _path + \"'\", e);          } finally {            ErrorContext.instance().reset();          }          reloader.addMapping(new java.net.URL(_urlString), _path);        }      }    }  }}");
            }
        }
    }

    class ParseHook extends ExprEditor {
        ParseHook() {
        }

        public void edit(MethodCall m) throws CannotCompileException {
            if ("isResourceLoaded".equals(m.getMethodName())) {
                m.replace("$_ = !SqlMapReloader.isReloading() && $proceed($$);");
            }
        }
    }

    public void process(ClassPool cp, ClassLoader cl, CtClass ctClass) throws Exception {
        cp.importPackage("org.apache.ibatis.io");
        cp.importPackage("org.apache.ibatis.executor");
        cp.importPackage("org.apache.ibatis.builder.xml");
        cp.importPackage("org.zeroturnaround.jrebel.mybatis");
        ctClass.getDeclaredMethod("loadXmlResource").instrument(new LoadXmlResourceHook());
        processAnnotationReloading(cp, ctClass);
    }

    private void processAnnotationReloading(ClassPool cp, CtClass ctClass) throws NotFoundException, CannotCompileException {
        cp.importPackage("org.zeroturnaround.javarebel");
        cp.importPackage("org.zeroturnaround.javarebel.integration.util");
        cp.importPackage("java.util");
        ctClass.addInterface(cp.get(ClassEventListener.class.getName()));
        ctClass.addField(CtField.make("private static final Map __types = Collections.synchronizedMap(new WeakIdentityHashMap());", ctClass));
        for (CtConstructor c : ctClass.getDeclaredConstructors()) {
            if (c.callsSuper()) {
                c.insertAfter("{  if (!__types.containsKey(type)) {    __types.put(type, null);    ReloaderFactory.getInstance().addHierarchyReloadListener(type, WeakUtil.weak(ClassLoaderLocalUtil.bind($0, type.getClassLoader())));  }}");
            }
        }
        ctClass.addMethod(CtNewMethod.make("public int priority() {  return ClassEventListener.PRIORITY_DEFAULT;}", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void onClassEvent(int eventType, Class clazz) {  SqlMapReloader.enterReloading();  try {    parse();  }  finally {    SqlMapReloader.exitReloading();    ErrorContext.instance().reset();  }}", ctClass));
        ctClass.getDeclaredMethod("parse").instrument(new ParseHook());
    }
}
