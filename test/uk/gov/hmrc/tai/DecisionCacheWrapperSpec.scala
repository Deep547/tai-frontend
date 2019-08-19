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

package uk.gov.hmrc.tai

import controllers.FakeTaiPlayApplication
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.mockito.Matchers.{any, eq => mockEq}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.mockito.{Matchers, Mockito}
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.tai.service.journeyCache.JourneyCacheService
import uk.gov.hmrc.tai.util.constants.{JourneyCacheConstants, UpdateOrRemoveCompanyBenefitDecisionConstants}
import org.mockito.Matchers.{eq => eqTo}
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.DecisionCacheWrapper.Redirect
import uk.gov.hmrc.tai.model.domain.Telephone
import play.api.test.{ResultExtractors}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DecisionCacheWrapperSpec
    extends PlaySpec with MockitoSugar with BeforeAndAfterEach with FakeTaiPlayApplication with JourneyCacheConstants
    with UpdateOrRemoveCompanyBenefitDecisionConstants with ScalaFutures {

  val journeyCacheService = mock[JourneyCacheService]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach: Unit = Mockito.reset(journeyCacheService)

  "getDecision" must {
    "return a None" when {
      "there is no cached BenefitType" in {

        when(journeyCacheService.mandatoryJourneyValue(eqTo(EndCompanyBenefit_BenefitTypeKey))(any()))
          .thenReturn(Future.successful(Left("")))

        val result = DecisionCacheWrapper.getDecision(journeyCacheService)
        whenReady(result) { r =>
          r mustBe None
        }
      }

      "there there is no cached Decision" in {
        when(journeyCacheService.mandatoryJourneyValue(eqTo(EndCompanyBenefit_BenefitTypeKey))(any()))
          .thenReturn(Future.successful(Right(Telephone.name)))
        when(journeyCacheService.currentValue(any())(any()))
          .thenReturn(Future.successful(None))

        val result = DecisionCacheWrapper.getDecision(journeyCacheService)
        whenReady(result) { r =>
          r mustBe None
        }
      }
    }

    "return the cached decision" when {
      "there is a cached value for the key given" in {
        when(journeyCacheService.mandatoryJourneyValue(eqTo(EndCompanyBenefit_BenefitTypeKey))(any()))
          .thenReturn(Future.successful(Right(Telephone.name)))
        when(journeyCacheService.currentValue(any())(any()))
          .thenReturn(Future.successful(Option(YesIGetThisBenefit)))

        val result = DecisionCacheWrapper.getDecision(journeyCacheService)
        whenReady(result) { r =>
          r mustBe Some(YesIGetThisBenefit)
        }
      }
    }
  }

  "cacheDecision" must {
    "cache the result and return the function" when {
      "we are able to generate a benefitDecisionKey" in {
        when(journeyCacheService.mandatoryJourneyValue(any())(any()))
          .thenReturn(Future.successful(Right(Telephone.name)))
        when(journeyCacheService.cache(any(), any())(any())).thenReturn(Future.successful(Map("" -> "")))
        val function = (a: String, b: Result) => b
        val result = DecisionCacheWrapper.cacheDecision(journeyCacheService, YesIGetThisBenefit, function)

        whenReady(result) { r =>
          verify(journeyCacheService, times(1)).cache(any(), any())(any())
        }
      }
    }
    "return the default redirection" when {
      "we have no benefit type cached" in {
        when(journeyCacheService.mandatoryJourneyValue(any())(any())).thenReturn(Future.successful(Left("")))

        val result =
          DecisionCacheWrapper.cacheDecision(journeyCacheService, YesIGetThisBenefit, (a: String, b: Result) => b)


        whenReady(result) { r =>
          verify(journeyCacheService, times(0)).cache(any(), eqTo(YesIGetThisBenefit))(any())
          r mustBe Redirect(controllers.routes.TaxAccountSummaryController.onPageLoad())
        }
      }
    }
  }

  "getBenefitDecisionKey" must {
    "create a compound key of the input and DecisionChoice" when {
      "given a Some" in {
        val result = DecisionCacheWrapper.getBenefitDecisionKey(Some("abc"))

        result mustBe Some("abc decisionChoice")
      }
    }

    "return a None" when {
      "given a none" in {
        val result = DecisionCacheWrapper.getBenefitDecisionKey(None)

        result mustBe None
      }
    }
  }
}
