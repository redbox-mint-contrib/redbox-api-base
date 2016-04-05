package au.com.redboxresearchdata.harvester.redbox

import org.apache.camel.builder.*
import org.apache.camel.model.rest.*
import org.apache.camel.processor.interceptor.*
import org.apache.camel.*
import org.springframework.beans.factory.annotation.*
import org.apache.camel.model.dataformat.*
import groovy.util.*
import javax.script.*
import java.io.*
import org.apache.camel.spring.boot.FatJarRouter
import org.springframework.boot.autoconfigure.SpringBootApplication
/**
 *
 * ReDBox Integration API Framework
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
@SpringBootApplication
class ReDBoxApiRouteBuilder extends FatJarRouter {

  
  @Override
  public void configure() {
    // General Config - TODO: replace with Harvester Config Utility 
    def env = System.properties.'env' ? System.properties.'env' : 'production'
    def configPath = System.getProperty('redboxApiConfig')
    log.info "Using configPath: ${configPath}"
    def config = new ConfigSlurper(env).parse(new File(configPath).text)
    log.info "Using environment: ${env}"
    log.debug config.toString()
    // Logging config
    if (config.logging.trace) {
      getContext().getProperties().put(Exchange.LOG_DEBUG_BODY_STREAMS, "true");
      getContext().setTracing(true)
      Tracer tracer = new Tracer()
      getContext().addInterceptStrategy(tracer)
    }
    def bindings = new SimpleBindings([config:config, log:log, routeBuilder:this])
    ScriptEngineManager manager = new ScriptEngineManager()
    def engine = manager.getEngineByName('groovy')
    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
    def buildRouteScript = (config.baseDir ? config.baseDir : "") + config.buildRoute
    log.info "Creating routes using script: ${buildRouteScript}"
    engine.eval(new File(buildRouteScript).text)
  }  
}
