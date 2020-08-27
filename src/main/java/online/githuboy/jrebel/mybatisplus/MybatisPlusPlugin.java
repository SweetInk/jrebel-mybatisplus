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
    private final static String MP_MARK_NAME = ".mybatisplus-jr-mark_";
    private final static String MP_3_4_0_ = ".mybatisplusV340-jr-mark_";
    private final static File mp_mark = new File(MP_MARK_NAME);
    private final static File mp_v340_mark = new File(MP_3_4_0_);

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
        //if there has MybatisPlus ClassResource
        if (mp_mark.exists()) {
            log.infoEcho("Add CBP for mybatis-plus core classes...");
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisConfiguration", new MybatisConfigurationCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.MybatisMapperAnnotationBuilder", new MybatisMapperAnnotationBuilderCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean", new MybatisSqlSessionFactoryBeanCBP());
            integration.addIntegrationProcessor(classLoader, "com.baomidou.mybatisplus.core.override.MybatisMapperProxy", new MybatisMapperProxyCBP());
            mp_mark.delete();
            if (mp_v340_mark.exists()) {
                log.infoEcho("Detected mybatis-plus version is v3.4.0+, add special CBPs...");
                integration.addIntegrationProcessor("com.baomidou.mybatisplus.core.MybatisConfiguration$StrictMap", new StrictMapCBP());
                mp_v340_mark.delete();
            }
        }else{
            log.infoEcho("Cannot find mybatis-plus classes in the classpath,please check.");
        }
    }

    private void configMybatisProcessor(Integration integration, ClassLoader classLoader) {
        integration.addIntegrationProcessor(classLoader, "org.apache.ibatis.builder.xml.XMLMapperBuilder", new XMLMapperBuilderCBP());
    }

    @Override
    public boolean checkDependencies(ClassLoader classLoader, ClassResourceSource classResourceSource) {
        checkMybatisPlus(classResourceSource);
        checkMybatisPlusV340(classResourceSource);
        return classResourceSource.getClassResource("org.apache.ibatis.session.SqlSessionFactoryBuilder") != null;
    }

    private void checkMybatisPlus(ClassResourceSource classResourceSource) {
        boolean hasMp = classResourceSource.getClassResource("com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder") != null;
        tryCreateThenClean(hasMp, mp_mark);
    }

    private void checkMybatisPlusV340(ClassResourceSource classResourceSource) {
        boolean v340 = classResourceSource.getClassResource("com.baomidou.mybatisplus.core.MybatisConfiguration$StrictMap") != null;
        tryCreateThenClean(v340, mp_v340_mark);
    }

    private void tryCreateThenClean(boolean clzExist, File markFile) {
        if (clzExist) {
            if (!markFile.exists()) {
                try {
                    markFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (markFile.exists()) {
                markFile.delete();
            }
        }
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
