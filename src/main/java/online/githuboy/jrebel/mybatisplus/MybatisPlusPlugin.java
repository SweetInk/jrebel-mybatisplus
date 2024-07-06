package online.githuboy.jrebel.mybatisplus;

import online.githuboy.jrebel.mybatisplus.cbp.*;
import org.zeroturnaround.javarebel.*;

import java.io.IOException;
import java.util.Properties;

/**
 * Plugin Main entry
 *
 * @author suchu
 * @author andresluuk
 * @since 2019/5/9 17:56
 */
public class MybatisPlusPlugin implements Plugin {
    private static final Logger log = LoggerFactory.getLogger("MyBatisPlus");

    @Override
    public void preinit() {
        Properties p = new Properties();
        String version = "";
        try {
            p.load(getClass().getClassLoader().getResourceAsStream("META-INF/maven/online.githuboy/jr-mybatisplus/pom.properties"));
            version = p.getProperty("version");
        } catch (IOException e) {
            log.error("Can not read jr-mybatisplus/pom.properties:", e.getMessage());
        }
        log.infoEcho("Ready config JRebel MybatisPlus plugin(" + version + ")...");
        ClassLoader classLoader = MybatisPlusPlugin.class.getClassLoader();
        Integration integration = IntegrationFactory.getInstance();
        //register class processor
        configMybatisPlusProcessor(integration, classLoader);
        configMybatisProcessor(integration, classLoader);
    }

    private void configMybatisPlusProcessor(Integration integration, ClassLoader classLoader) {
        //if there has MybatisPlus ClassResource
        log.infoEcho("Add CBP for mybatis-plus core classes...");
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisConfiguration", new MybatisConfigurationCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisMapperAnnotationBuilder", new MybatisMapperAnnotationBuilderCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean", new MybatisSqlSessionFactoryBeanCBP());
//        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.override.MybatisMapperProxy", new MybatisMapperProxyCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory", new MybatisMapperProxyFactoryCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisConfiguration$StrictMap", new StrictMapCBP());
    }

    private void configMybatisProcessor(Integration integration, ClassLoader classLoader) {
        integration.addIntegrationProcessor(classLoader, "org.apache.ibatis.builder.xml.XMLMapperBuilder", new XMLMapperBuilderCBP());
    }

    @Override
    public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource) {
        return classResourceSource.getClassResource("com.baomidou.mybatisplus.core.MybatisConfiguration") != null;
    }

    @Override
    public String getId() {
        return "mybatis_plus_plugin";
    }

    @Override
    public String getName() {
        return "MybatisPlus_plugin";
    }

    @Override
    public String getDescription() {
        return "<li>A hook plugin for Support MybatisPlus that reloads modified SQL maps.</li>";
    }

    @Override
    public String getAuthor() {
        return "suchu";
    }

    @Override
    public String getWebsite() {
        return "https://githuboy.online";
    }

    @Override
    public String getSupportedVersions() {
        return null;
    }

    @Override
    public String getTestedVersions() {
        return null;
    }
}
