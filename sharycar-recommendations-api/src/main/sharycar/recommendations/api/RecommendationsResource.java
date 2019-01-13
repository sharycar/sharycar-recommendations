package sharycar.recommendations.api;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import java.util.Map;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import jdk.nashorn.internal.runtime.JSONFunctions;

import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.cdi.LogParams;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;


@Path("/recommendations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
// Three log queries
// marker.name: ENTRY || marker.name: EXIT
// marker.name: ENTRY && contextMap.method: getCars
// marker.name: ENTRY && contextMap.method: getReservations


public class RecommendationsResource {

    @Inject
    @DiscoverService(value = "catalogue-service", version = "1.0.x", environment = "dev")
    private WebTarget target;



    /**
     * testing purpose
     * @return
     */
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public Response test() {
        return Response.ok("Status ok").build();
    }



    /**
     * testing purpose
     * @return
     */
    @GET
    @Path("url")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUrl() {
        return Response.ok(target.getUri().toString()).build();
    }


    /**
     *
     * get recommendations for current user
     *
     */
    @GET
    @Path("nearby/{lat}/{lon}/{uid}")
    @CircuitBreaker(requestVolumeThreshold = 10,failureRatio = 0.6)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "optimizeDefault")
    public Response getRecommendations(@PathParam("lat") Double lat, @PathParam("lon") Double lon,@PathParam("lon") Double uid) {

        Client client = ClientBuilder.newClient();
        //  WebTarget paymentService = client.target("http://104.197.143.157:8080");
        WebTarget paymentService = target;
        // Execute reservation on credit card
        paymentService = paymentService.path("catalogue/cars");

        Response response;
        try {
            response = paymentService.request().get();
        } catch (ProcessingException e) {
            return Response.ok("Error service call message: "+"( "+paymentService.getUri()+")"+"ERRmsg"+ e.getMessage()+ e.getStackTrace()).build();
        }

    }



}
