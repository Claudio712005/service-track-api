package br.com.servicetrack.infrastructure.client.fipe

import br.com.servicetrack.infrastructure.client.fipe.dto.FipeAnoResponse
import br.com.servicetrack.infrastructure.client.fipe.dto.FipeDetalheResponse
import br.com.servicetrack.infrastructure.client.fipe.dto.FipeMarcaResponse
import br.com.servicetrack.infrastructure.client.fipe.dto.FipeModeloResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "fipe-api")
@Path("/")
interface FipeClient {

    @GET
    @Path("/cars/brands")
    @Produces(MediaType.APPLICATION_JSON)
    fun listarMarcasCarros(): List<FipeMarcaResponse>

    @GET
    @Path("/cars/brands/{codigoMarca}/models")
    @Produces(MediaType.APPLICATION_JSON)
    fun listarModelosCarros(@PathParam("codigoMarca") codigoMarca: String): List<FipeModeloResponse>

    @GET
    @Path("/cars/brands/{codigoMarca}/models/{codigoModelo}/years")
    @Produces(MediaType.APPLICATION_JSON)
    fun listarAnosCarros(
        @PathParam("codigoMarca") codigoMarca: String,
        @PathParam("codigoModelo") codigoModelo: String
    ): List<FipeAnoResponse>

    @GET
    @Path("/cars/brands/{codigoMarca}/models/{codigoModelo}/years/{ano}")
    @Produces(MediaType.APPLICATION_JSON)
    fun consultarDetalhesCarros(
        @PathParam("codigoMarca") codigoMarca: String,
        @PathParam("codigoModelo") codigoModelo: String,
        @PathParam("ano") ano: String
    ): FipeDetalheResponse
}
