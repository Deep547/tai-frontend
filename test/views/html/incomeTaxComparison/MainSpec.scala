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

package views.html.incomeTaxComparison

import uk.gov.hmrc.tai.viewModels.incomeTaxComparison.{EstimatedIncomeTaxComparisonItem, EstimatedIncomeTaxComparisonViewModel, IncomeTaxComparisonViewModel}
import play.twirl.api.Html
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.config.ApplicationConfig
import uk.gov.hmrc.tai.viewModels.{TaxCodeComparisonViewModel, TaxFreeAmountComparisonViewModel}
import uk.gov.hmrc.tai.util.viewHelpers.TaiViewSpec


class MainSpec extends TaiViewSpec {
  "Cy plus one view" must {
    behave like pageWithTitle(incomeTaxComparisonViewModel.username + "-" + messages("tai.incomeTaxComparison.heading"))
    behave like pageWithBackButton(controllers.routes.WhatDoYouWantToDoController.whatDoYouWantToDoPage())
    behave like pageWithCombinedHeader(preHeaderText = incomeTaxComparisonViewModel.username,
      mainHeaderText = messages("tai.incomeTaxComparison.heading"),
      preHeaderAnnouncementText = Some(messages("tai.incomeTaxComparison.preHeading.screenReader")))

    "show the income tax section with heading" in {
      doc(view) must haveSectionWithId("incomeTax")
      doc(view) must haveH2HeadingWithText(messages("tai.incomeTaxComparison.incomeTax.subHeading.more", "£100"))
    }

    "show the tax codes section" in {
      doc(view) must haveSectionWithId("taxCodes")
    }

    "show the tax free amount section with heading" in {
      doc(view) must haveSectionWithId("taxFreeAmount")
      doc(view) must haveH2HeadingWithText(messages("tai.incomeTaxComparison.taxFreeAmount.subHeading"))
    }

    "have the tell us about a change section with heading" in {
      doc(view) must haveSectionWithId("tellAboutChange")
      doc(view) must haveH2HeadingWithText(messages("tai.incomeTaxComparison.tellAboutChange.subHeading"))
    }

    "have the tell us about a change links" in {
      doc(view) must haveLinkElement(id = "companyBenefitsLink",
        href = ApplicationConfig.companyBenefitsLinkUrl,
        text = messages("tai.incomeTaxComparison.tellAboutChange.companyBenefitsText"))

      doc(view) must haveLinkElement(id = "investmentIncomeLink",
        href = ApplicationConfig.investmentIncomeLinkUrl,
        text = messages("tai.incomeTaxComparison.tellAboutChange.investmentIncomeText"))

      doc(view) must haveLinkElement(id = "taxableStateBenefitLink",
        href = ApplicationConfig.taxableStateBenefitLinkUrl,
        text = messages("tai.incomeTaxComparison.tellAboutChange.stateBenefitsText"))

      doc(view) must haveLinkElement(id = "otherIncomeLink",
        href = ApplicationConfig.otherIncomeLinkUrl,
        text = messages("tai.incomeTaxComparison.tellAboutChange.otherIncomeText"))
    }

    "have the tell us about a change text" in {
      doc(view) must haveParagraphWithText(messages("tai.incomeTaxComparison.tellAboutChange.description"))
    }

    "have the sidebar heading" in {
      doc(view) must haveH2HeadingWithText(messages("tai.incomeTaxComparison.sidebar.subHeading"))
    }

    "have a sidebar with the correct link" in {
      doc(view) must haveLinkElement(id = "taxAccountSummaryLink",
        href = controllers.routes.TaxAccountSummaryController.onPageLoad().url,
        text = messages("tai.incomeTaxSummary.link"))
    }
  }

  private lazy val currentYearItem = EstimatedIncomeTaxComparisonItem(TaxYear(), 100)
  private lazy val nextYearItem = EstimatedIncomeTaxComparisonItem(TaxYear().next, 200)

  private lazy val estimatedIncomeTaxComparisonViewModel = EstimatedIncomeTaxComparisonViewModel(Seq(currentYearItem, nextYearItem))

  private lazy val incomeTaxComparisonViewModel = IncomeTaxComparisonViewModel("USERNAME", estimatedIncomeTaxComparisonViewModel,
    TaxCodeComparisonViewModel(Nil), TaxFreeAmountComparisonViewModel(Nil, Nil))

  override def view: Html = views.html.incomeTaxComparison.Main(incomeTaxComparisonViewModel)
}
