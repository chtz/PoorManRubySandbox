package ch.tschenett.rubysandbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.UUID;

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
	
	@RequestMapping(value = "/wf", method = RequestMethod.POST)
	public void createWorkflowDefinition(Reader req, Writer res) throws IOException {
		try {
			String wfId = UUID.randomUUID().toString();
			
			FileWriter wfOut = new FileWriter(wfFile(wfId));
			try {
				IOUtils.copy(req, wfOut);
			} finally {
				wfOut.close();
			}
			
			res.write(wfId);
			
			log.info("Workflow Definition created: {}", wfId);
		}
		catch (Exception e) {
			log.warn("Failed to create Workflow Definition", e);
		}
	}
	
	@RequestMapping(value = "/wf/{wfId}", method = RequestMethod.POST)
	public void createWorkflowInstance(InputStream req, Writer res, @PathVariable String wfId) throws IOException {    
		try {
			Process p = process(wfCommand, wfId);
			
			copyToProcessClosing(p, req);
			
			String stateId = UUID.randomUUID().toString();
			
			copyFromProcessClosing(p, new BufferedOutputStream(new FileOutputStream(stateFile(stateId))));
			
			p.waitFor();
			
			res.write(stateId);
			
			log.info("Workflow Instance created: {}. Workflow Definition: {}", stateId, wfId);
		} catch (Exception e) {
			log.warn("Failed to create Workflow Instance", e);
		}
	}

	private void copyToProcessClosing(final Process p, InputStream in) {
		BufferedOutputStream out = new BufferedOutputStream(p.getOutputStream());
		try {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			log.warn("Failed to write to process", e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				//ignore
			}
			try {
				in.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
	
	private void copyFromProcessClosing(Process p, BufferedOutputStream out) {
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		try {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			log.warn("Failed to read from process", e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				//ignore
			}
			try {
				in.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
	
	@RequestMapping(value = "/wf/{wfId}/{stateId}", method = RequestMethod.POST)
	public void signalProcessInstance(InputStream req, Writer res, @PathVariable String wfId, @PathVariable String stateId) throws IOException {    
		try {
			Process p = process(wfSignalCommand, wfId);
			
			copyToProcessClosing(p, new BufferedInputStream(new FileInputStream(existingStateFile(stateId))));
			
			copyFromProcessClosing(p, new BufferedOutputStream(new FileOutputStream(existingStateFile(stateId))));
			
			p.waitFor();
			
			res.write(stateId);
			
			log.info("Workflow Instance signalled: {}. Workflow Definition: {}", stateId, wfId);
		} catch (Exception e) {
			log.warn("Failed to signal Workflow Instance", e);
		}
	}

	private Process process(String command, String wfId) throws IOException, FileNotFoundException {
		ProcessBuilder pb = new ProcessBuilder(command, existingWfFile(wfId).getCanonicalPath());
		pb.redirectErrorStream(true);
		pb.directory(new File(wfWorkingDirectory));
		
		return pb.start();
	}

	private File existingWfFile(String wfId) throws IOException, FileNotFoundException {
		return existingFile(wfScriptDirectory, wfId, ".rb");
	}
	
	private File wfFile(String wfId) throws IOException, FileNotFoundException {
		return file(wfScriptDirectory, wfId, ".rb");
	}
	
	private File existingStateFile(String stateId) throws IOException, FileNotFoundException {
		return existingFile(wfStateDirectory, stateId, ".json");
	}
	
	private File stateFile(String stateId) throws IOException, FileNotFoundException {
		return file(wfStateDirectory, stateId, ".json");
	}
	
	private File existingFile(String directoryName, String fileName, String fileExt) throws IOException, FileNotFoundException {
		File file = file(directoryName, fileName, fileExt);
		
		if (!file.exists() || !file.canRead()) {
			throw new FileNotFoundException(file.getName());
		}
		
		return file;
	}
	
	private File file(String directoryName, String fileName, String fileExt) throws IOException, FileNotFoundException {
		File dir = new File(directoryName);
		File file = new File(dir, fileName + fileExt);
		
		if (!file.getCanonicalPath().startsWith(dir.getCanonicalPath())) {
			throw new SecurityException();
		}
		
		return file;
	}
}
