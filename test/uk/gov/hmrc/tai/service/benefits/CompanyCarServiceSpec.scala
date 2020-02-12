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

package uk.gov.hmrc.tai.service.benefits

import uk.gov.hmrc.tai.connectors.CompanyCarConnector
import org.joda.time.LocalDate
import org.mockito.Mockito
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.domain.benefits.{CompanyCar, CompanyCarBenefit}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.model.domain.calculation.CodingComponent
import uk.gov.hmrc.tai.util.constants.JourneyCacheConstants

class CompanyCarServiceSpec extends PlaySpec with MockitoSugar with JourneyCacheConstants with BeforeAndAfterEach {

  override def beforeEach: Unit =
    Mockito.reset(carConnector)

  "companyCarBenefits" must {
    "return empty seq of company car benefits" when {
      "connector returns empty seq" in {
        val sut = createSut
        val codingComponents = Seq(
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"),
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"),
          CodingComponent(CarBenefit, None, 1000, "CarBenefit description")
        )
        when(carConnector.companyCarsForCurrentYearEmployments(any())(any()))
          .thenReturn(Future.successful(Seq.empty[CompanyCarBenefit]))

        val result = sut.companyCarOnCodingComponents(generateNino, codingComponents)
        Await.result(result, 5 seconds) mustBe Seq.empty[CompanyCarBenefit]
        verify(carConnector, times(1)).companyCarsForCurrentYearEmployments(any())(any())
      }

      "Coding components don't have company car benefit" in {
        val sut = createSut
        val codingComponents = Seq(
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"),
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"))

        val result = sut.companyCarOnCodingComponents(generateNino, codingComponents)
        Await.result(result, 5 seconds) mustBe Seq.empty[CompanyCarBenefit]
        verify(carConnector, times(0)).companyCarsForCurrentYearEmployments(any())(any())
      }
    }

    "return seq of company car by removing withdrawn company cars" when {
      "Coding components have company car benefit" in {
        val sut = createSut
        val codingComponents = Seq(
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"),
          CodingComponent(GiftAidPayments, None, 1000, "GiftAidPayments description"),
          CodingComponent(CarBenefit, None, 1000, "CarBenefit description")
        )

        when(carConnector.companyCarsForCurrentYearEmployments(any())(any())).thenReturn(Future.successful(companyCars))
        val result = sut.companyCarOnCodingComponents(generateNino, codingComponents)
        Await.result(result, 5 seconds) mustBe Seq(companyCar)
      }
    }
  }

  "isCompanyCarDateWithdrawn" must {
    "return false if there a company car without date withdrawn" in {
      val sut = createSut
      sut.isCompanyCarDateWithdrawn(companyCar) mustBe false
    }
    "return true" when {
      "there is a car with dateWithdrawn" in {
        val sut = createSut
        sut.isCompanyCarDateWithdrawn(companyCarWithDateWithDrawn) mustBe true
      }
      "there is a list of cars and one of them has dateWithdrawn" in {
        val sut = createSut
        sut.isCompanyCarDateWithdrawn(companyCarListWithDateWithDrawn) mustBe true
      }
    }
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val companyCar = CompanyCarBenefit(
    10,
    1000,
    List(
      CompanyCar(
        10,
        "Make Model",
        hasActiveFuelBenefit = true,
        dateMadeAvailable = Some(new LocalDate("2016-10-10")),
        dateActiveFuelBenefitMadeAvailable = Some(new LocalDate("2016-10-11")),
        dateWithdrawn = None
      )),
    Some(1)
  )

  val companyCarWithDateWithDrawn = CompanyCarBenefit(
    10,
    1000,
    List(
      CompanyCar(
        10,
        "Make Model",
        hasActiveFuelBenefit = false,
        dateMadeAvailable = Some(new LocalDate("2016-10-10")),
        dateActiveFuelBenefitMadeAvailable = Some(new LocalDate("2016-10-11")),
        dateWithdrawn = Some(new LocalDate("2017-05-12"))
      )),
    Some(1)
  )

  val companyCarListWithDateWithDrawn = CompanyCarBenefit(
    10,
    1000,
    List(
      CompanyCar(
        10,
        "Make Model",
        hasActiveFuelBenefit = false,
        dateMadeAvailable = Some(new LocalDate("2016-10-10")),
        dateActiveFuelBenefitMadeAvailable = Some(new LocalDate("2016-10-11")),
        dateWithdrawn = Some(new LocalDate("2017-05-12"))
      ),
      CompanyCar(
        11,
        "Make Model2",
        hasActiveFuelBenefit = true,
        dateMadeAvailable = Some(new LocalDate("2016-10-10")),
        dateActiveFuelBenefitMadeAvailable = Some(new LocalDate("2016-10-11")),
        dateWithdrawn = None
      )
    ),
    Some(1)
  )

  val companyCarMissingStartDates = CompanyCarBenefit(
    10,
    1000,
    List(
      CompanyCar(
        10,
        "Make Model",
        hasActiveFuelBenefit = true,
        dateMadeAvailable = None,
        dateActiveFuelBenefitMadeAvailable = None,
        dateWithdrawn = None
      )),
    Some(1)
  )

  val employment =
    Employment("The Man Plc", None, new LocalDate("2016-06-09"), None, "", "", 1, None, false, false)
  val companyCars = Seq(companyCar, companyCarWithDateWithDrawn)

  def generateNino: Nino = new Generator(new Random).nextNino

  private def createSut = new SUT

  val carConnector = mock[CompanyCarConnector]

  private class SUT extends CompanyCarService(carConnector)

}
