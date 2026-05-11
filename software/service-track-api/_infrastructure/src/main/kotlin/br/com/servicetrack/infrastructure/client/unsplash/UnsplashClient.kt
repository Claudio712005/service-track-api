package br.com.servicetrack.infrastructure.client.unsplash

import br.com.servicetrack.infrastructure.client.unsplash.dto.UnsplashBuscaResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "unsplash-api")
@Path("/")
interface UnsplashClient {

    @GET
    @Path("/search/photos")
    @Produces(MediaType.APPLICATION_JSON)
    fun buscarFotos(
        @QueryParam("query") consulta: String,
        @QueryParam("per_page") quantidade: Int,
        @HeaderParam("Authorization") autorizacao: String
    ): UnsplashBuscaResponse
}
