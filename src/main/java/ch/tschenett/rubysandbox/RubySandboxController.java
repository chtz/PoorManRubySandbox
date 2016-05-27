package ch.tschenett.rubysandbox;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * samples:
 * <pre>
 * curl --header "Content-Type:text/plain" --data-binary @testdata/scripts/sample.rb http://www.sandbox.p.iraten.ch/
 * curl --header "Content-Type:text/plain" --data-binary @src/main/java/ch/tschenett/rubysandbox/RubySandboxApplication.java http://www.sandbox.p.iraten.ch/8df5e8ab-6cee-4339-ab2b-d3b7e4e51f7d 
 * </pre>
 */
@RestController
public class RubySandboxController {
	private static final Logger log = LoggerFactory.getLogger(RubySandboxController.class);
	
	@Value("${ruby.command}")
    private String rubyCommand;
	
	@Value("${ruby.timeout}")
    private String rubyTimeout;
	
	@Value("${ruby.readThreads}")
    private int rubyReadThreads;
	
	@Value("${ruby.workingDirectory}")
    private String rubyWorkingDirectory;
	
	@Value("${ruby.scriptDirectory}")
    private String rubyScriptDirectory;
	
	private ExecutorService rubyReadExecutorService;
	
	@PostConstruct
	public void init() {
		rubyReadExecutorService = Executors.newFixedThreadPool(rubyReadThreads);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public void createScript(Reader req, Writer res) throws IOException {
		String scriptId = UUID.randomUUID().toString();
		
		File scriptDir = new File(rubyScriptDirectory);
		File scriptFile = new File(scriptDir, scriptId + ".rb");
		FileWriter out = new FileWriter(scriptFile);
		try {
			IOUtils.copy(req, out);
		} finally {
			out.close();
		}
		
		res.write(scriptId);
		
		log.info("Script created: {}", scriptId);
	}
	
	@RequestMapping(value = "/{scriptId}", method = RequestMethod.GET)
	public void executeGetScript(HttpServletRequest request,  OutputStream res, @PathVariable String scriptId) throws IOException {
		executePostScript(request, res, scriptId);
	}
	
	@RequestMapping(value = "/{scriptId}", method = RequestMethod.POST)
	public void executePostScript(HttpServletRequest request, OutputStream res, @PathVariable String scriptId) throws IOException {    
		try {
			ProcessBuilder pb = new ProcessBuilder(rubyCommand, scriptFile(scriptId).getCanonicalPath(), rubyTimeout);
			
			pb.redirectErrorStream(true);
			pb.directory(new File(rubyWorkingDirectory));
			
			final Process p = pb.start();
			
			final DataOutputStream out = new DataOutputStream(p.getOutputStream());
			for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
				String key = e.nextElement();
				String value = request.getParameter(key);
				
				//pb.environment().put(key, value);
				out.writeBytes(key + "=" + value + "\n");
				
				log.info("Env {} = {}", key, value);
			}
			
			out.writeBytes("~~~<666~END~OF~SCRIPT~999>~~~\n");
			
			rubyReadExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						IOUtils.copy(request.getInputStream(), out);
					} catch (IOException e) {
						log.warn("Failed to read request", e);
					} finally {
						try {
							out.close();
						} catch (IOException e) {
							//ignore
						}
					}
				}
			});
			
			try {
				IOUtils.copy(p.getInputStream(), res);
			} catch (IOException e) {
				log.warn("Failed to write response", e);
			} finally {
				try {
					p.getInputStream().close();
				} catch (IOException e) {
					//ignore
				}
			}
			
			p.waitFor();
			
			log.info("Script processed: {}", scriptId);
		} catch (Exception e) {
			log.warn("Failed to process request", e);
		}
	}

	private File scriptFile(String scriptId) throws IOException, FileNotFoundException {
		File scriptDir = new File(rubyScriptDirectory);
		File scriptFile = new File(scriptDir, scriptId + ".rb");
		
		if (!scriptFile.getCanonicalPath().startsWith(scriptDir.getCanonicalPath())) {
			throw new SecurityException();
		}
		
		if (!scriptFile.exists() || !scriptFile.canRead()) {
			throw new FileNotFoundException();
		}
		
		return scriptFile;
	}
}
