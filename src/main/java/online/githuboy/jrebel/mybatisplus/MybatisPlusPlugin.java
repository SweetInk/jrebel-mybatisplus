package online.githuboy.jrebel.mybatisplus;

import online.githuboy.jrebel.mybatisplus.cbp.MybatisConfigurationCBP;
import online.githuboy.jrebel.mybatisplus.cbp.MybatisMapperAnnotationBuilderCBP;
import online.githuboy.jrebel.mybatisplus.cbp.MybatisMapperProxyCBP;
import online.githuboy.jrebel.mybatisplus.cbp.MybatisSqlSessionFactoryBeanCBP;
import org.zeroturnaround.javarebel.*;

/**
 * Plugin Main entry
 *
 * @author suchu
 * @since 2019/5/9 17:56
 */
public class MybatisPlusPlugin implements Plugin {

    @Override
    public void preinit() {
        LoggerFactory.getLogger("MyBatisPlus").info("Ready config jrebel mybatisplus plugins...");

        ClassLoader classLoader = MybatisPlusPlugin.class.getClassLoader();
        Integration integration = IntegrationFactory.getInstance();
        //register class processor
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisConfiguration", new MybatisConfigurationCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisMapperAnnotationBuilder", new MybatisMapperAnnotationBuilderCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean", new MybatisSqlSessionFactoryBeanCBP());
        integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.override.MybatisMapperProxy", new MybatisMapperProxyCBP());

    }

    @Override
    public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource) {
        return
         classResourceSource.getClassResource("com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder") != null;
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
        return "https://githuoby.online";
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
