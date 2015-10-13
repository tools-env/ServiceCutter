package ch.hsr.servicestoolkit.editor.web.rest;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;

import ch.hsr.servicestoolkit.editor.security.AuthoritiesConstants;

@RestController
@RequestMapping("/api/editor")
@Secured(AuthoritiesConstants.USER)
public class EditorResource {

	private final Logger log = LoggerFactory.getLogger(EditorResource.class);
	private final RestTemplate rest = new RestTemplate();
	@Value("http://${application.links.engine.host}:${application.links.engine.port}")
	private String engineUrl;

	@RequestMapping(value = "/model", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<?> uploadModelFile(@RequestParam("file") final MultipartFile file) {
		ResponseEntity<?> result = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		try {
			String theString = IOUtils.toString(file.getInputStream());
			log.trace("file content:{}", theString);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> requestEntity = new HttpEntity<Object>(theString, headers);
			@SuppressWarnings("rawtypes")
			ResponseEntity<Map> responseEntity = rest.exchange(engineUrl + "/engine/import", HttpMethod.POST, requestEntity, Map.class);
			@SuppressWarnings("unchecked")
			Map<String, Object> serviceResponse = responseEntity.getBody();
			log.debug("importer response: {}", serviceResponse);
			result = new ResponseEntity<>(serviceResponse, HttpStatus.CREATED);
		} catch (IOException e) {
			log.error("", e);
		}
		return result;
	}

	@RequestMapping(value = "/model/{modelId}/transactions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> uploadBusinessTransactionFile(@RequestParam("file") final MultipartFile file, @PathVariable("modelId") final String modelId) {
		ResponseEntity<Void> result = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		try {
			String theString = IOUtils.toString(file.getInputStream());
			log.debug("modelId:{}", modelId);
			log.debug("file content:{}", theString);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<?> requestEntity = new HttpEntity<Object>(theString, headers);
			String path = engineUrl + "/engine/import/" + modelId + "/businessTransactions/";
			log.debug("post transactions on {}", path);
			ResponseEntity<Void> responseEntity = rest.exchange(path, HttpMethod.POST, requestEntity, Void.class);
			result = new ResponseEntity<Void>(responseEntity.getStatusCode());
		} catch (IOException e) {
			log.error("", e);
		}
		return result;
	}

}
