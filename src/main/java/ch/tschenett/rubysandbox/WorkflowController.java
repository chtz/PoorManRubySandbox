package ch.tschenett.rubysandbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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
 * sample:
 * WF_ID=`curl -s --header "Content-Type:text/plain" --data-binary @test3_wf.rb http://www.sandbox.p.iraten.ch/wf`
 * IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary "" http://www.sandbox.p.iraten.ch/wf/$WF_ID`
 * IN_ID=`curl -s --header "Content-Type:text/plain" --data-binary "" http://www.sandbox.p.iraten.ch/wf/$WF_ID/$IN_ID`
 */
@RestController
public class WorkflowController {
	private static final Logger log = LoggerFactory.getLogger(WorkflowController.class);
	
	@Value("${wf.command}")
    private String wfCommand;
	
	@Value("${wf.Signalcommand}")
    private String wfSignalCommand;
	
	@Value("${wf.readThreads}")
    private int wfReadThreads;
	
	@Value("${wf.workingDirectory}")
    private String wfWorkingDirectory;
	
	@Value("${wf.scriptDirectory}")
    private String wfScriptDirectory;
	
	@Value("${wf.stateDirectory}")
    private String wfStateDirectory;
	
	private ExecutorService wfReadExecutorService;
	
	@PostConstruct
	public void init() {
		wfReadExecutorService = Executors.newFixedThreadPool(wfReadThreads);
	}
	
	@RequestMapping(value = "/wf", method = RequestMethod.POST)
	public void createScript(Reader req, Writer res) throws IOException {
		String scriptId = UUID.randomUUID().toString();
		
		File scriptDir = new File(wfScriptDirectory);
		File scriptFile = new File(scriptDir, scriptId + ".rb");
		FileWriter out = new FileWriter(scriptFile);
		try {
			IOUtils.copy(req, out);
		} finally {
			out.close();
		}
		
		res.write(scriptId);
		
		log.info("Wf created: {}", scriptId);
	}
	
	@RequestMapping(value = "/wf/{wfId}", method = RequestMethod.POST)
	public void executePostScript(HttpServletRequest request, OutputStream res, @PathVariable String wfId) throws IOException {    
		try {
			ProcessBuilder pb = new ProcessBuilder(wfCommand, wfFile(wfId).getCanonicalPath());
			
			pb.redirectErrorStream(true);
			pb.directory(new File(wfWorkingDirectory));
			
			final Process p = pb.start();
			
			final DataOutputStream out = new DataOutputStream(p.getOutputStream());
			
			wfReadExecutorService.execute(new Runnable() {
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
			
			String stateId = UUID.randomUUID().toString();
			
			File stateDir = new File(wfStateDirectory);
			File stateFile = new File(stateDir, stateId + ".json");
			BufferedOutputStream stateOut = new BufferedOutputStream(new FileOutputStream(stateFile));
			
			try {
				IOUtils.copy(p.getInputStream(), stateOut);
			} catch (IOException e) {
				log.warn("Failed to write response", e);
			} finally {
				try {
					p.getInputStream().close();
				} catch (IOException e) {
					//ignore
				}
				try {
					stateOut.close();
				} catch (IOException e) {
					//ignore
				}
				
			}
			
			p.waitFor();
			
			res.write(stateId.getBytes());
			
			log.info("Wf processed: {}", wfId);
		} catch (Exception e) {
			log.warn("Failed to process request", e);
		}
	}
	
	@RequestMapping(value = "/wf/{wfId}/{stateId}", method = RequestMethod.POST)
	public void executePostScript(HttpServletRequest request, OutputStream res, @PathVariable String wfId, @PathVariable String stateId) throws IOException {    
		try {
			ProcessBuilder pb = new ProcessBuilder(wfSignalCommand, wfFile(wfId).getCanonicalPath());
			
			pb.redirectErrorStream(true);
			pb.directory(new File(wfWorkingDirectory));
			
			final Process p = pb.start();
			
			final DataOutputStream out = new DataOutputStream(p.getOutputStream());
			BufferedInputStream stateIn = new BufferedInputStream(new FileInputStream(stateFile(stateId)));
			try {
				IOUtils.copy(stateIn, out);
			} catch (IOException e) {
				log.warn("Failed to read request", e);
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					//ignore
				}
				try {
					stateIn.close();
				} catch (IOException e) {
					//ignore
				}
			}
			
			BufferedOutputStream stateOut = new BufferedOutputStream(new FileOutputStream(stateFile(stateId)));
			try {
				IOUtils.copy(p.getInputStream(), stateOut);
			} catch (IOException e) {
				log.warn("Failed to write response", e);
			} finally {
				try {
					p.getInputStream().close();
				} catch (IOException e) {
					//ignore
				}
				try {
					stateOut.close();
				} catch (IOException e) {
					//ignore
				}
				
			}
			
			p.waitFor();
			
			res.write(stateId.getBytes());
			
			log.info("Wf processed: {}", wfId);
		} catch (Exception e) {
			log.warn("Failed to process request", e);
		}
	}

	private File wfFile(String scriptId) throws IOException, FileNotFoundException {
		File scriptDir = new File(wfScriptDirectory);
		File scriptFile = new File(scriptDir, scriptId + ".rb");
		
		if (!scriptFile.getCanonicalPath().startsWith(scriptDir.getCanonicalPath())) {
			throw new SecurityException();
		}
		
		if (!scriptFile.exists() || !scriptFile.canRead()) {
			throw new FileNotFoundException();
		}
		
		return scriptFile;
	}
	
	private File stateFile(String scriptId) throws IOException, FileNotFoundException {
		File scriptDir = new File(wfStateDirectory);
		File scriptFile = new File(scriptDir, scriptId + ".json");
		
		if (!scriptFile.getCanonicalPath().startsWith(scriptDir.getCanonicalPath())) {
			throw new SecurityException();
		}
		
		if (!scriptFile.exists() || !scriptFile.canRead()) {
			throw new FileNotFoundException();
		}
		
		return scriptFile;
	}
}
