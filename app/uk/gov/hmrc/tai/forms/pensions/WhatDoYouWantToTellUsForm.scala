/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.tai.forms.pensions

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object WhatDoYouWantToTellUsForm {

  val pensionDetailsCharacterLimit = 500

  def form(implicit messages: Messages): Form[String] = Form(
    single(
      "pensionDetails" ->
        text
          .verifying(
            StopOnFirstFail(nonEmptyText(Messages("tai.updatePension.whatDoYouWantToTellUs.textarea.error.blank"))),
            textExceedsCharacterLimit(
              Messages(
                "tai.updatePension.whatDoYouWantToTellUs.textarea.error.maximumExceeded",
                pensionDetailsCharacterLimit))
          ))
  )

  def nonEmptyText(requiredErrMsg: String)(implicit messages: Messages): Constraint[String] =
    Constraint[String]("required") {
      case textValue: String if textValue.trim.nonEmpty => Valid
      case _                                            => Invalid(requiredErrMsg)
    }

  def textExceedsCharacterLimit(exceedErrorMsg: String)(implicit messages: Messages): Constraint[String] =
    Constraint[String]("characterLimitExceeded") {
      case textValue if textValue.trim.length <= pensionDetailsCharacterLimit => Valid
      case _                                                                  => Invalid(exceedErrorMsg)
    }
}
