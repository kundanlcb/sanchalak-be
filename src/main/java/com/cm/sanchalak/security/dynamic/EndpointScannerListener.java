package com.cm.sanchalak.security.dynamic;

import com.cm.sanchalak.entity.ApiEndpoint;
import com.cm.sanchalak.repository.ApiEndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@Order(90)
@RequiredArgsConstructor
public class EndpointScannerListener implements ApplicationListener<ApplicationReadyEvent> {

    private final RequestMappingHandlerMapping handlerMapping;
    private final ApiEndpointRepository apiEndpointRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Starting API Endpoint Auto-Discovery...");

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // Ignore basic error controller and swagger
            String className = handlerMethod.getBeanType().getName();
            if (className.contains("org.springframework") || className.contains("springfox")
                    || className.contains("swagger")) {
                continue;
            }

            Set<String> patterns = requestMappingInfo.getPatternValues();
            Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();

            for (String urlPattern : patterns) {
                // If the endpoint applies to multiple HTTP methods, create an entry for each
                if (methods.isEmpty()) {
                    registerOrUpdateEndpoint("ALL", urlPattern, handlerMethod);
                } else {
                    for (RequestMethod method : methods) {
                        registerOrUpdateEndpoint(method.name(), urlPattern, handlerMethod);
                    }
                }
            }
        }

        log.info("Finished API Endpoint Auto-Discovery.");
    }

    private void registerOrUpdateEndpoint(String method, String urlPattern, HandlerMethod handlerMethod) {
        apiEndpointRepository.findByMethodAndUrlPattern(method, urlPattern)
                .orElseGet(() -> {
                    // It's a new endpoint that we haven't seen before. Let's register it.
                    String moduleName = handlerMethod.getBeanType().getSimpleName().replace("Controller", "");
                    String description = handlerMethod.getMethod().getName() + " " + moduleName;

                    ApiEndpoint newEndpoint = ApiEndpoint.builder()
                            .method(method)
                            .urlPattern(urlPattern)
                            .moduleName(moduleName)
                            .description(description)
                            .build();

                    log.info("Discovered and registering new endpoint: {} {}", method, urlPattern);
                    return apiEndpointRepository.save(newEndpoint);
                });
    }
}
