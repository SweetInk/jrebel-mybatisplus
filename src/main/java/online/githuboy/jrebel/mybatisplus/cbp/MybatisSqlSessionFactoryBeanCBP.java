package online.githuboy.jrebel.mybatisplus.cbp;


import org.zeroturnaround.bundled.javassist.*;
import org.zeroturnaround.bundled.javassist.expr.ExprEditor;
import org.zeroturnaround.bundled.javassist.expr.FieldAccess;
import org.zeroturnaround.bundled.javassist.expr.MethodCall;
import org.zeroturnaround.bundled.javassist.expr.NewExpr;
import org.zeroturnaround.javarebel.ConfigurationFactory;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
import org.zeroturnaround.javarebel.integration.util.JavassistUtil;

/**
 * MybatisSqlSessionFactoryBean class hook
 *
 * @author zeroturnaround
 * @author suchu
 * @since 2019/05/08 20:31
 */
public class MybatisSqlSessionFactoryBeanCBP extends JavassistClassBytecodeProcessor {
    private static final String LOGGER = LoggerFactory.class.getName() + ".getLogger(\"MyBatisPlus\")";

    public MybatisSqlSessionFactoryBeanCBP() {
    }

    public void process(ClassPool cp, ClassLoader cl, CtClass ctClass) throws Exception {
        if (!ConfigurationFactory.getInstance().isPluginEnabled("spring_plugin")) {
            LoggerFactory.getLogger("MyBatisPlus").warn("MyBatisPlus Spring integration requires Spring plugin, which is currently disabled");
        } else {
            // SqlMapReloader.HAS_MYBATIS_SPRING = true;
            cp.importPackage("org.apache.ibatis.builder.xml");
            cp.importPackage("org.apache.ibatis.session");
            cp.importPackage("org.springframework.beans");
            cp.importPackage("org.springframework.beans.factory.support");
            cp.importPackage("org.springframework.core.io");
            cp.importPackage("java.util");
            cp.importPackage("java.io");
            this.createRegisterMapperLocationMethod(cp, ctClass);
            this.makeBeanFactoryAware(cp, ctClass);
            this.makeRepopulatingBean(cp, ctClass);
            CtMethod m = ctClass.getDeclaredMethod("buildSqlSessionFactory");
            m.insertBefore("org.zeroturnaround.jrebel.mybatis.SqlMapReloader.HAS_MYBATIS_SPRING = true;");
            m.addLocalVariable("__resource", cp.get("org.springframework.core.io.Resource"));
            m.insertBefore("{  __resource = null;}");
            m.instrument(new ExprEditor() {
                private int count = 0;

                public void edit(NewExpr e) throws CannotCompileException {
                    if ("org.apache.ibatis.builder.xml.XMLMapperBuilder".equals(e.getClassName())) {
                        e.replace("{  $_ = $proceed($$);  registerMapperLocationToReloader($2, __resource, $3);}");
                    }

                }

                public void edit(MethodCall m) throws CannotCompileException {
                    String methodName = m.getMethodName();
                    if ("getInputStream".equals(methodName) && "org.springframework.core.io.Resource".equals(m.getClassName())) {
                        m.replace("{  __resource = $0;  $_ = $proceed($$);}");
                    }

                }

                public void edit(FieldAccess f) throws CannotCompileException {
                    if (f.getFieldName().equals("typeAliasesPackage") && ++this.count == 1) {
                        f.replace("$_ = $proceed($$);if ($0 instanceof " + Constants.JrSqlSessionFactoryBeanClass + ") {  " + Constants.SqlMapReloaderClass + " reloader = ((" + Constants.JrConfigurationClass + ") configuration).getReloader();  reloader.registerSqlSessionFactoryBean((" + Constants.JrSqlSessionFactoryBeanClass + ")$0);}");
                    }

                }
            });
        }
    }

    private void createRegisterMapperLocationMethod(ClassPool cp, CtClass ctClass) throws CannotCompileException {
        ctClass.addMethod(CtNewMethod.make("public void registerMapperLocationToReloader(Configuration configuration, Resource mapperLocation, String name) {  if (configuration instanceof " + Constants.JrConfigurationClass + ") {    " + Constants.SqlMapReloaderClass + " reloader = ((" + Constants.JrConfigurationClass + ") configuration).getReloader();    if (reloader != null && mapperLocation != null) {      java.net.URL resourceUrl = null;      try {        resourceUrl = mapperLocation.getURL();      } catch (Exception e) {        " + LOGGER + ".error(\"Unable to get URL from resource. class=\" + mapperLocation.getClass().getName() + \", \" +                              \"description='\" + mapperLocation.getDescription() + \"'\", e);      }      if (resourceUrl != null) {        reloader.addMapping(resourceUrl, name);      }    }  }}", ctClass));
    }

    private void makeRepopulatingBean(ClassPool cp, CtClass ctClass) throws NotFoundException, CannotCompileException {
        ctClass.addInterface(cp.get("org.springframework.beans.JrRepopulatingBean"));
        ctClass.addInterface(cp.get("org.zeroturnaround.jrebel.mybatis.JrSqlSessionFactoryBean"));
        ctClass.addField(CtField.make("private volatile String __name;", ctClass));
        ctClass.addField(CtField.make("private volatile RootBeanDefinition __mbd;", ctClass));
        ctClass.addField(CtField.make("private volatile BeanWrapper __bw;", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void setPopulateSources(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw) { __name = beanName; __bw = bw; if (mbd instanceof RootBeanDefinition) {   __mbd = (RootBeanDefinition)mbd; } else if (mbd != null) {   " + LOGGER + ".info(\"setPopulateSources wrong mbd type: \" + mbd.getClass() + \" (not reloadable)\"); }}", ctClass));
        boolean useInputStream = JavassistUtil.hasDeclaredConstructor(cp, "org.apache.ibatis.builder.xml.XMLMapperBuilder", new String[]{"java.io.InputStream", "org.apache.ibatis.session.Configuration", "java.lang.String", "java.util.Map"});
        ctClass.addMethod(CtNewMethod.make("private void addMapperLocationsToConfiguration(List mapperLocations, Configuration configuration) {  Iterator it = mapperLocations.iterator();  while (it.hasNext()) {    Resource mapperLocation = (Resource)it.next();    if (mapperLocation == null) {      continue;    }        try {      XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(" + (useInputStream ? "mapperLocation.getInputStream()" : "new InputStreamReader(mapperLocation.getInputStream())") + ",configuration, mapperLocation.toString(), configuration.getSqlFragments());      registerMapperLocationToReloader(configuration, mapperLocation, mapperLocation.toString());      xmlMapperBuilder.parse();      " + LOGGER + ".info(\"Successfully added: \" + mapperLocation);    } catch (Exception e) {      " + LOGGER + ".error(\"Can't parse mapping: \" + mapperLocation, e);    }  }}", ctClass));
        boolean reRegisterAlias = false;

        String superType;
        try {
            ctClass.getDeclaredField("typeAliasesPackage");
            reRegisterAlias = true;
            ctClass.getDeclaredField("typeAliasesSuperType");
            superType = ", typeAliasesSuperType == null ? Object.class : typeAliasesSuperType";
        } catch (NotFoundException var7) {
            superType = "";
        }

        if (reRegisterAlias) {
            ctClass.addMethod(CtNewMethod.make("private void reRegisterAliases(Configuration configuration) { if (org.springframework.util.StringUtils.hasLength(this.typeAliasesPackage)) {   String[] typeAliasPackageArray = org.springframework.util.StringUtils.tokenizeToStringArray(this.typeAliasesPackage, org.springframework.context.ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);   for(int i = 0; i < typeAliasPackageArray.length; i++) {     String packageToScan = typeAliasPackageArray[i];     configuration.getTypeAliasRegistry().registerAliases(packageToScan " + superType + ");   } }}", ctClass));
        } else {
            LoggerFactory.getLogger("MyBatis").warn("Not ReRegistering Aliases, assuming this is a version 1.0.0 of mybatis-spring.");
            ctClass.addMethod(CtNewMethod.make("private void reRegisterAliases(Configuration configuration) {}", ctClass));
        }

        ctClass.addMethod(CtNewMethod.make("private void findAndAddNewMapperLocations(Resource[] mapperLocations, Resource[] previousMapperLocations, Configuration configuration) {   if (mapperLocations == null) {     return;   }   List existing = previousMapperLocations != null ? Arrays.asList(previousMapperLocations) : Collections.emptyList();   List newList = new ArrayList(1);   for (int i = 0; i < mapperLocations.length; i++) {     if (!existing.contains(mapperLocations[i])) {       newList.add(mapperLocations[i]);     }   }   if (newList.isEmpty()) {     return;   }   reRegisterAliases(configuration);   " + LOGGER + ".info(\"New mapperLocations discovered: \" + newList);   addMapperLocationsToConfiguration(newList, configuration);}", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void reloadProperties(Configuration configuration) {  try {    if (this.jrBeanFactory != null && __name != null && __mbd != null && __bw != null) {      Resource[] previousMapperLocations = this.mapperLocations;      this.jrBeanFactory.jrPopulateBean(__name, __mbd, __bw);      findAndAddNewMapperLocations(this.mapperLocations, previousMapperLocations, configuration);    }  } catch (Exception e) {    " + LOGGER + ".error(\"Failed to reinject properties to SqlSessionFactoryBean: '\" + __name + \"'\", e);  }}", ctClass));
    }

    private void makeBeanFactoryAware(ClassPool cp, CtClass ctClass) throws NotFoundException, CannotCompileException {
        cp.importPackage("org.springframework.beans.factory");
        cp.importPackage("org.springframework.beans.JrDefaultListableBeanFactory");
        ctClass.addInterface(cp.get("org.springframework.beans.factory.BeanFactoryAware"));
        ctClass.addField(CtField.make("private volatile JrDefaultListableBeanFactory jrBeanFactory;", ctClass));
        ctClass.addMethod(CtNewMethod.make("public void setBeanFactory(BeanFactory beanFactory) { if (beanFactory instanceof JrDefaultListableBeanFactory) {   this.jrBeanFactory = (JrDefaultListableBeanFactory)beanFactory; }}", ctClass));
    }
}