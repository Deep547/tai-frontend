/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import javax.inject.Inject
import controllers.actions.ValidatePerson
import controllers.auth.AuthAction
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.config.FeatureTogglesConfig
import uk.gov.hmrc.tai.connectors.responses.{TaiSuccessResponseWithPayload, TaiTaxAccountFailureResponse}
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.domain.TaxAccountSummary
import uk.gov.hmrc.tai.model.domain.income.{NonTaxCodeIncome, TaxCodeIncome}
import uk.gov.hmrc.tai.service._
import uk.gov.hmrc.tai.util.constants.{AuditConstants, TaiConstants}
import uk.gov.hmrc.tai.viewModels.TaxAccountSummaryViewModel

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class TaxAccountSummaryController @Inject()(incomeTaxSummary: views.html.incomeTaxSummary,
                                            trackingService: TrackingService,
                                            employmentService: EmploymentService,
                                            taxAccountService: TaxAccountService,
                                            auditService: AuditService,
                                            authenticate: AuthAction,
                                            validatePerson: ValidatePerson,
                                            mcc: MessagesControllerComponents,
                                            errorPagesHandler: ErrorPagesHandler)
                                           (implicit ec: ExecutionContext) extends TaiBaseController(mcc)
  with AuditConstants {

  def onPageLoad: Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request =>

      implicit val user = request.taiUser
      val nino = user.nino

      auditService.createAndSendAuditEvent(TaxAccountSummary_UserEntersSummaryPage, Map("nino" -> nino.toString()))

      (taxAccountService.taxAccountSummary(nino, TaxYear()).flatMap {
        case (TaiTaxAccountFailureResponse(message)) if message.toLowerCase.contains(TaiConstants.NpsTaxAccountDataAbsentMsg) ||
          message.toLowerCase.contains(TaiConstants.NpsNoEmploymentForCurrentTaxYear) =>
          Future.successful(Redirect(routes.NoCYIncomeTaxErrorController.noCYIncomeTaxErrorPage()))
        case TaiSuccessResponseWithPayload(taxAccountSummary: TaxAccountSummary) =>
          taxAccountSummaryViewModel(nino, taxAccountSummary) map { vm =>
            Ok(incomeTaxSummary(vm))
          }
        case _ => throw new RuntimeException("Failed to fetch tax account summary details")
      }).recover {
        case NonFatal(e) => errorPagesHandler.internalServerError(e.getMessage)
      }
  }

  private def taxAccountSummaryViewModel(nino: Nino, taxAccountSummary: TaxAccountSummary)(implicit hc: HeaderCarrier, messages: Messages) = {
    for {
      taxCodeIncomes <- taxAccountService.taxCodeIncomes(nino, TaxYear())
      nonTaxCodeIncome <- taxAccountService.nonTaxCodeIncomes(nino, TaxYear())
      employments <- employmentService.employments(nino, TaxYear())
      isAnyFormInProgress <- trackingService.isAnyIFormInProgress(nino.nino)
    } yield {
      (taxCodeIncomes, nonTaxCodeIncome) match {
        case (TaiSuccessResponseWithPayload(taxCodeIncomes: Seq[TaxCodeIncome]),
        TaiSuccessResponseWithPayload(nonTaxCodeIncome: NonTaxCodeIncome)) =>
          TaxAccountSummaryViewModel(taxCodeIncomes, employments, taxAccountSummary, isAnyFormInProgress, nonTaxCodeIncome)(messages)
        case _ => throw new RuntimeException("Failed to fetch income details")
      }
    }
  }
}
