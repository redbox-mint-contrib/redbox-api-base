package au.com.redboxresearchdata.harvester.redbox.util

import org.apache.camel.builder.*
import org.apache.camel.model.rest.*
import org.apache.camel.processor.interceptor.*
import org.apache.camel.*

import javax.servlet.http.*
import java.nio.channels.*
  
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.*
import org.apache.camel.component.netty4.NettyConstants
import org.apache.camel.component.netty4.http.*

import org.apache.camel.model.dataformat.*
import org.apache.camel.processor.aggregate.*
import org.apache.camel.util.toolbox.*

import java.util.logging.Logger

/**
 *
 * Handles Multipart form data uploads. Expects a `config.upload.procdir` where uploads are copied to.
 * 
 *
 * @author <a target='_' href='https://github.com/shilob'>Shilo Banihit</a>
 *
 */
class Netty4HttpMultipartParser {
  def config
  def log = Logger.getLogger(this.getClass().toString())
  /**
  * Parses HTTP request and saves files to the 'config.upload.procdir'
  *
  * @returns A map containing 'files' and 'attributes'. 
  * 
  * Each 'file' entry has the ff. attributes: 
  *  file - the File object
  *  path - the File's absolute path
  *  name - the File name
  */
  def parseAndSave(exchange) {
    def request = exchange.in.getHttpRequest()
    def msg = request
    def channelHandlerContext = exchange.getIn().getHeader(NettyConstants.NETTY_CHANNEL_HANDLER_CONTEXT)
    def files = []
    def attribs = [:]
    if (request.getMethod().equals(HttpMethod.POST)) {
      log.debug "****************Parsing HTTP Post *****************"
      HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE)
      def decoder = new HttpPostRequestDecoder(factory, request)
      decoder.setDiscardThreshold(0)
      if (decoder != null) {
        log.debug msg.getClass().toString()
        if (msg instanceof HttpContent) {
          HttpContent chunk = (HttpContent) msg;
          readChunk(channelHandlerContext, decoder, files, attribs)
          if (chunk instanceof LastHttpContent) {
            request = null
            decoder.destroy()
            decoder = null
          }
        }
      }
    }
    return [files:files, attributes:attribs]
  }
  
  void readChunk(ctx, decoder, files, attribs) throws IOException {
    log.debug "Reading Chunk...."
    while (decoder.hasNext()) {
      InterfaceHttpData data = decoder.next();
      log.debug "Field:" + data.getName()
      if (data != null) {
        try {
          switch (data.getClass()) {
            case MixedAttribute:
                log.debug "Is Attribute: ${data.getValue()}" 
                attribs[data.getName()] = data.getValue()
                break;
            case MixedFileUpload:    
                def fileUpload = data;
                log.debug "Is File, copying to ${config.upload.procdir}"
                File parentDir = new File((String)config.upload.procdir)
                parentDir.mkdirs()
                File file = new File(parentDir, fileUpload.getFilename())
                if (!file.exists()) {
                    file.createNewFile();
                }
                def inputChannel
                def outputChannel = new FileOutputStream(file).getChannel()                        
                def dataSize = 0
                if (data.isInMemory()) {
                  def dataByteArr = fileUpload.get()
                  inputChannel = Channels.newChannel(new ByteArrayInputStream(dataByteArr))
                  dataSize = dataByteArr.length
                } else {
                  inputChannel = new FileInputStream(fileUpload.getFile()).getChannel()
                  dataSize = inputChannel.size()
                }
                outputChannel.transferFrom(inputChannel, 0, dataSize)
                log.debug "File copied to ${file.getAbsolutePath()}"
                files << [file:file, path:file.getAbsolutePath(), name: fileUpload.getFilename()]
                break
              default:
                log.debug "Don't know what to do: ${data.getName()} ==> ${data.getClass()}"
            }
        } catch (InterruptedException e) {
            e.printStackTrace()
        } finally {
            data.release()
        }
      }
    }
  }
  
  def moveFile(parentDirPath, from, to) {
    log.debug "Moving '${from.getAbsolutePath()}' to '${parentDirPath + '/' + to}'"
    File parentDir = new File(parentDirPath)
    parentDir.mkdirs()
    File file = new File(parentDir, to)
    if (!file.exists()) {
        file.createNewFile()
    }
    def inputChannel = new FileInputStream(from).getChannel()
    def outputChannel = new FileOutputStream(file).getChannel()                        
    def dataSize = inputChannel.size()
    outputChannel.transferFrom(inputChannel, 0, dataSize)
    log.debug "File moved from ${from} to ${file.getAbsolutePath()}"
  }
}