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

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import sharycar.recommendations.persistence.Car;
import sharycar.recommendations.persistence.Payment;
import sharycar.recommendations.persistence.Reservation;

@Path("/recommendations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
// Three log queries
// marker.name: ENTRY || marker.name: EXIT
// marker.name: ENTRY && contextMap.method: getCars
// marker.name: ENTRY && contextMap.method: getReservations


public class CatalogueResource {

    @Inject
    @DiscoverService(value = "payment-service", version = "1.0.x", environment = "dev")
    private WebTarget target;


    @Inject
    private CatalogueConfig properties;


    @GET
    @Path("/config")
    public Response test() {
        String response =
                "{" +
                        "\"currency\": \"%s\"," +
                        "\"reservation cost\": %d" +
                        "}";
        response = String.format(
                response,
                properties.getPaymentCurrency(),
                properties.getReservationValue()

        );

        return Response.ok(response).build();
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

    @PersistenceContext
    private EntityManager em;


    /**
     *  Get all cars with reservations
     */
    @GET
    @Path("/cars")
    @Metered(name = "get_cars_requests")
    public Response getCars() {

        TypedQuery<Car> query = em.createNamedQuery("Car.findAll", Car.class);

        List<Car> cars = query.getResultList();

        return Response.ok(cars).build();

    }


    /**
     *  route optimizator
     *   @param lat
     *   @param lon
     */
    @GET
    @Path("/optimize/{lat}/{lon}")
    @Metered(name = "get_optimizations")
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "optimizeDefault")
    public Response optimize(@PathParam("lat") Double lat, @PathParam("lon") Double lon) {

        Client client = ClientBuilder.newClient();
          WebTarget targetOpt = client.target("https://smart-cargo.p.rapidapi.com/smart-cargo/resolve");

          Response r = targetOpt.request()
                  .header("X-RapidAPI-Key", "cd3a1baf20mshc9ba5dfe4c4a555p1f1bc2jsn097485ca8edd")
                  .header("Content-Type", "application/json")
                  .post(Entity.json("{\"vehicleTypes\":[{\"id\":\"abcd\",\"capacityDimension\":[{\"index\":0,\"value\":30}],\"costPerDistance\":1,\"costPerTransportTime\":10,\"costPerServiceTime\":10,\"costPerWaitingTime\":10,\"fixedCost\":0,\"maxVelocity\":40}],\"vehicles\":[{\"id\":\"Pagano\",\"type\":\"abcd\",\"returnToDepot\":true,\"startLocation\":{\"lat\":45.471668,\"lng\":9.166214}}],\"services\":[{\"name\":\"Cimarosa\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.466472,\"lng\":9.159393}},{\"name\":\"Ponti\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.441196,\"lng\":9.15202}},{\"name\":\"Washington\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.465765,\"lng\":9.155347}},{\"name\":\"Cavour\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.47287,\"lng\":9.195434}},{\"name\":\"Brera\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.471021,\"lng\":9.187609}},{\"name\":\"Moscova\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.476848,\"lng\":9.189264}},{\"name\":\"Cusani\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.468748,\"lng\":9.184237}},{\"name\":\"Giovio\",\"serviceTime\":0.2,\"dimension\":[{\"index\":0,\"value\":1}],\"location\":{\"lat\":45.464064,\"lng\":9.161008}}],\"pickups\":[],\"deliveries\":[],\"shipments\":[]}"));

        return Response.ok(JSONFunctions.quote(r.readEntity(String.class))).build();
     //   return Response.ok(JSONFunctions.quote(lat.toString())).build();
    }


    public Response optimizeDefault() {
        return Response.ok(JSONFunctions.quote("Currently not available, but we know you can do it!")).build();
    }



    /**
     *  Get all reservations
     */
    @GET
    @Path("/reservations")
    @Metered(name = "get_reservations_requests")
    public Response getReservations() {

        TypedQuery<Reservation> query = em.createNamedQuery("Reservation.findAll", Reservation.class);

        List<Reservation> reservations = query.getResultList();

        return Response.ok(reservations).build();

    }

    /**
     * Get car details by id - with reservations etc.
     * @param carId
     * @return
     */
    @GET
    @Path("/cars/{carId}")
    public Response getCarDetails(@PathParam("carId") Integer carId) {

        try {
            Query query = em.createQuery("SELECT c FROM Car c WHERE c.id = :carId");
            query.setParameter("carId", carId);
            return Response.ok(query.getResultList()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }


    /**
     * Get reservation details by id
     * @param resId
     * @return
     */
    @GET
    @Path("/reservations/{resId}")
    public Response getReservationDetails(@PathParam("resId") Integer resId) {

        try {
            Query query = em.createQuery("SELECT r FROM Reservation r WHERE r.id = :resId");
            query.setParameter("resId", resId);
            return Response.ok(query.getResultList()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    /**
     * Get reservations for users
     * @param uid
     * @return
     */
    @GET
    @Path("/reservations/user/{uid}")
    public Response getReservationsForUser(@PathParam("uid") Integer uid) {

        try {
            Query query = em.createQuery("SELECT r FROM Reservation r WHERE r.user_id = :uid");
            query.setParameter("uid", uid);
            return Response.ok(query.getResultList()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

    }

    @POST
    @Path("/cars/add")
    public Response addCar(Car car) {

        car.setId(null);
        em.getTransaction().begin();
        em.persist(car);
        em.getTransaction().commit();

        return Response.status(Response.Status.CREATED).entity(car).build();
    }


    @POST
    @Path("/reservations")
    @Timed(name = "create_reservation_time")
    public Response createReservation(Reservation reservation) {
        //@TODO call payment service and take some money from the card.
        // @TODO implement service discovery
        reservation.setId(null);
        reservation.setReservationTime(new Date());

        if (reservation.getCar() == null || reservation.getCar().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Query query = em.createQuery("SELECT c FROM Car c WHERE c.id = :carId");
        query.setParameter("carId", reservation.getCar().getId());
        if (query.getResultList().size() > 0) {
            // @TODO check if car is reserved already
            reservation.setCar((Car) query.getResultList().get(0));
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (reservation.getUser_id() == null
                || reservation.getFromDateTime() == null ) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            // Create record in database
            try {
                em.getTransaction().begin();
                em.persist(reservation);
                em.getTransaction().commit();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(reservation).build();
            }
        }

        // Error if reservation was not inserted
        if (reservation.getId() == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ERROR").build();
        }

        Client client = ClientBuilder.newClient();
      //  WebTarget paymentService = client.target("http://104.197.143.157:8080");
        WebTarget paymentService = target;
        // Execute reservation on credit card
        paymentService = paymentService.path("payments/add");



        Response response;

        Payment p = new Payment();
        p.setUser_id(reservation.getUser_id());
        p.setPrice(properties.getReservationValue().doubleValue());
        p.setCurrency(properties.getPaymentCurrency());
        p.setReservationId(reservation.getId());

        try {
            response = paymentService.request().post(Entity.json(p));
        } catch (ProcessingException e) {
            return Response.ok("Error service call message: "+"( "+paymentService.getUri()+")"+"ERRmsg"+ e.getMessage()+ e.getStackTrace()).build();
        }

//        ProxiedResponse proxiedResponse = new ProxiedResponse();
//        proxiedResponse.setResponse(response.readEntity(String.class));
//        proxiedResponse.setProxiedFrom(paymentService.getUri().toString());

        return Response.ok(reservation).build();

    }

}
