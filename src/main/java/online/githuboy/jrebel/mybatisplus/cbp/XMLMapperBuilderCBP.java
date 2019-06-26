package online.githuboy.jrebel.mybatisplus.cbp;

import org.zeroturnaround.bundled.javassist.*;
import org.zeroturnaround.bundled.javassist.expr.ExprEditor;
import org.zeroturnaround.bundled.javassist.expr.MethodCall;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * XMLMapperBuilder class hook
 *
 * @author suchu
 * @since 2019/6/26 11:25
 */
public class XMLMapperBuilderCBP extends JavassistClassBytecodeProcessor {

    @Override
    public void process(ClassPool cp, ClassLoader cl, CtClass ctClass) throws Exception {
        ctClass.addField(CtField.make("private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(org.apache.ibatis.builder.xml.XMLMapperBuilder.class);", ctClass));
        addClearMethod(ctClass);
        hookConfigurationElementMethod(ctClass);
    }

    private void hookConfigurationElementMethod(CtClass ctClass) throws NotFoundException, CannotCompileException {

        ctClass.getDeclaredMethod("configurationElement").instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if ("setCurrentNamespace".equals(m.getMethodName())) {
                    m.replace("{$_=$proceed($$);this.clearInCompleteStatement();}");
                }
            }
        });
    }

    private void addClearMethod(CtClass ctClass) throws CannotCompileException {
        ctClass.addMethod(CtNewMethod.make("    public void clearInCompleteStatement() {\n" +
                "        java.util.Collection incompleteStatements = this.configuration.getIncompleteStatements();\n" +
                "        synchronized (incompleteStatements) {\n" +
                "            java.util.Iterator iterator = incompleteStatements.iterator();\n" +
                "            while (iterator.hasNext()) {\n" +
                "                org.apache.ibatis.builder.xml.XMLStatementBuilder statementBuilder = (org.apache.ibatis.builder.xml.XMLStatementBuilder)iterator.next();\n" +
                "                try {\n" +
                "                    java.lang.reflect.Field field = statementBuilder.getClass().getDeclaredField(\"builderAssistant\");\n" +
                "                    field.setAccessible(true);\n" +
                "                    org.apache.ibatis.builder.MapperBuilderAssistant tempBuilderAssistant = (org.apache.ibatis.builder.MapperBuilderAssistant) field.get(statementBuilder);\n" +
                "                    if (null != tempBuilderAssistant) {\n" +
                "                        if (tempBuilderAssistant.getCurrentNamespace().equals(builderAssistant.getCurrentNamespace())) {\n" +
                "                        logger.info(\"Cleaning {}'s incomplete statement\",builderAssistant.getCurrentNamespace());\n" +
                "                            iterator.remove();\n" +
                "                        }\n" +
                "                    }\n" +
                "                }  catch (Exception e) {\n" +
                "                    e.printStackTrace();\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }", ctClass));

    }
}
