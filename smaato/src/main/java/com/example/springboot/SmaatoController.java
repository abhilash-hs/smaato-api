package com.example.springboot;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/smaato/")
@EnableScheduling
/**
 * 
 * Rest Service which can accepts 10K req/sec. The configuration in
 * application.properties file
 *
 *
 */
public class SmaatoController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	 
	/**
	 * static hashmap to hold the data for one minute.
	 */
	static ConcurrentHashMap<Integer, String> concurrentHashMap = new ConcurrentHashMap<Integer, String>();

	/**
	 * 
	 * @param id       unique ID which is mandatory
	 * @param endpoint Optional Param - If provided, fire and http get request to
	 *                 the provided end point with the count of unique requests in
	 *                 the current minute as a query parameter
	 * @return string ok if success and failed otherwise
	 */
	@GetMapping("/accept")
	public String smaatoProc(@RequestParam(value = "id", required = true) String id,
			@RequestParam(value = "endpoint", required = false) String endpoint) {

		try {
			concurrentHashMap.put(Integer.parseInt(id), LocalDateTime.now().toString());

			if (null != endpoint) {
				RestTemplate restTemplate = new RestTemplate();

				String result = restTemplate.getForObject(endpoint + "?=" + concurrentHashMap.keySet().size(),
						String.class);

				LOGGER.debug("Response" + result);
			}
			return "ok";
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (RestClientException e) {

			e.printStackTrace();
		}

		return "failed";

	}

	/**
	 * Cron job which execute on every one minute which reads the number of unique
	 * entries from the data structure and write to log file once done, it
	 * clears/re-initialize the data structure
	 */
	@Scheduled(cron = "0/60 * * * * ?")
	private void timer() {
		final LocalDateTime start = LocalDateTime.now();
		// System.out.println("concurrentHashMap"+concurrentHashMap.keySet());
		LOGGER.debug("Request on every minute" + start + ">>>" + concurrentHashMap.keySet().size());
		concurrentHashMap.clear();
	}

}
