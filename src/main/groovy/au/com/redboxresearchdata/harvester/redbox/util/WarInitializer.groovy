package au.com.redboxresearchdata.harvester.redbox.util

import au.com.redboxresearchdata.harvester.redbox.*
import org.apache.camel.spring.boot.FatJarRouter;
import org.apache.camel.spring.boot.FatWarInitializer;

/**
 *
 * Allows creation of the WAR file.
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
public class WarInitializer extends FatWarInitializer {

    @Override
    protected Class<? extends FatJarRouter> routerClass() {
        return ReDBoxApiRouteBuilder.class;
    }

}