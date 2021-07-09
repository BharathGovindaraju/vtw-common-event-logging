package com.elsevier.vtw.event.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/health")
public class HealthCheckResource {
    private final ObjectMapper mapper = new ObjectMapper();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response healthCheck() {
        return buildSuccessResponse("UP");
    }

    private Response buildSuccessResponse(String status) {
        JsonNode body = getJsonNode(status);
        return Response.ok(body.toString()).build();
    }

    private JsonNode getJsonNode(String status) {
        return mapper.createObjectNode().put("status", status);
    }
}