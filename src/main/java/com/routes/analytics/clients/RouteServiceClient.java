package com.routes.analytics.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.analytics.entities.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;

@Service
public class RouteServiceClient {

    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void getRestTemplate() {
        this.restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setObjectMapper(objectMapper);
        restTemplate.setMessageConverters(singletonList(jsonMessageConverter));
    }

    @Value("${routes.service.url}")
    private String routesServiceUrl;

    public List<Route> getRoutes(String country) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(routesServiceUrl + "/routes")
                .queryParam("country", country);
        ResponseEntity<List<Route>> routesResponse = restTemplate.exchange(builder.build().toUri(), GET, null,
                new ParameterizedTypeReference<List<Route>>() {
                });
        return routesResponse.getBody();
    }
}
