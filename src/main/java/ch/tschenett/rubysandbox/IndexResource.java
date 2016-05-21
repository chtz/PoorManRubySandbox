package ch.tschenett.rubysandbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * sample: curl --header "Content-Type:application/octet-stream" --data-binary @../rubyRunContainer/sample.txt localhost:8080
 */
@RestController
public class IndexResource {
	private static final Logger log = LoggerFactory.getLogger(IndexResource.class);
	
	@Value("${ruby.command}")
    private String rubyCommand;
	
	@Value("${ruby.timeout}")
    private String rubyTimeout;
	
	@Value("${ruby.workingDirectory}")
    private String rubyWorkingDirectory;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public void addPet(InputStream req, OutputStream res) throws IOException {    
		try {
			ProcessBuilder pb = new ProcessBuilder(rubyCommand, "sample.rb", rubyTimeout);
			pb.directory(new File(rubyWorkingDirectory));
			
			final Process p = pb.start();
			
			Thread reqThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						IOUtils.copy(req, p.getOutputStream());
					} catch (IOException e) {
						log.warn("Failed to read request", e);
					} finally {
						try {
							p.getOutputStream().close();
						} catch (IOException e) {
							//ignore
						}
					}
				}
			});
			
			reqThread.start();
			
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
			
			log.info("Request processed");
		} catch (Exception e) {
			log.warn("Failed to process request", e);
		}
	}
}
