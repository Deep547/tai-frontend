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

import controllers.actions.ValidatePerson
import controllers.auth.{AuthAction, AuthenticatedRequest}
import javax.inject.{Inject, Named}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.config.{ApplicationConfig, FeatureTogglesConfig}
import uk.gov.hmrc.tai.connectors.responses.{TaiNoCompanyCarFoundResponse, TaiSuccessResponseWithPayload}
import uk.gov.hmrc.tai.forms.UpdateOrRemoveCarForm
import uk.gov.hmrc.tai.service.SessionService
import uk.gov.hmrc.tai.service.benefits.CompanyCarService
import uk.gov.hmrc.tai.service.journeyCache.JourneyCacheService
import uk.gov.hmrc.tai.util.constants.JourneyCacheConstants
import uk.gov.hmrc.tai.viewModels.benefit.CompanyCarChoiceViewModel

import scala.concurrent.{ExecutionContext, Future}

class CompanyCarController @Inject()(companyCarService: CompanyCarService,
                                     @Named("Company Car") journeyCacheService: JourneyCacheService,
                                     sessionService: SessionService,
                                     featureTogglesConfig: FeatureTogglesConfig,
                                     applicationConfig: ApplicationConfig,
                                     authenticate: AuthAction,
                                     validatePerson: ValidatePerson,
                                     mcc: MessagesControllerComponents,
                                     updateCompanyCar: views.html.benefits.updateCompanyCar)
                                    (implicit ec: ExecutionContext)
  extends TaiBaseController(mcc)
  with JourneyCacheConstants {
  def redirectCompanyCarSelection(employmentId: Int): Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request =>
      journeyCacheService.cache(CompanyCar_EmployerIdKey, employmentId.toString) map {
       _ => Redirect(controllers.routes.CompanyCarController.getCompanyCarDetails())
      }
  }

  def getCompanyCarDetails: Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      for {
        empId <- companyCarService.companyCarEmploymentId
        response <- companyCarService.beginJourney(request.taiUser.nino, empId)
      } yield {
        implicit val user = request.taiUser
        response match {
          case TaiSuccessResponseWithPayload(x: Map[String, String]) =>
            Ok(updateCompanyCar(UpdateOrRemoveCarForm.createForm, CompanyCarChoiceViewModel(x)))
          case TaiNoCompanyCarFoundResponse(_) =>
            Redirect(applicationConfig.companyCarServiceUrl)
        }
      }
  }

  def handleUserJourneyChoice: Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request =>
      UpdateOrRemoveCarForm.createForm.bindFromRequest.fold(
        formWithErrors => {
          journeyCacheService.mandatoryValues(CompanyCar_CarModelKey, CompanyCar_CarProviderKey) flatMap { seq =>
            implicit val user = request.taiUser
            Future.successful(BadRequest(updateCompanyCar(formWithErrors, CompanyCarChoiceViewModel(seq(0), seq(1)))))
          }
        },
        formData => {
            formData.whatDoYouWantToDo match {
              case Some("removeCar") if !featureTogglesConfig.companyCarForceRedirectEnabled =>
                sessionService.invalidateCache() map (_ => Redirect(applicationConfig.companyCarDetailsUrl))
              case _ =>
                sessionService.invalidateCache() map (_ => Redirect(applicationConfig.companyCarServiceUrl))
            }
          }
      )
  }

}
