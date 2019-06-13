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

package controllers.i18n

import controllers.actions.ValidatePerson
import controllers.auth.AuthAction
import javax.inject.Inject
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}
import uk.gov.hmrc.tai.config.FeatureTogglesConfig

class TaiLanguageController @Inject()(featureTogglesConfig: FeatureTogglesConfig,
                                      authenticate: AuthAction,
                                      validatePerson: ValidatePerson,
                                      implicit override val messagesApi: MessagesApi,
                                      implicit val application: Application) extends LanguageController {

  override protected def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  override protected def fallbackURL: String = controllers.routes.WhatDoYouWantToDoController.whatDoYouWantToDoPage.url

  protected def isWelshEnabled = featureTogglesConfig.welshLanguageEnabled

  override def switchToLanguage(language: String): Action[AnyContent] = (authenticate andThen validatePerson) {
    implicit request =>
      val newLanguage =
        if (isWelshEnabled)
          languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
        else
          LanguageUtils.getCurrentLang

      val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)

      Redirect(redirectURL).withLang(Lang(newLanguage.code))
  }
}
