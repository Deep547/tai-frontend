/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tai.connectors

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.connectors.EmploymentsConnector.baseUrl
import uk.gov.hmrc.tai.connectors.responses.{TaiResponse, TaiSuccessResponseWithPayload, TaiTaxAccountFailureResponse}
import uk.gov.hmrc.tai.model.domain.TaxCodeHistory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TaxCodeChangeConnector {

  val serviceUrl: String

  def httpHandler: HttpHandler

  def taxCodeHistoryUrl(nino: String): String = s"$serviceUrl/tai/$nino/tax-account/tax-code-history"

  def taxCodeHistory(nino: Nino)(implicit hc: HeaderCarrier): Future[TaiResponse] = {
    httpHandler.getFromApi(taxCodeHistoryUrl(nino.nino)) map (
      json =>
        TaiSuccessResponseWithPayload((json \ "data").as[TaxCodeHistory])
      ) recover {
      case e: Exception =>
        Logger.warn(s"Couldn't retrieve tax code history for $nino with exception:${e.getMessage}")
        TaiTaxAccountFailureResponse(e.getMessage)
    }
  }

}

object TaxCodeChangeConnector extends TaxCodeChangeConnector{
  override val serviceUrl = baseUrl("tai")
  override def httpHandler: HttpHandler = HttpHandler
}