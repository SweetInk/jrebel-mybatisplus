package online.githuboy.jrebel.mybatisplus;

import online.githuboy.jrebel.mybatisplus.cbp.*;
import org.zeroturnaround.javarebel.*;

import java.io.File;
import java.io.IOException;

/**
 * Plugin Main entry
 *
 * @author suchu
 * @since 2019/5/9 17:56
 */
public class MybatisPlusPlugin implements Plugin {
    private static final Logger log = LoggerFactory.getLogger("MyBatisPlus");
    private final static String MP_MARK_NAME = ".mybatisplus-jr-mark";

    @Override
    public void preinit() {
        log.infoEcho("Ready config JRebel MybatisPlus plugin...");
        ClassLoader classLoader = MybatisPlusPlugin.class.getClassLoader();
        Integration integration = IntegrationFactory.getInstance();
        //register class processor
        configMybatisPlusProcessor(integration, classLoader);
        configMybatisProcessor(integration, classLoader);
    }

    private void configMybatisPlusProcessor(Integration integration, ClassLoader classLoader) {
        File mark = new File(MP_MARK_NAME);
        //if there has MybatisPlus ClassResource
        if (mark.exists()) {
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisConfiguration", new MybatisConfigurationCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisMapperAnnotationBuilder", new MybatisMapperAnnotationBuilderCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean", new MybatisSqlSessionFactoryBeanCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.override.MybatisMapperProxy", new MybatisMapperProxyCBP());
            mark.delete();
        }
    }

    private void configMybatisProcessor(Integration integration, ClassLoader classLoader) {
        integration.addIntegrationProcessor(classLoader, "org.apache.ibatis.builder.xml.XMLMapperBuilder", new XMLMapperBuilderCBP());
    }

    @Override
    public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource) {
        boolean hasMp = classResourceSource.getClassResource("com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder") != null;
        File mark = new File(MP_MARK_NAME);
        if (hasMp) {
            if (!mark.exists()) {
                try {
                    mark.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (mark.exists()) {
                mark.delete();
            }
        }
        return classResourceSource.getClassResource("org.apache.ibatis.session.SqlSessionFactoryBuilder") != null;
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
