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

package uk.gov.hmrc.tai.service.yourTaxFreeAmount

import builders.RequestBuilder
import controllers.FakeTaiPlayApplication
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.service.benefits.CompanyCarService
import uk.gov.hmrc.tai.service.{EmploymentService, YourTaxFreeAmountService}
import uk.gov.hmrc.tai.util.yourTaxFreeAmount._
import uk.gov.hmrc.tai.viewModels.taxCodeChange.YourTaxFreeAmountViewModel

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

class DescribedYourTaxFreeAmountServiceSpec extends PlaySpec with MockitoSugar with FakeTaiPlayApplication {

  implicit val messages: Messages = play.api.i18n.Messages.Implicits.applicationMessages

  "taxFreeAmountComparison" must {
    "returns a YourTaxFreeAmountViewModel with the described comparison for previous and current" in {
      val yourTaxFreeAmountComparison = YourTaxFreeAmountComparison(
        previousTaxFreeInfo,
        currentTaxFreeInfo,
        AllowancesAndDeductionPairs(Seq.empty, Seq.empty)
      )

      when(yourTaxFreeAmountService.taxFreeAmountComparison(Matchers.eq(nino))(any(), any()))
        .thenReturn(Future.successful(yourTaxFreeAmountComparison))
      when(employmentService.employmentNames(Matchers.eq(nino), Matchers.eq(TaxYear()))(any()))
        .thenReturn(Future.successful(Map.empty[Int, String]))
      when(companyCarService.companyCars(Matchers.eq(nino))(any()))
        .thenReturn(Future.successful(Seq.empty))

      val expectedModel: YourTaxFreeAmountViewModel =
        YourTaxFreeAmountViewModel(
          previousTaxFreeInfo,
          currentTaxFreeInfo,
          Seq.empty,
          Seq.empty
        )

      val service = createTestService
      implicit val request = RequestBuilder.buildFakeRequestWithAuth("GET")
      val result = service.taxFreeAmountComparison(nino)

      Await.result(result, 5.seconds) mustBe expectedModel
    }

    "returns a translates the coding component pair to a described coding component pair for the view model" in {
      val yourTaxFreeAmountComparison = YourTaxFreeAmountComparison(
        None,
        currentTaxFreeInfo,
        AllowancesAndDeductionPairs(Seq(allowancePair), Seq(deductionPair))
      )

      when(yourTaxFreeAmountService.taxFreeAmountComparison(Matchers.eq(nino))(any(), any()))
        .thenReturn(Future.successful(yourTaxFreeAmountComparison))
      when(employmentService.employmentNames(Matchers.eq(nino), Matchers.eq(TaxYear()))(any()))
        .thenReturn(Future.successful(Map.empty[Int, String]))
      when(companyCarService.companyCars(Matchers.eq(nino))(any()))
        .thenReturn(Future.successful(Seq.empty))

      val expectedModel: YourTaxFreeAmountViewModel =
        YourTaxFreeAmountViewModel(
          None,
          currentTaxFreeInfo,
          Seq(describedAllowancePair),
          Seq(describedDeductionPair)
        )

      val service = createTestService
      implicit val request = RequestBuilder.buildFakeRequestWithAuth("GET")
      val result = service.taxFreeAmountComparison(nino)

      Await.result(result, 5.seconds) mustBe expectedModel
    }
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val nino: Nino = new Generator(new Random).nextNino
  private def createTestService = new TestService

  private val yourTaxFreeAmountService: YourTaxFreeAmountService = mock[YourTaxFreeAmountService]
  private val companyCarService: CompanyCarService = mock[CompanyCarService]
  private val employmentService: EmploymentService = mock[EmploymentService]

  private val deductionPair = CodingComponentPair(CarBenefit, Some(1), Some(1000), Some(1000))
  private val describedDeductionPair = CodingComponentPairDescription("Car benefit", 1000, 1000)
  private val allowancePair = CodingComponentPair(GiftAidPayments, None, None, Some(3000))
  private val describedAllowancePair = CodingComponentPairDescription("Gift Aid Payments", 0, 3000)

  private val previousTaxFreeInfo = Some(TaxFreeInfo("Previous", 1000, 1000))
  private val currentTaxFreeInfo = TaxFreeInfo("Current", 100, 100)

  private class TestService extends DescribedYourTaxFreeAmountService(
    yourTaxFreeAmountService: YourTaxFreeAmountService,
    companyCarService: CompanyCarService,
    employmentService: EmploymentService
  )
}