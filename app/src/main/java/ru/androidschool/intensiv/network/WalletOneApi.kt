package ru.androidschool.intensiv.network

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface WalletOneApi {

    companion object {
        const val EXTERNAL_ID: String = "c1f37217-12b9-4a6e-938f-acd06025d2fd"
        const val LEGAL_ID: String = "2f663d3f-09b8-4359-8b01-bb14251f7b33"

        const val NEW_EXTERNAL_ID: String = "c1f37217-12b9-4a6e-239f-acd06025d2fd"
        const val NEW_EXTERNAL_ID1: String = "c1f37217-12b9-4a6e-459f-acd06025d4fd"
        const val NEW_EXTERNAL_ID2: String = "c1f37514-12b9-4a6e-459f-acd06025d4fd"
        const val NEW_EXTERNAL_ID3: String = "c2f45714-12b9-4a6e-459f-acd06025d4fd"
        const val NEW_EXTERNAL_ID4: String = "c3f85714-12b9-4a6e-459f-acd06025d4fd"
        const val NEW_EXTERNAL_ID5: String = "c3f85713-12b7-5a6e-459f-acd06025d4fd"
        const val NEW_EXTERNAL_ID6: String = "c3f85713-12b7-6a7e-459f-acd12025d4fd"
        const val NEW_EXTERNAL_ID7: String = "c3f85318-12b7-6a7e-459f-acd12025d4fd"

    }

    @POST("subject/person/individual/")
    fun createPerson(
        @Header("Authorization") token: String,
        @Body requestBody: CreatePersonBody
    ): Observable<Response<CreatePersonResponse>>

    @POST("invoicing/paytools/card/")
    fun bindCard(
        @Header("Authorization") token: String,
        @Body requestBody: CardBody
    ): Observable<Response<CardResponse>>

    @POST("api/smz/getIncomeRequestV2")
    fun getIncome(
        @Header("Authorization") token: String,
        @Query("externalId") externalId: String = EXTERNAL_ID,
        @Body requestBody: IncomeRequestBody
    ): Observable<Response<IncomeResponse>>

    data class IncomeRequestBody(
        val from: String,
        val inn: String,
        val limit: Int,
        val offset: Int,
        val to: String
    )


    @PUT("subject/smz/{personId}/bind")
    fun bindPerson(
        @Header("Authorization") token: String,
        @Path("personId") personId: String,
        @Body requestBody: BindPersonBody = BindPersonBody(
            listOf(
                "INCOME_REGISTRATION",
                "PAYMENT_INFORMATION",
                "INCOME_LIST",
                "INCOME_SUMMARY",
                "CANCEL_INCOME"
            ),
            externalId = EXTERNAL_ID
        )
    ): Observable<Response<BindPersonResponse>>

    @GET("subject/smz/{personId}/status/{bind_request_uuid}")
    fun checkBind(
        @Header("Authorization") token: String,
        @Path("personId") personId: String,
        @Path("bind_request_uuid") bind_request_uuid: String,
        @Query("externalId") requestBody: ExternalIdBody
    ): Observable<Response<CheckBindResponse>>

    @POST("invoicing/invoice/")
    fun createInvoice(
        @Header("Authorization") token: String,
        @Body requestBody: InvoiceBody
    ): Observable<Response<InvoiceIdResponse>>

    @PUT("invoicing/invoice/{invoiceId}/accept")
    fun acceptInvoice(
        @Header("Authorization") token: String,
        @Path("invoiceId") invoiceId: String
    ): Observable<Response<InvoiceIdResponse>>

    @GET("invoicing/invoice/{invoiceId}")
    fun checkInvoice(
        @Header("Authorization") token: String,
        @Path("invoiceId") invoiceId: String
    ): Observable<Response<InvoiceFullResponse>>
}

data class CheckBindResponse(
    val payload: CheckPayLoad,
    val externalId: String,
    val code: String
)

data class InvoiceFullResponse(
    val id: String,
    val fromPerson: FromPerson,
    val toPerson: ToPerson,
    val payload: PayLoad,
    val createDate: String,
    val modifyDate: String,
    val acceptedToPerson: Boolean,
    val states: String,
    val externalId: String
) {
    fun paymentUrl(): String {
        val url = payload.params[0].second.replace("\u003d", "=").replace("\u0026", "&")

        return url
    }

    fun getSmzBillUrl(): String? {
        return payload.params.find { it.first == "SmzBillUrl" }?.second
    }
}

data class ExternalIdBody(
    val externalId: String
)

data class InvoiceIdResponse(
    val id: String
)

data class FromPerson(
    val id: String
)

data class ToPerson(
    val id: String
)

data class TransferNote(
    val note: String
)

data class Finance(
    val userAmount: Int
)


data class CheckPayLoad(
    val inn: String,
    val permissions: List<String>,
    val processingTime: String,
    val result: String
)

data class PayLoad(
    val transferNote: TransferNote,
    val finance: Finance,
    val providerIn: String,
    val providerOut: String,
    val params: List<PayloadParams>
)

data class PayloadParams(
    val first: String,
    val second: String
)


data class InvoiceBody(
    val externalId: String,
    val fromPerson: FromPerson,
    val toPerson: ToPerson,
    val payload: PayLoad
)


data class BindPersonBody(
    val permissions: List<String>,
    val externalId: String
)

data class BindPersonResponse(
    val id: String,
    val states: String,
    val note: String
)

data class CardBody(
    val externalId: String,
    val number: String
)

data class CardResponse(
    val id: String,
    val number: String,
    val isDefault: Boolean,
    val externalId: String,
    val state: String,
    val createDate: String,
    val modifyDate: String
)

data class Initials(
    val firstName: String,
    val lastName: String,
    val patronymic: String
)

data class Inn(
    val inn: String
)

data class Phone(
    val phone: String
)

data class CreatePersonBody(
    val externalId: String,
    val nationality: String,
    val initials: Initials,
    val inn: Inn,
    val phone: Phone
)

data class CreatePersonResponse(
    val id: String
)

data class WalletOneError(
    val timestamp: String,
    val path: String,
    val status: Int,
    val error: String,
    override val message: String,
    val requestId: String
) : Throwable(message)

data class Receipt(
    val cancelationTime: String,
    val link: String,
    val operationTime: String,
    val partnerCode: String,
    val receiptId: String,
    val requestTime: String,
    val services: List<ReceiptService>,
    val totalAmount: Long
)

data class ReceiptService(
    val amount: Long,
    val name: String,
    val quantity: Long
)

data class IncomeResponse(
    val hasMore: Boolean,
    val receipts: List<Receipt>
)