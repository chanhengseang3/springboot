package com.construction.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import graphql.DeferredExecutionResult;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;

@RestController
public class GraphQLController {

    private static final String PATH = "${spring.graphql.jpa.query.path:/graphql}";
    public static final String APPLICATION_GRAPHQL_VALUE = "application/graphql";

    private final GraphQLExecutor   graphQLExecutor;
    private final ObjectMapper  mapper;

    public GraphQLController(GraphQLExecutor graphQLExecutor, ObjectMapper mapper) {
        super();
        this.graphQLExecutor = graphQLExecutor;
        this.mapper = mapper;
    }
    
    @GetMapping(value = PATH,
                consumes = MediaType.TEXT_EVENT_STREAM_VALUE,
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEventStream(@RequestParam(name = "query") final String query,
                                     @RequestParam(name = "variables", required = false) final String variables) throws IOException {
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        SseEmitter sseEmitter = new SseEmitter(180_000L); // FIXME need to add parameter
        sseEmitter.onTimeout(sseEmitter::complete);
        
        if(!executionResult.getErrors().isEmpty()) {
            sseEmitter.send(executionResult.toSpecification(), MediaType.APPLICATION_JSON);
            sseEmitter.completeWithError(new RuntimeException(executionResult.getErrors().toString()));
            return sseEmitter;
        }
        
        Publisher<ExecutionResult> deferredResults = executionResult.getData(); 

        deferredResults.subscribe(new Subscriber<ExecutionResult>() {
            Subscription subscription;
            Long id = 0L;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                subscription.request(1);

                try {
                    SseEventBuilder event = wrap(executionResult);
                    
                    sseEmitter.send(event);
                } catch (IOException e) {
                    sseEmitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                sseEmitter.completeWithError(t);
            }

            @Override
            public void onComplete() {
                sseEmitter.complete();
            }
            
            SseEventBuilder wrap(ExecutionResult executionResult) {
                Map<String, Object> result = executionResult.getData();
                String name = result.keySet().iterator().next();
                
                return SseEmitter.event()
                                 .id((id++).toString())
                                 .name(name)
                                 .data(result, MediaType.APPLICATION_JSON);
                
            }
        });        
        
        return sseEmitter;
    }    

    @PostMapping(value = PATH,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void postJson(@RequestBody @Valid final GraphQLQueryRequest queryRequest,
                         HttpServletResponse httpServletResponse) throws IOException
    {
        ExecutionResult executionResult = graphQLExecutor.execute(queryRequest.getQuery(),
                                                                  queryRequest.getVariables());
        sendResponse(httpServletResponse, executionResult);
    }

    @GetMapping(value = PATH,
                consumes = {APPLICATION_GRAPHQL_VALUE},
                produces=MediaType.APPLICATION_JSON_VALUE)
    public void getQuery(@RequestParam(name = "query") final String query,
                         @RequestParam(name = "variables", required = false) final String variables,
                         HttpServletResponse httpServletResponse) throws IOException {
        
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        sendResponse(httpServletResponse, executionResult);
    }

    @PostMapping(value = PATH,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public void postForm(@RequestParam(name = "query") final String query,
                         @RequestParam(name = "variables", required = false) final String variables,
                         HttpServletResponse httpServletResponse) throws IOException    {
        Map<String, Object> variablesMap = variablesStringToMap(variables);

        ExecutionResult executionResult = graphQLExecutor.execute(query, variablesMap);
        
        sendResponse(httpServletResponse, executionResult);
    }

    @PostMapping(value = PATH,
            consumes = APPLICATION_GRAPHQL_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public void postApplicationGraphQL(@RequestBody final String query,
                                       HttpServletResponse httpServletResponse) throws IOException    {
        ExecutionResult executionResult = graphQLExecutor.execute(query, null);

        sendResponse(httpServletResponse, executionResult);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> variablesStringToMap(final String json) throws IOException {
        Map<String, Object> variables = null;

        if (json != null && !json.isEmpty())
            variables = mapper.readValue(json, Map.class);

        return variables;
    }

    @Validated
    public static class GraphQLQueryRequest {

        @NotNull
        private String query;

        private  Map<String, Object> variables;

        GraphQLQueryRequest() {}

        public GraphQLQueryRequest(String query) {
            super();
            this.query = query;
        }

        public String getQuery() {
            return this.query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getVariables() {
            return this.variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

    }
    
    private void sendResponse(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
        if (hasDeferredResults(executionResult)) {
            sendDeferredResponse(response, executionResult, executionResult.getExtensions());
        } 
        else if (hasPublisherResults(executionResult)) {
            sendMultipartResponse(response, executionResult, executionResult.getData());
        } else {
            sendNormalResponse(response, executionResult);
        }
    }

    private void sendNormalResponse(HttpServletResponse response, ExecutionResult executionResult) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getOutputStream(), executionResult.toSpecification());
    }    
    
    private boolean hasDeferredResults(ExecutionResult executionResult) {
        return Optional.ofNullable(executionResult.getExtensions())
                       .map(it -> it.containsKey(GraphQL.DEFERRED_RESULTS))
                       .orElse(false);
    }

    private boolean hasPublisherResults(ExecutionResult executionResult) {
        return Publisher.class.isInstance(executionResult.getData());
    }
    
    private static final String CRLF = "\r\n";

    @SuppressWarnings("unchecked")
    private void sendDeferredResponse(HttpServletResponse response, 
                                      ExecutionResult executionResult, 
                                      Map<Object, Object> extensions) {
        Publisher<DeferredExecutionResult> deferredResults = (Publisher<DeferredExecutionResult>) extensions.get(GraphQL.DEFERRED_RESULTS);
        try {
            sendMultipartResponse(response, executionResult, deferredResults);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMultipartResponse(HttpServletResponse response, 
                                       ExecutionResult executionResult, 
                                       Publisher<? extends ExecutionResult> deferredResults) {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "multipart/mixed; boundary=\"-\"");
        response.setHeader("Connection", "keep-alive");

        if(hasDeferredResults(executionResult)) {
           writeAndFlushPart(response, executionResult.toSpecification());
        }

        deferredResults.subscribe(new Subscriber<ExecutionResult>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(1);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                subscription.request(1);

                writeAndFlushPart(response, executionResult.toSpecification());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(System.err);
            }

            @Override
            public void onComplete() {
            }
        });

    }

    private void writeAndFlushPart(HttpServletResponse response, Map<String, Object> result) {
        DeferMultiPart deferMultiPart = new DeferMultiPart(result);
        StringBuilder sb = new StringBuilder();
        sb.append(CRLF).append("---").append(CRLF);
        String body = deferMultiPart.write();
        sb.append(body);
        writeAndFlush(response, sb);
    }

    private void writeAndFlush(HttpServletResponse response, StringBuilder sb) {
        try {
            PrintWriter writer = response.getWriter();
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private class DeferMultiPart {

        private Object body;

        public DeferMultiPart(Object data) {
            this.body = data;
        }

        public String write() {
            StringBuilder result = new StringBuilder();
            String bodyString = bodyToString();
            result.append("Content-Type: application/json").append(CRLF);
            result.append("Content-Length: ").append(bodyString.length()).append(CRLF).append(CRLF);
            result.append(bodyString);
            return result.toString();
        }

        private String bodyToString() {
            try {
                return mapper.writeValueAsString(body);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }
    }    

}
